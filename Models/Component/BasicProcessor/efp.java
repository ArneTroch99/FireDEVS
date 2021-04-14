/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.BasicProcessor;

import java.awt.*;

import Component.BasicProcessor.*;
import Component.MultiProcessors.*;
import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;
import view.modeling.ViewableAtomic;

import view.simView.*;


public class efp extends ViewableDigraph {


public efp (){
    super("efp");
   ViewableAtomic sp = new proc("basicProcessor", 25);

    ViewableDigraph  expf = new ef("ExpFrame", 10, 1000);
 


    add(expf);

    add(sp);

    addOutport("out");
    addTestInput("start",new entity());

    addCoupling(this,"start",expf,"start");
    addCoupling(this,"stop",expf,"stop");

    addCoupling(expf,"out",sp,"in");
    addCoupling(sp,"out",expf,"in");
    
    addCoupling(expf,"out",this,"out");

   initialize();

    preferredSize = new Dimension(549, 181);
    expf.setPreferredLocation(new Point(239, 23));
    sp.setPreferredLocation(new Point(15, 50));
 }
    /**
     * Automatically generated by the SimView program.
     * Do not edit this manually, as such changes will get overwritten.
     */
    public void layoutForSimView()
    {
        preferredSize = new Dimension(606, 325);
        ((ViewableComponent)withName("basicProcessor")).setPreferredLocation(new Point(176, 253));
        ((ViewableComponent)withName("ExpFrame")).setPreferredLocation(new Point(60, 42));
    }
 }
