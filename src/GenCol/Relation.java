/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */


/*
/*  no correspondence in Java collections
/*  iteration is through Pairs rather than Entries
*/

package GenCol;


import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;

import util.TriConsumer;


public class Relation<K, V> extends entity implements RelationInterface<K, V>
{
    // extending Hashtable{ results in loop due to overloading of put

    // protected
    public Hashtable<K, Set<V>> h;
    protected int size;

    public Relation()
    {
        h = new Hashtable<K, Set<V>>();
        size = 0;
    }

    public boolean isEmpty()
    {
        return size == 0; // bpz
    }

    public int size()
    {
        return size;
    }

    public Set<V> getSet(Object key)
    {
        if (h.get(key) == null)
        {
            return new HashSet<V>();
        }
        else
        {
            return h.get(key);
        }
    }

    public synchronized V put(K key, V value)
    {
        Set<V> s = getSet(key);
        Iterator<V> it = s.iterator();
        V old = it.hasNext() ? it.next() : null;
        if (s.add(value))
        {
            size++;
        }
        
        if (!h.containsKey(key))
        {
            h.put(key, s);
        }

        return old;
    }


    public synchronized boolean remove(K key, V value)
    {
        Set<V> s = getSet(key);
        
        boolean changed = false;
        if (s.size() > 0)
        {
            changed = s.remove(value);
            if (changed)
            {
                --size;
                if (s.size() - 1 > 0)
                {
                    h.remove(key);
                }
            }
        }

        return changed;
    }

    public synchronized void removeAll(K key)
    {
        Set<V> s = getSet(key);
        size -= s.size();
        h.remove(key);
    }

    public V get(K key)
    {
        Set<V> s = getSet(key);
        if (s.isEmpty())
        {
            return null;
        }
        else
        {
            Iterator<V> it = s.iterator();
            return it.next();
        }
    }

    public boolean contains(Object key, Object value)
    {
        return getSet(key).contains(value);
    }
    
    public Set<K> keySet()
    {
        return h.keySet();
    }

    public Set<V> valueSet()
    {
        HashSet<V> hs = new HashSet<V>();
        
        forEach((K key, V value) -> {
            hs.add(value);
        });

        return hs;
    }

    public RelationIterator<K, V> iterator()
    {
        return new RelationIterator<K, V>(this);
    }
    
    public void forEach(BiConsumer<K, V> f)
    {
        Iterator<Pair<K, V>> it = iterator();

        while(it.hasNext())
        {
            Pair<K, V> p = it.next();
            f.accept(p.key, p.value);
        }
    }
    
    public void enumerate(TriConsumer<Integer, K, V> f)
    {
        RelationIterator<K, V> it = iterator();
        
        while(it.hasNext())
        {
            Integer i = it.nextIndex();
            Pair<K, V> p = it.next();
            f.accept(i, p.key, p.value);
        }
    }
    
    public void clear()
    {
        size = 0;
        h.clear();
    }

    public String toString()
    {
        Iterator<Pair<K, V>> it = iterator();
        String s = "";
        while (it.hasNext())
        {
            s += it.next().toString() + "\n";
        }
        return s;
    }

    public int hashCode()
    {
        Iterator<Pair<K, V>> it = iterator();
        int sum = 0;
        while (it.hasNext())
        {
            sum += it.next().hashCode();
        }
        return sum;
    }

    public void print()
    {
        System.out.println(toString());
    }

    public synchronized boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        Class<?> cl = getClass();
        if (!cl.isInstance(o))
        {
            return false;
        }

        @SuppressWarnings("unchecked") //Cast checked above in cl.isInstance(o)
        Relation<K, V> t = (Relation<K, V>) o;
        if (t.size() != size())
        {
            return false;
        }

        Set<K> keyset = keySet();
        Set<K> tset = t.keySet();
        if (!keyset.equals(tset))
        {
            return false;
        }

        Iterator<K> it = keyset.iterator();
        while (it.hasNext())
        {
            Object key = it.next();
            Set<V> valueset = getSet(key);
            Set<V> tvalueset = t.getSet(key);
            if (!valueset.equals(tvalueset))
            {
                return false;
            }
        }
        return true;
    }
}

