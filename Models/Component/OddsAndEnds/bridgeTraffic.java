
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


public class bridgeTraffic extends proc {
  protected double left_time;
  protected double passing_time;
  protected double light_time;

  protected Queue EastQ;
  protected Queue WestQ;

public bridgeTraffic(String name, double ProcessingTime){
  super(name, ProcessingTime);
  addInport("East");
  addInport("West");
  addOutport("out");
}

public bridgeTraffic(){
  super("bridgeTraffic", 50);
  addTestInput("East", new entity("car1"));
  addTestInput("East", new entity("car2"), 20);
  addTestInput("West", new entity("car3"));
  addTestInput("West", new entity("car4"), 45);
  addTestInput("East", new entity("car5"), 40);

  initialize( );
}

/*initialization*/
public void initialize( ){

  //super.initialize( );
  light_time = 50;
  passing_time = 10;
  left_time = light_time;
  phase = "inEast";
  sigma = light_time;
  job = new entity("none");

  EastQ = new Queue( );
  WestQ = new Queue( );

}

/*External Transition Function*/

public void  deltext(double e, message x){
  entity jb;

  // queue the input cars
  for (int i=0; i<x.size(); i++){
      if ( messageOnPort(x, "East",i)){
        jb = x.getValOnPort("East", i);
        EastQ.add(jb);
      }

      if ( messageOnPort(x, "West",i)){
        jb = x.getValOnPort("West", i);
        WestQ.add(jb);
      }
  }

  Continue(e);

  if ( phaseIs("inEast")){
    left_time = left_time - e;
    if (left_time >= passing_time){
      if ( ! EastQ.isEmpty() ){
        job = (entity)EastQ.getFirst();
        holdIn("passingEast", passing_time);
      }
    }
  }

  if ( phaseIs("inWest")){
    left_time = left_time - e;
    if ( left_time >= passing_time){
      if ( !WestQ.isEmpty() ){
        job = (entity)WestQ.getFirst();
        holdIn("passingWest", passing_time);
      }
    }
  }
}

/*Internal Transition Function*/
public void  deltint( ){
  if ( phaseIs("passingEast")){
    EastQ.remove();
    left_time = left_time - 10;

    if ((left_time >= passing_time) & (! EastQ.isEmpty())){
      job = (entity)EastQ.getFirst();
      holdIn("passingEast", passing_time);
    }
    else{
      if ( left_time == 0){
        left_time = light_time;
        holdIn("inWest", left_time);
      }
      else
        holdIn("inEast", left_time);
    }
  }
  else if ( phaseIs("passingWest")){
    WestQ.remove();
    left_time = left_time - 10;

    if ((left_time >= passing_time) & (! WestQ.isEmpty())){
      job = (entity)WestQ.getFirst();
      holdIn("passingWest", passing_time);
    }
    else{
      if ( left_time == 0){
        left_time = light_time;
        holdIn("inEast", left_time);
      }
      else
        holdIn("inWest", left_time);
    }
  }
  else if ( phaseIs("inEast")){
    left_time = light_time;

    if ( ! WestQ.isEmpty() ){
      job = (entity)WestQ.getFirst() ;
      holdIn("passingWest", passing_time);
    }
    else
      holdIn("inWest", left_time);
  }
  else if ( phaseIs("inWest")){
    left_time = light_time;
    if ( !EastQ.isEmpty()){
      job = (entity)EastQ.getFirst() ;
      holdIn("passingEast", passing_time);
    }
    else
      holdIn("inEast", left_time);
  }
}

/*Output Function*/
public message out( ){
  message m = new message( );

  if ((phaseIs("passingEast")) | (phaseIs("passingWest"))){
     System.out.println("out: -->" + job);
     m.add(makeContent("out", job));
  }

  return m;
}

}