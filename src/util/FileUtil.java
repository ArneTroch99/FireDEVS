/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package util;

import java.io.*;

public class FileUtil
{
    static public String getContentsAsString(File file)
    {
        try {
            StringBuffer buffer = new StringBuffer();
            BufferedReader in = new BufferedReader(new FileReader(file));
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                buffer.append(line);
                buffer.append("\n");
            }

            return buffer.toString();
        } catch (IOException e) {e.printStackTrace(); return null;}
    }
}