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
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 * Damien Evans <damien.daddy@gmail.com>
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

package org.dcm4che2.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DICOM Part 15, Annex H compliant class, <code>NetworkConnection</code>
 * encapsulates the properties associated with a connection to a TCP/IP network.
 * <p>
 * The <i>network connection</i> describes one TCP port on one network device.
 * This can be used for a TCP connection over which a DICOM association can be
 * negotiated with one or more Network AEs. It specifies 8 the hostname and TCP
 * port number. A network connection may support multiple Network AEs. The
 * Network AE selection takes place during association negotiation based on the
 * called and calling AE-titles.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 13931 $ $Date: 2008-04-01 05:03:40 -0500 (Tue, 01 Apr
 *          2008) $
 * @since Nov 24, 2005
 */
public class NetworkConnection {
    static Logger log = LoggerFactory.getLogger(NetworkConnection.class);

    public static final int DEFAULT = 0;

    public static final int ONLY_ACTIVE = -1;

    private static final String[] TLS_NULL = { "SSL_RSA_WITH_NULL_SHA" };

    private static final String[] TLS_3DES_EDE_CBC = { "SSL_RSA_WITH_3DES_EDE_CBC_SHA" };

    private static final String[] TLS_AES_128_CBC = {
            "TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA" };

    private static String[] TLS_WO_SSLv2 = { "TLSv1", "SSLv3" };

    private static String[] TLS_AND_SSLv2 = { "TLSv1", "SSLv3", "SSLv2Hello" };

    private String commonName;

    private String hostname;

    private int port = ONLY_ACTIVE;

    private String[] tlsCipherSuite = {};

    private Boolean installed;

    private int backlog = 50;

    private int connectTimeout = 5000;

    private int requestTimeout = 5000;

    private int acceptTimeout = 5000;

    private int releaseTimeout = 5000;

    private int socketCloseDelay = 50;

    private int sendBufferSize = DEFAULT;

    private int receiveBufferSize = DEFAULT;

    private boolean tcpNoDelay = true;

    private boolean tlsNeedClientAuth = true;

    private String[] tlsProtocol = TLS_AND_SSLv2;

    private Device device;

    protected ServerSocket server;

    // Limiting factors
    private List<String> excludeConnectionsFrom;

    private int maxScpAssociations = 50;

    private AtomicInteger associationCount = new AtomicInteger();

    private InetAddress addr;


    private InetAddress addr() throws UnknownHostException {
        if (addr == null && hostname != null)
            addr = InetAddress.getByName(hostname);
        return addr;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("NetworkConnection[");
        sb.append(hostname).append(':').append(port);
        if (tlsCipherSuite.length != 0)
            sb.append(", TLS").append(Arrays.asList(tlsCipherSuite));
        if (installed != null)
            sb.append(", installed=").append(installed);
        if (commonName != null)
            sb.append(", cn=").append(commonName);
        sb.append(']');
        return sb.toString();
    }

    /**
     * Get the <code>Device</code> object that this Network Connection belongs
     * to.
     * 
     * @return Device
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Set the <code>Device</code> object that this Network Connection belongs
     * to.
     * 
     * @param device
     *                The owning <code>Device</code> object.
     */
    void setDevice(Device device) {
        this.device = device;
    }

    /**
     * This is the DNS name for this particular connection. This is used to
     * obtain the current IP address for connections. Hostname must be
     * sufficiently qualified to be unambiguous for any client DNS user.
     * 
     * @return A String containing the host name.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * This is the DNS name for this particular connection. This is used to
     * obtain the current IP address for connections. Hostname must be
     * sufficiently qualified to be unambiguous for any client DNS user.
     * 
     * @param hostname
     *                A String containing the host name.
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * An arbitrary name for the Network Connections object. Can be a meaningful
     * name or any unique sequence of characters.
     * 
     * @return A String containing the name.
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * An arbitrary name for the Network Connections object. Can be a meaningful
     * name or any unique sequence of characters.
     * 
     * @param name
     *                A String containing the name.
     */
    public void setCommonName(String name) {
        this.commonName = name;
    }

    /**
     * The TCP port that the AE is listening on. (This may be missing for a
     * network connection that only initiates associations.)
     * 
     * @return An int containing the port number.
     */
    public int getPort() {
        return port;
    }

    /**
     * The TCP port that the AE is listening on.
     * 
     * A valid port value is between 0 and 65535, or {@link #ONLY_ACTIVE} for
     * network connection that only initiates associations.
     * 
     * A port number of <code>zero</code> will let the system pick up an
     * ephemeral port.
     * 
     * @param port
     *                The port number
     */
    public void setPort(int port) {
        if (port < ONLY_ACTIVE || port > 0xFFFF) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        this.port = port;
    }

    /**
     * The TLS CipherSuites that are supported on this particular connection.
     * TLS CipherSuites shall be described using an RFC-2246 string
     * representation (e.g. 'SSL_RSA_WITH_3DES_EDE_CBC_SHA')
     * 
     * @return A String array containing the supported cipher suites
     */
    public String[] getTlsCipherSuite() {
        return tlsCipherSuite.clone();
    }

    /**
     * The TLS CipherSuites that are supported on this particular connection.
     * TLS CipherSuites shall be described using an RFC-2246 string
     * representation (e.g. 'SSL_RSA_WITH_3DES_EDE_CBC_SHA')
     * 
     * @param tlsCipherSuite
     *                A String array containing the supported cipher suites
     */
    public void setTlsCipherSuite(String[] tlsCipherSuite) {
        checkNotNull("tlsCipherSuite", tlsCipherSuite);
        this.tlsCipherSuite = tlsCipherSuite.clone();
    }

    private static void checkNotNull(String name, Object[] a) {
        if (a == null)
            throw new NullPointerException(name);
        for (int i = 0; i < a.length; i++)
            if (a[i] == null)
                throw new NullPointerException(name + '[' + i + ']');
    }

    public void setTlsWithoutEncyrption() {
        this.tlsCipherSuite = TLS_NULL;
    }

    public void setTls3DES_EDE_CBC() {
        this.tlsCipherSuite = TLS_3DES_EDE_CBC;
    }

    public void setTlsAES_128_CBC() {
        this.tlsCipherSuite = TLS_AES_128_CBC;
    }

    /**
     * True if the Network Connection is installed on the network. If not
     * present, information about the installed status of the Network Connection
     * is inherited from the device.
     * 
     * @return boolean True if the NetworkConnection is installed on the
     *         network.
     */
    public boolean isInstalled() {
        return installed != null ? installed.booleanValue() : device == null
                || device.isInstalled();
    }

    /**
     * True if the Network Connection is installed on the network. If not
     * present, information about the installed status of the Network Connection
     * is inherited from the device.
     * 
     * @param installed
     *                True if the NetworkConnection is installed on the network.
     */
    public void setInstalled(boolean installed) {
        this.installed = Boolean.valueOf(installed);
    }

    public boolean isListening() {
        return port != ONLY_ACTIVE;
    }

    public boolean isTLS() {
        return tlsCipherSuite.length > 0;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        if (backlog < 1)
            throw new IllegalArgumentException("backlog: " + backlog);
        this.backlog = backlog;
    }

    public int getAcceptTimeout() {
        return acceptTimeout;
    }

    public void setAcceptTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.acceptTimeout = timeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.connectTimeout = timeout;
    }

    /**
     * Timeout in ms for receiving A-ASSOCIATE-RQ, 5000 by default
     * 
     * @param An
     *                int value containing the milliseconds.
     */
    public int getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Timeout in ms for receiving A-ASSOCIATE-RQ, 5000 by default
     * 
     * @param timeout
     *                An int value containing the milliseconds.
     */
    public void setRequestTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.requestTimeout = timeout;
    }

    /**
     * Timeout in ms for receiving A-RELEASE-RP, 5000 by default.
     * 
     * @return An int value containing the milliseconds.
     */
    public int getReleaseTimeout() {
        return releaseTimeout;
    }

    /**
     * Timeout in ms for receiving A-RELEASE-RP, 5000 by default.
     * 
     * @param timeout
     *                An int value containing the milliseconds.
     */
    public void setReleaseTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.releaseTimeout = timeout;
    }

    /**
     * Delay in ms for Socket close after sending A-ABORT, 50ms by default.
     * 
     * @return An int value containing the milliseconds.
     */
    public int getSocketCloseDelay() {
        return socketCloseDelay;
    }

    /**
     * Delay in ms for Socket close after sending A-ABORT, 50ms by default.
     * 
     * @param delay
     *                An int value containing the milliseconds.
     */
    public void setSocketCloseDelay(int delay) {
        if (delay < 0)
            throw new IllegalArgumentException("delay: " + delay);
        this.socketCloseDelay = delay;
    }

    /**
     * Get the SO_RCVBUF socket value in KB.
     * 
     * @return An int value containing the buffer size in KB.
     */
    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * Set the SO_RCVBUF socket option to specified value in KB.
     * 
     * @param bufferSize
     *                An int value containing the buffer size in KB.
     */
    public void setReceiveBufferSize(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size: " + size);
        this.receiveBufferSize = size;
    }

    /**
     * Get the SO_SNDBUF socket option value in KB,
     * 
     * @return An int value containing the buffer size in KB.
     */
    public int getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * Set the SO_SNDBUF socket option to specified value in KB,
     * 
     * @param bufferSize
     *                An int value containing the buffer size in KB.
     */
    public void setSendBufferSize(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size: " + size);
        this.sendBufferSize = size;
    }

    /**
     * Determine if this network connection is using Nagle's algorithm as part
     * of its network communication.
     * 
     * @return boolean True if TCP no delay (disable Nagle's algorithm) is used.
     */
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Set whether or not this network connection should use Nagle's algorithm
     * as part of its network communication.
     * 
     * @param tcpNoDelay
     *                boolean True if TCP no delay (disable Nagle's algorithm) should be
     *                used.
     */
    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isTlsNeedClientAuth() {
        return tlsNeedClientAuth;
    }

    public void setTlsNeedClientAuth(boolean tlsNeedClientAuth) {
        this.tlsNeedClientAuth = tlsNeedClientAuth;
    }

    public String[] getTlsProtocol() {
        return tlsProtocol.clone();
    }

    public void setTlsProtocol(String[] tlsProtocol) {
        this.tlsProtocol = tlsProtocol.clone();
    }

    public void enableSSLv2Hello() {
        this.tlsProtocol = TLS_AND_SSLv2;
    }

    public void disableSSLv2Hello() {
        this.tlsProtocol = TLS_WO_SSLv2;
    }

    private InetSocketAddress getEndPoint() throws UnknownHostException {
        return new InetSocketAddress(addr(), port);
    }

    private InetSocketAddress getBindPoint() throws UnknownHostException {
        // don't use loopback address as bind point to avoid
        // ConnectionException connection to remote endpoint
        return new InetSocketAddress(maskLoopBackAddress(addr()), 0);
    }

    private static InetAddress maskLoopBackAddress(InetAddress addr) {
         return addr != null && addr.isLoopbackAddress() ? null : addr;
    }


    /**
     * Returns server socket associated with this Network Connection, bound to
     * the TCP port, listening for connect requests. Returns <code>null</code>
     * if this network connection only initiates associations or was not yet
     * bound by {@link #bind}.
     * 
     * @return server socket associated with this Network Connection or
     *         <code>null</code>
     */
    public ServerSocket getServer() {
        return server;
    }

    /**
     * Create a socket as an SCU and connect to a peer network connection (the
     * SCP).
     * 
     * @param peerConfig
     *                The peer <code>NetworkConnection</code> object that this
     *                network connection is connecting to.
     * @return Socket The created socket object.
     * @throws IOException
     *                 If the connection cannot be made due to network IO
     *                 reasons.
     */
    public Socket connect(NetworkConnection peerConfig) throws IOException {
        if (device == null)
            throw new IllegalStateException("Device not initalized");
        if (!peerConfig.isListening())
            throw new IllegalArgumentException("Only initiates associations - "
                    + peerConfig);
        Socket s = isTLS() ? createTLSSocket() : new Socket();
        InetSocketAddress bindPoint = getBindPoint();
        InetSocketAddress endpoint = peerConfig.getEndPoint();
        log.debug("Initiate connection from {} to {}", bindPoint, endpoint);
        s.bind(bindPoint);
        setSocketOptions(s);
        s.connect(endpoint, connectTimeout);
        return s;
    }

    /**
     * Set options on a socket that was either just accepted (if this network
     * connection is an SCP), or just created (if this network connection is an
     * SCU).
     * 
     * @param s
     *                The <code>Socket</code> object.
     * @throws SocketException
     *                 If the options cannot be set on the socket.
     */
    protected void setSocketOptions(Socket s) throws SocketException {
        int size;
        size = s.getReceiveBufferSize();
        if (receiveBufferSize == DEFAULT) {
            receiveBufferSize = size;
        }
        else if (receiveBufferSize != size) {
            s.setReceiveBufferSize(receiveBufferSize);
            receiveBufferSize = s.getReceiveBufferSize();
        }
        size = s.getSendBufferSize();
        if (sendBufferSize == DEFAULT) {
            sendBufferSize = size;
        }
        else if (sendBufferSize != size) {
            s.setSendBufferSize(sendBufferSize);
            sendBufferSize = s.getSendBufferSize();
        }
        if (s.getTcpNoDelay() != tcpNoDelay) {
            s.setTcpNoDelay(tcpNoDelay);
        }
    }

    /**
     * Bind this network connection to a TCP port and start a server socket
     * accept loop.
     * 
     * @param executor
     *                The <code>Executor</code> implementation that
     *                association threads should run within. The executor
     *                determines the threading model.
     * @throws IOException
     *                 If there is a problem with the network interaction.
     */
    public synchronized void bind(final Executor executor) throws IOException {
        if (device == null)
            throw new IllegalStateException("Device not initalized");
        if (!isListening())
            throw new IllegalStateException("Only initiates associations - "
                    + this);
        if (server != null)
            throw new IllegalStateException("Already listening - " + server);
        server = isTLS() ? createTLSServerSocket() : new ServerSocket();
        server.bind(getEndPoint(), backlog);
        executor.execute(new Runnable() {

            public void run() {
                SocketAddress sockAddr = server.getLocalSocketAddress();
                log.info("Start listening on {}", sockAddr);
                try {
                    for (;;) {
                        log.debug("Wait for connection on {}", sockAddr);
                        Socket s = server.accept();
                        setSocketOptions(s);
                        if (checkConnection(s)) {
                            Association a = Association.accept(s,
                                    NetworkConnection.this);
                            executor.execute(a);
                        }
                    }
                }
                catch (Throwable e) {
                    // assume exception was raised by graceful stop of server
                }
                log.info("Stop listening on {}", sockAddr);
            }
        });
    }

    /**
     * Check the incoming socket connection against the limitations set up for
     * this Network Connection.
     * 
     * @param s
     *                The socket connection.
     * @return boolean True if association negotiation should proceed.
     */
    protected boolean checkConnection(Socket s) {
        if (excludeConnectionsFrom == null
                || excludeConnectionsFrom.size() == 0)
            return true;

        // Check to see if this connection attempt is just a keep alive
        // ping from the CSS. Use a list of possible pingers in the case
        // of a high-availability network.
        for (String ip : excludeConnectionsFrom) {
            if (s.getInetAddress().getHostAddress().equals(ip)) {
                log.debug("Rejecting connection from {}", ip);
                try {
                    s.close();
                } catch (IOException e) {
                    log.debug("Caught IOException closing socket from {}", ip);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Increment the number of active associations.
     */
    protected void incListenerConnectionCount() {
        associationCount.incrementAndGet();
    }

    /**
     * Decrement the number of active associations.
     */
    protected void decListenerConnectionCount() {
        for (;;) {
            int current = associationCount.get();
            if (current <= 0
                    || associationCount.compareAndSet(current, current-1))
                return;
        }
    }

    /**
     * Check to see if the specified number of associations has been exceeded.
     * 
     * @param maxAssociations
     *                An int containing the maximum number of associations
     *                allowed.
     * @return boolean True if the max association count has not been exceeded.
     */
    public boolean checkConnectionCountWithinLimit() {
        return associationCount.intValue() <= maxScpAssociations;
    }

    public synchronized void unbind() {
        if (server == null)
            return;
        try {
            server.close();
        }
        catch (Throwable e) {
            // Ignore errors when closing the server socket.
        }
        associationCount.set(0);
        server = null;
    }

    protected Socket createTLSSocket() throws IOException {
        SSLContext sslContext = device.getSSLContext();
        if (sslContext == null)
            throw new IllegalStateException("TLS Context not initialized!");
        SSLSocketFactory sf = sslContext.getSocketFactory();
        SSLSocket s = (SSLSocket) sf.createSocket();
        s.setEnabledProtocols(tlsProtocol);
        s.setEnabledCipherSuites(tlsCipherSuite);
        return s;
    }

    protected ServerSocket createTLSServerSocket() throws IOException {
        SSLContext sslContext = device.getSSLContext();
        if (sslContext == null)
            throw new IllegalStateException("TLS Context not initialized!");
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
        ss.setEnabledProtocols(tlsProtocol);
        ss.setEnabledCipherSuites(tlsCipherSuite);
        ss.setNeedClientAuth(tlsNeedClientAuth);
        return ss;
    }

    /**
     * Get a list of IP addresses from which we should ignore connections.
     * Useful in an environment that utilizes a load balancer. In the case of a
     * TCP ping from a load balancing switch, we don't want to spin off a new
     * thread and try to negotiate an association.
     * 
     * @return Returns the list of IP addresses which should be ignored.
     */
    public List<String> getExcludeConnectionsFrom() {
        return excludeConnectionsFrom;
    }

    /**
     * Set a list of IP addresses from which we should ignore connections.
     * Useful in an environment that utilizes a load balancer. In the case of a
     * TCP ping from a load balancing switch, we don't want to spin off a new
     * thread and try to negotiate an association.
     * 
     * @param excludeConnectionsFrom
     *                the list of IP addresses which should be ignored.
     */
    public void setExcludeConnectionsFrom(List<String> excludeConnectionsFrom) {
        this.excludeConnectionsFrom = excludeConnectionsFrom;
    }

    /**
     * Get the maximum number of incoming associations that this Network
     * Connection will allow.
     * 
     * @return int An int which defines the max associations.
     */
    public int getMaxScpAssociations() {
        return maxScpAssociations;
    }

    /**
     * Set the maximum number of incoming associations that this Network
     * Connection will allow.
     * 
     * @param maxScpAssociations
     *                An int which defines the max associations.
     */
    public void setMaxScpAssociations(int maxListenerAssociations) {
        this.maxScpAssociations = maxListenerAssociations;
    }

}
