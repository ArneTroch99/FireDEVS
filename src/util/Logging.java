/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package util;

/**
 * A central repository for values concerning what kinds of log messages
 * get output from the GenDevs code.
 *
 * @author  Jeff Mather
 */
public class Logging
{
    /**
     * Logging levels that may be chosen from for the level variable below.
     * More may be added as necessary.
     */
    static public final int full = 100, none = 0, errorsOnly = 10,
        inputViolations = 20;

    /**
     * The current logging level used throughout the GenDevs code.  This
     * is meant to control what kinds (if any) of logging messages get
     * displayed on stdout.
     */
    static public final int level = errorsOnly;

    /**
     * Writes the given message to stdout.
     *
     * @param   message             The message to log.
     */
    static public void log(String message) {System.out.println(message);}

    /**
     * Writes the given message to stdout if the current logging level is
     * at least as high as the one given.
     *
     * @param   message             The message to log.
     * @param   ifLevelAtLeast      At least how high the current logging
     *                              level must be for this message to be logged.
     */
    static public void log(String message, int ifLevelAtLeast)
    {
        if (level >= ifLevelAtLeast) System.out.println(message);
    }
}