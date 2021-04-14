package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

import util.tracking.EncodedTrackerDataStore;
import util.tracking.TrackerDataStore;
import util.tracking.TrackerDataStore.DataType;
import util.tracking.TrackerDataStore.TrackedVariableMetadata;

public class TestEncodedTrackerDataStore
{
    private Comparator<String> strCompare = (String lhs, String rhs) -> {
        return lhs.compareTo(rhs);
    };

    private EncodedTrackerDataStore makeInstance()
    {
        return new EncodedTrackerDataStore(
                new SortedArrayList<String>(Arrays.asList("inport1", "inport2"), strCompare),
                new SortedArrayList<String>(Arrays.asList("outport1", "outport2", "outport3"), strCompare),
                new SortedArrayList<String>(Arrays.asList("state1", "state2"), strCompare),
                new SortedArrayList<String>(Arrays.asList("tL", "tN"), strCompare)
        );
    }

    @Test
    public void testForEachEncoded()
    {
        EncodedTrackerDataStore cut = makeInstance();
        
        cut.addInputPortData("inport1", "a");
        cut.addInputPortData("inport1", "b");
        cut.addOutputPortData("outport1", "c");
        cut.addStateData("state2", "d");
        cut.addStateData("state2", "e");
        cut.addTimeData("tN", "f");
        cut.addTimeData("tL", "g");
        cut.addTimeData("tL", "g");
        
        Integer[] expected = { 100, 101, 200, 300, 301, 400, 400, 500 };
        int i[] = { 0 };
        cut.forEachData("inport1", DataType.InputPorts, (Object o) -> {
            assertEquals(expected[i[0]], o);
            ++i[0];
        });
        assertEquals(2, i[0]);
        
        cut.forEachData("outport1", DataType.OutputPorts, (Object o) -> {;
            assertEquals(expected[i[0]], o);
            ++i[0];
        });
        assertEquals(3, i[0]);
        
        cut.forEachData("state2", DataType.States, (Object o) -> {
            assertEquals(expected[i[0]], o);
            ++i[0];
        });
        assertEquals(5, i[0]);
        
        cut.forEachData("tL", DataType.Time, (Object o) -> {
            assertEquals(expected[i[0]], o);
            ++i[0];
        });
        assertEquals(7, i[0]);
        
        cut.forEachData("tN", DataType.Time, (Object o) -> {
            assertEquals(expected[i[0]], o);
            ++i[0];
        });
        assertEquals(8, i[0]);
        
        cut.forEachData("inport2", DataType.InputPorts, (Object o) -> {
            ++i[0];
        });
        assertEquals(8, i[0]);
    }
    
    @Test
    public void testForEachLegend()
    {
        EncodedTrackerDataStore cut = makeInstance();

        cut.addInputPortData("inport1", "a");
        cut.addInputPortData("inport1", "b");
        cut.addInputPortData("inport1", "g");
        cut.addOutputPortData("outport1", "c");
        cut.addStateData("state2", "d");
        cut.addStateData("state2", "e");
        cut.addTimeData("tN", "f");
        cut.addTimeData("tL", "g");
        cut.addTimeData("tL", "g");
        
        Integer[] expectedEncodings = { 100, 101, 102, 200, 201, 300, 400, 500 };
        String[] expectedMapping = { "a", "b", "g", "d", "e", "c", "f", "g" };
        int[] i = { 0 };
        cut.forEachLegend("inport1", DataType.InputPorts, (String val, Integer encoding) -> {
            assertEquals(expectedEncodings[i[0]], encoding);
            assertEquals(expectedMapping[i[0]], val);
            ++i[0];
        });
        assertEquals(3, i[0]);
        
        cut.forEachLegend("state2", DataType.States, (String val, Integer encoding) -> {
            assertEquals(expectedEncodings[i[0]], encoding);
            assertEquals(expectedMapping[i[0]], val);
            ++i[0];
        });
        assertEquals(5, i[0]);
        
        cut.forEachLegend("outport1", DataType.OutputPorts, (String val, Integer encoding) -> {
            assertEquals(expectedEncodings[i[0]], encoding);
            assertEquals(expectedMapping[i[0]], val);
            ++i[0];
        });
        assertEquals(6, i[0]);
        
        cut.forEachLegend("tN", DataType.Time, (String val, Integer encoding) -> {
            assertEquals(expectedEncodings[i[0]], encoding);
            assertEquals(expectedMapping[i[0]], val);
            ++i[0];
        });
        assertEquals(7, i[0]);
        
        cut.forEachLegend("tL", DataType.Time, (String val, Integer encoding) -> {
            assertEquals(expectedEncodings[i[0]], encoding);
            assertEquals(expectedMapping[i[0]], val);
            ++i[0];
        });
        assertEquals(8, i[0]);
    }
    
    @Test
    public void testGetData()
    {
        EncodedTrackerDataStore cut = makeInstance();

        cut.addInputPortData("inport1", "a");
        cut.addInputPortData("inport1", "b");
        cut.addOutputPortData("outport1", "c");
        cut.addStateData("state2", "d");
        cut.addStateData("state2", "e");
        cut.addTimeData("tN", "f");
        cut.addTimeData("tL", "g");
        cut.addTimeData("tL", "g");
        
        assertEquals(100, cut.getData(new TrackedVariableMetadata("inport1", TrackerDataStore.DataType.InputPorts), 0));
        assertEquals(101, cut.getData(new TrackedVariableMetadata("inport1", TrackerDataStore.DataType.InputPorts), 1));

        assertEquals(200, cut.getData(new TrackedVariableMetadata("outport1", TrackerDataStore.DataType.OutputPorts), 0));

        assertEquals(300, cut.getData(new TrackedVariableMetadata("state2", TrackerDataStore.DataType.States), 0));
        assertEquals(301, cut.getData(new TrackedVariableMetadata("state2", TrackerDataStore.DataType.States), 1));
        
        assertEquals(400, cut.getData(new TrackedVariableMetadata("tN", TrackerDataStore.DataType.Time), 0));
        
        assertEquals(500, cut.getData(new TrackedVariableMetadata("tL", TrackerDataStore.DataType.Time), 0));
        assertEquals(500, cut.getData(new TrackedVariableMetadata("tL", TrackerDataStore.DataType.Time), 1));


    }
}
