// Governor.java
// Written by Eric Helser
// April 11, 2009

// The purpose of this class is to act as a coordinator for the simulator
// and time views to keep everything in sync.

package controller;

import java.util.*;
import view.timeView.*;

public class Governor
{
	private static int cycle=30;	//milliseconds between "checks"
	private static boolean enabled=false;
	private static ArrayList<TimeView> views=new ArrayList<TimeView>(0);
	
	
	//Called whenever a new time view is created.
	//Add the time view to the list, so the Governor can watch it.
	public static void registerTimeView(TimeView t)
	{
		views.add(t);
	}
	
	//added
	
	
	//called by central coordinator after each step of the simulation.
	//The purpose of this function is to make sure no time views have
	//data left to plot before letting the simulation continue.
	//To do this, call this.checkView(). If it returns true, return
	//control to the sim. If not, wait "cycle" milliseconds and repeat.
	public static void syncGraphs()
	{
		if(!enabled)return;
		
		while(!checkView())
		{
			try{Thread.sleep(cycle);}
			catch(InterruptedException e){}
		}
	}

	//Called by syncGraphs. This function returns true iff ALL views are
	//up-to-date with their plotting, i.e. none of them have a GraphsScrollComponent
	//whose "next" array has a size >0.
	private static boolean checkView() 
	{
		for(int i=0; i<views.size(); i++)
			if(!views.get(i).checkTimeGraphs())
				return false;
		return true;
	}	
	
	//Called by Controller. Before clearing the views array, you must
	//manually stop the Timer threads created in each TimeView, otherwise
	//they cause a cpu leak.
	public static void reset()
	{
		for(int i=0; i<views.size(); i++)
		{
			views.get(i).clockStop();
		}
		views.clear();
		enabled=false;
	}
	
	//Called by controller whenever the user adjusts the third slider
	//on the main screen. The controller sends the value here, and the
	//Gov distributes the value received to all registered views.
	public static void setTV(double x)
	{
		for(int i=0; i<views.size(); i++)
			views.get(i).setTV(x);
	}
	
	//Received from controller- whenever the user clicks the checkbox,
	//that value ends up here.
	public static void setSelected(boolean x)
	{
		enabled=x;
	}	
}