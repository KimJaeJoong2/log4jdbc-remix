package net.sf.log4jdbc;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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


public class ReturnGeneratedKeysTest {

    private static final String TESTDB_PASSWORD = "";
    private static final String TESTDB_USERNAME = "sa";
    private static final String JDBC_URL = "jdbc:h2:mem:db1";
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
    public void testGetGeneratedKeys() throws Exception {
        Logger.getLogger("jdbc.resultsettable").setLevel(Level.DEBUG);

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
		st.executeUpdate("CREATE TABLE test (id INTEGER IDENTITY, name char(50), last_name char(50), age INTEGER)"); 
		st.executeUpdate("insert into test(name, last_name, age) values ('two', 'three', 4)"); 
		
		String sql = "insert into test(name, last_name, age) values ('two', 'three', 4)";
		PreparedStatement stmt = proxyConnection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
		stmt.executeBatch();
		ResultSet generatedKeys = stmt.getGeneratedKeys();
		while (generatedKeys.next()) {
		    generatedKeys.getInt(1);
		}
		generatedKeys.close();
		proxyConnection.close();
        List<LoggingEvent> logList = inMemoryLogger.getLogList();
        Assert.assertEquals("|SCOPE_IDENTITY() |",logList.get(1).getMessage());
        Assert.assertEquals("|1                |",logList.get(3).getMessage());
    }

    

}


