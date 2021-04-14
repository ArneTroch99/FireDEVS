package util.classUtils;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Container for routine for reading class files by their package and class name
 * and returning a {@link java.lang.Class} object.
 * 
 * @see #readClass(String, String)
 */
public class DevsClassFileReader
{
    /**
     * Routine to read a class by package and name and return a
     * {@link java.lang.Class} object. Refer to <b>See Also</b> for information
     * about exceptions thrown by this method.
     * 
     * @see java.lang.ClassLoader#loadClass(String)
     * @param pkg
     *            package of the class to load
     * @param className
     *            name of the class to load
     * @return <code>Class</code> object for the package/name of the class
     *         provided
     * @throws ClassNotFoundException
     */
    public static Class<?> readClass(String pkg, String className) throws ClassNotFoundException
    {
        StringBuilder sb = new StringBuilder();
        
        if (pkg != null && !pkg.equals(""))
        {
            sb.append(pkg).append('.');
        }
        sb.append(className);

        ClassLoader loader = DevsClassFileReader.class.getClassLoader();

        try
        {
            return loader.loadClass(sb.toString());
        }
        catch (ClassNotFoundException e)
        {
            ClassNotFoundException e2 = new ClassNotFoundException("Class not found: " + e.getMessage());
            
            e2.setStackTrace(e.getStackTrace());
            Throwable c = e.getCause();
            if (c != null)
            {
                e2.initCause(c);
            }
            
            throw e2;
        }
    }
    
    /**
     * Routine to read a class by package and name and return a
     * {@link java.lang.Class} object. Refer to <b>See Also</b> for information
     * about exceptions thrown by this method.
     * 
     * @see java.lang.ClassLoader#loadClass(String)
     * @param qualifiedName
     * @return <code>Class</code> object for the package/name of the class
     *         provided
     * @throws ClassNotFoundException
     * @throws NullPointerException
     */
    public static Class<?> readClass(String qualifiedName) throws ClassNotFoundException, NullPointerException
    {
        String[] parsedName = qualifiedName.split("\\.");
        
        if (parsedName.length <= 0)
        {
            throw new ClassNotFoundException("Class not found: " + qualifiedName);
        }
        
        String className = parsedName[parsedName.length - 1];
        String pkg = "";
        
        if (parsedName.length > 1)
        {
            Stream<String> s = Arrays.stream(parsedName, 0, parsedName.length - 1);
            StringBuilder sb = new StringBuilder();

            s.forEach((String str) -> {
               sb.append(str).append('.');
            });

            if (sb.length() > 0)
            {
                pkg = sb.substring(0, sb.length() - 1).toString();
            }
        }
        
        return readClass(pkg, className);
    }

    /**
     * Static/Singleton class, no instances allowed.
     */
    private DevsClassFileReader()
    {
        // Static/Singleton Class, no instances allowed
    }
}
