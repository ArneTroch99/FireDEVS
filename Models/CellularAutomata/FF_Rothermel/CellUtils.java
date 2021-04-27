package CellularAutomata.FF_Rothermel;

import java.util.HashMap;
import java.util.Map;

public class CellUtils {

    private static final HashMap<String, String[]> dirOutputs = new HashMap<>(8);
    private static final Map<String, double[]> dirCoordinates = new HashMap<>(8);
    private static double maxWindSpeed;

    public static void init(double cellWidth, double cellHeight, double maxWindSpeed) {
        dirOutputs.put("N", new String[]{"NE", "E", "SE", "S", "SW", "W", "NW"});
        dirOutputs.put("E", new String[]{"N", "NE", "SE", "S", "SW", "W", "NW"});
        dirOutputs.put("S", new String[]{"N", "NE", "E", "SE", "SW", "W", "NW"});
        dirOutputs.put("W", new String[]{"N", "NE", "E", "SE", "S", "SW", "NW"});
        dirOutputs.put("NE", new String[]{"SE", "S", "SW", "W", "NW"});
        dirOutputs.put("SE", new String[]{"N", "NE", "SW", "W", "NW"});
        dirOutputs.put("SW", new String[]{"N", "NE", "E", "SE", "NW"});
        dirOutputs.put("NW", new String[]{"NE", "E", "SE", "S", "SW"});
        dirOutputs.put("C", new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"});

        dirCoordinates.put("N", new double[]{cellWidth / 2, 0});
        dirCoordinates.put("E", new double[]{cellWidth, -cellHeight / 2});
        dirCoordinates.put("S", new double[]{cellWidth / 2, -cellHeight});
        dirCoordinates.put("W", new double[]{0, -cellHeight / 2});
        dirCoordinates.put("NE", new double[]{cellWidth, 0});
        dirCoordinates.put("SE", new double[]{cellWidth, -cellHeight});
        dirCoordinates.put("SW", new double[]{0, -cellHeight});
        dirCoordinates.put("NW", new double[]{0, 0});
        dirCoordinates.put("C", new double[]{cellWidth / 2, -cellHeight / 2});

        CellUtils.maxWindSpeed = maxWindSpeed;

    }

    public static String[] getOutputDirections(String inputDir) {
        return dirOutputs.get(inputDir);
    }

    public static double[] getCoordinates(String dir) {
        return dirCoordinates.get(dir);
    }

    public static double getWindDir(){
        return -Math.PI / 4;
    }

    public static double getWindSpeed(){
        return AbsurdUnitConverter.km_h_to_ft_min(20);
    }

    public static double getMoisture(){
        return 0.14;
    }

    public static double getE(){
        return AbsurdUnitConverter.km_h_to_ft_min(20)/maxWindSpeed;
    }

}
