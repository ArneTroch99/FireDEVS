package util.db;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.assertj.core.util.Arrays;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.matcher.JTextComponentMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.jcip.annotations.NotThreadSafe;
import util.db.v.PasswordPromptView;

@NotThreadSafe
public class TestPostgresSQLDatabaseConnectionConfiguration
{
    private DatabaseConnectionConfiguration cut;
    private Connection mockConnection;
    private double passwordPromptWaitTime;
    
    private String databaseName, host, port, userName, schema;
    
    private static Robot robot;
    
    private class TestConnectionFactory implements ConnectionFactory
    {
        @Override
        public Connection newConnection() throws SQLException
        {
            return mockConnection;
        }
    }
    
    private class PasswordGetter extends GenericPasswordProtectedDatabaseConnectionConfiguration
    {
        private PostgreSQLDatabaseConnectionConfiguration _cut;
        
        public PasswordGetter(PostgreSQLDatabaseConnectionConfiguration _cut)
        {
            this._cut = _cut;
        }
        
        public String getPassword()
        {
            return _cut.password;
        }
        
        @Override
        public Optional<SQLException> test()
            throws ConnectionTestException
        {
            return null;
        }

        @Override
        public DatabaseTypes getDatabaseType()
        {
            return null;
        }

        @Override
        public ArrayList<String> toList()
        {
            return null;
        }

        @Override
        public Connection newConnection() throws SQLException
        {
            return null;
        }
    }
    
    public TestPostgresSQLDatabaseConnectionConfiguration()
    {
        passwordPromptWaitTime = 1.2;
        databaseName = "postgresdb";
        host = "localhost";
        port = "5432";
        userName = "postgresuser";
        schema = "postgresschema";
    }

    private void createCut()
    {
        mockConnection = mock(Connection.class);
        cut = new PostgreSQLDatabaseConnectionConfiguration(databaseName, host, port, userName, schema, Optional.of(new TestConnectionFactory()));
    }
    
    private static Robot robot()
    {
        if (robot == null)
        {
            robot = BasicRobot.robotWithCurrentAwtHierarchyWithoutScreenLock();
        }
        
        return robot;
    }
    
    private void clickOkInSeconds(double seconds)
    {
        clickButtonInSeconds(seconds, "OK", "");
    }
    
    private void clickCancelInSeconds(double seconds)
    {
        clickButtonInSeconds(seconds, "Cancel", "");
    }
    
    private void clickButtonInSeconds(double seconds, String button, String textToWrite)
    {
        final ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(2);
        stpe.schedule(new Runnable() {

            @Override
            public void run()
            {
                DialogFixture f = WindowFinder.findDialog(PasswordPromptView.DIALOG_NAME).using(robot());
                f.textBox(JTextComponentMatcher.withText("")).target().setText(textToWrite);
                f.button(withText(button)).target().doClick();
            }
            
        }, (int)(1000.0d * seconds), TimeUnit.MILLISECONDS);
    }
    
    @BeforeEach
    public void beforeEach()
    {
        createCut();
    }
    

    @Test
    public void testSqlException() throws SQLException
    {
        doThrow(SQLException.class).when(mockConnection).close();
        clickOkInSeconds(passwordPromptWaitTime);

        Optional<SQLException> e = cut.test();

        assertTrue(e.isPresent());
    }
    
    @Test
    public void testNoSqlException()
    {
        clickOkInSeconds(passwordPromptWaitTime);

        Optional<SQLException> e = cut.test();

        assertFalse(e.isPresent());
    }
    
    @Test
    public void testCancel()
    {
        clickCancelInSeconds(passwordPromptWaitTime);
        
        try
        {
            cut.test();
            fail();
        }
        catch(PasswordDialogCancelledException e)
        {
            
        }
    }
    
    @Test
    public void testPassword()
    {
        clickButtonInSeconds(passwordPromptWaitTime, "OK", "somePassword");

        Optional<SQLException> e = cut.test();

        assertEquals("somePassword", new PasswordGetter((PostgreSQLDatabaseConnectionConfiguration) cut).getPassword());
    }
    
    @Test
    public void testToList()
    {
        String[] exp = Arrays.array(databaseName, host, port, userName, schema);

        List<String> actList = cut.toList();
        assertArrayEquals(exp, actList.toArray());
    }
}
