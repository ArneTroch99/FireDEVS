package GenCol;

public class wrapObject<T>
{
    public T kernel;

    public Boolean isContained(ensembleBag<T> e)
    {
        return new Boolean(e.contains(kernel));
    }

    public synchronized void addSelf(ensembleBag<T> e)
    {
        e.add(kernel);
    }

    public synchronized void removeSelf(ensembleBag<T> e)
    {
        e.remove(kernel);
    }

    public synchronized void removeSelf(ensembleBag<T> source, ensembleBag<T> criterion)
    {
        if (!criterion.contains(kernel))
        {
            source.remove(kernel);
        }
    }

    public void print()
    {
        System.out.println(kernel.toString());
    }
}
