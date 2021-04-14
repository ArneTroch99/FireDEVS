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


import java.util.*;
import java.util.function.BiConsumer;


//Zcontainers compatibility through extending to ZRelationInterface

interface RelationInterface<K, V>
{

    public boolean isEmpty();

    public int size();

    public boolean contains(K key, V value);

    public Object get(K key);

    public Set<V> getSet(K key);

    public V put(K key, V value);

    public boolean remove(K key, V value);

    public void removeAll(K key);

    public Set<K> keySet();

    public Set<V> valueSet();

    public Iterator<Pair<K,V>> iterator();

    public String toString();

    public int hashCode();

    public boolean equals(Object o);
    
    public void forEach(BiConsumer<K, V> f);
    
    public void clear();
}



