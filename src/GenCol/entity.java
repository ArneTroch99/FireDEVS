/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;




public class entity implements EntityInterface{
protected String name;


public entity(){
name = "anEntity";
}
public entity(String nm){
name = nm;
}
public boolean eq(String nm){
return getName().equals(nm);
}

public Object equalName(String nm){
if (eq(nm)) return this;
else return null;
}

@Override
public int hashCode() {
    return name.hashCode();
}

public boolean equals(Object o){    //overrides pointer equality of Object
if  (!(o instanceof entity))return false;
else return eq(((entity)o).getName());
}

public ExternalRepresentation getExtRep(){
return new ExternalRepresentation.ByteArray();
}

public String getName(){
return name;
}


public synchronized void addSelf(ensembleCollection<Object> c){
c.add(this);
}
public synchronized void removeSelf(ensembleCollection<Object> c){
c.remove(this);
}

/**/

public String toString(){
return getName();
}

public void print(){
System.out.println(name);
}

}

