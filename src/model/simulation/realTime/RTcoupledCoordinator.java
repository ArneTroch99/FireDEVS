/* 
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 


package model.simulation.realTime;


import java.util.*;

import GenCol.*;
import GenCol.Queue;
import model.modeling.*;
import model.simulation.*;


public class RTcoupledCoordinator extends coupledCoordinator implements RTCoupledCoordinatorInterface{

protected int numIter;
//protected  Thread myThread;
protected RTCoordinatorInterface myRTRootParent;
protected RTcoupledCoordinator myRTParent;

public RTcoupledCoordinator(Coupled c){
super(c);
}

public long timeInSecs() {
    return (timeInMillis()/1000);
}
public long timeInMillis() {
    return System.currentTimeMillis();
}
 public void setRTRootParent(RTCoordinatorInterface r){
myRTRootParent = r;
}

public RTCoordinatorInterface getRTRootParent(){
return myRTRootParent;
}

public void setRTParent(RTcoupledCoordinator r){
myRTParent = r;
}

public RTCoupledCoordinatorInterface getRTParent(){
return myRTParent;
}

public void addSimulator(IOBasicDevs comp){
coupledRTSimulator s = new coupledRTSimulator(comp);
simulators.add(s);
s.setRTParent(this);      // set the parent
modelToSim.put(comp.getName(),s);
internalModelTosim.put(comp.getName(),s);
}

public void addCoordinator(Coupled comp){
RTcoupledCoordinator s = new RTcoupledCoordinator(comp);
s.setRTParent(this);       // set the parent
simulators.add(s);
modelToSim.put(comp.getName(),s);
internalModelTosim.put(comp.getName(),s);
}


public void  simulate(int numIter)
{
  this.numIter = numIter;
  tL = timeInSecs();
  tN = nextTN();
  tellAllSimulate(numIter);
//  myThread.start();

  }

public synchronized void putMessages(ContentInterface c){
input.add(c);
sendDownMessages();
input = new message();
}

public synchronized void putMyMessages(ContentInterface c){
output.add(c);
sendMessages();
output = new message();
}

public void sendMessages() {    //extend so they send message to its parent also
  MessageInterface o = getOutput();
  if( o!= null && !o.isEmpty()) {
    Map<String, Iterable<ContentInterface>> r = convertMsg((message)getOutput());//assume computeInputOutput done first
    r.forEach((key, value) -> {
       if(modelToSim.get(key) instanceof CoupledRTSimulatorInterface){
           CoupledRTSimulatorInterface sim = (CoupledRTSimulatorInterface)modelToSim.get(key);
           sim.putMessages(value);
       }
       else if(modelToSim.get(key) instanceof RTCoupledCoordinatorInterface){
           RTCoupledCoordinatorInterface sim = (RTCoupledCoordinatorInterface)modelToSim.get(key);
           sim.putMessages(value);
       }
       else{            // this is an internal output coupling
           RTCoupledCoordinatorInterface cci = getRTParent();
           RTCoordinatorInterface ci = getRTRootParent();
           if(cci != null) myRTParent.putMyMessages(value);
           else if(ci != null)  myRTRootParent.putMyMessages(value);
       }
    });
  }
}

public void sendDownMessages() {
  if(!input.isEmpty()){
    Map<String, Iterable<ContentInterface>> r = convertInput(input);
    r.forEach((key, value) -> {
       if(internalModelTosim.get(key) instanceof CoupledRTSimulatorInterface){
           CoupledRTSimulatorInterface sim = (CoupledRTSimulatorInterface)internalModelTosim.get(key);
           sim.putMessages(value);
       }
       else if(internalModelTosim.get(key) instanceof RTCoupledCoordinatorInterface){
           RTCoupledCoordinatorInterface sim = (RTCoupledCoordinatorInterface)internalModelTosim.get(key);
           sim.putMessages(value);
       }
    });
  }
}

public void tellAllSimulate(int numIter){
Class [] classes  = { Integer.class };
Object [] args  = {new Integer(numIter)};
simulators.tellAll("simulate",classes,args);
}

public void stopSimulate(){
simulators.tellAll("stopSimulate");
//myThread.interrupt();
}
/*
public void run(){
long observeTime = 10000;
 try {
  Thread.currentThread().sleep((long)observeTime);
} catch (Exception e) {}
tellAllStop();
System.out.println("Coordinator Terminated Normally at time: " + timeInMillis());

}
*/
public void tellAllStop(){
simulators.tellAll("stopSimulate");
}

}
