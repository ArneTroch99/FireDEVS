package util.tracking;

/**
 * Convenience class to access tracking information for a particular<br>
 * variable. TrackerConfiguration provides filtered access to read<br>
 * tracking data for a given variable type and variable name.
 */
public interface TrackerConfiguration
{
    public String getName();
    public boolean isTracked(TrackingType tType);
    public boolean isTracked();
    public boolean isTimeViewTracked();
    public String getUnits();
}
