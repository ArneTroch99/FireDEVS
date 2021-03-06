/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package util;

import org.eclipse.birt.chart.model.attribute.DateFormatSpecifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A central repository for values concerning what kinds of log messages
 * get output from the GenDevs code.
 *
 * @author  Jeff Mather
 */
public class Logging
{
    private static final Path errorPath = Paths.get("error.txt");
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


    /**
     * Logging levels that may be chosen from for the level variable below.
     * More may be added as necessary.
     */
    static public final int full = 100, none = 0, errorsOnly = 10, inputViolations = 20,
                            debug = 5, info = 4, warning = 3, error = 2, fatal = 1;

    /**
     * The current logging level used throughout the GenDevs code.  This
     * is meant to control what kinds (if any) of logging messages get
     * displayed on stdout.
     */
    static public final int level = fatal;

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

        // Write error or fatal messages to error.txt
        if (ifLevelAtLeast <= Logging.error) {
            try {
                Files.write(errorPath, (dtf.format(LocalDateTime.now()) + ":\n" + message + "\n\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}