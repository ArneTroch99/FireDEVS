package util.tracking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import util.SortedEnumerableList;

public class EncodedTrackerDataStore extends MutableTrackerDataStoreImpl
{
    private HashMap<String, Integer>[] seeds;
    private HashMap<String, HashMap<String, Integer>>[] legend;
    private int seed;
    
    public EncodedTrackerDataStore(SortedEnumerableList<String> inputPortNames,
            SortedEnumerableList<String> outputPortNames,
            SortedEnumerableList<String> stateNames,
            SortedEnumerableList<String> timeNames)
    {
        super(inputPortNames, outputPortNames, stateNames, timeNames);
        initialize();
    }
    
    public EncodedTrackerDataStore(TrackerDataStore dataStore)
    {
        super(dataStore);
        initialize();
    }
    
    @SuppressWarnings("unchecked")
    private void initialize()
    {
        this.seed = 0;

        this.legend = (HashMap<String, HashMap<String, Integer>>[]) new HashMap[] {
                new HashMap<String, HashMap<String, Integer>>(),
                new HashMap<String, HashMap<String, Integer>>(),
                new HashMap<String, HashMap<String, Integer>>(),
                new HashMap<String, HashMap<String, Integer>>()
        };

        this.seeds = (HashMap<String, Integer>[]) new HashMap[] {
                new HashMap<String, Integer>(),
                new HashMap<String, Integer>(),
                new HashMap<String, Integer>(),
                new HashMap<String, Integer>()
        };
    }
    
    @Override
    public Object getData(TrackedVariableMetadata meta, int index)
    {
        return this.getData(meta.name, meta.type, index);
    }
    
    @Override
    public Object getData(String name, TrackerDataStore.DataType dType, int index)
    {
        Object o = super.getData(name, dType, index); 
        
        return o == null ? "" : this.encode(name, dType, o.toString(), this.nextSeed(name, dType));
    }
    
    @Override
    public void forEachData(String name, TrackerDataStore.DataType dType, Consumer<Object> f)
    {
        super.forEachData(name, dType, (Object o) -> {
            String valString = o.toString();
            
            f.accept(this.encode(name, dType, valString, this.nextSeed(name, dType)));
        });
    }
    
    public void forEachLegend(String name, TrackerDataStore.DataType dType, BiConsumer<String, Integer> f)
    {
        Set<String> valSet = new HashSet<String>();
        super.forEachData(name, dType, (Object o) -> {
            String valString = "";
            if (o != null)
            {
                valString = o.toString();
            }
            
            if (!valSet.contains(valString))
            {
                valSet.add(valString);
                f.accept(valString, this.encode(name, dType, valString, this.nextSeed(name, dType)));
            }
        });
    }
    
    private boolean legendContainsKey(String name, TrackerDataStore.DataType dType, String key)
    {
        if (!this.legend[dType.i].containsKey(name))
        {
            return false;
        }
        return this.legend[dType.i].get(name).containsKey(key);
    }
    
    private Integer getLegend(String name, TrackerDataStore.DataType dType, String value)
    {
        if (!this.legend[dType.i].containsKey(name))
        {
            return null;
        }
        return this.legend[dType.i].get(name).get(value);
    }
    
    private Integer putLegend(String name, TrackerDataStore.DataType dType, String key, Integer value)
    {
        if (!this.legend[dType.i].containsKey(name))
        {
            this.legend[dType.i].put(name, new HashMap<String, Integer>());
        }
        return this.legend[dType.i].get(name).put(key, value);
    }
    
    private int nextSeed(String name, TrackerDataStore.DataType dType)
    {
        if (this.seeds[dType.i].containsKey(name))
        {
            int seed = this.seeds[dType.i].get(name) + 1;
            this.seeds[dType.i].put(name, seed);
            return seed;
        }
        
        this.seed += 100;
        this.seeds[dType.i].put(name, this.seed);
        
        return this.seed;
    }
    
    private Integer encode(String name, TrackerDataStore.DataType dType, String valString, int seed)
    {
        Integer value;
        if (this.legendContainsKey(name, dType, valString))
        {
            value = this.getLegend(name, dType, valString);
        }
        else
        {
            value = new Integer(seed);
            this.putLegend(name, dType, valString, value);
        }
        
        return value;
    }
}
