package util.db.sql;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import facade.modeling.FModel;
import util.ObjectWrapper;
import util.OptionalWrapper;
import util.db.BlockingCommunicationManager;
import util.db.DatabaseConnectionConfiguration;
import util.db.ModelHierarchyDatabaseHelper;
import util.db.NonBlockingCommunicationManager;
import util.db.PostgreSQLDatabaseConnectionConfiguration;
import util.db.SQLCallback;

public class PostgreSQLDatabaseRequestHandler implements DatabaseRequestHandler
{
    private DatabaseConnectionConfiguration dbConfig;
    private NonBlockingCommunicationManager nonBlockingCommunicationManager;
    private BlockingCommunicationManager blockingCommunicationManager;
    private ModelHierarchyDatabaseHelper hierarchyHelper;
    
    private Optional<Integer> lastSimulationRunNumber;
    private Optional<String> schema;
    
    protected UUID previousUUID;
    
    private String getSchema()
    {
        if (!schema.isPresent())
        {
            List<String> configList = dbConfig.toList();
            schema = Optional.of(configList.get(PostgreSQLDatabaseConnectionConfiguration.SCHEMA_INDEX));
        }
        return schema.get().equals("") ? schema.get() : schema.get() + ".";
    }
    protected static final String TIME_TYPE = "VARCHAR(32)";
    protected static final String MODEL_STRING_TYPE = "TEXT";
    protected static final String UUID_TYPE = "UUID";
    protected static final String MODEL_ID_TYPE = UUID_TYPE;

    private String CREATE_SIM_DATA_TABLE()
    {
        return 
        "CREATE TABLE " + getSchema() + "sim_data\n" +
        "(\n" +
            "\tsim_index INTEGER,\n" +
            "\tt " + TIME_TYPE + ",\n" +
            "\tt_i INTEGER,\n" +
            "\ttl " + TIME_TYPE + ",\n" +
            "\ttn " + TIME_TYPE + ",\n" +
            "\tsim_uid " + UUID_TYPE + "\n," +
            "\tPRIMARY KEY(sim_index, t, t_i)\n" + 
        ");";
    }
    
    private String CREATE_IO_STATE_TABLE()
    {
        return
        "CREATE TABLE " + getSchema() + "model_state_io\n" +
        "(\n" +
            "\tsim_uid " + UUID_TYPE + ",\n" +
            "\tmodel_id " + MODEL_ID_TYPE + ",\n" +
            "\tio JSONB,\n" +
            "\tstate JSONB,\n" +
            "\tPRIMARY KEY(sim_uid, model_id)\n" +
        ");";
    }
    
    private String CREATE_MODEL_HIERARCHY_TABLE()
    {
        return
        "CREATE TABLE " + getSchema() + "model_hierarchy\n" +
        "(\n" +
            "\tmodel_id " + MODEL_ID_TYPE + ",\n" +
            "\troot_id " + MODEL_ID_TYPE + ",\n" +
            "\tparent_id " + MODEL_ID_TYPE + ",\n" +
            "\tmodel_string " + MODEL_STRING_TYPE + ",\n" +
            "\tPRIMARY KEY(model_id)\n" +
        ");";
    }
    
    private String MAX_SIM_INDEX_QUERY()
    { 
        return
        "SELECT MAX(sim_index)\n" +
        "FROM " + getSchema() + "sim_data;";
    }
    
    private String GET_MODEL_FROM_HIERERCHY_QUERY()
    {
        return
        "SELECT model_id\n" +
        "FROM " + getSchema() + "model_hierarchy\n" +
        "WHERE model_string = ?;";
    }
    
    private String GET_MODELS_FOR_ROOT_QUERY()
    {
        return
        "SELECT model_id, parent_id, model_string\n" +
        "FROM " + getSchema() + "model_hierarchy\n" +
        "WHERE root_id = CAST(? AS UUID);";
    }

    private String INSERT_SIM_DATA_TEMPLATE()
    {    
        return
        "INSERT INTO " + getSchema() + "sim_data\n" +
        "VALUES(?, ?, ?, ?, ?, CAST(? AS UUID));";
    }
    
    private String INSERT_MODEL_STATE_IO_TEMPLATE()
    {    
        return
        "INSERT INTO " + getSchema() + "model_state_io\n" +
        "VALUES(CAST(? AS UUID), CAST(? AS UUID), CAST(? AS JSONB), CAST(? AS JSONB));";
    }
    
    private String INSERT_MODEL_HIERARCHY()
    {
        return
        "INSERT INTO " + getSchema() + "model_hierarchy\n" +
        "VALUES(CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?)\n" +
        "ON CONFLICT DO NOTHING;";
    }
    
    private String GET_VIEW_NAME()
    {
        return getSchema() + "simulation_" + (getLastSimulationRunNumber() + 1);
    }
    
    private String CREATE_VIEW_TEMPLATE(String query)
    {
        return
        "CREATE VIEW " + GET_VIEW_NAME() + " AS\n" +
        "(\n" +
            query + "\n" +
        ");";
    }
    
    private String CREATE_VIEW_TEMPLATE_QUERY(Set<String> states, Set<String> inputPorts, Set<String> outputPorts)
    {
        StringBuilder sb = new StringBuilder(
            "\tSELECT\n" +
                "\t\tsim.t,\n" + 
                "\t\tsim.t_i,\n" + 
                "\t\tsim.tl,\n" + 
                "\t\tsim.tn,\n" + 
                "\t\tmh.model_string"
        );
        
        if (!states.isEmpty() || !inputPorts.isEmpty() || !outputPorts.isEmpty())
        {
            sb.append(",\n");
        }
        else
        {
            sb.append("\n");
        }
        
        ObjectWrapper<Integer> i = new ObjectWrapper<Integer>(0);
        final int numStates = inputPorts.size() > 0 || outputPorts.size() > 0 ? states.size() + 1 : states.size();
        states.forEach(
            (String state) -> {
                sb.append("\t\tmsio.state->'").append(state).append("' AS state_").append(state).append(i.get() < numStates - 1 ? ",\n" : "\n");
                i.set(i.get() + 1);
            }
        );

        i.set(0);
        final int numInports = outputPorts.size() > 0 ? inputPorts.size() + 1 : inputPorts.size();
        inputPorts.forEach(
            (String inputPort) -> {
                sb.append("\t\tmsio.io->'InputPorts'->'").append(inputPort).append("' AS inport_").append(inputPort).append(i.get() < numInports - 1 ? ",\n" : "\n");
                i.set(i.get() + 1);
            }
        );

        i.set(0);
        final int numOutputPorts = outputPorts.size();
        outputPorts.forEach(
            (String outputPort) -> {
                sb.append("\t\tmsio.io->'OutputPorts'->'").append(outputPort).append("' AS outport_").append(outputPort).append(i.get() < numOutputPorts - 1 ? ",\n" : "\n");
                i.set(i.get() + 1);
            }
        );
        
        sb.append(
            "\tFROM\n" +
                "\t\tsim_data AS sim\n" +
            "\tINNER JOIN\n" +
                "\t\tmodel_state_io AS msio\n" + 
            "\tON\n" +
                "\t\tsim.sim_uid = msio.sim_uid\n" +
            "\tINNER JOIN\n" +
                "\t\tmodel_hierarchy AS mh\n" +
            "\tON\n" +
                "\t\tmsio.model_id = mh.model_id\n" +
            "\tWHERE\n" +
                "\t\tsim_index = " + (getLastSimulationRunNumber() + 1)
        );

        return sb.toString();
    }
    
    protected static final SQLCallback<Integer> SimIndexResultParser = (ResultSet result) -> {
        try
        {
            if (!result.next())
            {
                return 0;
            }
            return result.getInt(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    };
    
    protected static final SQLCallback<UUID> ModelHierarchyNodeProcessor = (ResultSet r) -> {
        UUID result = null;
        if (r.next())
        {
            result = UUID.fromString(r.getString("model_id"));
        }
        return result;
    };
    
    protected static final SQLCallback<List<ModelHierarchyResultRow>> ModelHierarchyProcessor =  (ResultSet r) -> {
        List<ModelHierarchyResultRow> result = new ArrayList<ModelHierarchyResultRow>();
        
        while(r.next())
        {
            String parent_id = r.getString("parent_id");
            Optional<UUID> parentID = Optional.empty();
            
            if (parent_id != null)
            {
                parentID = Optional.of(UUID.fromString(parent_id));
            }
            result.add(new ModelHierarchyResultRow(UUID.fromString(r.getString("model_id")), parentID, r.getString("model_string")));
        }
        
        return result;
    };
    
    private static final String DUPLICATE_TABLE_CODE = "42P07";
    
    private static final JsonSerializer<Double> DOUBLE_SERIALIZER = new JsonSerializer<Double>() {

        @Override
        public JsonElement serialize(
            Double src,
            Type typeOfSrc,
            JsonSerializationContext context
        )
        {
            if (src.isInfinite())
            {
                return new JsonPrimitive("Infinity");
            }
            else
            {
                return new JsonPrimitive(src.toString());
            }
        }
    };

    public PostgreSQLDatabaseRequestHandler(DatabaseConnectionConfiguration dbConfig, ModelHierarchyDatabaseHelper hierarchyHelper) throws SQLException, InterruptedException
    {
        this(dbConfig, hierarchyHelper, 1);
    }

    public PostgreSQLDatabaseRequestHandler(DatabaseConnectionConfiguration dbConfig, ModelHierarchyDatabaseHelper hierarchyHelper, int numThreads) throws SQLException, InterruptedException
    {
        this(dbConfig, numThreads, new NonBlockingCommunicationManager(dbConfig, numThreads), new BlockingCommunicationManager(dbConfig), hierarchyHelper);
    }
    
    protected PostgreSQLDatabaseRequestHandler(
        DatabaseConnectionConfiguration dbConfig,
        int numThreads,
        NonBlockingCommunicationManager nonBlockingCommunicationManager,
        BlockingCommunicationManager blockingCommunicationManager,
        ModelHierarchyDatabaseHelper hierarchyHelper
    ) throws SQLException, InterruptedException
    {
        this.dbConfig = dbConfig;
        this.nonBlockingCommunicationManager = nonBlockingCommunicationManager;
        this.blockingCommunicationManager = blockingCommunicationManager;
        this.hierarchyHelper = hierarchyHelper;
        
        this.schema = Optional.empty();
        this.lastSimulationRunNumber = Optional.empty();
    }
    
    private void initializeHierarchyHelper() throws SQLException, InterruptedException
    {
        String rootName = hierarchyHelper.getFullyQualifiedNameFor(hierarchyHelper.getRootModel());
        Optional<UUID> existingModelID = blockingCommunicationManager.executeQuery(GET_MODEL_FROM_HIERERCHY_QUERY(), ModelHierarchyNodeProcessor, rootName);
        
        if (existingModelID.isPresent())
        {
            Optional<List<ModelHierarchyResultRow>> preExistingModels = blockingCommunicationManager.executeQuery(GET_MODELS_FOR_ROOT_QUERY(), ModelHierarchyProcessor, existingModelID.get());
            
            if (preExistingModels.isPresent() && preExistingModels.get().size() > 0)
            {
                hierarchyHelper.mergeHierarchy(preExistingModels.get());
            }
        }
        
        List<ModelHierarchyResultRow> newRows = hierarchyHelper.getFlattenedRows();
        
        newRows.forEach(
            (ModelHierarchyResultRow row) -> {
                nonBlockingCommunicationManager.executeSQL(
                    INSERT_MODEL_HIERARCHY(),
                    row.modelID,
                    row.rootID,
                    new OptionalWrapper<UUID>(row.parentID, UUID.class),
                    row.modelString
                );
            }
        );
    }
    
    @Override
    public int getLastSimulationRunNumber()
    {
        if (lastSimulationRunNumber.isPresent())
        {
            return lastSimulationRunNumber.get();
        }

        try
        {
            Optional<Integer> _result = blockingCommunicationManager.executeQuery(MAX_SIM_INDEX_QUERY(), SimIndexResultParser);
            
            if (_result.isPresent())
            {
                lastSimulationRunNumber = Optional.of(_result.get());
                return lastSimulationRunNumber.get();
            }
        }
        catch (SQLException e)
        {
            System.err.println("Failed to execute query:");
            System.err.println(MAX_SIM_INDEX_QUERY());
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        lastSimulationRunNumber = Optional.of(-1);
        return -1;
    }

    @Override
    public void createTables()
    {
        createTableIgnoringDuplicateTable(CREATE_SIM_DATA_TABLE(), "sim_data");
        createTableIgnoringDuplicateTable(CREATE_IO_STATE_TABLE(), "model_state_io");
        createTableIgnoringDuplicateTable(CREATE_MODEL_HIERARCHY_TABLE(), "model_hierarchy");
        try
        {
            initializeHierarchyHelper();
        }
        catch (SQLException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    private void createTableIgnoringDuplicateTable(String sql, String tableName)
    {
        try
        {
            blockingCommunicationManager.executeSQL(sql);
            System.out.println("Created " + tableName + " table!");
        }
        catch (SQLException e)
        {
            if (!e.getSQLState().equals(DUPLICATE_TABLE_CODE))
            {
                System.err.println("Failed to execute create table:");
                System.err.println(sql);
                e.printStackTrace();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSimulationData(
        int simulationNumber,
        String timeString,
        int timeIndex,
        String tL,
        String tN
    )
    {
        previousUUID = UUID.randomUUID();
        nonBlockingCommunicationManager.executeSQL(INSERT_SIM_DATA_TEMPLATE(), simulationNumber, timeString, timeIndex, tL, tN, previousUUID.toString());
    }
    
    @Override
    public void saveModelStateAndIO(
        FModel model,
        Map<String, Object> stateData,
        Map<String, List<Object>> inputPortData,
        Map<String, List<Object>> outputPortData
    )
    {
        Map<String, Map<String, List<Object>>> ports = new TreeMap<String, Map<String, List<Object>>>();
        ports.put("InputPorts", inputPortData);
        ports.put("OutputPorts", outputPortData);
        
        Gson gson = new Gson();
        GsonBuilder b = gson.newBuilder();
        b.serializeSpecialFloatingPointValues();
        b.registerTypeAdapter(double.class, DOUBLE_SERIALIZER);
        b.registerTypeAdapter(Double.class, DOUBLE_SERIALIZER);
        gson = b.create();
        
        String portsJSON = gson.toJson(ports);
        String stateJSON = gson.toJson(stateData);
        
        Optional<UUID> modelID = hierarchyHelper.getIDForModel(model);
        nonBlockingCommunicationManager.executeSQL(INSERT_MODEL_STATE_IO_TEMPLATE(), previousUUID.toString(), modelID.get().toString(), portsJSON, stateJSON);
    }

    @Override
    public boolean isWorking()
    {
        return nonBlockingCommunicationManager.isExecutingDatabaseCommands();
    }

    @Override
    public void killAllWorkers(int timeout)
    {
        try
        {
            nonBlockingCommunicationManager.killAllThreads(timeout);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void setLoggingMode(boolean shouldLog)
    {
        nonBlockingCommunicationManager.setLoggingMode(shouldLog);
    }

    @Override
    public void createViews()
    {
        String sql = CREATE_VIEW_TEMPLATE(
            CREATE_VIEW_TEMPLATE_QUERY(
                hierarchyHelper.getStateNameSet(),
                hierarchyHelper.getInputPortSet(),
                hierarchyHelper.getOutputPortSet()
            )
        );

        try
        {
            blockingCommunicationManager.executeSQL(sql);
            System.out.println("Created " + GET_VIEW_NAME() + " view!");
        }
        catch (SQLException e)
        {
            System.err.println("Failed to execute create view:");
            System.err.println(sql);
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
