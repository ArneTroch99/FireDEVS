package model.modeling;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import view.modeling.ViewableComponent;

@Retention(RUNTIME)
@Target(METHOD)
public @interface initializerFactory
{
    public class DynamicInit
    {
        public interface InitFunc
        {
            public ViewableComponent build();
        }
        
        final private String display_name;
        final private InitFunc init_func;
        
        public DynamicInit(String display_name, InitFunc init_func)
        {
            this.display_name = display_name;
            this.init_func = init_func;
        }
        
        public String getDisplayName()
        {
            return display_name;
        }
        
        public InitFunc getInitFunc()
        {
            return init_func;
        }
    }
}
