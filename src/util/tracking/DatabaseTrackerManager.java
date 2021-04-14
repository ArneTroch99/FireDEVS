package util.tracking;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import view.DatabaseTracker;
import view.Tracker;

public class DatabaseTrackerManager
{
    private List<Tracker> trackers;
    public DatabaseTrackerManager()
    {
        trackers = new ArrayList<Tracker>();
    }
    
    public boolean areDatabaseOpsFinished()
    {
        return !getDatabaseTrackers().filter((DatabaseTracker t) -> !t.isDatabaseOpsFinished()).findFirst().isPresent();
    }
    
    public void stopDatabaseOperations(int timeout)
    {
        getDatabaseTrackers().forEach((DatabaseTracker t) -> t.killAllDatabaseWorkers(timeout));
    }
    
    private Stream<DatabaseTracker> getDatabaseTrackers()
    {
        return trackers.stream().filter((Tracker t) -> t.isDatabaseTrackingEnabled()).map((Tracker t) -> (DatabaseTracker) t);
    }
    
    public void addTrackers(List<Tracker> trackers)
    {
        this.trackers.addAll(trackers);
    }
    
    /**
     * Shows option pane prompt asking the user if they want to 
     * wait for database operations to finish.
     * 
     * @return true if user wants to wait for database operations to finish
     */
    public boolean showContinueDatabaseOperationsPrompt()
    {
        return JOptionPane.showConfirmDialog(null, "There are running database operations. Do you want to wait?", "Continue database operations?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    
    public void showCreatedViewName()
    {
        Optional<DatabaseTracker> dbTracker = getDatabaseTrackers().findFirst();
        
        if (dbTracker.isPresent())
        {
            JOptionPane.showMessageDialog(null, "The view created for this simulation is called: simulation_" + dbTracker.get().getSimulationNumber()); 
        }
    }
    
    public void setDatabaseOperationLoggingMode(boolean shouldLog)
    {
        getDatabaseTrackers().forEach((DatabaseTracker t) -> t.setDatabaseOperationLoggingMode(shouldLog));
    }
}
