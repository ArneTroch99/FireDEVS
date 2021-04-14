/*
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package model.simulation;

import java.util.*;

import GenCol.*;
import GenCol.Queue;
import model.modeling.*;
import util.*;

public class coupledCoordinator extends coordinator implements CoupledCoordinatorInterface
{

    protected CoupledCoordinatorInterface myParent;
    protected CoordinatorInterface myRootParent;

    public coupledCoordinator(Coupled c)
    {
        super(c);
    }

    public coupledCoordinator(Coupled c, boolean setSimulators)
    {
        super(c, setSimulators, null);
    }

    @Override
    public void addSimulator(IOBasicDevs comp)
    {
        coupledSimulator s = new coupledSimulator(comp);
        s.setParent(this); // set the parent
        simulatorCreated(s, comp);
        // later will download modelToSim to its children and then will be
        // updated by its parents
        // so after initialization, modelToSim store the brother models and
        // simulators
        // internalModelTosim store its children models and simulators
    }

    @Override
    public void addCoordinator(Coupled comp)
    {
        coupledCoordinator s = new coupledCoordinator(comp);
        s.setParent(this); // set the parent
        simulatorCreated(s, comp);
        // later will download modelToSim to its children and then will be
        // updated by its parents
        // so after initialization, modelToSim store the brother models and
        // simulators
        // internalModelTosim store its children models and simulators
    }

    @Override
    public void setParent(CoupledCoordinatorInterface p)
    {
        myParent = p;
    }

    @Override
    public CoupledCoordinatorInterface getParent()
    {
        return myParent;
    }

    @Override
    public void setRootParent(CoordinatorInterface p)
    {
        myRootParent = p;
    }

    @Override
    public CoordinatorInterface getRootParent()
    {
        return myRootParent;
    }

    @Override
    public void addPair(Pair<Object, Object> cs, Pair<Object, Object> cd)
    {
        coupInfo.add(cs, cd);
    }

    @Override
    public void removePair(Pair<Object, Object> cs, Pair<Object, Object> cd)
    {
        coupInfo.remove(cs, cd);
    }

    @Override
    public void showCoupling()
    {
        super.showCoupling();
        coupInfo.print();
    }

    @Override
    public void setModToSim(Function mts)
    {
        modelToSim = mts;
    }

    @Override
    public Map<String, Iterable<ContentInterface>> convertInput(MessageInterface<Object> x)
    {
        Map<String, Iterable<ContentInterface>> r = new HashMap<String, Iterable<ContentInterface>>();
        if (x.isEmpty())
        {
            return r;
        }
        ContentIteratorInterface cit = x.mIterator();
        while (cit.hasNext())
        {
            content co = (content) cit.next();
            Set<Object> s = extCoupInfo.translate(myCoupled.getName(), co.getPort().getName());
            s.forEach((Object o) -> {
                try
                {
                    @SuppressWarnings("unchecked")
                    Pair<Object, Object> cp = (Pair<Object, Object>) o;
                    String ds = (String) cp.getKey();
                    String por = (String) cp.getValue();
                    Object tempval = co.getValue();
                    content tempco = new content(por, (entity) tempval);
                    Queue<ContentInterface> q = (Queue<ContentInterface>) r.get(ds);
                    
                    if (q == null)
                    {
                        q = new Queue<ContentInterface>();
                        r.put(ds, q);
                    }
                    
                    q.add(tempco);

                    convertInputHook1(co, cp, tempco);
                }
                catch (ClassCastException e)
                {
                    e.printStackTrace();
                }
            });
        }
        return r;
    }

    public Map<String, Iterable<ContentInterface>> convertMsg(MessageInterface<Object> x)
    {
        Map<String, Iterable<ContentInterface>> r = new HashMap<String, Iterable<ContentInterface>>();
        if (x.isEmpty())
            return r;
        ContentIteratorInterface cit = x.mIterator();
        while (cit.hasNext())
        {
            ContentInterface co = cit.next();
            Set<Object> s = coupInfo.translate(myCoupled.getName(), co.getPort().getName());
            s.forEach((Object o) -> {
                try
                {
                    @SuppressWarnings("unchecked")
                    Pair<Object, Object> cp = (Pair<Object, Object>) o;
                    String ds = (String) cp.getKey();
                    String por = (String) cp.getValue();
                    Object tempval = co.getValue();
                    content tempco = new content(por, (entity) tempval);
                    Queue<ContentInterface> q = (Queue<ContentInterface>) r.get(ds);
                    
                    if (q == null)
                    {
                        q = new Queue<ContentInterface>();
                        r.put(ds, q);
                    }
                    
                    q.add(tempco);

                    convertMsgHook1((content) co, cp, tempco);
                }
                catch (ClassCastException e)
                {
                    e.printStackTrace();
                }
            });
        }
        return r;
    }

    protected void convertMsgHook1(content oldContent, Pair coupling, content newContent)
    {
        //No op in base implementation
        //Delegates to subclasses
    }

    @Override
    public void sendMessages()
    { // extend so they send message to its parent also
        MessageInterface<Object> o = getOutput();
        if (o != null && !o.isEmpty())
        {
            Map<String, Iterable<ContentInterface>> r = convertMsg((message) getOutput());// assume
                                                                           // computeInputOutput
                                                                           // done
                                                                           // first

            r.forEach((String key, Iterable<ContentInterface> msgs) -> {
                if (modelToSim.get(key) instanceof CoupledSimulatorInterface)
                {
                    CoupledSimulatorInterface sim = (CoupledSimulatorInterface) modelToSim.get(key);
                    sim.putMessages(msgs);
                }
                else if (modelToSim.get(key) instanceof CoupledCoordinatorInterface)
                {
                    CoupledCoordinatorInterface sim = (CoupledCoordinatorInterface) modelToSim.get(key);
                    sim.putMessages(msgs);
                }
                else
                { // this is an internal output coupling
                    CoupledCoordinatorInterface cci = getParent();
                    CoordinatorInterface ci = getRootParent();
                    if (cci != null)
                        myParent.putMyMessages(msgs);
                    else if (ci != null)
                        myRootParent.putMyMessages(msgs);
                }
            });
        }
    }

    @Override
    public void putMessages(Iterable<ContentInterface> msgs)
    {
        for(ContentInterface c : msgs)
        {
            input.add(c);
            /*
             * The input will be added to the inputForTimeView. It is separated from
             * regular input messages, which are used for simview and reset after
             * they are passed to next model.
             */
            inputForTimeView.add(c);
            // sendDownMessages();
            // It is a test to fix the bug that msg is not receiving from coupled
            // parent model.
        }
    }
    

    @Override
    public void sendDownMessages()
    {
        if (!input.isEmpty())
        {
            Map<String, Iterable<ContentInterface>> r = convertInput(input);

            r.forEach((String key, Iterable<ContentInterface> msgs) -> {
                if (internalModelTosim.get(key) instanceof CoupledSimulatorInterface)
                {
                    CoupledSimulatorInterface sim = (CoupledSimulatorInterface) internalModelTosim.get(key);
                    sim.putMessages(msgs);
                }
                else if (internalModelTosim.get(key) instanceof CoupledCoordinatorInterface)
                {
                    CoupledCoordinatorInterface sim = (CoupledCoordinatorInterface) internalModelTosim.get(key);
                    sim.putMessages(msgs);
                }
            });
        }
    }

    public void DeltFunc(Double d)
    {
        DeltFunc(d.doubleValue());
    }

    public void DeltFunc(double t)
    {
        wrapDeltFunc(t);
    }

    public void wrapDeltFunc(double time)
    {
        sendDownMessages();
        Class<?>[] classes = { Double.class };
        Object[] args = { new Double(time) };
        simulators.tellAll("DeltFunc", classes, args);
        input = new message();
        output = new message();
    }
	/*
	 * The input and output for timeView will be reset after the
	 * postComputeInputOutputHook() function called.
	 */
	public void resetIOforTimeView() {
		simulators.tellAll("resetIOforTimeView");
		inputForTimeView = new message();
		outputForTimeView = new message();
	}
    public void simulate(int num_iter)
    {
        int i = 1;
        tN = nextTN();
        while ((tN < DevsInterface.INFINITY) && (i <= num_iter))
        {
            Logging.log("ITERATION " + i + " ,time: " + tN, Logging.full);
            computeInputOutput(tN);
            showOutput();
            DeltFunc(tN);
            tL = tN;
            tN = nextTN();
            showModelState();
            i++;
        }
        System.out.println("Terminated Normally at ITERATION " + i + " ,time: " + tN);
    }

    /**
     * See parent method.
     */
    @Override
    public List<Object> getCouplingsToSourcePort(String portName)
    {
        return AtomicSimulatorUtil.getCouplingsToSourcePort(portName,
                myCoupled.getName(),
                coupInfo,
                extCoupInfo,
                modelToSim,
                internalModelTosim,
                (coordinator) myRootParent);
    }
}
