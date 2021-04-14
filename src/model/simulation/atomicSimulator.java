/*
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.simulation;

import GenCol.EntityInterface;
import model.modeling.DevsInterface;
import model.modeling.IOBasicDevs;
import model.modeling.MessageInterface;
import model.modeling.PortInterface;
import model.modeling.message;
import model.modeling.port;
import util.Logging;

public class atomicSimulator
             implements AtomicSimulatorInterface {//for usual devs

protected double tL,tN;
public MessageInterface<Object> input,output, inputForTimeView, outputForTimeView;
protected IOBasicDevs myModel;

public atomicSimulator(){}

public atomicSimulator(IOBasicDevs atomic){
myModel = atomic;
input = new message();
output = new message();
inputForTimeView = new message();
outputForTimeView = new message();
}

public double nextTN(){
return tN;
}

public boolean  equalTN(double t){return t == tN;}

public double getTN(){
return tN;
}

public double getTL(){
return tL;
}

public synchronized MessageInterface<Object>  getOutput(){return output;}

public synchronized MessageInterface<Object>  getInput(){return input;}

/*
 * Add the outputForTimeView/inputForTimeView as an extra message container showing input and output for timeView. Chanded by Chao. 12/1/2017
 */
public synchronized MessageInterface  getOutputForTimeView(){return outputForTimeView;}

public synchronized MessageInterface  getInputForTimeView(){return inputForTimeView;}

public Double nextTNDouble(){
return new Double(nextTN());
}

public synchronized void showModelState(){
myModel.showState();
}

public  synchronized void initialize(){ //for non real time usage, assume the time begins at 0
 myModel.initialize();
 tL = 0.0;
 tN = myModel.ta();
 Logging.log("INITIALIZATION, time: " + tL +", next event at: "+tN,
    Logging.full);
 myModel.showState();
 }

public synchronized  void  initialize(Double d){
initialize(d.doubleValue());
}

public  synchronized void initialize(double currentTime){     // for real time usage
 myModel.initialize();
 tL = currentTime;
 tN = tL + myModel.ta();
 Logging.log("INITIALIZATION, time: " + tL +", next event at: "+tN,
    Logging.full);
 myModel.showState();
 }

/*
 * The input and output for timeView will be reset after the
 * postComputeInputOutputHook() function called.
 */
public void resetIOforTimeView(){
	inputForTimeView = new message();	
	outputForTimeView = new message();
}


public synchronized void DeltFunc(Double d){
DeltFunc(d.doubleValue());
}
public  synchronized void DeltFunc(double t){
  wrapDeltfunc(t,new message());
}

public  synchronized void  wrapDeltfunc(double t){
 wrapDeltfunc(t,input); //changed to work with activity
 input = new message();
}

@Override
public  synchronized void  wrapDeltfunc(double t,MessageInterface<Object> x){
 if(x == null){
    System.out.println("ERROR RECEIVED NULL INPUT  " + myModel.toString());
    return;
  }
  if (x.isEmpty() && !equalTN(t)) {
    return;
  }
  else if((!x.isEmpty()) && equalTN(t)) {
    double e = t - tL;
    myModel.deltcon(e,x);
  }
  else if(equalTN(t)) {
    myModel.deltint();
  }
  else if(!x.isEmpty()) {
    double e = t - tL;
    myModel.deltext(e,x);
  }
  wrapDeltfuncHook2();
  tL = t;
  tN = tL + myModel.ta();
}

public  void computeInputOutput(Double d){
computeInputOutput(d.doubleValue());
}

public  void computeInputOutput(double t){
      if(equalTN(t)) {
          output = myModel.Out();
          outputForTimeView = myModel.Out();
      }
      else{
        output = new message();//bpz
      }

      computeInputOutputHook1();
}

public void  simulate(int numIter)
{
  int i=1;
  tN = nextTN();
  while( (tN < DevsInterface.INFINITY) && (i<=numIter) ) {
    Logging.log("ITERATION " + i + " ,time: " + tN, Logging.full);
    computeInputOutput(tN);
    showOutput();
    DeltFunc(tN);
    tL = tN;
    tN = nextTN();
    showModelState();
    i++;
  }
  System.out.println("Terminated Normally at ITERATION " + i + " ,time: " + tN);
}

public void simulate(Integer i){
simulate(i.intValue());
}

public synchronized void  showOutput(){
 if (output == null) return;
// System.out.println("Ouput -------------->");
 if(!output.isEmpty())
        output.print();
}

public MessageInterface<Object> makeMessage(){return new message();}

public void simInject(double e,PortInterface p,EntityInterface value){
MessageInterface<Object> m = makeMessage();
m.add(myModel.makeContent(p,value));
simInject(e,m);
}

@Override
public void simInject(double e,String portName,EntityInterface value){
                                            //  for use in usual devs
simInject(e,new port(portName),value);
}

@Override
public void simInject(double e, MessageInterface<Object> m){
double t = tL+e;
if (e <= myModel.ta()){
simInjectHook1(e);
wrapDeltfunc(t,m);
System.out.println("Time: " + t +" ,input injected: " );
m.print();
myModel.showState();
simInjectHook2(t);
}
else System.out.println("Time: " + tL+ " ,ERROR input rejected : elapsed time " + e +" is not in bounds.");
}

    /**
     * A hook used by the SimView package.
     */
    protected void wrapDeltfuncHook2() {}

    /**
     * A hook used by the SimView package.
     *
     * @param   waitTime        The amount of simulation time to wait before
     *                          injecting the input.
     */
    protected void simInjectHook1(double e) {}

    /**
     * A hook used by the SimView package.
     *
     * @param   newTime     The new current simulation time.
     */
    protected void simInjectHook2(double newTime) {}

    /**
     * A hook used by the SimView package.
     */
    protected void computeInputOutputHook1() {}

    public IOBasicDevs getModel() {return myModel;}
}
