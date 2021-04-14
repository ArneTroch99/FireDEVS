package util;

import java.util.List;
import java.util.function.BiConsumer;

public interface EnumerableList<T> extends List<T>
{
    /**
     * Iterates over the list in sorted order passing the index and element to
     * <code>f</code>.
     * 
     * @param f
     *            the function to call with each index and element
     */
    public void enumerate(BiConsumer<Integer, T> f);
}
