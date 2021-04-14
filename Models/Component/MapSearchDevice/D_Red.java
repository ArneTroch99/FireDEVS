/**
 * DEVS-Suite Simulator
 * Arizona Center for Integrative Modeling & Simulation
 * Arizona State University, Tempe, AZ, USA
 *
 * Author(s): H.S. Sarjoughian
 */

package Component.MapSearchDevice;

import GenCol.*;

import model.modeling.*;

import view.modeling.ViewableAtomic;

public class D_Red extends ViewableAtomic {

	protected double ta_out;
	protected int count = 1;

	public D_Red() {
		this("Red", 6.7);
	}

	public D_Red(String name, double ta_out) {
		super(name);
		this.ta_out = ta_out;
		addInport("stop");
		addOutport("outRed");
	}

	public void initialize() {
		holdIn("active", 4);
		super.initialize();
	}

	public void deltext(double e, message x) {
		Continue(e);
		for (int i = 0; i < x.getLength(); i++) {
			if (messageOnPort(x, "stop", i)) {
				passivate();
			}
		}

	}

	public void deltint() {

		if (phaseIs("active")) {

			holdIn("active", ta_out);
			count++;
		}

	}

	public message out() {

		// System.out.println(name+" out count "+count);

		message m = new message();
		if (phaseIs("active")) {
			content con;
			if (count % 5 == 0 && count != 0) {
				con = makeContent("outRed", new entity("False"));
			} else {
				con = makeContent("outRed", new entity("True"+count));
			}
			m.add(con);
		}

		return m;
	}

}


