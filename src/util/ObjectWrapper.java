package util;

public class ObjectWrapper<T>
{
    private T wrappedValue;
    
    public ObjectWrapper(T wrappedValue)
    {
        set(wrappedValue);
    }
    
    public T get()
    {
        return wrappedValue;
    }
    
    public void set(T wrappedValue)
    {
        this.wrappedValue = wrappedValue;
    }
    
    @Override
    public boolean equals(Object o)
    {
        return this.wrappedValue.equals(o);
    }
}
