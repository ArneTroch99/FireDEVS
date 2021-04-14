package util.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestBlockingCommunicationManager
{
    private BlockingCommunicationManager cut;
    private String sql;
    private PreparedStatement mockStatement;
    private ResultSet mockResult;
    
    @Mock
    private ConnectionFactory mockConnectionFactory;

    @Mock
    private Connection mockConnection;
    
    private ByteArrayOutputStream stdOutContent;
    private ByteArrayOutputStream stdErrContent;
    private PrintStream originalStdOut;
    private PrintStream originalStdErr;
    
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
    
    @BeforeEach
    public void makeCut()
    {
        cut = new BlockingCommunicationManager(mockConnectionFactory);
    }
    
    @BeforeEach
    public void initMocks()
    {
        MockitoAnnotations.initMocks(this);
    }
    
    private void setupBasicQuery() throws SQLException, InterruptedException
    {
        sql = "SELECT COUNT(*) AS num_rows FROM some_table";
        doReturn(mockConnection).when(mockConnectionFactory).newConnection();
        
        mockResult = mock(ResultSet.class);
        doReturn(10).when(mockResult).getInt(0);
        
        mockStatement = mock(PreparedStatement.class);
        doReturn(mockResult).when(mockStatement).getResultSet();
        doReturn(true).when(mockStatement).execute();
        
        doReturn(mockStatement).when(mockConnection).prepareStatement(sql);
    }
    
    private void setupBasicDDL() throws SQLException, InterruptedException
    {
        sql = "CREATE TABLE some_table (col1 VARCHAR(5), col2 INTEGER)";
        doReturn(mockConnection).when(mockConnectionFactory).newConnection();
        
        mockStatement = mock(PreparedStatement.class);
        doReturn(false).when(mockStatement).execute();
        
        doReturn(mockStatement).when(mockConnection).prepareStatement(sql);
    }
    
    @Test
    public void testExecuteQuery() throws SQLException, InterruptedException
    {
        setupBasicQuery();

        assertEquals(Optional.of(10), cut.executeQuery(sql, (ResultSet result) -> {
                try
                {
                    return result.getInt(0);
                }
                catch (SQLException e)
                {
                    return -1;
                }
        }));
        
        verify(mockResult, times(1)).close();
        verify(mockStatement, times(1)).close();
    }
    
    @Test
    public void testExecuteQueryException() throws SQLException, InterruptedException
    {
        setupBasicQuery();
        SQLException mockException = mock(SQLException.class);

        try
        {
            Optional<Object> x = cut.executeQuery(sql, (ResultSet result) -> { throw mockException; });
            assertFalse(x.isPresent());
            fail();
        }
        catch (SQLException e)
        {
            assertEquals(mockException, e);
        }
        
    }
    
    @Test
    public void testExecuteQueryTimeout() throws SQLException, InterruptedException
    {
        setupBasicQuery();

        catchOutput();
        assertEquals(Optional.empty(), cut.executeQueryWithTimeout(sql, (ResultSet result) -> {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e1)
            {
            }

            try
            {
                return result.getInt(0);
            }
            catch (SQLException e)
            {
                return -1;
            }
        }, 40));
        
        restoreOutput();
        assertTrue(stdErrContent.toString().contains("Thread runtime limit exceeded: 40ms"));
    }
    
    @Test
    public void testExecute() throws SQLException, InterruptedException
    {
        setupBasicDDL();

        try
        {
            cut.executeSQL(sql);
        }
        catch(SQLException e)
        {
            fail();
        }
        verify(mockStatement, times(1)).close();
    }
    
    @Test
    public void testExecuteException() throws SQLException, InterruptedException
    {
        setupBasicDDL();
        
        SQLException mockException = mock(SQLException.class);
        doThrow(mockException).when(mockStatement).execute();

        try
        {
            cut.executeSQL(sql);
            fail();
        }
        catch(SQLException e)
        {
            assertEquals(mockException, e);
        }
    }
    
    @Test
    public void testExecuteTimeout() throws SQLException, InterruptedException
    {
        setupBasicDDL();

        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation)
                throws Throwable
            {
                Thread.sleep(1000);
                return null;
            }

        }).when(mockStatement).execute();

        catchOutput();
        try
        {
            cut.executeSQLWithTimeout(sql, 50);
        }
        catch(SQLException e)
        {
            restoreOutput();
            fail();
        }
        restoreOutput();
        assertTrue(stdErrContent.toString().contains("Thread runtime limit exceeded: 50ms"));
    }
}
