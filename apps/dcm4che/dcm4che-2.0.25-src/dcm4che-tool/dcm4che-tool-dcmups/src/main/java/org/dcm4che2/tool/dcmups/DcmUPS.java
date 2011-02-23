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
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2010
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

package org.dcm4che2.tool.dcmups;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.ContentHandlerAdapter;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.UserIdentity;
import org.dcm4che2.net.service.DicomService;
import org.dcm4che2.net.service.NEventReportSCU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Rev$ $Date:: 0000-00-00 $
 * @since May 5, 2010
 */
public class DcmUPS {

    private static Logger LOG = LoggerFactory.getLogger(DcmUPS.class);
    private static final int KB = 1024;
    private static final String USAGE = 
            "dcmups <operation> <aet>[@<host>[:<port>]] [Options]\n" +
            "   or: dcmups -L aet[@host]:port [Other Options]";
    private static final String DESCRIPTION = 
"Invoke specified <operation> on remote Application Entity (=SCP of UPS Push, " +
"UPS Pull or UPS Watch). If <port> is not specified, DICOM default port 104 " +
"is assumed. If also no <host> is specified localhost is assumed.\n" +
"Can be also invoked without operation and remote AE parameter to (only) act " +
"as UPS Event SOP Class SCU accepting assocation requests on the network " +
"connection specified by option -L.\n.\n" +
"Operations:\n" +
"create      Create an Unified Procedure Step. Requires -f.\n" +
"set         Set Unified Procedure Step Information. Requires -iuid and -f.\n" +
"get         Get Unified Procedure Step Information. Requires -iuid.\n" +
"find        Search for Unified Procedure Step.\n" +
"chstate     Change UPS State. Requires -iuid, -tuid and -state.\n" +
"reqcancel   Request UPS Cancel. Requires -iuid and -aet.\n" +
"subscribe   Subscribe to Receive UPS Event Reports. Requires option -aet.\n" +
"unsubscribe Unsubscribe from Receiving UPS Event Reports. Requires -aet.\n" +
"suspend     Suspend Global Subscription. Requires option -aet.\n.\n" +
"Options:";
    private static final String EXAMPLE = 
            "\nExample: dcmups find UPSSCP@localhost:11112 -state SCHEDULED\n" +
            "=> Search Application Entity UPSSCP listening on local port 11112 " +
            "for all scheduled UPS";
    private static final byte[] EXT_NEG_INFO_FUZZY_MATCHING = { 1, 1, 1 };
    private static final String[] IVRLE_TS = { UID.ImplicitVRLittleEndian };
    private static final String[] NATIVE_LE_TS = {
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian};
    private static String[] TLS1 = { "TLSv1" };
    private static String[] SSL3 = { "SSLv3" };
    private static String[] NO_TLS1 = { "SSLv3", "SSLv2Hello" };
    private static String[] NO_SSL2 = { "TLSv1", "SSLv3" };
    private static String[] NO_SSL3 = { "TLSv1", "SSLv2Hello" };
    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };
    private static final int[] RETURN_KEYS = {
        Tag.SOPClassUID,
        Tag.SOPInstanceUID,
        Tag.ScheduledProcedureStepPriority,
        Tag.ScheduledProcedureStepModificationDateTime,
        Tag.ProcedureStepLabel,
        Tag.WorklistLabel,
        Tag.ScheduledProcessingParametersSequence,
        Tag.ScheduledStationName,
        Tag.ScheduledStationClassCodeSequence,
        Tag.ScheduledStationGeographicLocationCodeSequence,
        Tag.ScheduledProcessingApplicationsCodeSequence,
        Tag.ScheduledHumanPerformersSequence,
        Tag.ScheduledProcedureStepStartDateTime,
        Tag.ExpectedCompletionDateTime,
        Tag.ScheduledWorkitemCodeSequence,
        Tag.CommentsOnTheScheduledProcedureStep,
        Tag.InputAvailabilityFlag,
        Tag.InputInformationSequence,
        Tag.StudyInstanceUID,
        Tag.PatientName,
        Tag.PatientID,
        Tag.IssuerOfPatientID,
        Tag.TypeOfPatientID,
        Tag.OtherPatientIDsSequence,
        Tag.PatientBirthDate,
        Tag.AdmissionID,
        Tag.IssuerOfAdmissionIDSequence,
        Tag.AdmittingDiagnosesDescription,
        Tag.AdmittingDiagnosesCodeSequence,
        Tag.ReferencedRequestSequence,
        Tag.RelatedProcedureStepSequence,
        Tag.MedicalAlerts,
        Tag.PregnancyStatus,
        Tag.SpecialNeeds,
        Tag.UnifiedProcedureStepState,
        Tag.UnifiedProcedureStepProgressInformationSequence,
        Tag.UnifiedProcedureStepPerformedProcedureSequence,
    };
    private enum Operation {
        create(UID.UnifiedProcedureStepPushSOPClass) {
            @Override
            public void execute(DcmUPS dcmups)
                    throws IOException, InterruptedException {
                dcmups.create();
            }
        },
        set(UID.UnifiedProcedureStepPullSOPClass) {
            @Override
            public void execute(DcmUPS dcmups)
                    throws IOException, InterruptedException {
                dcmups.set();
            }
        },
        get(UID.UnifiedProcedureStepPullSOPClass) {
            @Override
            public void execute(DcmUPS dcmups)
                    throws IOException, InterruptedException {
                dcmups.get();
            }
        },
        find(UID.UnifiedProcedureStepPullSOPClass) {
            @Override
            public void execute(DcmUPS dcmups)
                    throws IOException, InterruptedException {
                dcmups.find();
            }
        },
        chstate(UID.UnifiedProcedureStepPullSOPClass) {
            @Override
            public void execute(DcmUPS dcmups)
                    throws IOException, InterruptedException {
                dcmups.chstatus();
            }
        },
        reqcancel(UID.UnifiedProcedureStepPushSOPClass) {
            @Override
            public void execute(DcmUPS dcmups)
                    throws IOException, InterruptedException {
                dcmups.reqcancel();
            }
        },
        subscribe(UID.UnifiedProcedureStepWatchSOPClass) {
            @Override
            public void execute(DcmUPS dcmups)
                    throws IOException, InterruptedException {
                dcmups.subscribe();
            }
        },
        unsubscribe(UID.UnifiedProcedureStepWatchSOPClass) {
            @Override
            public void execute(DcmUPS dcmups)
                    throws IOException, InterruptedException {
                dcmups.unsubscribe();
            }
        },
        suspend(UID.UnifiedProcedureStepWatchSOPClass) {
            @Override
            public void execute(DcmUPS dcmups)
                    throws IOException, InterruptedException {
                dcmups.suspend();
            }
        }; 
        public final String sopClassUID;
        Operation(String uid) { this.sopClassUID = uid; }
        public abstract void execute(DcmUPS dcmups)
                throws IOException, InterruptedException;
    }
    
    private enum Priority { NORMAL, HIGH, LOW }

    private final Executor executor;
    private final NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    private final NetworkConnection remoteConn = new NetworkConnection();
    private final Device device;
    private final NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private final NetworkConnection conn = new NetworkConnection();

    private Priority priority = Priority.NORMAL;
    private String keyStoreURL = "resource:tls/test_sys_1.p12";
    private char[] keyStorePassword = SECRET; 
    private char[] keyPassword; 
    private String trustStoreURL = "resource:tls/mesa_certs.jks";
    private char[] trustStorePassword = SECRET;
    private String iuid;
    private String cuid;
    private boolean fuzzySemanticPersonNameMatching;

    private DicomObject attrs = new BasicDicomObject();
    private Association assoc;

    private final DimseRSPHandler rspHandler = new DimseRSPHandler(){
        @Override
        public void onDimseRSP(Association as, DicomObject cmd, DicomObject data) {
            if (data != null)
                LOG.info("Data:\n{}", data);
        }
    };

    private static final class EventReportService extends DicomService
            implements NEventReportSCU{

        public EventReportService() {
            super(UID.UnifiedProcedureStepPushSOPClass);
        }

        public void neventReport(Association as, int pcid, DicomObject rq,
                DicomObject data) throws DicomServiceException, IOException {
            if (data != null)
                LOG.info("Data:\n{}", data);
            DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
            rsp.putInt(Tag.EventTypeID, VR.US, rq.getInt(Tag.EventTypeID));
            as.writeDimseRSP(pcid, rsp, null);
        }
    }

    public DcmUPS(String name) {
        device = new Device(name);
        executor = new NewThreadExecutor(name);
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });
        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationAcceptor(true);
        ae.setAssociationInitiator(true);
        ae.setAETitle(name);
        ae.register(new EventReportService());
    }
    public void load(File f) throws Exception {
        LOG.info("M-READ " + f);
        attrs = f.getName().endsWith(".xml") ? loadXML(f) : loadDICOM(f);
    }

    public final void setSOPInstanceUID(String uid) {
        this.iuid = uid;
    }

    public final void setSOPClassUID(String uid) {
        this.cuid = uid;
    }

    private static DicomObject loadDICOM(File f) throws Exception {
        DicomInputStream in = new DicomInputStream(f);
        try {
            return in.readDicomObject();
        } finally {
            in.close();
        }
    }

    private static DicomObject loadXML(File f) throws Exception {
        DicomObject dcmobj = new BasicDicomObject();
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(dcmobj);
        p.parse(f, ch);
        return dcmobj;
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
            return DcmUPS.class.getClassLoader().getResourceAsStream(
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

    public final void setLocalHost(String hostname) {
        conn.setHostname(hostname);
    }

    public final void setLocalPort(int port) {
        conn.setPort(port);
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

    public final void setPriority(Priority priority) {
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

    public void setFuzzySemanticPersonNameMatching(boolean b) {
        this.fuzzySemanticPersonNameMatching = b;
    }

    public void setTransactionUID(String uid) {
        attrs.putString(Tag.TransactionUID, VR.UI, uid);
    }

    public void setReceivingAE(String aet) {
        attrs.putString(Tag.ReceivingAE, VR.AE, aet);
    }

    public void setRequestingAE(String aet) {
        attrs.putString(Tag.RequestingAE, VR.AE, aet);
    }

    public void setDeletionLock(boolean lock) {
        attrs.putString(Tag.DeletionLock, VR.CS, lock ? "TRUE" : "FALSE");
    }

    public void setState(String state) {
        attrs.putString(Tag.UnifiedProcedureStepState, VR.CS, state);
    }

    public void addMatchingKey(int[] tagPath, String value) {
        attrs.putString(tagPath, null, value);
    }

    public void addReturnKey(int[] tagPath) {
        attrs.putNull(tagPath, null);
    }

    public void addDefReturnKeys() {
        for (int tag : RETURN_KEYS)
            attrs.putNull(tag, null);
    }

    public void configureTransferCapability(String[] tsuids) {
        TransferCapability eventtc = new TransferCapability(
                UID.UnifiedProcedureStepEventSOPClass, tsuids,
                TransferCapability.SCU);
        TransferCapability[] tcs;
        if (cuid == null) {
            tcs = new TransferCapability[]{ eventtc };
        } else {
            TransferCapability tc = new TransferCapability(
                    cuid, tsuids, TransferCapability.SCU);
            if (fuzzySemanticPersonNameMatching)
                tc.setExtInfo(EXT_NEG_INFO_FUZZY_MATCHING);
            tcs = new TransferCapability[]{ eventtc, tc };
        }
        ae.setTransferCapability(tcs);
    }

    public boolean start() throws IOException {
        if (!conn.isListening())
            return false;
        conn.bind(executor );
        System.out.println("Start Server listening on port "
                + conn.getPort());
        return true;
    }

    public void stop() {
        if (conn.isListening()) {
            conn.unbind();
        }
    }

    public void open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public void close() throws InterruptedException {
        assoc.release(true);
    }

    private String selectTransferSyntax()
            throws NoPresentationContextException {
        TransferCapability tcs = assoc.getTransferCapabilityAsSCU(cuid);
        if (tcs != null)
            return tcs.getTransferSyntax()[0];
        throw new NoPresentationContextException(
                UIDDictionary.getDictionary().prompt(cuid)
                + " not supported by " + remoteAE.getAETitle());
    }

    public void create() throws IOException, InterruptedException {
         String tsuid = selectTransferSyntax();
         LOG.info("Send N-CREATE Request using {}:\n{}",
                UIDDictionary.getDictionary().prompt(cuid), attrs);
         assoc.ncreate(cuid, UID.UnifiedProcedureStepPushSOPClass, iuid, attrs,
                tsuid, rspHandler);
    }

    public void set() throws IOException, InterruptedException {
        String tsuid = selectTransferSyntax();
        LOG.info("Send N-SET Request using {}:\n{}",
                UIDDictionary.getDictionary().prompt(cuid), attrs);
        assoc.nset(cuid, UID.UnifiedProcedureStepPushSOPClass, iuid, attrs,
                tsuid, rspHandler);
    }

    public void get() throws IOException, InterruptedException {
        assoc.nget(cuid, UID.UnifiedProcedureStepPushSOPClass, iuid,
                getAttributeIdentifierList(), rspHandler);
    }

    private int[] getAttributeIdentifierList() {
        if (attrs.isEmpty())
            return null;
        int[] tags = new int[attrs.size()];
        Iterator<DicomElement> attrIter = attrs.iterator();
        for (int i = 0; i < tags.length; i++)
            tags[i] = attrIter.next().tag();
        return tags;
    }

    public void find() throws IOException, InterruptedException {
        String tsuid = selectTransferSyntax();
        LOG.info("Send C-FIND Request using {}:\n{}",
               UIDDictionary.getDictionary().prompt(cuid), attrs);
        assoc.cfind(cuid, cuid,
                priority.ordinal(), attrs, tsuid, rspHandler);
    }

    public void chstatus() throws IOException, InterruptedException {
        String tsuid = selectTransferSyntax();
        LOG.info("Send N-ACTION Request using {}:\n{}",
               UIDDictionary.getDictionary().prompt(cuid), attrs);
        assoc.naction(cuid, UID.UnifiedProcedureStepPushSOPClass, iuid, 1,
                attrs, tsuid, rspHandler);
    }

    public void reqcancel() throws IOException, InterruptedException {
        String tsuid = selectTransferSyntax();
        LOG.info("Send N-ACTION Request using {}:\n{}",
               UIDDictionary.getDictionary().prompt(cuid), attrs);
        assoc.naction(cuid, UID.UnifiedProcedureStepPushSOPClass, iuid, 2,
                attrs, tsuid, rspHandler);
    }

    public void subscribe() throws IOException, InterruptedException {
        String tsuid = selectTransferSyntax();
        LOG.info("Send N-ACTION Request using {}:\n{}",
               UIDDictionary.getDictionary().prompt(cuid), attrs);
        assoc.naction(cuid, UID.UnifiedProcedureStepPushSOPClass, 
                iuid != null ? iuid 
                             : UID.UnifiedWorklistandProcedureStepSOPInstance,
                3, attrs, tsuid, rspHandler);
    }

    public void unsubscribe() throws IOException, InterruptedException {
        String tsuid = selectTransferSyntax();
        LOG.info("Send N-ACTION Request using {}:\n{}",
               UIDDictionary.getDictionary().prompt(cuid), attrs);
        assoc.naction(cuid, UID.UnifiedProcedureStepPushSOPClass,
                iuid != null ? iuid 
                        : UID.UnifiedWorklistandProcedureStepSOPInstance,
                4, attrs, tsuid, rspHandler);
    }

    public void suspend() throws IOException, InterruptedException {
        String tsuid = selectTransferSyntax();
        LOG.info("Send N-ACTION Request using {}:\n{}",
               UIDDictionary.getDictionary().prompt(cuid), attrs);
        assoc.naction(cuid, UID.UnifiedProcedureStepPushSOPClass,
                UID.UnifiedWorklistandProcedureStepSOPInstance, 5,
                attrs, tsuid, rspHandler);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = parse(args);
        DcmUPS dcmups = new DcmUPS(cl.hasOption("device") 
                ? cl.getOptionValue("device") : "DCMUPSSCU");
        final List<String> argList = cl.getArgList();
        Operation op = null;
        String remoteAE = null;
        if (!argList.isEmpty()) {
            op = toOperation(argList.get(0));
            dcmups.setSOPClassUID(op.sopClassUID);
            if (argList.size() < 2)
                exit("Missing <aet>[@<host>[:<port>]] after <operation>.");
            remoteAE = argList.get(1);
            String[] calledAETAddress = split(remoteAE, '@');
            dcmups.setCalledAET(calledAETAddress[0]);
            if (calledAETAddress[1] == null) {
                dcmups.setRemoteHost("127.0.0.1");
                dcmups.setRemotePort(104);
            } else {
                String[] hostPort = split(calledAETAddress[1], ':');
                dcmups.setRemoteHost(hostPort[0]);
                dcmups.setRemotePort(toPort(hostPort[1]));
            }
            if (argList.size() > 2)
                exit("Too many arguments.");
        }
        if (cl.hasOption("L")) {
            String localAE = cl.getOptionValue("L");
            String[] localPort = split(localAE, ':');
            if (localPort[1] != null)
                dcmups.setLocalPort(toPort(localPort[1]));
            String[] callingAETHost = split(localPort[0], '@');
            dcmups.setCalling(callingAETHost[0]);
            if (callingAETHost[1] != null) {
                dcmups.setLocalHost(callingAETHost[1]);
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
            dcmups.setUserIdentity(userId);
        }
        if (cl.hasOption("connectTO"))
            dcmups.setConnectTimeout(parseInt(cl.getOptionValue("connectTO"),
                    "illegal argument of option -connectTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("reaper"))
            dcmups.setAssociationReaperPeriod(parseInt(cl.getOptionValue("reaper"),
                            "illegal argument of option -reaper", 1,
                            Integer.MAX_VALUE));
        if (cl.hasOption("cfindrspTO"))
            dcmups.setDimseRspTimeout(parseInt(cl.getOptionValue("cfindrspTO"),
                    "illegal argument of option -cfindrspTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("acceptTO"))
            dcmups.setAcceptTimeout(parseInt(cl.getOptionValue("acceptTO"),
                    "illegal argument of option -acceptTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("releaseTO"))
            dcmups.setReleaseTimeout(parseInt(cl.getOptionValue("releaseTO"),
                    "illegal argument of option -releaseTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("soclosedelay"))
            dcmups.setSocketCloseDelay(parseInt(cl
                    .getOptionValue("soclosedelay"),
                    "illegal argument of option -soclosedelay", 1, 10000));
        if (cl.hasOption("rcvpdulen"))
            dcmups.setMaxPDULengthReceive(parseInt(cl
                    .getOptionValue("rcvpdulen"),
                    "illegal argument of option -rcvpdulen", 1, 10000)
                    * KB);
        if (cl.hasOption("sndpdulen"))
            dcmups.setMaxPDULengthSend(parseInt(cl.getOptionValue("sndpdulen"),
                    "illegal argument of option -sndpdulen", 1, 10000)
                    * KB);
        if (cl.hasOption("sosndbuf"))
            dcmups.setSendBufferSize(parseInt(cl.getOptionValue("sosndbuf"),
                    "illegal argument of option -sosndbuf", 1, 10000)
                    * KB);
        if (cl.hasOption("sorcvbuf"))
            dcmups.setReceiveBufferSize(parseInt(cl.getOptionValue("sorcvbuf"),
                    "illegal argument of option -sorcvbuf", 1, 10000)
                    * KB);
        dcmups.setPackPDV(!cl.hasOption("pdv1"));
        dcmups.setTcpNoDelay(!cl.hasOption("tcpdelay"));
        if (cl.hasOption("findprior"))
            dcmups.setPriority(toPriority(cl.getOptionValue("findprior")));

        if (cl.hasOption("r")) {
            String[] returnKeys = cl.getOptionValues("r");
            for (int i = 0; i < returnKeys.length; i++)
                dcmups.addReturnKey(Tag.toTagPath(returnKeys[i]));
        } else if (op == Operation.find)
            dcmups.addDefReturnKeys();

        if (cl.hasOption("q")) {
            String[] matchingKeys = cl.getOptionValues("q");
            for (int i = 1; i < matchingKeys.length; i++, i++)
                dcmups.addMatchingKey(Tag.toTagPath(matchingKeys[i - 1]), matchingKeys[i]);
        }

        dcmups.configureTransferCapability(
                cl.hasOption("ivrle") ? IVRLE_TS : NATIVE_LE_TS);

        if (cl.hasOption("tls")) {
            String cipher = cl.getOptionValue("tls");
            if ("NULL".equalsIgnoreCase(cipher)) {
                dcmups.setTlsWithoutEncyrption();
            } else if ("3DES".equalsIgnoreCase(cipher)) {
                dcmups.setTls3DES_EDE_CBC();
            } else if ("AES".equalsIgnoreCase(cipher)) {
                dcmups.setTlsAES_128_CBC();
            } else {
                exit("Invalid parameter for option -tls: " + cipher);
            }
            if (cl.hasOption("tls1")) {
                dcmups.setTlsProtocol(TLS1);
            } else if (cl.hasOption("ssl3")) {
                dcmups.setTlsProtocol(SSL3);
            } else if (cl.hasOption("no_tls1")) {
                dcmups.setTlsProtocol(NO_TLS1);
            } else if (cl.hasOption("no_ssl3")) {
                dcmups.setTlsProtocol(NO_SSL3);
            } else if (cl.hasOption("no_ssl2")) {
                dcmups.setTlsProtocol(NO_SSL2);
            }
            dcmups.setTlsNeedClientAuth(!cl.hasOption("noclientauth"));
            if (cl.hasOption("keystore")) {
                dcmups.setKeyStoreURL(cl.getOptionValue("keystore"));
            }
            if (cl.hasOption("keystorepw")) {
                dcmups.setKeyStorePassword(
                        cl.getOptionValue("keystorepw"));
            }
            if (cl.hasOption("keypw")) {
                dcmups.setKeyPassword(cl.getOptionValue("keypw"));
            }
            if (cl.hasOption("truststore")) {
                dcmups.setTrustStoreURL(
                        cl.getOptionValue("truststore"));
            }
            if (cl.hasOption("truststorepw")) {
                dcmups.setTrustStorePassword(
                        cl.getOptionValue("truststorepw"));
            }
            long t1 = System.currentTimeMillis();
            try {
                dcmups.initTLS();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to initialize TLS context:"
                        + e.getMessage());
                System.exit(2);
            }
            long t2 = System.currentTimeMillis();
            LOG.info("Initialize TLS context in {} s",
                    Float.valueOf((t2 - t1) / 1000f));
        }
        
        if (cl.hasOption("f"))
            try {
                dcmups.load(new File(cl.getOptionValue("f")));
            } catch (Exception e) {
                exit(e.getMessage());
            }
        else if (op == Operation.create || op == Operation.set)
            exit("Missing option -f");
        
        if (cl.hasOption("iuid"))
            dcmups.setSOPInstanceUID(cl.getOptionValue("iuid"));
        else if (op == Operation.set || op == Operation.get
                || op == Operation.chstate)
            exit("Missing option -iuid");
        
        if (cl.hasOption("state"))
            dcmups.setState(cl.getOptionValue("state"));
        else if (op == Operation.chstate)
            exit("Missing option -state");

        if (op == Operation.subscribe || op == Operation.unsubscribe
                || op == Operation.suspend || op == Operation.reqcancel) {
            if (cl.hasOption("aet"))
                if (op == Operation.reqcancel)
                    dcmups.setRequestingAE(cl.getOptionValue("aet"));
                else {
                    dcmups.setReceivingAE(cl.getOptionValue("aet"));
                    if (op == Operation.subscribe)
                        dcmups.setDeletionLock(cl.hasOption("dellock"));
                }
            else
                exit("Missing option -aet");
        }

        if (cl.hasOption("tuid"))
            dcmups.setTransactionUID(cl.getOptionValue("tuid"));
        else if (op == Operation.chstate)
            exit("Missing option -tuid");

        if (cl.hasOption("upspush"))
            dcmups.setSOPClassUID(UID.UnifiedProcedureStepPushSOPClass);

        if (cl.hasOption("upspull"))
            dcmups.setSOPClassUID(UID.UnifiedProcedureStepPullSOPClass);

        if (cl.hasOption("upswatch"))
            dcmups.setSOPClassUID(UID.UnifiedProcedureStepWatchSOPClass);

        try {
            if (!dcmups.start() && op == null)
                exit("Missing -L aet[@host]:port");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to start server for receiving " +
                    "requested objects:" + e.getMessage());
            System.exit(2);
        }
        if (op != null) {
            long t1 = System.currentTimeMillis();
            try {
                dcmups.open();
            } catch (Exception e) {
                LOG.error("Failed to establish association:", e);
                System.exit(2);
            }
            long t2 = System.currentTimeMillis();
            LOG.info("Connected to {} in {} s", remoteAE,
                    Float.valueOf((t2 - t1) / 1000f));
            try {
                op.execute(dcmups);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                try {
                    dcmups.close();
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
                LOG.info("Released connection to {}",remoteAE);
            }
        }
    }

    private static Priority toPriority(String s) {
        try {
            return Priority.valueOf(s);
        } catch (IllegalArgumentException e) {
            exit("Unrecognized priority: " + s);
            throw new RuntimeException();
        }
    }

    private static Operation toOperation(String s) {
        try {
            return Operation.valueOf(s);
        } catch (IllegalArgumentException e) {
            exit("Unrecognized operation: " + s);
            throw new RuntimeException();
        }
    }

    private static CommandLine parse(String[] args) {
        Options opts = new Options();

        OptionBuilder.withArgName("name");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set device name, use DCMGPWL by default");
        opts.addOption(OptionBuilder.create("device"));

        OptionBuilder.withArgName("aet[@host]");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set AET and local address of local Application Entity, use " +
                "device name and pick up any valid local address to bind the " +
                "socket by default");
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
        opts.addOption("fuzzy", false, 
                "negotiate support of fuzzy semantic person name attribute matching.");
        opts.addOption("pdv1", false,
                "send only one PDV in one P-Data-TF PDU, pack command and data " +
                "PDV in one P-DATA-TF PDU by default.");
        opts.addOption("tcpdelay", false,
                "set TCP_NODELAY socket option to false, true by default");
        
        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("timeout in ms for TCP connect, no timeout by default");
        opts.addOption(OptionBuilder.create("connectTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("delay in ms for Socket close after sending A-ABORT, 50ms by default");
        opts.addOption(OptionBuilder.create("soclosedelay"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("period in ms to check for outstanding DIMSE-RSP, 10s by default");
        opts.addOption(OptionBuilder.create("reaper"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("timeout in ms for receiving DIMSE-RSP, 10s by default");
        opts.addOption(OptionBuilder.create("rspTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("timeout in ms for receiving A-ASSOCIATE-AC, 5s by default");
        opts.addOption(OptionBuilder.create("acceptTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("timeout in ms for receiving A-RELEASE-RP, 5s by default");
        opts.addOption(OptionBuilder.create("releaseTO"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("maximal length in KB of received P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create("rcvpdulen"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("maximal length in KB of sent P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create("sndpdulen"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("set SO_RCVBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create("sorcvbuf"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set SO_SNDBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create("sosndbuf"));

        OptionBuilder.withArgName("state");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("match/change UPS of/to specified <state>");
        opts.addOption(OptionBuilder.create("state"));

        OptionBuilder.withArgName("file");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "path to DICOM or XML file with UPS attributes to create or set.");
        opts.addOption(OptionBuilder.create("f"));

        OptionBuilder.withArgName("uid");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("SOP Instance UID of UPS. Required for " +
                "operation set, get, chstate, reqcancel, and for operation " +
                "subscribe/unsubscribe to/form receiving UPS events about a " +
                "specific UPS - and not subscribe/unsubscribe globally.");
        opts.addOption(OptionBuilder.create("iuid"));

        OptionBuilder.withArgName("uid");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Transaction UID.");
        opts.addOption(OptionBuilder.create("tuid"));

        OptionBuilder.withArgName("aet");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Specifies AET of Requesting AE for " +
                "operation reqcancel and AET of Receiving AE for operation " +
                "subscribe, unsubscribe and suspend.");
        opts.addOption(OptionBuilder.create("aet"));

        opts.addOption("dellock", false,
                "invoke operation subscribe with Deletion Lock.");

        OptionBuilder.withArgName("[seq/]attr=value");
        OptionBuilder.hasArgs();
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.withDescription("specify matching key. attr can be " +
                "specified by name or tag value (in hex), e.g. " +
                "ScheduledProcedureStepStartDateTime or 00404005. " +
                "Attributes in nested Datasets can be specified by " +
                "including the name/tag value of the sequence attribute, " +
                "e.g. 00404018/00080100 for Code Value in the Scheduled " +
                "Workitem Code Sequence.");
        opts.addOption(OptionBuilder.create("q"));

        OptionBuilder.withArgName("attr");
        OptionBuilder.hasArgs();
        OptionBuilder.withDescription("specify return key. attr can be " +
                "specified by name or tag value (in hex). If no return key" +
                "is specified, defaults are used for invoked find operation.");
        opts.addOption(OptionBuilder.create("r"));

        OptionGroup sopClass = new OptionGroup();
        sopClass.addOption(new Option("upspush",
                "Use UPS Push SOP Class to invoke operation. " +
                "Use appropriate SOP Class by default."));
        sopClass.addOption(new Option("upspull",
                "Use UPS Pull SOP Class to invoke operation. " +
                "Use appropriate SOP Class by default."));
        sopClass.addOption(new Option("upswatch",
                "Use UPS Watch SOP Class to invoke operation. " +
                "Use appropriate SOP Class by default."));
        opts.addOptionGroup(sopClass);

        OptionBuilder.withArgName("NORMAL|HIGH|LOW");
        OptionBuilder.hasArgs();
        OptionBuilder.withDescription("specify priority of the C-FIND " +
                "operation, NORMAL by default.");
        opts.addOption(OptionBuilder.create("findprior"));

        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");

        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcmups: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = DcmUPS.class.getPackage();
            System.out.println("dcmups v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h')) {
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
        System.err.println("Try 'dcmups -h' for more information.");
        System.exit(1);
    }

}
