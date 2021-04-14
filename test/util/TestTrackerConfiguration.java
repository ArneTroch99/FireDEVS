package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import util.tracking.TrackerConfiguration;
import util.tracking.TrackerConfigurationImpl;
import util.tracking.TrackerDataStore;
import util.tracking.TrackingType;

public class TestTrackerConfiguration
{
    private TrackerDataStore mockDataStore;

    private TrackerConfiguration makeInstance(TrackerDataStore.DataType dType, String name)
    {
        mockDataStore = mock(TrackerDataStore.class);
        return new TrackerConfigurationImpl(mockDataStore, dType, name);
    }

    @Test
    public void testInputPorts()
    {
        TrackerConfiguration cut = makeInstance(TrackerDataStore.DataType.InputPorts, "inportA");

        when(mockDataStore.isInputPortTracked("inportA", TrackingType.STACK)).thenReturn(false);
        when(mockDataStore.isInputPortTracked("inportA", TrackingType.NO_STACK)).thenReturn(false);
        when(mockDataStore.isInputPortTracked("inportA", TrackingType.LOG)).thenReturn(false);
        when(mockDataStore.isInputPortTracked("inportA", TrackingType.DB)).thenReturn(false);
        when(mockDataStore.getInputPortUnits("inportA")).thenReturn("oz");

        assertFalse(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        assertFalse(cut.isTracked());
        assertFalse(cut.isTimeViewTracked());
        assertEquals("oz", cut.getUnits());

        when(mockDataStore.isInputPortTracked("inportA", TrackingType.STACK)).thenReturn(true);
        when(mockDataStore.isInputPortTracked("inportA", TrackingType.LOG)).thenReturn(true);

        assertTrue(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        assertTrue(cut.isTracked());
        assertTrue(cut.isTimeViewTracked());

        assertEquals("oz", cut.getUnits());
        assertEquals("inportA", cut.getName());
    }

    @Test
    public void testOutputPorts()
    {
        TrackerConfiguration cut = makeInstance(TrackerDataStore.DataType.OutputPorts, "outportA");

        when(mockDataStore.isOutputPortTracked("outportA", TrackingType.STACK)).thenReturn(false);
        when(mockDataStore.isOutputPortTracked("outportA", TrackingType.NO_STACK)).thenReturn(false);
        when(mockDataStore.isOutputPortTracked("outportA", TrackingType.LOG)).thenReturn(false);
        when(mockDataStore.isOutputPortTracked("outportA", TrackingType.DB)).thenReturn(false);
        when(mockDataStore.getOutputPortUnits("outportA")).thenReturn("oz");

        assertFalse(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        assertFalse(cut.isTracked());
        assertFalse(cut.isTimeViewTracked());
        assertEquals("oz", cut.getUnits());

        when(mockDataStore.isOutputPortTracked("outportA", TrackingType.STACK)).thenReturn(true);
        when(mockDataStore.isOutputPortTracked("outportA", TrackingType.LOG)).thenReturn(true);

        assertTrue(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        assertTrue(cut.isTracked());
        assertTrue(cut.isTimeViewTracked());

        assertEquals("oz", cut.getUnits());
        assertEquals("outportA", cut.getName());
    }

    @Test
    public void testStates()
    {
        TrackerConfiguration cut = makeInstance(TrackerDataStore.DataType.States, "stateA");

        when(mockDataStore.isStateTracked("stateA", TrackingType.STACK)).thenReturn(false);
        when(mockDataStore.isStateTracked("stateA", TrackingType.NO_STACK)).thenReturn(false);
        when(mockDataStore.isStateTracked("stateA", TrackingType.DB)).thenReturn(false);
        when(mockDataStore.isStateTracked("stateA", TrackingType.LOG)).thenReturn(false);
        when(mockDataStore.getStateUnits("stateA")).thenReturn("oz");

        assertFalse(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        assertFalse(cut.isTracked());
        assertFalse(cut.isTimeViewTracked());
        assertEquals("oz", cut.getUnits());

        when(mockDataStore.isStateTracked("stateA", TrackingType.STACK)).thenReturn(true);
        when(mockDataStore.isStateTracked("stateA", TrackingType.LOG)).thenReturn(true);

        assertTrue(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        assertTrue(cut.isTracked());
        assertTrue(cut.isTimeViewTracked());

        assertEquals("oz", cut.getUnits());
        assertEquals("stateA", cut.getName());
    }

    @Test
    public void testTime()
    {
        TrackerConfiguration cut = makeInstance(TrackerDataStore.DataType.Time, "tL");
        when(mockDataStore.isTimeTracked("tL", TrackingType.STACK)).thenReturn(false);
        when(mockDataStore.isTimeTracked("tL", TrackingType.NO_STACK)).thenReturn(false);
        when(mockDataStore.isTimeTracked("tL", TrackingType.LOG)).thenReturn(false);
        when(mockDataStore.isTimeTracked("tL", TrackingType.DB)).thenReturn(false);
        when(mockDataStore.getTimeUnits("tL")).thenReturn("oz");

        assertFalse(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTimeViewTracked());
        assertFalse(cut.isTracked(TrackingType.DB));
        assertFalse(cut.isTracked());
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertEquals("oz", cut.getUnits());

        when(mockDataStore.isTimeTracked("tL", TrackingType.STACK)).thenReturn(true);
        when(mockDataStore.isTimeTracked("tL", TrackingType.LOG)).thenReturn(true);

        assertTrue(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        assertTrue(cut.isTracked());
        assertTrue(cut.isTimeViewTracked());

        assertEquals("oz", cut.getUnits());
        assertEquals("tL", cut.getName());
    }
}
