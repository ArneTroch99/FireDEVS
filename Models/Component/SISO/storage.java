/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package Component.SISO;


import java.lang.*;

import Component.BasicProcessor.*;
import GenCol.*;


import model.modeling.*;
import model.simulation.*;

import view.simView.*;


public class storage extends siso{
protected double store;
protected double response_time;

  public storage(){
    super("storage");
    // add by huangdp 10-02-02
   addInport("in");
   addOutport("out");
   // end add 10-02-02
   AddTestPortValue(1);
   AddTestPortValue(2);
}

public storage(String name,double Response_time){
   super(name);
   response_time = Response_time;
}

public void initialize(){
     phase = "passive";
     sigma = INFINITY;
     store = 0;
     response_time = 10;
     super.initialize();
 }


public void  Deltext(double e,double input){
    Continue(e);
    if (input != 0)  // 0 is query
    store = input;
    else holdIn("respond", response_time);
}



public void  deltint( ){
    passivate();
}


public double sisoOut(){
  if (phaseIs("respond"))  return store;
  else return 0;
}

 public void showState(){
  super.showState();
  System.out.println("store: " + store);
 }



}

