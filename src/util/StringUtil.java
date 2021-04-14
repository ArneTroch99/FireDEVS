/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package util;

/**
 * A utility class containing various methods for manipulating strings.
 *
 * @author      Jeff Mather
 */
public class StringUtil
{
    /**
     * Replaces all the occurrences of the given substring within the
     * given string with a given replacement substring.
     *
     * @param   string      The string in which to make the replacements.
     * @param   replace     The substring to look for and replace.
     * @param   with        The substring to replace with.
     *
     * @return              A new string with the replacements made.
     */
    static public String replaceAll(String string, String replace, String with)
    {
        // keep doing this
        StringBuffer result = new StringBuffer();
        result.append(string);
        int replaceLength = replace.length();
        int withLength = with.length();
        int index = 0, resultIndex = 0;
        while (true) {
            // if there is a next instance of the string to replace
            int oldIndex = index;
            index = string.indexOf(replace, index);
            if (index >= 0) {
                // do the replacement
                resultIndex += index - oldIndex;
                result.replace(resultIndex, resultIndex + replaceLength, with);
                resultIndex += withLength - replaceLength + 1;
                index++;
            }

            // otherwise, we are done
            else break;
        }

        return result.toString();
    }

    /**
     * The return value type for the indexOfIgnoreWhitespace method below.
     */
    static public class IndexOfIgnoreWhitespaceResult
    {
        /**
         * The index where the query string was found within the text.
         * -1 means no instance of the query string was found.
         */
        public int index = -1;

        /**
         * How many whitespace characters in the text were ignored
         * within the match found for the query string.
         */
        public int numWhitespaceCharsSkipped;
    }

    /**
     * Returns the index of the first occurrence of the query string
     * within the given text, starting the search from the given index.
     *
     * @param   text        The text within which to search.
     * @param   query       The query string for which to search.
     * @param   startIndex  The index within the text to start the search.
     */
    static public IndexOfIgnoreWhitespaceResult indexOfIgnoreWhitespace(
        String text, String query, int startIndex)
    {
        // for each character in the given text, starting at the given
        // start-index
        IndexOfIgnoreWhitespaceResult result =
            new IndexOfIgnoreWhitespaceResult();
        int textLength = text.length(), queryLength = query.length();
        for (int i = startIndex; i < textLength - queryLength; i++) {
            // keep doing this
            int textIndex = i, queryIndex = 0;
            result.numWhitespaceCharsSkipped = 0;
            while (true) {
                // if the current matching process has gone beyond the end
                // of the text, abort it
                if (textIndex >= textLength) break;

                // if the text and query string characters match
                char textChar = text.charAt(textIndex);
                if (textChar == query.charAt(queryIndex)) {
                    // advance to the next text and query string characters
                    textIndex++;
                    queryIndex++;

                    // if we have reached the end of the query string
                    if (queryIndex == queryLength) {
                        // a match was found, so return the index of the match
                        result.index = i;
                        return result;
                    }

                    continue;
                }

                // else, if the text character is whitespace
                else if (Character.isWhitespace(textChar)) {
                    // if the whitespace is at the front of the potential
                    // match, abort this match
                    if (textIndex == i) break;

                    // record that a whitespace character was ignored
                    result.numWhitespaceCharsSkipped++;

                    // advance to the next text character
                    textIndex++;

                    continue;
                }

                break;
            }
        }

        return result;
    }
}