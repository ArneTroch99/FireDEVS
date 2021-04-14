package Component.TestFixtures.Homework;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import model.modeling.script.TestFrame;

class procTests
{
    public static class procFrame extends procHomework
    {
        public procFrame(String name, Method run_case)
        {
            
            // Student#3's processor was selected for the
            // workstation model.
            
            super(name, run_case, proc3);
        }
    }
    
    @TestFactory
    @DisplayName("Test Case Generator")
    Stream<DynamicTest> getTestCases()
    {
        return TestFrame.getTestCases(procFrame.class, false);
    }
}
