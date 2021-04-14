/*     
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;


import java.util.*;

import GenCol.*;


import model.simulation.*;

public interface ActivityInterface extends Runnable{
public void setSimulator(CoupledSimulatorInterface sim);
public double getProcessingTime();
public String getName();
public void kill();
public void start();
public entity computeResult();
}