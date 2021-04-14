package util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class that performs non-blocking, one-directional
 * communication using a new thread <b>to</b> the database.
 */
public class NonBlockingCommunicationThread extends Thread
{
    private Queue<StatementArguments> sqlQueue;
    private ConnectionFactory connectionFactory;
    private boolean kill, shouldLog;
    private Connection connection;
    private boolean isWaitingForInput;
    private int threadNumber;
    
    public NonBlockingCommunicationThread(ConnectionFactory connectionFactory, int threadNumber) throws SQLException
    {
        this.connectionFactory = connectionFactory;
        this.threadNumber = threadNumber;
        sqlQueue = new LinkedBlockingQueue<StatementArguments>();
        kill = false;
        shouldLog = false;
        connection = connectionFactory.newConnection();
    }
    
    public void addToQueue(StatementArguments sql)
    {
        sqlQueue.offer(sql);
    }
    
    @Override
    public void run()
    {
        while(!kill)
        {
            if (!sqlQueue.isEmpty())
            {
                isWaitingForInput = false;
                try
                {
                    if (connection.isClosed())
                    {
                        connection = connectionFactory.newConnection();
                    }
                }
                catch (SQLException e1)
                {
                    e1.printStackTrace();
                    System.err.println("Connection unable to be reopened!");
                    kill();
                    return;
                }

                StatementArguments stmtArgs = sqlQueue.poll();

                if (shouldLog)
                {
                    printStatus();
                }

                try
                {
                    PreparedStatement statement = connection.prepareStatement(stmtArgs.getSQL());
                    stmtArgs.setArguments(statement);
                    statement.execute();
                    statement.close();
                }
                catch (SQLException e)
                {
                    System.err.println("Failed to execute:");
                    System.err.println(stmtArgs.getSQL());
                    System.err.println("with arguments:");
                    System.err.println(stmtArgs.argsToString());
                    e.printStackTrace();
                }
            }
            else
            {
                isWaitingForInput = true;
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
    
    public void setLoggingMode(boolean shouldLog)
    {
        this.shouldLog = shouldLog;
    }
    
    private void printStatus()
    {
        System.out.println("Thread " + threadNumber + ": " + sqlQueue.size() + " database operations remaining.");
    }
    
    public void kill()
    {
        kill = true;

        try
        {
            if (!connection.isClosed())
            {
                connection.close();
            }
        }
        catch (SQLException e1)
        {
            e1.printStackTrace();
        }
    }
    
    public boolean isWaitingForInput()
    {
        return isWaitingForInput;
    }
}