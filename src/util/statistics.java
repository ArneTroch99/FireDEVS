/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package util;
import GenCol.*;

public class statistics{

protected double numbers[];
protected Queue q;

public statistics (){
q = new Queue();
}

public statistics (double numbers[]){
this.numbers = numbers;
}

public int size(){
return numbers.length;
}

public void add(double n){
q.add(new doubleEnt(n));
}

public void toArray(){
numbers = new double[q.size()];
for (int i = 0;i<numbers.length;i++){
 numbers[i] = ((doubleEnt)q.get(i)).getv();

 }
 }

public String toString(double[] array){
String s = "{";
for (int i = 0;i<array.length-1;i++)
  s += array[i]+" ,";
  return s+array[array.length-1]+"}";
}

public double average(){
double sum = 0;
for (int i = 0;i<numbers.length;i++)
  sum += numbers[i];
return sum/numbers.length;
}

public double avgOfSquares(){
double sum = 0;
for (int i = 0;i<numbers.length;i++)
  sum += numbers[i]*numbers[i];
return sum/numbers.length;
}
public double variance(){
double avg = average();
return avgOfSquares() - avg*avg;
}

public double std(){
return Math.sqrt(variance());
}

public double[] partition(double begin,double end, int numClasses){
double size =  (end-begin)/numClasses;
if (size <= 0) {System.out.println("invalid class specification"); return null;}
double classes[] = new double[numClasses+2];
for (int i = 0;i<numbers.length;i++){
   for (int j = 0;j<=numClasses;j++){
       if (numbers[i] < begin + j*size){
           classes[j]++;
           break;
           }
        if (numbers[i] >= end)classes[numClasses+1]++;
       }
       }
 return classes;
 }

public static void main(String args[]){


double [] numArray = {1,2,3,4,5,6,7,8,9};
//statistics s = new statistics(numArray);

statistics s = new statistics();
s.add(1);
s.add(2);
s.add(3);
s.toArray();

System.out.println(s.average());
System.out.println(s.avgOfSquares());
System.out.println(s.variance());
System.out.println(s.std());
System.out.println("------------------------------");
s.add(4);
s.toArray();
System.out.println(s.average());
System.out.println("------------------------------");

rand r = new rand(1);

numArray = new double[300];
for (int i = 0;i<numArray.length;i++)
 numArray[i] = r.expon(1);

s = new statistics(numArray);
System.out.println(s.average());
System.out.println(s.avgOfSquares());
System.out.println(s.variance());
System.out.println(s.std());
System.out.println(s.toString(s.partition(0,5,5)));
System.out.println("------------------------------");

numArray = new double[300];
for (int i = 0;i<numArray.length;i++)
 numArray[i] = r.lognormal(0,.7);

s = new statistics(numArray);
System.out.println(s.average());
System.out.println(s.avgOfSquares());
System.out.println(s.variance());
System.out.println(s.std());
System.out.println(s.toString(s.partition(0,100,10)));
System.out.println("------------------------------");
numArray = new double[300];
for (int i = 0;i<numArray.length;i++)
 numArray[i] = r.pareto(10,2);

s = new statistics(numArray);
System.out.println(s.average());
System.out.println(s.avgOfSquares());
System.out.println(s.variance());
System.out.println(s.std());
System.out.println("------------------------------");

}

/**/
}