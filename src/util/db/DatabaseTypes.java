package util.db;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

public enum DatabaseTypes
{
    PostgreSQL("PostgreSQL", 0);
    
    private String typeString;
    private int index;
    private DatabaseTypes(String typeString, int index)
    {
        this.typeString = typeString;
        this.index = index;
    }
    
    @Override
    public String toString()
    {
        return typeString;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    public static void forEach(Consumer<DatabaseTypes> c)
    {
        Arrays.stream(DatabaseTypes.values()).forEach(c);
    }

    public static Optional<DatabaseTypes> fromString(String databaseTypeString)
    {
        return Arrays.stream(DatabaseTypes.values()).filter((DatabaseTypes databaseType) -> { 
            return databaseType.toString().equals(databaseTypeString);
        }).findFirst();
    }
}
