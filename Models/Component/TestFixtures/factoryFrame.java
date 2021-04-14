/**
 * This compound test frame sets up a factory, adds cards for
 * manufacturing, and will run indefinitely. To end each test,
 * the method shutdown() must be called. This method sets
 * the CONWIP to zero to stop production. Due to the nature of 
 * this design, none of the tests can be run in sequence.
 */
package Component.TestFixtures;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Point;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import Component.TestFixtures.CONWIP.conwip;
import Component.TestFixtures.Workstation.ws;
import GenCol.entity;
import GenCol.intEnt;
import model.modeling.message;
import model.modeling.script.CompoundTestFrame;
import model.modeling.script.TestFixture;
import model.modeling.script.Trajectory;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class factoryFrame extends CompoundTestFrame
{
    @Target({METHOD})
    @Retention(RUNTIME)
    public @interface factoryParams
    {
        public int bufferCapacity() default 5;
        public double[] procTimes() default {2.0, 3.0, 2.0};
    }
    
    public factoryFrame()
    {
        this("testframe", null);
    }
    
    public factoryFrame(String name, Method run_case)
    {
        super(name, run_case);

        // Add ports for the test frame to interact with other models

        addOutport("set_wip");
        addOutport("cards");
        addInport("out");
        addInport("fail");
    }
    
    private void setup(double delay, int n, int wip) throws InterruptedException
    {
        // Setup and push factory through transient state.

        assertTrue(n >= wip, "number of cards should be at least WIP");
        
        Inject("set_wip", new intEnt(wip));
        
        for (int i = 0; i < n; i++)
        {
            entity e = WaitUntilOutput(delay, "fail", true);
            assertNull(e, "factory buffers are over capacity.");
            
            Inject("cards", new entity("item" + Integer.toString(i + 1)));
        }

        // Push through some arbitrary transient state.
        
        entity e = WaitUntilOutput(20.0, "fail", true);
        assertNull(e, "factory buffers are over capacity.");
    }
    
    private void shutdown() throws InterruptedException
    {
        Inject("set_wip", new intEnt(0));
    }

    @TestScript(canSequence = false, maxSimSteps = 300)
    public void TestShutdown() throws InterruptedException
    {
        setup(0.0, 3, 3);
        
        // Signal factory to shutdown, wait minimum time.

        shutdown();
        Wait(8.0, true);
        
        // Ensure nothing else leaves the factory.
        
        Wait(100.0, false);
    }
    
    private void setupBulkBuffer() throws InterruptedException
    {
        // This method sets up TestBulkBuffer[1-3].
        
        message m = new message();

        // The CONWIP is 3. By injecting 2, the W/S #1 will
        // contain 1 in buffer and 1 in processor.
        
        m.add(makeContent("cards", new entity("item")));
        m.add(makeContent("cards", new entity("item")));
        
        Inject(m);
        
        // This will inject a third item after W/S #1 has cleared the
        // processor OR cause W/S #1 to bulk if processing time is
        // longer than 1.
        
        WaitAndInject(1.0, "cards", new entity("item"), false);
    }

    @TestScript(canSequence = false, maxSimSteps = 300)
    @factoryParams(bufferCapacity = 1, procTimes = {5.0, 1.0, 1.0})
    public void TestBulkBuffer1() throws InterruptedException
    {
        setupBulkBuffer();       
        ReadOutput("fail", false);
        shutdown();
    }

    @TestScript(canSequence = false, maxSimSteps = 300)
    @factoryParams(bufferCapacity = 1, procTimes = {1.0, 5.0, 1.0})
    public void TestBulkBuffer2() throws InterruptedException
    {
        setupBulkBuffer();
        WaitForOutputAt(2.0, "fail", false, false);
        shutdown();
    }

    @TestScript(canSequence = false, maxSimSteps = 300)
    @factoryParams(bufferCapacity = 1, procTimes = {1.0, 1.0, 5.0})
    public void TestBulkBuffer3() throws InterruptedException
    {
        setupBulkBuffer();
        WaitForOutputAt(3.0, "fail", false, false);
        shutdown();
    }

    private double measureThroughput(int wip, double e_max) throws InterruptedException
    {
        double e_rem = e_max;
        int n_recvd = 0;
        
        while (e_rem > 0)
        {
            Trajectory t = WaitUntilOutput(e_rem);
            
            if (t == null)
            {
                break;
            }
            
            message m = t.getMessage();
            
            for (int i = 0; i < m.getLength(); i++)
            {
                assertTrue(m.onPort("out", i));
                n_recvd++;
            }

            e_rem -= t.getElapsedTime();
        }
        
        // The average amount of time each item spends in production.
        // Little's Law: WIP = [Production Rate] * [Throughput Time]
        
        return e_max * wip / n_recvd;
    }
    
    @TestScript(canSequence = false, maxSimSteps = 30000)
    @factoryParams(bufferCapacity = 5, procTimes = {2.0, 3.0, 2.0})
    public void TestThroughput() throws InterruptedException
    {
        setup(1.0, 30, 1);
        
        // In steady state, given W/S #2 has the highest processing time
        // and discrete timing, we can expect W/S #2 to have the most 
        // inventory in its buffer and all other buffers to contain 
        // 0 or 1 items.
        
        // Therefore, 3 processors + 5 W/S#2 buffer = 8 WIP is the largest that
        // can be held in the system. However, W/S #2 cannot accept completed
        // work from W/S #1, therefore 7 is the max before any buffer overflows
        // are generated.
        
        for (int wip = 1; wip < 8; wip++)
        {
            Inject("set_wip", new intEnt(wip));

            double tt = measureThroughput(wip, 100.0);
            System.out.println("WIP = " + wip + ", TT = " + tt);
        }

        shutdown();
    }

    @TestScript(canSequence = false, maxSimSteps = 30000)
    @factoryParams(bufferCapacity = 5, procTimes = {2.0, 3.0, 2.0})
    public void TestOverCapacity() throws InterruptedException
    {
        message m = new message();

        // Send six item (5 to fill W/S #1 buffer, 1 of W/S #1 to work on).
        
        m.add(makeContent("set_wip", new intEnt(9)));
        m.add(makeContent("cards", new entity("item")));
        m.add(makeContent("cards", new entity("item")));
        m.add(makeContent("cards", new entity("item")));
        m.add(makeContent("cards", new entity("item")));
        m.add(makeContent("cards", new entity("item")));
        m.add(makeContent("cards", new entity("item")));
        Inject(m);
        
        // Send seventh and eight item when W/S #1 completes each.
        
        m = new message();
        m.add(makeContent("cards", new entity("item")));
        WaitAndInject(2.0, m, false);
        WaitAndInject(2.0, m, false);
        
        // We can pack more into this factory, but steady state should
        // fail successfully with eight.
        
        entity e = WaitUntilOutput(100.0, "fail", true);
        assertNotNull(e);
        
        shutdown();
    }

    
    @Override
    public TestFixture createFixture(String name)
    {
        TestFixture root = super.createFixture(name);

        int ws_capacity = 5;
        double[] proc_times = {2.0, 3.0, 2.0};
        
        if (test_case != null)
        {
            factoryParams p = test_case.getAnnotation(factoryParams.class);
                
            if (p != null)
            {
                ws_capacity = p.bufferCapacity();
                proc_times = p.procTimes();
            }
        }
        
        // Add all the models
        
        ViewableAtomic cw = new conwip("conwip", proc_times.length);
        ws[] work_stations = new ws[proc_times.length];
        
        root.add(cw);
        
        for (int i = 0; i < proc_times.length; i++)
        {
            work_stations[i] = new ws("ws" + Integer.toString(i + 1), proc_times[i], ws_capacity);
            root.add(work_stations[i]);
        }

        // Define all couplings
        
        ws first_ws = work_stations[0];
        ws last_ws = work_stations[work_stations.length - 1];
        
        root.addCoupling(this,"set_wip",cw,"set");
        root.addCoupling(this,"cards",cw,"in");

        root.addCoupling(cw,"out",first_ws,"in");
        root.addCoupling(cw,"fail",this,"fail");
        root.addCoupling(first_ws,"fail",this,"fail");

        for (int i = 1; i < work_stations.length; i++)
        {
            root.addCoupling(work_stations[i-1],"out",work_stations[i],"in");
            root.addCoupling(work_stations[i],"fail",this,"fail");
        }

        root.addCoupling(last_ws,"out",this,"out");
        root.addCoupling(last_ws,"out",cw,"release");
        root.addCoupling(last_ws,"out",cw,"in");

        return root;
    }
    
    /**
     * Automatically generated by the SimView program.
     * Do not edit this manually, as such changes will get overwritten.
     */
    @Override
    public void layoutForSimViewRedirect(ViewableDigraph model)
    {
        model.setPreferredSize(new Dimension(675, 491));
        ((ViewableComponent)model.withName("testframe")).setPreferredLocation(new Point(368, 29));
        ((ViewableComponent)model.withName("conwip")).setPreferredLocation(new Point(203, 96));
        ((ViewableComponent)model.withName("ws1")).setPreferredLocation(new Point(48, 152));
        ((ViewableComponent)model.withName("ws3")).setPreferredLocation(new Point(109, 265));
        ((ViewableComponent)model.withName("ws2")).setPreferredLocation(new Point(81, 209));
    }
}
