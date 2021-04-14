
package model.modeling;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

import view.modeling.ViewableComponent;

/**
 * Provides a flavor for constructing a DEVS model. This
 * annotation is to be used with a static method of type
 *    +():ViewableComponent
 *    
 * 
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface initializer
{
    public String displayName() default "";
}
