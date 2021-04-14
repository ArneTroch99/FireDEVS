package model.modeling;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to be used within {@link view.modeling.ViewableAtomic} child class
 * definitions on class attributes that are state variables for the DEVS model.
 * Read by {@link util.classUtils.DevsClassField} instances that serve as
 * wrappers for {@link java.lang.reflect.Field} objects.
 * 
 * @see model.modeling.state#DEFAULT_CHECKED
 * @see model.modeling.state#time_view_stack()
 * @see model.modeling.state#time_view_no_stack()
 * @see model.modeling.state#time_view_separate()
 * @see model.modeling.state#log()
 * @see model.modeling.state#db()
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface state
{
    /**
     * Indicates that a tracking type should be checked in the<br>
     * configuration UI by default.
     */
    public static final boolean DEFAULT_CHECKED = true;
    /**
     * Indicates that Time View UI tracking should be checked by default.
     * Enables plot stacking - plots will be stacked on top of each other in the same window or tab.
     * @return true if class attribute has been annotated with<br>
     *         <code>@state(time_view_stack=state.DEFAULT_CHECKED)</code>
     */
    public boolean time_view_stack() default false;
    /**
     * Indicates that <b>no_stack</b> Time View UI tracking should be checked by default.
     * Disables plot stacking - plots will be in their own windows or tabs. 
     * @return true if class attribute has been annotated with<br>
     *         <code>@state(no_stack=state.DEFAULT_CHECKED)</code>
     */
    public boolean time_view_no_stack() default false;
    /**
     * Indicates that <b>separate</b> Time View UI tracking should be checked by default.
     * "Separate" Time View UI tracking refers to having time view plots in a separate
     * window instead of tabs underneath the Sim View.
     * @return true if class attribute has been annotated with<br>
     *         <code>@state(no_stack=state.DEFAULT_CHECKED)</code>
     */
    public boolean time_view_separate() default false;
    /**
     * Indicates that Log UI tracking should be checked by default<br>
     * @return true if class attribute has been annotated with<br>
     *         <code>@state(log=state.DEFAULT_CHECKED)</code>
     */
    public boolean log() default false;
    /**
     * Indicates that database tracking should be checked by default<br>
     * @return true if class attribute has been annotated with<br>
     *         <code>@state(db=state.DEFAULT_CHECKED)</code>
     */
    public boolean db() default false;
}
