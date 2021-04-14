/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package util;

import java.lang.*;
import java.util.*;

import GenCol.*;





public class rand extends entity{

Random ran;

public rand(long seed){
  super();
  ran = new Random((long)seed);
}


public  double uniform(double lo ,double hi)
{
  return  ran.nextDouble() * (hi - lo) + lo;

}

public double uniform(double hi)
{
  return uniform(0,hi);
}

public int iuniform(int lo, int hi)
{
  return (int)Math.rint(uniform(lo,hi));
      //rint = round to integral value (double)
}

public int iuniform(int hi)
{
  return iuniform(0,hi);
}

public  double expon(double mean){
  return -mean*Math.log(uniform(0,1));
}

public  double normal1(){
  double u = uniform(0,1);
  return Math.sqrt(-2*Math.log(u))*Math.cos(2*Math.PI*u);
}


public double normal(double mean,double sig){
   double save = mean + sig*normal1();
 // if (save < 0)System.out.println("Warning; negative normal variate");
   return save;
}

public double posNormal(double mean,double sig){
   double save = normal(mean,sig);
return save < 0?save:0;
}

public double pareto( double location, double shape ){
	double u = uniform(0,1);
	return location * Math.pow( u, (-1/shape) );
}

public double lognormal( double mean, double sig ){
	return Math.pow( 10, normal( mean, sig ) );
}
}