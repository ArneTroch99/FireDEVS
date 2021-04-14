package util.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.eclipse.swt.internal.Lock;

/**
 * Utility class that manages <b>blocking</b> communication 
 * <b>from</b> the database.
 */
public class BlockingCommunicationManager
{
    private ConnectionFactory connectionFactory;
    private Connection connection;
    public BlockingCommunicationManager(ConnectionFactory connectionFactory)
    {
        this.connectionFactory = connectionFactory;
    }
    
    /**
     * Executes a query to the database with unlimited timeout.
     * @param sqlQuery The query to perform within the database.
     * @param callBack A function to format the result.
     * @param args query parameters
     * @return the result of the query, formatted by the callback
     * @throws SQLException
     * @throws InterruptedException
     */
    public <T> Optional<T> executeQuery(String sqlQuery, SQLCallback<T> callBack, Object... args) throws SQLException, InterruptedException
    {
        return executeQueryWithTimeout(sqlQuery, callBack, 0, args);
    }
    
    /**
     * Executes a query to the database with specified timeout.
     * @param sqlQuery The query to perform within the database.
     * @param callBack A function to format the result.
     * @param timeoutMillis Number of milliseconds to wait before timing-out
     * @param args query parameters
     * @return the result of the query, formatted by the callback
     * @throws SQLException
     * @throws InterruptedException
     */
    public <T> Optional<T> executeQueryWithTimeout(String sqlQuery, SQLCallback<T> callBack, int timeoutMillis, Object... args) throws SQLException, InterruptedException
    {
        checkConnection();
        BlockingCommunicationThread<T> t = new BlockingCommunicationThread<T>(connection, sqlQuery, callBack, new StatementArguments(sqlQuery, args));
        t.start();
        
        if (timeoutMillis > 0)
        {
            synchronized(t)
            {
                t.wait(timeoutMillis);
                if (t.isAlive())
                {
                    System.err.println("Thread runtime limit exceeded: " + timeoutMillis + "ms");
                }
            }

            t.join(0, 1);
        }
        else
        {
            t.join();
        }
                
        if (t.getException().isPresent())
        {
            throw t.getException().get();
        }
        return t.getResult();
    }
    
    /**
     * Executes generic sql in a blocking manner. Designed mostly for data definition (DDL).
     * Waits indefinitely for the sql to complete.
     * @param sql SQL String to execute
     * @throws SQLException
     * @throws InterruptedException
     */
    public void executeSQL(String sql, Object... args) throws SQLException, InterruptedException
    {
        executeSQLWithTimeout(sql, 0, args);
    }
    
    /**
     * Executes generic sql in a blocking manner. Designed mostly for data definition (DDL).
     * Times-out after provided milliseconds.
     * @param sql SQL String to execute
     * @param timeoutMillis Number of milliseconds to wait
     * @throws SQLException
     * @throws InterruptedException
     */
    public void executeSQLWithTimeout(String sql, int timeoutMillis, Object... args) throws SQLException, InterruptedException
    {
        executeQueryWithTimeout(sql, (ResultSet unused) -> { return null; }, timeoutMillis, args);
    }
    
    private void checkConnection() throws SQLException
    {
        if (connection == null || connection.isClosed())
        {
            connection = connectionFactory.newConnection();
        }
    }
}