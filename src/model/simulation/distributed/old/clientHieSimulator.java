/*  
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.simulation.distributed.old;


import java.util.*;

import  java.net.*;
import  java.io.*;

import GenCol.*;


import model.modeling.*;
import model.simulation.*;

public class clientHieSimulator extends coupledCoordinator
               implements CoupledCoordinatorInterface,Runnable {

   private Socket             s;
   private DataInputStream    dis;
   private DataOutputStream   dos;
   private PrintStream        ps;
   private String             sMsgLine;
   private Thread             thrRead;
   private boolean            terminate = false;
   protected double lastTime,currentTime;
//   protected long lastTime,currentTime;

/*
* Same as clientSimulator except for the inheritance from coupledRTSimulator
* Lack of Multiple inheritance forces choice of one or the other parent
*/

public clientHieSimulator(Coupled c){
super(c);
if( !connect() ) {
 return;
}
thrRead = new Thread( this );
thrRead.start();
}

public synchronized void waitForNextTN(){  //from proxy
  String s = readMsg();
  if (s.startsWith("nextTN")){
  double t = nextTN();
  if (t == DevsInterface.INFINITY)t = 1E20; //can't send infinity
  sendMsg(""+t);
  }
  else System.out.println("did not receive nextTN");
  }

  public  synchronized void waitForComputeInputOutput(){  //from proxy
  String s = readMsg();
   int index = s.indexOf(":");
   if (index > -1){
   currentTime = Double.parseDouble(s.substring(index+1,s.length()));
//   currentTime = Long.getLong(s.substring(index+1,s.length()));
   computeInputOutput(currentTime);
   if (!output.isEmpty()) sendMsg(output.toString());
   else sendMsg("none");
  }
  else System.out.println("did not receive computeInputOutput:");
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

/*  public  synchronized message interpret(String s){ //single port expected for now
    if (s == null || s.startsWith("none"))
       return new message();

      //shortcuts will always kill you
      //need trim to remove the blank (which cannot be removed by index)
   int index1 = s.indexOf(": ");
   int index2 = s.indexOf(" v");
   String p = s.substring(index1+1,index2).trim();
   message m = new message();
   m.add(new content(p,new entity()));
 //  m.print();
   return m;
   }
*/

  public  synchronized void waitForInput(){  //from proxy
  String s = readMsg();
  message m = interpret(s);
  if (m != null){
    input = m;      //Xiaolin Hu, Jul 11, 2001
    wrapDeltFunc(currentTime);
//  wrapDeltfunc(currentTime,m);
  }
  showModelState();
  System.out.println("client Simulator's input " + s);
  }

  public  synchronized void waitForContinue(){  //from proxy
  String s = readMsg();
  if (s.startsWith("terminate")) {
   terminate = true;
  System.out.println("client Simulator ends");
  }
  }

public synchronized void waitForInitialize(){ // from proxy
   String s = readMsg();
   int index = s.indexOf(":");
   if (index > -1){
   currentTime = Double.parseDouble(s.substring(index+1,s.length()));
//   currentTime = Long.getLong(s.substring(index+1,s.length()));
   initialize(currentTime);
  }
}

 public void run() {
   try{
   Thread.sleep(2000);   //wait for proxy to be created
   }catch(Exception e){}
//   currentTime = System.currentTimeMillis()/1000;
//   initialize(currentTime);
   sendMsg(myCoupled.getName());
   waitForInitialize();
   waitForNextTN();
 while(!terminate){
  waitForContinue();
  waitForComputeInputOutput();
  waitForInput();
  waitForNextTN();

  }
 }


private  synchronized void sendMsg( String sMsg ) {
ps.println( sMsg );  // ps is PrintStream object for the socket.
System.out.println(myCoupled.getName() + " sends " +sMsg);
}

public  synchronized String readMsg(){
      try {
           String MsgLine = dis.readLine();   //read your own client's msg
//           System.out.println( sMsgLine );
           return MsgLine;
                }
           catch( IOException e ) {
           System.out.println( e.toString() );
           System.exit( -1 );
                }
return " ";
}



// Tries to connect: returns true if successful, false if not

private  synchronized boolean connect() {
 System.out.println(myCoupled.getName() + " starting !!" );
      try {
         Thread.sleep(1000);    //wait a while before trying again
         s = new Socket( InetAddress.getLocalHost(), 7000 );
         if  ( s == null )
            return false;
         System.out.println("Connected!\n" );
      }
      catch( UnknownHostException e ) {
         return false;
      }
      catch( IOException e ) {
         return false;
      }
      catch( Exception e ) {
         return false;
      }

      try {
         // Socket was created.  Now create IO streams from it.
         dis = new DataInputStream( s.getInputStream() );
         dos = new DataOutputStream( s.getOutputStream() );
         ps  = new PrintStream( dos );
         System.out.println( "Open was successful!\n" );
      }
      catch( IOException e ) {
        return connect(); //try to connect until successful
      }
      return true;
}





}  // End of clientSimulator class