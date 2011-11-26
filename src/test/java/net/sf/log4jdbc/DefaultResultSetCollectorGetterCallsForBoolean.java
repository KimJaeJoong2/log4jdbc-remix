package net.sf.log4jdbc;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lazyluke.log4jdbcremix.test.tools.SimpleInMemoryLog4jAppender;


public class DefaultResultSetCollectorGetterCallsForBoolean {

    private static final String INSERT4 = "insert into test values (4, 'four', 'five', 8.4, 44444.444444, false, DATE '2004-12-04', TIMESTAMP '2014-12-01 01:02:03')";
	private static final String INSERT3 = "insert into test values (3, 'three', 'four', 7.3, 33333.333333, true, DATE '2003-11-03', TIMESTAMP '2013-11-01 01:02:03.1234')";
	private static final String INSERT2 = "insert into test values (2, 'two', 'three', 6.2, 22222.222222, false, DATE '1902-02-02', TIMESTAMP '1912-01-01 01:02:03')";
	private static final String INSERT1 = "insert into test values (1, 'one', 'two', 5.1, 11111.111111, true, DATE '1901-01-01', TIMESTAMP '1911-01-01 01:02:03.1234')";
	private static final String CREATE_TABLE = "CREATE TABLE test (id INTEGER, name char(10), last_name varchar(20), age FLOAT, money DOUBLE, alive BOOLEAN, dob DATE, lastScan DATETIME)";
	private static final String TESTDB_PASSWORD = "";
    private static final String TESTDB_USERNAME = "sa";
    private static final String JDBC_URL = "jdbc:h2:mem:db1;MODE=Oracle";
    private static final String JDBC_DRIVER = "org.h2.Driver";

    private static SimpleInMemoryLog4jAppender inMemoryLogger;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
    public void testExecuteBatchAndGetters() throws Exception {
        coreTest(Level.INFO);
        assertEquals(CREATE_TABLE, line(0));
        assertTrue(line(1), line(1).contains(INSERT1));
        assertTrue(line(1), line(1).contains(INSERT2));
        assertTrue(line(1), line(1).contains(INSERT3));
        assertTrue(line(1), line(1).contains(INSERT4));
        assertEquals("|ID |NAME  |LAST_NAME |AGE |MONEY        |ALIVE |DOB        |LASTSCAN                 |",line(4));
        assertEquals("|1  |one   |two       |5.1 |11111.111111 |true  |1901-01-01 |1911-01-01 01:02:03.1234 |",line(6));
        assertEquals("|2  |two   |three     |6.2 |22222.222222 |false |1902-02-02 |1912-01-01 01:02:03.0    |",line(7));
        assertEquals("|3  |three |four      |7.3 |33333.333333 |true  |2003-11-03 |2013-11-01 01:02:03.1234 |",line(8));
        assertEquals("|4  |four  |five      |8.4 |44444.444444 |false |2004-12-04 |2014-12-01 01:02:03.0    |",line(9));
    }

	private String line(int line) {
		return inMemoryLogger.getLogList().get(line).getMessage().toString().replaceAll("\n", "").trim();
	}
    
    private void coreTest(Level resultSetTableLogLevel) throws ClassNotFoundException, SQLException {

        Logger.getLogger("jdbc.resultsettable").setLevel(resultSetTableLogLevel);
        Logger.getLogger("jdbc.sqlonly").setLevel(resultSetTableLogLevel);        
        
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
        st.executeUpdate(CREATE_TABLE);
        st.close();
        st = proxyConnection.createStatement();
        st.addBatch(INSERT1); 
	    st.addBatch(INSERT2); 
	    st.addBatch(INSERT3); 
	    st.addBatch(INSERT4);
        int[] updateCounts = st.executeBatch();
        assertEquals(4, updateCounts.length);
        assertEquals(1, updateCounts[0]);
        ResultSet rs = st.executeQuery("select id, name, last_name, age, money, alive, dob, lastScan  from test");
        while (rs.next()) {
          rs.getInt(1);
          rs.getString(2);
          rs.getString(3);
          rs.getFloat(4);
          rs.getDouble(5);
          rs.getBoolean(6);
          rs.getDate(7);
          rs.getTimestamp(8);
        }
        rs.close();
        proxyConnection.close();
        
    }

}


