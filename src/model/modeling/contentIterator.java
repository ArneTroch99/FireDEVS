/* Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;

import java.util.*;

import GenCol.*;




class contentIterator implements ContentIteratorInterface{
private Iterator it;
public contentIterator(MessageInterface m){it = m.iterator();}
public boolean hasNext(){return it.hasNext();}
public ContentInterface next() {return (ContentInterface)it.next();}

}