package net.sf.log4jdbc;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lazyluke.log4jdbcremix.test.tools.SimpleInMemoryLog4jAppender;


public class ResultSetTableLoggingTest {

    private static final String TESTDB_PASSWORD = "";
    private static final String TESTDB_USERNAME = "sa";
    private static final String JDBC_URL = "jdbc:h2:mem:db1;MODE=Oracle";
    private static final String JDBC_DRIVER = "org.h2.Driver";

    private static SimpleInMemoryLog4jAppender inMemoryLogger;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	LogManager.resetConfiguration();
        Logger.getRootLogger().setLevel(Level.ERROR);
        inMemoryLogger = new SimpleInMemoryLog4jAppender();
        Logger.getRootLogger().addAppender(inMemoryLogger);
    }
    
    @Before
    public void setUp() throws Exception {
        inMemoryLogger.clear();
    }

    @After
    public void tearDown() throws Exception {

    }
    
    @Test
    public void testInfoLevelPrintsUnreadForUnreadColumns() throws Exception {
        coreTest(Level.INFO);
        List<LoggingEvent> logList = inMemoryLogger.getLogList();
        Assert.assertEquals("|ID |NAME     |LASTNAME |",logList.get(1).getMessage());
        Assert.assertEquals("|1  |two      |three    |",logList.get(3).getMessage());
        Assert.assertEquals("|5  |[unread] |[unread] |",logList.get(4).getMessage());

    }

    @Test
    public void testDebugLevelFetchesUnreadColumns() throws Exception {
        coreTest(Level.DEBUG);
        List<LoggingEvent> logList = inMemoryLogger.getLogList();
        Assert.assertEquals("|ID |NAME |LASTNAME |",logList.get(1).getMessage());
        Assert.assertEquals("|1  |two  |three    |",logList.get(3).getMessage());
        Assert.assertEquals("|5  |five |six      |",logList.get(4).getMessage());

    }

    
    private void coreTest(Level resultSetTableLogLevel) throws ClassNotFoundException, SQLException {

        Logger.getLogger("jdbc.resultsettable").setLevel(resultSetTableLogLevel);
        
        
        // Logger logger = LoggerFactory.getLogger(this.getClass());
        
        Class.forName(JDBC_DRIVER);
        
        // Or you can use a spring SingleConnectionDataSource
        // SingleConnectionDataSource realDataSource = new SingleConnectionDataSource(DriverManager.getConnection(JDBC_URL, TESTDB_USERNAME, TESTDB_PASSWORD), false);
        
        JdbcDataSource realDataSource = new JdbcDataSource();
        realDataSource.setURL(JDBC_URL);
        realDataSource.setUser(TESTDB_USERNAME);
        realDataSource.setPassword(TESTDB_PASSWORD);
        
        Log4jdbcProxyDataSource proxyDataSource = new Log4jdbcProxyDataSource(realDataSource);
        
        Connection proxyConnection = proxyDataSource.getConnection();
        Statement st = proxyConnection.createStatement(); 
        st.executeUpdate("CREATE TABLE test (id INTEGER, name char(50), last_name char(50), age INTEGER)"); 
        st.executeUpdate("insert into test values (1, 'two', 'three', 4)"); 
        st.executeUpdate("insert into test values (5, 'five', 'six', 7)"); 
        ResultSet rs = st.executeQuery("select id, NAME, last_name as LASTNAME from test");
        while (rs.next()) {
          String c1 = rs.getString(1);
          //System.out.print(c1);
          if (c1.equals("1")) {
            // Only if the first column is one do we bother reading the next two columns
            // (This allows us to test the difference between the jdbc.resultsettable log level debug and info)
            rs.getString(2);
            rs.getString(3);
            // System.out.println(" " + c2 + " " + c3);
          }
        }
        rs.close();
        proxyConnection.close();
        
    }

}


