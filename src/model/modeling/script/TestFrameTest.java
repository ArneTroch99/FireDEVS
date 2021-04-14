package model.modeling.script;

import static model.modeling.script.TestFixture.assertHasInputPort;
import static model.modeling.script.TestScripting.assertEquivalent;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import GenCol.entity;
import GenCol.intEnt;
import model.modeling.IOBasicDevs;
import model.modeling.MessageInterface;
import model.modeling.content;
import model.modeling.devs;
import model.modeling.message;
import model.simulation.atomicSimulator;

class TestFrameTest
{
    static final double DELAY1 = 3.1;
    static final double DELAY2 = 0.5;
    static final double DELAY_MIN = 0.9;
    static final double DELAY_MAX = 9.2;
    static final double NO_DELAY = 0.0;
    
    static class TestScriptSimulator extends atomicSimulator
    {
        public TestScriptSimulator(IOBasicDevs model)
        {
            super(model);
        }

        @Override
        public void simInject(double e, MessageInterface<Object> m)
        {
            assertTrue(e <= myModel.ta());
            
            for (Object o : m)
            {
                String port_name = ((content) o).getPortName();
                assertHasInputPort((devs) myModel, port_name);
            }
            
            super.simInject(e, m);
        }
        
        public void step()
        {
            super.simInject(myModel.ta(), new message());
        }
    }
    
    static abstract class TestScriptMethods extends TestFrame
    {
        protected final boolean script_should_fail;
        
        private static Method getTest()
        {
            try
            {
                return TestScriptMethods.class.getMethod("DoTest");
            }
            catch (NoSuchMethodException | SecurityException e)
            {
                e.printStackTrace();
            }
            
            return null;
        }
        
        public TestScriptMethods(boolean script_should_fail)
        {
            super("testframe", getTest());
            this.script_should_fail = script_should_fail;
            
            addInport("out1");
            addInport("out2");
            addInport("out3");
            addOutport("in1");
            addOutport("in2");
            addOutport("in3");
        }
        
        @Override
        public TestFixture createFixture(String name)
        {
            return new TestFixture(name, this);
        }
        
        void Run()
        {
            TestScriptSimulator sim = new TestScriptSimulator(this);
            
            sim.initialize();
            sim.step();

            // Save off the old thread.
            
            Thread old_thread = test_thread;
            
            DoSim(sim);
            
            assertEquals(script_should_fail, test_failed != null);
            
            // Re-initialize to generate a new test thread.
            
            sim.initialize();
            sim.step();
            
            // The old thread is no longer active, assert that it properly
            // exits.
            
            assertNotEquals(old_thread, test_thread);
            
            // Clean up the new thread. We have no plans on running it.
            
            test_thread.interrupt();
        }

        public void assertState(double sigma, message bag, String phase, double delta)
        {
            assertEquals(phase, getPhase());
            assertEquals(sigma, getSigma(), delta);
            assertEquivalent(bag, out());
        }
        
        public void assertState(double sigma, message bag, String phase)
        {
            assertState(sigma, bag, phase, 0.0);
        }
        
        @TestScript(canSequence = false)
        public abstract void DoTest() throws InterruptedException;
        
        public abstract void DoSim(TestScriptSimulator sim);
    }

    
    static message createMessage(boolean unique, String... ports)
    {
        final message m = new message();
        
        for (int i = 0; i < ports.length; i++)
        {
            m.add(new content(ports[i], unique ? new intEnt(i) : new entity("test")));
        }
        
        return m;
    }

    @Test
    void testBagMultiplicityEquals()
    {
        content c1 = new content("out1", new entity("test"));
        content c2 = new content("out1", new entity("test"));

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        
        message m = new message();
        m.add(c1);
        m.add(c2);
        
        assertEquals(2, m.numberOf(c1));
    }
    
    @Test
    void testAssertEquivalent()
    {
        content c1 = new content("out1", new entity("test"));
        content c2 = new content("out1", new entity("test"));
        message m = new message();
        m.add(c1);
        m.add(c2);
        
        // Test wrong multiplicity.
        
        assertThrows(AssertionError.class, ()->
        {
            message m2 = new message();
            m2.add(c1);
            assertEquivalent(m, m2);
        });
        
        // Test correct multiplicity

        assertDoesNotThrow(()->
        {
            message m2 = new message();
            m2.add(c1);
            m2.add(c1);
            assertEquivalent(m, m2);
        });
        
    }

    @Test
    void testWaitAndInjectDoubleMessageFalseFails()
    { 
        final message m = createMessage(true, "in1");
        
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(DELAY1, m, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, m, false);
                assertState(DELAY1, m, "wait-inj");

                sim.simInject(DELAY1 - 0.1, createMessage(true, "out1"));
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectDoubleMessageFalsePassesConfluent()
    { 
        final message m = createMessage(true, "in2", "in1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(DELAY1, m, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, m, false);
                
                assertState(DELAY1, m, "wait-inj");

                sim.simInject(DELAY1, createMessage(true, "out1"));
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectDoubleMessageFalsePasses()
    { 
        final message m = createMessage(true, "in1", "in2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(DELAY1, m, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, m, false);
                assertState(DELAY1, m, "wait-inj");

                sim.step();
            }
        }.Run();
    }
    
    @Test
    void testWaitAndInjectDoubleNullFalseFails()
    { 
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(DELAY1, null, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, null, false);
                assertState(DELAY1, createMessage(true), "wait");

                sim.simInject(NO_DELAY, createMessage(true, "out2"));
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectDoubleNullFalsePassesConfluent()
    { 
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(DELAY1, null, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, null, false);
                assertState(DELAY1, createMessage(true), "wait");

                sim.simInject(DELAY1, createMessage(true, "out2"));
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectDoubleNullFalsePasses()
    { 
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                WaitAndInject(DELAY1, null, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, null, false);
                assertState(DELAY1, createMessage(true), "wait");

                sim.step();
            }
        }.Run();
    }


    @Test
    void testWaitAndInjectZeroMessageFalsePassesConfluent()
    { 
        final message m = createMessage(true, "in2", "in1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(NO_DELAY, m, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(NO_DELAY, m, false);
                assertState(NO_DELAY, m, "imm-inj");

                sim.simInject(NO_DELAY, createMessage(true, "out1"));
            }
        }.Run();
    }
    
    @Test 
    void testWaitAndInjectZeroMessageFalsePasses()
    { 
        final message m = createMessage(true, "in1", "in1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(NO_DELAY, m, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(NO_DELAY, m, false);
                assertState(NO_DELAY, m, "imm-inj");

                sim.step();
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectZeroNullFalsePassesConfluent()
    { 
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(NO_DELAY, null, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(NO_DELAY, null, false);
                assertState(NO_DELAY, createMessage(true), "imm");

                sim.simInject(NO_DELAY, createMessage(true, "out2"));
            }
        }.Run();
    }
    
    @Test
    void testWaitAndInjectZeroNullFalsePasses()
    { 
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                WaitAndInject(NO_DELAY, null, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(NO_DELAY, null, false);
                assertState(NO_DELAY, createMessage(true), "imm");

                sim.step();
            }
        }.Run();
    }
    


    @Test
    void testWaitAndInjectDoubleMessageTruePassesIgnored()
    { 
        final message m = createMessage(true, "in1", "in2", "in1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(DELAY1, m, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, m, true);
                assertState(DELAY1, m, "wait-inj-ignore");

                sim.simInject(DELAY1 * 0.4, createMessage(true, "out1"));
                assertState(DELAY1 * 0.6, m, "wait-inj-ignore", 1.e-10);
            }
        }.Run();
    }



    @Test
    void testWaitAndInjectDoubleMessageTruePassesConfluent()
    { 
        final message m = createMessage(true, "in2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(DELAY1, m, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, m, true);
                assertState(DELAY1, m, "wait-inj-ignore");

                sim.simInject(DELAY1, createMessage(true, "out1"));
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectDoubleMessageTruePasses()
    { 
        final message m = createMessage(true, "in2", "in2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(DELAY1, m, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, m, true);
                assertState(DELAY1, m, "wait-inj-ignore");

                sim.step();
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectDoubleNullTruePassesIgnored()
    { 
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(DELAY1, null, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, null, true);
                assertState(DELAY1, createMessage(true), "wait-ignore");

                sim.simInject(0.1, createMessage(true, "out2"));
                assertState(DELAY1 - 0.1, createMessage(true), "wait-ignore", 1.e-10);
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectDoubleNullTruePassesConfluent()
    { 
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(DELAY1, null, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, null, true);
                assertState(DELAY1, createMessage(true), "wait-ignore");

                sim.simInject(DELAY1, createMessage(true, "out2"));
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectDoubleNullTruePasses()
    { 
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                WaitAndInject(DELAY1, null, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(DELAY1, null, true);
                assertState(DELAY1, createMessage(true), "wait-ignore");

                sim.step();
            }
        }.Run();
    }


    @Test
    void testWaitAndInjectZeroMessageTruePassesConfluent()
    { 
        final message m = createMessage(true, "in1", "in1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(NO_DELAY, m, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(NO_DELAY, m, true);
                assertState(NO_DELAY, m, "imm-inj-ignore");

                sim.simInject(NO_DELAY, createMessage(true, "out1"));
            }
        }.Run();
    }
    
    @Test 
    void testWaitAndInjectZeroMessageTruePasses()
    { 
        final message m = createMessage(true, "in2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {   
                WaitAndInject(NO_DELAY, m, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(NO_DELAY, m, true);
                assertState(NO_DELAY, m, "imm-inj-ignore");

                sim.step();
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectZeroNullTruePassesConfluent()
    { 
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException
            {
                WaitAndInject(NO_DELAY, null, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(NO_DELAY, null, true);
                assertState(NO_DELAY, createMessage(true), "imm-ignore");

                sim.simInject(NO_DELAY, createMessage(true, "out2"));
            }
        }.Run();
    }
    
    @Test
    void testWaitAndInjectZeroNullTruePasses()
    { 
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                WaitAndInject(NO_DELAY, null, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitAndInject(NO_DELAY, null, true);
                assertState(NO_DELAY, createMessage(true), "imm-ignore");

                sim.step();
            }
        }.Run();
    }

    @Test
    void testWaitAndInjectDoubleStringEntityBoolean()
    {
        final content c = new content("in1", new entity("test"));
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                WaitAndInject(DELAY1, "in1", (entity) c.getValue(), true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                message m = new message();
                m.add(c);
                
                // Script is inside WaitAndInject(DELAY1, "in1", ent, false);
                assertState(DELAY1, m, "wait-inj-ignore");
            }
        }.Run();
    }

    @Test
    void testSetStateNormal()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                message m1 = createMessage(true, "in1", "in1", "in2", "in1", "in2");
                message m2 = createMessage(true, "in1");
                
                SetState(3.14, m1, "abc");
                assertState(3.14, m1, "abc");
                
                SetState(1.59, m2, "def");
                assertState(1.59, m2, "def");
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
            }
        }.Run();
    }
    
    @Test
    void testSetStateFailBadPorts()
    {
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                message m2 = createMessage(true, "out1");
                SetState(NO_DELAY, m2, "abc");
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
            }
        }.Run();
    }

    @Test
    void testSetStateFailNegativeTime()
    {
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                SetState(-0.14, null, "abc");
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
            }
        }.Run();
    }

    @Test
    void testPassSequence()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                // Do nothing, pass.
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // <-- Test script starts and exits
                
                message m = new message();
                m.add(makeContent("internal-out", new entity("pass")));

                // <-- Inside Inject("internal-out", new entity("pass"));
                
                assertState(0.0, m, "imm-inj-ignore");
                
                sim.step();

                assertState(INFINITY, null, "passed");
            }
        }.Run();
    }
    
    @Test
    void testFailSequence()
    {
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                assertFalse(true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // <-- Test script starts, but immediately fails.

                assertEquals("imm-inj-ignore", getPhase());
                assertEquals(0.0, getSigma());
                
                // Rather than testing the whole fail message (which may depend on 
                // external libraries), we test that our prefix of "fail - " is there.
                
                message m = out();
                assertEquals(1, m.size());
                assertEquals("internal-out", m.read(0).getPortName());
                
                entity e = (entity) m.read(0).getValue();
                assertTrue(e.getName().startsWith("fail - "));   
                
                sim.step();
                             
                assertState(INFINITY, null, "failed");
            }
        }.Run();
    }

    @Test
    void testReturnToDeltFunc()
    {
        final message m_ext = createMessage(true, "out2", "out1");
        final message m_con = createMessage(true, "out1");
        
        new TestScriptMethods(false) 
        {
            private int seq = 0;
            
            @Override
            public void initialize() 
            {
                seq = 0;
                super.initialize();
            };
            
            @Override
            public void DoTest() throws InterruptedException 
            {
                assertEquals(0, seq);
                seq++;
                
                holdIn("def", NO_DELAY);
                ReturnToDeltFunc();
                
                // <-- inside sim.step();

                assertEquals(2, seq);
                seq++;
                
                assertEquals("deltint", delt_func);
                assertNull(recv_msg);
                
                holdIn("ghi", 1.1);
                ReturnToDeltFunc();
                
                // <-- inside sim.simInject(1.0, m_ext);

                assertEquals(4, seq);
                seq++;
                
                assertEquals("deltext", delt_func);
                assertNotNull(recv_msg);
                assertEquivalent(recv_msg, m_ext);

                holdIn("jkl", 0.5);
                ReturnToDeltFunc();
                
                // <-- simInject(0.5, m_con);

                assertEquals(6, seq);
                seq++;
                
                assertEquals("deltcon", delt_func);
                assertNotNull(recv_msg);
                assertEquivalent(recv_msg, m_con);                
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                assertEquals(1, seq);
                seq++;
                
                sim.step();

                assertEquals(3, seq);
                seq++;
                
                sim.simInject(1.0, m_ext);

                assertEquals(5, seq);
                seq++;
                
                sim.simInject(0.5, m_con);

                // <-- Test script exits
                
                assertEquals(7, seq);
            }
        }.Run();
    }

    @Test
    void testWaitUntilOutputDoublePassesNullReturn()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1);
                
                assertNull(t);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1);
                assertState(DELAY1, null, "wait-out");
                
                sim.step();
            }
        }.Run();
    }

    @Test
    void testWaitUntilOutputDoublePassesConfluent()
    {
        final message m = createMessage(true, "out1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1);
                
                assertEquals(DELAY1, t.getElapsedTime());
                assertEquivalent(m, t.getMessage());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1);
                assertState(DELAY1, null, "wait-out");

                sim.simInject(DELAY1, m);
            }
        }.Run();
    }


    @Test
    void testWaitUntilOutputDoublePasses()
    {
        final message m = createMessage(true, "out2", "out2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1);
                
                assertEquals(DELAY1 - 0.1, t.getElapsedTime(), 1.e-10);
                assertEquivalent(m, t.getMessage());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1);
                assertState(DELAY1, null, "wait-out");
                
                sim.simInject(DELAY1 - 0.1, m);
            }
        }.Run();
    }

    @Test
    void testWaitUntilOutputZeroFails()
    {
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                WaitUntilOutput(NO_DELAY);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
            }
        }.Run();
    }

    @Test
    void testWaitUntilOutputDoubleEmptyArrayFalsePassesNullReturn()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1, new String[] {}, false);
                
                assertNull(t);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                assertState(DELAY1, null, "wait-out");

                sim.step();
            }
        }.Run();        
    }

    @Test
    void testWaitUntilOutputDoubleEmptyArrayTruePassesNullReturn()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1, new String[] {}, true);
                
                assertNull(t);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                assertState(DELAY1, null, "wait-out");
                
                sim.simInject(DELAY1 - 0.1, createMessage(true, "out1"));
                sim.step();
            }
        }.Run();        
    }
    
    @Test
    void testWaitUntilOutputDoubleStringArrayFalsePasses()
    {
        final message m = createMessage(true, "out1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1, new String[] {"out1"}, false);
                
                assertEquals(DELAY1 - 0.1, t.getElapsedTime());
                assertEquivalent(m, t.getMessage());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                assertState(DELAY1, null, "wait-out");

                sim.simInject(DELAY1 - 0.1, m);
            }
        }.Run();
    }

    @Test
    void testWaitUntilOutputDoubleStringArrayTruePasses()
    {
        final message m1 = createMessage(true, "out1");
        final message m2 = createMessage(true, "out2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1, new String[] {"out2"}, true);
                
                assertEquals(DELAY1 - 0.1, t.getElapsedTime());
                assertEquivalent(m2, t.getMessage());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                assertState(DELAY1, null, "wait-out");

                sim.simInject(DELAY1 - 0.2, m1);
                sim.simInject(0.1, m2);
            }
        }.Run();        
    }
    
    @Test
    void testWaitUntilOutputDoubleStringArrayFalsePassesConfluent()
    {
        final message m = createMessage(true, "out1", "out2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1, new String[] {"out1", "out2"}, false);
                
                assertEquals(DELAY1, t.getElapsedTime());
                assertEquivalent(m, t.getMessage());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                assertState(DELAY1, null, "wait-out");

                sim.simInject(DELAY1, m);
            }
        }.Run();        
    }

    @Test
    void testWaitUntilOutputDoubleStringArrayTruePassesConfluent()
    {
        final message m = createMessage(true, "out1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1, new String[] {"out1"}, false);
                
                assertEquals(DELAY1, t.getElapsedTime());
                assertEquivalent(m, t.getMessage());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                assertState(DELAY1, null, "wait-out");

                sim.simInject(DELAY1, m);
            }
        }.Run();                
    }
    
    @Test
    void testWaitUntilOutputDoubleStringArrayFalsePassesNullReturn()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1, new String[] {"out1"}, false);
                
                assertNull(t);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                assertState(DELAY1, null, "wait-out");
                
                sim.step();
            }
        }.Run();        
    }

    @Test
    void testWaitUntilOutputDoubleStringArrayTruePassesNullReturn()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1, new String[] {"out1"}, true);
                
                assertNull(t);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                assertState(DELAY1, null, "wait-out");
                
                sim.simInject(DELAY1 - 0.1, createMessage(false, "out2", "out2"));
                sim.step();
            }
        }.Run();       
    }

    @Test
    void testWaitUntilOutputDoubleEmptyArrayFalseFailsInterrupted()
    {
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                WaitUntilOutput(DELAY1, new String[] {}, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {}, false);
                
                assertState(DELAY1, null, "wait-out");
                
                sim.simInject(DELAY1 - 0.1, createMessage(false, "out2", "out1"));
            }
        }.Run();               
    }

    @Test
    void testWaitUntilOutputDoubleEmptyArrayTruePassesIgnored()
    {
        final message m = createMessage(false, "out1", "out2", "out1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitUntilOutput(DELAY1, new String[] {}, true);

                assertEquals(DELAY1 - 0.1, t.getElapsedTime());
                assertEquivalent(m, t.getMessage());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {}, false);
                
                assertState(DELAY1, null, "wait-out");
                
                sim.simInject(DELAY1 - 0.2, createMessage(false, "out2", "out1"));
                sim.simInject(0.1, m);
            }
        }.Run();               
    }
    
    @Test
    void testWaitUntilOutputDoubleStringArrayFalseFailsInterrupted()
    {
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                WaitUntilOutput(DELAY1, new String[] {"out1"}, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                
                assertState(DELAY1, null, "wait-out");
                
                sim.simInject(DELAY1 - 0.1, createMessage(false, "out2", "out2"));
            }
        }.Run();               
    }

    @Test
    void testWaitUntilOutputDoubleStringArrayTruePassesIgnored()
    {
        final message m = createMessage(false, "out1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
               Trajectory t = WaitUntilOutput(DELAY1, new String[] {"out1"}, true);

               assertEquals(DELAY1, t.getElapsedTime());
               assertEquivalent(m, t.getMessage());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                
                assertState(DELAY1, null, "wait-out");
                
                sim.simInject(DELAY1 - 0.1, createMessage(false, "out2", "out2"));
                sim.simInject(0.1, m);
            }
        }.Run();               
    }
    
    @Test
    void testWaitUntilOutputDoubleStringArrayBooleanFailsBadPorts()
    {
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
               WaitUntilOutput(DELAY1, new String[] {"in1"}, true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
            }
        }.Run();         
    }
    
    @Test
    void testWaitUntilOutputDoubleStringBoolean()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
               entity e = WaitUntilOutput(DELAY1, "out1", true);
               
               assertNotNull(e);
               assertEquals(e.getName(), "abc");
               
               e = WaitUntilOutput(DELAY2, "out2", false);
               
               assertNull(e);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside WaitUntilOutput(DELAY1, {"out1"}, false);
                
                assertState(DELAY1, null, "wait-out");
                
                sim.simInject(DELAY1 - 0.1, createMessage(false, "out2"));
                sim.simInject(0.1, "out1", new entity("abc"));
                sim.step();
            }
        }.Run(); 
    }
    
    @Test
    void testCollectOutputs()
    {
        message m1 = createMessage(true, "out1");
        message m2 = createMessage(true, "out1", "out2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory[] t = CollectOutputs(DELAY1);

                assertEquals(2, t.length);
                assertEquals(DELAY1 - .1, t[0].getElapsedTime());
                assertEquivalent(m1, t[0].getMessage());
                assertEquals(DELAY1, t[1].getElapsedTime());
                assertEquivalent(m2, t[1].getMessage());

                t = CollectOutputs(DELAY2);
                assertEquals(1, t.length);
                assertEquals(0.1, t[0].getElapsedTime(), 1.e-10);
                assertEquivalent(m2, t[0].getMessage());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside CollectOutputs(DELAY1);
                assertState(DELAY1, null, "wait-out");
                sim.simInject(DELAY1 - .1, m1);
                
                assertState(.1, null, "wait-out", 1.e-10);
                sim.simInject(0.1, m2);
                
                assertState(DELAY2, null, "wait-out");
                sim.simInject(0.1, m2);
                
                assertState(DELAY2 - .1, null, "wait-out", 1.e-10);
                sim.step();
            }
        }.Run();
    }

    @Test
    void testWaitPasses()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Wait(NO_DELAY, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside Wait(NO_DELAY, false);
                assertState(NO_DELAY, null, "imm");
                
                sim.step();
            }
        }.Run();
    }
    
    @Test
    void testWaitFails()
    {
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Wait(DELAY1, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside Wait(DELAY1, false);
                assertState(DELAY1, null, "wait");
                
                sim.simInject(NO_DELAY, createMessage(true, "out1"));
            }
        }.Run();
    }

    @Test
    void testGetOutputBagMultiplicityPasses()
    {
        final message m = createMessage(false, "out1", "out1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Wait(DELAY1, true);
                assertEquivalent(m, GetOutput());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside Wait(DELAY1, false);
                assertState(DELAY1, null, "wait-ignore");
                
                sim.simInject(DELAY1, m);
            }
        }.Run();
    }
    
    @Test
    void testGetOutputPasses()
    {
        final message m = createMessage(true, "out1", "out1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Wait(DELAY1, true);
                assertEquivalent(m, GetOutput());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside Wait(DELAY1, false);
                assertState(DELAY1, null, "wait-ignore");
                
                sim.simInject(DELAY1, m);
            }
        }.Run();
    }


    @Test
    void testGetOutputPassesNullMessage()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Wait(DELAY1, false);
                assertEquivalent(null, GetOutput());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Script is inside Wait(DELAY1, false);
                assertState(DELAY1, null, "wait");
                
                sim.step();
            }
        }.Run();
    }
    
    @Test
    void testGetOutputStringFalseFails()
    {
        final message m = createMessage(true, "out1", "out2");
        
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Wait(DELAY1, true);
                GetOutput("out1", false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Fails because when allow_others is false, any content 
                // destined for a different port is a failure.
                
                sim.simInject(DELAY1, m);
            }
        }.Run();
    }
    
    @Test
    void testGetOutputStringFalsePasses()
    {
        final message m = createMessage(true, "out1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Wait(DELAY1, true);
                entity e = GetOutput("out1", false);
                
                assertEquals(m.getValOnPort("out1", 0), e);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY1, m);
            }
        }.Run();
    }

    
    @Test
    void testGetOutputStringFalsePassesNullReturn()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Wait(DELAY1, true);
                entity e = GetOutput("out1", false);
                
                assertNull(e);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.step();
            }
        }.Run();
    }    
    
    
    
    @Test
    void testGetOutputStringTruePasses()
    {
        message m = createMessage(true, "out2", "out2");
        content c = new content("out1", new entity("test"));
        m.add(c);
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Wait(DELAY1, true);
                entity e = GetOutput("out1", true);
                
                assertEquals(c.getValue(), e);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY1, m);
            }
        }.Run();
    }    

    @Test
    void testGetOutputStringTrueTruePassesNoMessage()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Wait(DELAY1, true);
                entity e = GetOutput("out2", true);
                
                assertNull(e);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.step();
            }
        }.Run();
    }
    
    @Test
    void testReadOutputFails()
    {
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                ReadOutput();
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Message injected too late.
                
                sim.simInject(0.1, createMessage(true, "out1", "out2", "out2"));
            }
        }.Run();
    }

    @Test
    void testReadOutputPasses()
    {
        final message m = createMessage(true, "out2", "out2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                assertEquivalent(m, ReadOutput());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(0.0, m);
            }
        }.Run();
    }

    @Test
    void testReadOutputStringFalseFails()
    {   
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                ReadOutput("out1", false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(0.0, createMessage(true, "out2", "out1"));
            }
        }.Run();
    }


    @Test
    void testReadOutputStringFalsePasses()
    {
        final message m = createMessage(true, "out2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                assertEquals(m.getValOnPort("out2", 0), ReadOutput("out2", false));
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(0.0, m);
            }
        }.Run();
    }


    @Test
    void testReadOutputStringTrueFails()
    {   
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                ReadOutput("out1", true);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(0.0, createMessage(true, "out2"));
                sim.step();
            }
        }.Run();
    }


    @Test
    void testReadOutputStringTruePasses()
    {
        final message m = createMessage(true, "out1", "out2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                assertNotNull(ReadOutput("out2", true));
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(0.0, createMessage(true, "out1"));
                sim.simInject(0.0, m);
            }
        }.Run();
    }
    
    @Test
    void testInjectAndReadPass()
    {
        final message m_in = createMessage(true, "out1", "out2");
        final message m_out = createMessage(true, "in1", "in1");

        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                message m = InjectAndRead(m_out);
                assertEquivalent(m_in, m);
            }

            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                assertState(0.0, m_out, "imm-inj-ignore");
                sim.step();
                sim.simInject(0.0, m_in);
            }
        }.Run();
    }
    
    @Test
    void testInjectAndReadFail()
    {
        final message m_in = createMessage(true, "out1", "out2");
        final message m_out = createMessage(true, "in1", "in1");
        
        // A output cannot be received simultaneous with the injected input.
        // This would indicate the output is no associated with the input.

        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                InjectAndRead(m_out);
            }

            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                assertState(0.0, m_out, "imm-inj-ignore");
                sim.simInject(0.0, m_in);
            }
        }.Run();
    }
    
    abstract class TestScriptWaitForOutputBetween extends TestScriptMethods
    {
        private boolean ignore_early;
        private Trajectory expected;

        public TestScriptWaitForOutputBetween(boolean script_should_fail, boolean ignore_early, Trajectory expected)
        {
            super(script_should_fail);
            
            this.ignore_early = ignore_early;
            this.expected = expected;
        }

        @Override
        public void DoTest() throws InterruptedException 
        {
            Trajectory t = WaitForOutputBetween(DELAY_MIN, DELAY_MAX, ignore_early);
            
            if (!script_should_fail)
            {
                if (expected == null)
                {
                    assertNull(t);
                }
                else
                {
                    assertEquals(expected.getElapsedTime(), t.getElapsedTime(), 1.e-10);
                    assertEquivalent(expected.getMessage(), t.getMessage());
                }
            }
        }   
    }
      
    @Test
    void testWaitForOutputBetweenDoubleDoubleFalse()
    {
        message m = createMessage(true, "out2", "out2");
        
        // Boundary tests

        new TestScriptWaitForOutputBetween(true, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Too early
                
                sim.simInject(DELAY_MIN - .1, m);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetween(false, false, new Trajectory(DELAY_MIN, m)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m);
            }
        }.Run();

        new TestScriptWaitForOutputBetween(false, false, new Trajectory(DELAY_MIN + .1, m)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN + .1, m);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetween(false, false, new Trajectory(DELAY_MAX, m)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MAX, m);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetween(false, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Not received by max time
                sim.step();
            }
        }.Run();
    }
    
    @Test
    void testWaitForOutputBetweenDoubleDoubleTrue()
    {
        message m_ign = createMessage(true, "out2", "out2");
        message m_exp = createMessage(true, "out1", "out1");
        
        // Boundary tests

        new TestScriptWaitForOutputBetween(false, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Too early
                
                sim.simInject(DELAY_MIN - .1, m_ign);
                sim.step();
                sim.step();
            }
        }.Run();
        
        new TestScriptWaitForOutputBetween(false, true, new Trajectory(DELAY_MIN, m_exp)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m_exp);
            }
        }.Run();

        new TestScriptWaitForOutputBetween(false, true, new Trajectory(DELAY_MIN + .1, m_exp)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN - .1, m_ign);
                sim.step();
                sim.simInject(.1, m_exp);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetween(false, true, new Trajectory(DELAY_MAX, m_exp)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.step();
                sim.simInject(DELAY_MAX - DELAY_MIN, m_exp);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetween(false, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.step();
                sim.step();
            }
        }.Run();
    }

    abstract class TestScriptWaitForOutputBetweenOnPorts extends TestScriptMethods
    {
        private String[] ports;
        private boolean ignore_early;
        private boolean allow_others;
        private Trajectory expected;

        public TestScriptWaitForOutputBetweenOnPorts(boolean script_should_fail, String[] ports, boolean ignore_early, boolean allow_others, Trajectory expected)
        {
            super(script_should_fail);

            this.ports = ports;
            this.ignore_early = ignore_early;
            this.allow_others = allow_others;
            this.expected = expected;
        }

        @Override
        public void DoTest() throws InterruptedException 
        {
            Trajectory t = WaitForOutputBetween(DELAY_MIN, DELAY_MAX, ports, ignore_early, allow_others);
            
            if (!script_should_fail)
            {
                if (expected == null)
                {
                    assertNull(t);
                }
                else
                {
                    assertEquals(expected.getElapsedTime(), t.getElapsedTime(), 1.e-10);
                    assertEquivalent(expected.getMessage(), t.getMessage());
                }
            }
        }   
    }

    @Test
    void testWaitForOutputBetweenDoubleDoubleStringArrayFalseFalse()
    {
        message m = createMessage(true, "out2", "out2");
        String[] port1 = {"out1"};
        String[] port2 = {"out2"};
        String[] ports12 = {"out1", "out2"};
        
        // Boundary tests

        new TestScriptWaitForOutputBetweenOnPorts(true, port2, false, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Too early
                
                sim.simInject(DELAY_MIN - .1, m);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(true, port1, false, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Too early
                
                sim.simInject(DELAY_MIN - .1, m);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, ports12, false, false, new Trajectory(DELAY_MIN, m)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(true, port1, false, false, new Trajectory(DELAY_MIN, m)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port2, false, false, new Trajectory(DELAY_MIN + .1, m)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN + .1, m);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetweenOnPorts(true, port1, false, false, new Trajectory(DELAY_MIN + .1, m)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN + .1, m);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, ports12, false, false, new Trajectory(DELAY_MAX, m)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MAX, m);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(true, port1, false, false, new Trajectory(DELAY_MAX, m)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MAX, m);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetweenOnPorts(false, port2, false, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Not received by max time
                sim.step();
            }
        }.Run();
    }

    @Test
    void testWaitForOutputBetweenDoubleDoubleStringArrayFalseTrue()
    {
        message m2 = createMessage(false, "out2");
        message m12 = createMessage(false, "out2", "out1");
        String[] port1 = {"out1"};
        String[] port2 = {"out2"};
        String[] ports12 = {"out1", "out2"};
        
        // Boundary tests
        new TestScriptWaitForOutputBetweenOnPorts(true, port2, false, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Too early
                
                sim.simInject(DELAY_MIN - .1, m12);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port1, false, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Too early
                
                sim.simInject(DELAY_MIN - .1, m2);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, ports12, false, true, new Trajectory(DELAY_MIN, m2)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m2);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port1, false, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m2);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port2, false, true, new Trajectory(DELAY_MIN + .1, m2)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN + .1, m12);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetweenOnPorts(false, port1, false, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN + .1, m2);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, ports12, false, true, new Trajectory(DELAY_MAX, m12)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MAX, m12);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port1, false, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MAX, m2);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetweenOnPorts(false, port2, false, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Not received by max time
                sim.step();
            }
        }.Run();
    }

    @Test
    void testWaitForOutputBetweenDoubleDoubleStringArrayTrueFalse()
    {
        String[] port1 = {"out1"};
        String[] port2 = {"out2"};

        message m2 = createMessage(true, "out2", "out2");
        message m1 = createMessage(true, "out1", "out1");
        
        // Boundary tests

        new TestScriptWaitForOutputBetweenOnPorts(false, port1, true, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Too early
                
                sim.simInject(DELAY_MIN - .1, m2);
                sim.step();
                sim.step();
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port2, true, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Too early
                
                sim.simInject(DELAY_MIN - .1, m2);
                sim.step();
                sim.step();
            }
        }.Run();
        
        new TestScriptWaitForOutputBetweenOnPorts(false, port2, true, false, new Trajectory(DELAY_MIN, m2)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m2);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(true, port1, true, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m2);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port1, true, false, new Trajectory(DELAY_MIN + .1, m1)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN - .1, m2);
                sim.step();
                sim.simInject(.1, m1);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(true, port1, true, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN - .1, m1);
                sim.step();
                sim.simInject(.1, m2);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetweenOnPorts(false, port1, true, false, new Trajectory(DELAY_MAX, m1)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.step();
                sim.simInject(DELAY_MAX - DELAY_MIN, m1);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(true, port1, true, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.step();
                sim.simInject(DELAY_MAX - DELAY_MIN, m2);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetweenOnPorts(false, port2, true, false, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.step();
                sim.step();
            }
        }.Run();        
    }
    
    @Test
    void testWaitForOutputBetweenDoubleDoubleStringArrayTrueTrue()
    {
        String[] port1 = {"out1"};
        String[] port2 = {"out2"};

        message m2 = createMessage(true, "out2", "out2");
        message m1 = createMessage(true, "out1", "out1");
        
        // Boundary tests

        new TestScriptWaitForOutputBetweenOnPorts(false, port1, true, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Too early
                
                sim.simInject(DELAY_MIN - .1, m2);
                sim.step();
                sim.step();
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port2, true, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                // Too early
                
                sim.simInject(DELAY_MIN - .1, m2);
                sim.step();
                sim.step();
            }
        }.Run();
        
        new TestScriptWaitForOutputBetweenOnPorts(false, port1, true, true, new Trajectory(DELAY_MIN, m1)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m1);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port2, true, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m1);
                sim.step();
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port1, true, true, new Trajectory(DELAY_MIN + .1, m1)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN - .1, m2);
                sim.step();
                sim.simInject(.1, m1);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port2, true, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN - .1, m2);
                sim.step();
                sim.simInject(.1, m1);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetweenOnPorts(false, port1, true, true, new Trajectory(DELAY_MAX, m1)) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.step();
                sim.simInject(DELAY_MAX - DELAY_MIN, m1);
            }
        }.Run();

        new TestScriptWaitForOutputBetweenOnPorts(false, port2, true, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.step();
                sim.simInject(DELAY_MAX - DELAY_MIN, m1);
            }
        }.Run();
        
        new TestScriptWaitForOutputBetweenOnPorts(false, port2, true, true, null) 
        {   
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.step();
                sim.step();
            }
        }.Run();        
    }

    @Test
    void testCondenseOutputs()
    {
        entity e1 = new entity("job1");
        entity e2 = new entity("job2");
        entity e3 = new entity("job3");
        
        message m_res = TestScripting.CondenseOutputs(new Trajectory[]{});
        assertEquivalent(new message(), m_res);
        
        Trajectory trj[] = new Trajectory[2];
        message m = new message();
        message m_exp = new message();

        m.add(new content("out1", e1));
        m.add(new content("out2", e2));
        m_exp.addAll(m);
        
        trj[0] = new Trajectory(0.5, m);
        m = new message();

        m.add(new content("out2", e3));
        m.add(new content("out1", e2));
        m_exp.addAll(m);
        
        trj[1] = new Trajectory(1.5, m);

        m_res = TestScripting.CondenseOutputs(trj);
        assertEquivalent(m_exp, m_res);
    }

    @Test
    void testWaitForOutputAroundDoubleDoubleStringBooleanBooleanPasses()
    {
        final message m = createMessage(true, "out1", "out2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                entity e = WaitForOutputBetween(DELAY_MIN, DELAY_MAX, "out1", true, true);
                
                assertNotNull(e);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MIN, m);
            }
        }.Run();
    }

    @Test
    void testWaitForOutputAroundDoubleDoubleStringBooleanBooleanFails()
    {
        new TestScriptMethods(true) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                WaitForOutputBetween(DELAY_MIN, DELAY_MAX, "out2", false, false);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                sim.simInject(DELAY_MAX, createMessage(false, "out1", "out2"));
            }
        }.Run();
    }


    @Test
    void testWaitForOutputAroundDoubleDoubleBoolean()
    {
        final message m = createMessage(true, "out1", "out2", "out1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitForOutputAround(DELAY1, .1, false);
                assertNotNull(t);
                assertEquals(DELAY1, t.getElapsedTime());
                assertEquivalent(m,  t.getMessage());
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                assertState(DELAY1 + 0.05, null, "wait-out", 1.e-10);
                sim.simInject(DELAY1, m);
            }
        }.Run();
    }

    @Test
    void testWaitForOutputAroundDoubleDoubleStringArrayBooleanBoolean()
    {
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                Trajectory t = WaitForOutputAround(DELAY1, .1, new String[] {"out1", "out2"}, false, true);
                assertNull(t);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                assertState(DELAY1 + 0.05, null, "wait-out", 1.e-10);
                sim.step();
            }
        }.Run();
    }
    
    @Test
    void testWaitForOutputAroundDoubleDoubleStringBooleanBoolean()
    {
        final message m = createMessage(true, "out1");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                entity e = WaitForOutputAround(DELAY1, .1, "out1", true, false);
                
                assertNotNull(e);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                assertState(DELAY1 - 0.05, null, "wait-ignore", 1.e-10);
                sim.step();
                
                assertState(0.1, null, "wait-out", 1.e-10);
                sim.simInject(getSigma(), m);
            }
        }.Run();
    }
  
    @Test
    void testWaitForOutputAtDoubleBoolean()
    {
        final message m = createMessage(true, "out1", "out2");
        
        new TestScriptMethods(false) 
        {
            @Override
            public void DoTest() throws InterruptedException 
            {
                message t = WaitForOutputAt(DELAY1, false);
                assertNotNull(t);
                assertEquivalent(m, t);
                
                t = WaitForOutputAt(DELAY2, true);
                assertNotNull(t);
                assertEquivalent(m, t);
            }
            
            @Override
            public void DoSim(TestScriptSimulator sim)
            {
                
                // Test confluent receive with end of DELAY1.
                
                assertState(DELAY1, null, "wait");
                sim.simInject(DELAY1, m);

                // Test interrupt on ignore
                
                assertState(DELAY2, null, "wait-ignore");
                sim.simInject(0.1, createMessage(false, "out1"));

                // Don't have the message confluent with the end of DELAY2,
                // but have it immediately after.

                sim.step();
                sim.simInject(0.0, m);
            }
        }.Run();
    }
    
    @BeforeEach
    void setUp() throws Exception
    {
    }

    @AfterEach
    void tearDown() throws Exception
    {
    }
}
