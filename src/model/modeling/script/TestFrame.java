package model.modeling.script;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.awt.Color;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import GenCol.entity;
import model.modeling.DevsInterface;
import model.modeling.message;
import model.modeling.ports;
import model.modeling.state;
import model.simulation.coupledCoordinator;
import util.NestLock;
import util.SortedArrayList;
import view.modeling.ViewableAtomic;

/**
 * TestFrame is a DEVS-Scripting model for black-box testing
 * DEVS models. Depending on the purpose or the number of 
 * components for the system under testing, derive from 
 * {@link SimpleTestFrame} (for unit testing atomic or coupled models), 
 * {@link CompoundTestFrame} (for integration, system, and performance
 * testing), or {@link HomeworkTestFrame} (for testing/comparing
 * multiple implementations).
 * <p>
 * The {@link TestFrame} is responsible for creating a {@link TestFixture}, 
 * which is a coupled model that contains the test frame and
 * the model(s) under testing. Multiple tests can be created
 * using the same test fixture by using the @TestScript
 * annotation before a method with a <code>+():void</code> 
 * signature.
 * <p>
 * Scripting methods are added for the tester's convenience.
 * Methods that are available for use in test scripts have
 * a capital first letter and a protected scope.
 * 
 * @author Matthew McLaughlin
 */
public abstract class TestFrame extends ViewableAtomic implements TestScripting
{
    @Target({METHOD})
    @Retention(RUNTIME)
    public @interface TestScript
    {
        /**
         * Tests can be put in a sequence with other tests. Set this attribute
         * to false if the model:
         *  - uses a different initial state than other sequential tests
         *  - does not or cannot return to its initial state
         * 
         * It is up to the tester to determine what the initial state should be
         * and to return the state of the system back to the initial state
         * before closing out the {@link TestScript}.
         * 
         * @return whether to run the test in sequence with others.
         */
        public boolean canSequence() default false;
        
        /**
         * In simulations that run perpetually, a failure can prevent proper
         * teardown. By restricting the number of simulation steps, we can prevent 
         * infinite loops. This value is only applicable to JUnit tests created
         * in {@link TestFrame#getTestCases(Class, boolean)}. Running in DEVS-Suite will
         * be indefinite.
         * 
         * @return max number of simulation steps.
         */
        public int maxSimSteps() default Integer.MAX_VALUE;
    }

    /**
     * The synchronization lock between the test script and the test frame.
     */
    protected NestLock nest_lock;
    
    /**
     * Used to pass the message received from the system under testing to the test
     * script. It is assigned from the message sent to deltext() and deltcon().
     */
    protected message recv_msg;
    
    /**
     * Holds the message that should be sent to the system under testing.
     */
    @state(log = state.DEFAULT_CHECKED)
    protected message send_msg;
    
    /**
     * The thread running the test script.
     */
    protected Thread test_thread;
    
    /**
     * The specific {@link TestScript} that is to be run. Assigned by the constructor.
     */
    final protected Method test_case;
    
    /**
     * A list of all methods with a <code>@{@link TestScript}(canSequence = true)</code> 
     * annotation.
     */
    final protected Map<String, Method> seq_scripts;
    
    /**
     * If we capture the assertion, then this variable will be set to true. If we
     * do not capture assertions, the debugger, DEVS-Suite, or JUnit will, and this
     * member variable will not be set.
     */
    protected Throwable test_failed;
    
    /**
     * Used for logging purposes. This allows the test script to know which
     * block of code is embedded in which state transition method.
     */
    protected String delt_func;
    
    /**
     * A single method interface for constructing a test fixture with 
     * {@link TestFrame#getTestCases(Class, boolean, ConstructFixture)}. It will be
     * loaded with the current test frame, the specified method, and all other
     * relevant models.
     */
    public interface ConstructFixture
    {
        public TestFixture build(Method m);
    }
    
    /**
     * Find all test scripts provided in the specified test frame class. A
     * dynamic test is generated for each test to run individually in a
     * simulation. Other dynamic tests include:
     * <ul>
     * <li>structural testing of models/couplings. This ensures all EIC, EOC, 
     *     and IC couplings do not violate DEVS I/O specification.</li>
     * <li>running a sequence of <code>@{@link TestScript}(canSequence = true)</code>
     *     tests. This ensures that the sequenceable tests pass and that the system 
     *     returns to its initial state after each test script.</li>
     * </ul>
     * <p>
     * In regression testing, setup and teardown can become so expensive that
     * full regression testing for code changes are no longer feasible. Setting
     * <code>condense_seq</code> to <code>true</code> will run all sequenceable tests
     * without setup/teardown. The advantage is the speed of regression testing, but
     * the disadvantage is that only one failure will be reported, and subsequent tests
     * will not run. Therefore, upon a failing test frame, set <code>condense_seq</code>
     * to <code>false</code> to decompose the test when debugging.
     * <p>
     * Note that sequenceable tests will run indefinitely, if we are testing a
     * perpetual system that fails to teardown, we may enter an infinite loop.
     * 
     * @param klass the TestFrame containing one or more {@link TestScript} methods.
     * @param condense_seq Condense sequenceable tests in one instance and run
     *    non-sequenceable tests as normal.
     * @param build_fixture a {@link ConstructFixture} for controlling how the 
     *    {@link TestFixture} is built.
     * @return a stream of dynamic tests for JUnit to analyze
     */
    public static Stream<DynamicTest> getTestCases(Class<? extends TestFrame> klass, boolean condense_seq, ConstructFixture build_fixture)
    {
        Method[] methods = klass.getMethods();
        List<Method> scripts = new LinkedList<Method>();
        int num_seqenceable = 0;
        
        for (Method m : methods)
        {
            TestScript ann = m.getAnnotation(TestScript.class);
            
            if (ann != null)
            {
                scripts.add(m);
                
                if (ann.canSequence())
                {
                    num_seqenceable++;
                }
            }
        }
        
        Stream.Builder<DynamicTest> retval = Stream.builder();
        
        // Perform the structured test first. Issues with EIC/EOC/IC
        // couplings could be the reason future tests fail.

        retval.add(dynamicTest("Testing Structured Design", () -> {
            TestFixture fixture = build_fixture.build(null);
            
            TestFixture.structuredTests(fixture);
        }));
        
        // Then test each test script as an independent test.
        
        for (Method m : scripts)
        {
            TestScript ann = m.getAnnotation(TestScript.class);
            
            if (condense_seq && ann.canSequence())
            {
                
                // Skip these tests to save time on regression testing.
                // These are tested later.
                
                continue;
            }
            
            retval.add(dynamicTest("Testing: " + 
                ((Method)m).getName(), () -> {

                    int runtime = ann.maxSimSteps();
                    
                    // Setup

                    TestFixture fixture = build_fixture.build(m);
                    coupledCoordinator coord = new coupledCoordinator(fixture);
                    coord.initialize();

                    // Test

                    coord.simulate(runtime);

                    assertFalse(coord.nextTN() < DevsInterface.INFINITY, 
                        "Simulation timed-out after " + runtime + " steps");
                    
                    Throwable e = fixture.getTestFrame().test_failed;
                    
                    if (e != null)
                    {
                        throw e;
                    }
                }));
        }
        
        // Finally test sequenceability. Individual tests will not normally
        // test that the system has returned to its initial state. A failure
        // at this level with no preceding failures will likely result from
        // failing to return the model to the state the next test script expects.
        
        if (num_seqenceable > 1)
        {
            retval.add(dynamicTest("Testing Sequenceablity", () -> {

                // Setup

                TestFixture fixture = build_fixture.build(null);
                coupledCoordinator coord = new coupledCoordinator(fixture);
                coord.initialize();

                // Test

                coord.simulate(Integer.MAX_VALUE);
                
                Throwable e = fixture.getTestFrame().test_failed;
                
                if (e != null)
                {
                    throw e;
                }
            }));
        }
        
        return retval.build();
    }
    
    /**
     * A wrapper for {@link #getTestCases(Class, boolean, ConstructFixture)} using
     * a default implementation. This method should be used in conjunction with
     * JUnit {@link TestFactory}.
     * 
     * @param klass the {@link TestFrame} to run testing
     * @param condense_seq whether to condense sequenceable tests
     * @return a stream of dynamic tests for JUnit to analyze
     */
    public static Stream<DynamicTest> getTestCases(Class<? extends TestFrame> klass, boolean condense_seq)
    {
        return getTestCases(klass, condense_seq, (Method m) -> {
            try
            { 
                Constructor<? extends TestFrame> constructor = klass.getConstructor(String.class, Method.class);
                TestFrame frame = constructor.newInstance("testframe", m);
                
                return frame.createFixture("testfixture");
            }
            catch (
                InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e)
            {
                e.printStackTrace();
            }
            
            return null;
        });
    }
    
    /**
     * Sets up a test frame with the specified name for testing the specified
     * {@link TestScript}.
     *  
     * @param name the name of the model.
     * @param run_case the test script to run, or <code>null</code> if all 
     *    sequential tests should run.
     */
    protected TestFrame(String name, Method run_case)
    {
        super(name);

        nest_lock = null;
        send_msg = null;
        recv_msg = null;
        test_thread = null;
        test_case = run_case;
        seq_scripts = new HashMap<String, Method>();

        addOutport("internal-out");
        setBackgroundColor(Color.decode("#EDEDED"));
    }
    
    /**
     * Create a TestFixture for this test frame. This fixture should be used
     * as the root model of a simulation.
     * 
     * @return a TestFixture model containing the model(s) under testing and
     *  this test frame.
     */
    public abstract TestFixture createFixture(String name);
    
    /**
     * This method is used to determine whether a test thread has been
     * abandoned. This feature is required if models can be reset and
     * re-initialized. The old thread should know if it has been replaced
     * so that it can gracefully end. To accomplish this, the test frame 
     * signals an interrupt on the current test thread and generates a 
     * new test thread and script running from the top. The 
     * InterruptedException on the old test thread is immediately thrown 
     * if the thread was waiting in the nest lock, but it should check
     * the interrupted status before updating state information. 
     * 
     * @throws InterruptedException 
     */
    protected synchronized void ensureThreadActive() throws InterruptedException
    {
        long tid = Thread.currentThread().getId();
        
        if (Thread.interrupted() || test_thread == null || test_thread.getId() != tid)
        {
            throw new InterruptedException();
        }
    }
    
    /**
     * This function should be called inside deltint() instead of
     * initialize(). Thread creation is deferred in case there are 
     * multiple calls to initialize() before a model is allowed to 
     * run. This is the case when models are loaded into DEVS-Suite. 
     * To avoid this issue, thread creation initialization is 
     * deferred to run time.
     */
    private synchronized void ensureThreadIsRunning()
    {
        if (test_thread != null)
        {
            return;
        }

        test_thread = new Thread()
        {
            private void invokeScript() throws InterruptedException
            {
                try
                {
                    Object[] args = {};
                    
                    if (test_case != null)
                    {
                        test_case.setAccessible(true);
                        test_case.invoke(TestFrame.this, args);
                    }
                    else
                    {
                        for (Method m : seq_scripts.values())
                        {
                            Inject("internal-out", new entity(m.getName()));
                            m.setAccessible(true);
                            m.invoke(TestFrame.this, args);
                        }
                    }     
                }
                catch (InvocationTargetException e)
                {
                    Throwable cause = e.getCause();

                    if (cause instanceof InterruptedException)
                    {
                        
                        // The old thread was interrupted. Exit without
                        // changing any state variables.
                        
                        throw (InterruptedException) cause;
                    }
                    else
                    {
                        test_failed = cause;
                    }
                    
                    if (cause instanceof Error)
                    {
                        throw (Error) cause;
                    }
                }
                catch (IllegalAccessException | IllegalArgumentException e)
                {
                    test_failed = e;
                }
            }
            
            private void runSink() throws InterruptedException
            {
                String test_msg = "pass";
                
                System.out.println("Script: " + delt_func + "() activating script (phase = " + phase + ", sigma = " + getSigma() + ")");
                
                try
                {
                    invokeScript();
                }
                catch (InterruptedException e)
                {

                    // This inactive thread cannot perform any more assertions. It must
                    // exit as gracefully as possible.

                    throw e;
                }
                catch (Error e)
                {
                    test_msg = "fail - " + e.getClass().getSimpleName();
                    test_failed = e;

                    if (e.getMessage() != null)
                    {
                        test_msg += ": " + e.getMessage();
                    }
                }             

                // Test script is finished, produce an output.

                Inject("internal-out", new entity(test_msg));
                SetState(INFINITY, null, test_failed != null ? "failed" : "passed");

                System.out.println("Script: test script exiting (phase = " + phase + 
                    ", sigma = " + getSigma() + ") returns to " + delt_func + "()");
            }

            @Override
            public void run()
            {
                
                // Since we have a unique locking mechanism.
                
                try
                { 
                    nest_lock.innerSectionEnter();
                    
                    try
                    {
                        runSink();
                    }
                    catch (InterruptedException e)
                    {
                        return;
                    }
                    finally 
                    {
                        nest_lock.innerSectionExit();
                    }
                }
                catch (InterruptedException e)
                {
                    return;
                }
            }
        };
        
        test_thread.start();
    }
    

    /**
     * This method serves as the lowest-level scripting method. It returns 
     * control back to {@link #deltint()}, {@link #deltext(double, message)}, 
     * or {@link #deltcon(double, message)} in the main simulation thread.
     * <p>
     * If the thread is interrupted, the nest lock will catch it and
     * throw an {@link InterruptedException}.
     * 
     * @throws InterruptedException 
     */
    protected void ReturnToDeltFunc() throws InterruptedException
    {
        System.out.println("Script: test script (phase = " + phase + 
            ", sigma = " + getSigma() + ") returns to " + delt_func + "()"); 
        
        nest_lock.leave();
        
        System.out.println("Script: " + delt_func + "() enters test script (phase = " + 
            phase + ", sigma = " + getSigma() + ")");    
    }
    
    /**
     * This synchronized method assigns all of the state variables for the test 
     * frame before returning to the main simulation thread. It needs to address
     * synchronized access from a possible old thread and a current thread.
     * 
     * @param sigma the time to advance before an internal state transition
     * @param bag the message to send before internal or confluent state transitions
     * @param phase a name for the current phase. 
     * @throws InterruptedException 
     */
    protected synchronized void SetState(double sigma, message bag, String phase) throws InterruptedException
    {
        ensureThreadActive();

        send_msg.clear();
        
        if (bag != null)
        {
            assertTrue(getOutportNames().containsAll(bag.getPortNames()));
            send_msg.addAll(bag);
        }
        
        assertTrue(sigma >= 0.0, "time advance must be >= 0.0");

        holdIn(phase, sigma);
        recv_msg = null;
    }

    @Override
    public void WaitAndInject(double delay, message bag, boolean ignore) throws InterruptedException
    {   
        List<String> strings = new LinkedList<String>();
        
        if (delay > 0.0)
        {
            strings.add("wait");
        }
        else
        {
            strings.add("imm");
        }
        
        if (bag != null)
        {
            strings.add("inj");
        }
        
        if (ignore)
        {
            strings.add("ignore");
        }

        SetState(delay, bag, String.join("-", strings));
        
        do
        {
            ensureThreadActive();

            recv_msg = null;
            
            ReturnToDeltFunc();
        }
        while (ignore && sigma > 0);
        
        if (sigma > 0)
        {
            throw new AssertionError("passive wait interrupted by external event."); 
        }
    }
    
    @Override
    public Trajectory WaitUntilOutput(double e_max) throws InterruptedException
    {  
        assertTrue(e_max > 0.0);
        
        SetState(e_max, null, "wait-out");
        ReturnToDeltFunc();
        
        if (recv_msg == null)
        {
            return null;
        }
        
        return new Trajectory(e_max - getSigma(), recv_msg);
    }

    @Override
    public Trajectory WaitUntilOutput(double e_max, String[] port_set, boolean allow_others) throws InterruptedException
    {
        
        // This implementation focuses on an output for the specified
        // port. In some test cases, the user only focuses on a single
        // response to evaluate and ignores all other stimuli.
    
        assertValidOutportNames(port_set);
        
        message retval = null;
        double elapsed = 0.0;
            
        while(retval == null && elapsed < e_max)
        {
            WaitUntilOutput(e_max - elapsed);
            retval = GetOutput(port_set, allow_others);
            elapsed = e_max - getSigma();
        }
        
        return (retval != null ? new Trajectory(elapsed, retval) : null);
    }

    @Override
    public message GetOutput()
    {
       return recv_msg;
    }
    
    @Override
    public void assertValidInportNames(String[] port_set)
    {
        
        // From the perspective of the system under testing, the input ports
        // are the outputs of the test frame.
        
        assertTrue(getOutportNames().containsAll(Arrays.asList(port_set)));
    }
    
    @Override
    public void assertValidOutportNames(String[] port_set)
    {

        // From the perspective of the system under testing, the output ports
        // are the inputs of the test frame.
        
        assertTrue(getInportNames().containsAll(Arrays.asList(port_set)));
    }
    
    @Override
    public void initialize()
    {
        if (test_thread != null)
        {
            test_thread.interrupt();
            
            try
            {
                test_thread.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                return;
            }
        }
        
        phase = "initializing";
        sigma = 0.0;
        
        nest_lock = new NestLock();
        send_msg = new message();
        recv_msg = null;
        test_thread = null;
        test_failed = null;
        seq_scripts.clear();

        Class<? extends TestFrame> klass = getClass();
        
        for (Method m : klass.getMethods())
        {
            TestScript ann = m.getAnnotation(TestScript.class);
            
            if (ann != null && ann.canSequence())
            {
                seq_scripts.put(m.getName(), m);
            }
        }
        
        super.initialize();
    }

    @Override
    public void deltext(double e, message x)
    {
        Continue(e);

        recv_msg = x;
        delt_func = "deltext";
        
        if (!phaseIs("passed") && !phaseIs("failed"))
        {
            try
            {
                nest_lock.enter();
            }
            catch (InterruptedException e1)
            {
                return;
            }
        }
    }

    @Override
    public void deltint() 
    {   
        ensureThreadIsRunning();

        assertFalse(phaseIs("passed") || phaseIs("failed"));
        setSigma(0.0);
        send_msg.clear();
        delt_func = "deltint";
        
        try
        {
            nest_lock.enter();
        }
        catch (InterruptedException e)
        {
            return;
        }
    }

    @Override
    public void deltcon(double e, message x) 
    {
        Continue(e);
        
        // Handling confluence is not as simple as sequencing
        // deltint() and deltext() because the message should
        // be made available at the end of waiting, before
        // notifying the test thread. This also allows
        // the test frame to make one cycle to the test thread
        // without forcing a sequence. 

        assertFalse(phaseIs("passed") || phaseIs("failed"));
        send_msg.clear();
        recv_msg = x;
        delt_func = "deltcon";
        
        try
        {
            nest_lock.enter();
        }
        catch (InterruptedException e1)
        {
            return;
        }
    }

    @Override
    public message out() 
    {
        return send_msg;
    }
    
    /**
     * This utility method is used to extract port names from the specified 
     * <code>portSet</code>.
     * 
     * @param portSet 
     * @return a sorted {@link List} of {@link String}s
     */
    public static List<String> extractPortNames(ports portSet)
    {
        return SortedArrayList.MakeSortedStringArrayList(portSet.getNameList());
    }  
}
