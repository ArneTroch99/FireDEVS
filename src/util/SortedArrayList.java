package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Simply, this class maintains two different sort orders for an
 * {@link ArrayList}. If two different sort orders are needed from the same
 * {@link ArrayList}, this class will efficiently maintain iterators that will
 * respect the specified sort order. <b>Standard {@link ArrayList} methods do
 * not respect the sort order</b>, only those with <b>"sorted" in the
 * name</b>.<br>
 * <br>
 * 
 * <b>This class will insert items into an already sorted position, therefore
 * calling {@link #sort()} is unnecessary unless items are modified without the
 * knowledge of the class. The caller will need to use {@link #sort()} when
 * modifying the contained objects to have the sort order updated, otherwise
 * this structure may become inconsistent.<br>
 * <br>
 * <b>USE THE SET METHOD.</b><br>
 * <br>
 * 
 * For example:</b><br>
 * <code>
 * class SomeClass<br>
 * {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;public Integer someVar;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;public SomeClass(Integer someVar)<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;this.someVar = someVar;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br>
 *<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;public int compareTo(Integer i)<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return this.someVar.compareTo(i);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br>
 * }<br>
 *<br>
 * SortedArrayList<SomeClass> e = new SortedArrayList<SomeClass>(<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Arrays.toList(new SomeClass(1), new SomeClass(2)),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;(SomeClass lhs, SomeClass rhs) -> {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return lhs.compareTo(rhs);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br>
 * );<br>
 * <br>
 * for (SomeClass c : e)<br>
 * {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;//Make every other variable negative<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;c.someVar = someVar % 2 == 0 ? someVar * -1 : someVar;<br>
 * }<br>
 * <br>
 * Iterator<SomeClass> it = e.sortedIterator();<br>
 *<br>
 * it.next(); // 1<br>
 * it.next(); // -2!<br>
 * </code>
 * 
 * @param <T>
 */
public class SortedArrayList<T> implements SortedEnumerableList<T>
{
    private List<T> list;
    private ArrayList<Integer> sortOrder;
    private Comparator<? super T> sortFunction;
    private SortedArrayList<T> self = this;

    private SortedArrayList()
    {
        this.list = new ArrayList<T>();
        this.sortOrder = new ArrayList<Integer>();
    }
    
    public static SortedEnumerableList<String> MakeSortedStringArrayList()
    {
        return new SortedArrayList<String>(SortedEnumerableList.DefaultStringComparator);
    }
    
    public static SortedEnumerableList<String> MakeSortedStringArrayList(String... args)
    {
        SortedEnumerableList<String> stringList = new SortedArrayList<String>(SortedEnumerableList.DefaultStringComparator);
        for (String arg : args)
        {
            stringList.add(arg);
        }
        
        return stringList;
    }
    
    public static SortedEnumerableList<String> MakeSortedStringArrayList(Collection<String> c)
    {
        return new SortedArrayList<String>(c, SortedEnumerableList.DefaultStringComparator);
    }
    
    public static SortedEnumerableList<Integer> MakeSortedIntegerArrayList()
    {
        return new SortedArrayList<Integer>(SortedEnumerableList.DefaultIntegerComparator);
    }
    
    public static SortedEnumerableList<Integer> MakeSortedIntegerArrayList(Integer... args)
    {
        SortedEnumerableList<Integer> integerList = new SortedArrayList<Integer>(SortedEnumerableList.DefaultIntegerComparator);
        for (Integer arg : args)
        {
            integerList.add(arg);
        }
        
        return integerList;
    }
    
    public static SortedEnumerableList<Integer> MakeSortedIntegerArrayList(Collection<Integer> c)
    {
        return new SortedArrayList<Integer>(c, SortedEnumerableList.DefaultIntegerComparator);
    }
    
    public static SortedEnumerableList<Double> MakeSortedDoubleArrayList()
    {
        return new SortedArrayList<Double>(SortedEnumerableList.DefaultDoubleComparator);
    }
    
    public static SortedEnumerableList<Double> MakeSortedDoubleArrayList(Double... args)
    {
        SortedEnumerableList<Double> doubleList = new SortedArrayList<Double>(SortedEnumerableList.DefaultDoubleComparator);
        for (Double arg : args)
        {
            doubleList.add(arg);
        }
        
        return doubleList;
    }
    
    public static SortedEnumerableList<Double> MakeSortedDoubleArrayList(Collection<Double> c)
    {
        return new SortedArrayList<Double>(c, SortedEnumerableList.DefaultDoubleComparator);
    }
    
    public SortedArrayList(Comparator<? super T> f)
    {
        this();
        setSortFunction(f);
    }

    public SortedArrayList(int size, Comparator<? super T> f)
    {
        this.list = new ArrayList<T>(size);
        setSortFunction(f);
        this.sortOrder = new ArrayList<Integer>(size);
        initialize();
    }

    public SortedArrayList(Collection<? extends T> c, Comparator<? super T> f)
    {
        this.list = new ArrayList<T>(c.size());
        this.sortOrder = new ArrayList<Integer>(c.size());
        initialize();

        setSortFunction(f);
        this.addAll(c);
    }

    private SortedArrayList<T> initialize()
    {
        for (int i = 0; i < this.list.size(); ++i)
        {
            this.sortOrder.add(i, Integer.valueOf(i));
        }
        return this;
    }

    private boolean isInitialized()
    {
        return this.sortOrder != null;
    }

    /**
     * Sorts list using internal {@link Comparator} set by
     * {@link #setSortFunction(Comparator)}<br>
     * <br>
     * For more information about how this can be in an invalid state, check
     * <b>See Also</b>.
     * 
     * @see SortedArrayList
     * @see ArrayList#sort(Comparator)
     */
    public void sort()
    {
        this.sort(sortFunction);
    }

    /**
     * Sets the function used to determine ordering during sort. Follows the
     * standard pattern established by
     * {@link Comparator#compare(Object, Object)}.
     * 
     * @param f
     *            Ordering function to be used during sorting
     */
    public void setSortFunction(Comparator<? super T> f)
    {
        this.sortFunction = f;
    }

    @Override
    public Iterator<T> sortedIterator()
    {
        return new SortedIterator();
    }

    @Override
    public ListIterator<T> sortedListIterator()
    {
        return new SortedListIterator();
    }
    
    @Override
    public ListIterator<T> sortedListIterator(int index)
    {
        return new SortedListIterator(index);
    }

    @Override
    public void forEachSorted(Consumer<T> f)
    {
        Iterator<T> it = sortedIterator();
        while (it.hasNext())
        {
            f.accept(it.next());
        }
    }

    @Override
    public void enumerateSorted(BiConsumer<Integer, T> f)
    {
        Iterator<T> it = sortedIterator();
        Integer i = Integer.valueOf(0);
        while (it.hasNext())
        {
            T o = it.next();

            f.accept(i, o);
            ++i;
        }
    }

    private class SortedIterator implements Iterator<T>
    {
        private Iterator<Integer> it;

        private SortedIterator()
        {
            this.it = sortOrder.iterator();
        }

        @Override
        public boolean hasNext()
        {
            return this.it.hasNext();
        }

        @Override
        public T next() throws NoSuchElementException
        {
            Integer i = it.next();
            return get(i);
        }
    }

    private class SortedListIterator implements ListIterator<T>
    {
        private ListIterator<Integer> it;
        private Integer nextIndex;
        private Integer prevIndex;
        private boolean hasNext;
        private boolean hasPrevious;
        private boolean needsReset;
        private Boolean isForward;

        private SortedListIterator()
        {
            this(0);
        }
        
        private SortedListIterator(int index)
        {
            it = self.sortOrder.listIterator(index);
            if (index == self.sortOrder.size() - 1)
            {
                advance(1);
                retreat(1);
                this.hasNext = false;
                this.nextIndex = this.prevIndex;
            }
            else
            {
                advance(1);
                this.prevIndex = this.nextIndex;
            }

            if (index == 0)
            {
                if (sortOrder.size() > 2)
                {
                    this.hasPrevious = false;
                }
                else if (sortOrder.size() == 1)
                {
                    this.prevIndex = 0;
                    this.nextIndex = 0;
                    this.hasPrevious = true;
                    this.hasNext = true;
                }
            }
            this.needsReset = false;
            this.isForward = null;
        }

        @Override
        public void add(T arg0)
        {
            self.add(this.nextIndex(), arg0);
            this.needsReset = true;
        }

        private void resetForward()
        {
            Integer index = self.sortOrder.indexOf(this.nextIndex) - 1;
            this.it = self.sortOrder.listIterator();

            if (index == -1 && self.list.size() == 1)
            {
                advance(1);
            }
            else if (index == -2)
            {
                hasPrevious = false;
                this.prevIndex = null;
            }
            else
            {
                advance(index == -1 ? 0 : index);
            }
        }

        private void resetBackward()
        {
            Integer index = self.sortOrder.indexOf(this.prevIndex) + 1;
            this.it = self.sortOrder.listIterator();
            if (index == 0)
            {
                this.hasNext = false;
                this.nextIndex = null;
            }
            else
            {
                advance(index);
                retreat(1);
            }
        }

        private void advance(int times)
        {
            while (times > 0)
            {
                if (!it.hasNext())
                {
                    if (this.nextIndex != null)
                    {
                        this.prevIndex = this.nextIndex;
                        this.nextIndex = null;
                        this.hasPrevious = true;
                        this.hasNext = false;
                    }
                    return;
                }
                this.prevIndex = this.nextIndex;
                this.nextIndex = it.next();
                this.hasNext = true;
                this.hasPrevious = true;
                --times;
            }
        }

        private void retreat(int times)
        {
            while (times > 0)
            {
                if (!it.hasPrevious())
                {
                    if (this.prevIndex != null)
                    {
                        this.nextIndex = this.prevIndex;
                        this.prevIndex = null;
                        this.hasPrevious = false;
                        this.hasNext = true;
                    }
                    return;
                }
                this.nextIndex = this.prevIndex;
                this.prevIndex = it.previous();
                this.hasNext = true;
                this.hasPrevious = true;
                --times;
            }
        }

        @Override
        public boolean hasNext()
        {
            return this.hasNext;
        }

        @Override
        public boolean hasPrevious()
        {
            return this.hasPrevious;
        }

        @Override
        public T next()
        {
            if (this.needsReset)
            {
                resetForward();
                this.needsReset = false;
            }

            if (this.isForward == null)
            {
                this.isForward = true;
            }
            else if (this.isForward == false)
            {
                if (self.list.size() > 1)
                {
                    advance(1);
                }
                this.isForward = true;
            }

            if (this.hasNext)
            {
                T obj = self.get(this.nextIndex);
                advance(1);
                return obj;
            }
            else
            {
                it.next();
                return null;
            }
        }

        @Override
        public int nextIndex()
        {
            return this.nextIndex;
        }

        @Override
        public T previous()
        {
            if (this.needsReset)
            {
                resetBackward();
                this.needsReset = false;
            }

            if (this.isForward == null)
            {
                this.isForward = false;
            }
            else if (this.isForward == true)
            {
                if (self.size() > 1)
                {
                    retreat(1);
                }
                this.isForward = false;
            }

            if (this.hasPrevious)
            {
                T obj = self.get(this.prevIndex);
                retreat(1);
                return obj;
            }
            else
            {
                it.previous();
                return null;
            }
        }

        @Override
        public int previousIndex()
        {
            return this.prevIndex;
        }

        @Override
        public void remove()
        {
            if (!this.hasPrevious && !this.isForward)
            {
                self.remove((int) this.nextIndex);
                this.needsReset = true;
            }
            else if (!this.hasNext && this.isForward)
            {
                self.remove((int) this.prevIndex);
                this.needsReset = true;
            }
            else
            {
                Integer nextIndex = this.nextIndex;
                self.remove(self.get(this.isForward ? this.prevIndex : this.nextIndex));
                this.nextIndex = this.prevIndex;
                this.prevIndex = nextIndex + 1;
                this.needsReset = true;
            }
        }

        @Override
        public void set(T arg0)
        {
            self.set(this.prevIndex == this.nextIndex ? this.prevIndex : this.prevIndex + 1, arg0);
            self.sort();
            this.needsReset = true;
        }
    }

    @Override
    public void sort(Comparator<? super T> f)
    {
        @SuppressWarnings("unchecked")
        Comparator<Object> _f = (Comparator<Object>) f;
        SortedArrayList<? super T> self = this;
        this.sortOrder.sort(((Integer lhs, Integer rhs) -> {
            return _f.compare(self.get(lhs), self.get(rhs));
        }));
    }

    private void addSortOrder(int i)
    {
        Integer end = this.list.size() - 1;
        if (i >= end)
        {
            addSortOrder();
        }
        else
        {
            this.sortOrder.add(i, end);
        }
    }

    private void addSortOrder()
    {
        if (!isInitialized())
        {
            this.sortOrder = new ArrayList<Integer>();
        }
        if (isInitialized() && this.list.size() > this.sortOrder.size())
        {
            this.sortOrder.add(this.list.size() - 1);
        }
    }

    @Override
    public boolean add(T e)
    {
        int i = 0;

        if (this.sortOrder.size() > 0)
        {
            i = maxCompared(e, 0, this.sortOrder.size());
        }

        if (this.list.add(e))
        {
            addSortOrder(i);

            return true;
        }
        else
        {
            return false;
        }
    }
    
    private int binarySearch(T o, int start, int end)
    {
        List<Integer> l = this.sortOrder.subList(start, end);
        if (l.size() == 1)
        {
            int comparison = this.sortFunction.compare(o, this.get(l.get(0)));
            if (comparison < 0)
            {
                return -1;
            }
            else if (comparison == 0)
            {
                return l.get(0);
            }
            else
            {
                return -1;
            }
        }
        else
        {
            int midpoint = Math.floorDiv(l.size(), 2);
            if (this.sortFunction.compare(o, this.get(l.get(midpoint))) < 0)
            {
                return this.binarySearch(o, start, start + midpoint);
            }
            else
            {
                return this.binarySearch(o, start + midpoint, end);
            }
        }
    }

    private int maxCompared(T o, int start, int end)
    {
        List<Integer> l = this.sortOrder.subList(start, end);
        if (l.size() == 1)
        {
            if (this.sortFunction.compare(o, this.get(l.get(0))) < 0)
            {
                return 0;
            }
            else
            {
                return start + 1;
            }
        }
        else
        {
            int midpoint = Math.floorDiv(l.size(), 2);
            if (this.sortFunction.compare(o, this.get(l.get(midpoint))) < 0)
            {
                return maxCompared(o, start, start + midpoint);
            }
            else
            {
                return maxCompared(o, start + midpoint, end);
            }
        }
    }

    @Override
    public void add(int index, T e)
    {
        int i = 0;

        if (this.sortOrder.size() > 0)
        {
            i = maxCompared(e, 0, this.sortOrder.size());
        }

        this.list.add(index, e);
        incrementSortOrder(index);
        this.sortOrder.add(i, index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        Boolean[] changed = { Boolean.valueOf(false) };

        c.forEach((Object o) -> {
            if (add((T) o))
            {
                changed[0] = true;
            }
        });

        return changed[0];
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        return this.list.addAll(index, c);
    }

    @Override
    public void clear()
    {
        this.sortOrder.clear();
        this.list.clear();
    }

    @Override
    public boolean contains(Object o)
    {
        return this.indexOf(o) != -1;
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        for (Object o : c)
        {
            if (!this.contains(o))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public T get(int index)
    {
        return this.list.get(index);
    }

    @Override
    public void enumerate(BiConsumer<Integer, T> f)
    {
        ListIterator<T> it = this.listIterator();

        while (it.hasNext())
        {
            Integer i = it.nextIndex();
            f.accept(i, it.next());
        }
    }

    @Override
    public boolean removeIf(Predicate<? super T> f)
    {
        boolean[] success = { false };
        this.enumerate((Integer i, T o) -> {
            if (f.test(o))
            {
                this.remove((int) i);
                success[0] = true;
            }
        });

        return success[0];
    }

    @Override
    public int sortedIndexOf(Object o)
    {
        Integer i = this.indexOf(o);
        if (i > -1)
        {
            return this.sortOrder.indexOf(i);
        }
        return -1;
    }

    @Override
    public T getSorted(int i)
    {
        return this.get(this.sortOrder.get(i));
    }

    @SuppressWarnings("unchecked")
    @Override
    public int indexOf(Object o)
    {
        return this.size() == 0 ? -1 : this.binarySearch((T) o, 0, this.size());
    }

    @Override
    public boolean isEmpty()
    {
        return this.list.isEmpty();
    }

    @Override
    public Iterator<T> iterator()
    {
        return this.list.iterator();
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return this.list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator()
    {
        return this.list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index)
    {
        return this.list.listIterator(index);
    }

    private void incrementSortOrder(int index)
    {
        for (int _i = 0; _i < this.sortOrder.size(); ++_i)
        {
            int j = this.sortOrder.get(_i);
            if (j >= index)
            {
                this.sortOrder.set(_i, j + 1);
            }
        }
    }

    private void removeSortOrder(int index)
    {
        int i = this.sortOrder.get(index);
        this.sortOrder.remove(index);

        for (int _i = 0; _i < this.sortOrder.size(); ++_i)
        {
            int j = this.sortOrder.get(_i);
            if (j > i)
            {
                this.sortOrder.set(_i, j - 1);
            }
        }
    }

    @Override
    public boolean remove(Object o)
    {
        if (isInitialized())
        {
            int i = this.indexOf(o);
            return this.remove(i) != null;
        }
        return this.remove(o);
    }

    @Override
    public T remove(int index)
    {
        if (isInitialized())
        {
            removeSortOrder(this.sortOrder.indexOf((Integer) index));
        }
        return this.list.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        Boolean[] isChanged = { Boolean.valueOf(false) };

        c.forEach((Object o) -> {
            this.remove(o);
            isChanged[0] = true;
        });

        return isChanged[0];
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        int i = 0;
        boolean changed = false;
        
        while(i < this.size())
        {
            if (!c.contains(this.get(i)))
            {
                this.remove(i);
                changed = true;
            }
            else
            {
                ++i;
            }
        }
    
        return changed;
    }

    @Override
    public T set(int index, T element)
    {
        if (index < this.size())
        {
            if(!this.sortOrder.remove((Integer) index))
            {
                throw new UnsupportedOperationException("Failed to remove " + index);
            }
            int i = maxCompared(element, 0, this.sortOrder.size());
            this.sortOrder.add(i, index);
            return this.list.set(index, element);
        }
        else
        {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    @Override
    public int size()
    {
        return this.list.size();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex)
    {
        SortedArrayList<T> l = new SortedArrayList<T>(this.sortFunction);
        l.list = this.list.subList(fromIndex, toIndex);
        l.initialize();
        l.sort();
        
        return l;
    }

    @Override
    public Object[] toArray()
    {
        return this.list.toArray();
    }

    @Override
    public <_T> _T[] toArray(_T[] a)
    {
        return this.list.toArray(a);
    }
    
    @Override
    public boolean equals(Object rhs)
    {
        if (rhs instanceof List<?>)
        {
            return this.list.equals(rhs);
        }
        return false;
    }
    
    @Override
    public String toString()
    {
        return this.list.toString();
    }
    
    @Override
    public String toSortedString()
    {
        StringBuilder sb = new StringBuilder(2);
        sb.append("[");
        this.forEachSorted((T o) -> {
           sb.append(o.toString())
             .append(", ");
        });
        
        if (sb.length() > 1) 
        {
            sb.setCharAt(sb.length() - 2, ']');
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        
        return sb.append("]").toString();
    }

    @Override
    public int getSortedIndex(int index)
    {
        return this.sortOrder.get(index);
    }
    
}
