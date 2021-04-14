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


public class passive extends siso{



  public passive(){
    super("passive");
}

public passive(String name){
   super(name);
}

public void initialize(){
     phase = "passive";
     sigma = INFINITY;
     super.initialize();
 }


public void  Deltext(double e,double input){
    passivate();
}



public void  deltint( ){
    passivate();
}


public double sisoOut(){
    return 0;
}

 public void showState(){
  super.showState();
 }


}

