/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.SISO;


import java.lang.*;

import Component.BasicProcessor.*;
import Component.SISO.*;
import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.simView.*;


public class generator extends siso{
protected double period;

  public generator(){
    super("generator");
    period = 30;
}

public generator(String name,double Period){
   super(name);
   period = Period;
}

public void initialize(){
     phase = "active";
     sigma = period;
     super.initialize();
 }


public void  deltint( ){
    holdIn("active",period);
    showState();
}


public double sisoOut(){
    return 1;
}

 public void showState(){
  super.showState();
  System.out.println("period: " + period);
 }
}

