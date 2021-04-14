package util.classUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import facade.modeling.FAtomicModelTestArtifacts.FakeViewableAtomic;
import util.tracking.TrackingType;

public class TestDevsClassField
{
    private void addEntry(String s)
    {
        FakeViewableAtomic.stringList.add(s);
    }
    
    private Field[] getFields()
    {
        return FakeViewableAtomic.class.getFields();
    }
    
    @AfterEach
    public void tearDown()
    {
        FakeViewableAtomic.stringList.clear();
    }
    
    @Test
    public void testIsState()
    {
        Field[] fields = getFields();
        
        DevsClassField cut = new DevsClassField(fields[0]);
        
        assertTrue(cut.isState());
        
        cut = new DevsClassField(fields[1]);
        
        assertTrue(cut.isState());
        
        cut = new DevsClassField(fields[2]);
        
        assertFalse(cut.isState());
    }
    
    @Test
    public void testIsDefaultChecked()
    {
        Field[] fields = getFields();
        
        DevsClassField cut = new DevsClassField(fields[0]);
        
        assertFalse(cut.isDefaultCheckedFor(TrackingType.NO_STACK));
        assertFalse(cut.isDefaultCheckedFor(TrackingType.STACK));
        assertFalse(cut.isDefaultCheckedFor(TrackingType.LOG));
        assertFalse(cut.isDefaultCheckedFor(TrackingType.DB));
        cut = new DevsClassField(fields[1]);
        
        assertFalse(cut.isDefaultCheckedFor(TrackingType.NO_STACK));
        assertFalse(cut.isDefaultCheckedFor(TrackingType.STACK));
        assertTrue(cut.isDefaultCheckedFor(TrackingType.LOG));
        assertTrue(cut.isDefaultCheckedFor(TrackingType.DB));
        
        cut = new DevsClassField(fields[2]);
        
        assertFalse(cut.isDefaultCheckedFor(TrackingType.NO_STACK));
        assertFalse(cut.isDefaultCheckedFor(TrackingType.STACK));
        assertFalse(cut.isDefaultCheckedFor(TrackingType.LOG));
        assertFalse(cut.isDefaultCheckedFor(TrackingType.DB));
    }
    
    @Test
    public void testGetStateObject()
    {
        addEntry("Test Entry 1");

        Field[] fields = getFields();
        
        DevsClassField cut = new DevsClassField(fields[0]);
        
        ArrayList<String> expected = new ArrayList<String>();
        expected.add("Test Entry 1");
        
        try
        {
            @SuppressWarnings("unchecked")
            ArrayList<String> actual = (ArrayList<String>) cut.getObject();
            assertEquals(expected, actual);
        }
        catch(ClassCastException | IllegalArgumentException | IllegalAccessException e)
        {
            fail("Unexpected cast error!");
        }
    }
    
    @Test
    public void testGetNonStateObject()
    {
        Field[] fields = getFields();
        
        DevsClassField cut = new DevsClassField(fields[2]);
        
        Boolean expected = false;
        try
        {
            Boolean actual = (Boolean) cut.getObject();
            assertEquals(expected, actual);
        }
        catch(ClassCastException | IllegalArgumentException | IllegalAccessException e)
        {
            fail("Unexpected cast error!");
        }
    }
    
    @Test
    public void testGetStateObjectName()
    {
        Field[] fields = getFields();
        
        DevsClassField cut = new DevsClassField(fields[0]);
        
        String expected = "stringList";
        String actual = cut.getObjectName();
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void testGetNonStateObjectName()
    {
        Field[] fields = getFields();
        
        DevsClassField cut = new DevsClassField(fields[2]);
        
        String expected = "notState";
        String actual = cut.getObjectName();
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void testGetStateObjectType()
    {
        Field[] fields = getFields();
        
        DevsClassField cut = new DevsClassField(fields[0]);
        
        Class<?> expected = ArrayList.class;
        Class<?> actual = cut.getObjectType();
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void testGetNonStateObjectType()
    {
        Field[] fields = getFields();
        
        DevsClassField cut = new DevsClassField(fields[2]);
        
        Class<?> expected = boolean.class;
        Class<?> actual = cut.getObjectType();
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void testEquals()
    {
        Field[] fields = getFields();
        
        DevsClassField cut = new DevsClassField(fields[0]);
        
        DevsClassField rhs = new DevsClassField(fields[0]);
        
        assertTrue(cut.equals(rhs));
        
        rhs = new DevsClassField(fields[1]);
        
        assertFalse(cut.equals(rhs));
    }
}
