package FF_Rothermel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FireEnvironment {

    public static void main(String[] args) {
        FireEnvironment env = new FireEnvironment_TCP();
        env.connect();
        env.getParameters();
        env.disconnect();
    }

    // Abstract methods: these should be implemented depending un the underlying protocol
    public abstract void connect();
    public abstract void disconnect();
    protected abstract String receiveStringData();

    // Common method
    public Map<String, Double> getParameters(){
        Map<String, Double> parameters = new HashMap<>();
        String dataString = this.receiveStringData();
        List<String> parameterStrings = Arrays.asList(dataString.split("\\$"));

        parameterStrings.stream().skip(1).forEach(parameterString -> {
            String[] splitted = parameterString.split((":"));
            parameters.put(splitted[0].trim().toLowerCase(), Double.parseDouble(splitted[1]));
        });

        return parameters;
    }

}
