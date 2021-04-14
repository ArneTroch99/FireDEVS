package util.db;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;

import facade.modeling.FCoupledModel;
import facade.modeling.FModel;
import util.SortedEnumerableList;
import util.db.sql.ModelHierarchyResultRow;
import util.tracking.TrackingType;
import view.Tracker;

public class ModelHierarchyDatabaseHelperImpl implements ModelHierarchyDatabaseHelper
{
    private FModel rootModel;
    private Map<FModel, String> modelToStringMap;
    private Map<String, FModel> stringToModelMap;
    private Map<FModel, UUID> modelIDToModelMap;
    private Delimiters delimiter;
    private Map<FModel, Tracker> trackerMap;
    
    private class HierarchyNode
    {
        private UUID nodeID;
        private List<HierarchyNode> children;
        private String nodeText;
        
        public HierarchyNode(UUID nodeID, String nodeText)
        {
            this.nodeID = nodeID;
            this.children = new ArrayList<HierarchyNode>();
            this.nodeText = nodeText;
        }
        
        public void addChild(HierarchyNode child)
        {
            this.children.add(child);
        }
        
        public List<HierarchyNode> getChildren()
        {
            return this.children;
        }
    }
    
    public enum Delimiters
    {
        ARROW("->"),
        FORWARD_SLASH("/"),
        BACKWARD_SLASH("\\");
        
        private String delimiter;
        private Delimiters(String delimiter)
        {
            this.delimiter = delimiter;
        }
        
        public String enhance(String existingPath, String childName)
        {
            return existingPath + delimiter + childName;
        }
    }
    
    public ModelHierarchyDatabaseHelperImpl(FModel model, List<Tracker> trackers)
    {
        this(model, trackers, Delimiters.ARROW);
    }
    
    public ModelHierarchyDatabaseHelperImpl(FModel model, List<Tracker> trackers, Delimiters delimiter)
    {
        FModel previous = model;
        do
        {
            previous = model;
            model = model.getParent();
        }
        while(model != null);
        
        this.rootModel = previous;
        this.modelToStringMap = new IdentityHashMap<FModel, String>();
        this.stringToModelMap = new TreeMap<String, FModel>();
        this.modelIDToModelMap = new IdentityHashMap<FModel, UUID>();

        this.delimiter = delimiter;

        initializeModelMap();
        initializeTrackerMap(trackers);
    }
    
    @Override
    public boolean hasIDFor(FModel model)
    {
        return modelIDToModelMap.containsKey(model);
    }
    
    @Override
    public Optional<UUID> getIDForModel(FModel model)
    {
        if (hasIDFor(model))
        {
            return Optional.of(modelIDToModelMap.get(model));
        }
        return Optional.empty();
    }
    
    @Override
    public void addIDFor(FModel model, UUID id)
    {
        modelIDToModelMap.put(model, id);
    }
    
    @Override
    public void mergeHierarchy(List<ModelHierarchyResultRow> models)
    {
        if (models.isEmpty())
        {
            return;
        }

        TreeMap<UUID, HierarchyNode> idNodeMap = new TreeMap<UUID, HierarchyNode>();
        ModelHierarchyResultRow rootResult = models.get(0);

        HierarchyNode root = new HierarchyNode(rootResult.modelID, rootResult.modelString);
        
        for (int i = 1; i < models.size(); ++i)
        {
            ModelHierarchyResultRow nextModel = models.get(i);
            HierarchyNode node = new HierarchyNode(nextModel.modelID, nextModel.modelString);
            idNodeMap.put(node.nodeID, node);
        }
        
        models.forEach((ModelHierarchyResultRow result) -> {
            if (result.parentID.isPresent())
            {
                if (idNodeMap.containsKey(result.parentID.get()))
                {
                    idNodeMap.get(result.parentID.get()).addChild(idNodeMap.get(result.modelID));
                }
                else if (root.nodeID.equals(result.parentID.get()))
                {
                    root.addChild(idNodeMap.get(result.modelID));
                }
            }
        });
        
        mergeHierarchyTree(root);
    }
    
    private void mergeHierarchyTree(HierarchyNode modelNode)
    {
        if (stringToModelMap.containsKey(modelNode.nodeText))
        {
            FModel model = stringToModelMap.get(modelNode.nodeText);

            addIDFor(model, modelNode.nodeID);
        }

        modelNode.getChildren().forEach((HierarchyNode node) -> mergeHierarchyTree(node));
    }
    
    private void initializeModelMap()
    {
        modelToStringMap.put(rootModel, rootModel.getName());
        stringToModelMap.put(rootModel.getName(), rootModel);
        addIDFor(rootModel, UUID.randomUUID());

        if (rootModel instanceof FCoupledModel)
        {
            FCoupledModel rootCoupledModel = (FCoupledModel) rootModel;
            
            rootCoupledModel.getChildren().forEachSorted((FModel m) -> {
                initializeModelMap(rootCoupledModel.getName(), m);
            });
        }
    }
    
    private void initializeModelMap(String prefix, FModel model)
    {
        modelToStringMap.put(model, delimiter.enhance(prefix, model.getName()));
        stringToModelMap.put(delimiter.enhance(prefix, model.getName()), model);
        addIDFor(model, UUID.randomUUID());

        if (model instanceof FCoupledModel)
        {
            FCoupledModel coupledModel = (FCoupledModel) model;
            
            coupledModel.getChildren().forEachSorted((FModel m) -> {
                initializeModelMap(delimiter.enhance(prefix, model.getName()), m);
            });
        }
    }
    
    private void initializeTrackerMap(List<Tracker> trackers)
    {
        trackerMap = new IdentityHashMap<FModel, Tracker>();
        trackers.forEach((Tracker t) -> trackerMap.put(t.getAttachedModel(), t));
    }

    @Override
    public String getFullyQualifiedNameFor(FModel model)
    {
        return modelToStringMap.get(model);
    }

    @Override
    public void forEachQualifiedName(Consumer<String> c)
    {
        modelToStringMap.forEach((FModel m, String modelString) -> c.accept(modelString));
    }

    @Override
    public FModel getRootModel()
    {
        return rootModel;
    }

    @Override
    public List<ModelHierarchyResultRow> getFlattenedRows()
    {
        List<ModelHierarchyResultRow> flattenedRows = new ArrayList<ModelHierarchyResultRow>();
        
        UUID rootModelID = modelIDToModelMap.get(rootModel);
        String modelQualifiedName = modelToStringMap.get(rootModel);
        flattenedRows.add(new ModelHierarchyResultRow(rootModelID, rootModelID, Optional.empty(), modelQualifiedName));
        if (rootModel instanceof FCoupledModel)
        {
            FCoupledModel root = (FCoupledModel) rootModel;
            SortedEnumerableList<FModel> children = root.getChildren();
            
            children.forEachSorted((FModel child) -> flattenHierarchy(rootModelID,  rootModelID, child, flattenedRows));
        }
        return flattenedRows;
    }
    
    private void flattenHierarchy(UUID rootModelID, UUID parentID, FModel model, List<ModelHierarchyResultRow> flattenedRows)
    {
        UUID modelID = modelIDToModelMap.get(model);
        String modelQualifiedName = modelToStringMap.get(model);
        flattenedRows.add(new ModelHierarchyResultRow(modelID, rootModelID, Optional.of(parentID), modelQualifiedName));

        if (model instanceof FCoupledModel)
        {
            FCoupledModel coupledModel = (FCoupledModel) model;
            SortedEnumerableList<FModel> children = coupledModel.getChildren();
            
            children.forEachSorted((FModel child) -> flattenHierarchy(rootModelID, modelID, child, flattenedRows));
        }
    }

    @Override
    public Set<String> getStateNameSet()
    {
        Set<String> states = new TreeSet<String>();

        addToStateSet(states, rootModel);

        return states;
    }
    
    private void addToStateSet(Set<String> states, FModel model)
    {
        model.getStateNames().stream().filter(
            (String s) -> trackerMap.get(model).getDataStorage().isStateTracked(s, TrackingType.DB)
        ).forEach(
            (String s) -> states.add(s)
        );
        
        if (model instanceof FCoupledModel)
        {
            ((FCoupledModel)model).getChildren().forEachSorted((FModel m) -> addToStateSet(states, m));
        }
    }

    @Override
    public Set<String> getInputPortSet()
    {
        Set<String> inputPorts = new TreeSet<String>();

        addToInputPortSet(inputPorts, rootModel);

        return inputPorts;
    }
    
    private void addToInputPortSet(Set<String> inputPorts, FModel model)
    {
        model.getInputPortNames().stream().filter(
            (String s) -> trackerMap.get(model).getDataStorage().isInputPortTracked(s, TrackingType.DB)
        ).forEach(
            (String s) -> inputPorts.add(s)
        );
        
        if (model instanceof FCoupledModel)
        {
            ((FCoupledModel)model).getChildren().forEachSorted((FModel m) -> addToInputPortSet(inputPorts, m));
        }
    }

    @Override
    public Set<String> getOutputPortSet()
    {
        Set<String> outputPorts = new TreeSet<String>();

        addToOutputPortSet(outputPorts, rootModel);
        
        return outputPorts;
    }
    
    private void addToOutputPortSet(Set<String> outputPorts, FModel model)
    {
        model.getOutputPortNames().stream().filter(
            (String s) -> trackerMap.get(model).getDataStorage().isOutputPortTracked(s, TrackingType.DB)
        ).forEach(
            (String s) -> outputPorts.add(s)
        );
        
        if (model instanceof FCoupledModel)
        {
            ((FCoupledModel)model).getChildren().forEachSorted((FModel m) -> addToOutputPortSet(outputPorts, m));
        }
    }
    
}
