package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import controller.TrackingDataHandler;
import facade.modeling.FAtomicModel;
import facade.modeling.FModel;
import facade.modeling.FAtomicModelTestArtifacts.FakeViewableAtomic;
import facade.simulation.FSimulator;
import util.SortedArrayList;
import util.classUtils.DevsClassFieldFactory;
import util.tracking.EncodedTrackerDataStore;
import util.tracking.ModelTrackingDataHandler;
import util.tracking.MutableTrackerDataStore;
import util.tracking.TrackerDataStore;
import util.tracking.TrackerDataStore.DataType;

public class TestModelTrackingDataHandler
{
    private FModelView mockModelView;
    private ModelTrackingComponent mockTrackingComponent;
    private FSimulator mockSimulator;
    private FakeViewableAtomic fakeAtomic;
    private FModel fakeModel;
    private Tracker mockTracker;
    
    private TrackingDataHandler makeInstance()
    {
        mockModelView = mock(FModelView.class);
        mockTrackingComponent = mock(ModelTrackingComponent.class);
        mockSimulator = mock(FSimulator.class);
        fakeAtomic = new FakeViewableAtomic();
        fakeAtomic.setStates(DevsClassFieldFactory.createDevsClassFieldMap(fakeAtomic.getClass()));
        fakeModel = new FAtomicModel(fakeAtomic);
        fakeModel.setSimulator(mockSimulator);
        mockTracker = mock(Tracker.class);
        
        when(mockModelView.getSelectedModel()).thenReturn(fakeModel);
        when(mockTrackingComponent.findTrackerFor(fakeModel)).thenReturn(Optional.of(mockTracker));
        
        ModelTrackingDataHandler tdh = new ModelTrackingDataHandler(mockModelView);
        
        tdh.registerTrackingComponent(mockTrackingComponent);
        
        return tdh;
    }
    
    @Test
    public void testCSVExport()
    {
        TrackingDataHandler cut = makeInstance();
        
        MutableTrackerDataStore mockStore = mock(MutableTrackerDataStore.class);
        
        when(mockTracker.getDataStorage()).thenReturn(mockStore);
        
        List<TrackerDataStore.TrackedVariableMetadata> expected_meta = Arrays.asList(
                new TrackerDataStore.TrackedVariableMetadata("inport1", TrackerDataStore.DataType.InputPorts),
                new TrackerDataStore.TrackedVariableMetadata("inport2", TrackerDataStore.DataType.InputPorts),
                new TrackerDataStore.TrackedVariableMetadata("outport1", TrackerDataStore.DataType.OutputPorts),
                new TrackerDataStore.TrackedVariableMetadata("state1", TrackerDataStore.DataType.States),
                new TrackerDataStore.TrackedVariableMetadata("tL", TrackerDataStore.DataType.Time)
        );
        
        when(mockStore.getHeader()).thenReturn(expected_meta);
        
        List<String> expected_time = Arrays.asList("0", "10", "20", "25", "26");
        when(mockTrackingComponent.getHeaderRow()).thenReturn(expected_time);
        
        when(mockStore.getData(expected_meta.get(0), 0)).thenReturn("a");
        when(mockStore.getData(expected_meta.get(0), 1)).thenReturn("b");
        when(mockStore.getData(expected_meta.get(0), 2)).thenReturn("c");
        when(mockStore.getData(expected_meta.get(0), 3)).thenReturn("d");
        when(mockStore.getData(expected_meta.get(0), 4)).thenReturn("e");
        when(mockStore.dataSize(expected_meta.get(0))).thenReturn(5);

        when(mockStore.getData(expected_meta.get(1), 0)).thenReturn(Integer.valueOf(1));
        when(mockStore.getData(expected_meta.get(1), 1)).thenReturn(Integer.valueOf(2));
        when(mockStore.getData(expected_meta.get(1), 2)).thenReturn(Integer.valueOf(3));
        when(mockStore.getData(expected_meta.get(1), 3)).thenReturn(Integer.valueOf(4));
        when(mockStore.getData(expected_meta.get(1), 4)).thenReturn(Integer.valueOf(5));
        when(mockStore.dataSize(expected_meta.get(1))).thenReturn(5);

        when(mockStore.getData(expected_meta.get(2), 0)).thenReturn('z');
        when(mockStore.getData(expected_meta.get(2), 1)).thenReturn('y');
        when(mockStore.getData(expected_meta.get(2), 2)).thenReturn('x');
        when(mockStore.getData(expected_meta.get(2), 3)).thenReturn('w');
        when(mockStore.getData(expected_meta.get(2), 4)).thenReturn('v');
        when(mockStore.dataSize(expected_meta.get(2))).thenReturn(5);

        when(mockStore.getData(expected_meta.get(3), 0)).thenReturn("o");
        when(mockStore.getData(expected_meta.get(3), 1)).thenReturn("o1");
        when(mockStore.getData(expected_meta.get(3), 2)).thenReturn("o2");
        when(mockStore.getData(expected_meta.get(3), 3)).thenReturn("o3");
        when(mockStore.getData(expected_meta.get(3), 4)).thenReturn("o4");
        when(mockStore.dataSize(expected_meta.get(3))).thenReturn(5);

        when(mockStore.getData(expected_meta.get(4), 0)).thenReturn("k");
        when(mockStore.getData(expected_meta.get(4), 1)).thenReturn("k1");
        when(mockStore.getData(expected_meta.get(4), 2)).thenReturn("k2");
        when(mockStore.getData(expected_meta.get(4), 3)).thenReturn("k3");
        when(mockStore.getData(expected_meta.get(4), 4)).thenReturn("k4");
        when(mockStore.dataSize(expected_meta.get(4))).thenReturn(5);
        
        String expected = ",inport1,inport2,outport1,state1,tL\n" +
                          "0,a,1,z,o,k\n" +
                          "10,b,2,y,o1,k1\n" +
                          "20,c,3,x,o2,k2\n" +
                          "25,d,4,w,o3,k3\n" +
                          "26,e,5,v,o4,k4";
        
        String actual = cut.getCSVExport();
        assertEquals(expected, actual);
    }
    
    @Test
    public void testEncodedCSVExport()
    {
        TrackingDataHandler cut = makeInstance();
        
        EncodedTrackerDataStore mockStore = mock(EncodedTrackerDataStore.class);
        
        when(mockTracker.getDataStorage()).thenReturn(mockStore);
        
        List<TrackerDataStore.TrackedVariableMetadata> expected_meta = Arrays.asList(
                new TrackerDataStore.TrackedVariableMetadata("inport1", TrackerDataStore.DataType.InputPorts),
                new TrackerDataStore.TrackedVariableMetadata("inport2", TrackerDataStore.DataType.InputPorts),
                new TrackerDataStore.TrackedVariableMetadata("outport1", TrackerDataStore.DataType.OutputPorts),
                new TrackerDataStore.TrackedVariableMetadata("state1", TrackerDataStore.DataType.States),
                new TrackerDataStore.TrackedVariableMetadata("tL", TrackerDataStore.DataType.Time)
        );
        when(mockStore.getHeader()).thenReturn(expected_meta);
        
        List<String> expected_time = Arrays.asList("0", "10", "20", "25", "26");
        when(mockTrackingComponent.getHeaderRow()).thenReturn(expected_time);
        
        when(mockStore.getInputPortNames()).thenReturn(new SortedArrayList<String>(Arrays.asList("inport1", "inport2"), (String lhs, String rhs) -> {
                return lhs.compareTo(rhs);
        }));
        when(mockStore.getOutputPortNames()).thenReturn(new SortedArrayList<String>(Arrays.asList("outport1"), (String lhs, String rhs) -> {
            return lhs.compareTo(rhs);
        }));
        when(mockStore.getStateNames()).thenReturn(new SortedArrayList<String>(Arrays.asList("state1"), (String lhs, String rhs) -> {
            return lhs.compareTo(rhs);
        }));
        when(mockStore.getTimeDimensionNames()).thenReturn(new SortedArrayList<String>(Arrays.asList("tL"), (String lhs, String rhs) -> {
            return lhs.compareTo(rhs);
        }));
        
        @SuppressWarnings("unchecked")
        Function<Object, Answer<Object>> answerFactory = (Object o) -> {
            return (InvocationOnMock mock) -> {
                ((Consumer<Object>)mock.getArgument(2)).accept(o);
                return null;
            };
        };

        // Still calls the real method; need to rework. Class creates new object!
        List<Answer<Object>> inport1Values = Arrays.asList(
            answerFactory.apply("a"),
            answerFactory.apply("b"),
            answerFactory.apply("c"),
            answerFactory.apply("d"),
            answerFactory.apply("e")
        );
        
        Throwable[] e = { null };
        doAnswer((InvocationOnMock mock) -> {
            inport1Values.forEach((Answer<Object> a) -> { 
                try
                {
                    a.answer(mock);
                }
                catch (Throwable _e)
                {
                    e[0] = _e;
                    _e.printStackTrace();
                }
            });
            return null;
        }).when(mockStore).forEachData(eq("inport1"), eq(DataType.InputPorts), any());
        
        List<Answer<Object>> inport2Values = Arrays.asList(
                answerFactory.apply(Integer.valueOf(1)),
                answerFactory.apply(Integer.valueOf(2)),
                answerFactory.apply(Integer.valueOf(3)),
                answerFactory.apply(Integer.valueOf(4)),
                answerFactory.apply(Integer.valueOf(5))
        );

        doAnswer((InvocationOnMock mock) -> {
            inport2Values.forEach((Answer<Object> a) -> { 
                try
                {
                    a.answer(mock);
                }
                catch (Throwable _e)
                {
                    e[0] = _e;
                    _e.printStackTrace();
                }
            });
            return null;
        }).when(mockStore).forEachData(eq("inport2"), eq(DataType.InputPorts), any());

        List<Answer<Object>> outport1Values = Arrays.asList(
                answerFactory.apply('z'),
                answerFactory.apply('y'),
                answerFactory.apply('x'),
                answerFactory.apply('w'),
                answerFactory.apply('v')
        );

        doAnswer((InvocationOnMock mock) -> {
            outport1Values.forEach((Answer<Object> a) -> { 
                try
                {
                    a.answer(mock);
                }
                catch (Throwable _e)
                {
                    e[0] = _e;
                    _e.printStackTrace();
                }
            });
            return null;
        }).when(mockStore).forEachData(eq("outport1"), eq(DataType.OutputPorts), any());

        List<Answer<Object>> state1Values = Arrays.asList(
                answerFactory.apply("o"),
                answerFactory.apply("o1"),
                answerFactory.apply("o2"),
                answerFactory.apply("o3"),
                answerFactory.apply("o4")
        );
        
        doAnswer((InvocationOnMock mock) -> {
            state1Values.forEach((Answer<Object> a) -> { 
                try
                {
                    a.answer(mock);
                }
                catch (Throwable _e)
                {
                    e[0] = _e;
                    _e.printStackTrace();
                }
            });
            return null;
        }).when(mockStore).forEachData(eq("state1"), eq(DataType.States), any());

        List<Answer<Object>> tLValues = Arrays.asList(
                answerFactory.apply("k"),
                answerFactory.apply("k1"),
                answerFactory.apply("k2"),
                answerFactory.apply("k3"),
                answerFactory.apply("k4")
        );
        
        doAnswer((InvocationOnMock mock) -> {
            tLValues.forEach((Answer<Object> a) -> { 
                try
                {
                    a.answer(mock);
                }
                catch (Throwable _e)
                {
                    e[0] = _e;
                    _e.printStackTrace();
                }
            });
            return null;
        }).when(mockStore).forEachData(eq("tL"), eq(DataType.Time), any());
        
        String[] expected = {
           "<HTML>" +
               "<BODY>" +
                   "<B>Legend - fakeAtomicName</B>" +
                   "<P><B>inport1</B>" +
                   "<TABLE BORDER=\"1\" CELLPADDING=\"5\" CELLSPACING=\"1\" bordercolorlight=\"#C0C0C0\" bordercolordark=\"#C0C0C0\" bordercolor=\"#C0C0C0\" >" +
                       "<TR><TD width=\"100\"><B>Key</B></TD>" +
                       "<TD width=\"100\"><B>Value</B></TD></TR>" +
                       "<TR>" + 
                           "<TD nowrap>a</TD>" +
                           "<TD nowrap>100</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>b</TD>" +
                           "<TD nowrap>101</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>c</TD>" +
                           "<TD nowrap>102</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>d</TD>" +
                           "<TD nowrap>103</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>e</TD>" +
                           "<TD nowrap>104</TD>" +
                       "</TR>" +
                   "</TABLE>" +
                   "<P><B>inport2</B>" +
                   "<TABLE BORDER=\"1\" CELLPADDING=\"5\" CELLSPACING=\"1\" bordercolorlight=\"#C0C0C0\" bordercolordark=\"#C0C0C0\" bordercolor=\"#C0C0C0\" >" +
                       "<TR><TD width=\"100\"><B>Key</B></TD>" +
                       "<TD width=\"100\"><B>Value</B></TD></TR>" +
                       "<TR>" + 
                           "<TD nowrap>1</TD>" +
                           "<TD nowrap>200</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>2</TD>" +
                           "<TD nowrap>201</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>3</TD>" +
                           "<TD nowrap>202</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>4</TD>" +
                           "<TD nowrap>203</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>5</TD>" +
                           "<TD nowrap>204</TD>" +
                       "</TR>" +
                   "</TABLE>" +
                   "<P><B>outport1</B>" +
                   "<TABLE BORDER=\"1\" CELLPADDING=\"5\" CELLSPACING=\"1\" bordercolorlight=\"#C0C0C0\" bordercolordark=\"#C0C0C0\" bordercolor=\"#C0C0C0\" >" +
                       "<TR><TD width=\"100\"><B>Key</B></TD>" +
                       "<TD width=\"100\"><B>Value</B></TD></TR>" +
                       "<TR>" + 
                           "<TD nowrap>z</TD>" +
                           "<TD nowrap>300</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>y</TD>" +
                           "<TD nowrap>301</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>x</TD>" +
                           "<TD nowrap>302</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>w</TD>" +
                           "<TD nowrap>303</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>v</TD>" +
                           "<TD nowrap>304</TD>" +
                       "</TR>" +
                   "</TABLE>" +
                   "<P><B>state1</B>" +
                   "<TABLE BORDER=\"1\" CELLPADDING=\"5\" CELLSPACING=\"1\" bordercolorlight=\"#C0C0C0\" bordercolordark=\"#C0C0C0\" bordercolor=\"#C0C0C0\" >" +
                       "<TR><TD width=\"100\"><B>Key</B></TD>" +
                       "<TD width=\"100\"><B>Value</B></TD></TR>" +
                       "<TR>" + 
                           "<TD nowrap>o</TD>" +
                           "<TD nowrap>400</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>o1</TD>" +
                           "<TD nowrap>401</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>o2</TD>" +
                           "<TD nowrap>402</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>o3</TD>" +
                           "<TD nowrap>403</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>o4</TD>" +
                           "<TD nowrap>404</TD>" +
                       "</TR>" +
                   "</TABLE>" + 
                   "<P><B>tL</B>" +
                   "<TABLE BORDER=\"1\" CELLPADDING=\"5\" CELLSPACING=\"1\" bordercolorlight=\"#C0C0C0\" bordercolordark=\"#C0C0C0\" bordercolor=\"#C0C0C0\" >" +
                       "<TR><TD width=\"100\"><B>Key</B></TD>" +
                       "<TD width=\"100\"><B>Value</B></TD></TR>" +
                       "<TR>" + 
                           "<TD nowrap>k</TD>" +
                           "<TD nowrap>500</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>k1</TD>" +
                           "<TD nowrap>501</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>k2</TD>" +
                           "<TD nowrap>502</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>k3</TD>" +
                           "<TD nowrap>503</TD>" +
                       "</TR>" +
                       "<TR>" + 
                           "<TD nowrap>k4</TD>" +
                           "<TD nowrap>504</TD>" +
                       "</TR>" +
                   "</TABLE>" +
               "</BODY>" +
           "</HTML>",
           
           ",inport1,inport2,outport1,state1,tL\n" +
           "0,100,200,300,400,500\n" +
           "10,101,201,301,401,501\n" +
           "20,102,202,302,402,502\n" +
           "25,103,203,303,403,503\n" +
           "26,104,204,304,404,504"
        };
        String[] actual = cut.getEncodedCSVExport();
        assertNull(e[0]);
        assertEquals(Arrays.asList(expected), Arrays.asList(actual));
    }
}
