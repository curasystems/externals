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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2007
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

package org.dcm4che2.tool.dcmhpqr;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.UserIdentity;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 13905 $ $Date: 2010-08-19 17:01:12 +0200 (Thu, 19 Aug 2010) $
 * @since Mar 18, 2006
 * 
 */
public class DcmHPQR {

    private static final int KB = 1024;

    private static final String USAGE = "dcmhpqr <aet>[@<host>[:<port>]] [Options]";

    private static final String DESCRIPTION = 
            "Query specified remote Application Entity (=Hanging Protocol SCP) "
            + "and optional (s. option -dest) also retrieve matching HP instances. "
            + "If <port> is not specified, DICOM default port 104 is assumed. "
            + "If also no <host> is specified localhost is assumed.\n"
            + "Options:";

    private static final String EXAMPLE = 
            "\nExample: dcmhpqr HPSCP@localhost:11112 "
            + "-qHangingProtocolName NeurosurgeryPlan -user T1234:HOSP1\n"
            + "=> Query Application Entity HPSCP listening on local port 11112 "
            + "for hanging protocol with name 'NeurosurgeryPlan' and user with "
            + "code 'T1234:HOSP1'.";

    private static String[] TLS1 = { "TLSv1" };

    private static String[] SSL3 = { "SSLv3" };

    private static String[] NO_TLS1 = { "SSLv3", "SSLv2Hello" };

    private static String[] NO_SSL2 = { "TLSv1", "SSLv3" };

    private static String[] NO_SSL3 = { "TLSv1", "SSLv2Hello" };

    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };
    
    private static final int[] RETURN_KEYS = { Tag.SOPClassUID,
            Tag.SOPInstanceUID, Tag.HangingProtocolName,
            Tag.HangingProtocolDescription, Tag.HangingProtocolLevel,
            Tag.HangingProtocolCreator, Tag.HangingProtocolCreationDateTime,
            Tag.NumberOfPriorsReferenced, Tag.HangingProtocolUserGroupName,
            Tag.NumberOfScreens };

    private static final int[] HP_DEF_SEQ_RETURN_KEYS = { Tag.Modality,
            Tag.Laterality };

    private static final int[] MOVE_KEYS = { Tag.SOPInstanceUID };

    private static final String[] IVRLE_TS = { UID.ImplicitVRLittleEndian };

    private static final String[] LE_TS = { UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian };

    private final Executor executor;

    private final NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();

    private final NetworkConnection remoteConn = new NetworkConnection();

    private final Device device;

    private final NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private final NetworkConnection conn = new NetworkConnection();

    private Association assoc;

    private int priority = 0;

    private int cancelAfter = Integer.MAX_VALUE;

    private final DicomObject keys = new BasicDicomObject();

    private final DicomObject hpDefKeys = new BasicDicomObject();

    private final DicomObject anatomicRegKeys = new BasicDicomObject();

    private final DicomObject procCodeKeys = new BasicDicomObject();

    private final DicomObject reasonForReqProcKeys = new BasicDicomObject();

    private final DicomObject hpUserIDCodeKeys = new BasicDicomObject();

    private String moveDest;

    private int completed;

    private int warning;

    private int failed;

    private String keyStoreURL = "resource:tls/test_sys_1.p12";
    
    private char[] keyStorePassword = SECRET; 

    private char[] keyPassword; 
    
    private String trustStoreURL = "resource:tls/mesa_certs.jks";
    
    private char[] trustStorePassword = SECRET; 
    
    public DcmHPQR(String name) {
        device = new Device(name);
        executor = new NewThreadExecutor(name);
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAETitle(name);
        for (int i = 0; i < RETURN_KEYS.length; i++) {
            keys.putNull(RETURN_KEYS[i], null);
        }
        keys.putNestedDicomObject(Tag.HangingProtocolDefinitionSequence,
                hpDefKeys);
        for (int i = 0; i < HP_DEF_SEQ_RETURN_KEYS.length; i++) {
            hpDefKeys.putNull(HP_DEF_SEQ_RETURN_KEYS[i], null);
        }
        hpDefKeys.putNestedDicomObject(Tag.AnatomicRegionSequence,
                anatomicRegKeys);
        hpDefKeys.putNestedDicomObject(Tag.ProcedureCodeSequence, procCodeKeys);
        hpDefKeys.putNestedDicomObject(
                Tag.ReasonForRequestedProcedureCodeSequence,
                reasonForReqProcKeys);
        keys.putNestedDicomObject(
                Tag.HangingProtocolUserIdentificationCodeSequence,
                hpUserIDCodeKeys);
        keys.putNestedDicomObject(Tag.NominalScreenDefinitionSequence,
                new BasicDicomObject());
    }

    public final void setLocalHost(String hostname) {
        conn.setHostname(hostname);
    }

    public final void setRemoteHost(String hostname) {
        remoteConn.setHostname(hostname);
    }

    public final void setRemotePort(int port) {
        remoteConn.setPort(port);
    }

    public final void setTlsProtocol(String[] tlsProtocol) {
        conn.setTlsProtocol(tlsProtocol);
    }

    public final void setTlsWithoutEncyrption() {
        conn.setTlsWithoutEncyrption();
        remoteConn.setTlsWithoutEncyrption();
    }

    public final void setTls3DES_EDE_CBC() {
        conn.setTls3DES_EDE_CBC();
        remoteConn.setTls3DES_EDE_CBC();
    }

    public final void setTlsAES_128_CBC() {
        conn.setTlsAES_128_CBC();
        remoteConn.setTlsAES_128_CBC();
    }
    
    public final void setTlsNeedClientAuth(boolean needClientAuth) {
        conn.setTlsNeedClientAuth(needClientAuth);
    }  

    public final void setKeyStoreURL(String url) {
        keyStoreURL = url;
    }
    
    public final void setKeyStorePassword(String pw) {
        keyStorePassword = pw.toCharArray();
    }
    
    public final void setKeyPassword(String pw) {
        keyPassword = pw.toCharArray();
    }
    
    public final void setTrustStorePassword(String pw) {
        trustStorePassword = pw.toCharArray();
    }
    
    public final void setTrustStoreURL(String url) {
        trustStoreURL = url;
    }

    public final void setCalledAET(String called) {
        remoteAE.setAETitle(called);
    }

    public final void setCalling(String calling) {
        ae.setAETitle(calling);
    }

    public final void setUserIdentity(UserIdentity userIdentity) {
        ae.setUserIdentity(userIdentity);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setConnectTimeout(int connectTimeout) {
        conn.setConnectTimeout(connectTimeout);
    }

    public final void setMaxPDULengthReceive(int maxPDULength) {
        ae.setMaxPDULengthReceive(maxPDULength);
    }

    public final void setPackPDV(boolean packPDV) {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period) {
        device.setAssociationReaperPeriod(period);
    }

    public final void setDimseRspTimeout(int timeout) {
        ae.setDimseRspTimeout(timeout);
    }

    public final void setTcpNoDelay(boolean tcpNoDelay) {
        conn.setTcpNoDelay(tcpNoDelay);
    }

    public final void setAcceptTimeout(int timeout) {
        conn.setAcceptTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout) {
        conn.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int timeout) {
        conn.setSocketCloseDelay(timeout);
    }

    public final void setMaxPDULengthSend(int maxPDULength) {
        ae.setMaxPDULengthSend(maxPDULength);
    }

    public final void setReceiveBufferSize(int bufferSize) {
        conn.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize) {
        conn.setSendBufferSize(bufferSize);
    }

    public final void setCancelAfter(int limit) {
        this.cancelAfter = limit;
    }

    public void addMatchingKey(int[] tagPath, String value) {
        keys.putString(tagPath, null, value);
    }

    public void addReturnKey(int[] tagPath) {
        keys.putNull(tagPath, null);
    }

    public void addHPDefMatchingKey(int tag, String value) {
        hpDefKeys.putString(tag, null, value);
    }

    public void addAnatomicRegMatchingKey(int tag, String value) {
        anatomicRegKeys.putString(tag, null, value);
    }

    public void addProcCodeMatchingKey(int tag, String value) {
        procCodeKeys.putString(tag, null, value);
    }

    public void addReasonForReqProcMatchingKey(int tag, String value) {
        reasonForReqProcKeys.putString(tag, null, value);
    }

    public void addHPUserIDCodeMatchingKey(int tag, String value) {
        hpUserIDCodeKeys.putString(tag, null, value);
    }

    public void setMoveDest(String aet) {
        moveDest = aet;
    }

    public boolean isMove() {
        return moveDest != null;
    }

    public final int getFailed() {
        return failed;
    }

    public final int getWarning() {
        return warning;
    }

    private final int getTotalRetrieved() {
        return completed + warning;
    }

    public void setTransferSyntax(String[] ts) {
        TransferCapability[] tc = new TransferCapability[2];
        tc[0] = new TransferCapability(UID.HangingProtocolInformationModelFIND,
                ts, TransferCapability.SCU);
        tc[1] = new TransferCapability(UID.HangingProtocolInformationModelMOVE,
                ts, TransferCapability.SCU);
        ae.setTransferCapability(tc);
    }

    public void open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public void close() throws InterruptedException {
        assoc.release(true);
    }

    public List<DicomObject> query() throws IOException, InterruptedException {
        TransferCapability tc = assoc
                .getTransferCapabilityAsSCU(UID.HangingProtocolInformationModelFIND);
        if (tc == null) {
            throw new NoPresentationContextException(
                    "Hanging Protocol Query not supported by "
                            + remoteAE.getAETitle());
        }
        System.out.println("Send Query Request:");
        System.out.println(keys.toString());
        DimseRSP rsp = assoc.cfind(UID.HangingProtocolInformationModelFIND,
                priority, keys, tc.getTransferSyntax()[0], cancelAfter);
        List<DicomObject> result = new ArrayList<DicomObject>();
        while (rsp.next()) {
            DicomObject cmd = rsp.getCommand();
            if (CommandUtils.isPending(cmd)) {
                DicomObject data = rsp.getDataset();
                result.add(data);
                System.out.println("\nReceived Query Response #"
                        + result.size() + ":");
                System.out.println(data.toString());
            }
        }
        return result;
    }

    public void move(List<DicomObject> findResults)
            throws IOException, InterruptedException {
        if (moveDest == null)
            throw new IllegalStateException("moveDest == null");
        TransferCapability tc = assoc
                .getTransferCapabilityAsSCU(UID.HangingProtocolInformationModelMOVE);
        if (tc == null)
            throw new NoPresentationContextException(
                    "Hanging Protocol Retrieve not supported by "
                            + remoteAE.getAETitle());
        String cuid = tc.getSopClass();
        for (int i = 0, n = Math.min(findResults.size(), cancelAfter); i < n; ++i) {
            DicomObject keys = findResults.get(i).subSet(MOVE_KEYS);
            System.out.println("Send Retrieve Request using "
                    + UIDDictionary.getDictionary().prompt(cuid) + ":");
            System.out.println(keys.toString());
            DimseRSPHandler rspHandler = new DimseRSPHandler() {
                @Override
                public void onDimseRSP(Association as, DicomObject cmd,
                        DicomObject data) {
                    DcmHPQR.this.onMoveRSP(as, cmd, data);
                }
            };
            assoc.cmove(cuid, priority, keys, tc.getTransferSyntax()[0],
                    moveDest, rspHandler);
        }
        assoc.waitForDimseRSP();
    }

    protected void onMoveRSP(Association as, DicomObject cmd, DicomObject data) {
        if (!CommandUtils.isPending(cmd)) {
            completed += cmd.getInt(Tag.NumberOfCompletedSuboperations);
            warning += cmd.getInt(Tag.NumberOfWarningSuboperations);
            failed += cmd.getInt(Tag.NumberOfFailedSuboperations);
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = parse(args);
        DcmHPQR dcmhpqr = new DcmHPQR(cl.hasOption("device") 
                ? cl.getOptionValue("device") : "DCMHPQR");
        final List<String> argList = cl.getArgList();
        String remoteAE = argList.get(0);
        String[] calledAETAddress = split(remoteAE, '@');
        dcmhpqr.setCalledAET(calledAETAddress[0]);
        if (calledAETAddress[1] == null) {
            dcmhpqr.setRemoteHost("127.0.0.1");
            dcmhpqr.setRemotePort(104);
        } else {
            String[] hostPort = split(calledAETAddress[1], ':');
            dcmhpqr.setRemoteHost(hostPort[0]);
            dcmhpqr.setRemotePort(toPort(hostPort[1]));
        }
        if (cl.hasOption("L")) {
            String localAE = cl.getOptionValue("L");
            String[] callingAETHost = split(localAE, '@');
            dcmhpqr.setCalling(callingAETHost[0]);
            if (callingAETHost[1] != null) {
                dcmhpqr.setLocalHost(callingAETHost[1]);
            }
        }
        if (cl.hasOption("username")) {
            String username = cl.getOptionValue("username");
            UserIdentity userId;
            if (cl.hasOption("passcode")) {
                String passcode = cl.getOptionValue("passcode");
                userId = new UserIdentity.UsernamePasscode(username,
                        passcode.toCharArray());
            } else {
                userId = new UserIdentity.Username(username);
            }
            userId.setPositiveResponseRequested(cl.hasOption("uidnegrsp"));
            dcmhpqr.setUserIdentity(userId);
        }
        if (cl.hasOption("connectTO"))
            dcmhpqr.setConnectTimeout(parseInt(cl.getOptionValue("connectTO"),
                    "illegal argument of option -connectTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("reaper"))
            dcmhpqr.setAssociationReaperPeriod(parseInt(cl
                            .getOptionValue("reaper"),
                            "illegal argument of option -reaper", 1,
                            Integer.MAX_VALUE));
        if (cl.hasOption("rspTO"))
            dcmhpqr.setDimseRspTimeout(parseInt(cl.getOptionValue("rspTO"),
                    "illegal argument of option -rspTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("acceptTO"))
            dcmhpqr.setAcceptTimeout(parseInt(cl.getOptionValue("acceptTO"),
                    "illegal argument of option -acceptTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("releaseTO"))
            dcmhpqr.setReleaseTimeout(parseInt(cl.getOptionValue("releaseTO"),
                    "illegal argument of option -releaseTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("soclosedelay"))
            dcmhpqr.setSocketCloseDelay(parseInt(cl
                    .getOptionValue("soclosedelay"),
                    "illegal argument of option -soclosedelay", 1, 10000));
        if (cl.hasOption("rcvpdulen"))
            dcmhpqr.setMaxPDULengthReceive(parseInt(cl
                    .getOptionValue("rcvpdulen"),
                    "illegal argument of option -rcvpdulen", 1, 10000)
                    * KB);
        if (cl.hasOption("sndpdulen"))
            dcmhpqr.setMaxPDULengthSend(parseInt(
                    cl.getOptionValue("sndpdulen"),
                    "illegal argument of option -sndpdulen", 1, 10000)
                    * KB);
        if (cl.hasOption("sosndbuf"))
            dcmhpqr.setSendBufferSize(parseInt(cl.getOptionValue("sosndbuf"),
                    "illegal argument of option -sosndbuf", 1, 10000)
                    * KB);
        if (cl.hasOption("sorcvbuf"))
            dcmhpqr.setReceiveBufferSize(parseInt(
                    cl.getOptionValue("sorcvbuf"),
                    "illegal argument of option -sorcvbuf", 1, 10000)
                    * KB);
        dcmhpqr.setPackPDV(!cl.hasOption("pdv1"));
        dcmhpqr.setTcpNoDelay(!cl.hasOption("tcpdelay"));
        if (cl.hasOption("C"))
            dcmhpqr.setCancelAfter(parseInt(cl.getOptionValue("C"),
                    "illegal argument of option -C", 1, Integer.MAX_VALUE));
        if (cl.hasOption("lowprior"))
            dcmhpqr.setPriority(CommandUtils.LOW);
        if (cl.hasOption("highprior"))
            dcmhpqr.setPriority(CommandUtils.HIGH);
        if (cl.hasOption("q")) {
            String[] matchingKeys = cl.getOptionValues("q");
            for (int i = 1; i < matchingKeys.length; i++, i++)
                dcmhpqr.addMatchingKey(Tag.toTagPath(matchingKeys[i - 1]),
                        matchingKeys[i]);
        }
        if (cl.hasOption("r")) {
            String[] returnKeys = cl.getOptionValues("r");
            for (int i = 0; i < returnKeys.length; i++)
                dcmhpqr.addReturnKey(Tag.toTagPath(returnKeys[i]));
        }
        if (cl.hasOption("mod")) {
            dcmhpqr.addHPDefMatchingKey(Tag.Modality, cl.getOptionValue("mod"));
        }
        if (cl.hasOption("lat")) {
            dcmhpqr.addHPDefMatchingKey(Tag.Laterality, cl.getOptionValue("lat"));
        }
        if (cl.hasOption("ar")) {
            String[] anatomicRegion = split(cl.getOptionValue("ar"), ':');
            dcmhpqr.addAnatomicRegMatchingKey(Tag.CodeValue, anatomicRegion[0]);
            dcmhpqr.addAnatomicRegMatchingKey(Tag.CodingSchemeDesignator,
                    anatomicRegion[1]);
        }
        if (cl.hasOption("pc")) {
            String[] procCode = split(cl.getOptionValue("pc"), ':');
            dcmhpqr.addProcCodeMatchingKey(Tag.CodeValue, procCode[0]);
            dcmhpqr.addProcCodeMatchingKey(Tag.CodingSchemeDesignator, procCode[1]);
        }
        if (cl.hasOption("rfrp")) {
            String[] reasonForReqProc = split(cl.getOptionValue("rfrp"), ':');
            dcmhpqr.addReasonForReqProcMatchingKey(Tag.CodeValue, reasonForReqProc[0]);
            dcmhpqr.addReasonForReqProcMatchingKey(Tag.CodingSchemeDesignator,
                    reasonForReqProc[1]);
        }
        if (cl.hasOption("user")) {
            String[] hpUserIDCode = split(cl.getOptionValue("user"), ':');
            dcmhpqr.addHPUserIDCodeMatchingKey(Tag.CodeValue, hpUserIDCode[0]);
            dcmhpqr.addHPUserIDCodeMatchingKey(Tag.CodingSchemeDesignator,
                    hpUserIDCode[1]);
        }
        if (cl.hasOption("dest")) {
            dcmhpqr.setMoveDest(cl.getOptionValue("dest"));
        }

        dcmhpqr.setTransferSyntax(cl.hasOption("ivrle") ? IVRLE_TS : LE_TS);
        

        if (cl.hasOption("tls")) {
            String cipher = cl.getOptionValue("tls");
            if ("NULL".equalsIgnoreCase(cipher)) {
                dcmhpqr.setTlsWithoutEncyrption();
            } else if ("3DES".equalsIgnoreCase(cipher)) {
                dcmhpqr.setTls3DES_EDE_CBC();
            } else if ("AES".equalsIgnoreCase(cipher)) {
                dcmhpqr.setTlsAES_128_CBC();
            } else {
                exit("Invalid parameter for option -tls: " + cipher);
            }
            if (cl.hasOption("tls1")) {
                dcmhpqr.setTlsProtocol(TLS1);
            } else if (cl.hasOption("ssl3")) {
                dcmhpqr.setTlsProtocol(SSL3);
            } else if (cl.hasOption("no_tls1")) {
                dcmhpqr.setTlsProtocol(NO_TLS1);
            } else if (cl.hasOption("no_ssl3")) {
                dcmhpqr.setTlsProtocol(NO_SSL3);
            } else if (cl.hasOption("no_ssl2")) {
                dcmhpqr.setTlsProtocol(NO_SSL2);
            }
            dcmhpqr.setTlsNeedClientAuth(!cl.hasOption("noclientauth"));
            if (cl.hasOption("keystore")) {
                dcmhpqr.setKeyStoreURL(cl.getOptionValue("keystore"));
            }
            if (cl.hasOption("keystorepw")) {
                dcmhpqr.setKeyStorePassword(
                        cl.getOptionValue("keystorepw"));
            }
            if (cl.hasOption("keypw")) {
                dcmhpqr.setKeyPassword(cl.getOptionValue("keypw"));
            }
            if (cl.hasOption("truststore")) {
                dcmhpqr.setTrustStoreURL(
                        cl.getOptionValue("truststore"));
            }
            if (cl.hasOption("truststorepw")) {
                dcmhpqr.setTrustStorePassword(
                        cl.getOptionValue("truststorepw"));
            }
            long t1 = System.currentTimeMillis();
            try {
                dcmhpqr.initTLS();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to initialize TLS context:"
                        + e.getMessage());
                System.exit(2);
            }
            long t2 = System.currentTimeMillis();
            System.out.println("Initialize TLS context in "
                    + ((t2 - t1) / 1000F) + "s");
        }        
        
        long t1 = System.currentTimeMillis();
        try {
            dcmhpqr.open();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to establish association:");
            e.printStackTrace(System.err);
            System.exit(2);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Connected to " + remoteAE + " in "
                + ((t2 - t1) / 1000F) + "s");

        try {
            List<DicomObject> result = dcmhpqr.query();
            long t3 = System.currentTimeMillis();
            System.out.println("Received " + result.size()
                    + " matching entries in " + ((t3 - t2) / 1000F) + "s");
            if (dcmhpqr.isMove()) {
                dcmhpqr.move(result);
                long t4 = System.currentTimeMillis();
                System.out.println("Retrieved " + dcmhpqr.getTotalRetrieved()
                        + " objects (warning: " + dcmhpqr.getWarning()
                        + ", failed: " + dcmhpqr.getFailed() + ") in "
                        + ((t4 - t3) / 1000F) + "s");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            dcmhpqr.close();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Released connection to " + remoteAE);
    }

    private static CommandLine parse(String[] args) {
        Options opts = new Options();

        OptionBuilder.withArgName("name");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set device name, use DCMHPQR by default");
        opts.addOption(OptionBuilder.create("device"));

        OptionBuilder.withArgName("aet[@host]");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("set AET and local address of local "
                + "Application Entity, use device name and pick up any valid "
                + "local address to bind the socket by default");
        opts.addOption(OptionBuilder.create("L"));
        
        OptionBuilder.withArgName("username");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "enable User Identity Negotiation with specified username and "
                + " optional passcode");
        opts.addOption(OptionBuilder.create("username"));

        OptionBuilder.withArgName("passcode");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "optional passcode for User Identity Negotiation, "
                + "only effective with option -username");
        opts.addOption(OptionBuilder.create("passcode"));

        opts.addOption("uidnegrsp", false,
                "request positive User Identity Negotation response, "
                + "only effective with option -username");
        
        OptionBuilder.withArgName("NULL|3DES|AES");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "enable TLS connection without, 3DES or AES encryption");
        opts.addOption(OptionBuilder.create("tls"));

        OptionGroup tlsProtocol = new OptionGroup();
        tlsProtocol.addOption(new Option("tls1",
                "disable the use of SSLv3 and SSLv2 for TLS connections"));
        tlsProtocol.addOption(new Option("ssl3",
                "disable the use of TLSv1 and SSLv2 for TLS connections"));
        tlsProtocol.addOption(new Option("no_tls1",
                "disable the use of TLSv1 for TLS connections"));
        tlsProtocol.addOption(new Option("no_ssl3",
                "disable the use of SSLv3 for TLS connections"));
        tlsProtocol.addOption(new Option("no_ssl2",
                "disable the use of SSLv2 for TLS connections"));
        opts.addOptionGroup(tlsProtocol);

        opts.addOption("noclientauth", false,
                "disable client authentification for TLS");

        OptionBuilder.withArgName("file|url");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "file path or URL of P12 or JKS keystore, resource:tls/test_sys_1.p12 by default");
        opts.addOption(OptionBuilder.create("keystore"));

        OptionBuilder.withArgName("password");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "password for keystore file, 'secret' by default");
        opts.addOption(OptionBuilder.create("keystorepw"));

        OptionBuilder.withArgName("password");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "password for accessing the key in the keystore, keystore password by default");
        opts.addOption(OptionBuilder.create("keypw"));

        OptionBuilder.withArgName("file|url");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "file path or URL of JKS truststore, resource:tls/mesa_certs.jks by default");
        opts.addOption(OptionBuilder.create("truststore"));

        OptionBuilder.withArgName("password");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "password for truststore file, 'secret' by default");
        opts.addOption(OptionBuilder.create("truststorepw"));
                
        opts.addOption("ivrle", false,
                "offer only Implicit VR Little Endian Transfer Syntax.");
        opts.addOption("pdv1", false,
                "send only one PDV in one P-Data-TF PDU, pack command and data "
                        + "PDV in one P-DATA-TF PDU by default.");
        opts.addOption("tcpdelay", false,
                "set TCP_NODELAY socket option to false, true by default");

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for TCP connect, no timeout by default");
        opts.addOption(OptionBuilder.create("connectTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "delay in ms for Socket close after sending A-ABORT, 50ms by default");
        opts.addOption(OptionBuilder.create("soclosedelay"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "period in ms to check for outstanding DIMSE-RSP, 10s by default");
        opts.addOption(OptionBuilder.create("reaper"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving DIMSE-RSP, 10s by default");
        opts.addOption(OptionBuilder.create("rspTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving A-ASSOCIATE-AC, 5s by default");
        opts.addOption(OptionBuilder.create("acceptTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving A-RELEASE-RP, 5s by default");
        opts.addOption(OptionBuilder.create("releaseTO"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximal length in KB of received P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create("rcvpdulen"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximal length in KB of sent P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create("sndpdulen"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set SO_RCVBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create("sorcvbuf"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set SO_SNDBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create("sosndbuf"));

        OptionBuilder.withArgName("[seq/]attr=value");
        OptionBuilder.hasArgs();
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.withDescription("specify matching key. attr can be "
                + "specified by name or tag value (in hex), e.g. PatientName "
                + "or 00100010. Attributes in nested Datasets can "
                + "be specified by preceding the name/tag value of "
                + "the sequence attribute, e.g. 0072000C/00080060 "
                + "for Modality in the Hanging Protocol Definition "
                + "Sequence.");
        opts.addOption(OptionBuilder.create("q"));

        OptionBuilder.withArgName("Modality");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("specify matching Modality. Shortcut "
                + "for -q0072000C/00080060=<modality>.");
        opts.addOption(OptionBuilder.create("mod"));

        OptionBuilder.withArgName("Laterality");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify matching Laterality. Shortcut for -q0072000C/00200060=<lat>.");
        opts.addOption(OptionBuilder.create("lat"));

        OptionBuilder.withArgName("AnatomicRegion");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify matching Anatomic Region Sequence Code Value and Coding Scheme Designator. "
                + "Code Value and Coding Scheme Designer are separated by ':'. For example, T1234:DCM4CHE.");
        opts.addOption(OptionBuilder.create("ar"));

        OptionBuilder.withArgName("ProcedureCode");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify matching Procedure Code Sequence Code Value and Coding Scheme Designator. "
                 + "Code Value and Coding Scheme Designer are separated by ':'. For example, T1234:DCM4CHE.");
        opts.addOption(OptionBuilder.create("pc"));

        OptionBuilder.withArgName("ReasonCode");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify matching Reason for Requested Procedure Sequence Code Value and Coding Scheme Designator. "
                + "Code Value and Coding Scheme Designer are separated by ':'. For example, T1234:DCM4CHE.");
        opts.addOption(OptionBuilder.create("rfrp"));

        OptionBuilder.withArgName("HPUserIDCode");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify matching HP User ID Code Sequence Code Value and Coding Scheme Designator. "
                + "Code Value and Coding Scheme Designer are separated by ':'. For example, T1234:DCM4CHE.");
        opts.addOption(OptionBuilder.create("user"));

        OptionBuilder.withArgName("attr");
        OptionBuilder.hasArgs();
        OptionBuilder.withDescription(
                "specify additional return key. attr can be specified by name or tag value (in hex).");
        opts.addOption(OptionBuilder.create("r"));

        OptionBuilder.withArgName("num");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "cancel query after receive of specified number of responses, no cancel by default");
        opts.addOption(OptionBuilder.create("C"));

        OptionBuilder.withArgName("aet");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "retrieve matching objects to specified move destination.");
        opts.addOption(OptionBuilder.create("dest"));

        opts.addOption("lowprior", false,
                        "LOW priority of the C-FIND/C-MOVE operation, MEDIUM by default");
        opts.addOption("highprior", false,
                        "HIGH priority of the C-FIND/C-MOVE operation, MEDIUM by default");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcmhp: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = DcmHPQR.class.getPackage();
            System.out.println("dcmhpqr v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() != 1) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }

        return cl;
    }

    private static int toPort(String port) {
        return port != null ? parseInt(port, "illegal port number", 1, 0xffff)
                : 104;
    }

    private static int parseInt(String s, String errPrompt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {
            // parameter is not a valid integer; fall through to exit
        }
        exit(errPrompt);
        throw new RuntimeException();
    }

    private static String[] split(String s, char delim) {
        String[] s2 = { s, null };
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcmhpqr -h' for more information.");
        System.exit(1);
    }

    public void initTLS() throws GeneralSecurityException, IOException {
        KeyStore keyStore = loadKeyStore(keyStoreURL, keyStorePassword);
        KeyStore trustStore = loadKeyStore(trustStoreURL, trustStorePassword);
        device.initTLS(keyStore,
                keyPassword != null ? keyPassword : keyStorePassword,
                trustStore);
    }
    
    private static KeyStore loadKeyStore(String url, char[] password)
            throws GeneralSecurityException, IOException {
        KeyStore key = KeyStore.getInstance(toKeyStoreType(url));
        InputStream in = openFileOrURL(url);
        try {
            key.load(in, password);
        } finally {
            in.close();
        }
        return key;
    }

    private static InputStream openFileOrURL(String url) throws IOException {
        if (url.startsWith("resource:")) {
            return DcmHPQR.class.getClassLoader().getResourceAsStream(
                    url.substring(9));
        }
        try {
            return new URL(url).openStream();
        } catch (MalformedURLException e) {
            return new FileInputStream(url);
        }
    }

    private static String toKeyStoreType(String fname) {
        return fname.endsWith(".p12") || fname.endsWith(".P12")
                 ? "PKCS12" : "JKS";
    }
}
