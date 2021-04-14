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

public class simulatorProxy extends coupledSimulator
       implements CoupledSimulatorInterface,Runnable{

        coordServer          srvr;
        DataOutputStream  dos;
        DataInputStream   dis;
        PrintStream       ps;
        Thread myThread;



public simulatorProxy( Socket s, coordServer srvr) {

            this.srvr = srvr;
            myThread = new Thread(this);

            try {
                dis = new DataInputStream( s.getInputStream() );
                dos = new DataOutputStream( s.getOutputStream() );
                ps  = new PrintStream( dos );
            }
            catch( IOException e ) {
                System.out.println( e );
                System.exit( -1 );
            }

            myThread.start();
        }
    boolean quit = false;

    public void stop(){
     quit = true;
     }

    public void run() {
        waitForName();
            while( !quit) {
        waitForNextTN();
        waitForMsgToSend();
             }
    System.out.println("SimulatorProxy terminated");
        }


  public  synchronized  void waitForName(){  //from component
  System.out.println("waiting for name");
  String compName = readMsg();


 IOBasicDevs comp = (IOBasicDevs)srvr.getCoupled().withName(compName);
 myModel = comp;
 srvr.addSimulator(comp,this);
 System.out.println("name is : "+compName);
 srvr.register();
  }

  //client will be identified by component name it is working on
  public  synchronized void waitForNextTN(){  //from component
  String s = readMsg();
  srvr.telltN(Double.parseDouble(s));
  }

  public  message interpret(String s){
   String portString, valueString;
   int startIndex = 0;
   int portIndex, valueIndex;
   if (s == null || s.startsWith("none"))return new message();
   message m = new message();
    while(true){
        portIndex = s.indexOf("port: ",startIndex);
        valueIndex = s.indexOf(" value: ", startIndex);
        portString=s.substring(portIndex+6,valueIndex).trim();
        startIndex =  valueIndex+8;
        portIndex = s.indexOf("port: ",startIndex);
        if(portIndex!=-1){
          valueString = s.substring(valueIndex+8,portIndex).trim();
          m.add(new content(portString,new entity(valueString)));
        }
        else{
          valueString = s.substring(valueIndex+8,s.length()).trim();
          m.add(new content(portString,new entity(valueString)));
          break;
        }
    }
   return m;
   }

/*   public  synchronized  message interpret(String s){ //single port expected for now

   if (s == null || s.startsWith("none"))return null;
   int index1 = s.indexOf(": ");
   int index2 = s.indexOf(" v");
   String p = s.substring(index1+1,index2).trim();
   message m = new message();
   m.add(new content(p,new entity()));
   return m;
   }
*/

   public  synchronized void  waitForMsgToSend(){ //from component
   String s = readMsg();
   message output = interpret(s);
   sendMessages(output);
   }

   public  synchronized void sendMessages(message output) {
     srvr.done();
  if (output!=null && !output.isEmpty()) {
      Map<String, Iterable<ContentInterface>> r = convertMsg(output);//assume computeInputOutput done first
      r.forEach((key, value) -> {
   simulatorProxy cn  = (simulatorProxy)modelToSim.get(key);
   if(cn!=null) cn.putMessages(value);
   else srvr.putMyMessages(value);     // this goes to the root server
   });
  }
  }


  public /*synchronized */void sendInput() {
  String s = input.toString();
  if (s != "")
     sendMsg(s);
  else sendMsg("none");
  input = new message();
   srvr.done();
  }


  public /* synchronized */ String readMsg(){
         try {
            String MsgLine = dis.readLine();   //read your own client's msg
            System.out.println( MsgLine );
            return MsgLine;
                }
             catch( IOException e ) {
             System.out.println( e.toString() );
             System.exit( -1 );
                }
   return " ";
  }



public /* synchronized */ void sendMsg( String sMsg ) {
            ps.println( sMsg );
        }


}

