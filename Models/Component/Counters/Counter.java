//Counter.java 
//Generalization of the binaryCounter to a KCounter 
//Revisions are commented
package Component.Counters;



import java.lang.*;

import Component.BasicProcessor.*;
import Component.SISO.*;
import GenCol.*;


import model.modeling.*;
import model.simulation.*;

import view.simView.*;


public class Counter extends siso{
  //Primary states:
  //phase: model.modeling.atomic.phase
  //sigma: model.modeling.atomic.sigma
	
  //Secondary States:
  double count; //any positive integer greater than or equal to zero
  
  //Parameters:
  double stepTime; //any positive number greater than zero and less than infinity
  
  //Other:
  double K; //K is the max of count: count = {1,2,...,K}

  
  public Counter(){
    super("Counter");
    addInport("in");
    addOutport("out");
    AddTestPortValue(1);
}

public Counter(String name){
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
    	count++; 
    	holdIn("active",10);
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
 }



}

