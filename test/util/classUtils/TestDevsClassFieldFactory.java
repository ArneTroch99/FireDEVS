package util.classUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import org.junit.jupiter.api.Test;

import facade.modeling.FAtomicModelTestArtifacts.FakeViewableAtomic;

public class TestDevsClassFieldFactory
{
    private ArrayList<Field> getFields()
    {
        ArrayList<Field> _fields = new ArrayList<Field>();

        Class<?> cl = FakeViewableAtomic.class;

        Field[] fields = cl.getDeclaredFields();
        for (Field f : fields)
        {
            if (f.getDeclaringClass().equals(cl))
            {
                _fields.add(f);
            }
        }

        return _fields;
    }

    @Test
    public void testCreateDevsClassFields()
    {
        ArrayList<Field> fields = getFields();
        fields.sort(new Comparator<Field>() {

            @Override
            public int compare(Field f1, Field f2)
            {
                return f1.getName().compareTo(f2.getName());
            }

        });
        ArrayList<DevsClassField> actual = DevsClassFieldFactory.createDevsClassFields(FakeViewableAtomic.class);

        assertTrue(fields.size() == 5);
        for (int i = 0; i < fields.size(); ++i)
        {
            assertEquals(new DevsClassField(fields.get(i)), actual.get(i));
        }
    }

    @Test
    public void testReadModelStates()
    {
        ArrayList<Field> fields = getFields();

        HashMap<String, DevsClassField> expected = new HashMap<String, DevsClassField>();
        expected.put("stringList", new DevsClassField(fields.get(0)));
        expected.put("stringList_defaultChecked", new DevsClassField(fields.get(1)));

        HashMap<String, DevsClassField> actual = null;
        try
        {
            actual = DevsClassFieldFactory.createDevsClassFieldMap(FakeViewableAtomic.class);

            Set<String> keys = expected.keySet();
            assertTrue(actual.keySet().size() > 2);

            for (String key : keys)
            {
                assertEquals(expected.get(key), actual.get(key));
            }
        }
        catch (IllegalArgumentException e)
        {
            fail("Exception occurred while loading class!");
        }
    }
    
    @Test
    public void testReadModelStatesWithFilter()
    {
        ArrayList<Field> fields = getFields();

        HashMap<String, DevsClassField> expected = new HashMap<String, DevsClassField>();
        expected.put("stringList", new DevsClassField(fields.get(0)));
        expected.put("stringList_defaultChecked", new DevsClassField(fields.get(1)));

        HashMap<String, DevsClassField> actual = null;
        try
        {
            actual = DevsClassFieldFactory.createDevsClassFieldMapWithFilter(
                FakeViewableAtomic.class,
                null,
                (DevsClassField f) -> {
                    return f.isState();
                }
            );

            Set<String> keys = expected.keySet();
            assertEquals(2, actual.keySet().size());

            for (String key : keys)
            {
                assertEquals(expected.get(key), actual.get(key));
            }
        }
        catch (IllegalArgumentException e)
        {
            fail("Exception occurred while loading class!");
        }
    }
}
