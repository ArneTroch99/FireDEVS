/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.OddsAndEnds;


import java.lang.*;

import Component.BasicProcessor.*;
import Component.SISO.*;
import GenCol.*;


import model.modeling.*;
import model.simulation.*;

import view.simView.*;

/**
 * <p>Delay </p>
 * <p>CSE 591 - HW3 </p>
 * Author : Dongping Huang
 */

public class delay extends siso{
protected double sv;
protected double response_time;

public delay(){
  super("delay");
  addInport("in");
  addOutport("out");
  AddTestPortValue(1);
  AddTestPortValue(-1);
}

public delay(String name, double Response_time){
  super(name);
  response_time = Response_time;
}

public void initialize(){
  phase = "passive" ;
  sigma = INFINITY;
  sv = 0;
  response_time = 5;
  super.initialize();
}

public void Deltext(double e, double input){
  Continue(e);

  if (phaseIs("passive")){
    if ( input == -1 | input == 1){
      sv = input;
      holdIn("active", response_time);
    }
  }
}

public void deltint(){
  passivate();
}

public double sisoOut(){
  if ( phaseIs("active")){
    System.out.println("output on port 'out' is:" + sv);
    return sv;
  }
  else{
    return 0;
  }
}

}