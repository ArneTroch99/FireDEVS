package CellularAutomata.FF_Rothermel;

import model.modeling.CAModels.TwoDimCell;
import model.modeling.content;
import model.modeling.message;

import java.util.*;
import java.util.stream.Collectors;

public class MoorCell extends TwoDimCell {

    HashMap<String, String[]> outputDirs = new HashMap<>(8);  // The output directions for each possible input location
    // Rothermel variables
    private double w_o;
    private double sigma;
    private double h;
    private double M_f;
    private double U;
    private double beta;
    private double M_x;
    private ROS_Calculator rosCalculator;
    // Cell variables
    private double width = 5;
    private double height = 5;
    private String state;  // Possible states: unburned, unburnable, burning, burned
    private double windDir = -Math.PI / 2;  // The direction of the wind
    private List<String> ignitePositions = new ArrayList<>();  // The positions where the cell has been ignited
    // The timers to keep track of when a certain output will be ignited
    // NW N NE
    // W     E
    // SW S SE
    private Map<String, Double> outputTimers = new HashMap<>();
    // The coordinate positions of the different directions
    // (0, 0)  (0, w/2) (0, w)
    // (h/2, 0)         (h/2, w)
    // (h, 0)  (h, w/2) (h, w)
    private Map<String, double[]> dirPositions = new HashMap<>(8);


    public MoorCell() {
        this(0, 0);
    }

    public MoorCell(int xcoord, int ycoord) {
        super(xcoord, ycoord);
    }

    public MoorCell(int xcoord, int ycoord, double w_o, double sigma, double h, double M_f, double U, double beta, double M_x, double width, double height, ROS_Calculator rosCalculator) {
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
        MoorCell cell = new MoorCell();
        cell.initialize();
        cell.calculateTimers("N");
    }


    /**
     * Initialization method
     */
    public void initialize() {
        //super.initialize();
        state = "unburned";
        outputDirs.put("N", new String[]{"NE", "E", "SE", "S", "SW", "W", "NW"});
        outputDirs.put("E", new String[]{"N", "NE", "SE", "S", "SW", "W", "NW"});
        outputDirs.put("S", new String[]{"N", "NE", "E", "SE", "SW", "W", "NW"});
        outputDirs.put("W", new String[]{"N", "NE", "E", "SE", "S", "SW", "NW"});
        outputDirs.put("NE", new String[]{"SE", "S", "SW", "W", "NW"});
        outputDirs.put("SE", new String[]{"N", "NE", "SW", "W", "NW"});
        outputDirs.put("SW", new String[]{"N", "NE", "E", "SE", "NW"});
        outputDirs.put("NW", new String[]{"NE", "E", "SE", "S", "SW"});

        dirPositions.put("N", new double[]{0, width / 2});
        dirPositions.put("E", new double[]{-height / 2, width});
        dirPositions.put("S", new double[]{-height, width / 2});
        dirPositions.put("W", new double[]{-height / 2, 0});
        dirPositions.put("NE", new double[]{0, width});
        dirPositions.put("SE", new double[]{-height, width});
        dirPositions.put("SW", new double[]{-height, 0});
        dirPositions.put("NW", new double[]{0, 0});

    }

    /**
     * External Transition Function
     * <p>
     * Receive an ignite command from a neighbouring cell. Store this starting position of ignition.
     */
    public void deltext(double e, message x) {
        Continue(e);
        for (Object o : x) {
            content c = (content) o;
            if (!state.equals("unburnable")) {
                if (c.getValue().equals("ignite")) {
                    System.out.println(this.getName() + "| received ignite command from " + c.getPortName());
                    ignitePositions.add(c.getPortName());
                    System.out.println(this.getName() + "| transitioning to burning state");
                    state = "burning";
                    System.out.println(this.getName() + "| setting original timers");
                    calculateTimers(c.getPortName());
                }
            }
            holdIn(state, 1);
        }
    }

    /**
     * Calculate the timers for the edges of the cell to be reached based on ignite position, ROS and wind direction/speed
     */
    public void calculateTimers(String initPos) {
        List<String> targetDirs = Arrays.stream(outputDirs.get(initPos)).collect(Collectors.toList());

        // Calculate the angles and distances between the initial position and the target directions
        List<Double> angles = new ArrayList<>(targetDirs.size());
        List<Double> distances = new ArrayList<>(targetDirs.size());
        for (String t : targetDirs) {
            angles.add(Math.atan2((dirPositions.get(t)[1] - dirPositions.get(initPos)[1]), (dirPositions.get(t)[0] - dirPositions.get(initPos)[0])));
            distances.add(Math.sqrt((dirPositions.get(initPos)[0] - dirPositions.get(t)[0]) * (dirPositions.get(initPos)[0] - dirPositions.get(t)[0]) + (dirPositions.get(initPos)[1] - dirPositions.get(t)[1]) * (dirPositions.get(initPos)[1] - dirPositions.get(t)[1])));
        }
        System.out.println("-- " + this.getName() + "| Angles were calulated! " + angles.toString());
        System.out.println("-- " + this.getName() + "| Distances were calulated! " + distances.toString());

        // Update the angles to the "new" coordinate system
        angles = angles.stream().map(a -> {
            double angleDiff = a - windDir;
            System.out.println(angleDiff);
            angleDiff += (angleDiff > Math.PI) ? (-2 * Math.PI) : (a < -Math.PI) ? (2 * Math.PI) : 0;
            return angleDiff + Math.PI;
        }).collect(Collectors.toList());
        System.out.println("-- " + this.getName() + "| Angles were adjusted! " + angles.toString());

        // Calculate the scaled angles using the ellipse
        double e = 0.9;
        List<Double> scaledAngles = new ArrayList<>(targetDirs.size());
        for (Double a : angles) {
            scaledAngles.add((1 - e * e) * (1 - e * Math.cos(a)));
        }
        System.out.println("-- " + this.getName() + "| Angles were scaled! " + scaledAngles.toString());
        double maxAngle = ((1 - e * e) * (1 - e * Math.cos(-Math.PI / 2)));
        scaledAngles = scaledAngles.stream().map(a -> a / maxAngle).collect(Collectors.toList());
        System.out.println("-- " + this.getName() + "| Angles were normalized! " + scaledAngles.toString());

        // Calculate the current ROS
        double ros = 4;
        for (int i = 0; i < targetDirs.size(); i++) {
            outputTimers.put(targetDirs.get(i), distances.get(i) / (ros * scaledAngles.get(i)));
        }
        System.out.println("-- " + this.getName() + "| Timings were calculated! " + outputTimers.toString());


    }


    /**
     * Internal Transition Function
     */
    public void deltcon(double e, message x) {
        if (state.equals("burning")) {

        }
    }
}
