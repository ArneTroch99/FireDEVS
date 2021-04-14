/* 
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.simulation.distributed;

import java.util.*;

import GenCol.*;


import model.modeling.*;
import util.*;

/**
 * Utility methods employed by the classes in this package.
 */
public class Util
{
    /**
     * Builds a message from the port and value entry pairs in the given
     * string.
     *
     * @param   string      The string to parse for port-value entry pairs.
     * @return              A message embodying the port-value pairs.
     */
    static public message interpret(String string)
    {
        // if the given string is null, or signals there is nothing to
        // interpret, return an empty message
        if (string == null || string.startsWith("none")) return new message();

        // while we can keeping finding more port and value entries in the
        // given string
        int searchIndex = 0;
        message message = new message();
        while (true) {
            // find the indices of the next port and value entries in the given
            // string
            final String portLabel = "port: ", valueLabel = " value: ";
            final int portLabelLength = portLabel.length();
            final int valueLabelLength = valueLabel.length();
            int portIndex = (searchIndex == 0) ?
                string.indexOf(portLabel, searchIndex) : searchIndex;
            int valueIndex = string.indexOf(valueLabel, searchIndex);

            // extract the next port entry from the string
            String portString = string.substring(portIndex + portLabelLength,
                valueIndex).trim();

            // extract the next value entry from the string
            searchIndex = valueIndex + valueLabelLength;
            int nextPortIndex = string.indexOf(portLabel, searchIndex);
            String valueString = string.substring(searchIndex,
                (nextPortIndex != -1) ? nextPortIndex : string.length()).trim();
            searchIndex = nextPortIndex;

            // add a new content containing the port and value entries
            // to the message we are building
            message.add(new content(portString, new entity(valueString)));

            // if we have run out of port entries, quit this loop
            if (nextPortIndex == -1) break;
        }

        return message;
    }
}