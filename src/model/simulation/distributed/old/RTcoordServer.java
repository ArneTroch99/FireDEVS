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




public class RTcoordServer extends coordServer
                  implements RTCoordinatorInterface{

protected long timeToSleep;

public RTcoordServer(coupledDevs c,int numIter){
super(c,numIter);
}

public void addSimulator(IOBasicDevs comp,RTsimulatorProxy proxy){
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

public double getTN(){ return tN; }

public  synchronized   void DeltFunc(double t){
System.out.println("send DeltFunc " + t);
count = simulators.size();
//simulators.tellAll("sendInput");
//threadedEnsembleSet causes problems
Iterator it = simulators.iterator();
while (it.hasNext()){
RTsimulatorProxy sp = (RTsimulatorProxy)it.next();
sp.sendInput();
}
System.out.println("TellDeltFunc " + count);

while(count > 0){
try{
wait();
   }
catch (Exception e)
  {
  System.out.println("Error in DeltFunc " + e);
  }
}
System.out.println("end of DeltFunc " );
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
            RTsimulatorProxy proxy = new RTsimulatorProxy(s,this);
            System.out.println("number connected:"  + numConnected);

        }  // end of while( true )
      waitForAllSimRegistered();
      setSimulators();
      informCoupling();
      simulate();
   }

public void  simulate( ){
    double t ;
    tL = timeInMillis();
    broadcast("initialize:" + tL);
    tN = nextTN();  //broadcast request for nextTN
    int i=1;
    System.out.println("tN = " + tN +" numIter= "+numIter);
    while( (tN < INFINITY) && (i<=numIter) ) {
      try {
          t = tN-tL;
          System.out.println("coordServer Thread try to sleep for ==> " + t);
          Thread.currentThread().sleep((long)(tN-tL)*1000);
      } catch (Exception e) { }
    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!ITERATION " + i + " ,time: " + tN);
    broadcast("continue");
    computeInputOutput(tN);
  //  showOutput();
    DeltFunc(tN);
    tL = tN;
    tN = nextTN();
 //  showModelState();
    i++;
  }
  broadcast("terminate");
  simulators.tellAll("stop");
  System.out.println("Terminated Normally at ITERATION " + i + " ,time: " + tN);
}

}

