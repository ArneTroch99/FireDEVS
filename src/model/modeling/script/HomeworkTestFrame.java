package model.modeling.script;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;

import GenCol.Pair;
import model.modeling.devs;
import view.modeling.ViewableDigraph;

/**
 * {@link HomeworkTestFrame} is a DEVS-Scripting model for black-box 
 * testing multiple implementations of DEVS models. Each student's 
 * implementation is imported and tested against each test script. 
 * All assertion failures are captured and logged. Results are 
 * captured in a CSV file.
 * <p>
 * Instances of this class are not typically run from
 * DEVS-Suite, but executed from a main() routine. An
 * example of this class is provided in 
 * {@link Component.TestFixture.Homework.procHomework}.
 * 
 * @author Matthew McLaughlin
 */
public abstract class HomeworkTestFrame extends SimpleTestFrame
{
    /**
     * Annotates the implementation of a DEVS models. This may be a 
     * public static class nested inside the {@link HomeworkTestFrame}
     * or referenced by a public static Class<? extends devs> member 
     * that points to the implementation.
     *
     */
    @Target({TYPE, FIELD})
    @Retention(RUNTIME)
    public @interface Student
    {
        /**
         * A display name for the student. May be full name, student ID,
         * etc...
         * @return a {@link String}
         */
        public String name() default "Unknown";
    }
    
    /**
     * The homework test frame is used for testing multiple implementations
     * of DEVS models. Since the {@link HomeworkTestFrame} is derived from
     * the {@link SimpleTestFrame}, the derived class should not 
     * add couplings between the frame and the model under testing. These 
     * are all automatically generated. Derived classes should write a 
     * <code> public static main</code> method and call 
     * {@link #runTestCases(Class, String)}.
     *  
     * @param name name of the {@link TestFrame} model
     * @param run_case the method to run, or <code>null</code> for
     *    all sequential test scripts.
     * @param student_model the student model to test
     */
    protected HomeworkTestFrame(String name, Method run_case, Class<? extends devs> student_model)
    {
        super(name, run_case);

        // The professor must add the static modifier to nested classes.
        // Top-level classes do not need this modifier.
        
        if (student_model != null)
        {
            assertTrue(!student_model.isMemberClass() || Modifier.isStatic(student_model.getModifiers()));
        }
    }
    
    private static String escapeCSV(String s)
    {
        return s.replace("'", "").replace("\n", " ");
    }
    
    private static Iterable<Pair<Class<?>,Student>> getStudentsImpls(Class<? extends HomeworkTestFrame> klass, PrintWriter writer)
    {
        ArrayList<Pair<Class<?>,Student>> impls = new ArrayList<Pair<Class<?>,Student>>();
        
        for (Class<?> s_class : klass.getClasses())
        {
            Student ann = s_class.getAnnotation(Student.class);
            
            if (ann != null)
            {
                impls.add(new Pair<Class<?>,Student>(s_class, ann));
            }
        }
        
        for (Field field : klass.getFields())
        {
            Student ann = field.getAnnotation(Student.class);
            
            if (ann != null)
            {
                Object obj = null;
                
                try
                {
                    obj = field.get(null);
                }
                catch (IllegalArgumentException | IllegalAccessException e)
                {
                    throw new AssertionError("Field should be static and public", e);
                }

                assertTrue(obj instanceof Class<?>, "Field should be of type Class<? extends devs>");

                impls.add(new Pair<Class<?>,Student>((Class<?>) obj, ann));
            }
        }
        
        impls.sort((Pair<Class<?>,Student> a, Pair<Class<?>,Student> b)->
        {
            String sa = a.getValue().name();
            String sb = b.getValue().name();
            
            return sa.compareTo(sb);
        });
        
        return impls;
    }

    /**
     * This method should be called from the <code>main</code> method
     * within the derived class. This method parses through the derived
     * {@link HomeworkTestFrame} and locates {@link Student} and 
     * {@link TestScript} annotation, runs each student implementation
     * against each script, and generates the specified CSV file. 
     * 
     * @param klass the {@link HomeworkTestFrame} class
     * @param file_name the path for the CSV file to write
     */
    protected static void runTestCases(Class<? extends HomeworkTestFrame> klass, String file_name) throws FileNotFoundException
    {
        PrintWriter writer;
    
        writer = new PrintWriter(new File(file_name));
        writer.write("'Student','Test','Details'\n");
        
        Iterable<Pair<Class<?>, Student>> impls = getStudentsImpls(klass, writer);
                
        for (Pair<Class<?>, Student> entry : impls)
        {
            Class<?> s_class = entry.getKey();
            Student ann = entry.getValue();
            StringBuilder sb = new StringBuilder();

            if (! devs.class.isAssignableFrom(s_class))
            {
                writer.write("'" + escapeCSV(ann.name()) + "','','Student did not implement devs model'");
            }
            else
            {
                Stream<DynamicTest> s = getTestCases(klass, false, (Method m) -> {
                    Constructor<? extends HomeworkTestFrame> constructor;
                    HomeworkTestFrame frame;
                    
                    try
                    {
                        constructor = klass.getConstructor(String.class, Method.class, Class.class);
                        frame = constructor.newInstance("testframe", m, s_class);
                    }
                    catch (NoSuchMethodException | SecurityException | IllegalAccessException e)
                    {

                        // class requires a public constructor with (String, Method, Class<?>):void signature.

                        throw new AssertionError("HomeworkTestFrame is missing appropriate constructor.", e);
                    }
                    catch (InstantiationException | IllegalArgumentException | InvocationTargetException e)
                    {
                        throw new AssertionError("Unable to instantiate HomeworkTestFrame", e);
                    }
                    
                    return frame.createFixture("testfixture");
                });
     
                s.forEach((DynamicTest t) -> {
                    sb.append("'");
                    sb.append(escapeCSV(ann.name()));
                    sb.append("','");
                    sb.append(escapeCSV(t.getDisplayName()));
                    sb.append("','");
                
                    try
                    {
                        t.getExecutable().execute();
                        sb.append("OK");
                    }
                    catch(Throwable e)
                    {
                        String msg = "FAIL - " + e.getClass().getSimpleName();
                        
                        if (e.getMessage() != null)
                        {
                            msg += ": " + e.getMessage();
                        }
                        
                        sb.append(escapeCSV(msg));
                    }

                    sb.append("'\n");
                });
            }
            
            writer.write(sb.toString());
        }
        
        writer.close();
    }

    /**
     * This is a toy method. It creates all models and test frames and add 
     * them to the {@link ViewableDigraph} provided. This allows all
     * implementations to be observed in parallel.
     * 
     * @param self the {@link ViewableDigraph} used as a shell
     * @param hwklass the derived {@link HomeworkTestFrame}
     * @param test_case the test script that will run in parallel 
     */
    public static void addAllStudentFixtures(ViewableDigraph self, Class<? extends HomeworkTestFrame> hwklass, Method test_case)
    {
        try
        {
            Iterable<Pair<Class<?>, Student>> impls = getStudentsImpls(hwklass, null);
            Constructor<? extends HomeworkTestFrame> constructor;
            int n = 0;
            
            constructor = hwklass.getConstructor(String.class, Method.class, Class.class);

            for (Pair<Class<?>, Student> student_klass : impls)
            {
                HomeworkTestFrame frame;
                n++;
                
                frame = constructor.newInstance("testframe", test_case, student_klass.getKey());
                TestFixture sub_fixture = frame.createFixture("testfixture" + n);
                
                self.add(sub_fixture);
                
                self.addOutport("out" + n);
                self.addCoupling(sub_fixture, "out", self, "out" + n);
            }
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException | NoSuchMethodException | SecurityException e)
        {
            throw new AssertionError("Unable to build all models, use runTestCases() to diagnose.");
        }
    }
}
