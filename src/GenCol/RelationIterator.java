/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;

import java.util.*;

public class RelationIterator<K, V> implements Iterator<Pair<K, V>>
{
    private Relation<K, V> r;
    private Queue<K> keys;
    private K curKey = null;
    private Set<V> curSet;
    private Queue<V> values;
    private Integer index;
    private boolean change = true;

    public RelationIterator(Relation<K, V> R)
    {
        index = 1;
        r = R;
        Set<K> keyset = R.keySet();
        keys = (Queue<K>) Queue.set2Queue(keyset);
    }

    private Pair<K, V> Next()
    {
        if (keys.isEmpty())
        {
            return null;
        }
        if (change)
        {
            change = false;
            curKey = keys.first();
            curSet = r.getSet(curKey);
            values = Queue.set2Queue(curSet);
        }
        if (values.isEmpty())
        {
            return null;
        }
        return new Pair<K, V>(curKey, values.first());
    }

    private void removeNext()
    {
        values.remove();
        if (values.isEmpty())
        {
            keys.remove();
            change = true;
        }
    }

    public boolean hasNext()
    {
        return Next() != null;
    }

    public Pair<K, V> next()
    {
        Pair<K, V> ret = Next();
        ++index;
        removeNext();
        return ret;
    }
    
    public Integer nextIndex()
    {
        return index;
    }
}
