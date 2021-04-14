/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package Component.MultiProcessors;

import model.modeling.*;
import model.simulation.*;
import view.simView.*;

import java.awt.Color;

import Component.*;
import GenCol.*;

public class multiServerCoord extends Coord
{

  @state(log = state.DEFAULT_CHECKED)
  private int jobsInProcess = 0; // number of jobs in the queue
  @state(log = state.DEFAULT_CHECKED)
  private String availableProcessors = "zero"; // number of processors

  protected Queue procs;
  protected Queue jobs;
  // protected proc pcur;
  protected message yMessage;

  public multiServerCoord(String name)
  {
    super(name);

    jobs = new Queue();
    procs = new Queue();
    setBackgroundColor(Color.ORANGE);
  }

  public multiServerCoord()
  {
    super("multiServerCoord");

    jobs = new Queue();
    procs = new Queue();

    // CAUTION: start with port "setup" to test

    addTestInput("setup", new entity(""));
    addTestInput("in", new entity("val")); // for receiving unprocessed jobs
    addTestInput(      // for receiving processed jobs
     "x",
      new Pair(new proc("p", 5000), new entity("val"))
    );

    // initialize();
  }

  public void initialize()
  {
    phase = "passive";
    sigma = INFINITY;
    job = null;
    jobsInProcess = 0;
    availableProcessors = String.valueOf(procs.size());
    super.initialize();
    ;
  }

  public void showState()
  {
    super.showState();
    System.out.println("number of jobs: " + jobs.size());
    System.out.println("number of procs: " + procs.size());
    System.out.println("number of available procs: " + jobsInProcess);
  }

  // protected void add_procs(proc p){ //BPZ 99 this cause problem
  protected void add_procs(devs p)
  {

    procs.add(p);
    // pcur = p;
  }

  public void deltext(double e, message x)
  {
    Continue(e);

    if (phaseIs("passive"))
    {
      for (int i = 0; i < x.size(); i++)
        if (messageOnPort(x, "setup", i))
          add_procs(new proc("p", 1000));
    }

    if (phaseIs("passive"))
    {
      yMessage = new message();
      for (int i = 0; i < x.size(); i++)
        if (messageOnPort(x, "in", i))
        {
          job = x.getValOnPort("in", i);

          if (!procs.isEmpty())
          {
            entity pcur = (entity) procs.first();   
            procs.remove();
 
            jobsInProcess = jobsInProcess + 1; // decrement the number of processors
            availableProcessors = "# of " + String.valueOf(procs.size()); 
            
            yMessage.add(makeContent("y", new Pair(pcur, job)));
            holdIn("send_y", 0);
          }
        }
    }
    // (proc,job) Pairs returned on port x
    // always accept so that no processor is lost

    for (int i = 0; i < x.size(); i++)
      if (messageOnPort(x, "x", i))
      {
        entity val = x.getValOnPort("x", i);
        Pair pr = (Pair) val;
        procs.add(pr.getKey());
        entity jb = (entity) pr.getValue();
        jobs.add(jb);
        jobsInProcess = jobsInProcess - 1;
        availableProcessors = "# of " + String.valueOf(procs.size());
      }
    // output completed jobs at earliest opportunity
    if (phaseIs("passive") && !jobs.isEmpty())
      holdIn("send_out", 0);
  }

  public void deltint()
  {
    if (phaseIs("send_out"))
    {
      jobs = new Queue();
      passivate();
    }
    // output completed jobs at earliest opportunity

    else if (phaseIs("send_y") && !jobs.isEmpty())
      holdIn("send_out", 0);
    else
      passivate();
  }

  public message out()
  {
    message m = new message();
    if (phaseIs("send_out"))
      for (int i = 0; i < jobs.size(); i++)
      {
        entity job = (entity) jobs.get(i);
        m.add(makeContent("out", job));
      }
    else if (phaseIs("send_y"))
      m = yMessage;
    return m;
  }

  public String getTooltipText()
  {
    return super.getTooltipText()
      + "\n"
      + " nCount: "
      + jobsInProcess;
  }

}
