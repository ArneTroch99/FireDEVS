package util.tracking;

import static org.assertj.swing.core.matcher.DialogMatcher.withTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.jcip.annotations.NotThreadSafe;
import view.DatabaseTracker;
import view.Tracker;

@NotThreadSafe
public class TestDatabaseTrackerManager
{
    private DatabaseTrackerManager cut;
    private List<List<Tracker>> mockDatabaseTrackers;
    private Robot robot;
    
    private Robot robot()
    {
        if (robot == null)
        {
            robot = BasicRobot.robotWithCurrentAwtHierarchyWithoutScreenLock();
        }
        
        return robot;
    }
    
    @AfterEach
    private void TearDown()
    {
        if (robot != null)
        {
            robot.cleanUp();
        }
    }

    private void clickButtonInSeconds(double seconds, String button)
    {
        final ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(2);
        stpe.schedule(new Runnable() {

            @Override
            public void run()
            {
                DialogFixture f = WindowFinder.findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {

                    @Override
                    protected boolean isMatching(JDialog component)
                    {
                        return component.getTitle().equals("Continue database operations?") && component.isVisible();
                    }
                }).using(robot());
                f.button(JButtonMatcher.withText(button)).target().doClick();
            }
            
        }, (int)(1000.0d * seconds), TimeUnit.MILLISECONDS);
    }
    
    private void setupMockDatabaseTrackers()
    {
        mockDatabaseTrackers = new ArrayList<List<Tracker>>();
        
        mockDatabaseTrackers.add(new ArrayList<Tracker>());
        mockDatabaseTrackers.add(new ArrayList<Tracker>());
        
        mockDatabaseTrackers.get(0).add(mock(DatabaseTracker.class));
        mockDatabaseTrackers.get(0).add(mock(DatabaseTracker.class));
        mockDatabaseTrackers.get(1).add(mock(DatabaseTracker.class));
        
        doReturn(true).when(mockDatabaseTrackers.get(0).get(0)).isDatabaseTrackingEnabled();
        doReturn(true).when(mockDatabaseTrackers.get(0).get(1)).isDatabaseTrackingEnabled();
        doReturn(true).when(mockDatabaseTrackers.get(1).get(0)).isDatabaseTrackingEnabled();
        
        doReturn(true).when(((DatabaseTracker) mockDatabaseTrackers.get(0).get(0))).isDatabaseOpsFinished();
        doReturn(true).when(((DatabaseTracker) mockDatabaseTrackers.get(0).get(1))).isDatabaseOpsFinished();
        doReturn(true).when(((DatabaseTracker) mockDatabaseTrackers.get(1).get(0))).isDatabaseOpsFinished();
    }
    
    @BeforeEach
    public void createCut()
    {
        cut = new DatabaseTrackerManager();
    }
    
    private void verifyKilled(List<Tracker> trackers, int timeout)
    {
        trackers.stream().map(
            (Tracker t) -> ((DatabaseTracker) t)
        ).forEach(
            (DatabaseTracker t) -> {
                verify(t, atLeastOnce()).killAllDatabaseWorkers(timeout);
                verify(t, atLeastOnce()).isDatabaseOpsFinished();
            }
        );
    }

    @Test
    public void testStopDBOperations()
    {
        int timeout = 50;
        setupMockDatabaseTrackers();
        assertTrue(cut.areDatabaseOpsFinished());

        cut.addTrackers(mockDatabaseTrackers.get(0));
        
        assertTrue(cut.areDatabaseOpsFinished());

        cut.stopDatabaseOperations(timeout);
        
        verifyKilled(mockDatabaseTrackers.get(0), timeout);

        cut.addTrackers(mockDatabaseTrackers.get(1));
        
        assertTrue(cut.areDatabaseOpsFinished());

        cut.stopDatabaseOperations(timeout);

        verifyKilled(mockDatabaseTrackers.get(0), timeout);
        verifyKilled(mockDatabaseTrackers.get(1), timeout);
    }
    
    @Test
    public void testPromptYes() throws InterruptedException
    {
        clickButtonInSeconds(1.5, "Yes");
        assertEquals(true, cut.showContinueDatabaseOperationsPrompt());
    }
    
    @Test
    public void testPromptNo() throws InterruptedException
    {
        clickButtonInSeconds(1.2, "No");
        assertEquals(false, cut.showContinueDatabaseOperationsPrompt());
    }
}
