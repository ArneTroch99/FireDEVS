/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

/**
* This is the basic class that has the full implementation of ensemble
* capabilities
 * Note: Restrictions:
 *1) the classes whose instances are added to any ensemble collection
 * must be public for the ensemble methods to work
 *
 * 2) for threaded variants,e.g., threadEnsembleBag, deadlocks have occured when
 * methods that call notify have been called by the ensemble.
*/

package GenCol;

import java.util.Collection;
import java.util.Iterator;

public class ensembleBag<T> extends Bag<T> implements ensembleInterface<T>
{

    protected ensembleInterface<T> result; // set by specific ensemble

    public void tellAll(String MethodNm, Class<?>[] classes, Object[] args)
    {
        forEach((T o, Integer count) -> {
            new holder<T>(o, MethodNm, classes, args).execute();
        });
    }

    public void tellAll(String MethodNm)
    {
        Class<?>[] classes = {};
        Object[] args = {};

        tellAll(MethodNm, classes, args);
    }

    @Override
    public void AskAll(ensembleInterface<T> Result, String MethodNm, Class<?>[] classes, Object[] args)
    {
        forEach((T o, Integer count) -> {
            new holder<T>(Result, o, MethodNm, classes, args).execute();
        });
    }

    @Override
    public void wrapAll(ensembleInterface<Object> Result, Class<?> cl)
    {
        forEach((T o, Integer count) -> {
            holder.wrap(Result, o, cl);
        });
    }

    /**
     * The method called MethodNm must return "this" (pointer to itself) if
     * condition is true and null otherwise
     */

    public void which(ensembleInterface<T> result, String MethodNm, Class<?>[] classes, Object[] args)
    {
        AskAll(result, MethodNm, classes, args);
    }

    /**
     * The method called MethodNm must return "this" (pointer to itself) if
     * condition is true and null otherwise.
     */
    public T whichOne(String MethodNm, Class<?>[] classes, Object[] args)
    {
        ensembleBag<T> result = new ensembleBag<T>();
        AskAll(result, MethodNm, classes, args);
        Iterator<T> it = result.iterator();
        return !it.hasNext() ? null : it.next();
    }

    public boolean none(String MethodNm, Class<?>[] classes, Object[] args)
    {
        T o = whichOne(MethodNm, classes, args);
        return o == null;
    }

    public boolean all(String MethodNm, Class<?>[] classes, Object[] args)
    {
        ensembleBag<T> result = new ensembleBag<T>();
        which(result, MethodNm, classes, args);
        return result.size() == size();
    }

    public void print()
    {
        Class<?> classes[] = {};
        Object arguments[] = {};
        tellAll("print", classes, arguments);
    }

    public ensembleInterface<T> copy(ensembleInterface<?> c)
    {
        // inherit directly to ensembleBag
        ensembleBag<T> result = new ensembleBag<T>();
        
        ensembleBag<Object> wrapped = new ensembleBag<Object>();
        c.wrapAll(wrapped, wrapObject.class);
        Class<?>[] classes = { ensembleBag.class };
        Object[] args = { result };
        wrapped.tellAll("addSelf", classes, args);

        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    { // becomes set intersection
        if (c.isEmpty())
        {
            return false;
        }
        ensembleBag<Object> wrapped = new ensembleBag<Object>();

        ensembleBag<?> ce = (ensembleBag<?>) c; // needed for recognition in
                                                // reflect
        wrapAll(wrapped, wrapObject.class);
        Class<?>[] classes = {
                ensembleBag.class,
                ensembleBag.class };
        Object[] args = { this, ce };
        wrapped.tellAll("removeSelf", classes, args);

        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c)
    { // inclusion
        if (c.isEmpty())
        {
            return true;
        }
        
        ensembleBag<Object> wrapped = new ensembleBag<Object>();
        ensembleBag<Object> result = new ensembleBag<Object>(); // to efficiently use the
                                                      // return
        // test
        ensembleInterface<?> ce = (ensembleInterface<?>) c;
        
        ce.wrapAll(wrapped, wrapObject.class);
        Class<?>[] classes = { ensembleBag.class };
        Object[] args = { this };
        wrapped.AskAll(result, "isContained", classes, args);

        return !result.contains(Boolean.FALSE);
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    { // union
        ensembleInterface<? extends T> ce = (ensembleInterface<? extends T>) c;
        ensembleBag<Object> wrapped = new ensembleBag<Object>();
        ce.wrapAll(wrapped, wrapObject.class);
        Class<?>[] classes = { ensembleBag.class };
        Object[] args = { this };
        wrapped.tellAll("addSelf", classes, args);

        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    { // difference
        ensembleInterface<?> ce = (ensembleInterface<?>) c;
        ensembleBag<Object> wrapped = new ensembleBag<Object>();
        
        ce.wrapAll(wrapped, wrapObject.class);
        Class<?>[] classes = { ensembleBag.class };
        Object[] args = { this };
        wrapped.tellAll("removeSelf", classes, args);

        return true;
    }
}
