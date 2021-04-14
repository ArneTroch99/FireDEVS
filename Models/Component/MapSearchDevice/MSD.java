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

public class MSD extends ViewableAtomic {
	protected entity value, updateStartValue;
	protected double ta_search, ta_update, ta_interrupt, ta_update_left, clock;

	public MSD() {
		this("MSD", 2, 7, 0.3);
	}

	public MSD(String name, double ta_search, double ta_update, double ta_interrupt) {
		super(name);
		this.ta_search = ta_search;
		this.ta_update = ta_update;
		this.ta_interrupt = ta_interrupt;

		ta_update_left = 0;
		clock = 0;

		addInport("inGreen");
		addInport("inRed");
		addOutport("outGreen");
		addOutport("outRed");
	}

	public void initialize() {
		phase = "waiting";
		sigma = INFINITY;
		super.initialize();
	}

	public void deltext(double e, message x) {

		clock = clock + e;
		Continue(e);

		for (int i = 0; i < x.getLength(); i++) {
			if (messageOnPort(x, "inGreen", i)) {
				if (phaseIs("updating")) {
					holdIn("searching", ta_search);
					if(ta_update_left > 0)
					ta_update_left = ta_update_left - e + ta_interrupt;

				} else if (phaseIs("waiting")) {

					holdIn("searching", ta_search);
				}
			} else if (messageOnPort(x, "inRed", i)) {

				value = x.getValOnPort("inRed", i);

				if (phaseIs("waiting") & value.toString().contains("True")) {
					updateStartValue = value;
					holdIn("updating", ta_update);
					ta_update_left = ta_update;

				} else if (phaseIs("updating") & value.toString().equalsIgnoreCase("False")) {

					holdIn("waiting", INFINITY);
					ta_update_left = 0;

				} else if (phaseIs("searching") & value.toString().equalsIgnoreCase("False")) {

					ta_update_left = 0;

				}
				else{
					
					holdIn(phase,sigma);
					if(ta_update_left > 0)
					ta_update_left = ta_update_left - e;
				}
			}
		}
		
		System.out.println("Ta_update_left:" + ta_update_left);
	}

	public void deltint() {
		clock = clock + sigma;
		if (phaseIs("updating")) {
			passivateIn("waiting");
			ta_update_left = 0;

		} else if (phaseIs("searching")) {

			if (ta_update_left > 0) {
				holdIn("updating", ta_update_left);

			} else {

				passivateIn("waiting");
			}
		}
	}

	public void deltcon(double e, message x) {
		deltint();
		deltext(0, x);
	}

	public message out() {
		message m = new message();
		if (phaseIs("searching")) {
			m.add(makeContent("outGreen", new entity("GreenLight")));
		} else if (phaseIs("updating")) {
			m.add(makeContent("outRed", updateStartValue));
			//m.add(makeContent("updateTime", new entity(""+ (clock-time_for_update))));
		}
		return m;
	}
}


