/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package util;

import java.util.*;

/**
 * A comparator that compares strings that might contain one or more
 * numeric substrings, considering their numeric values in addition to
 * their usual alphabetical order.
 */
public class NumericStringComparator implements Comparator
{
    /**
     * Compares two strings to see which is greater, taking into
     * account any numbers starting at identical indices within the strings.
     * For instance, according to this comparator "x9" comes
     * before "x10", not after as would be dictated by a pure
     * alphabetical sort.
     *
     * See parent method for parameter and return value descriptions.
     */
    public int compare(Object o1, Object o2)
    {
        // cast the given objects to strings
        String a = (String)o1, b = (String)o2;

        // for each corresponding character position within the two strings
        int aLength = a.length(), bLength = b.length();
        int lengthToCompare = Math.min(aLength, bLength);
        int alphaResult = 0;
        char aChar = 0, bChar = 0;
        for (int i = 0; i < lengthToCompare; i++) {
            // detm if the character at this position within each string
            // is a numeral
            aChar = a.charAt(i);
            bChar = b.charAt(i);
            boolean aInNum = Character.isDigit(aChar),
                bInNum = Character.isDigit(bChar);

            // if both current characters are numerals (meaning, we are
            // now in the process of comparing two numbers)
            if (aInNum && bInNum) {
                // if no alphabetical result has yet been detm'd to fall back
                // upon in case the numbers being scanned have an equal number
                // of digits
                if (alphaResult == 0 && aChar != bChar) {
                    // detm what the normal, alphabetical result would be
                    // for the two strings
                    alphaResult = (aChar < bChar) ? -1 : 1;
                }
            }

            // else, if one or both numbers just ended, which we know
            // because we have an unused alphabetical result to report
            else if (alphaResult != 0) {
                // if "a" is still in a number, it must have the larger
                // number, so it is the greater string
                if (aInNum) return 1;

                // if "b" is still in a number, it must have the larger
                // number, so it is the greater string
                if (bInNum) return -1;

                // if neither string is now in a number, fall back on the
                // yet-unused alphabetical result
                if (!aInNum && !bInNum) return alphaResult;
            }

            // otherwise (meaning, there were no recent numbers in the strings)
            else {
                // do a normal alphabetical comparison of the current
                // characters
                if (aChar != bChar) return (aChar < bChar) ? -1 : 1;
            }
        }

        // if we make it here, then one or both of the strings have run out
        // of characters to compare, so whichever string is shorter is
        // the lesser string (which may not be correct in the case where both
        // strings ended in numbers with leading zeroes), but if
        // both strings are through, judge by the last character to detm
        // which string is greater
        if (aLength == bLength) return (aChar == bChar) ? 0 :
            ((aChar < bChar) ? -1 : 1);
        return (aLength < bLength) ? -1 : 1;
    }
}