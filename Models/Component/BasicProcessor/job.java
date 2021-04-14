/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.BasicProcessor;

import GenCol.*;
import model.modeling.*;
import model.simulation.*;

public class job extends entity{
     public double processing_time;

public job(String name){
    super(name);
    processing_time = 1000;
}

public job(String name,double Processing_time){
    super(name);
    processing_time = Processing_time;
}

public boolean equal( entity  m){
       job jm = (job)m;
     return processing_time == jm.processing_time;
}

public boolean greater_than( entity  m){
       job jm = (job)m;
      return processing_time < jm.processing_time;
                //choose on basis of smaller time left
}

public void print(){
    System.out.println("job: "+name +
         " processing time: " + processing_time);
}
public void update(double e){
    processing_time = processing_time - e;
}

}

//  extending class job

class jobSize extends job{
     public int size;

public jobSize(String name){
    super(name);
    size = 1;
}


public jobSize(String name,double Processing_time,int Size){
    super(name,Processing_time);
    size = Size;
}

public boolean equal( entity  m){
       jobSize jm = (jobSize)m;
     return processing_time == jm.processing_time
     && size == jm.size;
}


public boolean greater_than( entity  m){
       jobSize jm = (jobSize)m;
       if (super.equal(m))
          return size > jm.size;
       else return super.greater_than(m);
}

public void print(){
    System.out.println("job: "+name +
         " processing time: " + processing_time
         + " size: " + size
         );
}

}
