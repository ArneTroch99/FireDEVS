/*Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import GenCol.EntityInterface;
import GenCol.entity;



public abstract class devs extends entity implements IODevs{
//IOBasicDevs,EntityInterface{

//protected
protected IODevs myparent;
public messageHandler mh;

public devs(String name){
super(name);
mh = new messageHandler();
}


public void addInport(String portName){mh.addInport(portName);}
public void addOutport(String portName){mh.addOutport(portName);}

public void removeInport(String portName){mh.removeInport(portName);}
public void removeOutport(String portName){mh.removeOutport(portName);}

public ContentInterface makeContent(PortInterface port,EntityInterface value)
              {return mh.makeContent(port, value); }
public content makeContent(String p,entity value){  //for use in usual devs
return new content(p,value);
}
public boolean   messageOnPort(MessageInterface x, PortInterface port, ContentInterface c)
            {return mh.messageOnPort(x,port,c);}

public boolean   messageOnPort(message x,String p, int i){ //for use in usual devs
return x.onPort(p,i);
}

abstract public  void initialize();
/**Returns the messageHandler of this Model.
 *@return The messageHandler of this Model.
 */
public messageHandler getMessageHandler()
{
    return mh;
}

/**Contains the Lists of possible inputs for each port*/
private Map<String, List<TestInput>> testInputMap = new HashMap<String, List<TestInput>>();

/**
 * Adds this value to the list of possible inputs for the given Port name.
 * Assumes zero as the amount of simulation units to wait
 *
 * @param   portName    The name of the port on which to inject the value.
 * @param   value       The value to inject.
 */
public void addTestInput(String portName, entity value)
{
    addTestInput(portName, value, 0);
}
/**
 * Adds this value to the list of possible inputs for the given Port name.
 *
 * @param   portName    The name of the port on which to inject the value.
 * @param   value       The value to inject.
 * @param   e           The amount of simulation units to wait before
 *                      injecting the value.
 */
public void addTestInput(String portName, entity value, double e)
{
    TestInput testInput = new TestInput(portName, value, e);
    List<TestInput> inputList = testInputMap.get(portName);
    if (inputList == null)
    {
       inputList = new ArrayList<TestInput>();
       testInputMap.put(portName, inputList);
    }
    inputList.add(testInput);
}

/**
 * Returns the list of TestInput's for the given port name.
 *@param portName The name of the port.
 *@return List of TestInput objects.
 */
public List<TestInput> getTestInputsForPort(String portName)
{
    return testInputMap.get(portName);
}

public void setParent(IODevs p) { 
	myparent = p;
	}

public IODevs getParent(){

	return  myparent;
}

}

