package model.modeling.script;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;

import model.modeling.devs;
import model.modeling.messageHandler;

/**
 * {@link SimpleTestFrame} is a DEVS-Scripting model for black-box 
 * testing atomic or coupled DEVS models. This is the most common
 * test frame for unit testing. During construction of the derived
 * test frame, calling <code>setTestModel()</code> will 
 * establish ports on the test frame, and couples all ports with 
 * the test frame. 
 * 
 * @author Matthew McLaughlin
 */
public abstract class SimpleTestFrame extends TestFrame
{
    private devs test_model;
    
    /**
     * The simple test frame is used to test atomic or coupled DEVS
     * models. Derived classes should construct the model under testing
     * and call {@link #setTestModel(devs)} to establish ports and
     * couplings with that model.
     * 
     * @param name name of the {@link TestFrame} model
     * @param run_case the method to run, or <code>null</code> for
     *    all sequential test scripts.
     */
    public SimpleTestFrame(String name, Method run_case)
    {
        super(name, run_case);
    }
    
    /**
     * Set the model under testing. This establishes ports on the test frame
     * to mirror the ports on the <code>test_model</code>. If ports are added
     * later to the model being tested, they will not be reflected.
     * <p>
     * This model can be later retrieved calling {@link #getTestModel()}.
     * 
     * @param test_model the {@link devs} model under testing.
     */
    public void setTestModel(devs test_model)
    {
        devs old_model = this.test_model;
        
        if (old_model != test_model)
        {
            if (old_model != null)
            {
                messageHandler mh = old_model.getMessageHandler();

                TestFrame.extractPortNames(mh.getInports()).forEach((String p) -> {removeInport(p);});
                TestFrame.extractPortNames(mh.getOutports()).forEach((String p) -> {removeOutport(p);});
            }

            this.test_model = test_model;

            messageHandler mh = test_model.getMessageHandler();

            TestFrame.extractPortNames(mh.getInports()).forEach((String p) -> {addOutport(p);});
            TestFrame.extractPortNames(mh.getOutports()).forEach((String p) -> {addInport(p);});
        }
    }
    
    /**
     * Retrieve the model under testing--set by {@link #setTestModel(devs)}
     * @return the {@link devs} model under testing
     */
    public devs getTestModel()
    {
        return test_model;
    }
    
    @Override
    public TestFixture createFixture(String name)
    {
        Assertions.assertNull(getParent(), "cannot call createFixture() multiple times.");
        Assertions.assertNotNull(test_model, "no model under testing");
        
        return new Fixture(name);
    }

    private class Fixture extends TestFixture 
    {        
        public Fixture(String name)
        {
            super(name, SimpleTestFrame.this);
            
            messageHandler mh = test_model.getMessageHandler();

            add(test_model);
            
            TestFrame.extractPortNames(mh.getInports()).forEach((String p) -> {
                addCoupling(SimpleTestFrame.this,p,test_model,p);
            });

            TestFrame.extractPortNames(mh.getOutports()).forEach((String p) -> {
                addCoupling(test_model,p,SimpleTestFrame.this,p);
            });
        }
    }
}
