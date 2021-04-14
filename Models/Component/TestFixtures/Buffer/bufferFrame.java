package Component.TestFixtures.Buffer;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import java.awt.Point;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import GenCol.entity;
import model.modeling.devs;
import model.modeling.message;
import model.modeling.script.SimpleTestFrame;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class bufferFrame extends SimpleTestFrame
{
    public final static int DEFAULT_CAPACITY = 10;
    public final static int DEFAULT_INIT_RELEASE = 0;
    
    @Target({METHOD})
    @Retention(RUNTIME)
    public @interface BufferParams
    {
        public int capacity() default DEFAULT_CAPACITY;
        public int initRelease() default DEFAULT_INIT_RELEASE;
    }
    
    public bufferFrame()
    {
        this("testframe", null);
    }
    
    public bufferFrame(String name, Method run_case)
    {
        super(name, run_case);

        int init_release = DEFAULT_INIT_RELEASE;
        int capacity = DEFAULT_CAPACITY;
        
        if (run_case != null)
        {
            BufferParams p = run_case.getAnnotation(BufferParams.class);
                
            if (p != null)
            {
                init_release = p.initRelease();
                capacity = p.capacity();
            }
        }
                
        devs model = new buffer("buffer", init_release, capacity);
        setTestModel(model); 
    }
    
    @TestScript(canSequence = true)
    public void TestNonBlocking() throws InterruptedException
    {
        // Testing state changes for BUFFER(release,buffer)

        entity job_in = new entity("job");
        
        // Test immediate job release:
        //    BUFFER(0,0) -> BUFFER(0,0) 
        
        message m = new message();
        m.add(makeContent("in", job_in));
        m.add(makeContent("release", new entity("release")));
        
        Inject(m);
        
        entity job_out = ReadOutput("out", false);
        
        assertEquals(job_in, job_out);
    }
    
    @TestScript(canSequence = true)
    public void TestStarved() throws InterruptedException
    {
        // Testing state changes for BUFFER(release,buffer)

        // Generate a release to make the buffer starve
        //    BUFFER(0,0) -> BUFFER(1,0) 
        
        Inject("release", new entity("release"));
        
        // Wait to ensure no output was generated.
        
        Wait(1.0, false);
        
        // Inject a job and anticipate an immediate release.
        //    BUFFER(1,0) -> BUFFER(0,0) 
        
        entity job_in = new entity("job");
        
        Inject("in", job_in);
        
        entity job_out = ReadOutput("out", false);
        
        assertEquals(job_in, job_out);
    }

    
    @TestScript(canSequence = true)
    public void TestBuffered() throws InterruptedException
    {
        // Testing state changes for BUFFER(release,buffer)

        entity job_in = new entity("job");

        // Add a job that cannot be released.
        //    BUFFER(0,0) -> BUFFER(0,1) 
        
        Inject("in", job_in);

        // Wait to ensure no output was generated.
        
        Wait(1.0, false);
        
        // Emit a release and anticipate the queued job.
        //    BUFFER(0,1) -> BUFFER(0,0) 
        
        Inject("release", new entity("release"));

        entity job_out = ReadOutput("out", false);
        
        assertEquals(job_in, job_out);
    }

    @TestScript(canSequence = true)
    public void TestFIFO() throws InterruptedException
    {
        // Testing state changes for BUFFER(release,buffer)
        
        entity[] jobs_in = {new entity("job1"), new entity("job2"), new entity("job3")};
        
        // Queue all three jobs
        //    BUFFER(0,0) -> BUFFER(0,1) -> BUFFER(0,2)        
        
        for (int i = 0; i < jobs_in.length; i++)
        {
            Inject("in", jobs_in[i]);
        }

        // Test that the released order matches the sequence 
        //    BUFFER(0,2) -> BUFFER(0,1)  -> BUFFER(0,0)
        
        for (int i = 0; i < jobs_in.length; i++)
        {
            Inject("release", new entity("release"));

            entity job_out = ReadOutput("out", false);
            
            assertEquals(jobs_in[i], job_out);   
        }        
    }
    
    @TestScript(canSequence = true)
    public void TestBalking() throws InterruptedException
    {
        final int NUM_TO_BALK = 3;
        final int NUM_TO_RELEASE = 4;
        
        // Balking begins after DEFAULT_CAPACITY items.
        // Testing state changes for BUFFER(release,buffer)

        Set<entity> jobs_in = new HashSet<entity>();
        message m = new message();
        
        // Add excess jobs and releases to the input.
        //    BUFFER(0,0) -> BUFFER(0,10)
        
        for (int i = 0; i < DEFAULT_CAPACITY + NUM_TO_BALK + NUM_TO_RELEASE; i++)
        {
            entity job = new entity("job" + i);
            
            m.add(makeContent("in", job));
            jobs_in.add(job);
        }
        
        for (int i = 0; i < NUM_TO_RELEASE; i++)
        {
            m.add(makeContent("release", new entity("release")));
        }
        
        // We expect NUM_TO_RELEASE jobs to immediately release,
        // DEFAULT_CAPACITY to remain in the queue, and NUM_TO_BALK to balk.
        
        m = InjectAndRead(m);
        
        assertEquals(NUM_TO_BALK + NUM_TO_RELEASE, m.getLength());
        
        int num_released = 0;
        int num_balked = 0;
        
        for (int i = 0; i < m.getLength(); i++)
        {
            if (m.onPort("out", i))
            {
                num_released++;
            }
            else if (m.onPort("balked", i))
            {
                num_balked++;
            }
            
            jobs_in.remove(m.read(i).getValue());
        }
        
        assertEquals(NUM_TO_RELEASE, num_released);
        assertEquals(NUM_TO_BALK, num_balked);

        // Return the buffer to BUFFER(0,0)

        m.clear();
        
        for (int i = 0; i < DEFAULT_CAPACITY; i++)
        {
            m.add(makeContent("release", new entity("release")));
        }
        
        m = InjectAndRead(m);

        assertEquals(DEFAULT_CAPACITY, m.getLength());
        
        jobs_in.removeAll(m.valuesOnPort("out"));
        assertTrue(jobs_in.isEmpty());
    }
    
    @TestScript(canSequence = false)
    @BufferParams(capacity = 0)
    public void TestNoCapacity() throws InterruptedException
    {
        // Testing state changes for BUFFER(release,buffer)

        entity job_in = new entity("job1");
        
        // Test that job is immediately balked without a release:
        //    BUFFER(0,0) -> BUFFER(0,0) 
        
        Inject("in", job_in);
        
        entity job_out = ReadOutput("balked", false);
        
        assertEquals(job_in, job_out);

        // Release count overrides capacity. Test that job is immediately released:
        //    BUFFER(0,0) -> BUFFER(0,0)

        message m = new message();
        job_in = new entity("job2");
        m.add(makeContent("in", job_in));
        m.add(makeContent("release", new entity("release")));
        
        Inject(m);
        
        job_out = ReadOutput("out", false);
        
        assertEquals(job_in, job_out);
    }
    
    @TestScript(canSequence = false)
    @BufferParams(initRelease = 2, capacity = 1)
    public void TestInitialReleases() throws InterruptedException
    {
        // Testing state changes for BUFFER(release,buffer)

        // Test that two jobs are released, one is balked, one is buffered:
        //    BUFFER(2,0) -> BUFFER(0,1) 
        
        message m = new message();
        m.add(makeContent("in", new entity("job1")));
        m.add(makeContent("in", new entity("job2")));
        m.add(makeContent("in", new entity("job3")));
        m.add(makeContent("in", new entity("job4")));
        
        m = InjectAndRead(m);
        assertEquals(3, m.size());
        
        int num_released = 0;
        int num_balked = 0;
        
        for (int i = 0; i < m.size(); i++)
        {
            if (m.onPort("balked", i))
            {
                num_balked++;
            }
            else if (m.onPort("out", i))
            {
                num_released++;
            }
        }
        
        assertEquals(1, num_balked);
        assertEquals(2, num_released);
    }
    
    
    /**
     * Automatically generated by the SimView program.
     * Do not edit this manually, as such changes will get overwritten.
     */
    @Override
    public void layoutForSimViewRedirect(ViewableDigraph model)
    {
        model.setPreferredSize(new Dimension(591, 332));
        ((ViewableComponent)model.withName("testframe")).setPreferredLocation(new Point(206, 181));
        ((ViewableComponent)model.withName("buffer")).setPreferredLocation(new Point(34, 62));
    }
}
