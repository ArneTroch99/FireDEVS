/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//Collections Connections

//Standard Java API Imports
import java.io.FileWriter;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import GenCol.EntityInterface;
import facade.modeling.FCoupledModel;

/**
 * Controller.java
 * This class provides control function for the Tracking Environment	
 * Created on September 18, 2002, 4:18 PM
 * Modified with the integration of DEVSJAVA on May 29, 2008
 */

//M&S Connections (for load)//CREATE FACADE LOADER!!
import facade.modeling.FModel;
import facade.modeling.CAmodeling.FCASpaceModel;

import facade.simulation.FCoupledSimulator;
import facade.simulation.FSimulator;
import facade.simulation.CAsimulation.FCASimulator;

import facade.simulation.hooks.SimulatorHookListener;
import model.modeling.CAModels.TwoDimCellSpace;

import util.classUtils.DevsModelLoader;
import util.classUtils.LoadedDevsModel;
import util.db.DatabaseConnectionConfiguration;
import util.tracking.DatabaseTrackerManager;
import view.View;
import view.ViewInterface;
import view.ViewMenuControlsSubview;
import view.ViewMenuFileSubview;
import view.ViewMenuOptionsSubview;
import view.modelwizard.ModelWizardConfiguration;
import view.modelwizard.c.ModelAndPackageController;
import view.modelwizard.v.ComponentTrackingConfigurationPopupView;
import view.simView.SimView;

public class SimLauncher
    implements ControllerInterface, SimulatorHookListener
{
    private FSimulator simulator;
    private ViewInterface view;
    private Optional<DatabaseConnectionConfiguration> dbConfig;
    private Optional<DatabaseTrackerManager> dbTrackerManager;
    private TrackingDataHandler trackingDataHandler;
    private String lastUsedDirectory;
    static int sc;

    public static void main(String[] args)
    {
        new SimLauncher();
    }

    public SimLauncher()
    {
        view = new View(this);
        lastUsedDirectory = "";
        view.createLoadPage();
        
        dbConfig = Optional.empty();
        dbTrackerManager = Optional.empty();
    }

    @Override
    public FSimulator getSimulator()
    {
        return simulator;
    }

    @Override
    public ViewInterface getView()
    {
        return this.view;
    }

    @Override
    public void injectInputGesture(
        FModel model,
        String portName,
        EntityInterface input
    )
    {
        model.injectInput(portName, input);
    }

    public void userGesture(String gesture, Object params)
    {
        try
        {
            if (gesture.equals(SIM_RUN_GESTURE))
            {
                view.simlationControl(SIM_RUN_GESTURE);
                simulator.run();
                Stopwatch.start();
            }
            else if (gesture.equals(SIM_STEP_GESTURE))
            {
                view.simlationControl(SIM_STEP_GESTURE);
                simulator.step();
            }
            else if (gesture.equals(SIM_STEPN_GESTURE))
            {
                view.simlationControl(SIM_STEPN_GESTURE);
                simulator.step(((Integer) params).intValue());
            }
            else if (gesture.equals(SIM_PAUSE_GESTURE))
            {
                view.simlationControl(SIM_PAUSE_GESTURE);
                simulator.requestPause();
            }
            else if (gesture.equals(SIM_RESET_GESTURE))
            {
                // add by Chao for CAView
                if (view.isCASelected())
                {
                    view.getCatracking().getCAView().reset();
                }
				
                view.simlationControl(SIM_RESET_GESTURE);
                simulator.reset();
                resetTabbedPanel();
                view.loadSimulator(simulator);
                view.synchronizeView();
                Governor.reset();
                view.removeExternalWindows();
                reloadModelAction();
            }
            else if (gesture.equals(SIM_SET_RT_GESTURE))
            {
                simulator.setRTMultiplier(
                    ((Double) params).doubleValue()
                );
            }
            else if (gesture.equals(SIM_SET_TV_GESTURE))
            {
                Governor.setTV(((Double) params).doubleValue());
            }
            else if (gesture.equals(SAVE_TRACKING_LOG_GESTURE))
            {
                writeString(
                    (String) params,
                    trackingDataHandler.getHTMLTrackingLog()
                );
            }
            else if (gesture.equals(SAVE_CONSOLE_LOG_GESTURE))
            {
                writeString((String) params, view.getConsoleLog());
            }
            else if (gesture.equals(LOAD_MODEL_GESTURE))
            {
                resetTabbedPanel();
                try
                {
                    view.removeExternalWindows();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (gesture.equals(LOAD_MODEL_ASYNC))
            {
                resetTabbedPanel();
                /*
                 * Start the Progress Bar for Loading Model
                 */
                view.getModelLoadingProgressBar().setVisible(true);

                final SwingWorker worker = new SwingWorker() {
                    @Override
                    protected Void doInBackground() throws Exception
                    {
                        view.setSwingVisible(false);
                        return null;
                    }
                };

                worker.addPropertyChangeListener(
                    new PropertyChangeListener() {

                        @Override
                        public void propertyChange(
                            PropertyChangeEvent pcEvt
                        )
                        {
                            if (pcEvt.getNewValue() == SwingWorker.StateValue.DONE)
                            {
                                try
                                {
                                    worker.get();
                                    view.getModelLoadingProgressBar().setVisible(
                                        false
                                    );
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                );

                worker.execute();

                try
                {
                    view.removeExternalWindows();
                }
                catch (Exception e)
                {

                }
            }
            else if (gesture.equals(EXPORT_TO_CSV_GESTURE))
            {
                writeString(
                    (String) params,
                    trackingDataHandler.getCSVExport()
                );
            }
            else if (gesture.equals(EXPORT_TO_ENCODED_CSV_GESTURE))
            {
                String[] data = trackingDataHandler.getEncodedCSVExport();
                String[] paths = (String[]) params;
                writeString(paths[0], data[0]);
                writeString(paths[1], data[1]);
            }
            else if (gesture.equals(CLOSE_MODEL_LOADING_PROGRESS_BAR))
            {
                view.getModelLoadingProgressBar().setVisible(false);
            }
            else if (gesture.equals(RELOAD))
            {
                view.createLoadPage();
                view.restartConsole();
            }
        }
        catch (Exception e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    public void resetTabbedPanel()
    {
        view.removeAllTabs();
        view.addConsolePane();
    }

    public void systemExitGesture()
    {
        if (dbTrackerManager.isPresent() && !dbTrackerManager.get().areDatabaseOpsFinished())
        {
            if (!dbTrackerManager.get().showContinueDatabaseOperationsPrompt())
            {
                dbTrackerManager.get().stopDatabaseOperations(500);
            }
            else
            {
                while (!dbTrackerManager.get().areDatabaseOpsFinished())
                {
                    dbTrackerManager.get().setDatabaseOperationLoggingMode(true);
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        if(dbTrackerManager.isPresent())
        {
            dbTrackerManager.get().showCreatedViewName();
        }

        System.exit(0);
    }

    @Override
    public void postComputeInputOutputHook()
    {
        // This prevent the slow of the simulation
        if (view.isTrackingEnabled() || view.isCASelected())
        {
            //Change to time of last event, by Chao at 9.23.2019
            view.addTrackingAt(simulator.getTimeOfLastEvent());
        }       
        
        if (view.isCASelected())
        {
            view.synchronizeCAView();
        }
        else
        {
            view.synchronizeView();
        }
    }

    public void simulatorStateChangeHook()
    {
        // This prevent the slow of the simulation
        if (view.isCASelected())
        {
            view.synchronizeCAView();
        }
        view.synchronizeView();
    }

    private void writeString(String path, String stringToWrite)
    {
        try
        {
            FileWriter fw = new FileWriter(path);
            fw.write(stringToWrite);
            fw.close();
        }
        catch (Exception e)
        {
            System.err.println(
                "An Error Occured While Writing: " + path
            );
            System.err.println(e);
        }
    }

    public void registerTrackingDataHandler(
        TrackingDataHandler trackingHandler
    )
    {
        trackingDataHandler = trackingHandler;
    }

    @Override
    public void initializeSimulator(LoadedDevsModel metadata)
    {
        FCoupledModel rootModel;
        if (view.isCASelected())
        {
            TwoDimCellSpace model = (TwoDimCellSpace) metadata.instanceModel;

            rootModel = new FCASpaceModel(model);
            
            simulator = new FCASimulator(
                model,
                rootModel,
                SimView.modelView,
                metadata.modelType
            );

        }
        else
        {
            rootModel = new FCoupledModel(metadata.instanceModel);

            simulator = new FCoupledSimulator(
                metadata.instanceModel,
                rootModel,
                SimView.modelView,
                metadata.modelType
            );
        }

        simulator.setSimulatorHookListener(this);

        rootModel.setSimulator(simulator);

        if (view.isCASelected())
        {
            view.getCatracking().loadSimModel(rootModel);
        }
        else if (view.isTrackingEnabled())
        {
            view.getTrackingControl().loadSimModel(rootModel);
        }

        view.getModelView().loadModel(simulator.getRootModel());
        view.loadSimulator(simulator);
    }
    
    private void reloadModelSwing()
    {
        int option = JOptionPane.showConfirmDialog(
            view.getMainJFrame(),
            "Reload current model? (All log data will be lost)",
            "Reload Model...",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (option == JOptionPane.OK_OPTION)
        {
            reloadModelAction();
        }
    }

    /**
     * Reload a model
     */
    private void reloadModelAction()
    {
        LoadedDevsModel data = DevsModelLoader.loadModelClass(
            view.getSelectedPackage(),
            ModelAndPackageController.removeDecorators(view.getSelectedModel()),
            view.getSelectedInit()
        );
        DevsModelLoader.initializeSimulatorWithModel(view, data);
    }

    private void saveTrackingReportAction()
    {
        JFileChooser chooser = new JFileChooser(lastUsedDirectory);
        chooser.setDialogTitle("Save " + View.TRACKING_LOG_TAB_NAME + " (.html)");
        if (chooser.showSaveDialog(
            view.getMainJFrame()
        ) == JFileChooser.APPROVE_OPTION)
        {
            lastUsedDirectory = chooser.getSelectedFile().getAbsolutePath();
            String path = lastUsedDirectory;
            String tst = path.toLowerCase();
            if (!(tst.endsWith(".htm") || tst.endsWith(".html")))
            {
                path = path + ".html";
            }
            userGesture(
                ControllerInterface.SAVE_TRACKING_LOG_GESTURE,
                path
            );
        }
    }

    private void exportCSVAction()
    {
        if (view.getModelView().getSelectedModel() == null)
        {
            JOptionPane.showMessageDialog(
                view.getMainJFrame(),
                "Cannot Export, No Model Selected.",
                "Please choose a model first...",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser(lastUsedDirectory);
        chooser.setDialogTitle("Export CSV Tracking Data (.csv)");

        if (chooser.showSaveDialog(
            view.getMainJFrame()
        ) == JFileChooser.APPROVE_OPTION)
        {
            lastUsedDirectory = chooser.getSelectedFile().getAbsolutePath();
            String path = lastUsedDirectory;
            String tst = path.toLowerCase();
            if (!(tst.endsWith(".csv")))
            {
                path = path + ".csv";
            }
            userGesture(
                ControllerInterface.EXPORT_TO_CSV_GESTURE,
                path
            );
        }
    }

    private void exportEncodedCSVAction()
    {
        if (view.getModelView().getSelectedModel() == null)
        {
            JOptionPane.showMessageDialog(
                view.getMainJFrame(),
                "Cannot Export, No Model Selected.",
                "Please choose a model first...",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser(lastUsedDirectory);
        chooser.setDialogTitle(
            "Export Encoded CSV Tracking Data (.csv)"
        );

        if (chooser.showSaveDialog(
            view.getMainJFrame()
        ) == JFileChooser.APPROVE_OPTION)
        {
            lastUsedDirectory = chooser.getCurrentDirectory().getAbsolutePath();
            String path = lastUsedDirectory;
            String tst = path.toLowerCase();

            String[] exportPaths = new String[2];

            if (tst.endsWith(".htm") || tst.endsWith(".html"))
            {
                exportPaths[0] = path;
            }
            else
            {
                exportPaths[0] = path + ".html";
            }

            if (tst.endsWith(".csv"))
            {
                exportPaths[1] = path;
            }
            else
            {
                exportPaths[1] = path + ".csv";
            }

            userGesture(
                    ControllerInterface.EXPORT_TO_ENCODED_CSV_GESTURE,
                    exportPaths
                );
            }
        }

    private void clearConsoleAction()
    {
        int option = JOptionPane.showConfirmDialog(
            view.getMainJFrame(),
            "Clear All Console Data?",
            "Confirm Clear...",
            JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION)
        {
            view.getConsole().clearConsole();
        }
    }

    private void saveConsoleAction()
    {
        userGesture(
            ControllerInterface.SAVE_CONSOLE_LOG_GESTURE,
            view.getConsoleLog()
        );
    }

    private void loadModelAction()
    {
        view.openModelWizard(new ModelWizardConfiguration());
    }

    private void aboutBoxAction()
    {
        view.showAboutBox();
    }

    private void stepAction()
    {
        userGesture(ControllerInterface.SIM_STEP_GESTURE, null);
    }

    private void stepNAction()
    {
        String val = JOptionPane.showInputDialog(
            view.getMainJFrame(),
            "Number of steps to iterate: "
        );
        if (val != null)
        {
            try
            {
                Integer i = Integer.parseInt(val);
                userGesture(ControllerInterface.SIM_STEPN_GESTURE, i);
            }
            catch (Exception exp)
            {
                System.err.println(exp);
            }
        }
    }

    private void runAction()
    {
        userGesture(ControllerInterface.SIM_RUN_GESTURE, null);
    }

    private void pauseAction()
    {
        userGesture(ControllerInterface.SIM_PAUSE_GESTURE, null);
    }

    private void resetAction()
    {
        String msg = "Reset this Model?\n";
        msg += "All Tracking Data Will Be Lost";
        int option = JOptionPane.showConfirmDialog(
            view.getMainJFrame(),
            msg,
            "Reset Model?",
            JOptionPane.YES_NO_OPTION
        );
        if (option == JOptionPane.YES_OPTION)
        {
            userGesture(ControllerInterface.SIM_RESET_GESTURE, null);
        }
    }

    public WindowAdapter onWindowClosed = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent evt)
        {
            if (view.isSimView())
            {
                view.getSim().get().saveModelLayout();
            }
            systemExitGesture();
        }
    };

    public ActionListener onResetClicked = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            resetAction();
        }
    };

    public ActionListener onPauseClicked = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            pauseAction();
        }
    };

    public ActionListener onRunClicked = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            runAction();
        }
    };

    public ActionListener onStepNClicked = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            stepNAction();
        }
    };

    public ActionListener onStepClicked = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            stepAction();
        }
    };

    public ActionListener onNewSelected = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            // if no model is selected, then call LoadModel()
            // else reload a model
            if (view.getSelectedPackage() == null
                && view.getSelectedModel() == null)
            {
                loadModelAction();
            }
            else
            {
                reloadModelSwing();
            }
        }
    };

    public ActionListener onAboutSelected = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            aboutBoxAction();
        }
    };

    public ActionListener onConsoleSettingsSelected = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            view.getConsole().customizeComponent(
                view.getMainJFrame());
        }
    };

    public ActionListener onLoadModelSelected = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            loadModelAction();
        }
    };

    public ActionListener onSaveConsoleSelected = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            saveConsoleAction();
        }
    };

    public ActionListener onClearConsoleSelected = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            clearConsoleAction();
        }
    };

    public ActionListener onMenuSelected = new ActionListener() {
        @Override
        public void actionPerformed(
            java.awt.event.ActionEvent actionEvent)
        {
            String cmd = actionEvent.getActionCommand();
            if (cmd.equalsIgnoreCase(
                ViewMenuFileSubview.MenuItem.SaveTrackingLog.toString()
                ))
            {
                saveTrackingReportAction();
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuFileSubview.MenuItem.SaveConsole.toString()))
            {
                saveConsoleAction();
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuFileSubview.MenuItem.ExportToCSV.toString()))
            {
                exportCSVAction();
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuFileSubview.MenuItem.ExportToEncodedCSV.toString()
                ))
            {
                exportEncodedCSVAction();
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuOptionsSubview.MenuItem.ConsoleSettings.toString()
                ))
            {
                view.getConsole().customizeComponent(
                    view.getMainJFrame());
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuOptionsSubview.MenuItem.ClearConsole.toString()
                ))
            {
                clearConsoleAction();
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuOptionsSubview.MenuItem.TrackingLogSettings.toString()
                ))
            {
                view.getTrackingControl().trackingLogOption(
                    view.getMainJFrame(),
                    cmd);
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuFileSubview.MenuItem.LoadModel.toString()))
            {
                loadModelAction();
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuOptionsSubview.MenuItem.RefreshTrackingLog.toString()
                ))
            {
                view.getTrackingControl().trackingLogOption(
                    view.getMainJFrame(),
                    cmd);
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuControlsSubview.MenuItem.Step.toString()))
            {
                userGesture(
                    ControllerInterface.SIM_STEP_GESTURE,
                    null);
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuControlsSubview.MenuItem.Step_n.toString()))
            {
                String val = JOptionPane.showInputDialog(
                    view.getMainJFrame(),
                    "Number of steps to iterate: ");
                if (val != null)
                {
                    try
                    {
                        Integer i = Integer.parseInt(val);
                        userGesture(
                            ControllerInterface.SIM_STEPN_GESTURE,
                            i);
                    }
                    catch (Exception exp)
                    {
                        System.err.println(exp);
                    }
                }
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuControlsSubview.MenuItem.Run.toString()))
            {
                userGesture(
                    ControllerInterface.SIM_RUN_GESTURE,
                    null);
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuControlsSubview.MenuItem.Pause.toString()))
            {
                userGesture(
                    ControllerInterface.SIM_PAUSE_GESTURE,
                    null);
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuControlsSubview.MenuItem.Reset.toString()))
            {
                resetAction();
            }
            else if (cmd.equalsIgnoreCase("About"))
            {
                aboutBoxAction();
            }
            else if (cmd.equalsIgnoreCase(
                ViewMenuFileSubview.MenuItem.Exit.toString()))
            {
                view.getSim().get().saveModelLayout();
                System.exit(0);
            }
            else if (cmd.equals(
                ViewMenuOptionsSubview.MenuItem.ChangeTrackingSettings.toString()
                ))
            {
                ComponentTrackingConfigurationPopupView trackingConfig = new ComponentTrackingConfigurationPopupView(
                    view,
                    view.isDBVisible());
                trackingConfig.setVisible(true);
            }
        }
    };

    @Override
    public void setDatabaseConfiguration(
        DatabaseConnectionConfiguration dbConfig
    )
    {
        this.dbConfig = Optional.ofNullable(dbConfig);
    }

    @Override
    public Optional<DatabaseConnectionConfiguration> getDatabaseConnectionConfiguration()
    {
        return dbConfig;
    }

    @Override
    public void setDatabaseTrackerManager(
        DatabaseTrackerManager dbTrackerManager
    )
    {
        this.dbTrackerManager = Optional.ofNullable(dbTrackerManager);
    }
}
