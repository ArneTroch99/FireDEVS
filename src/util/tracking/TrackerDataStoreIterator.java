package util.tracking;

import java.util.Iterator;
import java.util.NoSuchElementException;

import util.tracking.TrackerDataStore.DataType;
import util.tracking.TrackerDataStore.TrackedVariableMetadata;

public class TrackerDataStoreIterator implements Iterator<TrackedVariableMetadata>
{
    private Iterator<String> it;
    private int count;
    private DataType type;

    private TrackerDataStore dataStore;
    public TrackerDataStoreIterator(TrackerDataStore dataStore)
    {
        this.dataStore = dataStore;
        type = TrackerDataStore.DataType.InputPorts;
        it = dataStore.getInputPortNames().iterator();
        count = 0;
    }

    private boolean hasNext(int count)
    {
        if (it.hasNext())
        {
            return true;
        }

        switch (count)
        {
        case 0:
            it = dataStore.getOutputPortNames().iterator();
            type = DataType.OutputPorts;
            break;
        case 1:
            it = dataStore.getStateNames().iterator();
            type = DataType.States;
            break;
        case 2:
            it = dataStore.getTimeDimensionNames().iterator();
            type = DataType.Time;
            break;
        }
        return it.hasNext();
    }

    @Override
    public boolean hasNext()
    {
        while (count < 3 && !it.hasNext())
        {
            if (hasNext(count++))
            {
                return true;
            }
        }
        return hasNext(count);
    }

    @Override
    public TrackedVariableMetadata next()
    {
        if (hasNext())
        {
            return new TrackedVariableMetadata(it.next(), type);
        }
        throw new NoSuchElementException();
    }
}
