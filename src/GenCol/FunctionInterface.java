/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
/*
/*  iteration is through Pairs rather than Entries
*/

package GenCol;


import java.util.*;




//inherit contains(Object), containsKey(Object), containsValue(Object)


interface FunctionInterface extends Map{

public boolean isEmpty();
public int size();
public boolean containsKey(Object key);
public boolean containsValue(Object value);
public boolean contains(Object key, Object value);
public Object get(Object key);
public Object put(Object key, Object value); //acts as replace
public Object remove(Object key);
public Set keySet();
public Set valueSet();
public Iterator iterator();
public String toString();
public int hashCode();
}

