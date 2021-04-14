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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Function;

import util.TriConsumer;

public class Bag<T> extends entity implements BagInterface<T>, Collection<T>
{
    private Relation<T, Integer> relation;

    public Bag()
    {
        this.relation = new Relation<T, Integer>();
    }

    public int numberOf(T key)
    {
        return this.relation.getSet(key).size();
    }
    
    public boolean add(T o, int n)
    {
        int k = numberOf(o);
        boolean changed = false;
        
        for (int i = 1; i <= n; i++)
        {
            Integer old = this.relation.put(o, new Integer(k + i));
            changed |= (old != null);
        }
        
        return changed;
    }

    @Override
    public boolean add(T o)
    {
        return this.relation.put(o, new Integer(numberOf(o) + 1)) != null;
    }

    @Override
    public boolean remove(Object o)
    {
        try
        {
            @SuppressWarnings("unchecked")
            T obj = (T) o;

            if (this.relation.remove(obj, new Integer(numberOf(obj))))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (ClassCastException e)
        {
            return false;
        }

    }

    public Set<T> bag2Set()
    {
        return this.relation.keySet();
    }

    @Override
    public boolean contains(Object key)
    {
        try
        {
            @SuppressWarnings("unchecked")
            T obj = (T) key;
            return numberOf(obj) > 0;
        }
        catch (ClassCastException e)
        {
            return false;
        }
    }

    @Override
    public void forEach(BiConsumer<T, Integer> f)
    {
        this.relation.forEach(f);
    }

    @Override
    public void enumerate(TriConsumer<Integer, T, Integer> f)
    {
        this.relation.enumerate(f);
    }

    public static <_T> Vector<_T> toVector(Bag<_T> b)
    {
        Vector<_T> v = new Vector<_T>(b.size());

        b.forEach((_T o, Integer count) -> {
            v.add(o);
        });

        return v;
    }

    public static <_T> Object[] toArray(Bag<_T> b) throws ArrayStoreException
    {
        try
        {
            @SuppressWarnings("unchecked")
            _T[] arr = (_T[]) Array.newInstance(b.getClass().getTypeParameters()[0].getClass(), b.size());

            b.enumerate((Integer i, _T o, Integer count) -> {
                arr[i] = o;
            });

            return arr;
        }
        catch (ClassCastException e)
        {
            ArrayStoreException e2 = new ArrayStoreException();
            e2.setStackTrace(e.getStackTrace());
            e2.initCause(e.getCause());

            throw e2;
        }
    }

    public static <T> Iterator<T> makeIterator(Bag<T> b)
    {
        return new BagIterator<T>(b.relation);
    }

    public Object[] toArray()
    {
        return Bag.toArray(this);
    }

    @Override
    public Iterator<T> iterator()
    {
        return Bag.makeIterator(this);
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        Set<T> s = bag2Set();
        s.forEach((T o) -> {
            sb.append(numberOf(o)).append(":").append(o.toString()).append("\n");
        });

        return sb.toString();
    }

    public static <T> Bag<T> List2Bag(List<T> l)
    {
        Bag<T> b = new Bag<T>();

        l.forEach((T o) -> {
            b.add(o);
        });

        return b;
    }

    public boolean contains(T key, Integer value)
    {
        try
        {
            T obj = (T) key;
            return value == numberOf(obj) && numberOf(obj) > 0;
        }
        catch (ClassCastException e)
        {
            return false;
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        boolean changed = false;
        for (T o : c)
        {
            if (this.add(o))
            {
                changed = true;
            }
        }

        return changed;
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        boolean doesContain = true;

        for (Object o : c)
        {
            if (!this.contains(o))
            {
                doesContain = false;
            }
        }

        return doesContain;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean changed = false;

        for (Object o : c)
        {
            if (this.remove(o))
            {
                changed = true;
            }
        }

        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        Boolean[] changed = new Boolean[1];
        changed[0] = false;
        this.forEach((T o, Integer count) -> {
            if (!c.contains(o))
            {
                remove(o);
                changed[0] = true;
            }
        });

        return changed[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public <_T> _T[] toArray(_T[] a) throws NullPointerException, ArrayStoreException
    {
        try
        {
            T[] arr = a.length < this.size() ? (T[]) new Object[this.size()] : (T[]) a;

            this.enumerate((Integer index, T obj, Integer count) -> {
                arr[index] = obj;
            });

            if (a.length > this.size())
            {
                arr[a.length] = null;
            }

            return (_T[]) arr;
        }
        catch (ClassCastException e)
        {
            ArrayStoreException e2 = new ArrayStoreException();
            e2.setStackTrace(e.getStackTrace());
            e2.initCause(e.getCause());

            throw e2;
        }
    }

    @Override
    public void clear()
    {
        this.relation.clear();
    }

    @Override
    public boolean isEmpty()
    {
        return this.relation.isEmpty();
    }

    @Override
    public int size()
    {
        return this.relation.size();
    }

    @Override
    public void removeAll(T key)
    {
        this.relation.removeAll(key);
    }

    @Override
    public T findIf(Function<T, Boolean> f)
    {
        for (T o : this)
        {
            if (f.apply(o) == true)
            {
                return o;
            }
        }
        return null;
    }

}

class BagIterator<T> implements Iterator<T>
{
    private RelationIterator<T, Integer> it;

    public BagIterator(Relation<T, Integer> r)
    {
        it = new RelationIterator<T, Integer>(r);
    }

    @Override
    public boolean hasNext()
    {
        return it.hasNext();
    }

    @Override
    public T next()
    {
        return it.next().key;
    }
}
