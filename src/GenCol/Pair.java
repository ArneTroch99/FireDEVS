/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
/*
/*
/*   Pair is more  primitive than Entry which is private to Hashtable
*/

package GenCol;

public class Pair<K, V> extends entity implements PairInterface<K, V>
{
    public K key;
    public V value;

    public Pair()
    {
        key = null;
        value = null;
    }

    public Pair(K Key, V Value)
    {
        key = Key;
        value = Value;
    }

    public String toString()
    {
        return new StringBuilder("key = ")
                .append(key.toString())
                .append(" ,value = ")
                .append(value.toString())
                .toString();
    }

    public boolean equals(Object o)
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

        @SuppressWarnings("unchecked") // Cast safety checked above ->
                                       // !cl.isInstance(o)
        Pair<K, V> p = (Pair<K, V>) o;

        return key.equals(p.key) && value.equals(p.value);
    }

    public K getKey()
    {
        return key;
    }

    public V getValue()
    {
        return value;
    }

    public int hashCode()
    {
        return key.hashCode() + value.hashCode();
    }

    public int compare(Object m, Object n)
    { // less than
        Class<?> cl = getClass();
        if (!cl.isInstance(m) || !cl.isInstance(n))
        {
            return 0;
        }

        @SuppressWarnings("unchecked") // Cast safety checked above
                                       // !cl.isInstance({m|n})
        Pair<K, V> pm = (Pair<K, V>) m;
        @SuppressWarnings("unchecked")
        Pair<K, V> pn = (Pair<K, V>) n;

        if (m.equals(n))
        {
            return 0;
        }

        if (pm.key.hashCode() < pn.key.hashCode())
        {
            return -1;
        }

        if (pm.key.hashCode() == pn.key.hashCode() 
                && pm.value.hashCode() <= pn.value.hashCode())
        {
            return -1;
        }
        return 1;
    }
}