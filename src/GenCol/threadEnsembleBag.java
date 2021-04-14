/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;

import java.util.Collection;

public class threadEnsembleBag<T> extends ensembleBag<T> implements ensembleInterface<T>
{

    private ensemble<T> el;

    public threadEnsembleBag()
    {
        el = threadEnsembleWrapper.make(this);
    }

    public threadEnsembleBag(Collection<T> c)
    {
        addAll(c);
        el = threadEnsembleWrapper.make(this);
    }

    @Override
    public void tellAll(String MethodNm, Class<?>[] classes, Object[] args)
    {
        el.tellAll(MethodNm, classes, args);
    }

    @Override
    public void tellAll(String MethodNm)
    {
        Class<?>[] classes = {};
        Object[] args = {};
        el.tellAll(MethodNm, classes, args);
    }

    @Override
    public void AskAll(ensembleInterface<T> result, String MethodNm, Class<?>[] classes, Object[] args)
    {
        el.AskAll(result, MethodNm, classes, args);
    }

    @Override
    public void which(ensembleInterface<T> result, String MethodNm, Class<?>[] classes, Object[] args)
    {
        // MethodNm method must return this if condition is true and null
        // otherwise
        el.AskAll(result, MethodNm, classes, args);
    }

    @Override
    public T whichOne(String MethodNm, Class<?>[] classes, Object[] args)
    {
        return el.whichOne(MethodNm, classes, args);
    }

    @Override
    public boolean none(String MethodNm, Class<?>[] classes, Object[] args)
    {
        return el.none(MethodNm, classes, args);
    }

    @Override
    public boolean all(String MethodNm, Class<?>[] classes, Object[] args)
    {
        return el.all(MethodNm, classes, args);
    }

    @Override
    public void print()
    {
        el.print();
    }
}


