/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package util;

import java.awt.Point;

public class PointUtil
{
    static public void translate(Point a, Point b)
    {
        a.translate(b.x, b.y);
    }

    static public void negativeTranslate(Point a, Point b)
    {
        a.translate(-b.x, -b.y);
    }
}