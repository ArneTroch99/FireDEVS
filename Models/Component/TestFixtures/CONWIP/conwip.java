package Component.TestFixtures.CONWIP;

import java.awt.Color;

import java.util.LinkedList;
import java.util.List;

import GenCol.entity;
import GenCol.intEnt;
import model.modeling.ContentIteratorInterface;
import model.modeling.content;
import model.modeling.message;
import model.modeling.port;
import view.modeling.ViewableAtomic;

/**
 * A factory model for maintaining a CONstant Work-In-Process (CONWIP) 
 * within a production system. It is tested with {@link conwipFrame}.
 * 
 * @author Matthew McLaughlin
 * @see conwipFrame
 */
public class conwip extends ViewableAtomic
{
    private List<entity> backlog;
    private int processing;
    private int target_wip;
    private boolean inv_state;

    public conwip() 
    {
        this("conwip", 0);
    }

    public conwip(String name, int target_wip)
    {
        super(name);
        this.backlog = new LinkedList<entity>();
        this.target_wip = target_wip;
        this.processing = 0;
        this.inv_state = false;

        addInport("in");
        addInport("set");
        addInport("release");
        addOutport("out");
        addOutport("fail");

        setBackgroundColor(Color.decode("#ffff99"));
    }

    public void initialize()
    {
        phase = "passive";
        sigma = INFINITY;
        backlog.clear();
        
        super.initialize();
    }
    
    private void updatePhaseAndSigma()
    {
        boolean has_request = (target_wip - processing > 0);
        boolean have_response = !backlog.isEmpty();
        
        if (processing < 0)
        {
            holdIn("invalid", inv_state ? INFINITY : 0.0);
            inv_state = true;
        }
        else if (has_request && have_response)
        {

            // Model is ready to release message. 
            
            holdIn("dispatch", 0);
        }
        else 
        {
            passivateIn(has_request ? "starved" : (have_response ? "queued" : "passive"));
        }
    }

    public void deltext(double e, message x) 
    {
        Continue(e);
        
        if (inv_state)
        {
            return;
        }

        ContentIteratorInterface cit = x.mIterator();

        while(cit.hasNext())
        {
            content c = (content) cit.next();
            
            // Note that receiving multiple jobs in parallel
            // will have arbitrary ordering in the FIFO queuing 
            // buffer. A symmetry to this issue exists when the
            // model simultaneously releases multiple items from the
            // buffer.
                            
            if (c.onPort(new port("in")))
            {
                backlog.add((entity) c.getValue());
            }
            else if (c.onPort(new port("release")))
            {
                processing--;
            }
            else if (c.onPort(new port("set")))
            {
               intEnt val = (intEnt) c.getValue();
               target_wip = val.getv();
            }    
        }

        updatePhaseAndSigma();
    }

    public void deltint()
    {
        assert (phaseIs("dispatch") || phaseIs("invalid"));

        if (! inv_state)
        {
            assert (!backlog.isEmpty());
            int release = target_wip - processing;
            int n_jobs = Math.min(release, backlog.size());

            while (n_jobs > 0)
            {
                n_jobs--;
                processing++;
                backlog.remove(0);
            }
        }

        updatePhaseAndSigma();
    }

    public void deltcon(double e, message x) 
    {
        deltint();
        deltext(0, x);
    }

    public message out() 
    {
        message m = new message();

        if (inv_state)
        {
            m.add(makeContent("fail", new entity("neg WIP")));
        }
        else if (phaseIs("dispatch"))
        {
            assert (target_wip - processing > 0);
            assert (!backlog.isEmpty());
            
            // Note that we release all available jobs simultaneously.
            // This may cause FIFO ordering to be lost unless the
            // receiving model can sort received jobs.

            int release = target_wip - processing;
            int n_jobs = Math.min(release, backlog.size());
            
            for (int i = 0; i < n_jobs; i++)
            {
                m.add(makeContent("out", backlog.get(i)));
            }
        }
        
        return m;
    }

    public String getTooltipText() 
    {
        String retval = super.getTooltipText() + 
            "<br> Target WIP: " + target_wip +
            "<br> Current WIP: " + processing +
            "<br> Backlog: ";
        
        if (backlog.isEmpty())
        {
            retval += "empty";
        }
        else
        {
            retval += backlog.size() + " job(s)";
        }
        
        return retval;
    }
}
