package util;

import java.util.function.BiConsumer;

public interface SortedEnumerableList<T> extends SortedList<T>, EnumerableList<T>
{
    /**
     * This method will iterate over the list in sorted order passing each
     * element and its enumeration to <code>f</code>
     * 
     * @param f
     *            the function to call with each index and element
     */
    public void enumerateSorted(BiConsumer<Integer, T> f);
}
