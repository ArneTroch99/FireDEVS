/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;

import java.util.Collection;

interface ensembleCollection<T> extends ensembleBasic<T>, Collection<T>
{
    public void print();

    public void wrapAll(ensembleInterface<Object> Result, Class<?> cl);

    public ensembleInterface<T> copy(ensembleInterface<?> ce);
}

