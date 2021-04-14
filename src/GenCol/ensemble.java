package GenCol;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

class ensemble<T> implements ensembleBasic<T>, ensembleLogic
{ // need static to be called from make
    public static <T> ensemble<T> make(Collection<T> c)
    {
        return new ensemble<T>(c);
    }

    protected Collection<T> col;

    protected ensemble(Collection<T> col)
    { // only called from ensembleWrapper
        this.col = col;
    }

    public void tellAll(String MethodNm, Class<?>[] classes, Object[] args)
    {
        col.forEach((T o) -> {
            new holder<T>(o, MethodNm, classes, args).execute();
        });
    }

    public void tellAll(String MethodNm)
    {
        Class<?>[] classes = {};
        Object[] args = {};
        tellAll(MethodNm, classes, args);
    }

    public void AskAll(ensembleInterface<T> Result, String MethodNm, Class<?>[] classes, Object[] args)
    {
        col.forEach((T o) -> {
            new holder<T>(Result, o, MethodNm, classes, args).execute();
        });
    }

    public void wrapAll(ensembleInterface<Object> Result, Class<?> cl)
    {
        col.forEach((T o) -> {
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
        Object o = whichOne(MethodNm, classes, args);
        return o == null;
    }

    public boolean all(String MethodNm, Class<?>[] classes, Object[] args)
    {
        ensembleBag<T> result = new ensembleBag<T>();
        which(result, MethodNm, classes, args);
        return result.size() == col.size();
    }


    public void print()
    {
        Class<?> classes[] = {};
        Object arguments[] = {};
        tellAll("print", classes, arguments);
    }

    public ensembleInterface<?> copy(ensembleInterface<?> c)
    {
        // inherit directly to ensembleBag
        ensembleBag<Object> wrapped = new ensembleBag<Object>();
        ensembleBag<Object> result = new ensembleBag<Object>();

        c.wrapAll(wrapped, GenCol.wrapObject.class);
        Class<?>[] classes = { GenCol.ensembleBag.class };
        Object[] args = { result };
        wrapped.tellAll("addSelf", classes, args);

        return result;
    }

    public boolean retainAll(Collection<?> c)
    { // becomes set intersection
        if (c.isEmpty())
        {
            return false;
        }
        ensembleBag<Object> wrapped = new ensembleBag<Object>();
        ensembleBag<?> ce = (ensembleBag<?>) c; // needed for recognition in
                                          // reflect

        wrapAll(wrapped, GenCol.wrapObject.class);

        Class<?>[] classes = {
                GenCol.ensembleBag.class,
                GenCol.ensembleBag.class
        };

        Object[] args = { this, ce };
        wrapped.tellAll("removeSelf", classes, args);
        return true;
    }

    public boolean containsAll(Collection<?> c)
    { // inclusion
        if (c.isEmpty())
        {
            return true;
        }
        ensembleBag<Object> wrapped = new ensembleBag<Object>();
        ensembleBag<Object> result = new ensembleBag<Object>(); // to efficiently use the
                                                // return test
        ensembleInterface<?> ce = (ensembleInterface<?>) c;
        
        ce.wrapAll(wrapped, wrapObject.class);
        Class<?>[] classes = { ensembleBag.class };
        Object[] args = { this };
        wrapped.AskAll(result, "isContained", classes, args);

        return !result.contains(Boolean.FALSE);
    }

    public boolean addAll(Collection<?> c)
    { // union
        ensembleInterface<?> ce = (ensembleInterface<?>) c;
        ensembleBag<Object> wrapped = new ensembleBag<Object>();

        ce.wrapAll(wrapped, GenCol.wrapObject.class);
        Class<?>[] classes = { GenCol.ensembleBag.class };
        Object[] args = { this };
        wrapped.tellAll("addSelf", classes, args);
        return true;
    }

    public boolean removeAll(Collection<?> c)
    { // difference
        ensembleInterface<?> ce = (ensembleInterface<?>) c;
        ensembleBag<Object> wrapped = new ensembleBag<Object>();

        ce.wrapAll(wrapped, GenCol.wrapObject.class);
        Class<?>[] classes = { GenCol.ensembleBag.class };
        Object[] args = { this };
        wrapped.tellAll("removeSelf", classes, args);
        return true;
    }

    public Object[] toArray()
    {
        Object[] result = new Object[col.size()];
        
        Iterator<T> e = col.iterator();
        for (int i = 0; e.hasNext(); i++)
        {
            result[i] = e.next();
        }

        return result;
    }

    public Object[] toArray(Object a[])
    {
        int size = col.size();
        if (a.length < size)
        {
            a = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
        }

        Iterator<T> it = col.iterator();
        for (int i = 0; it.hasNext(); i++)
        {
            a[i] = it.next();
        }

        if (a.length > size)
        {
            a[size] = null;
        }

        return a;
    }

}
