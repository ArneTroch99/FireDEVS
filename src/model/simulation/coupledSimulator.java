/* 
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package model.simulation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import GenCol.EntityInterface;
import GenCol.Function;
import GenCol.Pair;
import GenCol.Queue;
import GenCol.Relation;
import GenCol.entity;
import model.modeling.ActivityInterface;
import model.modeling.ContentInterface;
import model.modeling.ContentIteratorInterface;
import model.modeling.DevsInterface;
import model.modeling.IOBasicDevs;
import model.modeling.MessageInterface;
import model.modeling.atomic;
import model.modeling.content;
import model.modeling.couprel;
import model.modeling.message;
import util.Logging;

public class coupledSimulator extends atomicSimulator implements CoupledSimulatorInterface
{
    protected Function modelToSim;
    protected couprel coupInfo;
    protected ActivityInterface myActivity = null;
    protected boolean activityDue = false;
    protected CoupledCoordinatorInterface myParent;
    protected CoordinatorInterface myRootParent;

    public coupledSimulator()
    {
        this(new atomic());
    }

    public coupledSimulator(IOBasicDevs devs)
    {
        super(devs);
        modelToSim = new Function();
        coupInfo = new couprel();
        myActivity = myModel.getActivity();
        myModel.setSimulator(this);
    }

    @Override
    public void setParent(CoupledCoordinatorInterface p)
    {
        myParent = p;
    }

    @Override
    public void setRootParent(CoordinatorInterface p)
    {
        myRootParent = p;
    }

    @Override
    public CoupledCoordinatorInterface getParent()
    {
        return myParent;
    }

    @Override
    public CoordinatorInterface getRootParent()
    {
        return myRootParent;
    }

    @Override
    public Double nextTNDouble()
    {
        return new Double(nextTN());
    }

    @Override
    public void DeltFunc(Double d)
    {
        DeltFunc(d.doubleValue());
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
        System.out.println("The coupling is: ");
        coupInfo.print();
    }

    @Override
    public void setModToSim(Function mts)
    {
        modelToSim = mts;
    }

    public synchronized Map<String, Iterable<ContentInterface>> convertMsg(MessageInterface<Object> x)
    {
        // for each content in the given message
        Map<String, Iterable<ContentInterface>> r = new HashMap<String, Iterable<ContentInterface>>();
        if (x.isEmpty())
        {
            return r;
        }

        ContentIteratorInterface cit = x.mIterator();
        while (cit.hasNext())
        {
            content co = (content) cit.next();

            // get destination, might more than one
            // for each coupling to this content's outport
            Set<Object> s = coupInfo.translate(myModel.getName(), co.getPort().getName());
            s.forEach((Object o) -> {
                try
                {
                    @SuppressWarnings("unchecked")
                    Pair<Object, Object> p = (Pair<Object, Object>) o;
                    content tempco = new content((String) p.getValue(), (entity) co.getValue());
                    String ds = (String) p.getKey();
                    Queue<ContentInterface> q = (Queue<ContentInterface>) r.get(ds);
                    
                    if (q == null)
                    {
                        q = new Queue<ContentInterface>();
                        r.put(ds, q);
                    }
                    
                    q.add(tempco);
                    convertMsgHook1(co, p, tempco, myModel.getName(), (String) p.getKey());
                }
                catch (ClassCastException e)
                {
                    e.printStackTrace();
                }
            });
        }
        return r;
    }

    @Override
    public void startActivity(ActivityInterface a)
    {
        a.setSimulator(this);
        myActivity = a;
        double completionTime = Math.random() * 2 * a.getProcessingTime();
        // this should be replaced by a user specifyable distribution
        // and way of determinng completionTime
        if (myModel instanceof atomic)
        {
            if (completionTime < ((atomic) myModel).getSigma())
            {
                ((atomic) myModel).setSigma(completionTime);
                activityDue = true;
            }
        }
        else
        {
            return;
        }
    }

    @Override
    public void returnResultFromActivity(EntityInterface result)
    {
        content c = new content("outputFromActivity", (entity) result);
        putMessages(Arrays.asList(c));
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
        }
    }

    @Override
    public void sendMessages()
    {
        if (activityDue)
        {
            returnResultFromActivity(myActivity.computeResult());
            activityDue = false;
        }
        MessageInterface<Object> o = getOutput();
        if (o != null && !o.isEmpty())
        {
            Map<String, Iterable<ContentInterface>> r = convertMsg((message) getOutput());// assume
                                                           // computeInputOutput
                                                           // done first
            r.forEach((String ds, Iterable<ContentInterface> msgs) -> {
                if (modelToSim.get(ds) instanceof CoupledSimulatorInterface)
                {
                    CoupledSimulatorInterface sim = (CoupledSimulatorInterface) modelToSim.get(ds);
                    sim.putMessages(msgs);
                }
                else if (modelToSim.get(ds) instanceof CoupledCoordinatorInterface)
                {
                    CoupledCoordinatorInterface sim = (CoupledCoordinatorInterface) modelToSim.get(ds);
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

public void DeltFunc(double t) {
   wrapDeltfunc(t,input);
   input = new message();
}

@Override
public void simulate(int num_iter)    // compare to atomicSimulator, add the sendMessages()
{
  int i=1;
  tN = nextTN();
  while( (tN < DevsInterface.INFINITY) && (i<=num_iter) ) {
     Logging.log("ITERATION " + i + " ,time: " + tN, Logging.full);
     computeInputOutput(tN);
     showOutput();
     sendMessages();
     DeltFunc(tN);
     tL = tN;
     tN = nextTN();
     showModelState();
     i++;
  }
  System.out.println("Terminated Normally at ITERATION " + i + " ,time: " + tN);
}

    /**
     * Returns a list of the ports (along with their components) to which
     * the given port is a source.
     *
     * @param   portName    The source port name on the source component.
     * @return              The list of couplings to the source port's
     *                      desination ports.
     */

    public List<Object> getCouplingsToSourcePort(String portName)
    {
        return AtomicSimulatorUtil.getCouplingsToSourcePort(portName,
            myModel.getName(), coupInfo, null, modelToSim,
            null, (atomicSimulator)myRootParent);
    }

    /**
     * A hook used by the SimView package.
     *
     * @param   oldContent      The content object that existed before
     *                          its traversal of the coupling.
     * @param   coupling        The coupling traversed by the content.
     * @param   newContent      The content object as it stands now
     *                          after the traversal.
     * @param   sourceComponentName
     *                          The name of the component at the beginning
     *                          of the coupling.
     * @param   destComponentName
     *                          The name of the component at the end
     *                          of the coupling.
     */
    protected void convertMsgHook1(content oldContent, Pair<Object, Object> coupling,
        content newContent, String sourceComponentName,
        String destComponentName) {}
}
