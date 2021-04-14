/*  
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;

import java.util.*;

import GenCol.*;




interface atomicDevs {

public void Continue(double e);
public void passivate();
public void passivateIn(String phase);
public void holdIn(String phase, double time);
public void holdIn(String phase, double time, ActivityInterface a);
public boolean phaseIs(String phase);
//bpz
public double getSigma();
public void setSigma(double sigma);
}

