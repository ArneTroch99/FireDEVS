package Component.TestFixtures.CONWIP;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import java.awt.Point;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import GenCol.entity;
import GenCol.intEnt;
import model.modeling.devs;
import model.modeling.message;
import model.modeling.script.SimpleTestFrame;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class conwipFrame extends SimpleTestFrame
{   
    @Target({METHOD})
    @Retention(RUNTIME)
    public @interface ConwipParams
    {
        public int targetWIP() default 1;
    }
    
    public conwipFrame()
    {
        this("testframe", null);
    }
    
    public conwipFrame(String name, Method run_case)
    {
        super(name, run_case);
        
        int target_wip = 1;
        
        if (run_case != null)
        {
            ConwipParams p = run_case.getAnnotation(ConwipParams.class);
                
            if (p != null)
            {
                target_wip = p.targetWIP();
            }
        }
                
        devs model = new conwip("conwip", target_wip);
        setTestModel(model); 
    }
    
    @TestScript(canSequence = true)
    public void TestNonBlocking() throws InterruptedException
    {
        // Target WIP = 1
        // Testing state changes for CONWIP(wip,backlog)

        entity job_in = new entity("job");
        
        // Test immediate job release:
        //    CONWIP(0,0) -> CONWIP(1,0) 
        
        Inject("in", job_in);
        message m = ReadOutput();
        
        assertEquals(1, m.getLength());
        assertTrue(m.onPort("out", 0));
        assertEquals(m.read(0).getValue(), job_in);
       
        // Notify CONWIP the job has left the system
        //    CONWIP(1,0) -> CONWIP(0,0)

        Inject("release", job_in);
    }

    @TestScript(canSequence = true)
    public void TestBacklogged() throws InterruptedException
    {
        // Target WIP = 1
        // Testing state changes for CONWIP(wip,backlog)

        entity job_in = new entity("job");
        
        // Test immediate job release for one job, backlog the next two:
        //    CONWIP(0,0) -> CONWIP(1,2) 

        message m = new message();
        m.add(makeContent("in", job_in));
        m.add(makeContent("in", job_in));
        m.add(makeContent("in", job_in));
        
        Inject(m);

        entity job_out = ReadOutput("out", false);
        
        assertEquals(job_in, job_out);
        
        // Release the remaining jobs
        //    CONWIP(1,2) -> CONWIP(1,1) -> CONWIP(1,0)
        
        for (int i = 0; i < 2; i++)
        {
            Inject("release", job_in);
            
            job_out = ReadOutput("out", false);
            
            assertEquals(job_in, job_out);
        }

        // Notify CONWIP the last job has left the system
        //    CONWIP(1,0) -> CONWIP(0,0)

        Inject("release", job_in);
    }

    @TestScript(canSequence = true)
    public void TestFIFO() throws InterruptedException
    {
        // Target WIP = 1
        // Testing state changes for CONWIP(wip,backlog)

        // Queue up the first job:
        //    CONWIP(0,0) -> CONWIP(1,0) 

        entity job1_in = new entity("job1");
        
        Inject("in", job1_in);
        
        // Testing the output at this point isn't useful.
        // Normal operation has already been tested.
        // Queue up the next two jobs:
        //    CONWIP(1,0) -> CONWIP(1,1) -> CONWIP(1,2) 
        
        entity job2_in = new entity("job2");
        entity job3_in = new entity("job3");

        Inject("in", job2_in);
        Inject("in", job3_in);

        // Test that the next job released is job2:
        //    CONWIP(1,2) -> CONWIP(1,1) 
        
        Inject("release", job1_in);

        entity job_out = ReadOutput("out", false);
        
        assertEquals(job2_in, job_out);
        
        // Test that the final job released is job3:
        //    CONWIP(1,1) -> CONWIP(1,0) 
        
        Inject("release", job2_in);

        job_out = ReadOutput("out", false);
        
        assertEquals(job3_in, job_out);

        // Notify CONWIP the last job has left the system
        //    CONWIP(1,0) -> CONWIP(0,0)

        Inject("release", job3_in);
    }
    
    @TestScript(canSequence = true)
    public void TestIncreaseWIP() throws InterruptedException
    {
        // Target WIP = 1
        // Testing state changes for CONWIP(wip,backlog)

        entity job_in = new entity("job");
        
        // Test immediate job release for one job, backlog the next two:
        //    CONWIP(0,0) -> CONWIP(1,2) 

        message m = new message();
        m.add(makeContent("in", job_in));
        m.add(makeContent("in", job_in));
        m.add(makeContent("in", job_in));
        
        Inject(m);

        entity job_out = ReadOutput("out", false);
        
        assertEquals(job_in, job_out);
        
        // Update the target WIP to 4, this will empty the backlog
        //    CONWIP(1,2) -> CONWIP(3,0)
        
        Inject("set", new intEnt(4));

        m = ReadOutput();
        
        assertEquals(2, m.getLength());
        assertTrue(m.onPort("out", 0));
        assertTrue(m.onPort("out", 1));
        assertEquals(m.read(0).getValue(), job_in);
        assertEquals(m.read(1).getValue(), job_in);
        
        // The next job will release immediately
        //    CONWIP(3,0) -> CONWIP(4,0)
        
        Inject("in", job_in);

        job_out = ReadOutput("out", false);
        
        assertEquals(job_in, job_out);
        
        // Release all jobs
        //    CONWIP(4,0) -> CONWIP(0,0)
        
        m = new message();
        m.add(makeContent("release", job_in));
        m.add(makeContent("release", job_in));
        m.add(makeContent("release", job_in));
        m.add(makeContent("release", job_in));
        Inject(m);
        
        // Reset the model to Target WIP = 1

        Inject("set", new intEnt(1));
    }
    
    @TestScript(canSequence = false)
    @ConwipParams(targetWIP = 2)
    public void TestReduceWIP() throws InterruptedException
    {
        // Target WIP = 2
        // Testing state changes for CONWIP(wip,backlog)

        entity job_in = new entity("job");
        
        // Test immediate job release for two jobs, backloging the next:
        //    CONWIP(0,0) -> CONWIP(2,1) 

        message m = new message();
        m.add(makeContent("in", job_in));
        m.add(makeContent("in", job_in));
        m.add(makeContent("in", job_in));
        
        m = InjectAndRead(m);
        
        assertEquals(2, m.getLength());
        assertTrue(m.onPort("out", 0));
        assertTrue(m.onPort("out", 1));
        assertEquals(m.read(0).getValue(), job_in);
        assertEquals(m.read(1).getValue(), job_in);
        
        // Update the target WIP to 1
        
        Inject("set", new intEnt(1));
        Wait(1.0, false);
        
        // Releasing a job will bring WIP to target WIP,
        // and thus not release another job:
        //    CONWIP(2,1) -> CONWIP(1,1) 

        Inject("release", job_in);
        Wait(1.0, false);
        
        // Test that releasing the next job will:
        //    CONWIP(1,1) -> CONWIP(1,0) 
        
        Inject("release", job_in);

        entity job_out = ReadOutput("out", false);
        
        assertEquals(job_in, job_out);
    }

    @TestScript(canSequence = true)
    public void TestKanban() throws InterruptedException
    {
        // Target WIP = 1
        // Testing state changes for CONWIP(wip,backlog)

        // Get system to a WIP = 1 state
        //    CONWIP(0,0) -> CONWIP(1,0)
        
        entity job_in = new entity("job");
        Inject("in", job_in);
        
        // Parallel release and queue the next job
        //    CONWIP(1,0) -> CONWIP(1,0)
        
        message m = new message();
        m.add(makeContent("in", job_in));
        m.add(makeContent("release", job_in));
        
        Inject(m);

        entity job_out = ReadOutput("out", false);
        
        assertEquals(job_in, job_out);

        // Notify CONWIP the last job has left the system
        //    CONWIP(1,0) -> CONWIP(0,0)

        Inject("release", job_in);
    }
    
    @TestScript(canSequence = false)
    @ConwipParams(targetWIP = 1)
    public void TestInvalidWIP() throws InterruptedException
    {
        // Target WIP = 1
        // Testing state changes for CONWIP(wip,backlog)

        entity job_in = new entity("job");

        // WIP cannot be negative
        //    CONWIP(0,0) -> CONWIP(!!!)
        
        Inject("release", job_in);
        message m = ReadOutput();
        
        assertEquals(1, m.getLength());
        assertTrue(m.onPort("fail", 0));
        
        // Test that normal operation cannot not proceed.
        //    CONWIP(!!!) -> CONWIP(!!!)
        
        Inject("in", job_in);
        Wait(1.0, false);
    }
    
    /**
     * Automatically generated by the SimView program.
     * Do not edit this manually, as such changes will get overwritten.
     */
    @Override
    public void layoutForSimViewRedirect(ViewableDigraph model)
    {
        model.setPreferredSize(new Dimension(605, 138));
        ((ViewableComponent)model.withName("testframe")).setPreferredLocation(new Point(274, 61));
        ((ViewableComponent)model.withName("conwip")).setPreferredLocation(new Point(25, 8));
    }
}
