/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class holder<T>
{
    private T obj;
    private ensembleCollection<T> result;
    private String methodName;
    private Object[] args;
    private Method method;

    public static <T> Thread makeThread(T obj, String methodName, Class<?>[] classes, Object[] args)
    {
        return makeThread(null, obj, methodName, classes, args);
    }
    
    public static <T> Thread makeThread(ensembleCollection<T> result, T obj, String methodName, Class<?>[] classes, Object[] args)
    {
        return new Thread(new Runnable() {

            private holder<T> _holder = new holder<T>(result, obj, methodName, classes, args);

            @Override
            public void run()
            {
                _holder.execute();
            }
            
        });
    }
    
    public static <T> void wrap(ensembleCollection<Object> result, T O, Class<?> cl)
    {
        try
        {
            Object nw = cl.newInstance();
            wrapObject<T> w = (wrapObject<T>) nw;
            w.kernel = O;
            result.add(nw);
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
    
    public holder(T obj, String methodName, Class<?>[] classes, Object[] args)
    {
        this(null, obj, methodName, classes, args);
    }
    
    public holder(ensembleCollection<T> result, T obj, String methodName, Class<?>[] classes, Object[] args)
    {
        this.result = result;
        this.obj = obj;
        this.args = args;
        
        Class<?> c = obj.getClass();
        
        try
        {
            method = c.getMethod(methodName, classes);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            e.printStackTrace();
        }
    }
    
    public void execute()
    {
        try
        {
            Object out = method.invoke(obj, args);
            if (result != null && out != null)
            {
                result.add((T) out);
            }
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }
}
/*
public class holder<T>
{// extends Thread{ //threads accumulate in memory
    private T o;
    private Method m;
    private Class<?>[] classes;
    private Object[] args;
    private ensembleCollection<T> result;
    private countCoord coordinator;
    private boolean One = false;

    public holder(T O, String MethodNm, Class<?>[] Classes, Object[] Args)
    {
        o = O;
        classes = Classes;
        args = Args;
        Class<?> cl = o.getClass();
        result = null;
        coordinator = null;

        try
        {
            m = cl.getMethod(MethodNm, classes);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    public holder(ensembleCollection<T> Result, T O, String MethodNm, Class<?>[] Classes, Object[] Args)
    {
        this(O, MethodNm, Classes, Args);
        result = Result;
    }

    public holder(ensembleCollection<T> Result,
            T O,
            String MethodNm,
            Class<?>[] Classes,
            Object[] Args,
            countCoord Coordinator)
    {
        this(O, MethodNm, Classes, Args);
        result = Result;
        coordinator = Coordinator;
    }

    public holder(ensembleCollection<T> Result,
            T O,
            String MethodNm,
            Class<?>[] Classes,
            Object[] Args,
            countCoord Coordinator,
            boolean one)
    {
        this(O, MethodNm, Classes, Args);
        result = Result;
        coordinator = Coordinator;
        One = true;
    }

    public holder(T O, String MethodNm, Class<?>[] Classes, Object[] Args, countCoord Coordinator)
    {
        this(O, MethodNm, Classes, Args);
        coordinator = Coordinator;
    }

    public void runOne()
    {
        //
        if (!coordinator.isAlive())
        {
            return;
        }
        // to kill threads in whichOne
        // need to make sure coord has been started
        // so test result for non-empty

        try
        {
            Object out = m.invoke(o, args);
            if (out != null)
            {
                result.add((T) out);
                //
                coordinator.decrement();
                coordinator.allDecrement();
                return;
            }
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        //
        coordinator.allDecrement();
    }

    public void run()
    {
        if (One)
        {
            runOne();
        }
        else
        {
            try
            {
                Object out = m.invoke(o, args);
                if (result != null && out != null)
                {
                    result.add((T) out);
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }

            if (coordinator != null)
            {
                coordinator.allDecrement();
            }
        }
    }

    public holder(ensembleCollection<Object> e, T O, Class<?> cl)
    {
        try
        {
            Object nw = cl.newInstance();
            wrapObject<T> w = (wrapObject<T>) nw;
            w.kernel = O;
            e.add(nw);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

}
*/