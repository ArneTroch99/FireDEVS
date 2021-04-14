package SimpArcModTestArtifacts;

import java.awt.Dimension;
import java.awt.Point;

import GenCol.entity;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class Fake_gpt extends ViewableDigraph
{
    public Fake_gpt()
    {
        super("Fake_gpt");

        ViewableAtomic g = new Fake_genr("Fake_g", 10);
        ViewableAtomic p = new Fake_proc("Fake_p", 5);
        ViewableAtomic t = new Fake_transd("Fake_t", 370);

        add(g);
        add(p);
        add(t);

        addInport("in");
        addInport("start");
        addInport("stop");
        addOutport("out");
        addOutport("result");

        addTestInput("start", new entity());
        addTestInput("stop", new entity(), 5.0);

        addCoupling(this, "in", g, "in");

        addCoupling(this, "start", g, "start");
        addCoupling(this, "stop", g, "stop");

        addCoupling(g, "out", p, "in");

        addCoupling(g, "out", t, "ariv");
        addCoupling(p, "out", t, "solved");
        addCoupling(t, "out", g, "stop");

        addCoupling(p, "out", this, "out");
        addCoupling(t, "out", this, "result");
    }
}
