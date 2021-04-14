package util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class BlockingCommunicationThread<T> extends Thread
{
    private Connection connection;
    private String sqlQuery;
    private SQLCallback<T> callBack;
    private StatementArguments args;

    private Optional<T> result;
    private Optional<SQLException> exception;
    
    public BlockingCommunicationThread(Connection connection, String sqlQuery, SQLCallback<T> callBack, StatementArguments args)
    {
        this.connection = connection;
        this.sqlQuery = sqlQuery;
        this.callBack = callBack;
        this.args = args;
        result = Optional.empty();
        exception = Optional.empty();
    }
    
    @Override
    public void run()
    {
        try
        {
            PreparedStatement ps = connection.prepareStatement(sqlQuery);
            args.setArguments(ps);
            
            if (ps.execute())
            {
                ResultSet _result = ps.getResultSet();
                result = Optional.ofNullable(callBack.process(_result));
                _result.close();
            }
            
            ps.close();
        }
        catch (SQLException e)
        {
            exception = Optional.of(e);
        }
    }
    
    public Optional<T> getResult()
    {
        return result;
    }
    
    public Optional<SQLException> getException()
    {
        return exception;
    }
}
