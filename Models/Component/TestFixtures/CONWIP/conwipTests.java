package Component.TestFixtures.CONWIP;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import model.modeling.script.TestFrame;

class conwipTests
{
    @TestFactory
    @DisplayName("Test Case Generator")
    Stream<DynamicTest> getTestCases()
    {
        return TestFrame.getTestCases(conwipFrame.class, false);
    }
}
