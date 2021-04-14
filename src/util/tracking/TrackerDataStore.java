package util.tracking;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;

import util.SortedEnumerableList;

public interface TrackerDataStore
{
    public enum DataType
    {
        InputPorts(0),
        OutputPorts(1),
        States(2),
        Time(3);

        public int i;

        private DataType(int i)
        {
            this.i = i;
        }

        public static void forEach(Consumer<DataType> f)
        {
            Arrays.stream(values()).forEach(f);
        }
        
        public static int numTypes()
        {
            return values().length;
        }
    };
    
    public enum SortOrder
    {
        Alphabetical("A-Z"),
        Reversed("Z-A"),
        Unsorted("");
        
        private String str;
        private SortOrder(String str)
        {
            this.str = str;
        }
        
        @Override
        public String toString()
        {
            return str;
        }
    };

    public class TrackedVariableMetadata
    {
        public String name;
        public DataType type;

        public TrackedVariableMetadata(String name, DataType type)
        {
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof TrackedVariableMetadata)
            {
                TrackedVariableMetadata rhs = (TrackedVariableMetadata) o;
                return this.name == rhs.name && this.type == rhs.type;
            }
            return false;
        }
    };

    public static Iterable<String> StringIterable(Iterable<TrackedVariableMetadata> it)
    {
        return new Iterable<String>() {

            @Override
            public Iterator<String> iterator()
            {
                return new Iterator<String>() {
                    private Iterator<TrackedVariableMetadata> _it = it.iterator();

                    @Override
                    public boolean hasNext()
                    {
                        return this._it.hasNext();
                    }

                    @Override
                    public String next()
                    {
                        return this._it.next().name;
                    }

                };
            }
        };
    }

    public int dataSize(TrackerDataStore.TrackedVariableMetadata meta);
    public int dataSize(String name, DataType type);

    public int inputPortDataSize(String name);
    public int outputPortDataSize(String name);
    public int stateDataSize(String name);
    public int timeDataSize(String name);

    public Iterable<TrackedVariableMetadata> getHeader();
    public Iterable<TrackedVariableMetadata> getHeaderSorted();
    public Iterable<TrackedVariableMetadata> getHeaderReversed();

    public int headerSize();

    public Object getData(TrackedVariableMetadata meta, int index);
    public Object getData(String name, DataType dType, int index);

    public Object getInputPortData(String name, int index);
    public Object getOutputPortData(String name, int index);
    public Object getStateData(String name, int index);
    public Object getTimeData(String name, int index);

    public SortedEnumerableList<String> getInputPortNames();
    public SortedEnumerableList<String> getOutputPortNames();
    public SortedEnumerableList<String> getStateNames();
    public SortedEnumerableList<String> getTimeDimensionNames();

    public void forEachData(String name, DataType dType, Consumer<Object> f);

    public boolean isInputPortTracked(String name, TrackingType type);
    public boolean isOutputPortTracked(String name, TrackingType type);
    public boolean isStateTracked(String name, TrackingType type);
    public boolean isTimeTracked(String name, TrackingType type);
    public boolean isInputPortTracked(String name);
    public boolean isOutputPortTracked(String name);
    public boolean isStateTracked(String name);
    public boolean isTimeTracked(String name);
    
    public boolean isAnythingTracked();
    public boolean isAtLeastOneInputPortTrackedInLog();
    public boolean isAtLeastOneOutputPortTrackedInLog();
    public boolean isAtLeastOneInputPortPlotted();
    public boolean isAtLeastOneOutputPortPlotted();
    
    public boolean isTrackingLogEnabled();
    public SortOrder getTrackingLogSortOrder();
    public boolean isTimeViewEnabled();
    public SortOrder getTimeViewSortOrder();
    public boolean isDatabaseTrackingEnabled();

    public String getInputPortUnits(String name);
    public String getOutputPortUnits(String name);
    public String getStateUnits(String name);
    public String getTimeUnits(String name);

    public TrackerConfiguration getTrackingConfigurationFor(DataType dType, String name);
    public Optional<? extends TrackerConfiguration[]> getTrackingConfigurationFor(DataType dType);
    public TrackerConfiguration[] getTrackingConfigurationForInputPorts();
    public TrackerConfiguration[] getTrackingConfigurationForOutputPorts();
    public TrackerConfiguration[] getTrackingConfigurationForStates();
    public TrackerConfiguration[] getTrackingConfigurationForTime();
}
