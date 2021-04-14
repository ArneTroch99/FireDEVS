package util.classUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import model.modeling.atomic;

/**
 * Factory class for {@link DevsClassField} objects. Both methods of this class
 * take a ViewableAtomic child class and return all class attributes specific to
 * the child either as a list or a map.
 * 
 * This class is a singleton and contains only static methods.
 * 
 * @see #createDevsClassFieldMap(Class)
 * @see #createDevsClassFields(Class)
 */
public class DevsClassFieldFactory
{
    private static final Function<DevsClassField, Boolean> NO_FILTER = (DevsClassField f) -> { return Boolean.TRUE; };
    private static final Consumer<DevsClassField> NO_OP = (DevsClassField f) -> { };
    /**
     * Reads a class using java reflection api and builds a list of<br>
     * {@link DevsClassField} objects that correspond with a attribute of that
     * class.
     * 
     * @param c
     *            class in which to read and create {@link DevsClassField}
     *            instances for each class attribute in <b>c</b>.
     * @return <code>ArrayList</code> of {@link DevsClassField} objects
     *         <b>sorted by name</b>
     */
    public static ArrayList<DevsClassField> createDevsClassFields(Class<?> c)
    {
        ArrayList<DevsClassField> devsFields = new ArrayList<DevsClassField>();
        
        applyToClassFields(c, (Field f) -> {
            devsFields.add(new DevsClassField(f));
        });

        devsFields.sort((DevsClassField arg0, DevsClassField arg1) -> {
            return arg0.getObjectName().compareTo(arg1.getObjectName());
        });

        return devsFields;
    }
    
    /**
     * Generic method to apply a function <code>f</code> to each field in a given class <code>c</code>
     * 
     * @param c the class with fields to apply to function <code>f</code>
     * @param f the function to apply to each field of class <code>c</code>
     */
    private static void applyToClassFields(Class<?> c, Consumer<Field> f)
    {
        Field[] fields = c.getDeclaredFields();

        for (Field field : fields)
        {
            if (field.getDeclaringClass().equals(c))
            {
                f.accept(field);
            }
        }
    }

    /**
     * Reads a class using java reflection api and builds a list of
     * {@link DevsClassField} objects, hashed by their names, and corresponding
     * with each attribute of the class.
     * 
     * @param c
     *            class in which to read and create {@link DevsClassField}
     *            instances for each class attribute in <b>c</b>.
     * @return <code>HashMap</code> of {@link DevsClassField} objects, hashed by
     *         their names
     */
    public static HashMap<String, DevsClassField> createDevsClassFieldMap(Class<?> c)
    {
        return createDevsClassFieldMapAndDo(c, NO_OP, NO_FILTER);
    }
    
    /**
     * Reads a class using java reflection api and builds a list of
     * {@link DevsClassField} objects, hashed by their names, and corresponding
     * with each attribute of the class.
     * 
     * @param c class in which to read and create {@link DevsClassField}
     *          instances for each class attribute in <b>c</b>.
     * @param instance of the class to which to refer when retrieving data

     * @return <code>HashMap</code> of {@link DevsClassField} objects, hashed by
     *         their names
     */
    public static HashMap<String, DevsClassField> createDevsClassFieldMap(Class<?> c, atomic instance)
    {
        return createDevsClassFieldMapAndDo(c, (DevsClassField f) -> {
            f.setInstance(instance);
        }, NO_FILTER);
    }
    
    /**
     * Reads a class using java reflection api and builds a list of
     * {@link DevsClassField} objects, hashed by their names, and corresponding
     * with each attribute of the class, filtered by the supplied function.
     * 
     * @param c class in which to read and create {@link DevsClassField}
     *          instances for each class attribute in <b>c</b>.
     * @param instance of the class to which to refer when retrieving data
     * @param filter to apply when populating the map

     * @return <code>HashMap</code> of {@link DevsClassField} objects, hashed by
     *         their names
     */
    public static HashMap<String, DevsClassField> createDevsClassFieldMapWithFilter(Class<?> c, atomic instance, Function<DevsClassField, Boolean> filter)
    {
        return createDevsClassFieldMapAndDo(c, (DevsClassField f) -> {
            f.setInstance(instance);
        }, filter);
    }
    
    /**
     * Generic method to construct <code>DevsClassField</code>s from fields in class <code>c</code>,
     * add each <code>DevsClassField</code> to a new HashMap,
     * and apply function <code>f</code> to each new <code>DevsClassField</code>
     *
     * @param c the class from which to read fields
     * @param f the function to apply to each newly constructed <code>DevsClassField</code>
     * @return new <code>HashMap</code> state names mapped to <code>DevsClassField</code> objects
     */
    private static HashMap<String, DevsClassField> createDevsClassFieldMapAndDo(Class<?> c, Consumer<DevsClassField> f, Function<DevsClassField, Boolean> filter)
    {
        HashMap<String, DevsClassField> states = new HashMap<String, DevsClassField>();

        applyToClassFields(c, (Field field) -> {
            DevsClassField devsField = new DevsClassField(field);
            if (filter.apply(devsField).equals(Boolean.TRUE))
            {
                states.put(devsField.getObjectName(), devsField);
                f.accept(devsField);
            }
        });
        
        return states;
    }

    /**
     * Factory static/singleton class, no instances allowed.
     */
    private DevsClassFieldFactory()
    {
        // Factory static/singleton class; no instances allowed.
    }
}
