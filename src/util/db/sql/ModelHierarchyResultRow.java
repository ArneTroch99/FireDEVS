package util.db.sql;

import java.util.Optional;
import java.util.UUID;

public class ModelHierarchyResultRow
{
    public UUID modelID;
    public Optional<UUID> parentID;
    public String modelString;
    public UUID rootID;
    
    public ModelHierarchyResultRow(UUID modelID, Optional<UUID> parentID, String modelString)
    {
        this(modelID, null, parentID, modelString);
    }
    
    public ModelHierarchyResultRow(UUID modelID, UUID rootID, Optional<UUID> parentID, String modelString)
    {
        this.modelID = modelID;
        this.rootID = rootID;
        this.parentID = parentID;
        this.modelString = modelString;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        else if (o instanceof ModelHierarchyResultRow)
        {
            ModelHierarchyResultRow rhs = (ModelHierarchyResultRow) o;
            return modelID.equals(rhs.modelID) && modelString.equals(rhs.modelString) && parentID.equals(rhs.parentID) && rootID.equals(rhs.rootID);
        }
        return false;
    }
}