package FF_Rothermel;

import util.Logging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FireEnvironment {

    private final Map<String, Double> parameters = new HashMap<>();

    public static void main(String[] args) {
        FireEnvironment env = new FireEnvironment_TCP();
        env.connect();
        System.out.println(env.getParameters());
        env.disconnect();
    }

    // Abstract methods: these should be implemented depending un the underlying protocol
    public abstract void connect();
    public abstract void disconnect();
    protected abstract String receiveStringData();

    // Common method
    public Map<String, Double> getParameters(){
        String dataString;

        try {
             dataString = this.receiveStringData();
        }
        catch (NullPointerException e){
            Logging.log("ERROR: no data was received, providing stub data!:\n" + e.toString(), Logging.error);
            stub();
            return parameters;
        }

        List<String> parameterStrings = Arrays.asList(dataString.split("\\$"));

        parameterStrings.stream().skip(1).forEach(parameterString -> {
            String[] splitted = parameterString.split((":"));
            try {
                parameters.put(splitted[0].trim().toLowerCase(), Double.parseDouble(splitted[1]));
            }
            catch (ArrayIndexOutOfBoundsException e1){
                Logging.log("ERROR: The provided data could not be parsed, probably a ':' is missing " +
                        "between the parameter name and value. Stub data is filled in: \n" + e1.toString(), Logging.error);
            }
            catch (NumberFormatException e2){
                Logging.log("ERROR: The provided data could not be parsed, stub data is filled in:\n" + e2.toString(), Logging.error);
            }
        });

        stub();
        return parameters;
    }

    private void stub(){
        if(!parameters.containsKey("moisture")){
            parameters.put("moisture", 0.4);
        }
        if(!parameters.containsKey("winddir")){
            parameters.put("winddir", 1.4);
        }
        if(!parameters.containsKey("windspeed")){
            parameters.put("windspeed", 25.0);
        }
    }

}
