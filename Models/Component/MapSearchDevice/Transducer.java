/**
 * DEVS-Suite Simulator
 * Arizona Center for Integrative Modeling & Simulation
 * Arizona State University, Tempe, AZ, USA
 *
 * Author(s): H.S. Sarjoughian
 */

package Component.MapSearchDevice;

import java.util.HashMap;
import java.util.Map;

import GenCol.doubleEnt;
import GenCol.entity;

import model.modeling.content;
import model.modeling.message;

import view.modeling.ViewableAtomic;

public class Transducer extends ViewableAtomic {
	protected int searched, updated;
	protected Map updateStartMap, updateEndMap;
	protected double clock, total_ta, observation_time;

	public Transducer(String name, double Observation_time) {
		super(name);

		addOutport("stop");
		addOutport("searchNumber");
		addOutport("updateNumber");
		addOutport("averageUpdatedTime");

		addInport("Tsearched");
		addInport("updated");
		addInport("updateStart");

		searched = 0;
		updated = 0;

		updateStartMap = new HashMap();
		updateEndMap = new HashMap();

		observation_time = Observation_time;

		initialize();
	}

	public Transducer() {
		this("transd", 37);
	}

	public void initialize() {
		phase = "active";
		sigma = observation_time;
		clock = 0;
		total_ta = 0;
		super.initialize();
	}


	public void deltext(double e, message x) {
		clock = clock + e;
		Continue(e);
		entity val;
		entity ent;
		for (int i = 0; i < x.size(); i++) {
			if (messageOnPort(x, "Tsearched", i)) {
				val = x.getValOnPort("Tsearched", i);
				searched++;
			} else if (messageOnPort(x, "updated", i)) {
				val = x.getValOnPort("updated", i);

				updated++;
				if (updateStartMap.containsKey(val.getName())) {
					ent = (entity) updateStartMap.get(val.getName());
					doubleEnt num = (doubleEnt) ent;
					double arrival_time = num.getv();

					double turn_around_time = clock - arrival_time;
					total_ta = total_ta + turn_around_time;
					updateEndMap.put(val.getName(), new doubleEnt(clock));
				}

			} else if (messageOnPort(x, "updateStart", i)) {
				val = x.getValOnPort("updateStart", i);
				updateStartMap.put(val.getName(), new doubleEnt(clock));
			}

		}
		show_state();
	}

	public void deltint() {
		clock = clock + sigma;
		passivate();
		show_state();
	}

	public message out() {
		message m = new message();
		content con1 = makeContent("searchNumber", new entity(" " + searched));
		content con2 = makeContent("stop", new entity("stop"));
		content con3 = makeContent("updateNumber", new entity(" " + updateEndMap.size()));
		content con4 = makeContent("averageUpdatedTime", new entity(" " + (total_ta / updated)));

		m.add(con1);
		m.add(con2);
		m.add(con3);
		m.add(con4);

		return m;
	}

	public void show_state() {
		System.out.println("searched: " + searched);
		System.out.println("updated: " + updateEndMap.size());
		System.out.println("average updated time: " + (total_ta / updated));
	}
}

