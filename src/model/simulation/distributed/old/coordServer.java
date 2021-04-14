/* 
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 



package model.simulation.distributed.old;


import java.util.*;
import java.io.*;
import java.net.*;

import GenCol.*;


import model.modeling.*;
import model.simulation.*;
import model.simulation.realTime.*;




public class coordServer extends RTcoordinator
                  implements RTCoordinatorInterface{

    protected ServerSocket ss;
    protected Socket   s;
    protected boolean      shutdown = false;
    protected int          iServerPort = 7000;
    protected int  numConnected,numIter,count, registerCount;



public coordServer(coupledDevs c,int numIter){
super(c,true);
this.numIter = numIter;
myThread.start();
}


public void addSimulator(IOBasicDevs comp,simulatorProxy proxy){
 simulators.add(proxy);
 modelToSim.put(comp.getName(),proxy);   //atomic for now
}

public void startClientSimulators(){
componentIterator cit = myCoupled.getComponents().cIterator();
while (cit.hasNext()){
IOBasicDevs iod = cit.nextComponent();
new clientSimulator(iod);
}
}

public void setSimulators(){
componentIterator cit = myCoupled.getComponents().cIterator();
Class [] classes = { Function.class };
Object [] args  = {modelToSim};
simulators.tellAll("setModToSim",classes,args);
}

// void broadcast() sends a message to all clients
public void broadcast( String msg ) {      //send to clients via proxies
System.out.println("broadcast: tell all send "+ msg);
Class [] classes = { String.class };
Object [] args  = {msg};
simulators.tellAll("sendMsg",classes,args);
}


protected void setCountAndWait(int num){
count = num;
}

public void setRegiesterCount(int ct){
registerCount = ct;
}

public synchronized void register(){
registerCount--;
}

public synchronized  void done(){
count--;
notify();
}


public /*synchronized*/ void waitForAllSimRegistered(){
while(registerCount>0)
{
   try{
   Thread.sleep(1000);
   }catch(Exception e){}
}
System.out.println("--------------registration complete");
}

public synchronized double nextTN(){
setCountAndWait(simulators.size());
broadcast("nextTN");
tN = INFINITY;
while(count > 0){
try{
wait();
   }
catch (Exception e)
  {
  System.out.println("nextTN " + e);
  }
}
return tN;
}

public synchronized void telltN(double t){
if (t < tN)
  tN = t;
done();
}

public synchronized void computeInputOutput(double t){
setCountAndWait(simulators.size());
broadcast("computeInputOutput:" + t);
while(count > 0){
try{
wait();
   }
catch (Exception e)
  {
  System.out.println("computeInputOutput " + e);
  }
}
//System.out.println("--------------end of computeInputOutput " );
}


public  synchronized   void DeltFunc(double t){
System.out.println("send DeltFunc " + t);
count = simulators.size();
simulators.tellAll("sendInput");
//threadEnsembleSet causes problems so switched to ensembleSet
while(count > 0){
try{
wait();
   }
catch (Exception e)
  {
  System.out.println("Error in DeltFunc " + e);
  }
}
//System.out.println("end of DeltFunc " );
}


public void run() {
     // Establish ServerSocket for listening
      try {
          ss = new ServerSocket( iServerPort );  // Create server socket
        }
      catch (Exception e ) {
        System.out.println( e.toString() );
        System.exit( -1 );
        }

        // Loop for listening and processing client calls
      setRegiesterCount(myCoupled.getComponents().size());      //Xiaolin Hu, Jun 25, 2001
      while(numConnected < myCoupled.getComponents().size()){

            try {
                System.out.println( "Waiting for connection..." );
                s = ss.accept();      // Listen for client call.

           }
            catch(Exception e ) {
                System.out.println( e.toString() );
              }

            System.out.println( "Yes!  Received a connection!\n\n" );

            numConnected++;
            simulatorProxy proxy = new simulatorProxy(s,this);
            System.out.println("number connected:"  + numConnected);

        }  // end of while( true )
      waitForAllSimRegistered();
      setSimulators();
      informCoupling();
      simulate();
   }


public void  simulate( ){
  tL = 0;
  broadcast("initialize:" + tL);
  nextTN();  //broadcast request for nextTN
  int i=1;
  while( (tN < INFINITY) && (i<=numIter) ) {
   System.out.println("ITERATION " + i + " ,time: " + tN);
    broadcast("continue");
    computeInputOutput(tN);
  //  showOutput();
    DeltFunc(tN);
    tL = tN;
    nextTN();
 //  showModelState();
    i++;
  }
 broadcast("terminate");
 simulators.tellAll("stop");
 System.out.println("Terminated Normally at ITERATION " + i + " ,time: " + tN);
}

}

