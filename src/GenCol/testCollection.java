/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;


import java.util.*;


import java.lang.reflect.*;



class testCollection extends testGeneral{
protected Collection c;


public testCollection(Collection C){
super("GenCol.testCollection");
c = C;
}

public boolean testEmptyNew(){
description = "new collection must be empty";
precondition = new Boolean(c.size() == 0);
return c.isEmpty() == true;
}

public boolean testSizeNew(){
description = "new collection must  have 0 size";
precondition = new Boolean(c.isEmpty());
return c.size() == 0;
}

public boolean testSizeAdd(){
description = "add increases size by 1 if not already in";
Object e = new Object();
precondition = new Boolean(!c.contains(e));
int s = c.size();
c.add(e);
return c.size() == s+1;
}

public boolean testSizeAddAnother(){
description = "add increases size by 1 if already in";
Iterator it = c.iterator();
precondition = new Boolean(it.hasNext());
Object e = it.hasNext()? it.next():new Object();
int s = c.size();
c.add(e);
return c.size() == s+1;
}
public boolean testContainsAdd(){
description = "after adding an object,it is contained";
precondition = Boolean.TRUE;
Object e = new Object();
c.add(e);
return c.contains(e) == true;
}
public boolean testContainsAddDiff(){
description = "membership status is not affected by adding other";
precondition = Boolean.TRUE;;
Object e = new Object();
boolean before = c.contains(e);
Object a = new Object();
c.add(a);
boolean after = c.contains(e);
return before == after;
}
public boolean testSizeRemove(){
description = "remove decreases size by 1";
Iterator it = c.iterator();
precondition = new Boolean(it.hasNext());
Object e = it.hasNext()? it.next():new Object();
//precondition = new Boolean(c.contains(e));
//precondition = Boolean.TRUE;
int s = c.size();
c.remove(e);
return c.size() == s-1;
}

public static void main (String[ ] args){
/*          */
System.out.println("Testing remove");
HashSet s = new HashSet();
testCollection t = new testCollection(s);
boolean r = t.testSizeRemove();
System.out.println("In testing "+ t.description);
if (r)System.out.println("Test was satisfied");
 else System.out.println("Test was NOT satisfied");

System.out.println("Testing HashSet");
HashSet ss = new HashSet();
testCollection tt = new testCollection(ss);
 r = tt.testEmptyNew();
System.out.println("In testing "+ tt.description);
if (r)System.out.println("Test was satisfied");
 else System.out.println("Test was NOT satisfied");

 System.out.println("Testing Vector");
Vector v = new Vector();
t = new testCollection(v);
r = t.testEmptyNew();
System.out.println("In testing "+ t.description);
if (r)System.out.println("Test was satisfied");
 else System.out.println("Test was NOT satisfied");


HashSet sss = new HashSet();
testCollection ttt = new testCollection(sss);
ttt.applyTests("HashSet");
sss.add(new Object());  //test non empty
ttt.applyTests("HashSet");
v = new Vector();
t = new testCollection(v);
t.applyTests("Vector");
v.add(new Object());  //test non empty
t.applyTests("VectorNonEmpty");

 LinkedList li = new LinkedList();
 testList tl = new testList(li);
 tl.applyTests("LinkedList");
 
 v = new Vector();
 tl = new testList(v);
 tl.applyTests("Vector");
 v.add(new Object());  //test non empty
 tl.applyTests("VectorNonEmpty");

 }

}



class testList extends testGeneral{
List li;
public testList(List Li){
super("GenCol.testList");
li = Li;
}
public boolean testContainsAddNew(){
description = "after adding an object to new list,it is contained";
precondition = new Boolean(li.isEmpty());
Object e = new Object();
li.add(0,e);
return li.contains(e) == true;
}

public boolean testGetAdd1(){
description = "after adding an object at index 1 can get it there";
precondition = new Boolean(li.size()>=2);
Object e = new Object();
li.add(1,e);
return li.get(1) == e;
}
public boolean testGetAdd2(){
description = "after adding an object at index 2 can get it there";
precondition = new Boolean(li.size()>=3);
Object e = new Object();
li.add(2,e);
return li.get(2) == e;
}

public boolean testListIterator(){
description = "listIterator starts from given index";
precondition = new Boolean(li.size()>=2);
ListIterator lit = li.listIterator(1);
int i = 1;//0; will fail
while (lit.hasNext()){
 if (lit.next() != li.get(i))
    return false;
    else i++;
    }
return true;

}

public boolean testIndexShift(){
description = "after adding an object at index 0, right elements are shifted one over";
precondition = new Boolean(li.size()>0);
if (precondition.booleanValue()){
Object currentAt0 = li.get(0);
Object e = new Object();
li.add(0,e);
return li.get(1) == currentAt0;
}
else return false;
}


public boolean testIndexShiftException(){
description = "after adding an object at index 0, right elements are shifted one over";
precondition = Boolean.TRUE;
if (precondition.booleanValue()){
Object currentAt0 = li.get(0);
Object e = new Object();
li.add(0,e);
return li.get(1) == currentAt0;
}
else return false;
}
}

