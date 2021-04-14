/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package util;

import java.awt.*;
import java.awt.event.*;

/**
 * Utility methods involving window components.
 *
 * @author  Jeff Mather
 */
public class WindowUtil
{
    /**
     * Centers the given window on the screen.
     *
     * @param   window      The window to center.
     */
    static public void centerWindow(Window window)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = window.getSize();
        window.setLocation(screenSize.width / 2 - windowSize.width / 2,
            screenSize.height / 2 - windowSize.height / 2);
    }
}