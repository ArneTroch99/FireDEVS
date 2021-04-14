/* 
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package Component.UtilModels;

import java.awt.Color;
import java.util.*;

import GenCol.*;


import model.modeling.*;
import model.simulation.*;

import view.modeling.ViewableAtomic;
import view.simView.*;


public class Clock extends ViewableAtomic
{
    protected double time, savedSigma;
    public Clock(String nm)
    {
        super(nm);
        //bpz
        setBackgroundColor(Color.pink);
        addInport("time?");
        addOutport("timeMark");
        addOutport("timeIs");
        addTestInput("time?", new entity(""), 0);
        addTestInput("time?", new entity(""), 5);
    }

    public Clock()
    {
        this("clock");
    }

    public void initialize()
    {
        super.initialize();
        time = 0;
        savedSigma = 0;
        holdIn("active", 10);
    }

    public void deltext(double e, message x)
    {
        Continue(e);
        time += e;
        if (phaseIs("active"))
            for (int i = 0; i < x.getLength(); i++)
                if (messageOnPort(x, "time?", i)) {
                    savedSigma = sigma;
                    holdIn("respond", 0);
                }
    }

    public void deltint()
    {
        time += sigma;
        if (phaseIs("active"))
            holdIn("active", 10);
        else if (phaseIs("respond"))
            holdIn("active", savedSigma);
    }

    public message out()
    {
        message m = new message();
        if (phaseIs("active"))
            m.add(makeContent("timeIs", new entity("to all : " + (time + sigma))));
        else if (phaseIs("respond"))
            m.add(makeContent("timeMark", new entity("to you : " + time)));
        return m;
    }

    public void showState()
    {
        super.showState();
        System.out.println("time :" + time);
    }

    public String stringState()
    {
        return
            super.stringState()
            + "\n" + "time :" + time;
    }
}
