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


public class ramp extends siso{
protected double step_time;
protected double position,inp;

  public ramp(){
    super("ramp");
step_time = 20;
AddTestPortValue(1);
AddTestPortValue(-1);
AddTestPortValue(2);
AddTestPortValue(-2);

}


public ramp(String  name, double Step_time){
super(name);
step_time = Step_time;
}

public void initialize(){
     holdIn("active",step_time);
     inp = 0;
     position = 0;
     super.initialize();
 }

public void  Deltext(double e,double input)
{
    Continue(e);
       position = position + e*inp;
       inp = input;
}

public void  deltint( )
{
position = position + sigma*inp;
sigma = step_time;
}

public  double sisoOut()
{
double nextposition = position + sigma*inp;
return nextposition;
}


 public void showState(){
  super.showState();
  System.out.println("inp: " + inp);
  System.out.println("position: " + position);
 }



}

