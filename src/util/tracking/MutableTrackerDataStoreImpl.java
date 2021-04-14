package util.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import util.SortedEnumerableList;

public class MutableTrackerDataStoreImpl implements MutableTrackerDataStore
{
    private List<?>[] inputPortData, outputPortData, stateData, timeData;
    private List<String> headers;
    private Map<String, Integer> inputPorts, outputPorts, states, times;
    private boolean[][][] tracking;
    private SortedEnumerableList<String> inputPortNames, outputPortNames, stateNames, timeNames;
    private String[] inputPortUnits, outputPortUnits, stateUnits, timeUnits;
    private int numLogsTracked, numTimeViewTracked, numDBsTracked, numInputPortsPlotted, numOutputPortsPlotted, numInputPortsTrackedInLog, numOutputPortsTrackedInLog;
    private TrackerDataStore.SortOrder trackingLogSortOrder, timeViewSortOrder;
    
    public MutableTrackerDataStoreImpl(TrackerDataStore dStore)
    {
        if (dStore.getClass().equals(MutableTrackerDataStoreImpl.class))
        {
            MutableTrackerDataStoreImpl dataStore = (MutableTrackerDataStoreImpl) dStore;
            this.headers = dataStore.headers;
            this.tracking = dataStore.tracking;
            this.inputPortNames = dataStore.inputPortNames;
            this.inputPortData = dataStore.inputPortData;
            this.inputPorts = dataStore.inputPorts;
            this.inputPortUnits = dataStore.inputPortUnits;
            this.outputPortNames = dataStore.outputPortNames;
            this.outputPortData = dataStore.outputPortData;
            this.outputPorts = dataStore.outputPorts;
            this.outputPortUnits = dataStore.outputPortUnits;
            this.stateNames = dataStore.stateNames;
            this.stateData = dataStore.stateData;
            this.states = dataStore.states;
            this.stateUnits = dataStore.stateUnits;
            this.timeNames = dataStore.timeNames;
            this.timeData = dataStore.timeData;
            this.times = dataStore.times;
            this.timeUnits = dataStore.timeUnits;
            this.numLogsTracked = dataStore.numLogsTracked;
            this.numTimeViewTracked = dataStore.numTimeViewTracked;
            this.numDBsTracked = dataStore.numDBsTracked;
            this.numInputPortsPlotted = dataStore.numInputPortsPlotted;
            this.numOutputPortsPlotted = dataStore.numOutputPortsPlotted;
            this.numInputPortsTrackedInLog = dataStore.numInputPortsTrackedInLog;
            this.numOutputPortsTrackedInLog = dataStore.numOutputPortsTrackedInLog;
            this.numOutputPortsPlotted = dataStore.numOutputPortsPlotted;
            this.trackingLogSortOrder = dataStore.trackingLogSortOrder;
            this.timeViewSortOrder = dataStore.timeViewSortOrder;
        }
        else
        {
            NEW(
                dStore.getInputPortNames(),
                dStore.getOutputPortNames(),
                dStore.getStateNames(),
                dStore.getTimeDimensionNames()
            );

            COPY_DATA(dStore);
        }
    }

    public MutableTrackerDataStoreImpl(SortedEnumerableList<String> inputPortNames,
            SortedEnumerableList<String> outputPortNames,
            SortedEnumerableList<String> stateNames,
            SortedEnumerableList<String> timeNames)
    {
        NEW(inputPortNames, outputPortNames, stateNames, timeNames);
    }

    private void NEW(SortedEnumerableList<String> inputPortNames,
            SortedEnumerableList<String> outputPortNames,
            SortedEnumerableList<String> stateNames,
            SortedEnumerableList<String> timeNames)
    {
        this.inputPortNames = inputPortNames;
        this.outputPortNames = outputPortNames;
        this.stateNames = stateNames;
        this.timeNames = timeNames;
        this.inputPortUnits = new String[this.inputPortNames.size()];
        Arrays.fill(this.inputPortUnits, "");
        this.outputPortUnits = new String[this.outputPortNames.size()];
        Arrays.fill(this.outputPortUnits, "");
        this.stateUnits = new String[this.stateNames.size()];
        Arrays.fill(this.stateUnits, "");
        this.timeUnits = new String[this.timeNames.size()];
        Arrays.fill(this.timeUnits, "");
        this.headers = new ArrayList<String>();
        this.tracking = new boolean[4][][];
        this.tracking[0] = new boolean[inputPortNames.size()][];
        this.tracking[1] = new boolean[outputPortNames.size()][];
        this.tracking[2] = new boolean[stateNames.size()][];
        this.tracking[3] = new boolean[timeNames.size()][];
        this.initializeTracking(this.tracking);

        this.initializeInputPorts(inputPortNames)
            .initializeOutputPorts(outputPortNames)
            .initializeStates(stateNames)
            .initializeTime(timeNames);
        
        this.numLogsTracked = 0;
        this.numTimeViewTracked = 0;
        this.numDBsTracked = 0;
        this.numInputPortsPlotted = 0;
        this.numOutputPortsPlotted = 0;
        this.numInputPortsTrackedInLog = 0;
        this.numOutputPortsTrackedInLog = 0;

        this.trackingLogSortOrder = TrackerDataStore.SortOrder.Unsorted;
        this.timeViewSortOrder = TrackerDataStore.SortOrder.Unsorted;
    }

    private void COPY_DATA(TrackerDataStore dStore)
    {
        dStore.getHeader().forEach((TrackedVariableMetadata meta) -> {
            int[] i = { 0 };
            dStore.forEachData(meta.name, meta.type, (Object o) -> {
                this.addData(meta, o);
                ++i[0];
            });

            if (meta.type == DataType.InputPorts)
            {
                this.setInputPortUnit(meta.name, dStore.getInputPortUnits(meta.name));
            }
            else if (meta.type == DataType.OutputPorts)
            {
                this.setOutputPortUnit(meta.name, dStore.getOutputPortUnits(meta.name));
            }
            else if (meta.type == DataType.States)
            {
                this.setStateUnit(meta.name, dStore.getStateUnits(meta.name));
            }
            else if (meta.type == DataType.Time)
            {
                this.setTimeUnit(meta.name, dStore.getTimeUnits(meta.name));
            }

            if (this.isTracked(meta))
            {
                TrackingType.forEach((TrackingType tType) -> {
                    if (meta.type == DataType.InputPorts && dStore.isInputPortTracked(meta.name, tType))
                    {
                        this.setInputPortTracked(meta.name, tType);
                    }
                    else if (meta.type == DataType.OutputPorts && dStore.isOutputPortTracked(meta.name, tType))
                    {
                        this.setOutputPortTracked(meta.name, tType);
                    }
                    else if (meta.type == DataType.States && dStore.isStateTracked(meta.name, tType))
                    {
                        this.setStateTracked(meta.name, tType);
                    }
                    else if (meta.type == DataType.Time && dStore.isTimeTracked(meta.name, tType))
                    {
                        this.setTimeTracked(meta.name, tType);
                    }
                });
            }
        });
        
        this.trackingLogSortOrder = dStore.getTrackingLogSortOrder();
        this.timeViewSortOrder = dStore.getTimeViewSortOrder();
    }

    private void addData(TrackedVariableMetadata meta, Object o)
    {
        this.addData(meta.name, meta.type, o);
    }

    private void addData(String name, DataType dType, Object o)
    {
        if (dType == DataType.InputPorts)
        {
            this.addInputPortData(name, o);
        }
        else if (dType == DataType.OutputPorts)
        {
            this.addOutputPortData(name, o);
        }
        else if (dType == DataType.States)
        {
            this.addStateData(name, o);
        }
        else if (dType == DataType.Time)
        {
            this.addTimeData(name, o);
        }
    }

    private boolean isTracked(TrackedVariableMetadata meta)
    {
        return TrackingType.filter((TrackingType tType) -> isTracked(meta, tType)).findFirst().isPresent();
    }

    private boolean isTracked(TrackedVariableMetadata meta, TrackingType tType)
    {
        return this.isTracked(meta.name, meta.type, tType);
    }

    private boolean isTracked(String name, DataType dType, TrackingType tType)
    {
        if (dType == DataType.InputPorts)
        {
            return this.isInputPortTracked(name, tType);
        }
        else if (dType == DataType.OutputPorts)
        {
            return this.isOutputPortTracked(name, tType);
        }
        else if (dType == DataType.States)
        {
            return this.isStateTracked(name, tType);
        }
        else if (dType == DataType.Time)
        {
            return this.isTimeTracked(name, tType);
        }
        return false;
    }

    private void setData(TrackedVariableMetadata meta, int index, Object o)
    {
        this.setData(meta.name, meta.type, index, o);
    }

    private void setData(String name, DataType dType, int index, Object o)
    {
        if (dType == DataType.InputPorts)
        {
            this.getInputPortData(name).set(index, o);
        }
        else if (dType == DataType.OutputPorts)
        {
            this.getOutputPortData(name).set(index, o);
        }
        else if (dType == DataType.States)
        {
            this.getStateData(name).set(index, o);
        }
        else if (dType == DataType.Time)
        {
            this.getTimeData(name).set(index, o);
        }
    }

    private void initializeTracking(boolean[][][] tracking)
    {
        for (int i = 0; i < tracking.length; ++i)
        {
            initializeTracking(tracking[i]);
        }
    }

    private void initializeTracking(boolean[][] tracking)
    {
        for (int i = 0; i < tracking.length; ++i)
        {
            tracking[i] = new boolean[TrackingType.numTypes()];
            Arrays.fill(tracking[i], false);
        }
    }

    private MutableTrackerDataStoreImpl initializeInputPorts(SortedEnumerableList<String> names)
    {
        inputPortData = new List<?>[names.size()];
        inputPorts = new HashMap<String, Integer>();

        return initialize(names, inputPortData, inputPorts);
    }

    private MutableTrackerDataStoreImpl initializeOutputPorts(SortedEnumerableList<String> names)
    {
        outputPortData = new List<?>[names.size()];
        outputPorts = new HashMap<String, Integer>();

        return initialize(names, outputPortData, outputPorts);
    }

    private MutableTrackerDataStoreImpl initializeStates(SortedEnumerableList<String> names)
    {
        stateData = new List<?>[names.size()];
        states = new HashMap<String, Integer>();

        return initialize(names, stateData, states);
    }

    private MutableTrackerDataStoreImpl initializeTime(SortedEnumerableList<String> names)
    {
        timeData = new List<?>[names.size()];
        times = new HashMap<String, Integer>();

        return initialize(names, timeData, times);
    }

    private MutableTrackerDataStoreImpl initialize(SortedEnumerableList<String> names, List<?>[] l, Map<String, Integer> m)
    {
        int i[] = { 0 };

        names.forEachSorted((String str) -> {
            l[i[0]] = new ArrayList<Object>();
            m.put(str, i[0]);
            headers.add(str);
            ++i[0];
        });

        return this;
    }

    @Override
    public Iterable<TrackedVariableMetadata> getHeader()
    {
        TrackerDataStore self = this;
        return new Iterable<TrackedVariableMetadata>() {

            @Override
            public Iterator<TrackedVariableMetadata> iterator()
            {
                return new TrackerDataStoreIterator(self);
            }
        
        };
    }

    @Override
    public Iterable<TrackedVariableMetadata> getHeaderSorted()
    {
        TrackerDataStore self = this;
        return new Iterable<TrackedVariableMetadata>() {

            @Override
            public Iterator<TrackedVariableMetadata> iterator()
            {
                return new TrackerDataStoreSortedIterator(self);
            }
        
        };
    }

    @Override
    public boolean addInputPortData(String name, Object o)
    {
        if (this.inputPorts.containsKey(name))
        {
            return this.getInputPortData(name).add(o);
        }
        return false;
    }

    @Override
    public boolean addOutputPortData(String name, Object o)
    {
        if (this.outputPorts.containsKey(name))
        {
            return this.getOutputPortData(name).add(o);
        }
        return false;
    }

    @Override
    public boolean addStateData(String name, Object o)
    {
        if (this.states.containsKey(name))
        {
            return this.getStateData(name).add(o);
        }
        return false;
    }

    @Override
    public boolean addTimeData(String name, Object o)
    {
        if (this.times.containsKey(name))
        {
            return this.getTimeData(name).add(o);
        }
        return false;
    }

    private Object getData(List<Object> l, int index)
    {
        return l.get(index);
    }

    @Override
    public Object getInputPortData(String name, int index)
    {
        return this.getData(this.getInputPortData(name), index);
    }

    @SuppressWarnings("unchecked")
    private List<Object> getInputPortData(String name)
    {
        return (List<Object>) this.inputPortData[this.inputPorts.get(name)];
    }

    @Override
    public Object getOutputPortData(String name, int index)
    {
        return this.getData(this.getOutputPortData(name), index);
    }

    @SuppressWarnings("unchecked")
    private List<Object> getOutputPortData(String name)
    {
        return (List<Object>) this.outputPortData[this.outputPorts.get(name)];
    }

    @Override
    public Object getStateData(String name, int index)
    {
        return this.getData(this.getStateData(name), index);
    }

    @SuppressWarnings("unchecked")
    private List<Object> getStateData(String name)
    {
        return (List<Object>) this.stateData[this.states.get(name)];
    }

    @Override
    public Object getTimeData(String name, int index)
    {
        return this.getData(this.getTimeData(name), index);
    }

    @SuppressWarnings("unchecked")
    private List<Object> getTimeData(String name)
    {
        return (List<Object>) this.timeData[this.times.get(name)];
    }

    private boolean isTracked(int variableIndex, DataType dType, TrackingType tType)
    {
        return this.tracking[dType.i][variableIndex][tType.i];
    }

    @Override
    public boolean isInputPortTracked(String name, TrackingType type)
    {
        int i = this.inputPorts.get(name);
        return this.isTracked(i, DataType.InputPorts, type);
    }

    @Override
    public boolean isOutputPortTracked(String name, TrackingType type)
    {
        int i = this.outputPorts.get(name);
        return this.isTracked(i, DataType.OutputPorts, type);
    }

    @Override
    public boolean isStateTracked(String name, TrackingType type)
    {
        int i = this.states.get(name);
        return this.isTracked(i, DataType.States, type);
    }

    @Override
    public boolean isTimeTracked(String name, TrackingType type)
    {
        int i = this.times.get(name);
        return this.isTracked(i, DataType.Time, type);
    }

    private void setTracked(int i, DataType dType, TrackingType tType)
    {
        if(this.tracking[dType.i][i][tType.i])
        {
            return;
        }

        this.tracking[dType.i][i][tType.i] = true;
        
        if (tType == TrackingType.LOG)
        {
            ++numLogsTracked;
            if (dType == DataType.InputPorts)
            {
                ++numInputPortsTrackedInLog;
            }
            else if (dType == DataType.OutputPorts)
            {
                ++numOutputPortsTrackedInLog;
            }
        }
        else if (tType.isTimeViewType())
        {
            ++numTimeViewTracked;
            if (dType == DataType.InputPorts)
            {
                ++numInputPortsPlotted;
            }
            else if (dType == DataType.OutputPorts)
            {
                ++numOutputPortsPlotted;
            }
        }
        else if (tType == TrackingType.DB)
        {
            ++numDBsTracked;
        }
    }

    @Override
    public MutableTrackerDataStore setInputPortTracked(String name, TrackingType tType)
    {
        int i = this.inputPorts.get(name);
        this.setTracked(i, DataType.InputPorts, tType);

        return this;
    }

    @Override
    public MutableTrackerDataStore setOutputPortTracked(String name, TrackingType tType)
    {
        int i = this.outputPorts.get(name);
        this.setTracked(i, DataType.OutputPorts, tType);

        return this;
    }

    @Override
    public MutableTrackerDataStore setStateTracked(String name, TrackingType tType)
    {
        int i = this.states.get(name);
        this.setTracked(i, DataType.States, tType);

        return this;
    }

    @Override
    public MutableTrackerDataStore setTimeTracked(String name, TrackingType tType)
    {
        int i = this.times.get(name);
        this.setTracked(i, DataType.Time, tType);

        return this;
    }

    private void setUntracked(int i, DataType dType, TrackingType tType)
    {
        if (!this.tracking[dType.i][i][tType.i])
        {
            return;
        }

        this.tracking[dType.i][i][tType.i] = false;
        
        if (tType == TrackingType.LOG)
        {
            --numLogsTracked;
            if (dType == DataType.InputPorts)
            {
                --numInputPortsTrackedInLog;
            }
            else if (dType == DataType.OutputPorts)
            {
                --numOutputPortsTrackedInLog;
            }
        }
        else if (tType.isTimeViewType())
        {
            --numTimeViewTracked;
            if (dType == DataType.InputPorts)
            {
                --numInputPortsPlotted;
            }
            else if (dType == DataType.OutputPorts)
            {
                --numOutputPortsPlotted;
            }
        }
        else if (tType == TrackingType.DB)
        {
            --numDBsTracked;
        }
    }

    @Override
    public MutableTrackerDataStore setInputPortUntracked(String name, TrackingType type)
    {
        int i = this.inputPorts.get(name);
        this.setUntracked(i, DataType.InputPorts, type);

        return this;
    }

    @Override
    public MutableTrackerDataStore setOutputPortUntracked(String name, TrackingType type)
    {
        int i = this.outputPorts.get(name);
        this.setUntracked(i, DataType.OutputPorts, type);

        return this;
    }

    @Override
    public MutableTrackerDataStore setStateUntracked(String name, TrackingType type)
    {
        int i = this.states.get(name);
        this.setUntracked(i, DataType.States, type);

        return this;
    }

    @Override
    public MutableTrackerDataStore setTimeUntracked(String name, TrackingType type)
    {
        int i = this.times.get(name);
        this.setUntracked(i, DataType.Time, type);

        return this;
    }

    private boolean isTracked(Predicate<TrackingType> f)
    {
        return TrackingType.stream().filter((TrackingType tType) -> f.test(tType)).findFirst().isPresent();
    }

    @Override
    public boolean isInputPortTracked(String name)
    {
        return this.isTracked((TrackingType type) -> {
            return this.isInputPortTracked(name, type);
        });
    }

    @Override
    public boolean isOutputPortTracked(String name)
    {
        return this.isTracked((TrackingType type) -> {
            return this.isOutputPortTracked(name, type);
        });
    }

    @Override
    public boolean isStateTracked(String name)
    {
        return this.isTracked((TrackingType type) -> {
            return this.isStateTracked(name, type);
        });
    }

    @Override
    public boolean isTimeTracked(String name)
    {
        return this.isTracked((TrackingType type) -> {
            return this.isTimeTracked(name, type);
        });
    }

    @Override
    public int dataSize(TrackerDataStore.TrackedVariableMetadata meta)
    {
        return this.dataSize(meta.name, meta.type);
    }

    @Override
    public int dataSize(String name, TrackerDataStore.DataType type)
    {
        if (type == TrackerDataStore.DataType.InputPorts)
        {
            return this.getInputPortData(name).size();
        }
        else if (type == TrackerDataStore.DataType.OutputPorts)
        {
            return this.getOutputPortData(name).size();
        }
        else if (type == TrackerDataStore.DataType.States)
        {
            return this.getStateData(name).size();
        }
        else if (type == TrackerDataStore.DataType.Time)
        {
            return this.getTimeData(name).size();
        }
        return -1;
    }

    @Override
    public int inputPortDataSize(String name)
    {
        return this.dataSize(name, TrackerDataStore.DataType.InputPorts);
    }

    @Override
    public int outputPortDataSize(String name)
    {
        return this.dataSize(name, TrackerDataStore.DataType.OutputPorts);
    }

    @Override
    public int stateDataSize(String name)
    {
        return this.dataSize(name, TrackerDataStore.DataType.States);
    }

    @Override
    public int timeDataSize(String name)
    {
        return this.dataSize(name, TrackerDataStore.DataType.Time);
    }

    @Override
    public void forEachData(String name, TrackerDataStore.DataType dType, Consumer<Object> f)
    {
        if (dType == TrackerDataStore.DataType.InputPorts)
        {
            this.getInputPortData(name).forEach(f);
        }
        else if (dType == TrackerDataStore.DataType.OutputPorts)
        {
            this.getOutputPortData(name).forEach(f);
        }
        else if (dType == TrackerDataStore.DataType.States)
        {
            this.getStateData(name).forEach(f);
        }
        else if (dType == TrackerDataStore.DataType.Time)
        {
            this.getTimeData(name).forEach(f);
        }
    }

    @Override
    public int headerSize()
    {
        return this.inputPortNames.size()
                + this.outputPortNames.size()
                + this.stateNames.size()
                + this.timeNames.size();
    }

    @Override
    public Object getData(TrackedVariableMetadata meta, int index)
    {
        return this.getData(meta.name, meta.type, index);
    }

    @Override
    public Object getData(String name, DataType dType, int index)
    {
        if (dType == TrackerDataStore.DataType.InputPorts)
        {
            return this.getInputPortData(name, index);
        }
        else if (dType == TrackerDataStore.DataType.OutputPorts)
        {
            return this.getOutputPortData(name, index);
        }
        else if (dType == TrackerDataStore.DataType.States)
        {
            return this.getStateData(name, index);
        }
        else if (dType == TrackerDataStore.DataType.Time)
        {
            return this.getTimeData(name, index);
        }
        return null;
    }

    @Override
    public SortedEnumerableList<String> getInputPortNames()
    {
        return this.inputPortNames;
    }

    @Override
    public SortedEnumerableList<String> getOutputPortNames()
    {
        return this.outputPortNames;
    }

    @Override
    public SortedEnumerableList<String> getStateNames()
    {
        return this.stateNames;
    }

    @Override
    public SortedEnumerableList<String> getTimeDimensionNames()
    {
        return this.timeNames;
    }

    @Override
    public String getInputPortUnits(String name)
    {
        return this.inputPortUnits[this.inputPorts.get(name)];
    }

    @Override
    public String getOutputPortUnits(String name)
    {
        return this.outputPortUnits[this.outputPorts.get(name)];
    }

    @Override
    public String getStateUnits(String name)
    {
        return this.stateUnits[this.states.get(name)];
    }

    @Override
    public String getTimeUnits(String name)
    {
        return this.timeUnits[this.times.get(name)];
    }

    @Override
    public MutableTrackerDataStore setInputPortUnit(String name, String unit)
    {
        this.inputPortUnits[this.inputPorts.get(name)] = unit;

        return this;
    }

    @Override
    public MutableTrackerDataStore setOutputPortUnit(String name, String unit)
    {
        this.outputPortUnits[this.outputPorts.get(name)] = unit;

        return this;
    }

    @Override
    public MutableTrackerDataStore setStateUnit(String name, String unit)
    {
        this.stateUnits[this.states.get(name)] = unit;

        return this;
    }

    @Override
    public MutableTrackerDataStore setTimeUnit(String name, String unit)
    {
        this.timeUnits[this.times.get(name)] = unit;

        return this;
    }

    @Override
    public MutableTrackerConfiguration getTrackingConfigurationFor(DataType dType, String name)
    {
        return new MutableTrackerConfigurationImpl(this, dType, name);
    }
    
    @Override
    public Optional<MutableTrackerConfiguration[]> getTrackingConfigurationFor(DataType dType)
    {
        if (dType == DataType.InputPorts)
        {
            return Optional.of(getTrackingConfigurationForInputPorts());
        }
        else if (dType == DataType.OutputPorts)
        {
            return Optional.of(getTrackingConfigurationForOutputPorts());
        }
        else if (dType == DataType.States)
        {
            return Optional.of(getTrackingConfigurationForStates());
        }
        else if (dType == DataType.Time)
        {
            return Optional.of(getTrackingConfigurationForTime());
        }
        return Optional.empty();
    }

    @Override
    public MutableTrackerConfiguration[] getTrackingConfigurationForInputPorts()
    {
        MutableTrackerConfiguration[] dataViews = new MutableTrackerConfiguration[this.inputPortNames.size()];

        this.inputPortNames.enumerateSorted((Integer i, String name) -> {
            dataViews[i] = this.getTrackingConfigurationFor(DataType.InputPorts, name);
        });

        return dataViews;
    }

    @Override
    public MutableTrackerConfiguration[] getTrackingConfigurationForOutputPorts()
    {
        MutableTrackerConfiguration[] dataViews = new MutableTrackerConfiguration[this.outputPortNames.size()];

        this.outputPortNames.enumerateSorted((Integer i, String name) -> {
            dataViews[i] = this.getTrackingConfigurationFor(DataType.OutputPorts, name);
        });

        return dataViews;
    }

    @Override
    public MutableTrackerConfiguration[] getTrackingConfigurationForStates()
    {
        MutableTrackerConfiguration[] dataViews = new MutableTrackerConfiguration[this.stateNames.size()];

        this.stateNames.enumerateSorted((Integer i, String name) -> {
            dataViews[i] = this.getTrackingConfigurationFor(DataType.States, name);
        });

        return dataViews;
    }

    @Override
    public MutableTrackerConfiguration[] getTrackingConfigurationForTime()
    {
        MutableTrackerConfiguration[] dataViews = new MutableTrackerConfiguration[this.timeNames.size()];

        this.timeNames.enumerateSorted((Integer i, String name) -> {
            dataViews[i] = this.getTrackingConfigurationFor(DataType.Time, name);
        });

        return dataViews;
    }

    @Override
    public boolean isTrackingLogEnabled()
    {
        return this.numLogsTracked > 0;
    }
    
    @Override
    public boolean isTimeViewEnabled()
    {
        return this.numTimeViewTracked > 0;
    }

    @Override
    public boolean isAnythingTracked()
    {
        return isTrackingLogEnabled() || isTimeViewEnabled() || this.numDBsTracked > 0;
    }

    @Override
    public boolean isAtLeastOneInputPortPlotted()
    {
        return this.numInputPortsPlotted > 0;
    }

    @Override
    public boolean isAtLeastOneOutputPortPlotted()
    {
        return this.numOutputPortsPlotted > 0;
    }
    
    @Override
    public boolean isAtLeastOneInputPortTrackedInLog()
    {
        return this.numInputPortsTrackedInLog > 0;
    }

    @Override
    public boolean isAtLeastOneOutputPortTrackedInLog()
    {
        return this.numOutputPortsTrackedInLog > 0;
    }

    @Override
    public SortOrder getTrackingLogSortOrder()
    {
        return trackingLogSortOrder;
    }

    @Override
    public SortOrder getTimeViewSortOrder()
    {
        return timeViewSortOrder;
    }

    @Override
    public MutableTrackerDataStore setTrackingLogSortOrder(
        SortOrder order
    )
    {
        trackingLogSortOrder = order;
        return this;
    }

    @Override
    public MutableTrackerDataStore setTimeViewSortOrder(
        SortOrder order
    )
    {
        timeViewSortOrder = order;
        return this;
    }

    @Override
    public Iterable<TrackedVariableMetadata> getHeaderReversed()
    {
        TrackerDataStore self = this;
        return new Iterable<TrackedVariableMetadata>() {

            @Override
            public Iterator<TrackedVariableMetadata> iterator()
            {
                return new TrackerDataStoreReverseIterator(self);
            }
            
        };
    }

    @Override
    public boolean isDatabaseTrackingEnabled()
    {
        return numDBsTracked > 0;
    }
}
