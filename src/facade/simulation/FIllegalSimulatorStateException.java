/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.simulation;


/** Signals that a simulator method has been invoked at an illegal or inappropriate time.
 * In other words, the current Simulation is not in an
 * appropriate state for the requested operation.
 */
public class FIllegalSimulatorStateException extends FSimulatorException
{
    
    /** Constructs an FIllegalSimulatorStateException with no detail message.
     *
     */    
    public FIllegalSimulatorStateException()
    {
        super();
    }
    
    /** Constructs an FIllegalSimulatorStateException with the
     * specified detail message. A detail message is a String
     * that describes this particular exception.
     *
     * @param message The detailed message.
     */    
    public FIllegalSimulatorStateException(String message)
    {
        super(message);
    }
}