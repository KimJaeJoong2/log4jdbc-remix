# 2013-11-06 log4jdbc-remix is dead! long live log4jdbc-log4j2! #

log4jdbc-log4j2 is another fork of log4jdbc-remix (and log4jdbc) that basically finishes the job that log4jdbc-remix started.

# You are strongly recommended to look at log4jdbc-log4j2 rather than log4jdbc-remix #

https://code.google.com/p/log4jdbc-log4j2/

The code and documentation for log4jdbc-remix will be left active for now.
.
.
.
.
.
.
.
.
.
.


# What was log4jdbc-remix? #

log4jdbc-remix was an experimental fork of log4jdbc that:
  * Can log result sets as tables
  * Can be configured as a Spring Datasource
  * Can use a plugable SQL formatter
  * Is available in the sonatype maven repository. https://oss.sonatype.org/index.html#nexus-search;quick~log4jdbc
  * Only supports Java 6 and above (log4jdbc which also supports java 1.4 and 1.5)
  * Its now superseded by log4jdbc-log4j2 at https://code.google.com/p/log4jdbc-log4j2/ which also supports JDBC 4.1 (Java 7), JDBC 4 (Java 6), JDBC 3 (Java 5). The original log4jdbc also supports java 1.4.



## Where could I get it? ##

Via maven:
Stable release is 0.2.7
```
<dependency>
  <groupId>org.lazyluke</groupId>
  <artifactId>log4jdbc-remix</artifactId>
  <version>0.2.7</version>
</dependency>
```
Or for the bleeding edge:
```
<dependency>
  <groupId>org.lazyluke</groupId>
  <artifactId>log4jdbc-remix</artifactId>
  <version>0.2.8-SNAPSHOT</version>
</dependency>
```


Or you could download the jars/source from sonatype: https://oss.sonatype.org/index.html#nexus-search;quick~log4jdbc-remix

Or you could checkout the source from subversion: See: http://code.google.com/p/log4jdbc-remix/source/checkout

## The log4j.xml config ##

As per log4jdbc but with an additional parameters possible:
```
  <!-- log4jdbc option  log the jdbc results as a table --> 
  <logger name="jdbc.resultsettable" additivity="false"> 
    <level value="info" /> 
    <appender-ref ref="console-log4jdbc" /> 
  </logger> 
```
level debug will fill in unread values in the result set.

Example output:
```
select *
	from EMP
|------|-------|----------|-----|----------------------|-----|-----|-------|
|EMPNO |ENAME  |JOB       |MGR  |HIREDATE              |SAL  |COMM |DEPTNO |
|------|-------|----------|-----|----------------------|-----|-----|-------|
|7369  |SMITH  |CLERK     |7902 |1980-12-17 00:00:00.0 |800  |null |20     |
|7499  |ALLEN  |SALESMAN  |7698 |1981-02-20 00:00:00.0 |1600 |300  |30     |
|7521  |WARD   |SALESMAN  |7698 |1981-02-22 00:00:00.0 |1250 |500  |30     |
|7566  |JONES  |MANAGER   |7839 |1981-04-02 00:00:00.0 |2975 |null |20     |
|7654  |MARTIN |SALESMAN  |7698 |1981-09-28 00:00:00.0 |1250 |1400 |30     |
|7698  |BLAKE  |MANAGER   |7839 |1981-05-01 00:00:00.0 |2850 |null |30     |
|7782  |CLARK  |MANAGER   |7839 |1981-06-09 00:00:00.0 |2450 |null |10     |
|7788  |SCOTT  |ANALYST   |7566 |1987-04-19 00:00:00.0 |3000 |null |20     |
|7839  |KING   |PRESIDENT |null |1981-11-17 00:00:00.0 |5000 |null |10     |
|7844  |TURNER |SALESMAN  |7698 |1981-09-08 00:00:00.0 |1500 |0    |30     |
|7876  |ADAMS  |CLERK     |7788 |1987-05-23 00:00:00.0 |1100 |null |20     |
|7900  |JAMES  |CLERK     |7698 |1981-12-03 00:00:00.0 |950  |null |30     |
|7902  |FORD   |ANALYST   |7566 |1981-12-03 00:00:00.0 |3000 |null |20     |
|7934  |MILLER |CLERK     |7782 |1982-01-23 00:00:00.0 |1300 |null |10     |
|------|-------|----------|-----|----------------------|-----|-----|-------|
```

## Restrictions on result set logging ##
log4jdbc-remix does not warn about bi-directional result sets. It does
not deal with previous(), first(), last() etc, just next(). There is
no warning either so duplicate result rows would be seen if, for
example, previous() followed by next() is used by the application
(although I've not tested this). It would theoretically possible to
deal with this scenario or at least give a warning about an
unsupported operation. I just didn't need to do it in my application.

## Custom sql formatter ##
The modified SpyLogFactory makes it possible for any
application to set a  custom sql formatter, so that would be a useful change to log4jdb IMO.
There is an example custom Slf4jSpyLogDelegator called Log4JdbcCustomFormatter in package net.sf.log4jdbc.tools. Thats just an example.

To configure a Spring Datasource to use Log4jdbc-remix: If you have

```
  <bean id="dataSource" class="...">
    <property name="driverClass" value="${datasource.driverClassName}"/>
    <property name="jdbcUrl" value="${datasource.url}"/>
    <property name="user" value="${datasource.username}"/>
    <property name="password" value="${datasource.password}"/>
    ...
  </bean>
```
Change this to
```
  <bean id="dataSourceSpied" class="...">
    <property name="driverClass" value="${datasource.driverClassName}"/>
    <property name="jdbcUrl" value="${datasource.url}"/>
    <property name="user" value="${datasource.username}"/>
    <property name="password" value="${datasource.password}"/>
    ...
  </bean>

  <bean id="dataSource" class="net.sf.log4jdbc.Log4jdbcProxyDataSource">
    <constructor-arg ref="dataSourceSpied" />
  </bean>
```

If you want to define your own custom sql formatter, you can do it like this, where Log4JdbcCustomFormatter is an example custom formatter :
```
  <bean id="dataSource" class="net.sf.log4jdbc.Log4jdbcProxyDataSource">
    <constructor-arg ref="dataSourceSpied" />
    <property name="logFormatter"> 
      <bean class="net.sf.log4jdbc.tools.Log4JdbcCustomFormatter"> 
        <property name="loggingType" value="MULTI_LINE" /> 
        <property name="margin" value="19" /> 
        <property name="sqlPrefix" value="SQL:::" /> 
      </bean> 
    </property> 
  </bean>
```