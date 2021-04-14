/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package Component.MultiProcessors;

import model.modeling.*;
import model.simulation.*;
import view.simView.*;

import java.awt.*;

import Component.BasicProcessor.*;
import GenCol.*;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableDigraph;


public class pipeSimple extends ViewableDigraph{

public pipeSimple(){this("pipeSimple", 90);}

public pipeSimple(String name,double proc_time)
{
 super(name);
 make(proc_time);

 addInport("in");
 addOutport("out");

 addTestInput("in", new entity("job"));
}


 private void make(double proc_time){


    ViewableAtomic  p0 = new proc("proc0", proc_time/3);
    ViewableAtomic  p1 = new proc("proc1", proc_time/3);
    ViewableAtomic  p2 = new proc("proc2", proc_time/3);


    add(p0);
    add(p1);
    add(p2);


    addCoupling(this, "in", p0, "in");
    addCoupling(p0,"out",p1,"in");
    addCoupling(p1,"out",p2,"in");
    addCoupling(p2,"out",this,"out");

initialize();

    preferredSize = new Dimension(586, 90);
    p0.setPreferredLocation(new Point(-4, 19));
    p1.setPreferredLocation(new Point(163, 19));
    p2.setPreferredLocation(new Point(333, 19));
}

}