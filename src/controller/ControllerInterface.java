/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package controller;

import java.util.Optional;

import GenCol.EntityInterface;
import facade.modeling.FModel;
import facade.simulation.FSimulator;
import util.classUtils.LoadedDevsModel;
import util.db.DatabaseConnectionConfiguration;
import util.tracking.DatabaseTrackerManager;
import view.ViewInterface;

/**
 * Controller Interface
 * Provides the interface of the controller
 */
public interface ControllerInterface 
{
    public static final String SIM_RUN_GESTURE      = "RUN";
    public static final String SIM_STEP_GESTURE     = "STEP";
    public static final String SIM_STEPN_GESTURE    = "STEPN";
    public static final String SIM_PAUSE_GESTURE    = "PAUSE";
    public static final String SIM_RESET_GESTURE    = "RESET";
    public static final String SIM_SET_RT_GESTURE   = "SET_RT";
    public static final String SIM_SET_TV_GESTURE	= "SET_TV";
    
    public static final String LOAD_MODEL_GESTURE        = "LOAD_MODEL";
    public static final String LOAD_MODEL_ASYNC          = "LOAD_MODEL_ASYNC";
    public static final String SAVE_TRACKING_LOG_GESTURE = "SAVE_TLOG";
    public static final String SAVE_CONSOLE_LOG_GESTURE  = "SAVE_CLOG";
    public static final String EXPORT_TO_CSV_GESTURE     = "EXPORT_CSV";
    public static final String EXPORT_TO_ENCODED_CSV_GESTURE = "EXPORT_ENCODED_CSV";

    public static final String CLOSE_MODEL_LOADING_PROGRESS_BAR = "CLOSE_MODEL_LOADING_PROGRESS_BAR";
    public static final String RELOAD = "RELOAD";
    
    public void userGesture(String gesture, Object params);
    public void injectInputGesture(FModel model, String portName, EntityInterface value);
    public void systemExitGesture();
    public FSimulator getSimulator();
    public void registerTrackingDataHandler(TrackingDataHandler trackingHandler);

    public void initializeSimulator(LoadedDevsModel data);
    public ViewInterface getView();
    
    public Optional<DatabaseConnectionConfiguration> getDatabaseConnectionConfiguration();
    public void setDatabaseConfiguration(DatabaseConnectionConfiguration dbConfig);
    public void setDatabaseTrackerManager(DatabaseTrackerManager dbTrackerManager);
}
