package CellularAutomata.FF_Rothermel;

public class FuelModel {

    private double fine_fuel_load;
    private double SAV;
    private double packing_ratio;
    private double extinction_moisture_content;

    public FuelModel(double fine_fuel_load, double SAV, double packing_ratio, double extinction_moisture_content) {
        this.fine_fuel_load = fine_fuel_load;
        this.SAV = SAV;
        this.packing_ratio = packing_ratio;
        this.extinction_moisture_content = extinction_moisture_content;
    }



    // Getters and setters: ...

    public double getFine_fuel_load() {
        return fine_fuel_load;
    }

    public void setFine_fuel_load(double fine_fuel_load) {
        this.fine_fuel_load = fine_fuel_load;
    }

    public double getSAV() {
        return SAV;
    }

    public void setSAV(double SAV) {
        this.SAV = SAV;
    }

    public double getPacking_ratio() {
        return packing_ratio;
    }

    public void setPacking_ratio(double packing_ratio) {
        this.packing_ratio = packing_ratio;
    }

    public double getExtinction_moisture_content() {
        return extinction_moisture_content;
    }

    public void setExtinction_moisture_content(double extinction_moisture_content) {
        this.extinction_moisture_content = extinction_moisture_content;
    }
}
