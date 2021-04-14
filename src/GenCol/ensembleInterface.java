/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;

import java.util.function.Function;

public interface ensembleInterface<T> extends ensembleCollection<T>, ensembleLogic
{
    public T findIf(Function<T, Boolean> f);
}