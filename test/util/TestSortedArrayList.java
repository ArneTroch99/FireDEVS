package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class TestSortedArrayList
{

    private SortedArrayList<String> makeInstance()
    {
        return new SortedArrayList<String>((String lhs, String rhs) -> {
            return lhs.compareTo(rhs);
        });
    }
    @Test
    public void SmokeTest()
    {
        List<String> l = Arrays.asList("c", "a", "b");
        SortedArrayList<String> cut = new SortedArrayList<String>(l, (String lhs, String rhs) -> {
            return lhs.compareTo(rhs);
        });
        
        Integer[] indices = { 1, 2, 0 };
        int[] i = { 0 };
        cut.forEachSorted((String s) -> {
           assertEquals(l.get(indices[i[0]]), s);
           ++i[0];
        });
        assertEquals(3, i[0]);
        
        i[0] = 0;
        cut.enumerateSorted((Integer index, String s) -> {
           assertEquals(Integer.valueOf(i[0]), index);
           assertEquals(l.get(indices[index]), s);
           ++i[0];
        });
        assertEquals(3, i[0]);
        
        i[0] = 0;
        Iterator<String> sortedIt = cut.sortedIterator();
        while(sortedIt.hasNext())
        {
            assertEquals(l.get(indices[i[0]]), sortedIt.next());
            ++i[0];
        }
        assertEquals(3, i[0]);
        
        i[0] = 0;
        ListIterator<String> sortedListIt = cut.sortedListIterator();
        assertEquals(1, sortedListIt.nextIndex());
        assertEquals(1, sortedListIt.previousIndex());
        while(sortedListIt.hasNext())
        {
            assertEquals(indices[i[0]], (Integer) sortedListIt.nextIndex());
            assertEquals(l.get(indices[i[0]]), sortedListIt.next());
            ++i[0];
        }
        assertEquals(3, i[0]);
        
        --i[0];
        while(sortedListIt.hasPrevious())
        {
            assertEquals(indices[i[0]], (Integer) sortedListIt.previousIndex());
            assertEquals(l.get(indices[i[0]]), sortedListIt.previous());
            --i[0];
        }
        assertEquals(-1, i[0]);

        i[0] = 0;
        cut.forEach((String s) -> {
            assertEquals(l.get(i[0]), s);
            ++i[0];
        });
        assertEquals(3, i[0]);
        
        assertEquals(cut.get(0), "c");
        assertEquals(cut.get(1), "a");
        assertEquals(cut.get(2), "b");
        assertEquals(l, cut);
        assertEquals("[c, a, b]", cut.toString());
        assertEquals("[a, b, c]", cut.toSortedString());
    }
    
    @Test
    public void testEmptySortedString()
    {
        SortedEnumerableList<String> cut = makeInstance();
        assertEquals("[]", cut.toSortedString());
    }
    
    @Test
    public void testSortedListIterator()
    {
        SortedEnumerableList<String> cut = makeInstance();
        cut.add("z");
        cut.add("a");
        cut.add("b");
        
        ListIterator<String> it = cut.sortedListIterator(1);
        assertTrue(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals("b", it.next());
        assertTrue(it.hasNext());
        assertEquals("z", it.next());
        assertFalse(it.hasNext());
        
        assertEquals("z", it.previous());
        assertTrue(it.hasPrevious());
        assertEquals("b", it.previous());
        
        assertTrue(it.hasPrevious());
        assertEquals("a", it.previous());
        
        assertFalse(it.hasPrevious());
        
        it = cut.sortedListIterator(2);
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals("z", it.previous());
        assertTrue(it.hasPrevious());
        assertEquals("b", it.previous());
        
        cut = makeInstance();
        cut.add("z");
        
        it = cut.sortedListIterator(0);
        assertTrue(it.hasPrevious());
        assertEquals("z", it.previous());
    }
    
    private void runAddRemoveTest(SortedArrayList<String> s, Consumer<SortedArrayList<String>> removeFunc)
    {
        String[] expected1 = { "a", "b", "c" };
        int[] i = { 0 };
        s.forEachSorted((String str) -> {
            assertEquals(expected1[i[0]], str);
            ++i[0];
        });
        assertEquals(3, i[0]);
        
        removeFunc.accept(s);
        
        String[] expected2 = { "a", "b" };
        i[0] = 0;
        s.forEachSorted((String str) -> {
            assertEquals(expected2[i[0]], str);
            ++i[0];
        });
        assertEquals(2, i[0]);
    }

    @Test
    public void testAddRemoveIndex()
    {
        SortedArrayList<String> s = makeInstance();
        
        s.add(0, "c");
        s.add(1, "b");
        s.add(2, "a");
        
        runAddRemoveTest(s, (SortedArrayList<String> _s) -> {
            _s.remove(0);
        });
    }
    
    @Test
    public void testAddRemoveMultiple()
    {
        SortedArrayList<String> s = makeInstance();
        assertTrue("m".compareTo("r") < 1);
        s.addAll(Arrays.asList("c", "b"));
        s.add("b");
        s.add("a");
        s.add("q");
        s.add("r");
        s.add("A");
        s.add("0");
        s.add("s");
        s.add("t");
        s.add("m");
        s.add("m");

        String[] expected = { "0", "A", "a", "b", "b", "c", "m", "m", "q", "r", "s", "t" };
        int[] i = { 0 };
        s.forEachSorted((String str) -> {
            assertEquals(expected[i[0]], str);
            ++i[0];
        });
        assertEquals(12, i[0]);
        
        assertEquals("c", s.remove(0));
        assertEquals("b", s.remove(1));
        assertEquals("m", s.remove(s.size() - 1));
        assertEquals("m", s.remove(s.size() - 1));
        assertEquals("r", s.remove(3));
        assertEquals("q", s.remove(2));
        
        assertEquals("0", s.getSorted(0));
        assertEquals("A", s.getSorted(1));
        assertEquals("a", s.getSorted(2));
        assertEquals("b", s.getSorted(3));
        assertEquals("s", s.getSorted(4));
        assertEquals("t", s.getSorted(5));
        assertEquals(6, s.size());

        s = makeInstance();
        s.addAll(Arrays.asList("c", "c", "b"));

        expected[0] = "b";
        expected[1] = "c";
        expected[2] = "c";
        i[0] = 0;
        s.forEachSorted((String str) -> {
            assertEquals(expected[i[0]], str);
            ++i[0];
        });
        assertEquals(3, i[0]);
    }

    @Test
    public void testAddRemoveObject()
    {
        SortedArrayList<String> s = makeInstance();
        
        s.add("c");
        s.add("b");
        s.add("a");
        
        runAddRemoveTest(s, (SortedArrayList<String> _s) -> {
            _s.remove("c");
        });
    }
    
    @Test
    public void testAddRemoveAll()
    {
        SortedArrayList<String> s = makeInstance();
        
        s.addAll(Arrays.asList("c", "b", "a"));
        
        runAddRemoveTest(s, (SortedArrayList<String> _s) -> {
            _s.removeAll(Arrays.asList("c"));
        });
    }

    @Test
    public void testAddRemoveInPlace()
    {
        SortedArrayList<String> s = makeInstance();
        
        s.addAll(Arrays.asList("c", "b", "a"));
        
        s.add(0, "d");
        
        String[] expected = { "d", "c", "b", "a" };
        Integer[] i = { Integer.valueOf(0) };
        s.forEach((String str) -> {
            assertEquals(expected[i[0]], str);
            ++i[0];
        });
        assertEquals((Integer) 4, i[0]);
        
        expected[0] = "a";
        expected[1] = "b";
        expected[2] = "c";
        expected[3] = "d";
        
        i[0] = 0;
        s.forEachSorted((String str) -> {
            assertEquals(expected[i[0]], str);
            ++i[0];
        });
        assertEquals((Integer) 4, i[0]);
        
        s.removeIf((String str) -> {
           return str.equals("b"); 
        });
        
        assertEquals("d", s.get(0));
        assertEquals("c", s.get(1));
        assertEquals("a", s.get(2));
        
        Stream<String> stream = Arrays.stream(expected, 2, 4);
        Iterator<String> it = stream.iterator();
        i[0] = 0;
        s.forEachSorted((String str) -> {
           if  (i[0] == 0)
           {
               assertEquals("a", str);
           }
           else
           {
               assertEquals(it.next(), str);
           }
           ++i[0];
        });
        assertEquals((Integer) 3, i[0]);
        
        s.add(0, "B");
        s.add(0, "f");
        s.add(2, "b");
        s.add(5, "e");
        s.add(7, "A");
        
        assertEquals("A", s.get(7));
        assertEquals("A", s.getSorted(0));
        assertEquals("B", s.get(1));
        assertEquals("B", s.getSorted(1));
        assertEquals("b", s.get(2));
        assertEquals("b", s.getSorted(3));
        assertEquals("c", s.get(4));
        assertEquals("c", s.getSorted(4));
        assertEquals("a", s.get(6));
        assertEquals("a", s.getSorted(2));
        assertEquals("e", s.get(5));
        assertEquals("e", s.getSorted(6));
        assertEquals("d", s.get(3));
        assertEquals("d", s.getSorted(5));
        assertEquals("f", s.get(0));
        assertEquals("f", s.getSorted(7));
        assertEquals(8, s.size());
    }

    @Test
    public void testAddRemoveSortedIterator()
    {
        SortedArrayList<String> s = makeInstance();
        
        s.addAll(Arrays.asList("c", "b", "a"));
        
        ListIterator<String> sortedIt = s.sortedListIterator();
        
        assertEquals("a", sortedIt.next());
        sortedIt.add("z");
        assertEquals("b", sortedIt.next());
        assertEquals("c", sortedIt.next());
        assertEquals("z", sortedIt.next());
        assertFalse(sortedIt.hasNext());
        assertTrue(sortedIt.hasPrevious());
        assertEquals("z", sortedIt.previous());
        sortedIt.add("a");
        assertEquals("c", sortedIt.previous());
        assertEquals("b", sortedIt.previous());
        assertEquals("a", sortedIt.previous());
        assertEquals("a", sortedIt.previous());
        assertFalse(sortedIt.hasPrevious());

        assertTrue(sortedIt.hasNext());
        assertEquals("a", sortedIt.next());
        assertEquals("a", sortedIt.next());
        sortedIt.remove();
        assertEquals("b", sortedIt.next());
        assertEquals("c", sortedIt.next());
        assertEquals("z", sortedIt.next());
        assertFalse(sortedIt.hasNext());
        assertTrue(sortedIt.hasPrevious());
        assertEquals("z", sortedIt.previous());
        assertEquals("c", sortedIt.previous());
        sortedIt.remove();
        assertEquals("b", sortedIt.previous());
        assertEquals("a", sortedIt.previous());
        assertFalse(sortedIt.hasPrevious());
        
        sortedIt.remove();
        assertTrue(sortedIt.hasNext());
        assertEquals("b", sortedIt.next());
        assertEquals("z", sortedIt.next());
        assertFalse(sortedIt.hasNext());
        
        sortedIt.remove();
        assertTrue(sortedIt.hasPrevious());
        assertEquals("b", sortedIt.previous());
        assertFalse(sortedIt.hasPrevious());
        
        sortedIt.add("a");
        assertTrue(sortedIt.hasNext());
        assertEquals("a", sortedIt.next());
        assertEquals("b", sortedIt.next());
        assertFalse(sortedIt.hasNext());
        
        assertTrue(sortedIt.hasPrevious());
        assertEquals("b", sortedIt.previous());
        assertEquals("a", sortedIt.previous());
        assertFalse(sortedIt.hasPrevious());
        
        sortedIt.remove();
        assertTrue(sortedIt.hasNext());
        assertEquals("b", sortedIt.next());
        assertFalse(sortedIt.hasNext());
    }
    
    @Test
    public void testSublist()
    {
        SortedArrayList<String> s = makeInstance();
        
        s.addAll(Arrays.asList("m", "o", "n", "p", "l"));
        
        SortedArrayList<String> l = (SortedArrayList<String>) s.subList(1, 4);
        
        assertEquals("n", l.getSorted(0));
        assertEquals("n", l.get(1));
        assertEquals("n", s.get(2));
        assertEquals("o", l.getSorted(1));
        assertEquals("o", l.get(0));
        assertEquals("o", s.get(1));
        assertEquals("p", l.getSorted(2));
        assertEquals("p", l.get(2));
        assertEquals("p", s.get(3));
        assertEquals(3, l.size());
        assertEquals(5, s.size());
    }
    
    @Test
    public void testRetainAll()
    {
        SortedArrayList<String> s = makeInstance();
        
        s.addAll(Arrays.asList("f", "b", "a", "c", "d", "e"));
        
        s.retainAll(Arrays.asList("b", "d", "f"));
        
        assertEquals(Arrays.asList("f", "b", "d"), s);
        
        String[] expected = { "b", "d", "f" };
        int[] i = { 0 };
        s.forEachSorted((String str) -> {
            assertEquals(expected[i[0]], str);
            ++i[0];
        });
        assertEquals(3, i[0]);
    }

    @Test
    public void testGetSortedIndex()
    {
        SortedArrayList<String> s = makeInstance();
        
        s.addAll(Arrays.asList("c", "b", "a"));
        
        s.sort();
        
        assertEquals(2, s.sortedIndexOf("c"));
        assertEquals(1, s.sortedIndexOf("b"));
        assertEquals(0, s.sortedIndexOf("a"));
    }
    
    @Test
    public void testSetIndex()
    {
        SortedArrayList<String> s = makeInstance();
        
        s.addAll(Arrays.asList("m", "n","o"));
        
        assertEquals("m", s.get(0));
        assertEquals("m", s.getSorted(0));
        
        s.set(0, "p");
        
        assertEquals("p", s.get(0));
        assertEquals("p", s.getSorted(2));
        assertEquals("n", s.get(1));
        assertEquals("n", s.getSorted(0));
        assertEquals("o", s.get(2));
        assertEquals("o", s.getSorted(1));
        
        s.set(2, "m");
        
        assertEquals("m", s.get(2));
        assertEquals("m", s.getSorted(0));
        assertEquals("p", s.get(0));
        assertEquals("p", s.getSorted(2));
        assertEquals("n", s.get(1));
        assertEquals("n", s.getSorted(1));
        
        s.set(1, "l");
        
        assertEquals("l", s.get(1));
        assertEquals("l", s.getSorted(0));
        assertEquals("m", s.get(2));
        assertEquals("m", s.getSorted(1));
        assertEquals("p", s.get(0));
        assertEquals("p", s.getSorted(2));
        
        s.set(1, "n");
        
        assertEquals("m", s.get(2));
        assertEquals("m", s.getSorted(0));
        assertEquals("p", s.get(0));
        assertEquals("p", s.getSorted(2));
        assertEquals("n", s.get(1));
        assertEquals("n", s.getSorted(1));
        
        s.set(1, "q");
        
        assertEquals("m", s.get(2));
        assertEquals("m", s.getSorted(0));
        assertEquals("p", s.get(0));
        assertEquals("p", s.getSorted(1));
        assertEquals("q", s.get(1));
        assertEquals("q", s.getSorted(2));
    }
    
    @Test
    public void testForceSort()
    {
        class TestExample implements Comparable<String>
        {
            public String s;
            public TestExample(String s)
            {
                this.s = s;
            }

            @Override
            public int compareTo(String arg0)
            {
                return this.s.compareTo(arg0);
            }
        }
        SortedArrayList<TestExample> s = new SortedArrayList<TestExample>((TestExample lhs, TestExample rhs) -> {
           return lhs.compareTo(rhs.s); 
        });

        s.addAll(
            Arrays.asList(
                    new TestExample("c"),
                    new TestExample("b"),
                    new TestExample("a")
            )
        );

        TestExample[] expected = { 
            new TestExample("a"),
            new TestExample("b"),
            new TestExample("c")
        };

        Integer[] i = { 0 };
        s.forEachSorted((TestExample e) -> {
            assertEquals(expected[i[0]].s, e.s);
            ++i[0];
        });
        assertEquals((Integer) 3, i[0]);
        
        
        i[0] = 0;
        s.get(0).s = "a";
        expected[1].s = "a";
        expected[2].s = "b";
        
        assertEquals(expected[0].s, s.get(0).s);
        for (int _i = 1; _i < s.size(); ++_i)
        {
            assertNotEquals(expected[_i].s, s.get(_i).s);
        }

        s.sort();
        
        s.forEachSorted((TestExample e) -> {
            assertEquals(expected[i[0]].s, e.s);
            ++i[0];
        });
        assertEquals((Integer) 3, i[0]);
    }

}
