package util.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import facade.modeling.FAtomicModel;
import facade.modeling.FCoupledModel;
import facade.modeling.FModel;
import util.SortedArrayList;
import util.SortedEnumerableList;
import util.db.sql.ModelHierarchyResultRow;
import util.tracking.MutableTrackerDataStore;
import util.tracking.TrackingType;
import view.Tracker;

public class TestModelHierarchyDatabaseHelper
{
    private ModelHierarchyDatabaseHelper cut;
    private List<Tracker> mockTrackers;
    private List<MutableTrackerDataStore> mockTrackerDataStores;
    private List<FModel> mockModels;
    private List<ModelHierarchyResultRow> fakeModelResult;

    private static final Comparator<FModel> FModelComparator = new Comparator<FModel>() {

        @Override
        public int compare(FModel arg0, FModel arg1)
        {
            return SortedEnumerableList.DefaultStringComparator.compare(arg0.toString(), arg1.toString());
        }
        
    };
    
    private void setupModelHierarchy()
    {
        mockModels = new ArrayList<FModel>();

        FCoupledModel rootModel = mock(FCoupledModel.class);
        doReturn("a").when(rootModel).getName();
        doReturn("a").when(rootModel).toString();

        mockModels.add(rootModel);
        
        FCoupledModel mockModel1 = mock(FCoupledModel.class);
        doReturn("b").when(mockModel1).getName();
        doReturn("b").when(mockModel1).toString();
        
        FModel mockModel2 = mock(FAtomicModel.class);
        doReturn("c").when(mockModel2).getName();
        doReturn("c").when(mockModel2).toString();

        FModel mockModel1_1 = mock(FAtomicModel.class);
        doReturn("d").when(mockModel1_1).getName();
        doReturn("d").when(mockModel1_1).toString();

        FModel mockModel1_2 = mock(FAtomicModel.class);
        doReturn("e").when(mockModel1_2).getName();
        doReturn("e").when(mockModel1_2).toString();

        mockModels.add(mockModel1);
        mockModels.add(mockModel2);
        mockModels.add(mockModel1_1);
        mockModels.add(mockModel1_2);
        
        SortedEnumerableList<FModel> children = new SortedArrayList<FModel>(FModelComparator);
        children.add(mockModel1);
        children.add(mockModel2);
        
        doReturn(children).when(rootModel).getChildren();
        doReturn(rootModel).when(mockModel1).getParent();
        doReturn(rootModel).when(mockModel2).getParent();
        
        children = new SortedArrayList<FModel>(FModelComparator);
        children.add(mockModel1_1);
        children.add(mockModel1_2);
        
        doReturn(children).when(mockModel1).getChildren();
        doReturn(mockModel1).when(mockModel1_1).getParent();
        doReturn(mockModel1).when(mockModel1_2).getParent();
    }
    
    private void addModelResult(UUID modelID, Optional<UUID> parentID, String modelString)
    {
        if (fakeModelResult == null)
        {
            fakeModelResult = new ArrayList<ModelHierarchyResultRow>();
        }
        fakeModelResult.add(new ModelHierarchyResultRow(modelID, parentID, modelString));
    }
    
    private void createCut(FModel model, ModelHierarchyDatabaseHelperImpl.Delimiters delimiter)
    {
        mockTrackers = new ArrayList<Tracker>();
        mockTrackers.add(mock(Tracker.class));
        mockTrackers.add(mock(Tracker.class));
        mockTrackers.add(mock(Tracker.class));
        mockTrackers.add(mock(Tracker.class));
        mockTrackers.add(mock(Tracker.class));
        
        mockTrackerDataStores = new ArrayList<MutableTrackerDataStore>();
        mockTrackerDataStores.add(mock(MutableTrackerDataStore.class));
        doReturn(mockModels.get(0)).when(mockTrackers.get(0)).getAttachedModel();
        doReturn(mockTrackerDataStores.get(0)).when(mockTrackers.get(0)).getDataStorage();

        mockTrackerDataStores.add(mock(MutableTrackerDataStore.class));
        doReturn(mockModels.get(1)).when(mockTrackers.get(1)).getAttachedModel();
        doReturn(mockTrackerDataStores.get(1)).when(mockTrackers.get(1)).getDataStorage();

        mockTrackerDataStores.add(mock(MutableTrackerDataStore.class));
        doReturn(mockModels.get(2)).when(mockTrackers.get(2)).getAttachedModel();
        doReturn(mockTrackerDataStores.get(2)).when(mockTrackers.get(2)).getDataStorage();

        mockTrackerDataStores.add(mock(MutableTrackerDataStore.class));
        doReturn(mockModels.get(3)).when(mockTrackers.get(3)).getAttachedModel();
        doReturn(mockTrackerDataStores.get(3)).when(mockTrackers.get(3)).getDataStorage();

        mockTrackerDataStores.add(mock(MutableTrackerDataStore.class));
        doReturn(mockModels.get(4)).when(mockTrackers.get(4)).getAttachedModel();
        doReturn(mockTrackerDataStores.get(4)).when(mockTrackers.get(4)).getDataStorage();
        
        cut = new ModelHierarchyDatabaseHelperImpl(model, mockTrackers, delimiter);
    }
    
    private void setDefaultStateAndPortNames(FModel mockModel)
    {
        doReturn(SortedArrayList.MakeSortedStringArrayList()).when(mockModel).getStateNames();
        doReturn(SortedArrayList.MakeSortedStringArrayList()).when(mockModel).getInputPortNames();
        doReturn(SortedArrayList.MakeSortedStringArrayList()).when(mockModel).getOutputPortNames();
    }

    @Test
    public void testHierarchyQualifiedNames()
    {
        setupModelHierarchy();
        
        createCut(mockModels.get(0), ModelHierarchyDatabaseHelperImpl.Delimiters.ARROW);
        
        assertEquals("a", cut.getFullyQualifiedNameFor(mockModels.get(0)));
        assertEquals("a->b", cut.getFullyQualifiedNameFor(mockModels.get(1)));
        assertEquals("a->c", cut.getFullyQualifiedNameFor(mockModels.get(2)));
        assertEquals("a->b->d", cut.getFullyQualifiedNameFor(mockModels.get(3)));
        assertEquals("a->b->e", cut.getFullyQualifiedNameFor(mockModels.get(4)));

        createCut(mockModels.get(2), ModelHierarchyDatabaseHelperImpl.Delimiters.ARROW);
        
        assertEquals("a", cut.getFullyQualifiedNameFor(mockModels.get(0)));
        assertEquals("a->b", cut.getFullyQualifiedNameFor(mockModels.get(1)));
        assertEquals("a->c", cut.getFullyQualifiedNameFor(mockModels.get(2)));
        assertEquals("a->b->d", cut.getFullyQualifiedNameFor(mockModels.get(3)));
        assertEquals("a->b->e", cut.getFullyQualifiedNameFor(mockModels.get(4)));
        
        createCut(mockModels.get(3), ModelHierarchyDatabaseHelperImpl.Delimiters.ARROW);
        
        assertEquals("a", cut.getFullyQualifiedNameFor(mockModels.get(0)));
        assertEquals("a->b", cut.getFullyQualifiedNameFor(mockModels.get(1)));
        assertEquals("a->c", cut.getFullyQualifiedNameFor(mockModels.get(2)));
        assertEquals("a->b->d", cut.getFullyQualifiedNameFor(mockModels.get(3)));
        assertEquals("a->b->e", cut.getFullyQualifiedNameFor(mockModels.get(4)));
        
        createCut(mockModels.get(4), ModelHierarchyDatabaseHelperImpl.Delimiters.ARROW);
        
        assertEquals("a", cut.getFullyQualifiedNameFor(mockModels.get(0)));
        assertEquals("a->b", cut.getFullyQualifiedNameFor(mockModels.get(1)));
        assertEquals("a->c", cut.getFullyQualifiedNameFor(mockModels.get(2)));
        assertEquals("a->b->d", cut.getFullyQualifiedNameFor(mockModels.get(3)));
        assertEquals("a->b->e", cut.getFullyQualifiedNameFor(mockModels.get(4)));
        
        createCut(mockModels.get(0), ModelHierarchyDatabaseHelperImpl.Delimiters.FORWARD_SLASH);
        
        assertEquals("a", cut.getFullyQualifiedNameFor(mockModels.get(0)));
        assertEquals("a/b", cut.getFullyQualifiedNameFor(mockModels.get(1)));
        assertEquals("a/c", cut.getFullyQualifiedNameFor(mockModels.get(2)));
        assertEquals("a/b/d", cut.getFullyQualifiedNameFor(mockModels.get(3)));
        assertEquals("a/b/e", cut.getFullyQualifiedNameFor(mockModels.get(4)));
    }
    
    @Test
    public void testUUID()
    {
        setupModelHierarchy();
        
        createCut(mockModels.get(0), ModelHierarchyDatabaseHelperImpl.Delimiters.ARROW);
        
        UUID id = UUID.fromString("0000-00-00-00-000000");

        assertTrue(cut.hasIDFor(mockModels.get(0)));
        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(0)));
        assertNotEquals(Optional.of(id), cut.getIDForModel(mockModels.get(0)));
        
        cut.addIDFor(mockModels.get(0), id);
        
        assertTrue(cut.hasIDFor(mockModels.get(0)));
        assertEquals(Optional.of(id), cut.getIDForModel(mockModels.get(0)));
    }
    
    @Test
    public void testMergeHR()
    {
        setupModelHierarchy();
        
        createCut(mockModels.get(0), ModelHierarchyDatabaseHelperImpl.Delimiters.ARROW);

        mockModels.forEach((FModel m) -> assertTrue(cut.hasIDFor(m)));
        
        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(0)));
        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(1)));
        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(2)));
        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(3)));
        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(4)));
        
        assertNotEquals(UUID.fromString("0000-00-00-00-000000"), cut.getIDForModel(mockModels.get(0)).get());
        assertNotEquals(UUID.fromString("0000-00-00-00-000001"), cut.getIDForModel(mockModels.get(1)).get());
        assertNotEquals(UUID.fromString("0000-00-00-00-000002"), cut.getIDForModel(mockModels.get(2)).get());
        assertNotEquals(UUID.fromString("0000-00-00-00-000011"), cut.getIDForModel(mockModels.get(3)).get());
        assertNotEquals(UUID.fromString("0000-00-00-00-000012"), cut.getIDForModel(mockModels.get(4)).get());

        addModelResult(UUID.fromString("0000-00-00-00-000000"), Optional.empty(), "a");
        addModelResult(UUID.fromString("0000-00-00-00-000001"), Optional.of(UUID.fromString("0000-00-00-00-000000")), "a->b");
        addModelResult(UUID.fromString("0000-00-00-00-000002"), Optional.of(UUID.fromString("0000-00-00-00-000000")), "a->c");
        addModelResult(UUID.fromString("0000-00-00-00-000011"), Optional.of(UUID.fromString("0000-00-00-00-000001")), "a->b->d");
        addModelResult(UUID.fromString("0000-00-00-00-000012"), Optional.of(UUID.fromString("0000-00-00-00-000001")), "a->b->e");
        
        cut.mergeHierarchy(fakeModelResult);
        
        mockModels.forEach((FModel m) -> assertTrue(cut.hasIDFor(m)));
        
        assertEquals("a", cut.getFullyQualifiedNameFor(mockModels.get(0)));
        assertEquals("a->b", cut.getFullyQualifiedNameFor(mockModels.get(1)));
        assertEquals("a->c", cut.getFullyQualifiedNameFor(mockModels.get(2)));
        assertEquals("a->b->d", cut.getFullyQualifiedNameFor(mockModels.get(3)));
        assertEquals("a->b->e", cut.getFullyQualifiedNameFor(mockModels.get(4)));
        
        assertEquals(UUID.fromString("0000-00-00-00-000000"), cut.getIDForModel(mockModels.get(0)).get());
        assertEquals(UUID.fromString("0000-00-00-00-000001"), cut.getIDForModel(mockModels.get(1)).get());
        assertEquals(UUID.fromString("0000-00-00-00-000002"), cut.getIDForModel(mockModels.get(2)).get());
        assertEquals(UUID.fromString("0000-00-00-00-000011"), cut.getIDForModel(mockModels.get(3)).get());
        assertEquals(UUID.fromString("0000-00-00-00-000012"), cut.getIDForModel(mockModels.get(4)).get());
    }
    
    @Test
    public void mergeHRMismatch()
    {
        setupModelHierarchy();
        
        createCut(mockModels.get(0), ModelHierarchyDatabaseHelperImpl.Delimiters.ARROW);

        mockModels.forEach((FModel m) -> assertTrue(cut.hasIDFor(m)));
        
        assertNotEquals(Optional.of(UUID.fromString("0000-00-00-00-000000")), cut.getIDForModel(mockModels.get(0)));
        assertNotEquals(Optional.of(UUID.fromString("0000-00-00-00-000001")), cut.getIDForModel(mockModels.get(1)));
        assertNotEquals(Optional.of(UUID.fromString("0000-00-00-00-000002")), cut.getIDForModel(mockModels.get(2)));
        assertNotEquals(Optional.of(UUID.fromString("0000-00-00-00-000011")), cut.getIDForModel(mockModels.get(3)));
        assertNotEquals(Optional.of(UUID.fromString("0000-00-00-00-000012")), cut.getIDForModel(mockModels.get(4)));

        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(0)));
        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(1)));
        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(2)));
        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(3)));
        assertNotEquals(Optional.empty(), cut.getIDForModel(mockModels.get(4)));
        
        addModelResult(UUID.fromString("0000-00-00-00-000000"), Optional.empty(), "a");
        addModelResult(UUID.fromString("0000-00-00-00-000001"), Optional.of(UUID.fromString("0000-00-00-00-000000")), "a->b");
        addModelResult(UUID.fromString("0000-00-00-00-000011"), Optional.of(UUID.fromString("0000-00-00-00-000001")), "a->b->d");
        addModelResult(UUID.fromString("0000-00-00-00-000012"), Optional.of(UUID.fromString("0000-00-00-00-000001")), "a->b->z");

        cut.mergeHierarchy(fakeModelResult);
        
        assertEquals(UUID.fromString("0000-00-00-00-000000"), cut.getIDForModel(mockModels.get(0)).get());
        assertEquals(UUID.fromString("0000-00-00-00-000001"), cut.getIDForModel(mockModels.get(1)).get());
        assertNotEquals(UUID.fromString("0000-00-00-00-000002"), cut.getIDForModel(mockModels.get(2)).get());
        assertEquals(UUID.fromString("0000-00-00-00-000011"), cut.getIDForModel(mockModels.get(3)).get());
        assertNotEquals(UUID.fromString("0000-00-00-00-000012"), cut.getIDForModel(mockModels.get(4)).get());
    }
    
    @Test
    public void testGetFlattenedRows()
    {
        setupModelHierarchy();
        createCut(mockModels.get(0), ModelHierarchyDatabaseHelperImpl.Delimiters.ARROW);
        
        UUID rootID = UUID.fromString("0000-00-00-00-000000");
        cut.addIDFor(mockModels.get(0), rootID);
        cut.addIDFor(mockModels.get(1), UUID.fromString("0000-00-00-00-000001"));
        cut.addIDFor(mockModels.get(2), UUID.fromString("0000-00-00-00-000002"));
        cut.addIDFor(mockModels.get(3), UUID.fromString("0000-00-00-00-000011"));
        cut.addIDFor(mockModels.get(4), UUID.fromString("0000-00-00-00-000012"));

        List<ModelHierarchyResultRow> flattenedRows = cut.getFlattenedRows();
        
        assertEquals(new ModelHierarchyResultRow(rootID, rootID, Optional.empty(), "a"), flattenedRows.get(0));
        assertEquals(new ModelHierarchyResultRow(UUID.fromString("0000-00-00-00-000001"), rootID, Optional.of(rootID), "a->b"), flattenedRows.get(1));
        assertEquals(new ModelHierarchyResultRow(UUID.fromString("0000-00-00-00-000011"), rootID, Optional.of(UUID.fromString("0000-00-00-00-000001")), "a->b->d"), flattenedRows.get(2));
        assertEquals(new ModelHierarchyResultRow(UUID.fromString("0000-00-00-00-000012"), rootID, Optional.of(UUID.fromString("0000-00-00-00-000001")), "a->b->e"), flattenedRows.get(3));
        assertEquals(new ModelHierarchyResultRow(UUID.fromString("0000-00-00-00-000002"), rootID, Optional.of(rootID), "a->c"), flattenedRows.get(4));
    }
    
    @Test
    public void testGetSets()
    {
        setupModelHierarchy();
        createCut(mockModels.get(0), ModelHierarchyDatabaseHelperImpl.Delimiters.ARROW);
        
        SortedEnumerableList<String> stateNames1 = SortedArrayList.MakeSortedStringArrayList("state1", "state2");
        SortedEnumerableList<String> inputPorts1 = SortedArrayList.MakeSortedStringArrayList("inport1", "inport2", "inport3");
        SortedEnumerableList<String> outputPorts1 = SortedArrayList.MakeSortedStringArrayList("outport1", "outport2", "outport3");
        
        doReturn(stateNames1).when(mockModels.get(0)).getStateNames();
        doReturn(inputPorts1).when(mockModels.get(0)).getInputPortNames();
        doReturn(outputPorts1).when(mockModels.get(0)).getOutputPortNames();
        
        setDefaultStateAndPortNames(mockModels.get(1));
        setDefaultStateAndPortNames(mockModels.get(2));
        setDefaultStateAndPortNames(mockModels.get(3));
        setDefaultStateAndPortNames(mockModels.get(4));
        
        doReturn(false).when(mockTrackerDataStores.get(0)).isStateTracked(stateNames1.get(0), TrackingType.DB);
        doReturn(true).when(mockTrackerDataStores.get(0)).isStateTracked(stateNames1.get(1), TrackingType.DB);
        doReturn(true).when(mockTrackerDataStores.get(0)).isInputPortTracked(inputPorts1.get(0), TrackingType.DB);
        doReturn(false).when(mockTrackerDataStores.get(0)).isInputPortTracked(inputPorts1.get(1), TrackingType.DB);
        doReturn(true).when(mockTrackerDataStores.get(0)).isInputPortTracked(inputPorts1.get(2), TrackingType.DB);
        doReturn(true).when(mockTrackerDataStores.get(0)).isOutputPortTracked(outputPorts1.get(0), TrackingType.DB);
        doReturn(true).when(mockTrackerDataStores.get(0)).isOutputPortTracked(outputPorts1.get(1), TrackingType.DB);
        doReturn(false).when(mockTrackerDataStores.get(0)).isOutputPortTracked(outputPorts1.get(2), TrackingType.DB);
        
        Set<String> expectedStates = new TreeSet<String>();
        expectedStates.add("state2");
        
        Set<String> expectedInputPorts = new TreeSet<String>();
        expectedInputPorts.add("inport1");
        expectedInputPorts.add("inport3");
        
        Set<String> expectedOutputPorts = new TreeSet<String>();
        expectedOutputPorts.add("outport1");
        expectedOutputPorts.add("outport2");
        
        assertEquals(expectedStates, cut.getStateNameSet());
        assertEquals(expectedInputPorts, cut.getInputPortSet());
        assertEquals(expectedOutputPorts, cut.getOutputPortSet());
        
        SortedEnumerableList<String> stateNames2 = SortedArrayList.MakeSortedStringArrayList("state2", "state3");
        SortedEnumerableList<String> inputPorts2 = SortedArrayList.MakeSortedStringArrayList("inport11", "inport12");
        SortedEnumerableList<String> outputPorts2 = SortedArrayList.MakeSortedStringArrayList("outport11", "outport12", "outport13");

        doReturn(stateNames2).when(mockModels.get(1)).getStateNames();
        doReturn(inputPorts2).when(mockModels.get(1)).getInputPortNames();
        doReturn(outputPorts2).when(mockModels.get(1)).getOutputPortNames();

        doReturn(true).when(mockTrackerDataStores.get(1)).isStateTracked(stateNames2.get(0), TrackingType.DB);
        doReturn(true).when(mockTrackerDataStores.get(1)).isStateTracked(stateNames2.get(1), TrackingType.DB);
        doReturn(true).when(mockTrackerDataStores.get(1)).isInputPortTracked(inputPorts2.get(0), TrackingType.DB);
        doReturn(true).when(mockTrackerDataStores.get(1)).isInputPortTracked(inputPorts2.get(1), TrackingType.DB);
        doReturn(true).when(mockTrackerDataStores.get(1)).isOutputPortTracked(outputPorts2.get(0), TrackingType.DB);
        doReturn(false).when(mockTrackerDataStores.get(1)).isOutputPortTracked(outputPorts2.get(1), TrackingType.DB);
        doReturn(true).when(mockTrackerDataStores.get(1)).isOutputPortTracked(outputPorts2.get(2), TrackingType.DB);
        
        expectedStates.add("state3");
        
        expectedInputPorts.add("inport11");
        expectedInputPorts.add("inport12");
        
        expectedOutputPorts.add("outport11");
        expectedOutputPorts.add("outport13");
        
        assertEquals(expectedStates, cut.getStateNameSet());
        assertEquals(expectedInputPorts, cut.getInputPortSet());
        assertEquals(expectedOutputPorts, cut.getOutputPortSet());
    }
}
