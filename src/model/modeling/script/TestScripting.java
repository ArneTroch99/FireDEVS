package model.modeling.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import GenCol.ensembleBag;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;

public interface TestScripting
{

    /**
     * A scripting method for injecting a message into the system after a delay.
     * This method is useful for confluent testing as separate calls to 
     * {@link #Wait(double, boolean)} and {@link #Inject(message)} may be 
     * problematic. If a message is received at the end of <code>Wait()</code>,
     * it will be lost on the <code>Inject()</code> operation. 
     * <code>WaitAndInject()</code> performs both in a single cycle. If
     * <code>ignore</code> is set to <code>false</code>, and a message is received
     * by the test frame, an error will be thrown. If <code>true</code>, all
     * messages received by the test frame are ignored.
     * 
     * @param delay amount of time to wait before injecting a message bag
     * @param bag the message to inject into the system
     * @param ignore whether to allow message from the system to interrupt this operation.
     * @throws InterruptedException 
     */
    
    void WaitAndInject(double delay, message bag, boolean ignore)
        throws InterruptedException;

    /**
     * A convenience wrapper for {@link #WaitAndInject(double, message, boolean)}. A 
     * message is constructed from the <code>port</code>/<code>value</code> pair 
     * provided. All other parameters and behaviors are the same.
     * 
     * @param delay amount of time to wait before injecting a {@link message} bag
     * @param port the port the system should receive the {@link message} on
     * @param value the value the system should receive on the specified port
     * @param ignore whether to allow message from the system to interrupt this operation.
     * @throws InterruptedException 
     */
    default void WaitAndInject(double delay, String port, entity value,
        boolean ignore) throws InterruptedException
    {    
        message m = new message();
        m.add(new content(port, value));
        WaitAndInject(delay, m, ignore);
    }

    /**
     * Causes the test frame to wait up to a specified time. If an output from the
     * system is received by the test frame, time advances to that point and
     * returns the message received paired with the elapsed time.
     * <p>
     * If no message was received in the specified time, a <code>null</code> is returned.
     * <p>
     * The frame cannot be told to wait 0.0 time because it will immediately
     * reach {@link #deltint()}, meanwhile the black-box may take multiple simulation 
     * steps. If you believe that {@link #WaitUntilOutput(double)} in a loop would 
     * fix it, it would, but if the system passivates, the infinite loop prevents 
     * the test from failing. If you expect an output at 0.0 time, use 
     * {@link #ReadOutput()}.
     * 
     * @param e_max the maximum amount of time to wait
     * @return Trajectory, a message/elapsed time pair
     * @throws InterruptedException
     */
    Trajectory WaitUntilOutput(double e_max)
        throws InterruptedException;

    /**
     * Similar to {@link #WaitUntilOutput(double)}, but focused on a specified
     * <code>port_set</code>. If no messages are received for the specified port(s), 
     * a <code>null</code> is returned and time advances to <code>e_max</code>. If the 
     * parameter <code>allow_others</code> is <code>false</code> and the current message 
     * contains port(s) not specified in <code>port_set</code>, an assertion 
     * failure is raised.
     * 
     * @param e_max the maximum amount of time to wait
     * @param port_set a set of ports to filter 
     * @param allow_others whether the presence of other ports is a failure
     * @return Trajectory, a message/elapsed time pair
     * @throws InterruptedException
     */
    Trajectory WaitUntilOutput(double e_max, String[] port_set,
        boolean allow_others) throws InterruptedException;

    /**
     * Similar to {@link #WaitUntilOutput(double, String[], boolean)}, but is higher level 
     * and focuses on a single port. The qualifying message bag with content destined to 
     * the specified <code>port_name</code> is decomposed and the value for this content 
     * is returned.
     * 
     * Similar to {@link #GetOutput(String, boolean)}, if <code>allow_others</code> is set 
     * to false, only one port/value pair is permitted.
     *
     * @param e_max the maximum amount of time to wait
     * @param port_name the port to expect a message from
     * @param allow_others whether the presence of other ports is a failure, or if
     *    multiplicity on the allowed port can exceed one.
     * @return a single {@link entity} value.
     * @throws InterruptedException
     */
    default entity WaitUntilOutput(double e_max, String port_name,
        boolean allow_others) throws InterruptedException    
    {
        WaitUntilOutput(e_max, new String[] {port_name}, allow_others);
        return GetOutput(port_name, allow_others);
    }

    /**
     * This scripting method tells the test frame to collect all the {@link Trajectory}s
     * it captures within the specified amount of time. This method is useful for
     * testing an aggregation of information. In other words, for tests that are not
     * failing until all information from the system has been made available. An example
     * of this may be to assert the number of messages returned by the system under testing.
     * 
     * @param delay the amount of time to collect
     * @return an array of {@link Trajectory}s
     * @throws InterruptedException
     */
    default Trajectory[] CollectOutputs(double delay) throws InterruptedException
    {
        List<Trajectory> retval = new LinkedList<Trajectory>();
        double elapsed = 0.0;
        double remaining = delay;
        
        // Continuously wait until the specified time gathering all outputs received
        // by the frame.
        
        while (remaining > 0.0)
        {
            Trajectory t = WaitUntilOutput(remaining);
            
            if (t != null)
            {
                retval.add(new Trajectory(elapsed + t.getElapsedTime(), t.getMessage()));
                elapsed += t.getElapsedTime();
                remaining -= t.getElapsedTime();
            }
            else
            {
                remaining = 0.0;
            }
        }
        
        return retval.toArray(new Trajectory[retval.size()]);
    }

    /**
     * This method will block for the specified amount of simulation time--allowing
     * the system under testing to execute simulation events. The parameter 
     * <code>ignore</code> is set to <code>true</code>, any messages from the system 
     * under testing are ignored. If <code>false</code> and a message is received, 
     * an assertion is raised and the test fails.
     * 
     * @param e amount of time to wait
     * @param ignore whether to ignore all messages from the system under testing
     * @throws InterruptedException
     */
    default void Wait(double e, boolean ignore) throws InterruptedException
    {
        WaitAndInject(e, null, ignore);
    }
    
    /**
     * This method retrieves the current output from the system under testing. It
     * is important to note this method does not advance time, may be called any
     * number of times without changing the state of any models, and never relinguishes
     * control back to the simulation. This method retrieves the {@link message} passed
     * into the current execution of {@link #deltcon(double, message)} or
     * {@link #deltext(double, message)} for the test frame. If the current execution
     * is within {@link #deltint()}, the returned value is <code>null</code>. 
     * 
     * Calling this method is not typically needed because methods like 
     * {@link #ReadOutput()}, {@link #WaitUntilOutput(double)}, and so on, return this
     * message. This method has its use for testing confluent behaviors, we may want the
     * output received simultaneous to the end of a 
     * {@link #WaitAndInject(double, message, boolean)} call, but making a call to those
     * other methods would clear out the current message and hand control back to the
     * simulation.
     * 
     * @return a {@link message} or <code>null</code>
     */
    message GetOutput();

    /**
     * Get the current output from the specified port(s). If no messages exist
     * for the specified port(s), a <code>null</code> is returned. If the 
     * parameter <code>allow_others</code> is <code>false</code> and the current message 
     * contains port(s) not specified in <code>port_set</code>, an assertion 
     * failure is raised.
     * 
     * @param port_set the ports we are interested in
     * @param allow_others whether the presence of other ports in the current
     *    message is tolerated 
     * @return a {@link message} or <code>null</code>
     */
    default message GetOutput(String[] port_set, boolean allow_others)
    {  
        assertValidOutportNames(port_set);
        
        message recv_msg = GetOutput();
        
        if (recv_msg == null)
        {
            return null;
        }
        
        if (!allow_others)
        {
            assertTrue(Arrays.asList(port_set).containsAll(recv_msg.getPortNames()));
        }

        message retval = new message();
            
        recv_msg.forEach((Object c, Integer count) -> 
        {
            if(Arrays.asList(port_set).contains(((content)c).getPortName()))
            {
                retval.add(c);
            }
        });
        
        return retval.isEmpty() ? null : retval;
    }


    /**
     * Retrieve the output for the specified port. Instead of a {@link message},
     * an {@link entity} is returned. This introduces a race condition to the first
     * value encountered on this port, but if only one port/value pair is expected,
     * set <code>allow_others</code> to <code>false</code>. This will not only fail
     * when a different port is present in the current message, but raise an
     * assertion failure if the current message has more than one port/value pair for
     * the specified port.
     * 
     * @param port_name the port to expect a message from
     * @param allow_others whether the presence of other ports is a failure, or if
     *    multiplicity on the allowed port can exceed one.
     * @return a single {@link entity} value.
     */
    default entity GetOutput(String port_name, boolean allow_others)
    {
        message bag = GetOutput(new String[] {port_name}, allow_others);
        
        if (bag == null)
        {
            return null;
        }
        
        if (!allow_others)
        {

            // Fail when content is received on any other port or 
            // when multiple contents are received on the desired port.
            
            assertEquals(bag.size(), 1);
        }
        
        return bag.getValOnPort(port_name, 0);
    }

    /**
     * This method clears out the current message and waits for the next immediate message
     * to be received by the test frame. The test frame accomplishes this behavior by
     * having a non-zero sigma, but upon return will throw an {@link AssertionError} if 
     * any simulation time has passed.  
     * 
     * @return the next immediate message
     * @throws InterruptedException
     */
    default message ReadOutput() throws InterruptedException
    {
        // By having a large wait time, the programmer can better
        // diagnose late messages. Successful reads do not advance time.
        
        Trajectory t = WaitUntilOutput(10000.0);
        assertNotNull(t);
        assertEquals(0.0, t.getElapsedTime());
        return t.getMessage();
    }

    /**
     * Moves through superdense messages until the test frame encounters a message destined
     * for one of the specified ports. The parameter <code>allow_others</code>
     * is used to control whether the test frame can tolerate messages from any other port. 
     * 
     * @param port_set the ports we are interested in
     * @param allow_others whether the presence of other ports in the current
     *    message is tolerated 
     * @return the next immediate message filtered to contents on the specified ports
     * @throws InterruptedException
     */
    default message ReadOutput(String[] port_set, boolean allow_others)
        throws InterruptedException
    {
            message retval = null;
            
            assertValidOutportNames(port_set);
            
            while (retval == null)
            {
                ReadOutput();
        
                // Use GetOutput() to throw any failures.
                
                retval = GetOutput(port_set, allow_others);
            }
            
            return retval;
        }

    /**
     * Similar to {@link #ReadOutput(String[], boolean)}, but is higher level and focuses on
     * a single port. The next immediate message bag with content destined to the specified
     * <code>port_name</code> is decomposed and the value for this content is returned.
     * 
     * Similar to {@link #GetOutput(String, boolean)}, if <code>allow_others</code> is set 
     * to false, only one port/value pair is permitted.
     * 
     * @param port_name the port to expect a message from
     * @param allow_others whether the presence of other ports is a failure, or if
     *    multiplicity on the allowed port can exceed one.
     * @return a single {@link entity} value.
     * @throws InterruptedException
     */
    default entity ReadOutput(String port_name, boolean allow_others)
        throws InterruptedException
    {
        ReadOutput(new String[] {port_name}, allow_others);
        return GetOutput(port_name, allow_others);
    }

    /**
     * This method offers a lenient range to wait for messages. If no messages are received,
     * the model will have waited <code>e_max</code> time and a <code>null</code> is
     * returned. The time range is always inclusive, but if exclusivity is desired, the
     * test developer can write assertions on the returned {@link Trajectory}.
     * 
     * @param e_min the lower bound time (inclusive) the test frame should wait for the message
     * @param e_max the upper bound time (inclusive) the test frame should wait for the message
     * @param ignore_early whether to ignore all messages received before <code>e_min</code>
     * @return a {@link Trajectory} with the elapsed time from the start of this method call
     * @throws InterruptedException
     */
    default Trajectory WaitForOutputBetween(double e_min, double e_max,
        boolean ignore_early) throws InterruptedException
    {
        Trajectory retval;
        double elapsed = 0.0;
        double remaining = e_max;
        
        assertTrue(e_min >= 0.0);
        assertTrue(e_max > e_min);
        
        if (ignore_early)
        {
            Wait(e_min, true);
            remaining -= e_min;
            elapsed += e_min;
        
            // The e_min to e_max range is inclusive. Therefore we must handle the
            // possible confluency when a message was received at the end of Wait().
            
            message recv_msg = GetOutput();
            
            if (recv_msg != null)
            {
                return new Trajectory(e_min, recv_msg);
            }
        }

        retval = WaitUntilOutput(remaining);
        
        if (retval != null)
        {
            // Add the time that we have already waited.

            retval = new Trajectory(elapsed + retval.getElapsedTime(), retval.getMessage());

            if(!ignore_early && retval.getElapsedTime() < e_min)
            {
                Assertions.fail("message received on port before it was expected to.");
            }
        }
        
        return retval;
    }

    /**
     * This function follows the same logic as 
     * {@link #WaitForOutputBetween(double, double, boolean)}, but filters on the ports 
     * specified in <code>port_set</code>. If <code>ignore_early</code> is <code>false</code>,
     * all messages received before <code>e_min</code> are ignored, regardless of the value
     * set on <code>allow_others</code>. Between <code>e_min</code> and <code>e_max</code>,
     * the behavior of this method is similar to 
     * {@link #WaitUntilOutput(double, String[], boolean)}, with the same port set, except
     * that the elapsed time in the returned {@link Trajectory} is updated to be the elapsed
     * time from the start of this method call.
     *   
     * @param e_min the lower bound time (inclusive) the test frame should wait for the message
     * @param e_max the upper bound time (inclusive) the test frame should wait for the message
     * @param port_set a set of ports to filter 
     * @param ignore_early whether to ignore all messages received before <code>e_min</code>
     * @param allow_others whether the presence of other ports is a failure
     * @return a {@link Trajectory} with the elapsed time from the start of this method call
     * @throws InterruptedException
     */
    default Trajectory WaitForOutputBetween(double e_min, double e_max,
        String[] port_set, boolean ignore_early,
        boolean allow_others) throws InterruptedException
    {
        Trajectory retval;
        double elapsed = 0.0;
        double remaining = e_max;

        assertValidOutportNames(port_set);
        assertTrue(e_min >= 0.0);
        assertTrue(e_max > e_min);
        
        if (ignore_early)
        {
            Wait(e_min, true);
            remaining -= e_min;
            elapsed += e_min;
            
            message m = GetOutput(port_set, allow_others);
        
            // The e_min to e_max range is inclusive. Therefore we must handle the
            // possible confluency when a message was received at the end of Wait().
            
            if (m != null)
            {
                return new Trajectory(e_min, m);
            }
        }

        retval = WaitUntilOutput(remaining, port_set, allow_others);
        
        if (retval != null)
        {
            // Add the time that we have already waited.

            retval = new Trajectory(elapsed + retval.getElapsedTime(), retval.getMessage());

            if(!ignore_early && retval.getElapsedTime() < e_min)
            {
                Assertions.fail("message received on port before it was expected to.");
            }
        }
        
        return retval;
    }


    /**
     * 
     * Similar to {@link #WaitForOutputBetween(double, double, String[], boolean, boolean)}, but is higher level and focuses on
     * a single port. The qualifying message bag with content destined to the specified
     * <code>port_name</code> is decomposed and the value for this content is returned.
     * 
     * Similar to {@link #GetOutput(String, boolean)}, if <code>allow_others</code> is set 
     * to false, only one port/value pair is permitted.
     * 
     * @param e_min the lower bound time (inclusive) the test frame should wait for the message
     * @param e_max the upper bound time (inclusive) the test frame should wait for the message
     * @param port_name the port to expect a message from
     * @param ignore_early whether to ignore all messages received before <code>e_min</code>
     * @param allow_others whether the presence of other ports is a failure, or if
     *    multiplicity on the allowed port can exceed one.
     * @return a single {@link entity} value.
     * @throws InterruptedException
     */
    default entity WaitForOutputBetween(double e_min, double e_max,
        String port_name, boolean ignore_early,
        boolean allow_others) throws InterruptedException
    {
        WaitForOutputBetween(e_min, e_max, new String[] {port_name}, ignore_early, allow_others);
        return GetOutput(port_name, allow_others);
    }
    
    /**
     * A wrapper for {@link #WaitForOutputBetween(double, double, boolean)} with
     * a range of <code>eps - 0.5*e</code> and <code>eps + 0.5*e</code>. All other
     * behaviors are the same.
     * 
     * @param e the expected time that a message it to be received.
     * @param eps the full duration of time to expect messages (inclusive) centered around 
     *    <code>e</code>. Parameter must be greater than zero.
     * @param ignore_early whether to ignore all messages received before <code>e_min</code>
     * @return a {@link Trajectory} with the elapsed time from the start of this method call
     * @throws InterruptedException
     */
    default Trajectory WaitForOutputAround(double e, double eps,
        boolean ignore_early) throws InterruptedException
    {
        return WaitForOutputBetween(e - 0.5*eps, e + 0.5*eps, ignore_early);
    }

    /**
     * A wrapper for {@link #WaitForOutputBetween(double, double, String[], boolean, boolean)} 
     * with a range of <code>eps - 0.5*e</code> and <code>eps + 0.5*e</code>. All other
     * behaviors are the same.
     * 
     * @param e the expected time that a message it to be received.
     * @param eps the full duration of time to expect messages (inclusive) centered around 
     *    <code>e</code>. Parameter must be greater than zero.
     * @param port_set a set of ports to filter 
     * @param ignore_early whether to ignore all messages received before 
     *    <code>eps - 0.5*e</code>
     * @param allow_others whether the presence of other ports is a failure
     * @return a {@link Trajectory} with the elapsed time from the start of this method call
     * @throws InterruptedException
     */
    default Trajectory WaitForOutputAround(double e, double eps,
        String[] port_set, boolean ignore_early,
        boolean allow_others) throws InterruptedException
    {
        return WaitForOutputBetween(e - 0.5*eps, e + 0.5*eps, port_set, ignore_early, allow_others);
    }

    /**
     * A wrapper for {@link #WaitForOutputBetween(double, double, String, boolean, boolean)} 
     * with a range of <code>eps - 0.5*e</code> and <code>eps + 0.5*e</code>. All other
     * behaviors are the same.
     * 
     * @param e the expected time that a message it to be received.
     * @param eps the full duration of time to expect messages (inclusive) centered around 
     *    <code>e</code>. Parameter must be greater than zero.
     * @param port_name the port to expect a message from
     * @param ignore_early whether to ignore all messages received before 
     *    <code>eps - 0.5*e</code>
     * @param allow_others whether the presence of other ports is a failure, or if
     *    multiplicity on the allowed port can exceed one.
     * @return a single {@link entity} value.
     * @throws InterruptedException
     */
    default entity WaitForOutputAround(double e, double eps,
        String port, boolean ignore_early,
        boolean allow_others) throws InterruptedException
    {
        return WaitForOutputBetween(e - 0.5*eps, e + 0.5*eps, port, ignore_early, allow_others);
    }
    
    /**
     * This method is the most common for models with precise timing. The test frame employs
     * {@link #Wait(double, boolean)} and {@link #ReadOutput()}. Due to the superdense
     * nature of this method, not receiving a message at the expected time is an error.
     * If missing a message is to be tolerated, use 
     * {@link #WaitForOutputAround(double, double, boolean)}, which returns <code>null</code>
     * to alert the caller no messages were received.
     * 
     * @param e expected time of message to be received from the model under testing
     * @param ignore_early whether to ignore all messages received before <code>e</code>.
     * @return a {@link message} that was received at the specified time.
     * @throws InterruptedException
     */
    default message WaitForOutputAt(double e, boolean ignore_early)
        throws InterruptedException
    {
        Wait(e, ignore_early);
        
        message recv_msg = GetOutput();
        
        if (recv_msg == null)
        {
            return ReadOutput();
        }
        
        return recv_msg;
    }

    /**
     * Similar to {@link #WaitForOutputAt(double, boolean)}, but filters on the specified 
     * <code>port_set</code>. Similar to {@link #ReadOutput(String[], boolean)}, an assertion 
     * failure is thrown if a message was not received on one of the specified ports. 
     * If missing a message is to be tolerated, use 
     * {@link #WaitForOutputAround(double, double, String[], boolean, boolean), which returns 
     * <code>null</code> to alert the caller no messages were received.
     * 
     * @param e the expected time that a message it to be received.
     * @param port_set a set of ports to filter 
     * @param ignore_early whether to ignore all messages received before <code>e</code>
     * @param allow_others whether the presence of other ports is a failure
     * @return a {@link message} that was received at the specified time.
     * @throws InterruptedException
     */
    default message WaitForOutputAt(double e, String[] port_set, boolean ignore_early,
        boolean allow_others) throws InterruptedException
    {
        assertValidOutportNames(port_set);
        Wait(e, ignore_early);

        message retval = GetOutput(port_set, allow_others);
        
        if (retval == null)
        {
            return ReadOutput(port_set, allow_others);
        }
        
        return retval;
    }
    
    /**
     * Similar to {@link #WaitForOutputAt(double, String[], boolean, boolean)} but focused on
     * the specified port. The qualifying message bag with content destined to the specified
     * <code>port_name</code> is decomposed and the value for this content is returned.
     * 
     * Similar to {@link #GetOutput(String, boolean)}, if <code>allow_others</code> is set 
     * to false, only one port/value pair is permitted.
     * 
     * @param e the expected time that a message it to be received.
     * @param port_name the port to expect a message from
     * @param ignore_early whether to ignore all messages received before <code>e</code>
     * @param allow_others whether the presence of other ports is a failure, or if
     *    multiplicity on the allowed port can exceed one.
     * @return a single {@link entity} value.
     * @throws InterruptedException
     */
    default entity WaitForOutputAt(double e, String port_name, boolean ignore_early,
        boolean allow_others) throws InterruptedException
    {
        WaitForOutputAt(e, new String[] {port_name}, ignore_early, allow_others);
        return GetOutput(port_name, allow_others);
    }

    /**
     * This method immediately injects the specified {@link message} <code>bag</code>.
     * This Method is the same as calling {@link #WaitAndInject(double, message, boolean)}
     * with a time of zero.
     * 
     * @param bag
     * @throws InterruptedException
     */
    default void Inject(message bag) throws InterruptedException
    {
        WaitAndInject(0.0, bag, true);
    }
    
    /**
     * This method is a wrapper around {@link #Inject(message)} where the {@link message}
     * bag consists of only one <code>port</code>/<code>value</code> pair.
     * 
     * @param port the port the system should receive the {@link message} on
     * @param value the value the system should receive on the specified port
     * @throws InterruptedException
     */
    default void Inject(String port, entity value)
        throws InterruptedException
    {
        message m = new message();
        m.add(new content(port, value));
        Inject(m);
    }

    /**
     * This method is used for I/O of time-independent behaviors. It is a simply
     * a wrapper for {@link #Inject(message)} and {@link #ReadOutput()}. If the
     * test frame receives an output simultaneous to {@link #Inject(message)},
     * an error is thrown, as this is not an output in response to the injected
     * input.
     * 
     * @param bag
     * @return
     * @throws InterruptedException
     */
    default message InjectAndRead(message bag) throws InterruptedException
    {
        // A convenience method for testing IO Function systems.
        // This type of system only produces an immediate output
        // for an input.
        
        Inject(bag);
        assertNull(GetOutput());
        return ReadOutput();
    }


    /**
     * This method maybe used on the return value of {@link #CollectOutputs(double)}
     * when the time component is not important. The returned {@link message} contains
     * all elements from the messages passed in with <code>trjs</code>.
     * 
     * @param trjs an array of {@link Trajectory}s, likely from 
     *    {@link #CollectOutputs(double)}.
     * @return a {@link message}
     */
    static message CondenseOutputs(Trajectory[] trjs)
    {
        message m = new message();
        
        for (int i = 0; i < trjs.length; i++)
        {
            m.addAll(trjs[i].getMessage());
        }
        
        return m;
    }

    /**
     * This utility function offers an easy way to assert that two multisets or bags
     * are equivalent. This is typically used for checking the same {@link content}
     * and multiplicity is present between {@link message}s <code>a</code> and 
     * <code>b</code>.
     * <p>
     * If the bags are not equivalent, an assertion is raised.
     * 
     * @param a a bag
     * @param b another bag
     */
    static void assertEquivalent(ensembleBag<Object> a, ensembleBag<Object> b)
    {
        if (a != null && a.isEmpty())
        {
            a = null;
        }

        if (b != null && b.isEmpty())
        {
            b = null;
        }
        
        if (a != b)
        {
            assertNotNull(a);
            assertNotNull(b);
            assertEquals(a.size(), b.size());
            assertTrue(a.containsAll(b));
            assertTrue(b.containsAll(a));
        }
    }

    /**
     * Asserts that the provided array of port names are valid inputs
     * to the system under testing. In other words, these ports can be used
     * with {@link #Inject(String, entity)} and such.
     * @param port_set array of port names
     */
    void assertValidInportNames(String[] port_set);
    
    /**
     * Asserts that the provided array of port names are valid outputs
     * from the system under testing. In other words, these ports can be
     * used with {@link #GetOutput(String[], boolean)} and such.
     * @param port_set array of port names
     */
    void assertValidOutportNames(String[] port_set);
}
