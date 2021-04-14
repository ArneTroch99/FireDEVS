/*     
 *    
 *  Author     : Savitha and Anindita ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 15-April-2012
 */
package SimpArcModTestArtifacts;

import GenCol.entity;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class Fake_proc extends ViewableAtomic
{// ViewableAtomic is used instead
    // of atomic due to its
    // graphics capability
    protected entity job;
    protected double processing_time;

    public Fake_proc()
    {
        this("Fake_proc", 10);
    }

    public Fake_proc(String name, double Processing_time)
    {
        super(name);
        addInport("in");
        addOutport("out");
        addInport("none"); // allows testing for null input
                           // which should cause only "continue"
        processing_time = Processing_time;
        addTestInput("in", new entity("job1"));
        addTestInput("in", new entity("job2"), 20);
        addTestInput("none", new entity("job"));
    }

    public void initialize()
    {
        phase = "passive";
        sigma = INFINITY;
        job = new entity("job");
        super.initialize();
    }

    public void deltext(double e, message x)
    {
        Continue(e);

        System.out.println("The elapsed time of the processor is" + e);
        System.out.println("*****************************************");
        System.out.println("external-Phase before: " + phase);

        if (phaseIs("passive"))
            for (int i = 0; i < x.getLength(); i++)
                if (messageOnPort(x, "in", i))
                {
                    job = x.getValOnPort("in", i);
                    holdIn("busy", 10);
                    System.out.println("processing tiem of proc is" + processing_time);
                }

        System.out.println("external-Phase after: " + phase);
    }

    public void deltint()
    {
        System.out.println("Internal-Phase before: " + phase);
        passivate();
        job = new entity("none");
        System.out.println("Internal-Phase after: " + phase);
    }

    public void deltcon(double e, message x)
    {
        System.out.println("confluent");
        deltint();
        deltext(0, x);
    }

    public message out()
    {
        message m = new message();
        if (phaseIs("busy"))
        {
            m.add(makeContent("out", job));
        }
        return m;
    }

    public void showState()
    {
        super.showState();
        // System.out.println("job: " + job.getName());
    }

    public String getTooltipText()
    {
        return super.getTooltipText() + "\n" + "job: " + job.getName();
    }
}
