package util.tracking;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import util.tracking.TrackerDataStore.DataType;
import util.tracking.TrackerDataStore.TrackedVariableMetadata;

public class TrackerDataStoreReverseIterator implements Iterator<TrackedVariableMetadata>
{
    private ListIterator<String> it;
    private int count;
    private DataType type;

    private TrackerDataStore dataStore;
    public TrackerDataStoreReverseIterator(TrackerDataStore dataStore)
    {
        this.dataStore = dataStore;
        type = TrackerDataStore.DataType.Time;
        it = dataStore.getTimeDimensionNames().sortedListIterator(dataStore.getTimeDimensionNames().size() - 1);
        count = 0;
    }

    private boolean hasNext(int count)
    {
        if (it.hasPrevious())
        {
            return true;
        }

        switch (count)
        {
        case 0:
            it = dataStore.getStateNames().sortedListIterator(dataStore.getStateNames().size() - 1);
            type = DataType.States;
            break;
        case 1:
            it = dataStore.getOutputPortNames().sortedListIterator(dataStore.getOutputPortNames().size() - 1);
            type = DataType.OutputPorts;
            break;
        case 2:
            it = dataStore.getInputPortNames().sortedListIterator(dataStore.getInputPortNames().size() - 1);
            type = DataType.InputPorts;
            break;
        }
        return it.hasPrevious();
    }

    @Override
    public boolean hasNext()
    {
        while (count < 3 && !it.hasPrevious())
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
            return new TrackedVariableMetadata(it.previous(), type);
        }
        throw new NoSuchElementException();
    }
}
