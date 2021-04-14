package util.classUtils;

import java.lang.reflect.Field;

import model.modeling.atomic;
import model.modeling.state;
import util.tracking.TrackingType;

/**
 * Wrapper class for java.lang.reflect.Field, which is the generic type for
 * class attributes. Simplifies reflection API to perform DEVS-Suite-specific
 * tasks on ViewableAtomic child classes. Contains a set of more intuitive
 * methods for<br>
 * &emsp;1)&emsp;Getting the value (Object) of an arbitrary class attribute.<br>
 * &emsp;2)&emsp;Checking whether it is annotated with a
 * model.modeling.state<br>
 * &emsp;&emsp;&nbsp;&nbsp;&nbsp;annotation (signifying whether it should be
 * tracked by default<br>
 * &emsp;&emsp;&nbsp;&nbsp;&nbsp;if tracking is enabled).<br>
 * &emsp;3)&emsp;Getting the name of the class attribute.<br>
 * &emsp;4)&emsp;Getting the type (Class<?>) of the class attribute.<br>
 * &emsp;5)&emsp;Determining whether a Field is equal to another object.<br>
 * 
 * @see #DevsClassField(Field)
 * @see #getObject()
 * @see #getObjectName()
 * @see #getObjectType()
 * @see #isState()
 */
public class DevsClassField
{
    private Field devsField;
    private atomic instance;
    
    /**
     * Constructor for DevsClassField objects
     * 
     * @param Field
     *            the {@link java.lang.reflect.Field} object to wrap
     * @return <b>DevsClassField</b> the wrapper class for the
     *         java.lang.reflect.Field object
     */
    public DevsClassField(Field devsField)
    {
        this.devsField = devsField;
    }
    
    public void setInstance(atomic instance)
    {
        this.instance = instance;
    }

    /**
     * Retrieves the object referenced by the java.lang.reflect.Field object
     * wrapped in this object. For more information about exceptions thrown by
     * this method, refer to the links under <b>See Also</b>.
     * 
     * @see java.lang.reflect.Field#get(Object)
     * @return The value of the object referenced by the java.lang.reflect.Field
     *         object
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public Object getObject() throws IllegalArgumentException, IllegalAccessException
    {
        if (this.instance == null)
        {
            return devsField.get(null);
        }
        devsField.setAccessible(true);
        return devsField.get(instance);
    }

    /**
     * Determines whether the class attribute wrapped by this instance has been
     * annotated with the <b>state</b> annotation.
     * 
     * @return true if the class attribute was annotated with the <b>state</b>
     *         annotation
     */
    public boolean isState()
    {
        return devsField.getAnnotation(state.class) != null;
    }
    
    /**
     * Determines whether the class attribute wrapped by this instance has been
     * annotated with the <b>state</b> annotation, with the option of being
     * checked by default in the configuration UI.
     * 
     * @return true if the class attribute was annotated with the <b>state</b>
     *         annotation and set to be checked by default
     */
    public boolean isDefaultCheckedFor(TrackingType setting)
    {
        state p = devsField.getAnnotation(state.class);
        if (p != null)
        {
            if (setting == TrackingType.STACK)
            {
                return p.time_view_stack();
            }
            else if (setting == TrackingType.NO_STACK)
            {
                return p.time_view_no_stack();
            }
            else if (setting == TrackingType.SEPARATE)
            {
                return p.time_view_separate();
            }
            else if (setting == TrackingType.LOG)
            {
                return p.log();
            }
            else if (setting == TrackingType.DB)
            {
                return p.db();
            }
        }
        return false;
    }

    /**
     * Wrapper method for {@link java.lang.reflect.Field#getName()}.
     * 
     * @return the name of the class attribute wrapped in this class
     */
    public String getObjectName()
    {
        return devsField.getName();
    }

    /**
     * 
     * Wrapper method for {@link java.lang.reflect.Field#getType()}.
     * 
     * @return the Class object for the class attribute wrapped by this instance
     */
    public Class<?> getObjectType()
    {
        return devsField.getType();
    }

    @Override
    public boolean equals(Object _rhs)
    {
        try
        {
            DevsClassField rhs = (DevsClassField) _rhs;
            return devsField.equals(rhs.devsField);
        }
        catch (ClassCastException e)
        {
            return false;
        }
    }
}
