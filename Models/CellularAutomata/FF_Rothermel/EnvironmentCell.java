package CellularAutomata.FF_Rothermel;

import GenCol.Pair;
import model.modeling.CAModels.TwoDimCell;
import model.modeling.message;
import util.Logging;

import java.util.*;
import java.util.stream.Collectors;

public class EnvironmentCell extends TwoDimCell {

    // Environmental variables
    private final double windDir = -Math.PI / 2;  // The direction of the wind TODO: Change based on wind direction
    private final double e = 0.99; // Determines the layout of the ellipse TODO: change this value based on the wind speed

    // Cell-variables
    // The positions where the cell has been ignited TODO: allow the cell to be ignited from multiple directions
    private final List<String> ignitePositions = new ArrayList<>();
    // The timers to keep track of when a certain output will be ignited
    // NW N NE      (0, 0)  (w/2, 0) (w, 0)
    // W     E      (0, h/2)         (w, h/2)
    // SW S SE      (0, h)  (w/2, h) (w, h)
    private final Map<String, Double> outputTimers = new HashMap<>();
    // The coordinate positions of the different directions
    private final Map<String, double[]> dirCoordinates = new HashMap<>(8);
    // List to determine if an ignite command has been sent to an output (based on the order of outputTimers)
    private final List<String> igniteSent = new ArrayList<>();
    // The output directions for each possible input location
    HashMap<String, String[]> dirOutputs = new HashMap<>(8);
    // Width of the cell
    private double width = 5;
    // Height of the cell
    private double height = 5;
    // Possible states for the cell (unburned, unburnable, burning, burned)
    private State state;

    private boolean startFire;

    // Rothermel variables
    private double w_o;
    private double sigma;
    private double h;
    private double M_f;
    private double U;
    private double beta;
    private double M_x;
    private ROS_Calculator rosCalculator;

    public EnvironmentCell() {
        this(0, 0);
    }

    public EnvironmentCell(int xcoord, int ycoord) {
        super(xcoord, ycoord);
        addTestInput("inN", new Pair<>("status", "ignite"));
    }

    public EnvironmentCell(int xcoord, int ycoord, double w_o, double sigma, double h, double M_f, double U, double beta, double M_x, double width, double height, ROS_Calculator rosCalculator) {
        super(xcoord, ycoord);
        this.w_o = w_o;
        this.sigma = sigma;
        this.h = h;
        this.M_f = M_f;
        this.U = U;
        this.beta = beta;
        this.M_x = M_x;
        this.width = width;
        this.height = height;
        this.rosCalculator = rosCalculator;
    }

    public static void main(String[] args) {
        EnvironmentCell cell = new EnvironmentCell();
        cell.initialize();
        cell.calculateTimers("NE");
    }

    /**
     * Initialization method
     */
    public void initialize() {
        super.initialize();
        if (startFire){
            state = State.BURNING;
            calculateTimers("N");
        } else {
            state = State.UNBURNED;
        }
        Logging.log("-- " + this.getName() + "| Initializing cell in state: " + state);
        dirOutputs.put("N", new String[]{"NE", "E", "SE", "S", "SW", "W", "NW"});
        dirOutputs.put("E", new String[]{"N", "NE", "SE", "S", "SW", "W", "NW"});
        dirOutputs.put("S", new String[]{"N", "NE", "E", "SE", "SW", "W", "NW"});
        dirOutputs.put("W", new String[]{"N", "NE", "E", "SE", "S", "SW", "NW"});
        dirOutputs.put("NE", new String[]{"SE", "S", "SW", "W", "NW"});
        dirOutputs.put("SE", new String[]{"N", "NE", "SW", "W", "NW"});
        dirOutputs.put("SW", new String[]{"N", "NE", "E", "SE", "NW"});
        dirOutputs.put("NW", new String[]{"NE", "E", "SE", "S", "SW"});

        dirCoordinates.put("N", new double[]{width / 2, 0});
        dirCoordinates.put("E", new double[]{width, -height / 2});
        dirCoordinates.put("S", new double[]{width / 2, -height});
        dirCoordinates.put("W", new double[]{0, -height / 2});
        dirCoordinates.put("NE", new double[]{width, 0});
        dirCoordinates.put("SE", new double[]{width, -height});
        dirCoordinates.put("SW", new double[]{0, -height});
        dirCoordinates.put("NW", new double[]{0, 0});

        holdIn(state.toString(), INFINITY);
        EnvironmentCellUI.setPhaseColor();
    }

    /**
     * Trigger the external and internal transition functions
     */
    public void deltcon(double e, message x) {
        deltint();
        deltext(e, x);
    }

    /**
     * External Transition Function
     * Receive an ignite command from a neighbouring cell. Store this starting position of ignition.
     */
    public void deltext(double e, message x) {
        String port = "";
        for (int i = 0; i < x.getLength(); i++) {
            if (!state.equals(State.UNBURNABLE) && !state.equals(State.BURNED)) {
                if (somethingOnPort(x, "inN")) {
                    inpair = (Pair) x.getValOnPort("inN", i);
                    port = "N";
                } else if (somethingOnPort(x, "inNE")) {
                    inpair = (Pair) x.getValOnPort("inNE", i);
                    port = "NE";
                } else if (somethingOnPort(x, "inE")) {
                    inpair = (Pair) x.getValOnPort("inE", i);
                    port = "E";
                } else if (somethingOnPort(x, "inSE")) {
                    inpair = (Pair) x.getValOnPort("inSE", i);
                    port = "SE";
                } else if (somethingOnPort(x, "inS")) {
                    inpair = (Pair) x.getValOnPort("inS", i);
                    port = "S";
                } else if (somethingOnPort(x, "inSW")) {
                    inpair = (Pair) x.getValOnPort("inSW", i);
                    port = "SW";
                } else if (somethingOnPort(x, "inW")) {
                    inpair = (Pair) x.getValOnPort("inW", i);
                    port = "W";
                } else if (somethingOnPort(x, "inNW")) {
                    inpair = (Pair) x.getValOnPort("inNW", i);
                    port = "NW";
                }
                if (inpair != null && inpair.getValue().toString().equals("ignite")) {
                    Logging.log("-- " + this.getName() + "| received ignite command from " + port);
                    // TODO: add multiple triggers
                    ignitePositions.add(port);
                    Logging.log("-- " + this.getName() + "| transitioning to burning state");
                    state = State.BURNING;
                    Logging.log("-- " + this.getName() + "| setting original timers");
                    calculateTimers(port);
                }
            }
        }
        holdIn(state.toString(), 1);
    }

    /**
     * Calculate the timers for the edges of the cell to be reached based on ignite position, ROS and wind direction/speed
     */
    public void calculateTimers(String initPos) {
        List<String> targetDirs = Arrays.stream(dirOutputs.get(initPos)).collect(Collectors.toList());

        // Calculate the angles and distances between the initial position and the target directions
        List<Double> angles = new ArrayList<>(targetDirs.size());
        List<Double> distances = new ArrayList<>(targetDirs.size());
        for (String t : targetDirs) {
            angles.add(Math.atan2((dirCoordinates.get(t)[1] - dirCoordinates.get(initPos)[1]), (dirCoordinates.get(t)[0] - dirCoordinates.get(initPos)[0])));
            distances.add(AbsurdUnitConverter.m_to_ft(Math.sqrt((dirCoordinates.get(initPos)[0] - dirCoordinates.get(t)[0]) * (dirCoordinates.get(initPos)[0] - dirCoordinates.get(t)[0]) + (dirCoordinates.get(initPos)[1] - dirCoordinates.get(t)[1]) * (dirCoordinates.get(initPos)[1] - dirCoordinates.get(t)[1]))));
        }
        Logging.log("-- " + this.getName() + "| Angles were calulated! " + angles.toString());
        Logging.log("-- " + this.getName() + "| Distances were calulated! " + distances.toString());

        // Update the angles to the "new" coordinate system
        angles = angles.stream().map(a -> {
            double angleDiff = a - windDir;
            angleDiff += (angleDiff > Math.PI) ? (-2 * Math.PI) : (angleDiff < -Math.PI) ? (2 * Math.PI) : 0;
            return angleDiff;
        }).collect(Collectors.toList());
        Logging.log("-- " + this.getName() + "| Angles were adjusted! " + angles.toString());

        angles.forEach(a -> {
            assert (a <= Math.PI && a >= -Math.PI);
        });

        // Calculate the scaled angles using the ellipse
        List<Double> scaledAngles = new ArrayList<>(targetDirs.size());
        assert (e > 0.0 && e < 1.0);
        for (Double a : angles) {
            scaledAngles.add((1 - e * e) * (1 - e * Math.cos(a + Math.PI)));
        }
        Logging.log("-- " + this.getName() + "| Angles were scaled! " + scaledAngles.toString());
        double maxAngle = ((1 - e * e) * (1 - e * Math.cos(Math.PI)));
        scaledAngles = scaledAngles.stream().map(a -> a / maxAngle).collect(Collectors.toList());
        Logging.log("-- " + this.getName() + "| Angles were normalized! " + scaledAngles.toString());

        // TODO: Calculate the current ROS
        double ros = 4;
        for (int i = 0; i < targetDirs.size(); i++) {
            outputTimers.put(targetDirs.get(i), distances.get(i) / (ros * scaledAngles.get(i)));
        }
        Logging.log("-- " + this.getName() + "| Timings were calculated! " + outputTimers.toString());
    }

    /**
     * Internal Transition Function
     * Tick down the timers each step with a value of e
     */
    public void deltint() {
        if (this.state.equals(State.BURNING)) {
            this.outputTimers.keySet().forEach(k -> {
                double newTime = Math.max(this.outputTimers.get(k) - this.getSigma(), 0);
                this.outputTimers.put(k, newTime);
            });
            Logging.log("-- " + this.getName() + "| Current timers: " + this.outputTimers);
        }
    }

    /**
     * Message out function
     *
     * @return Message to send to neighbours
     */
    public message out() {
        message m = super.out();
        this.outputTimers.keySet().forEach(k -> {
            if (this.outputTimers.get(k) <= 0) {
                if (!igniteSent.contains(k)) {
                    Logging.log("-- " + this.getName() + "| Sending ignite command to " + k);
                    m.add(makeContent("out" + k, new Pair<>("status", "ignite")));
                    igniteSent.add(k);
                }
            }
            if (igniteSent.size() == outputTimers.size()) {
                state = State.BURNED;
                holdIn(state.toString(), INFINITY);
            }
        });
        return m;
    }

    public void setStartFire(boolean startFire) {
        this.startFire = startFire;
    }

    enum State {
        BURNED,
        UNBURNED,
        UNBURNABLE,
        BURNING
    }
}
