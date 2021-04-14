package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import util.tracking.MutableTrackerConfiguration;
import util.tracking.MutableTrackerDataStore;
import util.tracking.MutableTrackerDataStoreImpl;
import util.tracking.TrackerDataStore;
import util.tracking.TrackingType;
import util.tracking.TrackerDataStore.DataType;

public class TestTrackerDataStoreImpl
{
    private Comparator<String> strCompare = (
        String lhs,
        String rhs
    ) -> {
        return lhs.compareTo(rhs);
    };

    private MutableTrackerDataStore makeInstance()
    {
        return new MutableTrackerDataStoreImpl(
            new SortedArrayList<String>(
                Arrays.asList("inport1", "inport2"),
                strCompare
            ),
            new SortedArrayList<String>(
                Arrays.asList("outport1", "outport2", "outport3"),
                strCompare
            ),
            new SortedArrayList<String>(
                Arrays.asList("state1", "state2"),
                strCompare
            ),
            new SortedArrayList<String>(Arrays.asList("tL", "tN"), strCompare)
        );
    }

    @Test
    public void testNewInstance()
    {
        MutableTrackerDataStore cut = makeInstance();

        assertEquals(0, cut.inputPortDataSize("inport1"));
        assertEquals(0, cut.inputPortDataSize("inport2"));
        assertEquals("", cut.getInputPortUnits("inport1"));
        assertEquals("", cut.getInputPortUnits("inport2"));
        assertEquals(
            Arrays.asList("inport1", "inport2"),
            cut.getInputPortNames()
        );
        assertEquals(0, cut.outputPortDataSize("outport1"));
        assertEquals(0, cut.outputPortDataSize("outport2"));
        assertEquals(0, cut.outputPortDataSize("outport3"));
        assertEquals("", cut.getOutputPortUnits("outport1"));
        assertEquals("", cut.getOutputPortUnits("outport2"));
        assertEquals("", cut.getOutputPortUnits("outport3"));
        assertEquals(
            Arrays.asList("outport1", "outport2", "outport3"),
            cut.getOutputPortNames()
        );
        assertEquals(0, cut.stateDataSize("state1"));
        assertEquals(0, cut.stateDataSize("state2"));
        assertEquals("", cut.getStateUnits("state1"));
        assertEquals("", cut.getStateUnits("state2"));
        assertEquals(
            Arrays.asList("state1", "state2"),
            cut.getStateNames()
        );
        assertEquals(0, cut.timeDataSize("tL"));
        assertEquals(0, cut.timeDataSize("tN"));
        assertEquals("", cut.getTimeUnits("tL"));
        assertEquals("", cut.getTimeUnits("tN"));
        assertEquals(
            Arrays.asList("tL", "tN"),
            cut.getTimeDimensionNames()
        );
        
        assertEquals(TrackerDataStore.SortOrder.Unsorted, cut.getTrackingLogSortOrder());
        assertEquals(TrackerDataStore.SortOrder.Unsorted, cut.getTimeViewSortOrder());

        List<TrackerDataStore.TrackedVariableMetadata> expectedHeaders = Arrays.asList(
            new TrackerDataStore.TrackedVariableMetadata(
                "inport1",
                TrackerDataStore.DataType.InputPorts
            ),
            new TrackerDataStore.TrackedVariableMetadata(
                "inport2",
                TrackerDataStore.DataType.InputPorts
            ),
            new TrackerDataStore.TrackedVariableMetadata(
                "outport1",
                TrackerDataStore.DataType.OutputPorts
            ),
            new TrackerDataStore.TrackedVariableMetadata(
                "outport2",
                TrackerDataStore.DataType.OutputPorts
            ),
            new TrackerDataStore.TrackedVariableMetadata(
                "outport3",
                TrackerDataStore.DataType.OutputPorts
            ),
            new TrackerDataStore.TrackedVariableMetadata(
                "state1",
                TrackerDataStore.DataType.States
            ),
            new TrackerDataStore.TrackedVariableMetadata(
                "state2",
                TrackerDataStore.DataType.States
            ),
            new TrackerDataStore.TrackedVariableMetadata(
                "tL",
                TrackerDataStore.DataType.Time
            ),
            new TrackerDataStore.TrackedVariableMetadata(
                "tN",
                TrackerDataStore.DataType.Time
            )
        );

        Iterator<TrackerDataStore.TrackedVariableMetadata> expected = expectedHeaders.iterator();
        Iterator<TrackerDataStore.TrackedVariableMetadata> actual = cut.getHeader().iterator();
        while (expected.hasNext())
        {
            assertEquals(expected.next(), actual.next());
        }
        assertFalse(actual.hasNext());

        try
        {
            cut.inputPortDataSize("not inport");
            fail("Expected exception thrown!");
        }
        catch (NullPointerException e)
        {
            // Success
        }

        try
        {
            cut.outputPortDataSize("not outport");
            fail("Expected exception thrown!");
        }
        catch (NullPointerException e)
        {
            // Success
        }

        try
        {
            cut.stateDataSize("not state");
            fail("Expected exception thrown!");
        }
        catch (NullPointerException e)
        {
            // Success
        }

        try
        {
            cut.timeDataSize("not time");
            fail("Expected exception thrown!");
        }
        catch (NullPointerException e)
        {
            // Success
        }
    }

    @Test
    public void testAddInputPortData()
    {
        MutableTrackerDataStore cut = makeInstance();

        assertTrue(cut.addInputPortData("inport1", "some value"));
        assertTrue(cut.addInputPortData("inport2", new Object() {
            @Override
            public String toString()
            {
                return "some other object";
            }
        }));

        assertEquals(1, cut.inputPortDataSize("inport1"));
        assertEquals(
            "some value",
            cut.getInputPortData("inport1", 0)
        );

        assertEquals(1, cut.inputPortDataSize("inport2"));
        assertEquals(
            "some other object",
            cut.getInputPortData("inport2", 0).toString()
        );

        assertFalse(cut.addInputPortData("asdf", "a third value"));
        try
        {
            cut.getInputPortData("asdf", 0);
            fail("Exception expected!");
        }
        catch (NullPointerException e)
        {
            // Success
        }
    }

    @Test
    public void testForEachData()
    {
        MutableTrackerDataStore cut = makeInstance();

        assertEquals(0, cut.outputPortDataSize("outport1"));

        assertTrue(cut.addOutputPortData("outport1", "c"));
        assertTrue(cut.addOutputPortData("outport1", "b"));
        assertTrue(cut.addOutputPortData("outport1", "a"));

        String[] expected = {
            "c",
            "b",
            "a"
        };
        int i[] = {
            0
        };
        cut.forEachData(
            "outport1",
            DataType.OutputPorts,
            (Object o) -> {
                assertEquals(expected[i[0]], (String) o);
                ++i[0];
            }
        );
        assertEquals(3, i[0]);

        assertEquals(0, cut.inputPortDataSize("inport1"));
        assertEquals(0, cut.inputPortDataSize("inport2"));
        assertEquals(3, cut.outputPortDataSize("outport1"));
        assertEquals(0, cut.outputPortDataSize("outport2"));
        assertEquals(0, cut.outputPortDataSize("outport3"));
        assertEquals(0, cut.stateDataSize("state1"));
        assertEquals(0, cut.stateDataSize("state2"));
        assertEquals(0, cut.timeDataSize("tL"));
        assertEquals(0, cut.timeDataSize("tN"));

        assertTrue(cut.addInputPortData("inport2", "a"));
        assertTrue(cut.addInputPortData("inport2", "b"));
        assertTrue(cut.addInputPortData("inport2", "c"));

        cut.forEachData(
            "inport2",
            DataType.InputPorts,
            (Object o) -> {
                --i[0];
                assertEquals(expected[i[0]], (String) o);
            }
        );
        assertEquals(0, i[0]);

        assertEquals(3, cut.inputPortDataSize("inport2"));

        assertTrue(cut.addStateData("state2", "c"));
        assertTrue(cut.addStateData("state2", "b"));
        assertTrue(cut.addStateData("state2", "a"));

        cut.forEachData("state2", DataType.States, (Object o) -> {
            assertEquals(expected[i[0]], (String) o);
            ++i[0];
        });
        assertEquals(3, i[0]);

        assertEquals(3, cut.stateDataSize("state2"));

        assertTrue(cut.addTimeData("tL", "a"));
        assertTrue(cut.addTimeData("tL", "b"));
        assertTrue(cut.addTimeData("tL", "c"));

        cut.forEachData("tL", DataType.Time, (Object o) -> {
            --i[0];
            assertEquals(expected[i[0]], (String) o);
        });

        assertEquals(0, cut.stateDataSize("state1"));
        assertEquals(3, cut.timeDataSize("tL"));
        assertEquals(0, cut.timeDataSize("tN"));
    }

    @Test
    public void testAddOutputPortData()
    {
        MutableTrackerDataStore cut = makeInstance();

        assertTrue(cut.addOutputPortData("outport2", "some value"));
        assertTrue(cut.addOutputPortData("outport1", new Object() {
            @Override
            public String toString()
            {
                return "some other object";
            }
        }));
        assertTrue(
            cut.addOutputPortData("outport3", Integer.valueOf(42))
        );

        assertEquals(1, cut.outputPortDataSize("outport2"));
        assertEquals(
            "some value",
            cut.getOutputPortData("outport2", 0)
        );

        assertEquals(1, cut.outputPortDataSize("outport1"));
        assertEquals(
            "some other object",
            cut.getOutputPortData("outport1", 0).toString()
        );

        assertEquals(1, cut.outputPortDataSize("outport3"));
        assertEquals(
            (Integer) 42,
            cut.getOutputPortData("outport3", 0)
        );

        assertFalse(cut.addOutputPortData("asdf", "a fourth value"));
        try
        {
            cut.getOutputPortData("asdf", 0);
            fail("Exception expected!");
        }
        catch (NullPointerException e)
        {
            // Success
        }
    }

    @Test
    public void testAddStateData()
    {
        MutableTrackerDataStore cut = makeInstance();

        assertTrue(cut.addStateData("state1", "some value"));
        assertTrue(cut.addStateData("state2", new Object() {
            @Override
            public String toString()
            {
                return "some other object";
            }
        }));

        assertEquals(1, cut.stateDataSize("state1"));
        assertEquals("some value", cut.getStateData("state1", 0));

        assertEquals(1, cut.stateDataSize("state2"));
        assertEquals(
            "some other object",
            cut.getStateData("state2", 0).toString()
        );

        assertFalse(cut.addStateData("asdf", "a third value"));
        try
        {
            cut.getStateData("asdf", 0);
            fail("Exception expected!");
        }
        catch (NullPointerException e)
        {
            // Success
        }
    }

    @Test
    public void testAddTimeData()
    {
        MutableTrackerDataStore cut = makeInstance();

        assertTrue(cut.addTimeData("tL", "some value"));
        assertTrue(cut.addTimeData("tN", new Object() {
            @Override
            public String toString()
            {
                return "some other object";
            }
        }));

        assertEquals(1, cut.timeDataSize("tL"));
        assertEquals("some value", cut.getTimeData("tL", 0));

        assertEquals(1, cut.timeDataSize("tN"));
        assertEquals(
            "some other object",
            cut.getTimeData("tN", 0).toString()
        );

        assertFalse(cut.addTimeData("asdf", "a third value"));
        try
        {
            cut.getTimeData("asdf", 0);
            fail("Exception expected!");
        }
        catch (NullPointerException e)
        {
            // Success
        }
    }
    
    private void doInputPortDataViews(MutableTrackerDataStore cut, MutableTrackerConfiguration[] dataViews)
    {
        assertFalse(
            cut.isInputPortTracked("inport1", TrackingType.STACK)
        );
        assertFalse(
            cut.isInputPortTracked("inport1", TrackingType.NO_STACK)
        );
        assertFalse(
            cut.isInputPortTracked("inport1", TrackingType.LOG)
        );
        assertFalse(
            cut.isInputPortTracked("inport1", TrackingType.DB)
        );
        assertFalse(
            cut.isInputPortTracked("inport2", TrackingType.STACK)
        );
        assertFalse(
            cut.isInputPortTracked("inport2", TrackingType.NO_STACK)
        );
        assertFalse(
            cut.isInputPortTracked("inport2", TrackingType.LOG)
        );
        assertFalse(
            cut.isInputPortTracked("inport2", TrackingType.DB)
        );
        assertFalse(cut.isDatabaseTrackingEnabled());
        assertEquals(2, dataViews.length);

        dataViews[0].setTracked(TrackingType.NO_STACK);
        assertFalse(
            cut.isInputPortTracked("inport1", TrackingType.STACK)
        );
        assertTrue(
            cut.isInputPortTracked("inport1", TrackingType.NO_STACK)
        );
        assertFalse(
            cut.isInputPortTracked("inport1", TrackingType.LOG)
        );
        assertFalse(
            cut.isInputPortTracked("inport1", TrackingType.DB)
        );
        assertFalse(cut.isDatabaseTrackingEnabled());

        dataViews[1].setTracked(TrackingType.LOG);
        assertFalse(
            cut.isInputPortTracked("inport2", TrackingType.STACK)
        );
        assertFalse(
            cut.isInputPortTracked("inport2", TrackingType.NO_STACK)
        );
        assertTrue(
            cut.isInputPortTracked("inport2", TrackingType.LOG)
        );
        assertFalse(
            cut.isInputPortTracked("inport2", TrackingType.DB)
        );

        dataViews[0].setUntracked(TrackingType.NO_STACK);
        dataViews[0].setTracked(TrackingType.DB);
        dataViews[0].setTracked(TrackingType.STACK);

        assertTrue(
            cut.isInputPortTracked("inport1", TrackingType.STACK)
        );
        assertFalse(
            cut.isInputPortTracked("inport1", TrackingType.NO_STACK)
        );
        assertFalse(
            cut.isInputPortTracked("inport1", TrackingType.LOG)
        );
        assertTrue(
            cut.isInputPortTracked("inport1", TrackingType.DB)
        );
        assertTrue(cut.isDatabaseTrackingEnabled());

        assertEquals("", dataViews[0].getUnits());
        assertEquals("", dataViews[1].getUnits());

        dataViews[0].setUnits("oz");

        assertEquals("oz", dataViews[0].getUnits());
        assertEquals("", dataViews[1].getUnits());
    }

    @Test
    public void testMakeInputPortDataViews()
    {
        MutableTrackerDataStore cut = makeInstance();

        MutableTrackerConfiguration[] dataViews = cut.getTrackingConfigurationForInputPorts();

        doInputPortDataViews(cut, dataViews);
        
        cut = makeInstance();
        dataViews = cut.getTrackingConfigurationFor(TrackerDataStore.DataType.InputPorts).get();
        doInputPortDataViews(cut, dataViews);
    }

    private void doOutputPortDataViewTest(
        MutableTrackerDataStore cut,
        MutableTrackerConfiguration[] dataViews
    )
    {
        assertFalse(
            cut.isOutputPortTracked("outport1", TrackingType.STACK)
        );
        assertFalse(
            cut.isOutputPortTracked("outport1", TrackingType.NO_STACK)
        );
        assertFalse(
            cut.isOutputPortTracked("outport1", TrackingType.LOG)
        );
        assertFalse(
            cut.isOutputPortTracked("outport1", TrackingType.DB)
        );
        assertFalse(
            cut.isOutputPortTracked("outport2", TrackingType.STACK)
        );
        assertFalse(
            cut.isOutputPortTracked("outport2", TrackingType.NO_STACK)
        );
        assertFalse(
            cut.isOutputPortTracked("outport2", TrackingType.LOG)
        );
        assertFalse(
            cut.isOutputPortTracked("outport2", TrackingType.DB)
        );
        assertFalse(
            cut.isDatabaseTrackingEnabled()
        );
        assertEquals(3, dataViews.length);

        dataViews[0].setTracked(TrackingType.NO_STACK);
        assertFalse(
            cut.isOutputPortTracked("outport1", TrackingType.STACK)
        );
        assertTrue(
            cut.isOutputPortTracked("outport1", TrackingType.NO_STACK)
        );
        assertFalse(
            cut.isOutputPortTracked("outport1", TrackingType.LOG)
        );
        assertFalse(
            cut.isOutputPortTracked("outport1", TrackingType.DB)
        );

        dataViews[1].setTracked(TrackingType.LOG);
        assertFalse(
            cut.isOutputPortTracked("outport2", TrackingType.STACK)
        );
        assertFalse(
            cut.isOutputPortTracked("outport2", TrackingType.NO_STACK)
        );
        assertTrue(
            cut.isOutputPortTracked("outport2", TrackingType.LOG)
        );
        assertFalse(
            cut.isOutputPortTracked("outport2", TrackingType.DB)
        );
        assertFalse(cut.isDatabaseTrackingEnabled());

        dataViews[0].setUntracked(TrackingType.NO_STACK);
        dataViews[0].setTracked(TrackingType.DB);
        dataViews[0].setTracked(TrackingType.STACK);

        assertTrue(
            cut.isOutputPortTracked("outport1", TrackingType.STACK)
        );
        assertFalse(
            cut.isOutputPortTracked("outport1", TrackingType.NO_STACK)
        );
        assertFalse(
            cut.isOutputPortTracked("outport1", TrackingType.LOG)
        );
        assertTrue(
            cut.isOutputPortTracked("outport1", TrackingType.DB)
        );
        assertTrue(cut.isDatabaseTrackingEnabled());

        assertEquals("", dataViews[0].getUnits());
        assertEquals("", dataViews[1].getUnits());

        dataViews[0].setUnits("oz");

        assertEquals("oz", dataViews[0].getUnits());
        assertEquals("", dataViews[1].getUnits());
    }

    @Test
    public void testMakeOutputPortDataViews()
    {
        MutableTrackerDataStore cut = makeInstance();

        MutableTrackerConfiguration[] dataViews = cut.getTrackingConfigurationForOutputPorts();

        doOutputPortDataViewTest(cut, dataViews);
        
        cut = makeInstance();
        doOutputPortDataViewTest(
            cut,
            cut.getTrackingConfigurationFor(
                TrackerDataStore.DataType.OutputPorts
            ).get()
        );
    }

    private void doStateDataViewTest(MutableTrackerDataStore cut, MutableTrackerConfiguration[] dataViews)
    {
        assertFalse(cut.isStateTracked("state1", TrackingType.STACK));
        assertFalse(
            cut.isStateTracked("state1", TrackingType.NO_STACK)
        );
        assertFalse(cut.isStateTracked("state1", TrackingType.LOG));
        assertFalse(cut.isStateTracked("state1", TrackingType.DB));
        assertFalse(cut.isStateTracked("state2", TrackingType.STACK));
        assertFalse(
            cut.isStateTracked("state2", TrackingType.NO_STACK)
        );
        assertFalse(cut.isStateTracked("state2", TrackingType.LOG));
        assertFalse(cut.isStateTracked("state2", TrackingType.DB));
        assertFalse(cut.isDatabaseTrackingEnabled());
        assertEquals(2, dataViews.length);

        dataViews[0].setTracked(TrackingType.NO_STACK);
        assertFalse(cut.isStateTracked("state1", TrackingType.STACK));
        assertTrue(
            cut.isStateTracked("state1", TrackingType.NO_STACK)
        );
        assertFalse(cut.isStateTracked("state1", TrackingType.LOG));
        assertFalse(cut.isStateTracked("state1", TrackingType.DB));

        dataViews[1].setTracked(TrackingType.LOG);
        assertFalse(cut.isStateTracked("state2", TrackingType.STACK));
        assertFalse(
            cut.isStateTracked("state2", TrackingType.NO_STACK)
        );
        assertTrue(cut.isStateTracked("state2", TrackingType.LOG));
        assertFalse(cut.isStateTracked("state2", TrackingType.DB));
        assertFalse(cut.isDatabaseTrackingEnabled());

        dataViews[0].setUntracked(TrackingType.NO_STACK);
        dataViews[0].setTracked(TrackingType.DB);
        dataViews[0].setTracked(TrackingType.STACK);

        assertTrue(cut.isStateTracked("state1", TrackingType.STACK));
        assertFalse(
            cut.isStateTracked("state1", TrackingType.NO_STACK)
        );
        assertFalse(cut.isStateTracked("state1", TrackingType.LOG));
        assertTrue(cut.isStateTracked("state1", TrackingType.DB));
        assertTrue(cut.isDatabaseTrackingEnabled());

        assertEquals("", dataViews[0].getUnits());
        assertEquals("", dataViews[1].getUnits());

        dataViews[0].setUnits("oz");

        assertEquals("oz", dataViews[0].getUnits());
        assertEquals("", dataViews[1].getUnits());
    }
    @Test
    public void testMakeStateDataViews()
    {
        MutableTrackerDataStore cut = makeInstance();
        MutableTrackerConfiguration[] dataViews = cut.getTrackingConfigurationForStates();

        doStateDataViewTest(cut, dataViews);
        
        cut = makeInstance();
        dataViews = cut.getTrackingConfigurationFor(TrackerDataStore.DataType.States).get();
        doStateDataViewTest(cut, dataViews);
    }
    
    private void doTimeDataViewTest(MutableTrackerDataStore cut, MutableTrackerConfiguration[] dataViews)
    {
        assertFalse(cut.isTimeTracked("tL", TrackingType.STACK));
        assertFalse(cut.isTimeTracked("tL", TrackingType.NO_STACK));
        assertFalse(cut.isTimeTracked("tL", TrackingType.LOG));
        assertFalse(cut.isTimeTracked("tL", TrackingType.DB));
        assertFalse(cut.isTimeTracked("tN", TrackingType.STACK));
        assertFalse(cut.isTimeTracked("tN", TrackingType.NO_STACK));
        assertFalse(cut.isTimeTracked("tN", TrackingType.LOG));
        assertFalse(cut.isTimeTracked("tN", TrackingType.DB));
        assertFalse(cut.isDatabaseTrackingEnabled());
        assertEquals(2, dataViews.length);

        dataViews[0].setTracked(TrackingType.NO_STACK);
        assertFalse(cut.isTimeTracked("tL", TrackingType.STACK));
        assertTrue(cut.isTimeTracked("tL", TrackingType.NO_STACK));
        assertFalse(cut.isTimeTracked("tL", TrackingType.LOG));
        assertFalse(cut.isTimeTracked("tL", TrackingType.DB));
        assertFalse(cut.isDatabaseTrackingEnabled());
        
        dataViews[1].setTracked(TrackingType.LOG);
        assertFalse(cut.isTimeTracked("tN", TrackingType.STACK));
        assertFalse(cut.isTimeTracked("tN", TrackingType.NO_STACK));
        assertTrue(cut.isTimeTracked("tN", TrackingType.LOG));
        assertFalse(cut.isTimeTracked("tN", TrackingType.DB));
        assertFalse(cut.isDatabaseTrackingEnabled());

        dataViews[0].setUntracked(TrackingType.NO_STACK);
        dataViews[0].setTracked(TrackingType.DB);
        dataViews[0].setTracked(TrackingType.STACK);

        assertTrue(cut.isTimeTracked("tL", TrackingType.STACK));
        assertFalse(cut.isTimeTracked("tL", TrackingType.NO_STACK));
        assertFalse(cut.isTimeTracked("tL", TrackingType.LOG));
        assertTrue(cut.isTimeTracked("tL", TrackingType.DB));
        assertTrue(cut.isDatabaseTrackingEnabled());

        assertEquals("", dataViews[0].getUnits());
        assertEquals("", dataViews[1].getUnits());

        dataViews[0].setUnits("oz");

        assertEquals("oz", dataViews[0].getUnits());
        assertEquals("", dataViews[1].getUnits());
    }

    @Test
    public void testMakeTimeDataViews()
    {
        MutableTrackerDataStore cut = makeInstance();
        MutableTrackerConfiguration[] dataViews = cut.getTrackingConfigurationForTime();
        doTimeDataViewTest(cut, dataViews);
        
        cut = makeInstance();
        dataViews = cut.getTrackingConfigurationFor(TrackerDataStore.DataType.Time).get();
        doTimeDataViewTest(cut, dataViews);
    }

    private void testIsTracked(
        TrackerDataStore cut,
        String[] ports,
        Function<String, Boolean> isTrackedFunction,
        BiFunction<String, TrackingType, Boolean> isTrackedTypeFunction,
        BiConsumer<String, TrackingType> setTrackedFunction,
        BiConsumer<String, TrackingType> setUntrackedFunction
    )
    {
        assertFalse(cut.isAnythingTracked());
        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.STACK)
        );
        assertFalse(
            isTrackedTypeFunction.apply(
                ports[0],
                TrackingType.NO_STACK
            )
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.LOG)
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.DB)
        );

        assertFalse(isTrackedFunction.apply(ports[0]));

        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.STACK)
        );
        assertFalse(
            isTrackedTypeFunction.apply(
                ports[1],
                TrackingType.NO_STACK
            )
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.LOG)
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.DB)
        );

        assertFalse(isTrackedFunction.apply(ports[1]));

        if (ports.length > 2)
        {
            assertFalse(
                isTrackedTypeFunction.apply(
                    ports[2],
                    TrackingType.STACK
                )
            );
            assertFalse(
                isTrackedTypeFunction.apply(
                    ports[2],
                    TrackingType.NO_STACK
                )
            );
            assertFalse(
                isTrackedTypeFunction.apply(ports[2], TrackingType.LOG)
            );
            assertFalse(
                isTrackedTypeFunction.apply(ports[2], TrackingType.DB)
            );

            assertFalse(isTrackedFunction.apply(ports[2]));
        }
        assertFalse(cut.isTrackingLogEnabled());
        assertFalse(cut.isTimeViewEnabled());

        setTrackedFunction.accept(ports[0], TrackingType.LOG);
        assertTrue(cut.isAnythingTracked());
        assertTrue(cut.isTrackingLogEnabled());
        assertTrue(isTrackedFunction.apply(ports[0]));
        assertTrue(
            isTrackedTypeFunction.apply(ports[0], TrackingType.LOG)
        );

        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.STACK)
        );
        assertFalse(
            isTrackedTypeFunction.apply(
                ports[0],
                TrackingType.NO_STACK
            )
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.DB)
        );

        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.STACK)
        );
        assertFalse(
            isTrackedTypeFunction.apply(
                ports[1],
                TrackingType.NO_STACK
            )
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.LOG)
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.DB)
        );

        assertFalse(isTrackedFunction.apply(ports[1]));

        setUntrackedFunction.accept(ports[0], TrackingType.LOG);
        assertFalse(cut.isTrackingLogEnabled());
        assertFalse(isTrackedFunction.apply(ports[0]));
        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.LOG)
        );

        setTrackedFunction.accept(ports[1], TrackingType.NO_STACK);
        assertTrue(cut.isTimeViewEnabled());
        assertTrue(isTrackedFunction.apply(ports[1]));
        assertTrue(
            isTrackedTypeFunction.apply(
                ports[1],
                TrackingType.NO_STACK
            )
        );

        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.STACK)
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.LOG)
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.DB)
        );

        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.STACK)
        );
        assertFalse(
            isTrackedTypeFunction.apply(
                ports[0],
                TrackingType.NO_STACK
            )
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.LOG)
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.DB)
        );

        assertFalse(isTrackedFunction.apply(ports[0]));

        setUntrackedFunction.accept(ports[1], TrackingType.NO_STACK);
        assertFalse(cut.isAnythingTracked());
        assertFalse(cut.isTimeViewEnabled());
        assertFalse(isTrackedFunction.apply(ports[1]));
        assertFalse(
            isTrackedTypeFunction.apply(
                ports[1],
                TrackingType.NO_STACK
            )
        );

        setTrackedFunction.accept(ports[0], TrackingType.DB);
        assertTrue(cut.isAnythingTracked());
        assertTrue(isTrackedFunction.apply(ports[0]));
        assertTrue(
            isTrackedTypeFunction.apply(ports[0], TrackingType.DB)
        );

        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.STACK)
        );
        assertFalse(
            isTrackedTypeFunction.apply(
                ports[0],
                TrackingType.NO_STACK
            )
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.LOG)
        );
        assertFalse(cut.isTimeViewEnabled());
        assertFalse(cut.isTrackingLogEnabled());

        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.STACK)
        );
        assertFalse(
            isTrackedTypeFunction.apply(
                ports[1],
                TrackingType.NO_STACK
            )
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.LOG)
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.DB)
        );

        setUntrackedFunction.accept(ports[1], TrackingType.DB);
        assertTrue(cut.isAnythingTracked());
        assertTrue(isTrackedFunction.apply(ports[0]));
        assertTrue(
            isTrackedTypeFunction.apply(ports[0], TrackingType.DB)
        );

        setTrackedFunction.accept(ports[0], TrackingType.STACK);
        assertTrue(cut.isAnythingTracked());
        setUntrackedFunction.accept(ports[0], TrackingType.DB);
        assertTrue(isTrackedFunction.apply(ports[0]));
        assertTrue(
            isTrackedTypeFunction.apply(ports[0], TrackingType.STACK)
        );

        assertFalse(
            isTrackedTypeFunction.apply(
                ports[0],
                TrackingType.NO_STACK
            )
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.LOG)
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[0], TrackingType.DB)
        );

        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.STACK)
        );
        assertFalse(
            isTrackedTypeFunction.apply(
                ports[1],
                TrackingType.NO_STACK
            )
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.LOG)
        );
        assertFalse(
            isTrackedTypeFunction.apply(ports[1], TrackingType.DB)
        );

        if (ports.length > 2)
        {
            assertFalse(
                isTrackedTypeFunction.apply(
                    ports[2],
                    TrackingType.STACK
                )
            );
            assertFalse(
                isTrackedTypeFunction.apply(
                    ports[2],
                    TrackingType.NO_STACK
                )
            );
            assertFalse(
                isTrackedTypeFunction.apply(ports[2], TrackingType.LOG)
            );
            assertFalse(
                isTrackedTypeFunction.apply(ports[2], TrackingType.DB)
            );

            assertFalse(isTrackedFunction.apply(ports[2]));

            setTrackedFunction.accept(
                ports[2],
                TrackingType.NO_STACK
            );
            assertTrue(cut.isAnythingTracked());
            assertTrue(isTrackedFunction.apply(ports[2]));
            assertTrue(
                isTrackedTypeFunction.apply(
                    ports[2],
                    TrackingType.NO_STACK
                )
            );

            assertTrue(isTrackedFunction.apply(ports[0]));
            assertTrue(
                isTrackedTypeFunction.apply(
                    ports[0],
                    TrackingType.STACK
                )
            );
            assertFalse(
                isTrackedTypeFunction.apply(
                    ports[0],
                    TrackingType.NO_STACK
                )
            );
            assertFalse(
                isTrackedTypeFunction.apply(ports[0], TrackingType.LOG)
            );
            assertFalse(
                isTrackedTypeFunction.apply(ports[0], TrackingType.DB)
            );

            assertFalse(isTrackedFunction.apply(ports[1]));
            assertFalse(
                isTrackedTypeFunction.apply(
                    ports[1],
                    TrackingType.STACK
                )
            );
            assertFalse(
                isTrackedTypeFunction.apply(
                    ports[1],
                    TrackingType.NO_STACK
                )
            );
            assertFalse(
                isTrackedTypeFunction.apply(ports[1], TrackingType.LOG)
            );
            assertFalse(
                isTrackedTypeFunction.apply(ports[1], TrackingType.DB)
            );

            assertFalse(
                isTrackedTypeFunction.apply(
                    ports[2],
                    TrackingType.STACK
                )
            );
            assertFalse(
                isTrackedTypeFunction.apply(ports[2], TrackingType.LOG)
            );
            assertFalse(
                isTrackedTypeFunction.apply(ports[2], TrackingType.DB)
            );
        }
    }

    @Test
    public void testIsInputPortTracked()
    {
        MutableTrackerDataStore cut = makeInstance();

        testIsTracked(
            cut,
            new String[]
            {
                "inport1",
                "inport2"
            },
            (String portName) -> {
                return cut.isInputPortTracked(portName);
            },
            (String portName, TrackingType type) -> {
                return cut.isInputPortTracked(portName, type);
            },
            (String portName, TrackingType type) -> {
                cut.setInputPortTracked(portName, type);
            },
            (String portName, TrackingType type) -> {
                cut.setInputPortUntracked(portName, type);
            }
        );
    }

    @Test
    public void testIsOutputPortTracked()
    {
        MutableTrackerDataStore cut = makeInstance();

        testIsTracked(
            cut,
            new String[]
            {
                "outport1",
                "outport2",
                "outport3"
            },
            (String portName) -> {
                return cut.isOutputPortTracked(portName);
            },
            (String portName, TrackingType type) -> {
                return cut.isOutputPortTracked(portName, type);
            },
            (String portName, TrackingType type) -> {
                cut.setOutputPortTracked(portName, type);
            },
            (String portName, TrackingType type) -> {
                cut.setOutputPortUntracked(portName, type);
            }
        );
    }

    @Test
    public void testIsStateTracked()
    {
        MutableTrackerDataStore cut = makeInstance();

        testIsTracked(
            cut,
            new String[]
            {
                "state1",
                "state2"
            },
            (String portName) -> {
                return cut.isStateTracked(portName);
            },
            (String portName, TrackingType type) -> {
                return cut.isStateTracked(portName, type);
            },
            (String portName, TrackingType type) -> {
                cut.setStateTracked(portName, type);
            },
            (String portName, TrackingType type) -> {
                cut.setStateUntracked(portName, type);
            }
        );
    }

    @Test
    public void testIsTimeTracked()
    {
        MutableTrackerDataStore cut = makeInstance();

        testIsTracked(
            cut,
            new String[]
            {
                "tL",
                "tN"
            },
            (String portName) -> {
                return cut.isTimeTracked(portName);
            },
            (String portName, TrackingType type) -> {
                return cut.isTimeTracked(portName, type);
            },
            (String portName, TrackingType type) -> {
                cut.setTimeTracked(portName, type);
            },
            (String portName, TrackingType type) -> {
                cut.setTimeUntracked(portName, type);
            }
        );
    }
    
    @Test
    public void testSetSortOrder()
    {
        MutableTrackerDataStore cut = makeInstance();
        
        assertEquals(TrackerDataStore.SortOrder.Unsorted, cut.getTrackingLogSortOrder());
        assertEquals(TrackerDataStore.SortOrder.Unsorted, cut.getTimeViewSortOrder());

        cut.setTrackingLogSortOrder(TrackerDataStore.SortOrder.Reversed);

        assertEquals(TrackerDataStore.SortOrder.Reversed, cut.getTrackingLogSortOrder());
        assertEquals(TrackerDataStore.SortOrder.Unsorted, cut.getTimeViewSortOrder());
        
        cut.setTrackingLogSortOrder(TrackerDataStore.SortOrder.Alphabetical);

        assertEquals(TrackerDataStore.SortOrder.Alphabetical, cut.getTrackingLogSortOrder());
        assertEquals(TrackerDataStore.SortOrder.Unsorted, cut.getTimeViewSortOrder());

        cut.setTimeViewSortOrder(TrackerDataStore.SortOrder.Reversed);

        assertEquals(TrackerDataStore.SortOrder.Alphabetical, cut.getTrackingLogSortOrder());
        assertEquals(TrackerDataStore.SortOrder.Reversed, cut.getTimeViewSortOrder());
    }
}
