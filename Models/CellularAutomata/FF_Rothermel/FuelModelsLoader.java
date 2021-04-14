package CellularAutomata.FF_Rothermel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;


public class FuelModelsLoader {

    public static void main(String[] args) {
        FuelModelsLoader.loadFuelModels("/home/siemen/CPS-II/FuelModels.json");
    }

    public static Map<String, FuelModel> loadFuelModels(String filePath){
        Gson gson = new GsonBuilder().create();
        String jsonString = "";

        try {
            jsonString = new String(Files.readAllBytes(Paths.get(filePath))).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Type empMapType = new TypeToken<Map<String, FuelModel>>() {}.getType();
        Map<String, FuelModel> fuelModels = gson.fromJson(jsonString, empMapType);

        fuelModels.values().forEach(fuelModel -> fuelModel.setFine_fuel_load(
                AbsurdUnitConverter.t_ac_to_lb_ftsqrd(fuelModel.getFine_fuel_load())));

        return fuelModels;
    }
}
