package model.modeling.script;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;

/**
 * CompoundTestFrame is a DEVS-Scripting model for black-box
 * testing multiple DEVS models in a scenario. This child class
 * offers more control than a SimpleTestFrame, but requires its
 * input/outport ports, models, and couplings to be explicitly 
 * added. Examples of this class are provided in 
 * {@link Component.TestFixture.pipelineFrame} and
 * {@link Component.TestFixtures.factoryFrame}.
 * 
 * @author Matthew McLaughlin
 */
public abstract class CompoundTestFrame extends TestFrame
{
    /**
     * The compound test frame is used for testing a collection
     * of interconnected models. It is the responsibility of 
     * the derived class to establish all input and output ports
     *  on the frame here. Note "internal-out" will be the only
     *  port automatically generated.
     *  
     * @param name name of the {@link TestFrame} model
     * @param run_case the method to run, or <code>null</code> for
     *    all sequential test scripts.
     */
    protected CompoundTestFrame(String name, Method run_case)
    {
        super(name, run_case);
    }

    @Override
    public TestFixture createFixture(String name)
    {
        // This method can only be called once for an instance of
        // a test frame.
        
        Assertions.assertNull(getParent(), "cannot call createFixture() multiple times.");
        
        return new Fixture(name);
        
        // It is the responsibility of the derived class to
        // establish all internal coupling within this test fixture.
        // Note that the EIC "internal-out" to "out" is already handled.
    }

    private class Fixture extends TestFixture 
    {
        public Fixture(String name)
        {
            super(name, CompoundTestFrame.this);
        }
    }
}
