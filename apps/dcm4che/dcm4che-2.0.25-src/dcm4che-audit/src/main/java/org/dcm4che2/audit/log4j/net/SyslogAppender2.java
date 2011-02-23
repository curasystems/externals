/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che2.audit.log4j.net;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.dcm4che2.audit.util.SSLUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 * @version $Revision$ $Date$
 * @since Nov 25, 2009
 */
public class SyslogAppender2 extends AppenderSkeleton {

    private enum Facility {
        KERN,     //  0 kernel messages
        USER,     //  1 user-level messages
        MAIL,     //  2 mail system
        DAEMON,   //  3 system daemons
        AUTH,     //  4 security/authorization messages
        SYSLOG,   //  5 messages generated internally by syslogd
        LPR,      //  6 line printer subsystem
        NEWS,     //  7 network news subsystem
        UUCP,     //  8 UUCP subsystem
        CRON,     //  9 clock daemon
        AUTHPRIV, // 10 security/authorization messages
        FTP,      // 11 FTP daemon
        NTP,      // 12 NTP subsystem
        AUDIT,    // 13 log audit
        ALERT,    // 14 log alert
        CRON2,    // 15 clock daemon (note 2)
        LOCAL0,   // 16 local use 0  (local0)
        LOCAL1,   // 17 local use 1  (local1)
        LOCAL2,   // 18 local use 2  (local2)
        LOCAL3,   // 19 local use 3  (local3)
        LOCAL4,   // 20 local use 4  (local4)
        LOCAL5,   // 21 local use 5  (local5)
        LOCAL6,   // 22 local use 6  (local6)
        LOCAL7    // 23 local use 7  (local7)
    }

    private enum Severity {
        EMERGENCY,     // 0 Emergency: system is unusable
        ALERT,         // 1 Alert: action must be taken immediately
        CRITICAL,      // 2 Critical: critical conditions
        ERROR,         // 3 Error: error conditions
        WARNING,       // 4 Warning: warning conditions
        NOTICE,        // 5 Notice: normal but significant condition
        INFORMATIONAL, // 6 Informational: informational messages
        DEBUG          // 7 Debug: debug-level messages
    }

    private enum Protocol { 
        UDP {
            @Override
            void init(SyslogAppender2 app) {
                app.initDatagramSocket();
            }

            @Override
            void send(SyslogAppender2 app) throws IOException {
                app.sendDatagramPacket();
            }

            @Override
            void close(SyslogAppender2 app) {
                app.closeDatagramSocket();
            }
        }, 

        TCP {
            @Override
            void init(SyslogAppender2 app) {
                app.initSocketFactory();
            }

            @Override
            void send(SyslogAppender2 app) throws IOException {
                app.writeToSocket();
            }

            @Override
            void close(SyslogAppender2 app) {
                app.closeSocket();
            }
        };

        abstract void init(SyslogAppender2 app);
        abstract void send(SyslogAppender2 app) throws IOException;
        abstract void close(SyslogAppender2 app);
    }

    private String host = "localhost";
    private int port = 514;
    private String bindAddress = "0.0.0.0";
    private int localPort = 0;
    private Protocol protocol = Protocol.UDP;
    private int sendBuffer = 0;
    private int tcpConnectTimeout = 300;
    private int tcpRetryInterval = 60000;
    private boolean tcpNoDelay = true;
    private boolean tlsEnabled = false;
    private String tlsProtocol = "TLSv1";
    private String[] tlsCiphers = { "TLS_RSA_WITH_AES_128_CBC_SHA" };
    private String keyStoreFile;
    private char[] keyStorePass;
    private char[] keyPass;
    private String keyStoreType = "JKS";
    private String trustStoreFile;
    private char[] trustStorePass;
    private String trustStoreType = "JKS";

    private Facility syslogFacility = Facility.AUTHPRIV;
    private Severity fatalSeverity = Severity.EMERGENCY;
    private Severity errorSeverity = Severity.ERROR;
    private Severity warnSeverity = Severity.WARNING;
    private Severity infoSeverity = Severity.NOTICE;
    private Severity debugSeverity = Severity.DEBUG;
    private String applicationName;
    private String messageID;
    private boolean timestampInUTC = false;
    private boolean prefixMessageWithBOM = true;

    private String hostName;
    private String processID;
    private Calendar calendar;
    private Buffer buf = new Buffer();
    private SocketAddress bindaddr;
    private SocketAddress addr;
    private DatagramSocket ds;
    private SocketFactory socketFactory;
    private Socket sock;
    private OutputStream sockout;
    private long retryConnectAt;

    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public String getBindAddress() {
        return bindAddress;
    }


    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }


    public int getLocalPort() {
        return localPort;
    }


    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }


    public String getProtocol() {
        return protocol.toString();
    }


    public void setProtocol(String protocol) {
        this.protocol = Protocol.valueOf(protocol.toUpperCase());
    }


    public int getSendBuffer() {
        return sendBuffer;
    }


    public void setSendBuffer(int sendBuffer) {
        this.sendBuffer = sendBuffer;
    }


    public int getTcpConnectTimeout() {
        return tcpConnectTimeout;
    }


    public void setTcpConnectTimeout(int tcpConnectTimeout) {
        this.tcpConnectTimeout = tcpConnectTimeout;
    }


    public int getTcpRetryInterval() {
        return tcpRetryInterval;
    }


    public void setTcpRetryInterval(int tcpRetryInterval) {
        this.tcpRetryInterval = tcpRetryInterval;
    }


    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }


    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }


    public boolean isTlsEnabled() {
        return tlsEnabled;
    }


    public void setTlsEnabled(boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }


    public String getTlsProtocol() {
        return tlsProtocol;
    }


    public void setTlsProtocol(String tlsProtocol) {
        this.tlsProtocol = tlsProtocol;
    }


    public String getTlsCiphers() {
        return toString(tlsCiphers);
    }


    public void setTlsCiphers(String tlsCiphers) {
        this.tlsCiphers = split(tlsCiphers);
    }


    private static String toString(String[] ss) {
        if (ss.length == 0)
            return "";

        if (ss.length == 1)
            return ss[0];

        int iMax = ss.length - 1;
        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(String.valueOf(ss[i]));
            if (i == iMax)
                return b.toString();
            b.append(", ");
        }
     }


    private static String[] split(String s) {
        StringTokenizer stk = new StringTokenizer(s, " ,");
        int count = stk.countTokens();
        if (count == 0)
            throw new IllegalArgumentException(s);
        String[] ss = new String[count];
        for (int i = 0; i < ss.length; i++)
            ss[i] = stk.nextToken();
        return ss;
    }


    public String getKeyStoreFile() {
        return keyStoreFile;
    }


    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }


    public String getKeyStorePass() {
        return new String(keyStorePass);
    }


    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass.toCharArray();
    }


    public String getKeyPass() {
        return new String(keyPass);
    }


    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass.toCharArray();
    }


    public String getKeyStoreType() {
        return keyStoreType;
    }


    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }


    public String getTrustStoreFile() {
        return trustStoreFile;
    }


    public void setTrustStoreFile(String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }


    public String getTrustStorePass() {
        return new String(trustStorePass);
    }


    public void setTrustStorePass(String trustStorePass) {
        this.trustStorePass = trustStorePass.toCharArray();
    }


    public String getTrustStoreType() {
        return trustStoreType;
    }


    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }


    public String getSyslogFacility() {
        return syslogFacility.toString();
    }


    public void setSyslogFacility(String facility) {
        this.syslogFacility = Facility.valueOf(facility.toUpperCase());
    }


    public String getFatalSeverity() {
        return fatalSeverity.toString();
    }


    public void setFatalSeverity(String severity) {
        this.fatalSeverity = Severity.valueOf(severity.toUpperCase());
    }


    public String getErrorSeverity() {
        return errorSeverity.toString();
    }


    public void setErrorSeverity(String severity) {
        this.errorSeverity = Severity.valueOf(severity.toUpperCase());
    }


    public String getWarnSeverity() {
        return warnSeverity.toString();
    }


    public void setWarnSeverity(String severity) {
        this.warnSeverity = Severity.valueOf(severity.toUpperCase());
    }


    public String getInfoSeverity() {
        return infoSeverity.toString();
    }


    public void setInfoSeverity(String severity) {
        this.infoSeverity = Severity.valueOf(severity.toUpperCase());
    }


    public String getDebugSeverity() {
        return debugSeverity.toString();
    }


    public void setDebugSeverity(String severity) {
        this.debugSeverity = Severity.valueOf(severity.toUpperCase());
    }


    public String getHostName() {
        return hostName;
    }


    public void setHostName(String hostname) {
        this.hostName = hostname.length() > 0 ? hostname : null;
    }


    public String getApplicationName() {
        return applicationName;
    }


    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }


    public String getMessageID() {
        return messageID;
    }


    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }


    public boolean isTimestampInUTC() {
        return timestampInUTC;
    }


    public void setTimestampInUTC(boolean timestampInUTC) {
        this.timestampInUTC = timestampInUTC;
    }


    public boolean isPrefixMessageWithBOM() {
        return prefixMessageWithBOM;
    }


    public void setPrefixMessageWithBOM(boolean prefixMessageWithBOM) {
        this.prefixMessageWithBOM = prefixMessageWithBOM;
    }


    @Override
    public void activateOptions() {
        initCalendar();
        initProcessID();
        initHostName();
        initConnection();
    }


    private void initCalendar() {
        calendar = timestampInUTC 
            ? Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH)
            : Calendar.getInstance(Locale.ENGLISH);
    }


    private void initProcessID() {
        String s = ManagementFactory.getRuntimeMXBean().getName();
        int atPos = s.indexOf('@');
        processID = atPos > 0 ? s.substring(0, atPos) : s;
    }


    private void initHostName() {
        if (hostName == null)
            try {
                hostName = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                errorHandler.error("Failed to detect local host name", e,
                        ErrorCode.GENERIC_FAILURE);
            }
    }

    private void initConnection() {
        bindaddr = new InetSocketAddress(bindAddress, localPort);
        addr = new InetSocketAddress(host, port);
        protocol.init(this);
    }

    @Override
    protected void append(LoggingEvent event) {
        try {
            buf.reset();
            writeHeader(event);
            writeSP();
            writeStructuredData();
            writeSP();
            if (prefixMessageWithBOM)
                writeBOM();
            writeString(event.getRenderedMessage());
            protocol.send(this);
        } catch (IOException e) {
            errorHandler.error("Failed to emit message by " + protocol 
                    + " connection to " + host + ":" + port, e,
                    ErrorCode.WRITE_FAILURE, event);
        }
    }


    private void writeHeader(LoggingEvent event) {
        writePRI(event.getLevel());
        writeVersion();
        writeSP();
        writeTimeStamp(event.timeStamp);
        writeSP();
        writeString(hostName);
        writeSP();
        writeString(applicationName);
        writeSP();
        writeString(processID);
        writeSP();
        writeString(messageID);
    }


    private void writeVersion() {
        buf.write('1');
    }


    private void writeStructuredData() {
        writeNIL();  // no structured data
    }


    private void writeSP() {
        buf.write(' ');
    }

    private void writeNIL() {
        buf.write('-');
    }

    private void writePRI(Level level) {
        buf.write('<');
        writeNumber(buf, prival(level));
        buf.write('>');
    }


    private int prival(Level level) {
        return (syslogFacility.ordinal() << 3) | severityOf(level).ordinal();
    }

    private Severity severityOf(Level level) {
        if (level.isGreaterOrEqual(Level.FATAL)) {
            return fatalSeverity ;
        } else if (level.isGreaterOrEqual(Level.ERROR)) {
            return errorSeverity;
        } else if (level.isGreaterOrEqual(Level.WARN)) {
            return warnSeverity;
        } else if (level.isGreaterOrEqual(Level.INFO)) {
            return infoSeverity;
        } else {
            return debugSeverity;
        }
    }

    private void writeTimeStamp(long time) {
        calendar.setTimeInMillis(time);
        writeNumber(buf, calendar.get(Calendar.YEAR), 4);
        buf.write('-');
        writeNumber(buf, calendar.get(Calendar.MONTH) + 1, 2);
        buf.write('-');
        writeNumber(buf, calendar.get(Calendar.DAY_OF_MONTH), 2);
        buf.write('T');
        writeNumber(buf, calendar.get(Calendar.HOUR_OF_DAY), 2);
        buf.write(':');
        writeNumber(buf, calendar.get(Calendar.MINUTE), 2);
        buf.write(':');
        writeNumber(buf, calendar.get(Calendar.SECOND), 2);
        buf.write('.');
        writeNumber(buf, calendar.get(Calendar.MILLISECOND), 3);
        if (timestampInUTC)
            buf.write('Z');
        else
            writeTimezone((calendar.get(Calendar.ZONE_OFFSET)
                    + calendar.get(Calendar.DST_OFFSET)) / 60000);
    }

    private void writeTimezone(int tzoff) {
        if (tzoff < 0) {
            tzoff = -tzoff;
            buf.write('-');
        } else {
            buf.write('+');
        }
        int hh = tzoff / 60;
        int mm = tzoff - (hh << 6) + (hh << 2) ; // mm = tzoff - hh * 60
        writeNumber(buf, hh, 2);
        buf.write(':');
        writeNumber(buf, mm, 2);
    }

    private void writeString(String s) {
        if (s == null)
            writeNIL();
        else
            try {
                buf.write(s.getBytes("UTF-8"));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
    }

    private void writeNumber(OutputStream out, int d) {
       writeNumber(out, d, stringSize(d));
    }

    private static int stringSize(int d) {
        if (d < 10)
            return 1;
        if (d < 100)
            return 2;
        if (d < 1000)
            return 3;
        if (d < 10000)
            return 4;
        d /= 10000;
        int n = 5;
        while (d > 9) {
            d /= 10;
            n++;
        }
        return n;
    }

    private void writeNumber(OutputStream out, int d, int w) {
        int q, n;
        switch (w) {
        case 4:
            writeDigit(out, q = d / 1000);
            d -= (q << 10) - (q << 4) - (q << 3); // d -= q * 1000
            //$FALL-THROUGH$
        case 3:
            writeDigit(out, q = d / 100);
            d -= (q << 7) + (q << 2) - (q << 5); // d -= q * 100
            //$FALL-THROUGH$
        case 2:
            writeDigit(out, q = d / 10);
            d -= (q << 3) + (q << 1); // d -= q * 10
            //$FALL-THROUGH$
        case 1:
            writeDigit(out, d);
            break;
        default:
            n = 10000;
            w -= 4;
            while (--w > 0)
                n = (n << 3) + (n << 1); // n *= 10
            while (n > 1) {
                writeDigit(out, q = d / n);
                d -= q * n;
                n /= 10;
            }
            writeDigit(out, d);
        }
    }

    private static final int[] DIGITS = { 
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    private void writeDigit(OutputStream out, int d) {
        try {
            out.write(DIGITS[d]);
        } catch (IOException e) {
            // should never happen because writes always in byte buffer
            throw new AssertionError(e);
        }
    }

    private void writeBOM() {
        buf.write(0xEF);
        buf.write(0xBB);
        buf.write(0xBF);
    }


    @Override
    public void close() {
        synchronized (this) {
            if (closed)
                return;
            closed = true;
        }
        protocol.close(this);
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    private class Buffer extends ByteArrayOutputStream {
        void writeTo(DatagramSocket ds) throws IOException {
            ds.send(new DatagramPacket(buf, count, addr));
        }
    }

    private void initDatagramSocket() {
        try {
            this.ds = new DatagramSocket(bindaddr);
        } catch (SocketException e) {
            errorHandler.error("Failed to create datagram socket bound to "
                    + bindaddr, e, ErrorCode.GENERIC_FAILURE);
            return;
        }
        if (sendBuffer > 0)
            try {
                ds.setSendBufferSize(sendBuffer);
            } catch (SocketException e) {
                errorHandler.error("Failed to set SO_SNDBUF option to "
                        + sendBuffer + " for datagram socket " + ds, 
                        e, ErrorCode.GENERIC_FAILURE);
            }
    }

    private void sendDatagramPacket() throws IOException {
        if (buf != null)
            buf.writeTo(ds);
    }

    private void closeDatagramSocket() {
        ds.close();
        ds = null;
    }


    private void initSocketFactory() {
        if (tlsEnabled)
            try {
                socketFactory = getSSLContext().getSocketFactory();
            } catch (Exception e) {
                errorHandler.error(
                        "Failed to configure TLS context from key store "
                        + keyStoreFile + " and trust store " + trustStoreFile,
                        e, ErrorCode.GENERIC_FAILURE);
            }
        else
            socketFactory = SocketFactory.getDefault();
    }


    private SSLContext getSSLContext() throws Exception {
        KeyStore keystore = SSLUtils.loadKeyStore(
                keyStoreFile, keyStorePass, keyStoreType);
        KeyStore truststore = SSLUtils.loadKeyStore(
                trustStoreFile, trustStorePass, trustStoreType);
        return SSLUtils.getSSLContext(keystore, keyPass, truststore, null);
    }


    private void writeToSocket() throws IOException {
        if (socketFactory == null)
            return;

        if (sock != null) {
            try {
                doWriteToSocket();
                return;
            } catch (IOException e) {
                close();
                retryConnectAt = 0;
            }
        }

        if (retryConnectAt > 0
                && retryConnectAt > System.currentTimeMillis()) {
            return;
        }

        try {
            connect();
        } catch (IOException e) {
            retryConnectAt = System.currentTimeMillis()
                    + tcpRetryInterval;
            throw e;
        }
        doWriteToSocket();
    }


    private void connect() throws IOException {
        Socket tmp = null;
        try {
            tmp = socketFactory.createSocket();
            tmp.setTcpNoDelay(tcpNoDelay);
            if (sendBuffer > 0)
                tmp.setSendBufferSize(sendBuffer);
            tmp.bind(bindaddr);
            if (tmp instanceof SSLSocket) {
                SSLSocket sslsock = (SSLSocket) tmp;
                sslsock.setEnabledProtocols(new String[] { tlsProtocol });
                sslsock.setEnabledCipherSuites(tlsCiphers);
            }
            tmp.connect(addr, tcpConnectTimeout);
            if (tmp instanceof SSLSocket) {
                SSLSocket sslsock = (SSLSocket) tmp;
                sslsock.startHandshake();
            }
            sockout = new BufferedOutputStream(tmp.getOutputStream());
            sock = tmp;
            tmp = null;
        } finally {
            if (tmp != null)
                try { tmp.close(); } catch (Exception ignore) { /* ignore */ }
        }
    }


    private void doWriteToSocket() throws IOException {
        writeNumber(sockout, buf.size());
        sockout.write(' ');
        buf.writeTo(sockout);
        sockout.flush();
    }


    private void closeSocket() {
        try { sockout.close(); } catch (Exception ignore) { /* ignore */ }
        try { sock.close(); } catch (Exception ignore) { /* ignore */ }
        sockout = null;
        sock = null;
    }
}
