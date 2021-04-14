package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import util.tracking.MutableTrackerConfiguration;
import util.tracking.MutableTrackerConfigurationImpl;
import util.tracking.MutableTrackerDataStore;
import util.tracking.TrackerDataStore;
import util.tracking.TrackingType;

public class TestMutableTrackerConfiguration
{
    private MutableTrackerDataStore mockDataStore;
    private MutableTrackerConfiguration makeInstance(TrackerDataStore.DataType dType, String name)
    {
        mockDataStore = mock(MutableTrackerDataStore.class);
        
        return new MutableTrackerConfigurationImpl(mockDataStore, dType, name);
    }
    
    @Test
    public void testSetInputPortTracked()
    {
        MutableTrackerConfiguration cut = makeInstance(TrackerDataStore.DataType.InputPorts, "inportA");
        
        when(mockDataStore.setInputPortTracked("inportA", TrackingType.STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isInputPortTracked("inportA", TrackingType.STACK)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setInputPortTracked("inportA", TrackingType.NO_STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isInputPortTracked("inportA", TrackingType.NO_STACK)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setInputPortTracked("inportA", TrackingType.LOG)).then((InvocationOnMock i) -> {
            when(mockDataStore.isInputPortTracked("inportA", TrackingType.LOG)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setInputPortTracked("inportA", TrackingType.DB)).then((InvocationOnMock i) -> {
            when(mockDataStore.isInputPortTracked("inportA", TrackingType.DB)).thenReturn(true);
            return mockDataStore;
        });
        
        when(mockDataStore.setInputPortUntracked("inportA", TrackingType.STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isInputPortTracked("inportA", TrackingType.STACK)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setInputPortUntracked("inportA", TrackingType.NO_STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isInputPortTracked("inportA", TrackingType.NO_STACK)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setInputPortUntracked("inportA", TrackingType.LOG)).then((InvocationOnMock i) -> {
            when(mockDataStore.isInputPortTracked("inportA", TrackingType.LOG)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setInputPortUntracked("inportA", TrackingType.DB)).then((InvocationOnMock i) -> {
            when(mockDataStore.isInputPortTracked("inportA", TrackingType.DB)).thenReturn(false);
            return mockDataStore;
        });
        
        when(mockDataStore.setInputPortUnit("inportA", "oz")).then((InvocationOnMock i) -> {
            when(mockDataStore.getInputPortUnits("inportA")).thenReturn("oz");
            return mockDataStore; 
        });
        
        when(mockDataStore.isInputPortTracked("inportA", TrackingType.STACK)).thenReturn(false);
        when(mockDataStore.isInputPortTracked("inportA", TrackingType.NO_STACK)).thenReturn(false);
        when(mockDataStore.isInputPortTracked("inportA", TrackingType.LOG)).thenReturn(false);
        when(mockDataStore.isInputPortTracked("inportA", TrackingType.DB)).thenReturn(false);
        when(mockDataStore.getInputPortUnits("inportA")).thenReturn("");

        assertFalse(cut.isTracked(TrackingType.STACK));
        
        assertEquals(cut, cut.setTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.LOG));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.DB));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertTrue(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setUnits("oz"));
        assertEquals("oz", cut.getUnits());
    }
    
    @Test
    public void testSetOutputPortTracked()
    {
        MutableTrackerConfiguration cut = makeInstance(TrackerDataStore.DataType.OutputPorts, "outPortB");
        
        when(mockDataStore.setOutputPortTracked("outPortB", TrackingType.STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.STACK)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setOutputPortTracked("outPortB", TrackingType.NO_STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.NO_STACK)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setOutputPortTracked("outPortB", TrackingType.LOG)).then((InvocationOnMock i) -> {
            when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.LOG)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setOutputPortTracked("outPortB", TrackingType.DB)).then((InvocationOnMock i) -> {
            when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.DB)).thenReturn(true);
            return mockDataStore;
        });
        
        when(mockDataStore.setOutputPortUntracked("outPortB", TrackingType.STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.STACK)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setOutputPortUntracked("outPortB", TrackingType.NO_STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.NO_STACK)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setOutputPortUntracked("outPortB", TrackingType.LOG)).then((InvocationOnMock i) -> {
            when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.LOG)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setOutputPortUntracked("outPortB", TrackingType.DB)).then((InvocationOnMock i) -> {
            when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.DB)).thenReturn(false);
            return mockDataStore;
        });
        
        when(mockDataStore.setOutputPortUnit("outPortB", "oz")).then((InvocationOnMock i) -> {
            when(mockDataStore.getOutputPortUnits("outPortB")).thenReturn("oz");
            return mockDataStore; 
        });
        
        when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.STACK)).thenReturn(false);
        when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.NO_STACK)).thenReturn(false);
        when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.LOG)).thenReturn(false);
        when(mockDataStore.isOutputPortTracked("outPortB", TrackingType.DB)).thenReturn(false);
        when(mockDataStore.getOutputPortUnits("outPortB")).thenReturn("");

        assertFalse(cut.isTracked(TrackingType.STACK));
        
        assertEquals(cut, cut.setTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.LOG));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.DB));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertTrue(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setUnits("oz"));
        assertEquals("oz", cut.getUnits());
    }
    
    @Test
    public void testSetStateTracked()
    {
        MutableTrackerConfiguration cut = makeInstance(TrackerDataStore.DataType.States, "AState");
        
        when(mockDataStore.setStateTracked("AState", TrackingType.STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isStateTracked("AState", TrackingType.STACK)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setStateTracked("AState", TrackingType.NO_STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isStateTracked("AState", TrackingType.NO_STACK)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setStateTracked("AState", TrackingType.LOG)).then((InvocationOnMock i) -> {
            when(mockDataStore.isStateTracked("AState", TrackingType.LOG)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setStateTracked("AState", TrackingType.DB)).then((InvocationOnMock i) -> {
            when(mockDataStore.isStateTracked("AState", TrackingType.DB)).thenReturn(true);
            return mockDataStore;
        });
        
        when(mockDataStore.setStateUntracked("AState", TrackingType.STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isStateTracked("AState", TrackingType.STACK)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setStateUntracked("AState", TrackingType.NO_STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isStateTracked("AState", TrackingType.NO_STACK)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setStateUntracked("AState", TrackingType.LOG)).then((InvocationOnMock i) -> {
            when(mockDataStore.isStateTracked("AState", TrackingType.LOG)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setStateUntracked("AState", TrackingType.DB)).then((InvocationOnMock i) -> {
            when(mockDataStore.isStateTracked("AState", TrackingType.DB)).thenReturn(false);
            return mockDataStore;
        });
        
        when(mockDataStore.setStateUnit("AState", "oz")).then((InvocationOnMock i) -> {
            when(mockDataStore.getStateUnits("AState")).thenReturn("oz");
            return mockDataStore; 
        });
        
        when(mockDataStore.isStateTracked("AState", TrackingType.STACK)).thenReturn(false);
        when(mockDataStore.isStateTracked("AState", TrackingType.NO_STACK)).thenReturn(false);
        when(mockDataStore.isStateTracked("AState", TrackingType.LOG)).thenReturn(false);
        when(mockDataStore.isStateTracked("AState", TrackingType.DB)).thenReturn(false);
        when(mockDataStore.getStateUnits("AState")).thenReturn("");

        assertFalse(cut.isTracked(TrackingType.STACK));
        
        assertEquals(cut, cut.setTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.LOG));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.DB));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertTrue(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setUnits("oz"));
        assertEquals("oz", cut.getUnits());
    }
    
    @Test
    public void testSetTimeTracked()
    {
        MutableTrackerConfiguration cut = makeInstance(TrackerDataStore.DataType.Time, "ATime");
        
        when(mockDataStore.setTimeTracked("ATime", TrackingType.STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isTimeTracked("ATime", TrackingType.STACK)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setTimeTracked("ATime", TrackingType.NO_STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isTimeTracked("ATime", TrackingType.NO_STACK)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setTimeTracked("ATime", TrackingType.LOG)).then((InvocationOnMock i) -> {
            when(mockDataStore.isTimeTracked("ATime", TrackingType.LOG)).thenReturn(true);
            return mockDataStore;
        });
        when(mockDataStore.setTimeTracked("ATime", TrackingType.DB)).then((InvocationOnMock i) -> {
            when(mockDataStore.isTimeTracked("ATime", TrackingType.DB)).thenReturn(true);
            return mockDataStore;
        });
        
        when(mockDataStore.setTimeUntracked("ATime", TrackingType.STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isTimeTracked("ATime", TrackingType.STACK)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setTimeUntracked("ATime", TrackingType.NO_STACK)).then((InvocationOnMock i) -> {
            when(mockDataStore.isTimeTracked("ATime", TrackingType.NO_STACK)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setTimeUntracked("ATime", TrackingType.LOG)).then((InvocationOnMock i) -> {
            when(mockDataStore.isTimeTracked("ATime", TrackingType.LOG)).thenReturn(false);
            return mockDataStore;
        });
        when(mockDataStore.setTimeUntracked("ATime", TrackingType.DB)).then((InvocationOnMock i) -> {
            when(mockDataStore.isTimeTracked("ATime", TrackingType.DB)).thenReturn(false);
            return mockDataStore;
        });
        
        when(mockDataStore.setTimeUnit("ATime", "oz")).then((InvocationOnMock i) -> {
            when(mockDataStore.getTimeUnits("ATime")).thenReturn("oz");
            return mockDataStore; 
        });
        
        when(mockDataStore.isTimeTracked("ATime", TrackingType.STACK)).thenReturn(false);
        when(mockDataStore.isTimeTracked("ATime", TrackingType.NO_STACK)).thenReturn(false);
        when(mockDataStore.isTimeTracked("ATime", TrackingType.LOG)).thenReturn(false);
        when(mockDataStore.isTimeTracked("ATime", TrackingType.DB)).thenReturn(false);
        when(mockDataStore.getTimeUnits("ATime")).thenReturn("");

        assertFalse(cut.isTracked(TrackingType.STACK));
        
        assertEquals(cut, cut.setTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertFalse(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertFalse(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.LOG));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertFalse(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setTracked(TrackingType.DB));
        assertTrue(cut.isTracked(TrackingType.STACK));
        assertTrue(cut.isTracked(TrackingType.NO_STACK));
        assertTrue(cut.isTracked(TrackingType.LOG));
        assertTrue(cut.isTracked(TrackingType.DB));
        
        assertEquals(cut, cut.setUnits("oz"));
        assertEquals("oz", cut.getUnits());
    }
}
