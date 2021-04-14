package util.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory
{
    public Connection newConnection() throws SQLException;
}
