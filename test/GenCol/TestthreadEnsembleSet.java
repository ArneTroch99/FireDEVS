package GenCol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestthreadEnsembleSet
{
    protected threadEnsembleSet<EntityInterface> b;
    public entity e;
    public entity f;
    public entity g;

    public TestthreadEnsembleSet()
    {
        e = new entity("e");
        f = new entity("f");
        g = new entity("g");
    }

    @BeforeEach
    public void beforeOnce()
    {
        b = new threadEnsembleSet<EntityInterface>();
    }

    @Test
    public void testAddAll()
    {
        assertTrue(b.isEmpty());
 
        b.add(e);
        b.add(e);
        b.add(f);
        ensembleBag<EntityInterface> c = new ensembleBag<EntityInterface>();
        c.addAll(b);
        
        assertEquals(2, c.size());
    }
    
    @Test
    public void testWhichSize()
    {
        assertTrue(!b.contains(g));
        
        b.add(e);
        b.add(f);
        b.add(g);
        ensembleBag<EntityInterface> c = new ensembleBag<EntityInterface>();
        Class<?>[] classes = { String.class };
        Object[] args = { "g" };
        b.which(c, "equalName", classes, args);
        
        assertEquals(1, c.size());
    }
    
    @Test
    public void testWhichOne()
    {
        b.add(e);
        b.add(e);
        b.add(f);
        b.add(g);
        Class<?>[] classes = { String.class };
        Object[] args = { "e" };
        Object r = b.whichOne("equalName", classes, args);
        
        assertEquals("e", ((entity) r).getName());
    }
    
    @Test
    public void testWhichNone()
    {
        Class<?>[] classes = { String.class };
        Object[] args = { "ej" };
        Object r = b.whichOne("equalName", classes, args);
        assertNull(r);
    }

}
