/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package util;


import java.lang.*;
import java.util.*;


public class doubleFormat{

private final static String s = ".0123456789-";
private static char [] chA = new char[s.length()];
private static char [] c1 = new char[1];

public static boolean isDigit(char c){
     for (int j = 0;j <s.length(); j++)
      if (c1[0] == chA[j]) return true;
     return false;
     }
public static boolean allDigits(String inp){
if (inp == null) return true;
 s.getChars(0,s.length(),chA,0);
 for (int i = 0;i<inp.length(); i++){
       inp.getChars(i,i+1,c1,0);
       if (!isDigit(c1[0]))return false;
     }
    return true;
   }
public  static String niceString(String s){
     if (s == null)return s;
     if (!allDigits(s))return s;
     if (s.indexOf("-") > 0)return s;
      int dot =  s.indexOf(".");
      String left = s.substring(0,dot);
      String right = s.substring(dot+1,s.length());
     if (right.length() <3)return s;
     return left + "."+ right.substring(0,3);
}

public static double niceDouble(double d){
  String s = niceString(Double.toString(d));
  return Double.parseDouble(s);
  }

public static void main(String[] args){
System.out.println(niceDouble(.0100));
}
}