/*  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;

import java.util.Objects;

//import org.eclipse.birt.chart.ui.swt.composites.IntegerSpinControl;

import GenCol.EntityInterface;
import GenCol.entity;

public class content extends entity implements ContentInterface,EntityInterface{
private port port;
private entity value;

public content(port p, entity value){
port = p;
this.value =  value;
}

public content(String portNm, entity value){
this(new port(portNm),value);
}

public PortInterface getPort(){return port;}

public String getPortName(){return port.getName();}

public Object getValue (){return value;}

public String toString(){
return "port: " + port.getName() + " value: "+ value.getName();
}

public void print(){
System.out.println(toString());
}


// An issue was occurring in Bag<>.contains that forcing a hashCode
// on the contents seems to fix.

@Override
    public int hashCode()
    {
        return Objects.hash(port, value);
    }
public boolean equals(Object o)
{
    if (o instanceof content) {
        content content = (content)o;
        return port.equals(content.port) && value.equals(content.value);
    }

    return false;
}

public boolean onPort(PortInterface port){
return port.equals(getPort());
}


public Object valueOnPort(String portName){
if (port.eq(portName))
return getValue();
else return null;
}
}

