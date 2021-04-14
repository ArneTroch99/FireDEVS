/*
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;

import java.util.*;

import GenCol.*;


import model.simulation.*;

public class activity extends Thread implements ActivityInterface {

protected double processingTime;

protected CoupledSimulatorInterface sim;

public activity(String name){
 super(name);
 processingTime = 10;

 }

public activity(String name, double pt){
this(name);
processingTime = pt;
}

public void setSimulator(CoupledSimulatorInterface sim){
this.sim = sim;
}

public void returnTheResult(entity myresult) {
System.out.println("return result in Activity");
 sim.returnResultFromActivity(myresult);
}

public double getProcessingTime() {
    return processingTime;
}
/*
public void run(){
   try {
 sleep((long)getProcessingTime()*1000);
} catch (InterruptedException e) {return;}
returnTheResult();
}
*/
public entity computeResult(){
return  new entity(getName() + " --activity result");
}

public void kill(){
interrupt();
}


}