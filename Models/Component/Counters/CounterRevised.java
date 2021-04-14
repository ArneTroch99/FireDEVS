//CounterRevision.java 
//Generalization of the binaryCounter to a KCounter with Revisions
package Component.Counters;


import java.lang.*;

import Component.BasicProcessor.*;
import Component.SISO.*;
import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.simView.*;


public class CounterRevised extends siso{
  //Primary states:
  //phase: model.modeling.atomic.phase
  //sigma: model.modeling.atomic.sigma
	
  //Secondary States:
  double count; //any positive integer greater than or equal to zero
  
  //Parameters:
  double stepTime; //any positive number greater than zero and less than infinity
  
  //Other:
  double K; //K is the max of count: count = {1,2,...,K}
  double lastCount; 

  
  public CounterRevised(){
    super("Counter");
    addInport("in");
    addOutport("out");
    AddTestPortValue(1);
}

public CounterRevised(String name){
   super(name);
   addInport("in");
   addOutport("out");
   AddTestPortValue(1);
}

public void initialize(){
	//Initialization:
    phase = "passive"; //phase: passive
    sigma = 1/0.0; //sigma: infinity
    count  = 0; //count: zero
    super.initialize();
 }

//External Transition Function
public void  Deltext(double e,double input){
    Continue(e);
    
    sigma = sigma - e; //update to sigma to reflect elapsed time
    
    //when receive input value on input port “in”
    if (input != 0 && phase == "passive"){
    	phase = "active";
    	sigma = stepTime; 
    	lastCount = count; //(2) Immediately output the last count
    	count++; 
    	holdIn("active",10);
    }
    
    //((1)Respond to input zero when it is in phase “active” 
    if (input == 0 && phase == "active"){
    	phase = "active";
    	sigma = stepTime; 
    	lastCount = count; //(2) Immediately output the last count
    	count++; 
    	holdIn("active",10);
    	System.out.println("lastCount: " + lastCount);
    }
    
    else if (input == 0 && phase == "passive"){
    	phase = "respond"; 
    	sigma = stepTime;
    }
}

//Internal Transition Function:
public void  deltint( ){
phase = "passive"; //Phase = passive 
sigma = 1/0.0;  //Sigma = infinity
}

//Output Function:
public double sisoOut(){
    if (phaseIs("respond"))
       return count;
     else return 0;
}

 public void showState(){
  super.showState();
System.out.println("count: " + count);
//(2) Immediately output the last count
System.out.println("lastCount: " + lastCount);
return;
 }



}

