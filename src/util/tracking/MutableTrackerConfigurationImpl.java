package util.tracking;

public class MutableTrackerConfigurationImpl extends TrackerConfigurationImpl implements MutableTrackerConfiguration
{
    public MutableTrackerConfigurationImpl(MutableTrackerDataStore dataStore, TrackerDataStore.DataType dType, String name)
    {
        super(dataStore, dType, name);
    }
    
    private MutableTrackerDataStore getDataStore()
    {
        return (MutableTrackerDataStore) this.dataStore;
    }
    
    @Override
    public MutableTrackerConfiguration setTracked(TrackingType tType)
    {
        if (dType == TrackerDataStore.DataType.InputPorts)
        {
            this.getDataStore().setInputPortTracked(this.name, tType);
        }
        else if (dType == TrackerDataStore.DataType.OutputPorts)
        {
            this.getDataStore().setOutputPortTracked(this.name, tType);
        }
        else if (dType == TrackerDataStore.DataType.States)
        {
            this.getDataStore().setStateTracked(this.name, tType);
        }
        else if (dType == TrackerDataStore.DataType.Time)
        {
            this.getDataStore().setTimeTracked(this.name, tType);
        }
        return this;
    }
    
    @Override
    public MutableTrackerConfiguration setUntracked(TrackingType tType)
    {
        if (dType == TrackerDataStore.DataType.InputPorts)
        {
            this.getDataStore().setInputPortUntracked(this.name, tType);
        }
        else if (dType == TrackerDataStore.DataType.OutputPorts)
        {
            this.getDataStore().setOutputPortUntracked(this.name, tType);
        }
        else if (dType == TrackerDataStore.DataType.States)
        {
            this.getDataStore().setStateUntracked(this.name, tType);
        }
        else if (dType == TrackerDataStore.DataType.Time)
        {
            this.getDataStore().setTimeUntracked(this.name, tType);
        }
        return this;
    }

    @Override
    public MutableTrackerConfiguration setUnits(String units)
    {
        if (dType == TrackerDataStore.DataType.InputPorts)
        {
            this.getDataStore().setInputPortUnit(this.name, units);
        }
        else if (dType == TrackerDataStore.DataType.OutputPorts)
        {
            this.getDataStore().setOutputPortUnit(this.name, units);
        }
        else if (dType == TrackerDataStore.DataType.States)
        {
            this.getDataStore().setStateUnit(this.name, units);
        }
        else if (dType == TrackerDataStore.DataType.Time)
        {
            this.getDataStore().setTimeUnit(this.name, units);
        }
        
        return this;
    }

}
