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
import GenCol.Queue;
import model.modeling.*;
import model.simulation.*;

public class RTsimulatorProxy extends simulatorProxy {
             //   implements CoupledRTSimulatorInterface{



public RTsimulatorProxy( Socket s, RTcoordServer srvr) {
super(s,srvr);
}



   public  synchronized void sendMessages(message output) {
     srvr.done();
  if (output!=null && !output.isEmpty()) {
   Map<String, Iterable<ContentInterface>> r = convertMsg(output);//assume computeInputOutput done first
   //r.print();
   r.forEach((key, value) -> {
   //co.print();
   RTsimulatorProxy cn  = (RTsimulatorProxy)modelToSim.get(key);
   if(cn!=null) cn.putMessages(value);
   else srvr.putMyMessages(value);     // this goes to the root server
   });
  }
  }


public long timeInSecs() {
    return (timeInMillis()/1000);
}
public long timeInMillis() {
    return System.currentTimeMillis();
}


}

