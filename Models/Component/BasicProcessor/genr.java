/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.BasicProcessor;

import java.awt.Color;
import java.lang.*;

import GenCol.*;

import model.modeling.*;
import model.simulation.*;

import view.modeling.ViewableAtomic;
import view.simView.*;

public class genr extends ViewableAtomic {

	protected double int_arr_time;
	protected int count;
	static int c = 0;

	public genr() {
		this("genr", 10);
	}

	public genr(String name, double Int_arr_time) {
		super(name);
		addInport("in");
		addOutport("out");
		addInport("stop");
		addInport("start");
		int_arr_time = Int_arr_time;

		addTestInput("start", new entity(""));
		addTestInput("stop", new entity(""));
		
		setBackgroundColor(Color.orange);
	}

	public void initialize() {
		holdIn("active", int_arr_time);

		// phase = "passive";
		// sigma = INFINITY;
		sigma = 0;
		count = 0;
		super.initialize();
	}

	public void deltext(double e, message x) {
		Continue(e);
//		System.out.println("******************** elapsed tiem for generator is " + e + "************************");
		if (phaseIs("passive")) {
			for (int i = 0; i < x.getLength(); i++)
				if (messageOnPort(x, "start", i)) {

					holdIn("active", int_arr_time);
				}
		}
		if (phaseIs("active"))
			for (int i = 0; i < x.getLength(); i++)
				if (messageOnPort(x, "stop", i))
					phase = "finishing";
	}

	public void deltint() {
		/*
		 * System.out.println(name+" deltint count "+count);
		 * System.out.println(name+" deltint int_arr_time "+int_arr_time);
		 * System.out.println(name+" deltint tL "+tL);
		 * System.out.println(name+" deltint tN "+tN);
		 */

		// System.out.println("********generator**********" + c);
		if (phaseIs("active")) {
			count = count + 1;

			holdIn("active", 10);
		} else
			passivate();
	}

	public message out() {

		// System.out.println(name+" out count "+count);

		message m = new message();
		content con = makeContent("out", new entity("job" + count));
		m.add(con);

		return m;
	}

	public void showState() {
		super.showState();
//		System.out.println("int_arr_t: " + int_arr_time);
	}

	public String getTooltipText() {
		return super.getTooltipText() + "\n" + " int_arr_time: " + int_arr_time + "\n" + " count: " + count;
	}

}
