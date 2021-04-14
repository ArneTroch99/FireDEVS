package facade.modeling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import facade.modeling.FAtomicModelTestArtifacts.FakeViewableAtomic;
import facade.simulation.FSimulator;
import util.classUtils.DevsClassFieldFactory;
import view.modeling.ViewableAtomic;

public class TestFAtomicModel
{
    @Test
    public void testGetStateVariableNames()
    {
        ViewableAtomic fakeAtomic = new FakeViewableAtomic();
        fakeAtomic.setStates(DevsClassFieldFactory.createDevsClassFieldMap(FakeViewableAtomic.class));

        FSimulator mockSimulator = mock(FSimulator.class);

        FAtomicModel cut = new FAtomicModel(fakeAtomic);
        cut.setSimulator(mockSimulator);

        String[] expected = {
                "Phase",
                "Sigma",
                "instanceVariable",
                "notState",
                "privateInstanceVariable",
                "stringList",
                "stringList_defaultChecked"
        };
        
        int[] i = { 0 };
        cut.getStateNames().forEachSorted((String name) -> {
           assertEquals(expected[i[0]], name);
           ++i[0];
        });
        assertEquals(7, i[0]);
    }
    
    @Test
    public void testGetStateVariableByName()
    {
        ViewableAtomic fakeAtomic = new FakeViewableAtomic();
        fakeAtomic.setStates(DevsClassFieldFactory.createDevsClassFieldMap(FakeViewableAtomic.class, fakeAtomic));

        FSimulator mockSimulator = mock(FSimulator.class);

        FAtomicModel cut = new FAtomicModel(fakeAtomic);
        cut.setSimulator(mockSimulator);
        
        assertEquals("instance", cut.getStateValue("instanceVariable"));
        assertFalse((Boolean) cut.getStateValue("notState"));
        assertEquals("privateInstance", cut.getStateValue("privateInstanceVariable"));
    }
}
