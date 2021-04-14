package util;

import java.util.Enumeration;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class EnumerationUtils
{
    public static final Boolean CONTINUE = Boolean.TRUE;
    public static final Boolean STOP = Boolean.FALSE;
    
    public static <T> void enumerate(Enumeration<T> e, BiConsumer<Integer, T> c)
    {
        enumerate(e, (Integer i, T o) -> {
            c.accept(i, o);
            return CONTINUE;
        });
    }
    
    public static <T> void enumerate(Enumeration<T> e, BiFunction<Integer, T, Boolean> c)
    {
        Integer i = Integer.valueOf(0);
        Boolean willContinue = Boolean.TRUE;
        while(e.hasMoreElements() && willContinue.equals(Boolean.TRUE))
        {
            willContinue = c.apply(i, e.nextElement());
            ++i;
        }
    }
    
    public static <T> void forEach(Enumeration<T> e, Consumer<T> c)
    {
        while(e.hasMoreElements())
        {
            c.accept(e.nextElement());
        }
    }
}
