/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
/*
/* PairInterface is similar to Entry contained within Map
*/

package GenCol;

interface PairInterface<K, V>
{

    public String toString();

    public boolean equals(Object o);

    public K getKey();

    public V getValue();

    public int hashCode();

    public int compare(Object m, Object n); // less than
}



