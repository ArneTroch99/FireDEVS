package util;

import java.util.concurrent.Semaphore;

public class NestLock
{
    private Semaphore entry_lock;
    private Semaphore exit_lock;
    
    public NestLock()
    {
        entry_lock = new Semaphore(0);
        exit_lock = new Semaphore(0);
    }
    
    // The innerSectionEnter() blocks until the outer thread calls enter(). 
    // The outer thread blocks in enter() until the inner thread 
    // calls innerSectionExit().  
    
    public void innerSectionEnter() throws InterruptedException
    {
        entry_lock.acquire();
    }
    
    public void innerSectionExit()
    {
        exit_lock.release();
    }
    
    public void enter() throws InterruptedException
    {   
        entry_lock.release();
        exit_lock.acquire();
    }
    
    public void leave() throws InterruptedException
    {
        innerSectionExit();  
        innerSectionEnter();
    }
}
