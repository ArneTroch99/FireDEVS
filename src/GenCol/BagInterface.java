/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

/*
/*  no correspondence in Java collections
*/

package GenCol;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import util.TriConsumer;

interface BagInterface<T>
{
    public int numberOf(T key);

    public void removeAll(T key);

    public Set<T> bag2Set();
    
    public void forEach(BiConsumer<T, Integer> f);
    
    public T findIf(Function<T, Boolean> f);

    public void enumerate(TriConsumer<Integer, T, Integer> f);
}
