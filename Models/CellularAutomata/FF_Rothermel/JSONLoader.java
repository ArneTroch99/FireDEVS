package CellularAutomata.FF_Rothermel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JSONLoader {

    private static Gson gson = new GsonBuilder().create();

    public static void main(String[] args) {
        JSONLoader.loadFuelModels("FuelData/FuelModels.json");
    }

    public static Map<String, FuelModel> loadFuelModels(String filePath){

        String jsonString = readJSON(filePath);
        Type empMapType = new TypeToken<Map<String, FuelModel>>() {}.getType();
        Map<String, FuelModel> fuelModels = gson.fromJson(jsonString, empMapType);

        fuelModels.values().forEach(fuelModel -> fuelModel.setFine_fuel_load(
                AbsurdUnitConverter.t_ac_to_lb_ftsqrd(fuelModel.getFine_fuel_load())));

        return fuelModels;
    }

    public static Map<Integer, String> loadVegetationColours(String filePath){

        Map<Integer, String> result = new HashMap<>();

        // Load a map with the fuel type as key and the fuel color (rgb array) as value
        String jsonString = readJSON(filePath);
        Type empMapType = new TypeToken<Map<String, byte[]>>() {}.getType();
        Map<String, byte[]> fuelColors = gson.fromJson(jsonString, empMapType);

        // Convert the rgb value from an array to one (4 byte) integer, where the first byte is alpha = 255
        byte[] rgb;
        for (Map.Entry<String, byte[]> entry: fuelColors.entrySet()){
            rgb = entry.getValue();
            int argb = 0;
            argb += -16777216; // 255 alpha
            argb += ((int) rgb[2] & 0xff); // blue
            argb += (((int) rgb[1] & 0xff) << 8); // green
            argb += (((int) rgb[0] & 0xff) << 16); // red
            result.put(argb, entry.getKey());       // Create the inverse hash map as result
        }
        return result;
    }

    private static String readJSON(String filePath){
        try {
            return new String(Files.readAllBytes(Paths.get(filePath))).trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
