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




public class RTcoordinator extends coordinator
                  implements RTCoordinatorInterface{
protected int numIter,count;
protected Thread myThread;
protected double observeTime;
protected RTCoupledinjectThread injThread;

public long timeInSecs() {
    return (timeInMillis()/1000);
}
public long timeInMillis() {
    return System.currentTimeMillis();
}
public RTcoordinator(coupledDevs c){
super(c);
myThread = new Thread(this);
}

public RTcoordinator(coupledDevs c,boolean minimal){
super(c,minimal);
myThread = new Thread(this);
}

public void addSimulator(IOBasicDevs comp){
coupledRTSimulator s = new coupledRTSimulator(comp);
simulators.add(s);
s.setRTRootParent(this);      // set the parent
modelToSim.put(comp.getName(),s);
internalModelTosim.put(comp.getName(),s);
}

public void addCoordinator(Coupled comp){
RTcoupledCoordinator s = new RTcoupledCoordinator(comp);
s.setRTRootParent(this);       // set the parent
simulators.add(s);
modelToSim.put(comp.getName(),s);
internalModelTosim.put(comp.getName(),s);
// later will download modelToSim to its children and then will be updated by its parents
// so after initialization, modelToSim store the brother models and simulators
// internalModelTosim store its children models and simulators
}

//bpz
public void  simulate(int numIter){
simulate(numIter,DevsInterface.INFINITY);
}

public void  simulate(int numIter,double observeTime)
{
  this.numIter = numIter;
  this.observeTime = observeTime;
  tL = timeInSecs();
  tN = nextTN();
  tellAllSimulate(numIter);
  myThread.start();

  }

public void tellAllSimulate(int numIter){
Class [] classes  = { Integer.class };
Object [] args  = {new Integer(numIter)};
simulators.tellAll("simulate",classes,args);
}

public void tellAllStop(){
simulators.tellAll("stopSimulate");
}


public void run(){

 try {
  Thread.currentThread().sleep((long)observeTime);
} catch (Exception e) {}
tellAllStop();
System.out.println("Coordinator Terminated Normally at time: " + timeInMillis());

}

public void wrapDeltFunc(double time) {   // used for external input inject
sendDownMessages();
input = new message();
output = new message();
}

public void sendDownMessages() {
  if(!input.isEmpty()){
    Map<String, Iterable<ContentInterface>> r = convertInput(input);
    r.forEach((key, value) -> {
       if(internalModelTosim.get(key) instanceof CoupledRTSimulatorInterface){
           CoupledRTSimulatorInterface sim = (CoupledRTSimulatorInterface)internalModelTosim.get(key);
           sim.putMessages(value);
       }
       else if(internalModelTosim.get(key) instanceof RTcoupledCoordinator){
           RTcoupledCoordinator sim = (RTcoupledCoordinator)internalModelTosim.get(key);
           sim.putMessages(value);
       }
    });
  }
}

public void simInject(double e,String portName,entity value){
simInject(e,new port(portName),value);
}

public void simInject(double e,PortInterface p,EntityInterface value){
MessageInterface m = makeMessage();
m.add(myModel.makeContent(p,value));
simInject(e,m);
}

public void simInject(double e,MessageInterface m){
injThread = new RTCoupledinjectThread(this,e,m);
}

}

class RTCoupledinjectThread extends Thread{
protected RTcoordinator sim;
protected double e;
protected MessageInterface m;
public RTCoupledinjectThread(RTcoordinator sim,double e,MessageInterface m){
this.sim = sim;
this.e = e;
this.m = m;
start();
}

public void run(){
try {
 sleep((long)e*1000);
} catch (Exception e) {}
sim.input = m;
sim.wrapDeltFunc(e);
System.out.println("Time: " + sim.timeInMillis() +" ,input injected: " );
m.print();
}

}

