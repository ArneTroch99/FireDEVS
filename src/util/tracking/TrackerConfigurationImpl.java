package util.tracking;

public class TrackerConfigurationImpl implements TrackerConfiguration
{
    protected TrackerDataStore dataStore;
    protected TrackerDataStore.DataType dType;
    protected String name;

    public TrackerConfigurationImpl(
        TrackerDataStore dataStore,
        TrackerDataStore.DataType dType,
        String name
    )
    {
        this.dataStore = dataStore;
        this.dType = dType;
        this.name = name;
    }

    @Override
    public boolean isTracked(TrackingType tType)
    {
        if (this.dType == TrackerDataStore.DataType.InputPorts)
        {
            return this.dataStore.isInputPortTracked(
                this.name,
                tType
            );
        }
        else if (this.dType == TrackerDataStore.DataType.OutputPorts)
        {
            return this.dataStore.isOutputPortTracked(
                this.name,
                tType
            );
        }
        else if (this.dType == TrackerDataStore.DataType.States)
        {
            return this.dataStore.isStateTracked(this.name, tType);
        }
        else if (this.dType == TrackerDataStore.DataType.Time)
        {
            return this.dataStore.isTimeTracked(this.name, tType);
        }
        return false;
    }

    @Override
    public String getUnits()
    {
        if (this.dType == TrackerDataStore.DataType.InputPorts)
        {
            return this.dataStore.getInputPortUnits(this.name);
        }
        else if (this.dType == TrackerDataStore.DataType.OutputPorts)
        {
            return this.dataStore.getOutputPortUnits(this.name);
        }
        else if (this.dType == TrackerDataStore.DataType.States)
        {
            return this.dataStore.getStateUnits(this.name);
        }
        else if (this.dType == TrackerDataStore.DataType.Time)
        {
            return this.dataStore.getTimeUnits(this.name);
        }
        return null;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean isTracked()
    {
        return TrackingType.stream().filter(
            (TrackingType tType) -> isTracked(tType)
        ).findFirst().isPresent();
    }

    @Override
    public boolean isTimeViewTracked()
    {
        return TrackingType.stream().filter(
            (TrackingType tType) -> tType.isTimeViewType()
        ).filter(
            (TrackingType tType) -> isTracked(tType)
        ).findFirst().isPresent();
    }
}
