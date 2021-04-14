/* 
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package model.modeling;

import GenCol.Pair;
import GenCol.entity;
import model.simulation.CoupledSimulatorInterface;
import model.simulation.coordinator;
import util.classUtils.DevsClassField;
import util.classUtils.DevsClassFieldFactory;

public class digraph extends devs implements Coupled
{
    protected coordinator coordinator;
    protected Components components;
    protected couprel cp;

    public digraph(String nm)
    {
        super(nm);
        components = new Components();
        cp = new couprel();
    }

    @Override
    public void add(IODevs iod)
    {
        if (iod instanceof atomic)
        {
            atomic atomicModel = (atomic) iod;
            atomicModel.setStates(
                DevsClassFieldFactory.createDevsClassFieldMapWithFilter(
                    iod.getClass(),
                    atomicModel,
                    (DevsClassField f) -> {
                        return f.isState();
                    }
                )
            );
        }
        components.add(iod);
        ((devs) iod).setParent(this);
    }

    public void remove(IODevs d)
    {
        components.remove(d);
    }

    @Override
    public void addCoupling(IODevs src, String p1, IODevs dest, String p2)
    {
        cp.add((entity) src, new port(p1), (entity) dest, new port(p2));
    }

    @Override
    public IODevs withName(String nm)
    {
        Object[] args = { nm };
        IODevs model = components.whichOne("equalName", new Class<?>[] { String.class }, args);
        return model;
    }

    @Override
    public ComponentsInterface<IODevs> getComponents()
    {
        return components;
    }

    @Override
    public couprel getCouprel()
    {
        return cp;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        this.getComponents().forEach((IODevs iod) -> {
            sb.append(" ").append(iod.toString());
        });
        
        return sb.toString();
    }

    @Override
    public void showState()
    {
        components.tellAll("showState");
    }

    @Override
    public void initialize()
    {
        components.tellAll("initialize");
    }

    // added for DSDEVS

    public void addPair(Pair<Object, Object> cs, Pair<Object, Object> cd)
    {
        cp.put(cs, cd);
    }

    public void removePair(Pair<Object, Object> cs, Pair<Object, Object> cd)
    {
        cp.remove(cs, cd);
    }

    public void addInport(String modelName, String port)
    {
        // s.s("Inside digraph addInport");
        digraph P = (digraph) getParent();
        if (P != null)
            ((IODevs) P.withName(modelName)).addInport(port);
        else
            System.out.print("parent is not defined");
        addInportHook(modelName, port);
    }

    public void addOutport(String modelName, String port)
    {
        // s.s("Inside digraph addOutport");
        digraph P = (digraph) getParent();
        if (P != null)
            ((IODevs) P.withName(modelName)).addOutport(port);
        else
            System.out.print("parent is not defined");
        addOutportHook(modelName, port);
    }

    public void addInportHook(String modelName, String port)
    {
        // s.s("Inside digraph addInport hook 1");
        System.out.print("Inport added: " + port + "      component: " + modelName);
    }

    public void addOutportHook(String modelName, String port)
    {
        // s.s("Inside digraph addInport hook 1");
        System.out.print("Inport added: " + port + "      component: " + modelName);
    }

    public boolean checkNameUniqueness(String modelName)
    {
        return !this.components.containsName(modelName);
    }

    @Override
    public void setSimulator(CoupledSimulatorInterface sim)
    {
    }

    @Override
    public ActivityInterface getActivity()
    {
        return new activity("name");
    }

    @Override
    public void deltext(double e, MessageInterface<Object> x)
    {
    }

    @Override
    public void deltcon(double e, MessageInterface<Object> x)
    {
    }

    @Override
    public void deltint()
    {
    }

    @Override
    public MessageInterface<Object> Out()
    {
        return new message();
    }

    @Override
    public double ta()
    {
        return 0;
    }

    @Override
    public void setCoordinator(coordinator coordinator_)
    {
        coordinator = coordinator_;
    }

    @Override
    public coordinator getCoordinator()
    {
        return coordinator;
    }
}
