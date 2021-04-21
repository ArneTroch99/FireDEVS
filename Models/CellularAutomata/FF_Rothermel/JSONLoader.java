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
        JSONLoader.loadVegetationColours("/home/siemen/FireDEVS/fuelColors.json");
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
        Type empMapType = new TypeToken<Map<String, short[]>>() {}.getType();
        Map<String, short[]> fuelColors = gson.fromJson(jsonString, empMapType);

        // Convert the rgb value from an array to one (4 byte) integer, where the first byte is alpha = 255
        Integer colorInteger;
        short[] rgb;
        for (Map.Entry<String, short[]> entry: fuelColors.entrySet()){
            rgb = entry.getValue();
            colorInteger = ((byte)0xFF << 24) & (rgb[0] << 16) & (rgb[1] << 8) & rgb[2];

            result.put(colorInteger, entry.getKey());       // Create the inverse hash map as result
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
