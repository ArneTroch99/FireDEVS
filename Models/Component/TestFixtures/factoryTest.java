package Component.TestFixtures;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import model.modeling.script.TestFrame;

class factoryTest
{

    @TestFactory
    @DisplayName("Test Case Generator")
    Stream<DynamicTest> getTestCases()
    {
        return TestFrame.getTestCases(factoryFrame.class, false);
    }

}
