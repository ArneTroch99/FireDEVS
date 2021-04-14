package GenCol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class threadEnsemble<T> extends ensemble<T>
{
    public static <T> ensemble<T> make(Collection<T> c)
    {
        return new threadEnsemble<T>(c);
    }

    private threadEnsemble(Collection<T> col)
    {
        super(col);
    }

    @Override
    public void tellAll(String MethodNm, Class<?>[] classes, Object[] args)
    {
//        countCoord c = new countCoord(col.size());
//        c.start();
//        coordTimer t = new coordTimer(c);
//        t.start();
//        c.setTimer(t);
        
        List<Thread> threads = new ArrayList<Thread>();
        
        col.forEach((T o) -> {
            Thread t = holder.makeThread(o, MethodNm, classes, args);
            threads.add(t);
            t.start();
        });

        threads.forEach((Thread t) -> {
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    @Override
    public void AskAll(ensembleInterface<T> result, String MethodNm, Class<?>[] classes, Object[] args)
    {
//        countCoord c = new countCoord(col.size());
//        c.start();
//        coordTimer t = new coordTimer(c);
//        t.start();
//        c.setTimer(t);
        
        List<Thread> threads = new ArrayList<Thread>();
        
        col.forEach((T o) -> {
            Thread t = holder.makeThread(result, o, MethodNm, classes, args);
            threads.add(t);
            t.start();
        });

        threads.forEach((Thread t) -> {
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    @Override
    public T whichOne(String MethodNm, Class<?>[] classes, Object[] args)
    {
        ensembleBag<T> result = new ensembleBag<T>();
//        countCoord c = new countCoord(1, col.size());
//        c.start();
//        coordTimer t = new coordTimer(c);
//        t.start();
//        c.setTimer(t);

        List<Thread> threads = new ArrayList<Thread>();
        
        col.forEach((T o) -> {
            Thread t = holder.makeThread(result, o, MethodNm, classes, args);
            threads.add(t);
            t.start();
        });

        threads.forEach((Thread t) -> {
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

        Iterator<T> itr = result.iterator();
        return itr.hasNext() ? itr.next() : null;
    }
}