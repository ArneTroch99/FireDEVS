package facade.modeling.FAtomicModelTestArtifacts;

import java.util.ArrayList;

import model.modeling.state;
import view.modeling.ViewableAtomic;

public class FakeViewableAtomic extends ViewableAtomic
{
    @state
    public static ArrayList<String> stringList = new ArrayList<String>();

    @state(log=state.DEFAULT_CHECKED, db=state.DEFAULT_CHECKED)
    public static ArrayList<String> stringList_defaultChecked = new ArrayList<String>();
    
    public static boolean notState = false;

    public String instanceVariable = "instance";

    @SuppressWarnings("unused")
    private String privateInstanceVariable = "privateInstance";
    
    public FakeViewableAtomic()
    {
        super("fakeAtomicName");
        this.addInport("inport1");
        this.addInport("inport2");
        this.addOutport("outport1");
        this.addOutport("outport2");
    }
}
