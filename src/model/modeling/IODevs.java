/*
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;


import java.util.*;

import GenCol.*;






public interface IODevs extends EntityInterface{

public void addInport(String portName);
public void addOutport(String portName);
public void removeInport(String portName);;
public void removeOutport(String portName);
public ContentInterface makeContent(PortInterface port,EntityInterface value);
public boolean   messageOnPort(MessageInterface<EntityInterface> x, PortInterface port, ContentInterface c);
}