/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package Component.OddsAndEnds;


import java.lang.*;

import Component.BasicProcessor.*;
import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.modeling.ViewableAtomic;
import view.simView.*;


public class classic extends ViewableAtomic{



  public classic(){
}

public classic(String name){
   super(name);
}


public content get_content(message x){
    entity ent = x.read(0);
    return (content)ent;
}

public void  Deltext(double e,content con){
}  //virtual for single input at a time

public void  deltext(double e,message x)
{

    Deltext(e,get_content(x));

}


}

