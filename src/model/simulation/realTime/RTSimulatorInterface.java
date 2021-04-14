/*   
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.simulation.realTime;


import java.util.*;

import GenCol.*;


import model.simulation.*;


public interface RTSimulatorInterface extends AtomicSimulatorInterface,Runnable{

public long timeInSecs();
public long timeInMillis();

public void setTN();
public double getTN();

//public void setCurrentTime();
//public double getCurrentTime();
//public void setLastTime();
//public double getLastTime();

public void stopSimulate();

public void sendMessages();
}


