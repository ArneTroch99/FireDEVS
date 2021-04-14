/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
/*
/* wrapper class to create threaded ensemble classes
/*
*/

package GenCol;

import java.util.Collection;

public class ensembleWrapper<T>
{
    private ensembleWrapper()
    {
    } // no instantiation

    public static <T> ensemble<T> make(Collection<T> c)
    {
        return ensemble.make(c);
    }
}
