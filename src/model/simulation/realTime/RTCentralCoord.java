/*
 *Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7
 *  Date       : 08-15-02
 */

package model.simulation.realTime;

import java.util.*;

import GenCol.*;


import model.modeling.*;
import model.simulation.*;

public class RTCentralCoord extends coordinator
implements RTCoordinatorInterface{
	protected int numIter;
	protected Thread myThread;
	protected long timeToSleep;

	public long timeInSecs() {
		return (timeInMillis()/1000);
	}
	public long timeInMillis() {
		return System.currentTimeMillis();
	}


	public RTCentralCoord(){ //for ease of inheritance
	}

	public RTCentralCoord(coupledDevs c){this(c, true, null);}

	public RTCentralCoord(coupledDevs c, boolean setSimulators, Object dummyParameter){
		super(c, setSimulators, dummyParameter);
		myThread = new Thread(this);
	}

	public RTCentralCoord(coupledDevs c,boolean minimal){
		super(c,minimal);
		myThread = new Thread(this);

	}

	public void initialize(){
		tL = timeInMillis();
		Class [] classes  = { Double.class };
		Object [] args  = {new Double(tL/1000)};
		simulators.tellAll("initialize",classes,args);
	}

	public void  simulate(int numIter)
	{
		this.numIter = numIter;
		myThread.start();
	}

	public void tellAllStop(){
		simulators.tellAll("stopSimulate");
	}

//	public double getTN(){ return tN; }

	public void run(){
		int i=1;
		tN = nextTN()*1000;
		while( (tN < INFINITY) && (i<=numIter) ) {
			while(timeInMillis() < getTN() - 10){
				timeToSleep = (long)(getTN() - timeInMillis());
				System.out.println("Thread try to sleep for ==> " + timeToSleep+" milliseconds");
				if(timeToSleep >= 0) {
					try {
						myThread.sleep(timeToSleep);
					} catch (Exception e) { continue; }
				}
			}
			computeInputOutput(tN/1000);
			wrapDeltFunc(tN/1000);
			tL = tN;
			tN = nextTN()*1000;
			showModelState();
			i++;


		}

		System.out.println("Terminated Normally at ITERATION " + i + " ,time: " + tN);
	}

}
