package util;

import java.util.Optional;

public class OptionalWrapper<T> extends ObjectWrapper<Optional<T>>
{
    private Class<T> c;
    public OptionalWrapper(Optional<T> obj, Class<T> c)
    {
        super(obj);
        this.c = c;
    }
    
    public Class<T> getGenericClass()
    {
        return c;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof OptionalWrapper<?>)
        {
            OptionalWrapper<?> optionalWrapper = (OptionalWrapper<?>)o;
            return getGenericClass().equals(optionalWrapper.getGenericClass()) && super.equals(optionalWrapper.get());
        }
        else
        {
            return false;
        }
    }
    
    public static <T> OptionalWrapper<T> empty(Class<T> c)
    {
        return new OptionalWrapper<T>(Optional.empty(), c);
    }
}
