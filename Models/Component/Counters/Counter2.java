package Component.Counters;

import Component.BasicProcessor.*;
import Component.SISO.*;
/**
 * 
 * @author Prasanth
 *
 */
public class Counter2 extends siso{
	double count;
	static final double stepTime = 10;

	/**
	 * 
	 */
	public Counter2(){
		super("Counter2");
		addInport("in");
		addOutport("out");
		AddTestPortValue(1);
		AddTestPortValue(2);
		AddTestPortValue(-1);
	}

	/**
	 * 
	 */
	public void initialize(){
		count  = 0;
		phase = "passive";
		super.initialize();
	}

	/**
	 * 
	 */
	public void  Deltext(double e,double input){
		Continue(e);
		if(input != 0 && phase.equalsIgnoreCase("passive")){
			//phase = "active";
			count = count + 1;
			holdIn("active",stepTime);

		}else{
			if(input == 0 && phase.equalsIgnoreCase("passive")){
				//phase = "respond";
				holdIn("respond",stepTime);
			}else if(input == 0 && phaseIs("active")){
				Continue(e);
				double sig2 = sigma;
				holdIn("respond",0);
				holdIn("active",sig2);
			}
		}
	}

	public void  deltint( ){
		passivate();
	}

	public double sisoOut(){
		if (phaseIs("respond")){
			System.out.println("count: " + count);
			return count;
		}
		else return 0;
	}

/*	public void showState(){
		super.showState();
		System.out.println("count: " + count);
	}*/

}
