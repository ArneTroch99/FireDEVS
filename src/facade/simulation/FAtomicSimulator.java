/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.simulation;

//Model Connections
import model.modeling.DevsInterface;
import model.modeling.IOBasicDevs;
import model.modeling.atomic;
import controller.simulation.*;
import view.modeling.ViewableAtomic;
import view.simView.*;
import view.timeView.*;
import facade.modeling.FAtomicModel;
import facade.modeling.FModel;
import facade.simulation.hooks.SimulatorHookListener;

/**
 * FAtomic Simulator : extended from ViewableAtomicSimulator
 * @modified Sungung Kim 05/29/2008
 */

public class FAtomicSimulator implements FSimulator
{
    private FAtomicSimulator.FAtomicSimulatorX simulator;
    private FModel rootModel;
    private short currentState;
    
    public FAtomicSimulator(ViewableAtomic model)
    {
        simulator = new FAtomicSimulator.FAtomicSimulatorX(model);
        rootModel = new FAtomicModel(model);
    }
  
    public void step()                          {simulator.simulate(1);}
    public void step(int n)                     {simulator.simulate(n);}
    public void run()                           {simulator.simulate(Integer.MAX_VALUE);}
    public void requestPause()                  {simulator.requestPause();}  
    public void reset()                         {simulator.reset();}
    public void setRTMultiplier(double factor)  {simulator.setRTMultiplier(factor);}
    public double getRTMultiplier()             {return simulator.getRTMultiplier();}
    public double getTimeOfLastEvent()          {return simulator.getTL();}
    public double getTimeOfNextEvent()          {return simulator.getTN();}
    public short  getCurrentState()             {return currentState;}
    public FModel getRootModel()                {return rootModel;}
    
    public void setSimulatorHookListener(SimulatorHookListener listener) 
    {
        
    }
    
    //------------------------------------------------------------------------
    //
    //
    //------------------------------------------------------------------------
    public class FAtomicSimulatorX extends ViewableAtomicSimulator implements Runnable
    {
        /**
        * How many milliseconds this coordinator should wait to simulate one
        * unit of simulation time.
        */
        private int rtMultiplier;
        private int numIter;
        private Thread myThread;
        private double INFINITY  = DevsInterface.INFINITY;
        
        public FAtomicSimulatorX(ViewableAtomic atomicModel)
        {
            super(atomicModel);
            myThread = new Thread(this);
            rtMultiplier = 1000;
            currentState = STATE_INITIAL;
            reset();
        }
        
        //----------------------------------------------------------------------
        //		THREAD LOGIC
        //----------------------------------------------------------------------
        private boolean stopRequested;
        private boolean suspendRequested;

        private void requestStop()          {stopRequested = true;}
        private void requestSuspend()       {suspendRequested = true;}
        
        private void requestResume()
        {
            suspendRequested = false;
            try
            {
                notifyAll();
            }
            catch(Exception e){}
        }
        
        private synchronized void checkSuspended()
        {
            while(suspendRequested)
            {
                try {wait();}
                catch (InterruptedException e){}
            } 
        }
        
        private synchronized void sleep(long time)
        {
            try
            {
                 wait(time);
            }
            catch (InterruptedException e){}
        }
        //----------------------------------------------------------------------
        
        /**
         * Sets how many seconds this coordinator should wait to simulate one
         * unit of simulation time.
         *
         * @param   realTimeFactor      How many seconds to wait per one unit
         *                              of simulation time.
         */
        public synchronized void setRTMultiplier (double realTimeFactor)
        {
            // convert the given time factor to milliseconds
            rtMultiplier = (int)Math.floor(1000 * realTimeFactor);
            //If the current thread is not suspended and is not stopped, interrupt it
            //so that the multiplier can take effect right away.
            if (currentState == STATE_SIMULATING)
                notifyAll();
        }

        public double getRTMultiplier()
        {
            return ((double)rtMultiplier)/1000;
        }
        
        /**
         * Initializes this coordinator to starting state.
         */
        public synchronized void reset()
        {
            if (currentState == STATE_INITIAL ||  currentState == STATE_PAUSE ||
                currentState == STATE_END)
            {
                initialize();
                stopRequested    = false;
                suspendRequested = false;
                currentState     = STATE_INITIAL;
            }
            else
            {
                throw new FIllegalSimulatorStateException(
                 formatErrorMsg("Can only [Reset] from state: {Initial} | {Pause} | {End}."));
            }
        }

        /**
         * Tells this coordinator to execute the specified number of iterations
         * of the simulation.
         *
         * @param   numIterations       The number of iterations to execute.
         */
        public synchronized void simulate(int numIterations)
        {
            if (numIterations > 0)
            {
                if (currentState == STATE_INITIAL || currentState == STATE_PAUSE)
                {
                    currentState = STATE_SIMULATING;
                    numIter = numIterations;
                    if (!myThread.isAlive())
                    {
                        myThread = null; //Ensure garbage collection
                        myThread = new Thread(this); //Create new Thread 
                        myThread.start(); //Start the thread
                    }
                    else
                    {   
                        requestResume();
                    }
                }
                else
                    throw new FIllegalSimulatorStateException(
                     formatErrorMsg("Can only [Simulate] from state: {Initial} | {Pause}."));
            }
            else
                throw new FIllegalSimulatorParameterException(
                 "Cannot [Simulate], number of iterations must be > 0: " + numIterations+".");
        }

        public synchronized void requestPause()
        {
            if (currentState == STATE_SIMULATING || currentState == STATE_PAUSE)
            {
                numIter = 0;
            }
            else
                throw new FIllegalSimulatorStateException(
                 formatErrorMsg("Can only [Pause] from state: {Simulating} | {Pause}."));
        }
        
        /**
         * Executes the actual iterations of the simulation. This method
         * should not be called from any method outside this class.
         */
        public void run()
        {    
            while (!stopRequested) 
            {
                //while this coordinator hasn't yet been told to do more iterations
                checkSuspended();         

                // for each iteration this coordinator was told to do,
                // stopping early if there are no more next events to process
                int iterationsComplete = 0;
                tN = nextTN();
                while (tN < INFINITY && iterationsComplete < numIter) 
                {System.out.println("Simulation is stepping--->>");
                 System.out.println("TL: " + tL);
                    System.out.println("TN: " + tN);
                    // detm how long to sleep before performing this iteration,
                    // such that it will be performed at the actual time of next
                    // event
                    long timeToSleep = (long)((tN - tL) * rtMultiplier);

                    // if the simulation is restarted, there may be a brief period
                    // of time where tL is greater than tN (which is now zero), so
                    // make sure we don't have a negative time to sleep
                    timeToSleep = Math.max(timeToSleep, 1);

                    // sleep for that amount
                    sleep(timeToSleep);
                    tL = tN;
                    // perform the actual simulation iteration
                    computeInputOutput(tN);
                    wrapDeltfunc(tN);
                    tN = nextTN();
                    iterationsComplete++;
                }
                if (!(tN < INFINITY))
                {
                    requestStop();
                    currentState = STATE_END;
                }
                else
                {
                    requestSuspend();
                    currentState = STATE_PAUSE;
                }
            }
        }
        
        private String formatErrorMsg(String msg)
        {
            switch (currentState)
            {
                case STATE_INITIAL:
                    msg += " {Current State = Ready}";
                    break;
                case STATE_SIMULATING:
                    msg += " {Current State = Simulating}";
                    break;
                case STATE_PAUSE:
                    msg += " {Current State = Pause}";
                    break;
                case STATE_END:
                    msg += " {Current State = End}";
                    break;
                default:
                    msg += " {Current State = Unknown}";
                    break;
            }
            return msg;
        }
    }    
}