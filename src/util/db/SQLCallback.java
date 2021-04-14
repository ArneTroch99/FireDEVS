package util.db;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface SQLCallback<T>
{
    public T process(ResultSet result) throws SQLException;
}
