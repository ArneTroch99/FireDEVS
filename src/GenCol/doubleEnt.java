/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 


package GenCol;

public class doubleEnt extends entity {

	double v;

public doubleEnt(double t)
{
  v = t;

}

public boolean greaterThan(entity ent){
    return (this.v > ((doubleEnt)  ent).getv());
}

public void setv(double t){v = t;}

public double getv(){return v;}

public void print(){System.out.print( v);}

public boolean equal(entity ent){
//System.out.println(v + " " + ((doubleEnt)ent).getv());
    return (Math.abs(this.v -((doubleEnt)  ent).getv())< 0.0000001);
}

public boolean equals(Object ent){ //needed for Relation
    if (!(ent instanceof doubleEnt)) return false;
    return  equal((entity)ent);
}

public entity  copy(){
     doubleEnt  ip = new doubleEnt(getv());
     return (entity)ip;
}

public String getName(){
     return Double.toString(v);
}

}
