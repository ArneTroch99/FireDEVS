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

public class threadEnsembleWrapper<T>
{
    private threadEnsembleWrapper()
    {
    } // no instantiation

    public static <T> ensemble<T> make(Collection<T> c)
    {
        return threadEnsemble.make(c);
    }
}
