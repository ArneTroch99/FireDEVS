package Component.TestFixtures.Homework;
 
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
 
import GenCol.entity;
import model.modeling.initializer;
import model.modeling.initializerFactory;
import model.modeling.initializerFactory.DynamicInit;
import model.modeling.message;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;
 
public class proc extends ViewableAtomic {// ViewableAtomic is used instead
    // of atomic due to its
    // graphics capability
    protected entity job;
    protected double processing_time;
 
    @initializerFactory
    public static Iterable<DynamicInit> getInits()
    {
        List<DynamicInit> retval = new LinkedList<DynamicInit>();
 
        retval.add(new DynamicInit("proc_time = 20", () -> {
                return new proc("proc", 20);
            }));
       
        retval.add(new DynamicInit("proc_time = 10", () -> {
            return new proc("proc", 10);
        }));
       
        return retval;
    }
 
    @initializer(displayName = "proc_time = 30")
    public static ViewableComponent anotherInitExample()
    {
        return new proc("proc", 30);
    }
   
    public proc() {
        this("proc", 10);
    }
 
    public proc(String name, double Processing_time) {
        super(name);
        addInport("in");
        addOutport("out");
        processing_time = Processing_time;
        addTestInput("in", new entity("job1"));
        addTestInput("in", new entity("job2"), 20);
       
        setBackgroundColor(Color.decode("#ffaaaa"));
    }
 
    public void initialize() {
        phase = "passive";
        sigma = INFINITY;
        job = new entity("job");
        super.initialize();
    }
 
    public void deltext(double e, message x) {
        Continue(e);
 
        System.out.println("The elapsed time of the processor is" + e);
        System.out.println("*****************************************");
        System.out.println("external-Phase before: "+phase);
       
        if (phaseIs("passive"))
            for (int i = 0; i < x.getLength(); i++)
                if (messageOnPort(x, "in", i)) {
                    job = x.getValOnPort("in", i);
                    holdIn("busy", processing_time);
                    System.out.println("processing tiem of proc is"
                            + processing_time);
                }
       
        System.out.println("external-Phase after: "+phase);
    }
 
    public void deltint() {
        System.out.println("Internal-Phase before: "+phase);
        passivate();
        job = new entity("none");
        System.out.println("Internal-Phase after: "+phase);
    }
 
    public void deltcon(double e, message x) {
        System.out.println("confluent");
        deltint();
        deltext(0, x);
    }
 
    public message out() {
        message m = new message();
        if (phaseIs("busy")) {
            m.add(makeContent("out", job));
        }
        return m;
    }
 
    public void showState() {
        super.showState();
        // System.out.println("job: " + job.getName());
    }
 
    public String getTooltipText() {
        return super.getTooltipText() + "\n" + "job: " + job.getName();
    }
}
 