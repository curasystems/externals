/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//provided by dcm4che-audit for use with versions before log4j-1.2.14
//package org.apache.log4j.net;
package org.dcm4che2.audit.log4j.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Locale;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Layout;
//import org.apache.log4j.helpers.SyslogWriter;
//import org.apache.log4j.helpers.SyslogQuietWriter;
import org.dcm4che2.audit.log4j.helpers.SyslogWriter;

// Contributors: Yves Bossel <ybossel@opengets.cl>
//               Christopher Taylor <cstaylor@pacbell.net>

/**
    Use SyslogAppender to send log messages to a remote syslog daemon.

    @author Ceki G&uuml;lc&uuml;
    @author Anders Kristensen
 */
public class SyslogAppender extends AppenderSkeleton {
  // The following constants are extracted from a syslog.h file
  // copyrighted by the Regents of the University of California
  // I hope nobody at Berkley gets offended.

  /** Kernel messages */
  final static public int LOG_KERN     = 0;
  /** Random user-level messages */
  final static public int LOG_USER     = 1<<3;
  /** Mail system */
  final static public int LOG_MAIL     = 2<<3;
  /** System daemons */
  final static public int LOG_DAEMON   = 3<<3;
  /** security/authorization messages */
  final static public int LOG_AUTH     = 4<<3;
  /** messages generated internally by syslogd */
  final static public int LOG_SYSLOG   = 5<<3;

  /** line printer subsystem */
  final static public int LOG_LPR      = 6<<3;
  /** network news subsystem */
  final static public int LOG_NEWS     = 7<<3;
  /** UUCP subsystem */
  final static public int LOG_UUCP     = 8<<3;
  /** clock daemon */
  final static public int LOG_CRON     = 9<<3;
  /** security/authorization  messages (private) */
  final static public int LOG_AUTHPRIV = 10<<3;
  /** ftp daemon */
  final static public int LOG_FTP      = 11<<3;

  // other codes through 15 reserved for system use
  /** reserved for local use */
  final static public int LOG_LOCAL0 = 16<<3;
  /** reserved for local use */
  final static public int LOG_LOCAL1 = 17<<3;
  /** reserved for local use */
  final static public int LOG_LOCAL2 = 18<<3;
  /** reserved for local use */
  final static public int LOG_LOCAL3 = 19<<3;
  /** reserved for local use */
  final static public int LOG_LOCAL4 = 20<<3;
  /** reserved for local use */
  final static public int LOG_LOCAL5 = 21<<3;
  /** reserved for local use */
  final static public int LOG_LOCAL6 = 22<<3;
  /** reserved for local use*/
  final static public int LOG_LOCAL7 = 23<<3;
  
  /** system is unusable */
  final static public int PRI_EMERGENCY = 0;
  /** action must be taken immediately */
  final static public int PRI_ALERT = 1;  
  /** critical conditions */
  final static public int PRI_CRITICAL = 2;
  /** error conditions */
  final static public int PRI_ERROR = 3;
  /** warning conditions */
  final static public int PRI_WARNING = 4;
  /** normal but significant condition */
  final static public int PRI_NOTICE = 5;
  /** informational messages */
  final static public int PRI_INFORMATIONAL = 6;
  /** debug-level messages */
  final static public int PRI_DEBUG = 7;

  protected static final int SYSLOG_HOST_OI = 0;
  protected static final int FACILITY_OI = 1;
  private static final String[] SHORT_MONTHS = {
          "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

  // Have LOG_USER as default
  int syslogFacility = LOG_USER;
  
  int fatalPriority = PRI_EMERGENCY;
  int errorPriority = PRI_ERROR;
  int warnPriority = PRI_WARNING;
  int infoPriority = PRI_NOTICE;
  int debugPriority = PRI_DEBUG;
  String encoding = "UTF-8";
 
  //SyslogTracerPrintWriter stp;
  SyslogWriter sw;
  String syslogHost;
  String localHostname;

  public
  SyslogAppender() {
      // empty default c'tor
  }

  public
  SyslogAppender(Layout layout, int syslogFacility) {
    this.layout = layout;
    this.syslogFacility = syslogFacility;
  }

  public
  SyslogAppender(Layout layout, String syslogHost, int syslogFacility) {
    this(layout, syslogFacility);
    setSyslogHost(syslogHost);
  }

  /**
     * Release any resources held by this SyslogAppender.
     * 
     * @since 0.8.4
     */
    @Override
    synchronized public void close() {
        closed = true;
        // A SyslogWriter is UDP based and needs no opening. Hence, it
        // can't be closed. We just unset the variables here.
        sw = null;
    }

  /**
     Returns the specified syslog facility as a lower-case String,
     e.g. "kern", "user", etc.
  */
  public
  static
  String getFacilityString(int syslogFacility) {
    switch(syslogFacility) {
    case LOG_KERN:      return "kern";
    case LOG_USER:      return "user";
    case LOG_MAIL:      return "mail";
    case LOG_DAEMON:    return "daemon";
    case LOG_AUTH:      return "auth";
    case LOG_SYSLOG:    return "syslog";
    case LOG_LPR:       return "lpr";
    case LOG_NEWS:      return "news";
    case LOG_UUCP:      return "uucp";
    case LOG_CRON:      return "cron";
    case LOG_AUTHPRIV:  return "authpriv";
    case LOG_FTP:       return "ftp";
    case LOG_LOCAL0:    return "local0";
    case LOG_LOCAL1:    return "local1";
    case LOG_LOCAL2:    return "local2";
    case LOG_LOCAL3:    return "local3";
    case LOG_LOCAL4:    return "local4";
    case LOG_LOCAL5:    return "local5";
    case LOG_LOCAL6:    return "local6";
    case LOG_LOCAL7:    return "local7";
    default:            return null;
    }
  }

  /**
     Returns the integer value corresponding to the named syslog
     facility, or -1 if it couldn't be recognized.

     @param facilityName one of the strings KERN, USER, MAIL, DAEMON,
            AUTH, SYSLOG, LPR, NEWS, UUCP, CRON, AUTHPRIV, FTP, LOCAL0,
            LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5, LOCAL6, LOCAL7.
            The matching is case-insensitive.

     @since 1.1
  */
  public
  static
  int getFacility(String facilityName) {
    if(facilityName != null) {
      facilityName = facilityName.trim();
    }
    if("KERN".equalsIgnoreCase(facilityName)) {
      return LOG_KERN;
    } else if("USER".equalsIgnoreCase(facilityName)) {
      return LOG_USER;
    } else if("MAIL".equalsIgnoreCase(facilityName)) {
      return LOG_MAIL;
    } else if("DAEMON".equalsIgnoreCase(facilityName)) {
      return LOG_DAEMON;
    } else if("AUTH".equalsIgnoreCase(facilityName)) {
      return LOG_AUTH;
    } else if("SYSLOG".equalsIgnoreCase(facilityName)) {
      return LOG_SYSLOG;
    } else if("LPR".equalsIgnoreCase(facilityName)) {
      return LOG_LPR;
    } else if("NEWS".equalsIgnoreCase(facilityName)) {
      return LOG_NEWS;
    } else if("UUCP".equalsIgnoreCase(facilityName)) {
      return LOG_UUCP;
    } else if("CRON".equalsIgnoreCase(facilityName)) {
      return LOG_CRON;
    } else if("AUTHPRIV".equalsIgnoreCase(facilityName)) {
      return LOG_AUTHPRIV;
    } else if("FTP".equalsIgnoreCase(facilityName)) {
      return LOG_FTP;
    } else if("LOCAL0".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL0;
    } else if("LOCAL1".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL1;
    } else if("LOCAL2".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL2;
    } else if("LOCAL3".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL3;
    } else if("LOCAL4".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL4;
    } else if("LOCAL5".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL5;
    } else if("LOCAL6".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL6;
    } else if("LOCAL7".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL7;
    } else {
      return -1;
    }
  }

  int priorityOf(Level level) {
      if (level.isGreaterOrEqual(Level.FATAL)) {
          return fatalPriority ;
      } else if (level.isGreaterOrEqual(Level.ERROR)) {
          return errorPriority;
      } else if (level.isGreaterOrEqual(Level.WARN)) {
          return warnPriority;
      } else if (level.isGreaterOrEqual(Level.INFO)) {
          return infoPriority;
      } else {
          return debugPriority;
      }
  }

  public String getLocalHostname() {
      try {
        InetAddress addr = InetAddress.getLocalHost();
        return skipDomain(addr.getHostName());
      } catch (UnknownHostException uhe) {
        return "UNKNOWN_HOST";
      }
    }

  private String skipDomain(String hostName) {
    int len = hostName.indexOf('.');
    return len > 0 && !Character.isDigit(hostName.charAt(0))
            ? hostName.substring(0, len) : hostName;
  }

public final int getFatalPriority() {
      return fatalPriority;
  }

  public final void setFatalPriority(int fatalPriority) {
      this.fatalPriority = fatalPriority;
  }

  public final int getErrorPriority() {
      return errorPriority;
  }

  public final void setErrorPriority(int errorPriority) {
      this.errorPriority = errorPriority;
  }

  public final int getWarnPriority() {
      return warnPriority;
  }

  public final void setWarnPriority(int warnPriority) {
      this.warnPriority = warnPriority;
  }

  public final int getInfoPriority() {
      return infoPriority;
  }

  public final void setInfoPriority(int infoPriority) {
      this.infoPriority = infoPriority;
  }  

  public final int getDebugPriority() {
      return debugPriority;
  }

  public final void setDebugPriority(int debugPriority) {
      this.debugPriority = debugPriority;
  }

  public final String getEncoding() {
    return encoding;
  }

  public final void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  @Override
  public
  void append(LoggingEvent event) {

    if(!isAsSevereAsThreshold(event.getLevel()))
      return;

    // We must not attempt to append if sqw is null.
    if(sw == null) {
      errorHandler.error("No syslog host is set for SyslogAppedender named \""+
                        this.name+"\".");
      return;
    }
    synchronized (sw) {
        Calendar now = Calendar.getInstance(Locale.ENGLISH);
        int pri = syslogFacility + priorityOf(event.getLevel());
        sw.write('<');
        sw.write(String.valueOf(pri));
        sw.write('>');
        sw.write(SHORT_MONTHS[now.get(Calendar.MONTH)]);
        sw.write(' ');
        writeNN(' ', now.get(Calendar.DAY_OF_MONTH));
        sw.write(' ');
        writeNN('0', now.get(Calendar.HOUR_OF_DAY));
        sw.write(':');
        writeNN('0', now.get(Calendar.MINUTE));
        sw.write(':');
        writeNN('0', now.get(Calendar.SECOND));
        sw.write(' ');
        sw.write(localHostname);
        sw.write(' ');
        sw.write(layout.format(event));
        try {
            sw.flush();
        } catch (IOException e) {
            errorHandler.error("Failed to emit UDP message to " + syslogHost, e, 
                             ErrorCode.WRITE_FAILURE);        
            sw.reset();
        }
    }
  }


  private void writeNN(char c, int n) {        
      if (n < 10) {
          sw.write(c);           
      }
      sw.write(String.valueOf(n));
  }
  

    /**
     * This method returns immediately as options are activated when they are
     * set.
     */
    @Override
    public void activateOptions() {
        this.sw = new SyslogWriter(syslogHost, encoding);
        this.localHostname = getLocalHostname();
    }

  /**
     * The SyslogAppender requires a layout. Hence, this method returns
     * <code>true</code>.
     * 
     * @since 0.8.4
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }

  /**
    The <b>SyslogHost</b> option is the name of the the syslog host
    where log output should go.  A non-default port can be specified by
    appending a colon and port number to a host name,
    an IPv4 address or an IPv6 address enclosed in square brackets.

    <b>WARNING</b> If the SyslogHost is not set, then this appender
    will fail.
   */
  public
  void setSyslogHost(final String syslogHost) {
    this.syslogHost = syslogHost;
  }

  /**
     Returns the value of the <b>SyslogHost</b> option.
   */
  public
  String getSyslogHost() {
    return syslogHost;
  }

  /**
     Set the syslog facility. This is the <b>Facility</b> option.

     <p>The <code>facilityName</code> parameter must be one of the
     strings KERN, USER, MAIL, DAEMON, AUTH, SYSLOG, LPR, NEWS, UUCP,
     CRON, AUTHPRIV, FTP, LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4,
     LOCAL5, LOCAL6, LOCAL7. Case is unimportant.

     @since 0.8.1 */
  public
  void setFacility(String facilityName) {
    if(facilityName == null)
      return;

    syslogFacility = getFacility(facilityName);
    if (syslogFacility == -1) {
      System.err.println("["+facilityName +
                  "] is an unknown syslog facility. Defaulting to [USER].");
      syslogFacility = LOG_USER;
    }
  }

  /**
     Returns the value of the <b>Facility</b> option.
   */
  public
  String getFacility() {
    return getFacilityString(syslogFacility);
  }

}