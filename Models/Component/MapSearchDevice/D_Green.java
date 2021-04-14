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

public class D_Green extends ViewableAtomic {

	protected double ta_out;

	public D_Green() {
		this("Green", 1);
	}

	public D_Green(String name, double ta_out) {
		super(name);
		this.ta_out = ta_out;
		addInport("stop");
		addOutport("outGreen");
	}

	public void initialize() {
		holdIn("active", 1);
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
		}

	}

	public message out() {

		// System.out.println(name+" out count "+count);

		message m = new message();
		if (phaseIs("active")) {
			content con = makeContent("outGreen", new entity("True"));
			m.add(con);
		}

		return m;
	}

}
