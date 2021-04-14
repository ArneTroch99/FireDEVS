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


public class coupledRTSimulator extends coupledSimulator
                              implements CoupledRTSimulatorInterface {

protected double startTime;
protected int numIter;
protected  Thread myThread;
protected boolean inputReady = false;
protected boolean Elapsed = false;
protected long timeToSleep;
protected RTCoordinatorInterface myRTRootParent;
protected RTcoupledCoordinator myRTParent;
protected simTimer timer;

public coupledRTSimulator(IOBasicDevs devs){
super(devs);
}

public coupledRTSimulator(){
super();
}

public  synchronized void initialize(){
 myModel.initialize();
 startTime = timeInMillis();
 tL = startTime;
 tN = tL + myModel.ta()*1000;
 System.out.println("INITIALIZATION, relative time: " +0 +", next event at: "+(tN-startTime)/1000+" seconds");
 myModel.showState();
 myThread = new  Thread(this);

}

public  synchronized void initialize(double sTime){
 myModel.initialize();
 startTime = sTime;
 tL = startTime;
 tN = tL + myModel.ta()*1000;
 System.out.println("INITIALIZATION, relative time: " +0 +", next event at: "+(tN-startTime)/1000+" seconds");
 myModel.showState();
 myThread = new  Thread(this);

}

public void setTN(){
   tN = timeInMillis() + myModel.ta()*1000;
}

public double getTN(){
return tN;
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

public void run(){
     Thread.currentThread().setName(myModel.getName());
     setTN();
     int iter = 0;
//     while( iter < numIter ) {
     while( true ) {
//        System.out.println("Simulator For: " +myModel.getName()+" :ITERATION " + iter  + " ,relative time: " + (tN-startTime));
        while(timeInMillis() < getTN() - 10){
            timeToSleep = (long)(getTN() - timeInMillis());
            if (timeToSleep < DevsInterface.INFINITY){
                timer = new simTimer(this,timeToSleep);
                Elapsed = false;
            }
            waitForNextEvent();
            if (inputReady) break;
        } // out of the while loop becuase of getting an external input or time elapsed
        try{Thread.sleep(100);} //time granule --- wait for other input
             catch (Exception e){}
        if(timeInMillis() >= getTN() - 10) Elapsed = true;

        if(Elapsed){   // time elapsed
           computeInputOutput(getTN());
           showOutput();
           sendMessages();
           wrapDeltfunc(getTN());
        }
        else if(inputReady){ // get external input
            double externalEventTime = timeInMillis();
            if (externalEventTime > getTN())  externalEventTime = getTN();
            wrapDeltfunc(externalEventTime);
        }
//        showModelState();
        if(timer!=null) timer.interrupt();
        inputReady = false;
        Elapsed = false;
        tL = timeInMillis();
        tN = tL + myModel.ta()*1000;
        iter ++;
     }
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

public  synchronized void waitForNextEvent(){
while(!inputReady && !Elapsed){
try{
wait();
   }
catch (InterruptedException e)
  {}
}
}

public synchronized void notifyElapsed(){
Elapsed = true;
notify();
}

public void simulate(Integer i){
simulate(i.intValue());
}

public void  simulate(int NumIter)
{
  int i=1;
  numIter = NumIter;
  tN = nextTN();
  myThread.start();
}

public void stopSimulate(){
numIter = 0;
myThread.interrupt();
}

public void  wrapDeltfunc(double t){
 wrapDeltfunc(t,getInput()); //changed to work with activity
 input = new message();
}

public   void  wrapDeltfunc(double t,MessageInterface x){
 if(x == null){
    System.out.println("ERROR RECEIVED NULL INPUT  " + myModel.toString());
    return;
  }

  if (x.isEmpty() && !equalTN(t)) {
    return;
  }
  else if((!x.isEmpty()) && equalTN(t)) {
    double e = t - tL;
    myModel.deltcon(e/1000,x);
  }

 else if(equalTN(t)) {
     myModel.deltint();
  }
  else if(!x.isEmpty()) {
    double e = t - tL;
    myModel.deltext(e/1000,x);
  }
}

public void startActivity(ActivityInterface a){
a.setSimulator(this);
a.start();
}

public synchronized void putMessages(ContentInterface c){
    if(c == null) return;
    System.out.flush();
    input.add(c);
    inputReady = true;
    notify();
}

}
class simTimer extends Thread{
coupledRTSimulator sim;
long timeOut;

public simTimer (coupledRTSimulator Sim,long Time){
sim = Sim;
timeOut = Time;
start();
}


public void run(){
  try
        {
          Thread.sleep(timeOut);
        }
        catch (Exception e)
        {
        }
sim.notifyElapsed();
}
}




