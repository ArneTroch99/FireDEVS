package util.db;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import facade.modeling.FModel;
import util.db.sql.ModelHierarchyResultRow;

public interface ModelHierarchyDatabaseHelper
{
    public boolean hasIDFor(FModel model);
    public void addIDFor(FModel model, UUID id);
    public Optional<UUID> getIDForModel(FModel model);
    public String getFullyQualifiedNameFor(FModel model);
    public void forEachQualifiedName(Consumer<String> c);
    public FModel getRootModel();
    public void mergeHierarchy(List<ModelHierarchyResultRow> models);
    public List<ModelHierarchyResultRow> getFlattenedRows();
    
    public Set<String> getStateNameSet();
    public Set<String> getInputPortSet();
    public Set<String> getOutputPortSet();
}
