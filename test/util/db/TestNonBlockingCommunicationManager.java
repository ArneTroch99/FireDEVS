package util.db;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import util.ObjectWrapper;
import util.OptionalWrapper;

public class TestNonBlockingCommunicationManager
{
    private NonBlockingCommunicationManager cut;
    private String sql;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockStatement;
    
    private ByteArrayOutputStream stdOutContent;
    private ByteArrayOutputStream stdErrContent;
    private PrintStream originalStdOut;
    private PrintStream originalStdErr;
    
    private ConnectionFactory fakeConnectionFactory;
    
    private void catchOutput()
    {
        stdOutContent = new ByteArrayOutputStream();
        stdErrContent = new ByteArrayOutputStream();
        originalStdOut = System.out;
        originalStdErr = System.err;
        
        System.setOut(new PrintStream(stdOutContent));
        System.setErr(new PrintStream(stdErrContent));
    }
    
    private void restoreOutput()
    {
        System.setOut(originalStdOut);
        System.setErr(originalStdErr);
    }
    
    private void setupBasicDDL() throws SQLException, InterruptedException
    {
        sql = "CREATE TABLE some_table (col1 VARCHAR(5), col2 INTEGER)";
        
        doReturn(mockStatement).when(mockConnection).prepareStatement(sql);
    }
    
    private void setupBasicDML() throws SQLException, InterruptedException
    {
        sql = "INSERT INTO some_table VALUES(?, ?, ?, ?)";
        
        doReturn(mockStatement).when(mockConnection).prepareStatement(sql);
    }
    
    private String escape(String s)
    {
        return s.replace("(", "\\(").replace(")", "\\)").replace("?", "\\?");
    }
    
    private void createCut(Optional<ConnectionFactory> connectionFactory)
    {
        if (connectionFactory.isPresent())
        {
            fakeConnectionFactory = connectionFactory.get();
        }
        else
        {
            fakeConnectionFactory = new ConnectionFactory() {
                @Override
                public Connection newConnection() throws SQLException
                {
                    return mockConnection;
                }
            };
        }

        cut = new NonBlockingCommunicationManager(fakeConnectionFactory);
    }
    
    @BeforeEach
    private void initMocks()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteSQL() throws SQLException, InterruptedException
    {
        setupBasicDDL();
        createCut(Optional.empty());
        
        doReturn(false).when(mockConnection).isClosed();
        
        cut.executeSQL(sql);
        Thread.sleep(800);
        cut.killAllThreads(0);
        
        verify(mockStatement, times(1)).execute();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).prepareStatement(sql);
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    public void testExecuteSQLException() throws SQLException, InterruptedException
    {
        setupBasicDDL();
        createCut(Optional.empty());

        doReturn(false).when(mockConnection).isClosed();
        
        SQLException mockException = mock(SQLException.class);
        doThrow(mockException).when(mockConnection).prepareStatement(sql);
        
        catchOutput();
        cut.executeSQL(sql);
        Thread.sleep(800);
        restoreOutput();
        cut.killAllThreads(0);
        
        verify(mockConnection, times(1)).prepareStatement(sql);
        
        Pattern errorPattern = Pattern.compile("Failed to execute:\\s+" + escape(sql) + "\\s+with arguments:\\s*", Pattern.DOTALL);
        Matcher m = errorPattern.matcher(stdErrContent.toString());
        assertTrue(m.matches());
    }
    
    @Test
    public void testExecuteInsert() throws SQLException, InterruptedException
    {
        setupBasicDML();
        createCut(Optional.empty());

        doReturn(false).when(mockConnection).isClosed();
        
        String col1Value = "abc";
        Integer col2Value = 42;
        Double col3Value = 42.1234d;
        UUID col4Value = UUID.fromString("0000-00-00-00-000000");
        
        cut.executeSQL(sql, col1Value, col2Value, col3Value, col4Value);
        Thread.sleep(800);
        cut.killAllThreads(0);
        
        verify(mockStatement, times(1)).execute();
        verify(mockStatement, times(1)).close();
        verify(mockStatement, times(1)).setString(1, col1Value);
        verify(mockStatement, times(1)).setInt(2, col2Value);
        verify(mockStatement, times(1)).setDouble(3, col3Value);
        verify(mockStatement, times(1)).setString(4, col4Value.toString());

        verify(mockConnection, times(1)).prepareStatement(sql);
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    public void testExecuteInsertNulls() throws SQLException, InterruptedException
    {
        setupBasicDML();
        createCut(Optional.empty());

        doReturn(false).when(mockConnection).isClosed();
        
        OptionalWrapper<String> col1Value = OptionalWrapper.empty(String.class);
        OptionalWrapper<Integer> col2Value = OptionalWrapper.empty(Integer.class);
        OptionalWrapper<Double> col3Value = OptionalWrapper.empty(Double.class);
        OptionalWrapper<UUID> col4Value = OptionalWrapper.empty(UUID.class);
        
        cut.executeSQL(sql, col1Value, col2Value, col3Value, col4Value);
        Thread.sleep(800);
        cut.killAllThreads(0);
        
        verify(mockStatement, times(1)).execute();
        verify(mockStatement, times(1)).close();
        verify(mockStatement, times(1)).setNull(1, Types.VARCHAR);
        verify(mockStatement, times(1)).setNull(2, Types.INTEGER);
        verify(mockStatement, times(1)).setNull(3, Types.DOUBLE);
        verify(mockStatement, times(1)).setNull(4, Types.VARCHAR);

        verify(mockConnection, times(1)).prepareStatement(sql);
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    public void testExecuteInsertException() throws SQLException, InterruptedException
    {
        setupBasicDML();
        createCut(Optional.empty());

        doReturn(false).when(mockConnection).isClosed();

        SQLException mockException = mock(SQLException.class);
        doThrow(mockException).when(mockConnection).prepareStatement(sql);

        String col1Value = "abc";
        Integer col2Value = 42;
        Double col3Value = 42.1234d;
        
        catchOutput();
        cut.executeSQL(sql, col1Value, col2Value, col3Value);
        Thread.sleep(800);
        restoreOutput();
        cut.killAllThreads(0);
        
        verify(mockConnection, times(1)).prepareStatement(sql);
        verify(mockConnection, times(1)).close();
        
        Pattern errorPattern = Pattern.compile("Failed to execute:\\s+" + escape(sql) + "\\s+with arguments:\\s+abc, 42, 42\\.1234\\s*");
        Matcher m = errorPattern.matcher(stdErrContent.toString());
        assertTrue(m.matches());
    }
    
    @Test
    public void testKillAllTimeout() throws SQLException, InterruptedException
    {
        setupBasicDML();
        createCut(Optional.empty());

        doReturn(false).when(mockConnection).isClosed();
        
        final String col1Value = "abc";
        final Integer col2Value = 42;
        final Double col3Value = 42.1234d;
        
        ObjectWrapper<Boolean> keepWaiting = new ObjectWrapper<Boolean>(Boolean.TRUE);
        
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation)
                throws Throwable
            {
                LocalTime whenToStop = LocalTime.now().plusSeconds(20);
                
                while(LocalTime.now().isBefore(whenToStop))
                {
                }                
                return null;
            }
            
        }).when(mockStatement).execute();
        
        Thread t = new Thread(new Runnable() {

            @Override
            public void run()
            {
                while(keepWaiting.get())
                {
                    cut.executeSQL(sql, col1Value, col2Value, col3Value);
                }
            }
            
        });
        
        t.start();
        Thread.sleep(1000);

        keepWaiting.set(Boolean.FALSE);
        catchOutput();
        assertTrue(cut.isExecutingDatabaseCommands());
        cut.killAllThreads(1);
        restoreOutput();
        
        t.join();
        
        verify(mockStatement, atLeastOnce()).execute();
        verify(mockStatement, atLeastOnce()).setString(1, col1Value);
        verify(mockStatement, atLeastOnce()).setInt(2, col2Value);
        verify(mockStatement, atLeastOnce()).setDouble(3, col3Value);

        verify(mockConnection, atLeastOnce()).prepareStatement(sql);
        verify(mockConnection, times(1)).close();
        
        Pattern errorPattern = Pattern.compile("Killing database thread\\.\\.\\.\\s*");
        Matcher m = errorPattern.matcher(stdErrContent.toString());
        assertTrue(m.matches());
    }
    
    @Test
    public void testConnectionDying() throws SQLException, InterruptedException
    {
        setupBasicDML();

        ObjectWrapper<Optional<SQLException>> mockException = new ObjectWrapper<Optional<SQLException>>(Optional.empty());

        createCut(Optional.of(new ConnectionFactory() {

            @Override
            public Connection newConnection() throws SQLException
            {
                if( mockException.get().isPresent() )
                {
                    throw mockException.get().get();
                }

                return mockConnection;
            }
            
        }));

        final String col1Value = "abc";
        final Integer col2Value = 42;
        final Double col3Value = 42.1234d;
        
        ObjectWrapper<Boolean> keepWaiting = new ObjectWrapper<Boolean>(Boolean.TRUE);
        
        Thread t = new Thread(new Runnable() {

            @Override
            public void run()
            {
                while(keepWaiting.get())
                {
                    cut.executeSQL(sql, col1Value, col2Value, col3Value);
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            
        });
        
        t.start();
        Thread.sleep(1000);

        catchOutput();

        doReturn(true).when(mockConnection).isClosed();
        mockException.set(Optional.of(mock(SQLException.class)));
        Thread.sleep(1000);

        restoreOutput();
        
        keepWaiting.set(Boolean.FALSE);
        cut.killAllThreads(0);
        
        verify(mockStatement, atLeastOnce()).execute();
        verify(mockStatement, atLeastOnce()).setString(1, col1Value);
        verify(mockStatement, atLeastOnce()).setInt(2, col2Value);
        verify(mockStatement, atLeastOnce()).setDouble(3, col3Value);

        verify(mockConnection, atLeastOnce()).prepareStatement(sql);
        
        Pattern errorPattern = Pattern.compile("Connection unable to be reopened!\\s*");
        Matcher m = errorPattern.matcher(stdErrContent.toString());
        assertTrue(m.matches());
    }
}
