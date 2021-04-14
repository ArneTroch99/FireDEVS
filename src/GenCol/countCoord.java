/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */


package GenCol;


class countCoord extends Thread{
private int count,allCount;
private coordTimer t;
boolean One = false;

public countCoord(int n){
allCount = n;
}

public countCoord(int n,int all){
count = n;
allCount = all;
One = true;
}

public void setTimer(coordTimer T){
t = T;
}

public void runOne(){
long ts = System.currentTimeMillis();
while(allCount >  0 && count >0){
waitBoth();
}
//
ts = System.currentTimeMillis() - ts;
//System.out.println("processing took "+ts);
if (t != null)
    t.interrupt();
}

public void run(){
if (One)runOne();
//long ts = System.currentTimeMillis();
while(allCount >  0)
  waitForAll();
//ts = System.currentTimeMillis() - ts;
//System.out.println("processing took "+ts);
if (t != null)
    t.interrupt();
}

public  synchronized void waitBoth(){
try{
wait();
   }
catch (InterruptedException e)
  {
  System.out.println("time allowed has elapsed");
  allCount = 0;
  count = 0;
  }
//System.out.println("ensemble method is finished");
notify();
}

public  synchronized void waitForAll(){
try{
wait();
   }
catch (InterruptedException e)
  {
  System.out.println("time allowed has elapsed");
  allCount = 0;
  }
//}
//System.out.println("ensemble method is finished");
count = 0;
notify();
}

public  synchronized void waitForN(){
try{
wait();
   }
catch (InterruptedException e)
  {
  System.out.println("time allowed has elapsed");
  count = 0;
  }
//}
//System.out.println("ensemble method is finished");
allCount = 0;
notify();
}

public synchronized  void allDecrement(){
allCount--;
notify();
}


public synchronized  void decrement(){
count--;
notify();
}
}


