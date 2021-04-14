package util.tracking;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum TrackingType
{
    STACK(0, "Stack", true),
    NO_STACK(1, "No Stack", true),
    SEPARATE(2, "Separate", true),
    LOG(3, "Tabulate"),
    DB(4, "Database");

    public int i;
    private String name;
    private boolean isTimeView;

    private TrackingType(int i, String name)
    {
        this(i, name, false);
    }
    
    private TrackingType(int i, String name, boolean isTimeView)
    {
        this.i = i;
        this.name = name;
        this.isTimeView = isTimeView;
    }
    
    public static void forEach(Consumer<TrackingType> c)
    {
        Arrays.stream(values()).forEach(c);
    }
    
    public static void enumerate(BiConsumer<Integer, TrackingType> bc)
    {
        final TrackingType[] values = values();
        final int numValues = values.length;

        for (int i = 0; i < numValues; ++i)
        {
            bc.accept(i, values[i]);
        }
    }
    
    public static Stream<TrackingType> stream()
    {
        return Arrays.stream(values());
    }
    
    public static Stream<TrackingType> filter(Predicate<TrackingType> p)
    {
        return stream().filter(p);
    }
    
    public static int numTypes()
    {
        return values().length;
    }
    
    public boolean isTimeViewType()
    {
        return isTimeView;
    }
    
    public String toString()
    {
        return this.name;
    }
}
