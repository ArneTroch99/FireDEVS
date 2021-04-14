package model.modeling.script;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Iterator;
import java.util.List;

import GenCol.Pair;
import model.modeling.IODevs;
import model.modeling.couprel;
import model.modeling.devs;
import model.modeling.digraph;
import model.modeling.messageHandler;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

/**
 * {@link TestFixture} is a container model for a {@link TestFrame} and
 * model(s) under testing.
 * 
 * @author Matthew McLaughlin
 */
public class TestFixture extends ViewableDigraph
{
    /**
     * The {@link TestFrame} object and child atomic model that created this fixture.
     */
    protected TestFrame test_frame;
    
    /**
     * Adds the frame and couples and output port to the fixture. This
     * coupling is useful for the visualization of pass/fail in DEVS-Suite.
     * 
     * @param name name of the {@link TestFixture} model
     * @param frame the {@link TestFrame} object and child atomic model
     */
    public TestFixture(String name, TestFrame frame)
    {
        super(name);
        
        test_frame = frame;
        add(frame);

        addOutport("out");
        addCoupling(frame, "internal-out", this, "out");
    }
    
    @Override
    public ViewableComponent getLayoutRedirect()
    {
        return test_frame;
    }
    
    /**
     * Retrieve the test frame that generated this fixture.
     * @return the {@link TestFrame}
     */
    public TestFrame getTestFrame()
    {
        return test_frame;
    }

    /**
     * A utility function to ensure that the specified {@link devs} model has
     * the specified input port. If the port is not present, an assertion is raised.
     * 
     * @param model the {@link devs} model being checked
     * @param port_name the port
     */
    public static void assertHasInputPort(devs model, String port_name)
    {
        messageHandler m1 = model.getMessageHandler();
        List<String> ports = m1.getInports().getNameList();

        if (!ports.contains(port_name))
        {
            throw new AssertionError("Input port '" + port_name + "' expected on '" + model.getName() + "'");
        }
    }

    /**
     * A utility function to ensure that the specified {@link devs} model has
     * the specified output port. If the port is not present, an assertion is raised.
     * 
     * @param model the {@link devs} model being checked
     * @param port_name the port
     */
    public static void assertHasOutputPort(devs model, String port_name)
    {
        messageHandler m1 = model.getMessageHandler();
        List<String> ports = m1.getOutports().getNameList();
        
        if (!ports.contains(port_name))
        {
            throw new AssertionError("Output port '" + port_name + "' expected on '" + model.getName() + "'");
        }
    }
    
    private static void assertValidEIC(digraph e1, String p1, IODevs e2, String p2)
    {
        
        // Tests whether this is a valid external input coupling.
        
        assertNotNull(e1);
        assertNotNull(e2);

        assertHasInputPort(e1, p1);
        
        if (e2 instanceof devs)
        {
            // Needs to access message handler
            
            assertHasInputPort((devs) e2, p2);
        }
    }
    
    private static void assertValidEOC(IODevs e1, String p1, digraph e2, String p2)
    {
        
        // Tests whether this is a valid external output coupling.
        
        assertNotNull(e1);
        assertNotNull(e2);
        
        if (e1 instanceof devs)
        {
            // Needs to access message handler
            
            assertHasOutputPort((devs) e1, p1);
        }

        assertHasOutputPort(e2, p2);
    }
    
    private static void assertValidIC(IODevs e1, String p1, IODevs e2, String p2)
    {
        
        // Tests whether this is a valid internal coupling.
        
        assertNotNull(e1);
        assertNotNull(e2);
        
        if (e1 instanceof devs)
        {
            // Needs to access message handler
            
            assertHasOutputPort((devs) e1, p1);
        }
        
        if (e2 instanceof devs)
        {
            // Needs to access message handler
            
            assertHasInputPort((devs) e2, p2);
        }
    }
    
    /**
     * Recursively decomposes the specified model to ensure all couplings make
     * sense. If the source port of a coupling belongs to <code>m</code>, then the
     * coupling will be checked as an external input coupling (EIC). If the
     * destination port belongs to <code>m</code>, then the coupling is checked
     * as an external output coupling (EOC). Otherwise the coupling is check as an
     * internal coupling (IC). 
     * <p>
     * Recall that EICs couple the input port of the parent coupled model to the 
     * input port of a child model. EOCs couple their outputs. ICs couple an output 
     * port of a child model to the input port of a child model.
     * <p>
     * This test is free. Even if no test scripts are given, when 
     * {@link TestFrame#getTestCases(Class, boolean)} is called, a dynamic test is added
     * to call this method.
     * 
     * @param the coupled model to test structure.
     */
    public static void structuredTests(digraph m)
    {
        couprel cp = m.getCouprel();
        Iterator<Pair<Object, Object>> it = cp.iterator();

        while(it.hasNext())
        {
            Pair<Object, Object> rel = it.next();
            Pair<?,?> coup1 = (Pair<?,?>) rel.key;
            Pair<?,?> coup2 = (Pair<?,?>) rel.value;
            String e1 = (String) coup1.key;
            String p1 = (String) coup1.value;
            String e2 = (String) coup2.key;
            String p2 = (String) coup2.value;
            
            try
            {
                if (m.eq(e1))
                {
                    assertValidEIC(m, p1, m.withName(e2), p2);
                }
                else if (m.eq(e2))
                {
                    assertValidEOC(m.withName(e1), p1, m, p2);
                }
                else
                {
                    assertValidIC(m.withName(e1), p1, m.withName(e2), p2);
                }
            }
            catch (Error e)
            {
                throw new AssertionError("Bad coupling from (" + e1 + ", " + p1 +") to (" + e2 + ", " + p2 + ")", e);
            }
        }  
        
        // Recursively check all children components.

        Iterator<IODevs> i = m.getComponents().iterator();
        while (i.hasNext()) {
            IODevs child = i.next();
            
            if (child instanceof digraph)
            {
                structuredTests((digraph) child);
            }
        }
    }
}
