package util;

@FunctionalInterface
public interface TriConsumer<T1, T2, T3>
{
    public abstract void accept(T1 p1, T2 p2, T3 p3);
}
