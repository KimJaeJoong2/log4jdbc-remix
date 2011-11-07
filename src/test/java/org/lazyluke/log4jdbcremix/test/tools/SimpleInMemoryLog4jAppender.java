package org.lazyluke.log4jdbcremix.test.tools;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

public class SimpleInMemoryLog4jAppender extends AppenderSkeleton  {

private List<LoggingEvent> logList = new LinkedList<LoggingEvent>();

public List<LoggingEvent> getLogList() {
  return logList;
}

public synchronized void close() {
  if (this.closed) {
    return;
  }
  this.closed = true;
}

public void clear() {
  logList = new LinkedList<LoggingEvent>();
}

public boolean requiresLayout() {
  return false;
}

protected boolean checkEntryConditions() {
  if (this.closed) {
    LogLog.warn("Not allowed to write to a closed appender.");
    return false;
  }
  return true;
}


public void append(LoggingEvent event) {
  if (!checkEntryConditions()) {
     return;
  }
  logList.add(event);
  System.out.println(event.getMessage());
}

}