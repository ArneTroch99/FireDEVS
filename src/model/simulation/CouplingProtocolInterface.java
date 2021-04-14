/*
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.simulation;

import GenCol.Function;
import GenCol.Pair;
import model.modeling.ContentInterface;

public interface CouplingProtocolInterface
{
    public void putMessages(Iterable<ContentInterface> msgs);

    public void sendMessages();

    public void setModToSim(Function mts);

    public void addPair(Pair<Object, Object> cs, Pair<Object, Object> cd); // coupling
                                                                           // pair

    public void removePair(Pair<Object, Object> cs, Pair<Object, Object> cd); // remove
                                                                      // coupling
                                                                      // pair

    public void showCoupling();
}
