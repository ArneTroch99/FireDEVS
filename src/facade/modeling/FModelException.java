/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.modeling;

public class FModelException extends RuntimeException
{
    /** Constructs a new FModelException with null as its
     * detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to
     * Throwable.initCause(java.lang.Throwable).
     */    
    public FModelException()
    {
        super();
    }
    
    /** Constructs a new FModelException with the specified detail message. 
     * The cause is not initialized, and may subsequently be initialized 
     * by a call to Throwable.initCause(java.lang.Throwable). 
     *
     *@param message  the detail message. The detail message is saved for 
     * later retrieval by the Throwable.getMessage() method.
     */
    public FModelException(String message)
    {
        super(message);
    }
         
    /** Constructs a new FModelException with the specified detail message and cause.
     * Note that the detail message associated with cause is not automatically
     * incorporated in this exception's detail message.
     *
     * @param message the detail message (which is saved for later
     * retrieval by the Throwable.getMessage() method).
     *
     * @param cause the cause (which is saved for later retrieval
     * by the Throwable.getCause() method).
     * (A null value is permitted, and indicates
     * that the cause is nonexistent or unknown.)
     */    
    public FModelException(String message, Throwable cause) 
    {
        super(message,cause);
    }
   
   /** Constructs a new FModelException with the specified cause
    * and a detail message of (cause==null ? null :
    * cause.toString()) (which typically contains the class
    * and detail message of cause). This constructor is useful
    * for exceptions that are little more than wrappers for
    * other throwables (for example, PrivilegedActionException).
    *
    * @param cause the cause (which is saved for later retrieval
    * by the Throwable.getCause() method).
    * (A null value is permitted, and indicates
    * that the cause is nonexistent or unknown.)
    */   
    public FModelException(Throwable cause)
    {
        super(cause);
    }
}