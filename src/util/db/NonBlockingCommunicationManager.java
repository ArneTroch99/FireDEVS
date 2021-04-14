package util.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that manages multiple threads for non-blocking, 
 * one-directional communication <b>to</b> the database.
 */
public class NonBlockingCommunicationManager
{
    private List<NonBlockingCommunicationThread> communicationThreads;
    private int roundRobinIndex;
    
    public NonBlockingCommunicationManager(ConnectionFactory connectionFactory)
    {
        this(connectionFactory, 1);
    }
    
    public NonBlockingCommunicationManager(ConnectionFactory connectionFactory, int numThreads)
    {
        communicationThreads = new ArrayList<NonBlockingCommunicationThread>();
        roundRobinIndex = 0;
        
        for (int i = 0; i < numThreads; ++i)
        {
            try
            {
                NonBlockingCommunicationThread t = new NonBlockingCommunicationThread(connectionFactory, i + 1);
                t.start();
                communicationThreads.add(t);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void executeSQL(String sql, Object... args)
    {
        communicationThreads.get(roundRobinIndex).addToQueue(new StatementArguments(sql, args));
        nextRoundRobin();
    }
    
    /**
     * Stops all threads after the given timeout. A timeout value of
     * zero (0) means that the thread will wait for the last database
     * operation to finish.
     * 
     * @param timeout milliseconds to wait
     * @throws InterruptedException
     */
    public void killAllThreads(int timeout) throws InterruptedException
    {
        for (NonBlockingCommunicationThread communicationThread : communicationThreads)
        {
            if (timeout > 0)
            {
                synchronized (communicationThread)
                {
                    communicationThread.kill();
                    communicationThread.wait(timeout);
                    
                    if (communicationThread.isAlive())
                    {
                        System.err.println("Killing database thread...");
                        communicationThread.join(1);
                    }
                }
            }
            else
            {
                communicationThread.kill();
                communicationThread.join();
            }
        }
    }
    
    public void setLoggingMode(boolean shouldLog)
    {
        communicationThreads.forEach((NonBlockingCommunicationThread t) -> t.setLoggingMode(shouldLog));
    }
    
    private void nextRoundRobin()
    {
        if (roundRobinIndex == communicationThreads.size() - 1)
        {
            roundRobinIndex = 0;
        }
        else
        {
            ++roundRobinIndex;
        }
    }
    
    public boolean isExecutingDatabaseCommands()
    {
        return communicationThreads.stream().filter((NonBlockingCommunicationThread t) -> !t.isWaitingForInput()).findFirst().isPresent();
    }
}
