package util.tracking;

import java.util.Optional;

public interface MutableTrackerDataStore extends TrackerDataStore
{
    public boolean addInputPortData(String name, Object o);
    public boolean addOutputPortData(String name, Object o);
    public boolean addStateData(String name, Object o);
    public boolean addTimeData(String name, Object o);
    public MutableTrackerDataStore setInputPortTracked(String name, TrackingType type);
    public MutableTrackerDataStore setOutputPortTracked(String name, TrackingType type);
    public MutableTrackerDataStore setStateTracked(String name, TrackingType type);
    public MutableTrackerDataStore setTimeTracked(String name, TrackingType type);
    public MutableTrackerDataStore setInputPortUntracked(String name, TrackingType type);
    public MutableTrackerDataStore setOutputPortUntracked(String name, TrackingType type);
    public MutableTrackerDataStore setStateUntracked(String name, TrackingType type);
    public MutableTrackerDataStore setTimeUntracked(String name, TrackingType type);
    public MutableTrackerDataStore setInputPortUnit(String name, String unit);
    public MutableTrackerDataStore setOutputPortUnit(String name, String unit);
    public MutableTrackerDataStore setStateUnit(String name, String unit);
    public MutableTrackerDataStore setTimeUnit(String name, String unit);
    
    public MutableTrackerDataStore setTrackingLogSortOrder(TrackerDataStore.SortOrder order);
    public MutableTrackerDataStore setTimeViewSortOrder(TrackerDataStore.SortOrder order);
    
    @Override
    public MutableTrackerConfiguration getTrackingConfigurationFor(DataType dType, String name);
    
    @Override
    public Optional<MutableTrackerConfiguration[]> getTrackingConfigurationFor(DataType dType);
    
    @Override
    public MutableTrackerConfiguration[] getTrackingConfigurationForInputPorts();
    
    @Override
    public MutableTrackerConfiguration[] getTrackingConfigurationForOutputPorts();
    
    @Override
    public MutableTrackerConfiguration[] getTrackingConfigurationForStates();
    
    @Override
    public MutableTrackerConfiguration[] getTrackingConfigurationForTime();
}
