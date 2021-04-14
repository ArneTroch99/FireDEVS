/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package GenCol;

class coordTimer extends Thread{
Thread c;

public coordTimer (Thread C){
c = C;
}

public void run(){
while(c.isAlive()){
  try
        {
          Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
        //System.out.println(e);
        }
}
c.interrupt();
}
}