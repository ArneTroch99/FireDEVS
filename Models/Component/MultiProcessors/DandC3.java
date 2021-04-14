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
import view.modeling.ViewableDigraph;

public class DandC3 extends ViewableDigraph{

public DandC3()
{
    super("dandc3");
 make(6000,3);
 addTestInput("in", new entity("job"));
}

public DandC3(String name,double proc_time)
{
 super(name);

 make(proc_time,3);
}


 private void make(double proc_time,int size){

    addInport("in");
    addOutport("out");

   divideCoord co  = new divideCoord("DCco");
   add(co);

 addCoupling(this, "in", co, "in");
 addCoupling(co,"out",this,"out");


    proc  p1 = new proc("proc1", proc_time/size);
    proc  p2 = new proc("proc2", proc_time/size);
    proc  p3 = new proc("proc3", proc_time/size);

    add(p1);
    add(p2);
    add(p3);

    co.add_procs(p1);
    co.add_procs(p2);
    co.add_procs(p3);

    addCoupling(co, "y", p1,"in");
    addCoupling(p1,"out",co,"x");
    addCoupling(co, "y", p2,"in");
    addCoupling(p2,"out",co,"x");
    addCoupling(co, "y", p3,"in");
    addCoupling(p3,"out",co,"x");

initialize();

    preferredSize = new Dimension(605, 183);
    co.setPreferredLocation(new Point(161, 19));
    p1.setPreferredLocation(new Point(-17, 112));
    p2.setPreferredLocation(new Point(162, 112));
    p3.setPreferredLocation(new Point(336, 112));

}

}