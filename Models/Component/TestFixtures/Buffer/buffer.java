package Component.TestFixtures.Buffer;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import GenCol.entity;
import model.modeling.ContentIteratorInterface;
import model.modeling.content;
import model.modeling.message;
import model.modeling.port;
import view.modeling.ViewableAtomic;

/**
 * A factory model for queueing jobs. It is tested with {@link bufferFrame}.
 * 
 * @author Matthew McLaughlin
 * @see bufferFrame
 */
public class buffer extends ViewableAtomic
{
    private List<entity> fifo_queue;
    private int release;
    private int capacity;

    public buffer() 
    {
        this("buffer", 0, 10);
    }

    public buffer(String name, int init_release, int capacity)
    {
        super(name);
        this.fifo_queue = new LinkedList<entity>();
        this.release = init_release;
        this.capacity = capacity;
        
        addInport("in");
        addInport("release");
        addOutport("out");
        addOutport("balked");
        
        setBackgroundColor(Color.decode("#aad3f0"));
    }

    public void initialize()
    {
        phase = "passive";
        sigma = INFINITY;
        fifo_queue.clear();
        
        super.initialize();
    }
    
    private void updatePhaseAndSigma()
    {
        boolean has_request = (release > 0);
        boolean have_response = !fifo_queue.isEmpty();
        boolean emit_balk = (fifo_queue.size() - release > capacity);
        
        if (emit_balk)
        {
            // Buffer exceeds capacity
            
            holdIn("failing", 0);
        }
        else if (has_request && have_response)
        {
            // Model is ready to release message. 
            
            holdIn("dispatch", 0);
        }
        else 
        {
            passivateIn(has_request ? "await" : (have_response ? "queued" : "passive"));
        }
    }

    public void deltext(double e, message x) 
    {
        Continue(e);
        
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
                fifo_queue.add((entity) c.getValue());
            }
            else if (c.onPort(new port("release")))
            {
                release++;
            }
        }
        
        updatePhaseAndSigma();
    }

    public void deltint()
    {
        boolean has_request = (release > 0);
        boolean have_response = !fifo_queue.isEmpty();
        
        // Process out the released jobs from the buffer
        
        if (have_response && has_request)
        {
            int n_jobs = Math.min(release, fifo_queue.size());
            
            while (n_jobs > 0)
            {
                n_jobs--;
                release--;
                fifo_queue.remove(0);
            }
        }
        
        // Process out the balked jobs from the buffer

        while (fifo_queue.size() > capacity)
        {
            fifo_queue.remove(fifo_queue.size() - 1);
        }

        updatePhaseAndSigma();
    }

    public message out() 
    {
        message m = new message();

        boolean has_request = (release > 0);
        boolean have_response = !fifo_queue.isEmpty();
        
        // Add released jobs to the message
        
        if (have_response && has_request)
        {
            int n_jobs = Math.min(release, fifo_queue.size());
            
            for (int i = 0; i < n_jobs; i++)
            {
                m.add(makeContent("out", fifo_queue.get(i)));
            }
        }
        
        // Add balked jobs to the message

        for (int i = capacity + release; i < fifo_queue.size(); i++)
        {
            m.add(makeContent("balked", fifo_queue.get(i)));
        }

        return m;
    }

    public String getTooltipText() 
    {
        String retval = super.getTooltipText() + "<br> queue: ";
        
        if (fifo_queue.isEmpty())
        {
            retval += "empty";
        }
        else
        {
            retval += fifo_queue.size() + " message(s)";
        }
        
        retval += "<br> capacity: " + capacity + 
            "<br> release count: " + release;
        
        return retval;
    }
}
