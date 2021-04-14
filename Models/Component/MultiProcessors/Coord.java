/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.MultiProcessors;

import Component.BasicProcessor.*;
import model.modeling.*;
import model.simulation.*;
import view.simView.*;



public class Coord extends proc{

public Coord(String name)
{
super(name,100);
addInport("setup");
addInport("x");
addOutport("y");
}

public Coord(){this("Coord");}

public void initialize(){
    super.initialize();
}

//protected void add_procs(proc p){
    protected void add_procs(devs p){
System.out.println("Default in Coord is being used");
}
}