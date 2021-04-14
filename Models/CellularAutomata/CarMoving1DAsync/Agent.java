package CellularAutomata.CarMoving1DAsync;

import java.util.ArrayList;
import java.util.Random;
import model.modeling.message;
import model.modeling.CAModels.TwoDimCell;

public class Agent extends TwoDimCell
{
    private static final int rate = 20;
    protected inputEntity msg;
    // 8 neighbors and 1 itself
    protected String[] goingDirections = {
        "outE"
    };
    protected String[] neighborIn = {
        "inE",
        "inW"
    };
    protected String[] neighborOut = {
        "outE",
        "outW"
    };
    protected String[] FeedbackDirections = {
        "No",
        "No"
    };

    protected String[] waitingDirections = {
        "No",
        "No"
    };

    protected ArrayList<Integer> FeedbackShuffle = new ArrayList<Integer>();
    protected ArrayList<Integer> waitingShuffle = new ArrayList<Integer>();
    Random r = new Random();

    // status is the phase
    private String status;
    private String goDirection = "Stay";
    private String comeDirection = "No";
    private String waitDirection = "No";
    private String finalDirection = "No";

    private double step;
    // protected boolean clocked = true;

    public Agent()
    {
        this(0, 0);
    }

    public Agent(int xcoord, int ycoord)
    {
        super(xcoord, ycoord);
        status = "EMPTY";
        step = 1;
    }

    public Agent(int xcoord, int ycoord, String _status)
    {
        super(xcoord, ycoord);
        status = _status;
        step = 1;
    }

    public Agent(int xcoord, int ycoord, String _status, int _step)
    {
        super(xcoord, ycoord);
        status = _status;
        step = _step;
        addInport("inTrans");
        addOutport("outTrans");
    }

    /**
     * Initialization method
     */
    public void initialize()
    {
        super.initialize();
        if (status == "EMPTY")
        {
            holdIn("EMPTY", INFINITY);
        }
        else
        {
            holdIn("Agent: " + status, step);
        }
        // Define the Phase Color for CA Display
        AgentUI.setPhaseColor();
    }

    /**
     * External Transition Function
     */

    public void deltext(double e, message x)
    {
        Continue(e);

        for (int i = 0; i < x.getLength(); i++)
        {
            for (int j = 0; j < neighborIn.length; j++)
            {
                if (somethingOnPort(x, neighborIn[j]))
                {
                    msg = (inputEntity) x.getValOnPort(
                        neighborIn[j],
                        i
                    );
                    if (msg != null
                        && msg.getInput().contains("Agent"))
                    {
                        if (status == "EMPTY")
                            FeedbackDirections[j] = "WANTTOCOME";
                        else
                            waitingDirections[j] = "WAITTOCOME";
                    }
                    else if (msg != null
                        && msg.getInput().contains("Feedback"))
                    {
                        finalDirection = neighborOut[j];
                    }
                    else if (msg != null)
                    {
                        phase = "Agent: " + msg.getInput();
                        status = msg.getInput();
                        // agent has constant speed not cell
                        step = msg.getSigma();
                    }
                }
            }
        }

        for (int i = 0; i < FeedbackDirections.length; i++)
        {
            if (FeedbackDirections[i] == "WANTTOCOME")
            {
                FeedbackShuffle.add(i);
            }
            else if (waitingDirections[i] == "WAITTOCOME")
            {
                waitingShuffle.add(i);
            }
        }
        if (FeedbackShuffle.size() > 0)
        {
            int pickCome = r.nextInt(FeedbackShuffle.size());
            comeDirection = neighborIn[FeedbackShuffle.get(pickCome)];
        }

        if (waitingShuffle.size() > 0)
        {
            int pickCome = r.nextInt(waitingShuffle.size());
            waitDirection = neighborIn[waitingShuffle.get(pickCome)];
        }

        if (goDirection == "Stay"
            && comeDirection == "No"
            && finalDirection == "No"
            && waitDirection == "No")
        {
            holdIn(phase, step);
        }
        else if (comeDirection != "No")
        {
            holdIn("Feedback:- " + comeDirection, 0);

        }

        else if (finalDirection != "No" && waitDirection == "No")
        {
            holdIn(status + ":- Move " + finalDirection, 0);
        }
        else if (finalDirection != "No" && waitDirection != "No")
        {
            holdIn(status + ":- Wait " + finalDirection, 0);
        }
        else if(sigma >0 && sigma < step) {
            holdIn(phase, sigma);
            waitDirection = "No";           
        }

        // reset the direction array
        for (int i = 0; i < FeedbackDirections.length; i++)
        {
            FeedbackDirections[i] = "No";
        }
        FeedbackShuffle.clear();

        for (int i = 0; i < waitingDirections.length; i++)
        {
            waitingDirections[i] = "No";
        }
        waitingShuffle.clear();

        // System.out.println(getXcoord() + ", " + getYcoord() + ": " + phase);

    }

    /*
     * Internal Transition Function
     */

    public void deltint()
    {

        if (status == "EMPTY")
        {
            holdIn("EMPTY", INFINITY);
        }
        else if (phase.contains("Agent"))
        {
            holdIn(phase, step);
            waitDirection = "No";    
        }
        else if (phase.contains("Wait"))
        {
            holdIn(status + ":- Move " + finalDirection, 0);
        }
        else
        {
            holdIn(phase, INFINITY);
        }

    }

    public void deltcon(double e, message x)
    {
        deltint();
        deltext(0, x);
    }

    /*
     * Message out Function
     */
    public message out()
    {

        message m = new message();

        if (phase.contains("Agent"))
        {
            goDirection = goingDirections[r.nextInt(
                goingDirections.length
            )];
            for (int i = 0; i < neighborOut.length; i++)
            {
                if (goDirection == neighborOut[i])
                {
                    m.add(
                        makeContent(
                            neighborOut[i],
                            new inputEntity(phase, step)
                        )
                    );
                    break;
                }
            }

        }
        else if (phase.contains("Feedback"))
        {
            for (int i = 0; i < neighborOut.length; i++)
            {
                if (comeDirection == neighborIn[i])
                {
                    m.add(
                        makeContent(
                            neighborOut[i],
                            new inputEntity(phase, step)
                        )
                    );
                    break;
                }
            }
        }
        else if (phase.contains("Move"))
        {
            for (int i = 0; i < neighborOut.length; i++)
            {
                if (finalDirection == neighborOut[i])
                {
                    m.add(
                        makeContent(
                            neighborOut[i],
                            new inputEntity(status, step)
                        )
                    );
                    status = "EMPTY";
                    finalDirection = "No";
                    break;

                }
            }
        }
        else if (phase.contains("Wait"))
        {
            for (int i = 0; i < neighborOut.length; i++)
            {

                if (waitDirection == neighborIn[i])
                {
                    m.add(
                        makeContent(
                            neighborOut[i],
                            new inputEntity(
                                "Feedback:- " + waitDirection,
                                step
                            )
                        )
                    );
                    waitDirection = "No";
                    break;
                }
            }

        }

        goDirection = "Stay";
        comeDirection = "No";

        return m;

    }

    public String getStatus()
    {
        return status;
    }

    public void setInitialStatus(String status)
    {
        this.status = status;
    }

}
