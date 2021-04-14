package util.tracking;

/**
 * Convenience class to read and modify tracking information for a<br>
 * particular variable. MutableTrackerConfiguration provides filtered<br>
 * access to read and write tracking data for a given variable type and<br>
 * variable name.
 */
public interface MutableTrackerConfiguration extends TrackerConfiguration
{
    public MutableTrackerConfiguration setTracked(TrackingType tType);
    public MutableTrackerConfiguration setUntracked(TrackingType tType);
    public MutableTrackerConfiguration setUnits(String units);
}
