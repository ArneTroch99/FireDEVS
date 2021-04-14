/*  
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;


import java.util.*;
import java.awt.Color;

import GenCol.*;







/*
interface coupledDevs extends EntityInterface{

public void add(IODevs d);
public void addCoupling(IODevs src, String p1, IODevs dest, String p2);

public IODevs withName(String nm);
public ComponentsInterface getComponents();
public couprel getCouprel();
}

*/


public interface DevsInterface extends AtomicInterface,Coupled{
public final double INFINITY = Double.POSITIVE_INFINITY;
}


