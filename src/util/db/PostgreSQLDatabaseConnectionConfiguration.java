package util.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public class PostgreSQLDatabaseConnectionConfiguration extends GenericPasswordProtectedDatabaseConnectionConfiguration
{
    public static final int SCHEMA_INDEX = 4;
    private String databaseName, host, port, userName, schema;
    private ConnectionFactory connectionFactory;
    
    private class DefaultConnectionFactory implements ConnectionFactory
    {
        @Override
        public Connection newConnection() throws SQLException
        {
            return DriverManager.getConnection(getURL(), getUserName(), getPassword());
        }
    }
    
    public PostgreSQLDatabaseConnectionConfiguration(String databaseName, String host, String port, String userName, String schema)
    {
        this(databaseName, host, port, userName, schema, Optional.empty());
    }
    
    public PostgreSQLDatabaseConnectionConfiguration(String databaseName, String host, String port, String userName, String schema, Optional<ConnectionFactory> connectionFactory)
    {
        this.databaseName = databaseName;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.schema = schema;
        
        if (connectionFactory.isPresent())
        {
            this.connectionFactory = connectionFactory.get();
        }
        else
        {
            this.connectionFactory = new DefaultConnectionFactory();
        }
    }
    
    @Override
    public Optional<SQLException> test() throws ConnectionTestException
    {
        promptPassword();

        try
        {
            Connection conn = newConnection();
            conn.close();
            return Optional.empty();
        }
        catch (SQLException e)
        {
            return Optional.of(e);
        }
    }
    
    @Override
    public ArrayList<String> toList()
    {
        ArrayList<String> list = new ArrayList<String>();
        
        list.add(databaseName);
        list.add(host);
        list.add(port);
        list.add(userName);
        list.add(schema);
        
        return list;
    }
    
    @Override
    public Connection newConnection() throws SQLException
    {
        return connectionFactory.newConnection();
    }
    
    private String getURL()
    {
        StringBuilder sb = new StringBuilder("jdbc:postgresql://");
        sb.append(host)
          .append(':')
          .append(port)
          .append('/')
          .append(databaseName);
        
        return sb.toString();
    }
    
    private String getUserName()
    {
        return userName;
    }
    
    private String getPassword()
    {
        return password;
    }

    @Override
    public DatabaseTypes getDatabaseType()
    {
        return DatabaseTypes.PostgreSQL;
    }
}
