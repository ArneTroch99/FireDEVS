/** Represents a TestInput that can be injected into 
 * the specified port.
 *
 * @author Ranjit Singh
 */

package model.modeling;

import GenCol.entity;

public class TestInput
{
    /**The name of the port this input can be injected into*/
    private String portName;
    
    /**The value that can be injected*/
    private entity value;
    
    /**The amount of simulation units to wait before injecting the value.*/
    private double timeToWait;
    
    /** Creates a new TestInput object with the associated Port name,
     * value and time to wait.
     */    
    public TestInput(String portName, entity value, double timeToWait)
    {
        this.portName   = portName;
        this.value      = value;
        this.timeToWait = timeToWait;
    }
    
    /** Returns the name of the port that input can be injected into.
     * @return The name of the port that input can be injected into.
     */    
    public String getPortName()         {return portName;}
    /** Returns the associated value that can be injected.
     * @return The associated value that can be injected.
     */    
    public entity getValue()            {return value;}
    /** Returns the amount of simulation units to wait before injecting.
     * @return The amount of simulation units to wait before injecting.
     */    
    public double getTimeToWait()          {return timeToWait;}
}