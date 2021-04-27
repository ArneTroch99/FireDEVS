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

    /**
     * This method should be implemented to open a connection with a parameter server. This connection should be used
     * by "receiveStringData()" to receive data from this connection.
     */
    public abstract void connect();

    /**
     * This method should be implemented to close the connection that was opened by "connect()".
     */
    public abstract void disconnect();

    /**
     * This method reads data from the connection that was opened by "connect()". The data should be read as a line
     * that ends with "\n" (eg. using the readLine() of the BufferedReader class).
     * @return The received string from the connection.
     */
    protected abstract String receiveStringData();

    // Common method

    /**
     * This method get the dynamic parameters from an underlying communication stack.
     * @return parameters A method map that contains dynamic parameter names as key and their value as value.
     */
    public Map<String, Double> getParameters() {
        String dataString;

        try {
            dataString = this.receiveStringData();
        } catch (NullPointerException e) {
            Logging.log("ERROR: no data was received, providing stub data!:\n" + e.toString(), Logging.error);
            stub();
            return parameters;
        }

        List<String> parameterStrings = Arrays.asList(dataString.split("\\$"));

        parameterStrings.stream().skip(1).forEach(parameterString -> {
            String[] splitted = parameterString.split((":"));
            try {
                parameters.put(splitted[0].trim().toLowerCase(), Double.parseDouble(splitted[1]));
            } catch (ArrayIndexOutOfBoundsException e1) {
                Logging.log("ERROR: The provided data could not be parsed, probably a ':' is missing " +
                        "between the parameter name and value. Stub data is filled in: \n" + e1.toString(), Logging.error);
            } catch (NumberFormatException e2) {
                Logging.log("ERROR: The provided data could not be parsed, stub data is filled in:\n" + e2.toString(), Logging.error);
            }
        });

        stub();
        return parameters;
    }

    private void stub(){
        if(!parameters.containsKey("moisture")){
            parameters.put("moisture", 0.1);
        }
        if (!parameters.containsKey("winddir")) {
            parameters.put("winddir", 1.75);
        }
        if (!parameters.containsKey("windspeed")) {
            parameters.put("windspeed", 25.0);
        }
    }

}
