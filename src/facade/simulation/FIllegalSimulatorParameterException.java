/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.simulation;


/** Signals that a simulator method has been invoked with an illegal or inappropriate
 * parameter (argument).
 */
public class FIllegalSimulatorParameterException extends FSimulatorException
{
    
    /** Constructs an FIllegalSimulatorParameterException with no detail message.
     *
     */    
    public FIllegalSimulatorParameterException()
    {
        super();
    }
    
    /** Constructs an FIllegalSimulatorParameterException with the
     * specified detail message. A detail message is a String
     * that describes this particular exception.
     *
     * @param message The detailed message.
     */    
    public FIllegalSimulatorParameterException(String message)
    {
        super(message);
    }
}