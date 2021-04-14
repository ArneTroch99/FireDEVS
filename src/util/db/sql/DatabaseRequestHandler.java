package util.db.sql;

import java.util.List;
import java.util.Map;

import facade.modeling.FModel;

public interface DatabaseRequestHandler
{
    public int getLastSimulationRunNumber();

    public void createTables();
    public void createViews();

    public void saveSimulationData(
        int simulationNumber,
        String timeString,
        int timeIndex,
        String tL,
        String tN
    );
    
    public void saveModelStateAndIO(
        FModel model,
        Map<String, Object> stateData,
        Map<String, List<Object>> inputPortData,
        Map<String, List<Object>> outputPortData
    );
    
    public boolean isWorking();
    
    public void killAllWorkers(int timeout);
    public void setLoggingMode(boolean shouldLog);
}
