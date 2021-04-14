package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import facade.modeling.FAtomicModel;
import facade.modeling.FAtomicModelTestArtifacts.FakeViewableAtomic;
import facade.simulation.FAtomicSimulator;
import facade.simulation.FSimulator;
import model.modeling.message;
import model.simulation.coupledSimulator;
import util.classUtils.DevsClassFieldFactory;
import util.tracking.MutableTrackerDataStore;
import util.tracking.TrackerDataStore.SortOrder;
import util.tracking.TrackingType;
import view.timeView.Event;


public class TestTracker
{
    @Test
    public void testStateTracking()
    {
        FakeViewableAtomic fakeAtomic = new FakeViewableAtomic();

        coupledSimulator mockSimulator = mock(coupledSimulator.class);
        
        fakeAtomic.setSimulator(mockSimulator);
        fakeAtomic.setStates(DevsClassFieldFactory.createDevsClassFieldMap(FakeViewableAtomic.class, fakeAtomic));

        FAtomicSimulator atomicSim = new FAtomicSimulator(fakeAtomic);
        FAtomicModel m = new FAtomicModel(fakeAtomic);
        m.setSimulator((FSimulator) atomicSim);

        Tracker t = new TrackerImpl(m, 0);
        MutableTrackerDataStore dataStore = t.getDataStorage();
        assertFalse(dataStore.isInputPortTracked("inport1"));
        assertFalse(dataStore.isInputPortTracked("inport2"));
        assertEquals(0, dataStore.inputPortDataSize("inport1"));
        assertEquals(0, dataStore.inputPortDataSize("inport2"));
        assertEquals(Arrays.asList("inport1", "inport2").toString(), dataStore.getInputPortNames().toSortedString());
        assertFalse(dataStore.isOutputPortTracked("outport1"));
        assertFalse(dataStore.isOutputPortTracked("outport2"));
        assertEquals(0, dataStore.outputPortDataSize("outport1"));
        assertEquals(0, dataStore.outputPortDataSize("outport2"));
        assertEquals(Arrays.asList("outport1", "outport2").toString(), dataStore.getOutputPortNames().toSortedString());
        
        assertFalse(dataStore.isStateTracked("Phase"));
        assertFalse(dataStore.isStateTracked("Sigma"));
        assertFalse(dataStore.isStateTracked("instanceVariable"));
        assertFalse(dataStore.isStateTracked("notState"));
        assertFalse(dataStore.isStateTracked("privateInstanceVariable"));
        assertFalse(dataStore.isStateTracked("stringList"));
        assertEquals(0, dataStore.stateDataSize("Phase"));
        assertEquals(0, dataStore.stateDataSize("Sigma"));
        assertEquals(0, dataStore.stateDataSize("instanceVariable"));
        assertEquals(0, dataStore.stateDataSize("notState"));
        assertEquals(0, dataStore.stateDataSize("privateInstanceVariable"));
        assertEquals(0, dataStore.stateDataSize("stringList"));
        assertEquals(0, dataStore.stateDataSize("stringList_defaultChecked"));
        assertEquals(Arrays.asList("Phase", "Sigma", "instanceVariable", "notState", "privateInstanceVariable", "stringList", "stringList_defaultChecked").toString(), dataStore.getStateNames().toSortedString());
        
        assertFalse(dataStore.isTimeTracked("tL"));
        assertFalse(dataStore.isTimeTracked("tN"));
        assertEquals(0, dataStore.timeDataSize("tL"));
        assertEquals(0, dataStore.timeDataSize("tN"));
        assertEquals(Arrays.asList("tL", "tN").toString(), dataStore.getTimeDimensionNames().toSortedString());

        dataStore.setStateTracked("Phase", TrackingType.NO_STACK);
        assertTrue(dataStore.isStateTracked("Phase"));
        assertTrue(dataStore.isStateTracked("Phase", TrackingType.NO_STACK));
        assertFalse(dataStore.isStateTracked("Phase", TrackingType.STACK));
        assertFalse(dataStore.isStateTracked("Phase", TrackingType.LOG));
        assertFalse(dataStore.isStateTracked("Phase", TrackingType.DB));
        
        assertFalse(dataStore.isStateTracked("Sigma"));
        
        dataStore.setStateTracked("Sigma", TrackingType.NO_STACK);
        assertTrue(dataStore.isStateTracked("Sigma"));
        assertTrue(dataStore.isStateTracked("Sigma", TrackingType.NO_STACK));        
        assertFalse(dataStore.isStateTracked("Sigma", TrackingType.STACK));        
        assertFalse(dataStore.isStateTracked("Sigma", TrackingType.LOG));        
        assertFalse(dataStore.isStateTracked("Sigma", TrackingType.DB));
        
        assertTrue(dataStore.isStateTracked("Phase"));
        assertTrue(dataStore.isStateTracked("Phase", TrackingType.NO_STACK));
        assertFalse(dataStore.isStateTracked("Phase", TrackingType.STACK));
        assertFalse(dataStore.isStateTracked("Phase", TrackingType.LOG));
        assertFalse(dataStore.isStateTracked("Phase", TrackingType.DB));

        dataStore.setInputPortTracked("inport1", TrackingType.NO_STACK)
                 .setInputPortTracked("inport2", TrackingType.STACK)
                 .setInputPortTracked("inport1", TrackingType.LOG);

        dataStore.setOutputPortTracked("outport1", TrackingType.STACK)
                 .setOutputPortTracked("outport2", TrackingType.NO_STACK)
                 .setOutputPortTracked("outport2", TrackingType.LOG);
        
        dataStore.setStateTracked("Phase", TrackingType.LOG)
                 .setStateTracked("Sigma", TrackingType.LOG);

        assertTrue(dataStore.isAtLeastOneInputPortPlotted());
        assertTrue(dataStore.isAtLeastOneOutputPortPlotted());
        assertTrue(dataStore.isAtLeastOneInputPortTrackedInLog());
        assertTrue(dataStore.isAtLeastOneOutputPortTrackedInLog());
        
        when(mockSimulator.getInput()).thenReturn(new message());
        when(mockSimulator.getOutput()).thenReturn(new message());

        List<Event> l = t.getCurrentTimeViewData(0.0);
        assertEquals(2, l.size());
        assertEquals("<== at 0.0: Phase, STATE, null==>", l.get(0).toString());
        assertEquals("<== at 0.0: Sigma, SIGMA, Infinity==>", l.get(1).toString());
        assertEquals(new StringBuilder("<B>Input Ports:</B><BR>")
            .append("inport1: <BR>")
            .append("<B>Output Ports:</B><BR>")
            .append("outport2: <BR>")
            .append("<B>Phase:</B>null<BR>")
            .append("<B>Sigma:</B>Infinity<BR>").toString(), t.getCurrentTrackingHTMLString());
        
        dataStore.setStateTracked("instanceVariable", TrackingType.STACK);
        l = t.getCurrentTimeViewData(0.0);
        assertEquals(3, l.size());
        assertEquals("<== at 0.0: Phase, STATE, null==>", l.get(0).toString());
        assertEquals("<== at 0.0: Sigma, SIGMA, Infinity==>", l.get(1).toString());
        assertEquals("<== at 0.0: instanceVariable, STATE, instance==>", l.get(2).toString());
        assertEquals(new StringBuilder("<B>Input Ports:</B><BR>")
            .append("inport1: <BR>")
            .append("<B>Output Ports:</B><BR>")
            .append("outport2: <BR>")
            .append("<B>Phase:</B>null<BR>")
            .append("<B>Sigma:</B>Infinity<BR>").toString(), t.getCurrentTrackingHTMLString());
        
        dataStore.setStateTracked("notState", TrackingType.STACK);
        l = t.getCurrentTimeViewData(0.0);
        assertEquals(4, l.size());
        assertEquals("<== at 0.0: Phase, STATE, null==>", l.get(0).toString());
        assertEquals("<== at 0.0: Sigma, SIGMA, Infinity==>", l.get(1).toString());
        assertEquals("<== at 0.0: instanceVariable, STATE, instance==>", l.get(2).toString());
        assertEquals("<== at 0.0: notState, STATE, false==>", l.get(3).toString());
        assertEquals(new StringBuilder("<B>Input Ports:</B><BR>")
            .append("inport1: <BR>")
            .append("<B>Output Ports:</B><BR>")
            .append("outport2: <BR>")
            .append("<B>Phase:</B>null<BR>")
            .append("<B>Sigma:</B>Infinity<BR>").toString(), t.getCurrentTrackingHTMLString());
        
        dataStore.setStateTracked("privateInstanceVariable", TrackingType.LOG);
        dataStore.setStateTracked("stringList", TrackingType.NO_STACK);

        l = t.getCurrentTimeViewData(0.0);
        assertEquals(6, l.size());
        assertEquals("<== at 0.0: Phase, STATE, null==>", l.get(0).toString());
        assertEquals("<== at 0.0: Sigma, SIGMA, Infinity==>", l.get(1).toString());
        assertEquals("<== at 0.0: instanceVariable, STATE, instance==>", l.get(2).toString());
        assertEquals("<== at 0.0: notState, STATE, false==>", l.get(3).toString());
        assertEquals("<== at 0.0: privateInstanceVariable, STATE, privateInstance==>", l.get(4).toString());
        assertEquals("<== at 0.0: stringList, STATE, []==>", l.get(5).toString());
        assertEquals(new StringBuilder("<B>Input Ports:</B><BR>")
            .append("inport1: <BR>")
            .append("<B>Output Ports:</B><BR>")
            .append("outport2: <BR>")
            .append("<B>Phase:</B>null<BR>")
            .append("<B>Sigma:</B>Infinity<BR>")
            .append("<B>privateInstanceVariable:</B>privateInstance<BR>").toString(), t.getCurrentTrackingHTMLString());

        l = t.getCurrentTimeViewData(1.1);
        assertEquals(6, l.size());
        assertEquals("<== at 1.1: Phase, STATE, null==>", l.get(0).toString());
        assertEquals("<== at 1.1: Sigma, SIGMA, Infinity==>", l.get(1).toString());
        assertEquals("<== at 1.1: instanceVariable, STATE, instance==>", l.get(2).toString());
        assertEquals("<== at 1.1: notState, STATE, false==>", l.get(3).toString());
        assertEquals("<== at 1.1: privateInstanceVariable, STATE, privateInstance==>", l.get(4).toString());
        assertEquals("<== at 1.1: stringList, STATE, []==>", l.get(5).toString());
        
        dataStore.setTimeTracked("tN", TrackingType.STACK);
        assertEquals(new StringBuilder("<B>Input Ports:</B><BR>")
                .append("inport1: <BR>")
                .append("<B>Output Ports:</B><BR>")
                .append("outport2: <BR>")
                .append("<B>Phase:</B>null<BR>")
                .append("<B>Sigma:</B>Infinity<BR>")
                .append("<B>privateInstanceVariable:</B>privateInstance<BR>").toString(), t.getCurrentTrackingHTMLString());

        dataStore.setTimeTracked("tL", TrackingType.NO_STACK);
        dataStore.setStateTracked("notState", TrackingType.LOG);
        dataStore.setStateTracked("instanceVariable", TrackingType.LOG);
        when(mockSimulator.getTN()).thenReturn(1.1);
        fakeAtomic.setSigma(1.1);
        fakeAtomic.getSim().initialize();
        assertEquals(new StringBuilder("<B>Input Ports:</B><BR>")
                .append("inport1: <BR>")
                .append("<B>Output Ports:</B><BR>")
                .append("outport2: <BR>")
                .append("<B>Phase:</B>null<BR>")
                .append("<B>Sigma:</B>1.1<BR>")
                .append("<B>notState:</B>false<BR>")
                .append("<B>instanceVariable:</B>instance<BR>")
                .append("<B>privateInstanceVariable:</B>privateInstance<BR>").toString(), t.getCurrentTrackingHTMLString());
        
        dataStore.setTrackingLogSortOrder(SortOrder.Alphabetical);
        
        assertEquals(new StringBuilder("<B>Input Ports:</B><BR>")
            .append("inport1: <BR>")
            .append("<B>Output Ports:</B><BR>")
            .append("outport2: <BR>")
            .append("<B>Phase:</B>null<BR>")
            .append("<B>Sigma:</B>1.1<BR>")
            .append("<B>instanceVariable:</B>instance<BR>")
            .append("<B>notState:</B>false<BR>")
            .append("<B>privateInstanceVariable:</B>privateInstance<BR>").toString(), t.getCurrentTrackingHTMLString());

        dataStore.setTrackingLogSortOrder(SortOrder.Reversed);
        
        assertEquals(new StringBuilder("<B>privateInstanceVariable:</B>privateInstance<BR>")
            .append("<B>notState:</B>false<BR>")
            .append("<B>instanceVariable:</B>instance<BR>")
            .append("<B>Sigma:</B>1.1<BR>")
            .append("<B>Phase:</B>null<BR>")
            .append("<B>Output Ports:</B><BR>")
            .append("outport2: <BR>")
            .append("<B>Input Ports:</B><BR>")
            .append("inport1: <BR>").toString(), t.getCurrentTrackingHTMLString());
    }

}
