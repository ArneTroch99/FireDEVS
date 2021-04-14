/*  
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;

import java.util.Collection;

import GenCol.ensembleBag;

public interface MessageInterface<T> extends Collection<T>{
public boolean onPort(PortInterface port, ContentInterface c);
public Object getValOnPort(PortInterface port,ContentInterface c);
public void print();
/* examples of using ensembleBag approach */
//public ensembleBag getPortNames();
public ensembleBag<T> valuesOnPort(String portName);

// Jeff
ContentIteratorInterface mIterator();
// Jeff
}

