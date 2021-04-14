/*  
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.simulation;

import GenCol.EntityInterface;
import model.modeling.MessageInterface;

//what is the core requirement on legacy models if they
//are just required to have a message interface
//but not necessarily a Devs structure

//standalone simulator

public interface coreSimulatorInterface{

public void initialize();
public Double nextTNDouble();
public void computeInputOutput(Double d);
public void DeltFunc(Double d);
public MessageInterface<Object> getOutput();
public void simulate(int numIter);
}






