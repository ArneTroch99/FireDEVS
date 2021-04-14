package util.db;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

import util.OptionalWrapper;

public class StatementArguments
{
    private String sql;
    private Object[] args;
    public StatementArguments(String sql, Object... args)
    {
        this.sql = sql;
        this.args = args;
    }
    
    public String getSQL()
    {
        return sql;
    }
    
    public String argsToString()
    {
        StringBuilder sb = new StringBuilder();
        
        int i = 0;
        int numArgs = args.length;
        for (Object o : args)
        {
            sb.append(o.toString());

            if (i < numArgs - 1)
            {
                sb.append(", ");
            }
            ++i;
        }
        
        return sb.toString();
    }
    
    public void setArguments(PreparedStatement ps) throws SQLException
    {
        for (int i = 0; i < args.length; ++i)
        {
            Object value = args[i];
            if (value instanceof OptionalWrapper<?>)
            {
                OptionalWrapper<?> optionalValue = (OptionalWrapper<?>) value;
                if (optionalValue.get().isPresent())
                {
                    value = optionalValue.get().get();
                }
                else
                {
                    int sqlType = getSqlType(optionalValue.getGenericClass());
                    if (sqlType != Types.OTHER)
                    {
                        ps.setNull(i + 1, sqlType);
                        continue;
                    }
                }
            }
            
            setPreparedStatement(ps, i + 1, value);
        }
    }
    
    private <T> int getSqlType(Class<T> c)
    {
        if (c.equals(String.class) || c.equals(UUID.class))
        {
            return Types.VARCHAR;
        }
        else if (c.equals(Integer.class))
        {
            return Types.INTEGER;
        }
        else if (c.equals(Double.class))
        {
            return Types.DOUBLE;
        }
        return Types.OTHER;
    }
    
    private void setPreparedStatement(PreparedStatement ps, int row, Object arg) throws SQLException
    {
        if (arg instanceof String)
        {
            ps.setString(row, (String) arg);
        }
        else if (arg instanceof UUID)
        {
            ps.setString(row, ((UUID) arg).toString());
        }
        else if (arg instanceof Integer)
        {
            ps.setInt(row, (int) arg);
        }
        else if (arg instanceof Double)
        {
            ps.setDouble(row, (double) arg);
        }
    }
}
