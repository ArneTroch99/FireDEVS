/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;

import java.util.*;

interface ZRelationInterface extends RelationInterface,EntityInterface{

public boolean empty();
public int getLength();
public boolean isIn(Object key, Object value);
public Object assoc(Object key);
public Set assocAll(Object key);
public  void add(Object key, Object value);
public void Remove(Object key, Object value);//to avoid clash
public Set domainObjects();
public Set rangeObjects();
}
