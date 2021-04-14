package util.db.sql;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.modeling.FCoupledModel;
import facade.modeling.FModel;
import util.OptionalWrapper;
import util.SortedArrayList;
import util.db.BlockingCommunicationManager;
import util.db.DatabaseConnectionConfiguration;
import util.db.DatabaseTypes;
import util.db.ModelHierarchyDatabaseHelper;
import util.db.NonBlockingCommunicationManager;
import util.db.SQLCallback;
import util.tracking.TrackingType;

public class TestPostgreSQLDatabaseRequestHandler
{
    private CutWrapper cut;
    private DatabaseConnectionConfiguration mockConfig;
    private int numThreads;
    private NonBlockingCommunicationManager mockNonBlockingCommunicationManager;
    private BlockingCommunicationManager mockBlockingCommunicationManager;
    private ModelHierarchyDatabaseHelper mockHierarchyHelper;
    
    private FModel mockRootModel;
    private String mockRootModelName;
    private UUID mockRootModelID;
    List<ModelHierarchyResultRow> mockRows;
    
    private String databaseName, host, port, userName, schema;
    
    private class CutWrapper extends PostgreSQLDatabaseRequestHandler
    {
        public CutWrapper() throws SQLException, InterruptedException
        {
            super(mockConfig, numThreads, mockNonBlockingCommunicationManager, mockBlockingCommunicationManager, mockHierarchyHelper);
        }
        
        public SQLCallback<Integer> getSimIndexParser()
        {
            return super.SimIndexResultParser;
        }
        
        public SQLCallback<UUID> getModelFromHierarchyCallback()
        {
            return super.ModelHierarchyNodeProcessor;
        }
        
        public SQLCallback<List<ModelHierarchyResultRow>> getModelHierarchyCallback()
        {
            return super.ModelHierarchyProcessor;
        }
        
        public UUID getUUID()
        {
            return previousUUID;
        }
        
        public void setUUID(UUID uuid)
        {
            previousUUID = uuid;
        }
        
        public String timeType()
        {
            return TIME_TYPE;
        }
        
        public String modelIdType()
        {
            return MODEL_ID_TYPE;
        }
        
        public String modelStringType()
        {
            return MODEL_STRING_TYPE;
        }
    }
    
    private void makeCut() throws SQLException, InterruptedException
    {
        mockConfig = mock(DatabaseConnectionConfiguration.class);
        numThreads = 2;
        mockNonBlockingCommunicationManager = mock(NonBlockingCommunicationManager.class);
        mockBlockingCommunicationManager = mock(BlockingCommunicationManager.class);
        mockHierarchyHelper = mock(ModelHierarchyDatabaseHelper.class);
        mockRootModel = mock(FModel.class);
        mockRootModelName = "a";
        
        doReturn(mockRootModel).when(mockHierarchyHelper).getRootModel();
        doReturn(mockRootModelName).when(mockHierarchyHelper).getFullyQualifiedNameFor(mockRootModel);
        doReturn(mockRootModelName).when(mockRootModel).getName();
        doReturn(mockRootModelName).when(mockRootModel).toString();

        cut = new CutWrapper();
    }
    
    private void setDefaultStateAndPortNames(FModel mockModel)
    {
        doReturn(SortedArrayList.MakeSortedStringArrayList()).when(mockModel).getStateNames();
        doReturn(SortedArrayList.MakeSortedStringArrayList()).when(mockModel).getInputPortNames();
        doReturn(SortedArrayList.MakeSortedStringArrayList()).when(mockModel).getOutputPortNames();
    }
    
    private void setupHierarchyInitializer() throws SQLException, InterruptedException
    {
        mockRootModelID = UUID.fromString("0000-00-00-00-000000");

        mockRows = new ArrayList<ModelHierarchyResultRow>();
        
        UUID modelID1 = UUID.fromString("0000-00-00-00-000001");
        UUID modelID2 = UUID.fromString("0000-00-00-00-000002");
        UUID modelID3 = UUID.fromString("0000-00-00-00-000011");
        UUID modelID4 = UUID.fromString("0000-00-00-00-000012");
        
        FCoupledModel model1 = mock(FCoupledModel.class);
        FModel model2 = mock(FModel.class);
        FModel model3 = mock(FModel.class);
        FModel model4 = mock(FModel.class);
        
        Set<String> stateNames1 = new TreeSet<String>(SortedArrayList.MakeSortedStringArrayList("Phase", "Sigma"));
        Set<String> inputPorts1 = new TreeSet<String>(SortedArrayList.MakeSortedStringArrayList("ariv", "in", "none", "solved", "start", "stop"));
        Set<String> outputPorts1 = new TreeSet<String>(SortedArrayList.MakeSortedStringArrayList("TA", "Thru", "out"));
        
        doReturn(stateNames1).when(mockHierarchyHelper).getStateNameSet();
        doReturn(inputPorts1).when(mockHierarchyHelper).getInputPortSet();
        doReturn(outputPorts1).when(mockHierarchyHelper).getOutputPortSet();
        
        doReturn(Optional.of(mockRootModelID)).when(mockHierarchyHelper).getIDForModel(mockRootModel);
        doReturn(Optional.of(modelID1)).when(mockHierarchyHelper).getIDForModel(model1);
        doReturn(Optional.of(modelID2)).when(mockHierarchyHelper).getIDForModel(model2);
        doReturn(Optional.of(modelID3)).when(mockHierarchyHelper).getIDForModel(model3);
        doReturn(Optional.of(modelID4)).when(mockHierarchyHelper).getIDForModel(model4);

        mockRows.add(new ModelHierarchyResultRow(mockRootModelID, mockRootModelID, Optional.empty(), "a"));
        mockRows.add(new ModelHierarchyResultRow(modelID1, mockRootModelID, Optional.of(mockRootModelID), "a->b"));
        mockRows.add(new ModelHierarchyResultRow(modelID3, mockRootModelID, Optional.of(modelID1), "a->b->d"));
        mockRows.add(new ModelHierarchyResultRow(modelID4, mockRootModelID, Optional.of(modelID1), "a->b->e"));
        mockRows.add(new ModelHierarchyResultRow(modelID2, mockRootModelID, Optional.of(mockRootModelID), "a->c"));
        
        doReturn(mockRows).when(mockHierarchyHelper).getFlattenedRows();
    }
    
    private void verifyHierarchyQueried() throws SQLException, InterruptedException
    {
        verify(mockBlockingCommunicationManager).executeQuery(
                "SELECT model_id\n" +
                "FROM " + schema + ".model_hierarchy\n" +
                "WHERE model_string = ?;",
            cut.getModelFromHierarchyCallback(), 
            mockRootModelName
        );
        
        verify(mockBlockingCommunicationManager).executeQuery(
                "SELECT model_id, parent_id, model_string\n" +
                "FROM " + schema + ".model_hierarchy\n" +
                "WHERE root_id = CAST(? AS UUID);", 
            cut.getModelHierarchyCallback(), 
            mockRootModelID
        );
    }
        
    private void verifyHierarchyInitialized() throws SQLException, InterruptedException
    {
        mockRows.forEach((ModelHierarchyResultRow row) -> {
            verify(mockNonBlockingCommunicationManager).executeSQL(
                    "INSERT INTO " + schema + ".model_hierarchy\n" +
                    "VALUES(CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?)\n" +
                    "ON CONFLICT DO NOTHING;",
                row.modelID,
                row.rootID,
                new OptionalWrapper<UUID>(row.parentID, UUID.class),
                row.modelString
            );
        });
    }
    
    private void verifyTablesCreated() throws SQLException, InterruptedException
    {
        verify(mockBlockingCommunicationManager, times(1)).executeSQL(
            "CREATE TABLE " + schema + ".sim_data\n" +
            "(\n" +
                "\tsim_index INTEGER,\n" +
                "\tt " + cut.timeType() + ",\n" +
                "\tt_i INTEGER,\n" +
                "\ttl " + cut.timeType() + ",\n" +
                "\ttn " + cut.timeType() + ",\n" +
                "\tsim_uid UUID\n," +
                "\tPRIMARY KEY(sim_index, t, t_i)\n" + 
            ");"
        );
        
        verify(mockBlockingCommunicationManager, times(1)).executeSQL(
            "CREATE TABLE " + schema + ".model_state_io\n" +
            "(\n" +
                "\tsim_uid UUID,\n" +
                "\tmodel_id " + "UUID,\n" +
                "\tio JSONB,\n" +
                "\tstate JSONB,\n" +
                "\tPRIMARY KEY(sim_uid, model_id)\n" +
            ");"
        );
        
        verify(mockBlockingCommunicationManager, times(1)).executeSQL(
            "CREATE TABLE " + schema + ".model_hierarchy\n" +
            "(\n" +
                "\tmodel_id " + cut.modelIdType() + ",\n" +
                "\troot_id " + cut.modelIdType() + ",\n" +
                "\tparent_id " + cut.modelIdType() + ",\n" +
                "\tmodel_string " + cut.modelStringType() + ",\n" +
                "\tPRIMARY KEY(model_id)\n" +
            ");"
        );
    }
    
    @BeforeEach
    public void beforeEach() throws SQLException, InterruptedException
    {
        makeCut();
        initMockConfig();
    }
    
    private void initMockConfig()
    {
        databaseName = "postgresdb";
        host = "localhost";
        port = "5432";
        userName = "postgresuser";
        schema = "postgresschema";
        
        ArrayList<String> configList = new ArrayList<String>();
        configList.add(databaseName);
        configList.add(host);
        configList.add(port);
        configList.add(userName);
        configList.add(schema);
        
        doReturn(configList).when(mockConfig).toList();
        doReturn(DatabaseTypes.PostgreSQL).when(mockConfig).getDatabaseType();
    }
    
    @Test
    public void testSimulationRunNumber() throws SQLException, InterruptedException
    {
        doReturn(Optional.of(3)).when(mockBlockingCommunicationManager).executeQuery(
                "SELECT MAX(sim_index)\n" +
                "FROM " + schema + ".sim_data;",
            cut.getSimIndexParser()
        );
        
        assertEquals(3, cut.getLastSimulationRunNumber());
    }
    
    @Test
    public void testSaveSimulationData()
    {
        cut.saveSimulationData(4, "2", 0, "1", "INFINITY");
        
        verify(mockNonBlockingCommunicationManager, times(1)).executeSQL(
                "INSERT INTO " + schema + ".sim_data\n" +
                "VALUES(?, ?, ?, ?, ?, CAST(? AS UUID));",
            4,
            "2",
            0,
            "1",
            "INFINITY",
            cut.getUUID().toString()
        );
    }
    
    @Test
    public void testSaveModelStateAndIO() throws SQLException, InterruptedException
    {
        setupHierarchyInitializer();
        Map<String, Object> stateData = new TreeMap<String, Object>();
        stateData.put("state1", "Obj0");
        stateData.put("state2", "Obj0_1");

        Map<String, List<Object>> inputPortData = new TreeMap<String, List<Object>>();
        
        List<Object> inport1Objects = new ArrayList<Object>();
        inport1Objects.add("Obj1");
        inport1Objects.add("Obj2");

        inputPortData.put("inport1", inport1Objects);
        
        Map<String, List<Object>> outputPortData = new TreeMap<String, List<Object>>();
        List<Object> outport1Objects = new ArrayList<Object>();
        outport1Objects.add("Obj3");
        outport1Objects.add("Obj4");

        outputPortData.put("outport1", outport1Objects);
        
        UUID simID = UUID.fromString("0000-00-00-00-00000A");
        cut.setUUID(simID);
        cut.saveModelStateAndIO(mockRootModel, stateData, inputPortData, outputPortData);

        verify(mockNonBlockingCommunicationManager, times(1)).executeSQL(
                "INSERT INTO " + schema + ".model_state_io\n" +
                "VALUES(CAST(? AS UUID), CAST(? AS UUID), CAST(? AS JSONB), CAST(? AS JSONB));",
            simID.toString(),
            mockRootModelID.toString(),
            "{\"InputPorts\":{\"inport1\":[\"Obj1\",\"Obj2\"]},\"OutputPorts\":{\"outport1\":[\"Obj3\",\"Obj4\"]}}",
            "{\"state1\":\"Obj0\",\"state2\":\"Obj0_1\"}"
        );
    }
    
    @Test
    public void testCreateTables() throws SQLException, InterruptedException
    {
        setupHierarchyInitializer();
        
        doReturn(Optional.of(mockRootModelID)).when(mockBlockingCommunicationManager).executeQuery(
                "SELECT model_id\n" +
                "FROM " + schema + ".model_hierarchy\n" +
                "WHERE model_string = ?;",
            cut.getModelFromHierarchyCallback(), 
            mockRootModelName
        );
        
        doReturn(Optional.of(mockRows)).when(mockBlockingCommunicationManager).executeQuery(
                "SELECT model_id, parent_id, model_string\n" +
                "FROM " + schema + ".model_hierarchy\n" +
                "WHERE root_id = CAST(? AS UUID);",
            cut.getModelHierarchyCallback(),
            mockRootModelID
        );
        
        cut.createTables();
        
        Thread.sleep(500);
        
        verifyTablesCreated();
        verifyHierarchyQueried();
        verifyHierarchyInitialized();
    }
    
    @Test
    public void testCreateTablesWithNoExistingHierarchy() throws SQLException, InterruptedException
    {
        setupHierarchyInitializer();
        
        doReturn(Optional.empty()).when(mockBlockingCommunicationManager).executeQuery(
                "SELECT model_id\n" +
                "FROM " + schema + ".model_hierarchy\n" +
                "WHERE model_string = ?;",
            cut.getModelFromHierarchyCallback(), 
            mockRootModelName
        );

        doReturn(Optional.empty()).when(mockBlockingCommunicationManager).executeQuery(
                "SELECT model_id, parent_id, model_string\n" +
                "FROM " + schema + ".model_hierarchy\n" +
                "WHERE root_id = CAST(? AS UUID);",
            cut.getModelHierarchyCallback(),
            mockRootModelID.toString()
        );
        
        cut.createTables();
        
        Thread.sleep(500);
        
        verifyTablesCreated();
        verifyHierarchyInitialized();
    }
    
    @Test
    public void testCreateViews() throws SQLException, InterruptedException
    {
        setupHierarchyInitializer();

        String expSql =
        "CREATE VIEW postgresschema.simulation_0 AS\n" +
        "(\n" +
            "\tSELECT\n" + 
                "\t\tsim.t,\n" +
                "\t\tsim.t_i,\n" +
                "\t\tsim.tl,\n" +
                "\t\tsim.tn,\n" +
                "\t\tmh.model_string,\n" +
                "\t\tmsio.state->'Phase' AS state_Phase,\n" +
                "\t\tmsio.state->'Sigma' AS state_Sigma,\n" +
                "\t\tmsio.io->'InputPorts'->'ariv' AS inport_ariv,\n" +
                "\t\tmsio.io->'InputPorts'->'in' AS inport_in,\n" +
                "\t\tmsio.io->'InputPorts'->'none' AS inport_none,\n" +
                "\t\tmsio.io->'InputPorts'->'solved' AS inport_solved,\n" +
                "\t\tmsio.io->'InputPorts'->'start' AS inport_start,\n" +
                "\t\tmsio.io->'InputPorts'->'stop' AS inport_stop,\n" +
                "\t\tmsio.io->'OutputPorts'->'TA' AS outport_TA,\n" +
                "\t\tmsio.io->'OutputPorts'->'Thru' AS outport_Thru,\n" +
                "\t\tmsio.io->'OutputPorts'->'out' AS outport_out\n" +
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
                "\t\tsim_index = 0\n" +
        ");";
        
        cut.createViews();
        verify(mockBlockingCommunicationManager).executeSQL(expSql);
        
        doReturn(new TreeSet<String>()).when(mockHierarchyHelper).getInputPortSet();
        
        expSql =
            "CREATE VIEW postgresschema.simulation_0 AS\n" +
            "(\n" +
                "\tSELECT\n" + 
                    "\t\tsim.t,\n" +
                    "\t\tsim.t_i,\n" +
                    "\t\tsim.tl,\n" +
                    "\t\tsim.tn,\n" +
                    "\t\tmh.model_string,\n" +
                    "\t\tmsio.state->'Phase' AS state_Phase,\n" +
                    "\t\tmsio.state->'Sigma' AS state_Sigma,\n" +
                    "\t\tmsio.io->'OutputPorts'->'TA' AS outport_TA,\n" +
                    "\t\tmsio.io->'OutputPorts'->'Thru' AS outport_Thru,\n" +
                    "\t\tmsio.io->'OutputPorts'->'out' AS outport_out\n" +
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
                    "\t\tsim_index = 0\n" +
            ");";
        
        cut.createViews();
        verify(mockBlockingCommunicationManager).executeSQL(expSql);
        
        doReturn(new TreeSet<String>()).when(mockHierarchyHelper).getStateNameSet();

        expSql =
            "CREATE VIEW postgresschema.simulation_0 AS\n" +
            "(\n" +
                "\tSELECT\n" + 
                    "\t\tsim.t,\n" +
                    "\t\tsim.t_i,\n" +
                    "\t\tsim.tl,\n" +
                    "\t\tsim.tn,\n" +
                    "\t\tmh.model_string,\n" +
                    "\t\tmsio.io->'OutputPorts'->'TA' AS outport_TA,\n" +
                    "\t\tmsio.io->'OutputPorts'->'Thru' AS outport_Thru,\n" +
                    "\t\tmsio.io->'OutputPorts'->'out' AS outport_out\n" +
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
                    "\t\tsim_index = 0\n" +
            ");";
        
        cut.createViews();
        verify(mockBlockingCommunicationManager).executeSQL(expSql);
        
        expSql =
            "CREATE VIEW postgresschema.simulation_0 AS\n" +
            "(\n" +
                "\tSELECT\n" + 
                    "\t\tsim.t,\n" +
                    "\t\tsim.t_i,\n" +
                    "\t\tsim.tl,\n" +
                    "\t\tsim.tn,\n" +
                    "\t\tmh.model_string\n" +
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
                    "\t\tsim_index = 0\n" +
            ");";
        
        doReturn(new TreeSet<String>()).when(mockHierarchyHelper).getOutputPortSet();
        
        cut.createViews();
        verify(mockBlockingCommunicationManager).executeSQL(expSql);
        
        Set<String> states = new TreeSet<String>();
        states.add("aNewState");
        doReturn(states).when(mockHierarchyHelper).getStateNameSet();
        
        expSql =
            "CREATE VIEW postgresschema.simulation_0 AS\n" +
            "(\n" +
                "\tSELECT\n" + 
                    "\t\tsim.t,\n" +
                    "\t\tsim.t_i,\n" +
                    "\t\tsim.tl,\n" +
                    "\t\tsim.tn,\n" +
                    "\t\tmh.model_string,\n" +
                    "\t\tmsio.state->'aNewState' AS state_aNewState\n" +
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
                    "\t\tsim_index = 0\n" +
            ");";
        
        cut.createViews();
        verify(mockBlockingCommunicationManager).executeSQL(expSql);
        
        doReturn(new TreeSet<String>()).when(mockHierarchyHelper).getStateNameSet();
        Set<String> inputPorts = new TreeSet<String>();
        inputPorts.add("newInport1");
        
        doReturn(inputPorts).when(mockHierarchyHelper).getInputPortSet();
        
        expSql =
            "CREATE VIEW postgresschema.simulation_0 AS\n" +
            "(\n" +
                "\tSELECT\n" + 
                    "\t\tsim.t,\n" +
                    "\t\tsim.t_i,\n" +
                    "\t\tsim.tl,\n" +
                    "\t\tsim.tn,\n" +
                    "\t\tmh.model_string,\n" +
                    "\t\tmsio.io->'InputPorts'->'newInport1' AS inport_newInport1\n" +
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
                    "\t\tsim_index = 0\n" +
            ");";
        
        cut.createViews();
        verify(mockBlockingCommunicationManager).executeSQL(expSql);
    }
    
}
