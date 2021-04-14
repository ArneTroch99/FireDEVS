package Component.TestFixtures.Workstation;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import model.modeling.script.TestFrame;

class wsTest
{
    @TestFactory
    @DisplayName("Test Case Generator")
    Stream<DynamicTest> getTestCases()
    {
        return TestFrame.getTestCases(wsFrame.class, false);
    }
}
