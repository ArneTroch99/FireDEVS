package util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

public interface SortedList<T> extends List<T>
{
    /**
     * @return iterator that traverses the list in sorted order
     */
    public Iterator<T> sortedIterator();
    
    /**
     * The SortedListIterator will return the unsorted indices of the object.
     * These indices can be passed to {@link #get(int index)} to return the same object
     * that the iterator will return.
     * 
     * @return listiterator that traverses the list in sorted order
     */
    public ListIterator<T> sortedListIterator();
    
    /**
     * The SortedListIterator will return the unsorted indices of the object.
     * These indices can be passed to {@link #get(int index)} to return the same object
     * that the iterator will return.
     * 
     * @param index the starting point of the iterator
     * @return listiterator that traverses the list in sorted order at the specified index
     */
    public ListIterator<T> sortedListIterator(int index);
    
    /**
     * @return the sorted position for the item at <code>index</code>
     */
    public int getSortedIndex(int index);
    
    /**
     * Finds the position of the first object <code>o</code> in list and returns
     * its sorted index, or <b>-1</b> if not found.
     * 
     * @param o
     *            the object to search
     * @return the sorted index
     */
    public int sortedIndexOf(Object o);

    /**
     * @param index
     *            sorted index
     * @return object at sorted index
     */
    public T getSorted(int index);

    /**
     * This method will iterate over each element in sorted order, passing each
     * consecutive element to <code>f</code>.
     * 
     * @param f
     *            the function to call with each element.
     */
    public void forEachSorted(Consumer<T> f);
    
    /**
     * @return a string containing the string representation of each item
     * in the list, in the sorted order.
     */
    public String toSortedString();
    
    /**
     * Default sorter for String comparisons. Compares strings case insensitive, breaking ties
     * using case sensitivity.
     */
    public static Comparator<String> DefaultStringComparator = (String lhs, String rhs) -> {
        int comparisonValue = lhs.toLowerCase().compareTo(rhs.toLowerCase());
        
        return comparisonValue == 0 ? lhs.compareTo(rhs) : comparisonValue;
    };
    
    /**
     * Standard integer comparison method using {@link java.lang.Integer#compareTo(Integer)} .
     */
    public static Comparator<Integer> DefaultIntegerComparator = (Integer lhs, Integer rhs) -> {
        return lhs.compareTo(rhs);
    };
    
    /**
     * Standard Double comparison method using {@link java.lang.Double#compareTo(Double)} .
     */
    public static Comparator<Double> DefaultDoubleComparator = (Double lhs, Double rhs) -> {
        return lhs.compareTo(rhs);
    };
}
