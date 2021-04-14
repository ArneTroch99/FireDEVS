/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
/*
/*  ZRelation is same as relation in ZContainers
/*  can't define relation and Relation due to Java restriction
*/

package GenCol;


import java.util.*;


public class ZRelation  extends Relation implements ZRelationInterface{
String name;

public ZRelation(){
super();
}

public ZRelation(String name){
super();
this.name = name;
}

public String getName(){
return name;
}

public ExternalRepresentation getExtRep(){
return new ExternalRepresentation.ByteArray();
}

public boolean empty(){
return isEmpty();
}

public int getLength(){
return size();
}

public Set assocAll(Object key){
return getSet(key);
}


public Object assoc(Object key){
return get(key);
}


public boolean isIn(Object key, Object value){
return contains(key, value);
}

public Set domainObjects(){
return keySet();
}

public Set rangeObjects(){
return valueSet();
}


public synchronized void add(Object key, Object value){
put(key,value);
}

public synchronized void Remove(Object key, Object value){
remove(key,value);
}

}
