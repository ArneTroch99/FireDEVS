/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package util;

import java.io.*;
import javax.swing.*;

/**
 * An application that tries to convert DEVSJava model files into ones
 * that are compatible with GenDevs.  This is done using only simple textual
 * substitutions, so there may be changes necessary that are beyond the
 * capabilities of this program.  However, most of the grunt work
 * of the conversion process should be eliminated.
 *
 * @author      Jeff Mather
 */
public class OldModelConverter
{
    /**
     * The command to specify as the second line of a conversion-pair to
     * indicate that the entire line of text should be deleted.
     */
    static final protected String deleteCommand = "<del>";

    /**
     * The conversions to perform.  For each pair of lines, the first
     * represents the string to convert, and the seconds represents the
     * string with which to replace it.
     */
    protected static String newLine = System.getProperty("line.separator");
    protected String[] conversions = {
        "import Zdevs.*;",
            "import genDevs.modeling.*;" + newLine + "import genDevs.simulation.*;",
        "import Zdevs.Zcontainer.*;",
            "import GenCol.*;",
        "import GUIDEVS.*;",
            "import simView.*;",
        "atomGraph",
            "ViewableAtomic",
        "digraphGraph",
            "ViewableDigraph",
        "inports.add",
            "addInport",
        "outports.add",
            "addOutport",
        "phases.add",
            deleteCommand,
        "phase_is",
            "phaseIs",
        "message_on_port",
            "messageOnPort",
        "hold_in",
            "holdIn",
        "get_length",
            "getLength",
        "addString",
            "System.out.println",
        "get_name",
            "getName",
        "classname =",
            deleteCommand,
        "get_val_on_port",
            "getValOnPort",
        "make_content",
            "makeContent",
        "addTestPortValue",
            "addTestInput",
        "get_head()",
            "read(0)",
        ".get_ent()",
            "",
        "con.val",
            "con.getValue()",
        "double Out()",
            "double sisoOut()",
        "new doubleEnt(Out())",
            "new doubleEnt(sisoOut())",
        "function",
            "Function",
        ".getLength()",
            ".size()",
        ".empty()",
            ".isEmpty()",
        "Add_coupling",
            "addCoupling",
        "add_coupling",
            "addCoupling",
        "show_state",
            "showState",
        "show_coupling",
            deleteCommand,
        "queue",
            "Queue",
        "pair",
            "Pair",
        "get_key(",
            "getKey(",
        "get_value(",
            "getValue(",
        ".eq(",
            ".equals(",
        ".front()",
            ".first()",
        "passivate_in",
            "passivateIn",
        "relation",
            "Relation",
        "set ",
            "Set ",
        "set()",
            "HashSet()",
        "remove_all",
            "removeAll",
        ".read(0)",
            ".iterator().next()",
        ".list_ref(",
            ".get(",
        "get_components()",
            "getComponents()",
        ".equal(",
            ".equals(",
        "initialize()",
            deleteCommand,
    };

    /**
     * A constructor.
     */
    public OldModelConverter()
    {
        // create a file-chooser, to be used below
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setMultiSelectionEnabled(true);

        // keep doing this
        while (true) {
            // pop up the file chooser so the user may select which
            // files to convert
            int result = chooser.showOpenDialog(null);

            // if the user clicked 'ok' in the file chooser
            if (result == JFileChooser.APPROVE_OPTION) {
                // for each file selected by the user
                File[] files = chooser.getSelectedFiles();
                for (int i = 0; i < files.length; i++) {
                    // convert this file
                    System.out.print("Converting " + files[i].getName() + "...");
                    convertFile(files[i]);
                    System.out.println(" done");
                }
            }

            else break;
        }

        System.exit(0);
    }

    /**
     * Reads in the contents of the given file, performs the above textual
     * substitutions on them, then writes the converted contents out to a new
     * file.
     *
     * @param   file        The file to convert.
     */
    protected void convertFile(File file)
    {
        // read in the contents of the file
        byte[] buffer = null;
        try {
            InputStream stream = new FileInputStream(file);
            buffer = new byte[(int)file.length()];
            stream.read(buffer);
            stream.close();
        } catch (IOException e) {e.printStackTrace();}

        // turn the contents of the file into a string
        String text = new String(buffer);

        // for each conversion pair specified
        for (int i = 0; i < conversions.length / 2; i++) {
            // keep doing this
            String convert = conversions[i * 2];
            int index = 0;
            while (true) {
                // find the next instance of the string to convert within the
                // text
                StringUtil.IndexOfIgnoreWhitespaceResult result =
                    StringUtil.indexOfIgnoreWhitespace(text, convert, index);
                index = result.index;
                if (index == -1) break;

                // if the conversion indicates the whole line should be deleted
                String conversion = conversions[i * 2 + 1];
                if (conversion.equals(deleteCommand)) {
                    // delete the line on which the instance was found
                    int previousNewLineIndex = text.lastIndexOf(newLine, index);
                    int nextNewLineIndex = text.indexOf(newLine, index);
                    text = text.substring(0, previousNewLineIndex)
                        + text.substring(nextNewLineIndex, text.length());
                }

                // otherwise
                else {
                    // convert the string
                    text = text.substring(0, index) + conversion
                        + text.substring(index + convert.length() +
                        result.numWhitespaceCharsSkipped, text.length());

                    // advance beyond the conversion
                    index += conversion.length();
                }
            }
        }

        // if the file being converted doesn't already have an old-suffix
        // on it
        final String oldSuffix = ".old";
        if (!file.getName().endsWith(oldSuffix)) {
            // rename the original file with an old-suffix, so the
            // old version isn't lost
            file.renameTo(new File(file.getPath() + oldSuffix));
        }

        // otherwise
        else {
            // the converted file should have the old filename minus the
            // old-suffix
            String path = file.getPath();
            file = new File(path.substring(0, path.lastIndexOf(oldSuffix)));
        }

        // write out the converted file
        try {
            OutputStream stream = new FileOutputStream(file);
            stream.write(text.getBytes());
            stream.close();
        } catch (IOException e) {e.printStackTrace();}
    }

    /**
     * Starts this application.
     */
    static public void main(String[] args)
    {
        new OldModelConverter();
    }
}
