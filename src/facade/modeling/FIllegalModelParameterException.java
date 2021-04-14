/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.modeling;

/** Signals that a modeling method has been invoked with an illegal or inappropriate
 * parameter (argument).
 */
public class FIllegalModelParameterException extends FModelException
{
    
    /** Constructs an FIllegalModelPortException with no detail message.
     */    
    public FIllegalModelParameterException()
    {
        super();
    }
    
    /** Constructs an FIllegalModelPortException with the
     * specified detail message. A detail message is a String
     * that describes this particular exception.
     *
     * @param message The detailed message.
     */    
    public FIllegalModelParameterException(String message)
    {
        super(message);
    }
}