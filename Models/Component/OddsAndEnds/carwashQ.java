/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.OddsAndEnds;

import Component.BasicProcessor.*;
import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.simView.*;


public class carwashQ extends proc{

protected Queue Carq;


public carwashQ(String name, double ProcessingTime){
  super(name, ProcessingTime);
  Carq = new Queue( );
  addInport("inCar");
  addInport("inTruck");
}

public carwashQ(){
  super("carwashQ", 20);
  addTestInput("inCar", new entity("car1"));
  addTestInput("inCar", new entity("car2"));
  addTestInput("inCar", new entity("car3"), 15);
  addTestInput("inTruck", new entity("truck1"));
  addTestInput("inTruck", new entity("truck2"));

  initialize( );
}

/*initialization*/
public void initialize( ){
  Carq = new Queue( );
  super.initialize( );
}

/*External Transition Function*/

public void  deltext(double e, message x){
  boolean truckFirst = false;
  System.out.println("ext");
  Continue(e);
  if (phaseIs("passive")){
    // check the first is truck or not
    if ( messageOnPort(x, "inTruck", 0)){
      job = x.getValOnPort("inTruck", 0);
      holdIn("busyTruck", processing_time*2);
      truckFirst = true;
      Carq.add(job);
    }

    // queue the cars for service
    for (int i=0; i<x.size(); i++){
      if (messageOnPort(x, "inCar",i)){
        job = x.getValOnPort("inCar", i);
        if ( !truckFirst) {
          holdIn("busyCar", processing_time);
        }
        Carq.add(job);
      }
    }

    job = (entity)Carq.first();
  }
  else if (phaseIs("busyCar") || phaseIs("busyTruck")){
    for (int i=0; i<x.size(); i++){
      if ( messageOnPort(x, "inCar",i)){
        entity jb = x.getValOnPort("inCar", i);
        Carq.add(jb);
      }
    }
  }
}

/*Internal Transition Function*/
public void  deltint( ){
  Carq.remove();
  if ( !Carq.isEmpty()){
    job = (entity)Carq.first();
    holdIn("busyCar", processing_time);
  }
  else passivate( );
}

/*Output Function*/
public message out( ){
  message m = new message( );

  if ((phaseIs("busyCar")) | (phaseIs("busyTruck"))){
      System.out.println("out: -->" + job);
      m.add(makeContent("out", job));
  }

  return m;
}

public void showState( ){
  super.showState( );
  System.out.println("Queue length: " + Carq.size( ));
}

public String getTooltipText( ){
  return
  super.getTooltipText( )
   +"\n"+"queue length: " + Carq.size( )
    +"\n"+"queue itself: " + Carq.toString( );
}

}