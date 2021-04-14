package Component.TestFixtures.Homework;

import static model.modeling.script.TestFixture.assertHasInputPort;
import static model.modeling.script.TestFixture.assertHasOutputPort;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Dimension;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//import Component.TestFixture.pipeline;
import GenCol.entity;
import model.modeling.devs;
import model.modeling.initializer;
import model.modeling.initializerFactory;
import model.modeling.message;
import model.modeling.script.HomeworkTestFrame;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class procHomework extends HomeworkTestFrame
{
    public static final double DELAY = 4.3;
    
    public static void main(String[] args)
    {
        try
        {
            runTestCases(procHomework.class, "results.csv");
        }
        catch (FileNotFoundException e)
        {
            // Cannot generate output.
            
            e.printStackTrace();
        }
    }
    
    public procHomework(String name, Method run_case)
    {
        // The model loader will recognize the procHomework class as a TestFrame,
        // and therefore requires this constructor. However, homework test frames
        // have multiple implementations, so we demonstrate only one. For a parallel
        // experience, load procHomeworkParallel.
        
        this(name, run_case, proc1.class);
    }
    
    public procHomework(String name, Method run_case, Class<? extends devs> student_model)
    {
        super(name, run_case, student_model);
        
        if (student_model == null)
        {
            return;
        }

        try
        {
            Constructor<? extends devs> constructor;
            devs model;
            
            // The student must implement the correct constructor.
            
            constructor = student_model.getConstructor(String.class, Double.TYPE);
            model = constructor.newInstance("proc", DELAY);
        
            // The student must implement the correct ports.
            
            assertHasOutputPort(model, "out");
            assertHasInputPort(model, "in");
            
            setTestModel(model);
        }
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            throw new AssertionError("Student did not implement proper constructor");
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // The section below implements the black-box tests to ensure proper implementation
    // of the processor model(s).
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @TestScript
    public void TestPassivated() throws InterruptedException
    {
        entity job_out;
        job_out = WaitUntilOutput(100.0 * DELAY, "out", false);

        assertNull(job_out);
    }
    
    @TestScript
    public void TestNormal() throws InterruptedException
    {
        entity job_in = new entity("job");
        entity job_out;
        
        // Test the normal behavior
        
        Inject("in", job_in);
        
        job_out = WaitForOutputAt(DELAY, "out", false, false);

        assertEquals(job_in, job_out);
    }
    
    @TestScript
    public void TestConfluence() throws InterruptedException
    {
        entity job_in1 = new entity("job1");
        entity job_in2 = new entity("job2");
        entity job_out;
        
        Inject("in", job_in1);

        WaitAndInject(DELAY, "in", job_in2, false);
        
        job_out = GetOutput("out", false);

        assertEquals(job_in1, job_out);
        job_out = WaitForOutputAt(DELAY, "out", false, false);

        assertEquals(job_in2, job_out);
    }

    @TestScript
    public void TestBalked() throws InterruptedException
    {
        entity job_in = new entity("job");
        entity job_out;
        
        // Test the balking behavior:
        // - add jobs at t=0 and t=5
        // - receive job at t=10 that matches the first job
        // - wait some time to make sure job-two is lost

        Inject("in", job_in);
        Wait(DELAY - 0.1, false);
        
        Inject("in", new entity("job-two"));
        
        job_out = WaitForOutputAround(0.1, 1.e-10, "out", false, false);

        assertEquals(job_in, job_out);
        Wait(100.0 * DELAY, false);
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // The section below is replaced by student implementations of the processor model.
    // Each student is annotated before their implementation.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Student(name = "Student#1")
    public static class proc1 extends ViewableAtomic 
    {
        protected entity job;
        protected double processing_time;
    
        public proc1(String name, double Processing_time) {
            super(name);
            addInport("in");
            addOutport("out");
            
            processing_time = Processing_time;
        }
    
        public void initialize() {
            phase = "passive";
            sigma = INFINITY;
            job = new entity("job");
            super.initialize();
        }
    
        public void deltext(double e, message x) {
            Continue(e);
                
            if (phaseIs("passive"))
                for (int i = 0; i < x.getLength(); i++)
                    if (messageOnPort(x, "in", i)) {
                        job = x.getValOnPort("in", i);
                        
                        // Student hard-coded the processing time.
                        
                        holdIn("busy", 10.0);
                    }
        }
    
        public void deltint() {
            passivate();
            job = new entity("none");
        }
    
        public void deltcon(double e, message x) {
            deltint();
            deltext(0, x);
        }
    
        public message out() {
            message m = new message();
            if (phaseIs("busy")) {
                m.add(makeContent("out", job));
            }
            return m;
        }
    }

    @Student(name = "Student#2")
    public static class proc2 extends ViewableAtomic 
    {
        protected entity job;
        protected double processing_time;
    
        public proc2(String name, double Processing_time) {
            super(name);
            addInport("in");
            addOutport("out");
            
            processing_time = Processing_time;
        }
    
        public void initialize() {
            phase = "passive";
            sigma = INFINITY;
            job = new entity("job");
            super.initialize();
        }
    
        public void deltext(double e, message x) {
            Continue(e);
                
            if (phaseIs("passive"))
                for (int i = 0; i < x.getLength(); i++)
                    if (messageOnPort(x, "in", i)) {
                        job = x.getValOnPort("in", i);
                        holdIn("busy", processing_time);
                    }
        }
    
        public void deltint() {
            passivate();
            job = new entity("none");
        }
    
        public void deltcon(double e, message x) {
            
            // Student improperly handled confluence and balked the
            // new job, though the old job has already left.
            
            deltext(e, x);
            deltint();
        }
    
        public message out() {
            message m = new message();
            if (phaseIs("busy")) {
                m.add(makeContent("out", job));
            }
            return m;
        }
    }
    
    @Student(name = "Student#3")
    public static Class<? extends devs> proc3 = proc.class;
    
    @Student(name = "Student#4")
    public static Class<? extends devs> proc4 = pipeline.class;
    /**
     * Automatically generated by the SimView program.
     * Do not edit this manually, as such changes will get overwritten.
     */
    @Override
    public void layoutForSimViewRedirect(ViewableDigraph model)
    {
        model.setPreferredSize(new Dimension(591, 148));
        ((ViewableComponent)model.withName("testframe")).setPreferredLocation(new Point(303, 54));
        ((ViewableComponent)model.withName("proc")).setPreferredLocation(new Point(24, 52));
    }
}
