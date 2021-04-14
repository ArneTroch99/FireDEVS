/* Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package Component.UtilModels;

import java.util.Iterator;

import GenCol.Bag;
import GenCol.EntityInterface;
import GenCol.Pair;
import GenCol.Relation;
import GenCol.doubleEnt;
import GenCol.entity;
import model.modeling.message;
import view.modeling.ViewableAtomic;

/**
 * An atomic devs component which outputs jobs a certain amount of time
 * after they are received.
 *
 * @author      Bernard Zeigler, Jeff Mather
 */
public class JobQueue extends ViewableAtomic
{
    /**
     * The jobs that have arrived at this job-queue.
     */
    protected Relation arrived = new Relation();

    /**
     * Those jobs that have become due.
     */
    protected Bag due;

    /**
     * This component's record of what the current simulation time is.
     */
    protected double clock;

    /**
     * The length of time after a job arrives until it is due.
     */
    protected double jobDueDelay;

    /**
     * The minimum time of all the jobs that have arrived.
     */
    protected doubleEnt minimumJobTime;

    /**
     * Constructor.
     *
     * @param   name        The name to call this job-queue.
     * @param   jobDueDelay The length of time after a job arrives until
     *                      it is due.
     */
    public JobQueue(String name, double jobDueDelay_)
    {
        super(name);

        // create this job-queue's ports
        addInport("in");
        addInport("none");
        addOutport("out");

        jobDueDelay = jobDueDelay_;
    }

    /**
     * Constructs an job-queue with default values and test-inputs,
     * for convenience.
     */
    public JobQueue()
    {
        this("JobQueue", 10);

        // add some test inputs
        addTestInput("none", new entity("job"), 1);
        addTestInput("in", new entity("job1"), 0);
        addTestInput("in", new entity("job1"), 1);
        addTestInput("in", new entity("job2"), 1);
        addTestInput("in", new entity("job3"), 1);
    }

    /**
     * See parent method.
     */
    public void initialize()
    {
        passivate();
        clock = 0;
        super.initialize();
        arrived = new Relation();
        due = new Bag();
        minimumJobTime = new doubleEnt(INFINITY);
    }

    /**
     * Determines the minimum time of all the jobs that have arrived.
     */
    protected void detmMinimumJobTime()
    {
        double min = INFINITY;

        // if there are any jobs that have arrived
        if (!arrived.isEmpty()) {
            // for each job that has arrived
            Iterator i = arrived.iterator();
            while (i.hasNext()) {
                // if this job's time is the minimum so far
                Pair pair = (Pair)i.next();
                double time = ((doubleEnt)pair.getKey()).getv();
                if (time < min) {
                    // remember this job's time
                    min = time;
                }
            }
        }

        minimumJobTime = new doubleEnt(min);
    }

    /**
     * Creates a bag of jobs that are due consisting of those that have arrived
     * and have the minimum time.
     */
    protected void detmDueJobs()
    {
        // for each arrived job
        due = new Bag();
        Iterator i = arrived.iterator();
        while (i.hasNext()) {
            // if the job is one of those that has the minimum time
            Pair pair = (Pair)i.next();
            if (pair.getKey().equals(minimumJobTime)) {
                // add to this job to the bag of those that are due
                due.add(pair.getValue());
            }
        }
    }

    /**
     * Removes all the jobs of the minimum time from the container of
     * arrived jobs.
     */
    protected void removeAllMinimumJobs()
    {
        // for each arrived job
        Iterator i = arrived.iterator();
        while (i.hasNext()) {
            // if the job is one of those that has the minimum time
            Pair pair = (Pair)i.next();
            if (pair.getKey().equals(minimumJobTime)) {
                // remove the job from the arrived jobs container
                arrived.remove(pair.getKey(), pair.getValue());
            }
        }
    }

    /**
     * See parent method.
     */
    public void deltext(double e, message message)
    {
        // update this job-queue's track of the current clock value
        clock = clock + e;

        Continue(e);

        // for each content in the message
        for (int i = 0; i < message.getLength(); i++) {
            // if this content is on port "in"
            if (messageOnPort(message, "in", i)) {
                // the content represents a job, add it to the list of
                // arrived jobs
                EntityInterface value = message.getValOnPort("in", i);
                arrived.put(new doubleEnt(clock + jobDueDelay), value);
            }
        }

        deltextHook1(message);

        holdUntilNextJob();
    }

    protected void deltextHook1(message message) {}

    /**
     * Makes this job-queue hold in phase "active" until the time of the
     * next job of minimum time.
     */
    protected void holdUntilNextJob()
    {
        // if there are any arrived jobs
        detmMinimumJobTime();
        if (!arrived.isEmpty()) {
            // hold in phase "active" until the minimum time amongst all
            // the arrived jobs
            holdIn("active", minimumJobTime.getv() - clock);
        }

        // otherwise
        else {
            passivate();
        }

        detmDueJobs();
    }

    /**
     * See parent method.
     */
    public void deltcon(double e, message message)
    {
        // the order needed here is the reverse of the default deltcon order
        deltext(e, message);
        deltint();
    }

    /**
     * See parent method.
     */
    public void deltint()
    {
        // update this job-queue's clock value
        clock = clock + sigma;

        deltintHook1();

        removeAllMinimumJobs();

        holdUntilNextJob();
    }

    protected void deltintHook1() {}

    /**
     * See parent method.
     */
    public message out()
    {
        message message = new message();

        if (phaseIs("active")) {
            // for each job that is due
            Iterator i = due.iterator();
            while (i.hasNext()) {
                // add this job to the output message
                message.add(makeContent("out", (entity)i.next()));
            }
        }

        return message;
    }

    /**
     * See parent method.
     */
    public String stringState()
    {
        if (arrived != null && due != null) {
            return "arrived : " + arrived.size() +
                "\n" + "clock : " + clock + "\n" +
                "min : " + minimumJobTime.getv() + "\n" +
                "due : " + due.size();
        }

        else {
            return "";
        }
    }
}



