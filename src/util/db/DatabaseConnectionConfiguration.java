package util.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public interface DatabaseConnectionConfiguration extends ConnectionFactory
{
    public Optional<SQLException> test() throws ConnectionTestException;
    public DatabaseTypes getDatabaseType();
    public ArrayList<String> toList();
}
