/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package util;

/**
 *
 * @author  Jeff Mather
 */
public class Util 
{
    /**
     * Prints a stack trace to stdout.
     */
    static public void printStackTrace() {try {throw new Exception();} catch (Exception ex) {ex.printStackTrace();}}

    /**
     * Puts the current thread to sleep.
     *
     * @param   millis      How long to sleep (in ms).
     */
    static public void sleep(long millis) {try {Thread.sleep(millis);} catch (InterruptedException ex) {}}

    /**
     * Puts the given thread to sleep.
     *
     * @param   thread      The thread to put to sleep.
     * @param   millis      How long to sleep (in ms).
     */
    static public void sleep(Thread thread, long millis) {try {thread.sleep(millis);} catch (InterruptedException ex) {}}

    /**
     * A shorthand for System.currentTimeMillis().
     */ 
    static public long time() {return System.currentTimeMillis();}
}
