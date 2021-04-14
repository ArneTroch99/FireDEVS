/*
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 



package model.simulation;

import java.util.Map;

import GenCol.Pair;
import GenCol.Relation;
import model.modeling.ContentInterface;
import model.modeling.Coupled;
import model.modeling.IOBasicDevs;
import model.modeling.MessageInterface;
import model.modeling.coupledDevs;

public interface CoordinatorInterface extends AtomicSimulatorInterface{
public void setSimulators();
public void addSimulator(IOBasicDevs comp);
public void addCoordinator(Coupled comp);
public coupledDevs getCoupled();
public void addExtPair(Pair cs,Pair cd) ;
public void informCoupling();
public void showCoupling();
public Map<String, Iterable<ContentInterface>> convertInput(MessageInterface<Object> x);
public void putMyMessages(Iterable<ContentInterface> msgs);
public void sendDownMessages();
//public void decrement();


}
