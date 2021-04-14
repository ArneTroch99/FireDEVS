package model.modeling.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;

/**
 * Trajectory is a data structure that ties a message and an
 * elapsed time. 
 * 
 * @author Matthew McLaughlin
 */
public class Trajectory
{
    /**
     * The message that was received.
     */
    private message msg;
    
    /**
     * The amount of simulation time that passed from the start of
     * a scripting method to the time it returns. 
     */
    private double t;
    
    /**
     * Construct a {@link Trajectory} given the time and message.
     * @param t the elapsed simulation time
     * @param msg a {@link message}
     */
    public Trajectory(double t, message msg)
    {
        this.msg = msg;
        this.t = t;
    }

    /**
     * 
     * @param t
     * @param pairs
     */
    public Trajectory(double t, Object... pairs)
    {
        assertEquals(0, pairs.length % 2);
        
        msg = new message();
        
        for (int i = 0; i < pairs.length; i += 2)
        {
            assertTrue(pairs[i] instanceof String);
            assertTrue(pairs[i + 1] instanceof entity);
            
            msg.add(new content((String) pairs[i], (entity) pairs[i + 1]));
        }
        
        this.t = t;
    }
    
    /**
     * retrieve the message bag portion of a trajectory.
     * 
     * @return a {@link message}
     */
    public message getMessage()
    {
        return msg;
    }
    
    /**
     * retrieve the elapsed time portion of a trajectory.
     * @return the elapsed time
     */
    public double getElapsedTime()
    {
        return t;
    }
    
    @Override
    public String toString()
    {
        return  "msg: " + msg.toString() + " time: "+ Double.toString(t);
    }
    
    public void print()
    {
        System.out.println(toString());
    }
}
