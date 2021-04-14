package util.classUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import view.View;

import org.junit.jupiter.api.Test;

import facade.modeling.FAtomicModelTestArtifacts.FakeViewableAtomic;

public class TestDevsClassFileReader
{
    @Test
    public void testReadClass()
    {
        Class<?> expected = FakeViewableAtomic.class;
        try
        {
            Class<?> actual = DevsClassFileReader.readClass("facade.modeling.FAtomicModelTestArtifacts",
                                                            "FakeViewableAtomic");
            
            assertEquals(expected, actual);
        }
        catch (ClassNotFoundException e)
        {
            fail("ClassNotFoundException not expected!");
        }
    }
    
    @Test
    public void testReadExternalClass()
    {
        Class<?> expected = View.class;
        
        try
        {
            Class<?> actual = DevsClassFileReader.readClass("view", "View");
            
            assertEquals(expected, actual);
        }
        catch (ClassNotFoundException e)
        {
            fail("ClassNotFoundException not expected!");
        }
    }
    
    @Test
    public void testReadClassWithoutPackage()
    {        
        try
        {
            DevsClassFileReader.readClass("", "TestDevsClassFileReader");
            fail("ClassNotFoundException expected!");
        }
        catch (ClassNotFoundException e)
        {
            assertEquals("Class not found: TestDevsClassFileReader", e.getMessage());
        }
    }
    
    @Test 
    public void testReadClassWithNullPkg()
    {
        try
        {
            DevsClassFileReader.readClass(null, "TestDevsClassFileReader");
            fail("ClassNotFoundException expected!");
        }
        catch (ClassNotFoundException e)
        {
            assertEquals("Class not found: TestDevsClassFileReader", e.getMessage());
        }
    }
    
    
    @Test
    public void testReadClassByQualifiedName()
    {
        Class<?> expected = FakeViewableAtomic.class;
        try
        {
            Class<?> actual = 
                    DevsClassFileReader.readClass("facade.modeling.FAtomicModelTestArtifacts.FakeViewableAtomic");
            
            assertEquals(expected, actual);
        }
        catch (ClassNotFoundException e)
        {
            fail("ClassNotFoundException not expected!");
        }
    }
    
    @Test
    public void testReadClassByQualifiedNameWithoutPackage()
    {
        try
        {
            DevsClassFileReader.readClass("TestDevsClassFileReader");
            
            fail("ClassNotFoundException expected!");
        }
        catch (ClassNotFoundException e)
        {
            assertEquals("Class not found: TestDevsClassFileReader", e.getMessage());
        }
    }

    @Test
    public void testReadClassByQualifiedNameException()
    {        
        try
        {
            DevsClassFileReader.readClass("");
            
            fail("Expected ClassNotFoundException!");
        }
        catch(ClassNotFoundException e)
        {
            assertEquals("Class not found: ", e.getMessage());
        }
    }
    
    @Test
    public void testReadClassByFullyQualfiedNull()
    {
        try
        {
            DevsClassFileReader.readClass(null);
            
            fail("Expected NullPointerException!");
        }
        catch(NullPointerException e)
        {
            assertNull(e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            fail("Unexpected ClassNotFoundException!");
        }
    }
}
