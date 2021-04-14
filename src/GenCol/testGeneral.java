/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;


import java.util.*;
import java.lang.reflect.*;

public class testGeneral{
protected Class testClass;
protected String description;
protected Boolean precondition;

public testGeneral(String TC){
testClass = getTheClass(TC);
}


public static Class getTheClass(String classnm){
Class str = null;
try{
     str = Class.forName(classnm);
}
catch(Exception e1) {
System.out.println(e1);
    }
return str;
}

public String getDescription(){
return description;
}

public Boolean getPrecondition(){
return precondition;
}

/*********************************************
* test methods  of testCLass must satisfy:
*  names must start with "test"
*  must return boolean
**********************************************/

public void applyTests(String testFocus){
//
HashSet notTested = new HashSet();
//
HashSet Failed = new HashSet();
System.out.println("================ testing "+testFocus);
Method[] methods = testClass.getMethods();
Object[]  args = {};
/*
ensembleBag li = new ensembleBag();
for (int i=0;i<methods.length;i++)
li.add(new extMethod(this,methods[i]));
Class [] classes = {};
//li.tellAll("nullSelf",classes,args);     //unresolved problem:
//li.tellAll("analyze",classes,args);       //java.lang exception, invocation target
*/

for (int i=0;i<methods.length;i++){
  if (methods[i].getName().startsWith("test")) {
  Boolean result = null;
  try{
      Object o = methods[i].invoke(this,args);
      result = (Boolean)o;
    }
   catch(Exception e1) {
   System.out.println(e1 + " in " +methods[i].getName()+" :" +this.description );
   notTested.add(methods[i].getName());
   }
   if (result != null){
   if (this.precondition.equals(Boolean.FALSE))
        notTested.add(methods[i].getName());
      else  if (result.equals(Boolean.FALSE))
           Failed.add(methods[i].getName());
    }
 }
}


//System.out.println("the following tests failed:"+Failed.toString());
//System.out.println("the following were not tested: " + notTested.toString());
//System.out.println("the following tests failed:"+extMethod.Failed.toString());
//System.out.println("the following were not tested: "
//      +extMethod.notTested.toString());
}

 public static void main (String[ ] args){

HashSet s = new HashSet();
testCollection t = new testCollection(s);
t.applyTests("HashSet");
s.add(new Object());  //test non empty
t.applyTests("HashSet");
Vector v = new Vector();
t = new testCollection(v);
t.applyTests("Vector");
v.add(new Object());  //test non empty
t.applyTests("Vector");
 }
}


class  extMethod{ // inherit in Method not allowed extends Method{
private Method m;
private static testGeneral target;
public static HashSet notTested, Failed;

public extMethod(testGeneral t,Method M){
m = M;
target = t;
notTested = new HashSet();
Failed = new HashSet();
}


public void analyze(){
if (m == null)return;
Boolean result = null;
try{
Object[]  args = {};
result  = (Boolean)m.invoke(target,args);
   }
catch(Exception e1) {
System.out.println(e1);
}
if (target.getPrecondition().equals(Boolean.FALSE))
        notTested.add(m.getName());
      else if (result.equals(Boolean.FALSE))
           Failed.add(m.getName()+" :" +target.getDescription());
}


public void nullSelf(){
if (!m.getName().startsWith("test"))
m = null;
}
}

