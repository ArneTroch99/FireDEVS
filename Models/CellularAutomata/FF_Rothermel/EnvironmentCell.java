package CellularAutomata.FF_Rothermel;

import GenCol.Pair;
import model.modeling.CAModels.TwoDimCell;
import model.modeling.message;
import util.Logging;

import java.util.*;
import java.util.stream.Collectors;

public class EnvironmentCell extends TwoDimCell {

    // Cell-variables
    // The positions where the cell has been ignited
    private final List<String> ignitePositions = new ArrayList<>();
    // The timers to keep track of when a certain output will be ignited
    // NW N NE      (0, 0)  (w/2, 0) (w, 0)
    // W     E      (0, h/2)         (w, h/2)
    // SW S SE      (0, h)  (w/2, h) (w, h)
    private final Map<String, Double> outputTimers = new HashMap<>();
    // List to determine if an ignite command has been sent to an output (based on the order of outputTimers)
    private final List<String> igniteSent = new ArrayList<>();
    // Possible states for the cell (unburned, unburnable, burning, burned)
    private State state;

    private boolean startFire;

    private double sigma_time = 1; // Timestep in seconds

    // Rothermel variables
    private double w_o;
    private double delta;
    private double sigma;
    private double h;
    private double beta;
    private double M_f;
    private double tan_phi = 0; // TODO: Change this value based on the slope
    private double M_x;
    private ROS_Calculator rosCalculator;

    public EnvironmentCell() {
        this(0, 0);
    }

    public EnvironmentCell(int xcoord, int ycoord) {
        super(xcoord, ycoord);
    }

    public void setFuelmodel(FuelModel fuelModel) {
        this.w_o = AbsurdUnitConverter.t_ac_to_lb_ftsqrd(fuelModel.getFine_fuel_load());
        this.delta = fuelModel.getFuel_bed_depth();
        this.sigma = fuelModel.getSAV();
        this.h = fuelModel.getHeat_content();
        this.beta = fuelModel.getPacking_ratio();
        this.M_x = fuelModel.getExtinction_moisture_content();
    }

    public void setRosCalculator(ROS_Calculator rosCalculator) {
        this.rosCalculator = rosCalculator;
    }

    /**
     * Initialization method
     */
    public void initialize() {
        super.initialize();
        if (startFire) {
            state = State.BURNING;
            ignitePositions.add("C");
            calculateTimers("C");
            holdIn("START", sigma_time);
        } else if (M_x == 0){
            state = State.UNBURNABLE;
            holdIn(state.toString(), INFINITY);
        } else {
            state = State.UNBURNED;
            holdIn(state.toString(), INFINITY);
        }
        Logging.log("-- " + this.getName() + "| Initialized cell in state: " + state + " with sav " + sigma, Logging.debug);

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
            if (state.equals(State.UNBURNED) || state.equals(State.BURNING)) {
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
                    Logging.log("-- " + this.getName() + "| received ignite command from " + port, Logging.debug);
                    ignitePositions.add(port);
                    Logging.log("-- " + this.getName() + "| transitioning to burning state", Logging.debug);
                    state = State.BURNING;
                    Logging.log("-- " + this.getName() + "| setting original timers", Logging.debug);
                    calculateTimers(port);
                }
            }
        }
        if (startFire) {
            holdIn("START", sigma_time);
        } else {
            holdIn(state.toString(), sigma_time);
        }
    }

    /**
     * Calculate the timers for the edges of the cell to be reached based on ignite position, ROS and wind direction/speed
     */
    public void calculateTimers(String initPos) {
        List<String> targetDirs = Arrays.stream(CellUtils.getOutputDirections(initPos)).collect(Collectors.toList());
        CellUtils.updateParameters();

        // Calculate the angles and distances between the initial position and the target directions
        List<Double> angles = new ArrayList<>(targetDirs.size());
        List<Double> distances = new ArrayList<>(targetDirs.size());
        for (String t : targetDirs) {
            angles.add(Math.atan2((CellUtils.getCoordinates(t)[1] - CellUtils.getCoordinates(initPos)[1]), (CellUtils.getCoordinates(t)[0] - CellUtils.getCoordinates(initPos)[0])));
            distances.add(AbsurdUnitConverter.m_to_ft(Math.sqrt((CellUtils.getCoordinates(initPos)[0] - CellUtils.getCoordinates(t)[0]) * (CellUtils.getCoordinates(initPos)[0] - CellUtils.getCoordinates(t)[0]) + (CellUtils.getCoordinates(initPos)[1] - CellUtils.getCoordinates(t)[1]) * (CellUtils.getCoordinates(initPos)[1] - CellUtils.getCoordinates(t)[1]))));
        }
        Logging.log("-- " + this.getName() + "| Angles were calculated! " + angles.toString(), Logging.debug);
        Logging.log("-- " + this.getName() + "| Distances were calculated! " + distances.toString(), Logging.debug);

        // Update the angles to the "new" coordinate system
        double windDir = CellUtils.getWindDir();
        angles = angles.stream().map(a -> {
            double angleDiff = a - windDir;
            angleDiff += (angleDiff > Math.PI) ? (-2 * Math.PI) : (angleDiff < -Math.PI) ? (2 * Math.PI) : 0;
            return angleDiff;
        }).collect(Collectors.toList());
        Logging.log("-- " + this.getName() + "| Angles were adjusted! " + angles.toString(), Logging.debug);

        angles.forEach(a -> {
            assert (a <= Math.PI && a >= -Math.PI);
        });

        // Calculate the scaled angles using the ellipse
        List<Double> scaledAngles = new ArrayList<>(targetDirs.size());
        double e = CellUtils.getE();
        assert (e > 0.0 && e < 1.0);
        for (Double a : angles) {
            scaledAngles.add((1 - e * e) * (1 - e * Math.cos(a + Math.PI)));
        }
        Logging.log("-- " + this.getName() + "| Angles were scaled! " + scaledAngles.toString(), Logging.debug);
        double maxAngle = ((1 - e * e) * (1 - e * Math.cos(Math.PI)));
        scaledAngles = scaledAngles.stream().map(a -> a / maxAngle).collect(Collectors.toList());
        Logging.log("-- " + this.getName() + "| Angles were normalized! " + scaledAngles.toString(), Logging.debug);

        double ros = rosCalculator.calculate_ROS(w_o, delta, sigma, h, beta, CellUtils.getMoisture(), CellUtils.getWindSpeed(), tan_phi, M_x);
        for (int i = 0; i < targetDirs.size(); i++) {
            double newTime = (distances.get(i) / (ros * scaledAngles.get(i))) * 60.0;
            String target = targetDirs.get(i);
            if ((outputTimers.get(target) == null) || (newTime < outputTimers.get(target)) && !(outputTimers.get(target) <= 0)) {
                outputTimers.put(target, newTime);
            }
        }
        outputTimers.put(initPos, 0.0);
        Logging.log("-- " + this.getName() + "| Timings were calculated! " + outputTimers.toString(), Logging.debug);
    }

    /**
     * Internal Transition Function
     * Tick down the timers each step with a value of sigma
     */
    public void deltint() {
        if (this.state.equals(State.BURNING)) {
            this.outputTimers.keySet().forEach(k -> {
                double newTime = Math.max(this.outputTimers.get(k) - this.getSigma(), 0);
                this.outputTimers.put(k, newTime);
            });
            Logging.log("-- " + this.getName() + "| Current timers: " + this.outputTimers, Logging.debug);
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
                    Logging.log("-- " + this.getName() + "| Sending ignite command to " + k, Logging.debug);
                    m.add(makeContent("out" + k, new Pair<>("status", "ignite")));
                    igniteSent.add(k);
                }
            }
            if (igniteSent.size() == outputTimers.size()) {
                state = State.BURNED;
                if (startFire) {
                    holdIn("START", INFINITY);
                } else {
                    holdIn(state.toString(), INFINITY);
                }
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
