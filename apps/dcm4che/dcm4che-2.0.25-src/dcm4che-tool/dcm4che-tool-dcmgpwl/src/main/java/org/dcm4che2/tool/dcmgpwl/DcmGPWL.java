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

package org.dcm4che2.tool.dcmgpwl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Date;
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
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.UserIdentity;
import org.dcm4che2.util.CloseUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 14420 $ $Date: 2010-11-26 14:22:59 +0100 (Fri, 26 Nov 2010) $
 * @since Mar 18, 2006
 *
 */
public class DcmGPWL {

    private static final int KB = 1024;
    private static final String USAGE = 
            "dcmgpwl <aet>[@<host>[:<port>]] [Options]";
    private static final String DESCRIPTION = 
            "Query specified remote Application Entity (=General Purpose Worklist SCP) " + 
            "If <port> is not specified, DICOM default port 104 is assumed. " +
            "If also no <host> is specified localhost is assumed.\n" +
            "Options:";
    private static final String EXAMPLE = 
            "\nExample: dcmgpwl GPWLSCP@localhost:11112 -status SCHEDULED\n" +
            "=> Query Application Entity GPWLSCP listening on local port 11112 for " +
            "all scheduled GP-SPS";

    private static final byte[] EXT_NEG_INFO_FUZZY_MATCHING = { 1, 1, 1 };

    private static String[] TLS1 = { "TLSv1" };

    private static String[] SSL3 = { "SSLv3" };

    private static String[] NO_TLS1 = { "SSLv3", "SSLv2Hello" };

    private static String[] NO_SSL2 = { "TLSv1", "SSLv3" };

    private static String[] NO_SSL3 = { "TLSv1", "SSLv2Hello" };

    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };
    
    private static final String[] SOP_CUIDS = {
        UID.GeneralPurposeWorklistInformationModelFIND,
        UID.GeneralPurposeScheduledProcedureStepSOPClass,
        UID.GeneralPurposePerformedProcedureStepSOPClass};
    
    private static final String[] METASOP_CUID = {
        UID.GeneralPurposeWorklistManagementMetaSOPClass };

    private static final int[] RETURN_KEYS = {
        Tag.SOPClassUID,
        Tag.SOPInstanceUID,
        Tag.PatientName,
        Tag.PatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.StudyInstanceUID,
        Tag.ScheduledProcedureStepID,
        Tag.GeneralPurposeScheduledProcedureStepStatus,
        Tag.GeneralPurposeScheduledProcedureStepPriority,
        Tag.ScheduledProcedureStepStartDateTime,
        Tag.MultipleCopiesFlag,
        Tag.ScheduledProcedureStepModificationDateTime,
        Tag.ExpectedCompletionDateTime,
        Tag.InputAvailabilityFlag,
    };

    private static final int[] REQUEST_RETURN_KEYS = {
        Tag.AccessionNumber,
        Tag.StudyInstanceUID,
        Tag.RequestingPhysician,
        Tag.RequestedProcedureDescription,
        Tag.RequestedProcedureID,
    };

    private static final int[] RETURN_SQ_KEYS = {
        Tag.ReferencedPerformedProcedureStepSequence,
        Tag.ScheduledProcessingApplicationsCodeSequence,
        Tag.ResultingGeneralPurposePerformedProcedureStepsSequence,
        Tag.ScheduledWorkitemCodeSequence,
        Tag.InputInformationSequence,
        Tag.RelevantInformationSequence,
        Tag.ScheduledStationNameCodeSequence,
        Tag.ScheduledStationClassCodeSequence,
        Tag.ScheduledStationGeographicLocationCodeSequence,
        Tag.ScheduledHumanPerformersSequence,
        Tag.ActualHumanPerformersSequence,
        Tag.ReferencedRequestSequence,
    };

    private static final int[] PPS_CREATE_TYPE2 = {
        Tag.PatientName,
        Tag.PatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.PerformedProcedureStepDescription,
    };

    private static final int[] PPS_SQ_CREATE_TYPE2 = {
        Tag.PerformedProcessingApplicationsCodeSequence,
        Tag.PerformedWorkitemCodeSequence,
        Tag.PerformedStationNameCodeSequence,
        Tag.PerformedStationClassCodeSequence,
        Tag.PerformedStationGeographicLocationCodeSequence,
        Tag.OutputInformationSequence,
        Tag.ActualHumanPerformersSequence,
        Tag.ReferencedRequestSequence,
    };
    
    private static final int[] SPS_SQ_PPS_SQ_MAP = {
        Tag.ScheduledProcessingApplicationsCodeSequence,
        Tag.PerformedProcessingApplicationsCodeSequence,
        Tag.ScheduledWorkitemCodeSequence,
        Tag.PerformedWorkitemCodeSequence,
        Tag.ScheduledStationNameCodeSequence,
        Tag.PerformedStationNameCodeSequence,
        Tag.ScheduledStationClassCodeSequence,
        Tag.PerformedStationClassCodeSequence,
        Tag.ScheduledStationGeographicLocationCodeSequence,
        Tag.PerformedStationGeographicLocationCodeSequence,
        Tag.ScheduledHumanPerformersSequence,
        Tag.ActualHumanPerformersSequence,
    };

    private static final int[] PPS_CREATE_FROM_SPS = {
        Tag.SpecificCharacterSet,
        Tag.PatientName,
        Tag.PatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.ActualHumanPerformersSequence,
        Tag.ReferencedRequestSequence,
    };
    
    private static final String[] IVRLE_TS = {
        UID.ImplicitVRLittleEndian };
    
    private static final String[] LE_TS = {
        UID.ExplicitVRLittleEndian, 
        UID.ImplicitVRLittleEndian };
    
    private final Executor executor;
    private final NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    private final NetworkConnection remoteConn = new NetworkConnection();
    private final Device device;
    private final NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private final NetworkConnection conn = new NetworkConnection();
    private File outDir;
    private String retrieveAET;
    private Association assoc;
    private int priority = 0;
    private int cancelAfter = Integer.MAX_VALUE;
    private boolean fuzzySemanticPersonNameMatching; 
    private final DicomObject attrs = new BasicDicomObject();

    private String keyStoreURL = "resource:tls/test_sys_1.p12";
    
    private char[] keyStorePassword = SECRET; 

    private char[] keyPassword; 
    
    private String trustStoreURL = "resource:tls/mesa_certs.jks";
    
    private char[] trustStorePassword = SECRET;
    
    public DcmGPWL(String name) {
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
    }

    public void initQuery() {
        for (int i = 0; i < RETURN_KEYS.length; i++) {
            attrs.putNull(RETURN_KEYS[i], null);
        }
        for (int i = 0; i < RETURN_SQ_KEYS.length; i++) {
            attrs.putNestedDicomObject(RETURN_SQ_KEYS[i],
                    new BasicDicomObject());
        }
        DicomObject rqAttrs = new BasicDicomObject();
        attrs.putNestedDicomObject(Tag.ReferencedRequestSequence, rqAttrs );
        for (int i = 0; i < REQUEST_RETURN_KEYS.length; i++) {
            rqAttrs.putNull(REQUEST_RETURN_KEYS[i], null);
        }
        rqAttrs.putNestedDicomObject(Tag.RequestedProcedureCodeSequence,
                new BasicDicomObject());
    }

    public void initAction() {
        attrs.putString(Tag.GeneralPurposeScheduledProcedureStepStatus,
                VR.CS, "IN PROGRESS");
    }

    public void initCreatePPS(String[] refsps) {
        long ts = System.currentTimeMillis();
        Date now = new Date(ts);
        attrs.putString(Tag.PerformedProcedureStepID, VR.SH, Long.toString(ts));
        attrs.putDate(Tag.PerformedProcedureStepStartDate, VR.DA, now);
        attrs.putDate(Tag.PerformedProcedureStepStartTime, VR.TM, now);
        attrs.putString(Tag.GeneralPurposePerformedProcedureStepStatus,
                VR.CS, "IN PROGRESS");
        for (int i = 0; i < PPS_CREATE_TYPE2.length; i++) {
            attrs.putNull(PPS_CREATE_TYPE2[i], null);
        }
        for (int i = 0; i < PPS_SQ_CREATE_TYPE2.length; i++) {
            attrs.putSequence(PPS_SQ_CREATE_TYPE2[i]);
        }
        if (refsps != null) {            
            DicomObject item = new BasicDicomObject();
            item.putString(Tag.ReferencedSOPClassUID, VR.UI,
                    UID.GeneralPurposeScheduledProcedureStepSOPClass);
            item.putString(Tag.ReferencedSOPInstanceUID, VR.UI, refsps[0]);
            item.putString(
                    Tag.ReferencedGeneralPurposeScheduledProcedureStepTransactionUID,
                    VR.UI, refsps[1]);
            attrs.putNestedDicomObject(
                    Tag.ReferencedGeneralPurposeScheduledProcedureStepSequence,
                    item);
            if (outDir != null) {
                File f = new File(outDir, refsps[0]);
                if (f.isFile()) {
                    DicomInputStream din = null;
                    try {
                        din = new DicomInputStream(f);
                        DicomObject sps = din.readDicomObject();
                        for (int i = 1; i < SPS_SQ_PPS_SQ_MAP.length; i++,i++) {
                            DicomObject codeItem = sps.getNestedDicomObject(SPS_SQ_PPS_SQ_MAP[i-1]);
                            if (codeItem != null) {
                                attrs.putNestedDicomObject(SPS_SQ_PPS_SQ_MAP[i], codeItem);
                            }
                        }
                        sps.subSet(PPS_CREATE_FROM_SPS).copyTo(attrs);
                    } catch (IOException e) {
                        System.out.println("WARNING: Failed to read " + f + ": "
                                + e.getMessage());
                    } finally {
                        CloseUtils.safeClose(din);
                    }
                }
            }
        }
    }
    
    public void addOutput(DicomObject inst) {
        DicomObject studyRef = findOrCreateItem(
                attrs.get(Tag.OutputInformationSequence),
                Tag.StudyInstanceUID, inst.getString(Tag.StudyInstanceUID),
                Tag.ReferencedSeriesSequence);
        DicomObject seriesRef = findOrCreateItem(
                studyRef.get(Tag.ReferencedSeriesSequence),
                Tag.SeriesInstanceUID, inst.getString(Tag.SeriesInstanceUID),
                Tag.ReferencedSOPSequence);
        DicomObject refSOP = new BasicDicomObject();
        refSOP.putString(Tag.ReferencedSOPClassUID, VR.UI, 
                inst.getString(Tag.SOPClassUID));
        refSOP.putString(Tag.ReferencedSOPInstanceUID, VR.UI,
                inst.getString(Tag.SOPInstanceUID));
        refSOP.putString(Tag.RetrieveAETitle, VR.AE, retrieveAET);
        seriesRef.get(Tag.ReferencedSOPSequence).addDicomObject(refSOP);
    }
        
    private DicomObject findOrCreateItem(DicomElement sq, int tag, String uid,
            int sqtag) {
        DicomObject item;
        for (int i = 0, n = sq.countItems(); i < n; i++) {
            item = sq.getDicomObject(i);
            if (uid.equals(item.getString(tag))) {
                return item;
            }
        }
        item = new BasicDicomObject();
        item.putString(tag, VR.UI, uid);
        item.putSequence(sqtag);
        sq.addDicomObject(item);
        return item;
    }

    public void initSetPPS() {
        attrs.putSequence(Tag.OutputInformationSequence);
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

    public final void setOutDir(File out) {
        if (out.mkdirs()) {
            System.out.println("M-WRITE " + out);
        }
        this.outDir = out;
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

    public final void setRetrieveAET(String aet) {
        this.retrieveAET = aet;        
    }

    public void setFuzzySemanticPersonNameMatching(boolean b) {
        this.fuzzySemanticPersonNameMatching = b;
    }

    public void addAttr(int tag, String value) {
        attrs.putString(tag, null, value);
    }
 
    public void addRefRequestAttr(int tag, String value) {
        DicomObject rqAttrs = 
            attrs.getNestedDicomObject(Tag.ReferencedRequestSequence);
        if (rqAttrs == null) {
            rqAttrs = new BasicDicomObject();
            attrs.putNestedDicomObject(Tag.ReferencedRequestSequence, rqAttrs);
        }
        rqAttrs .putString(tag, null, value);
    }
    
    public void addCodeValueAndScheme(int tag, String[] code) {
        DicomObject item = attrs.getNestedDicomObject(tag);
        if (item == null) {
            item = new BasicDicomObject();
            attrs.putNestedDicomObject(tag, item);
        }
        setCodeValueAndScheme(item, code);
    }

    private void setCodeValueAndScheme(DicomObject codeItem, String[] code) {
        codeItem.putString(Tag.CodeValue, VR.SH, code[0]);
        codeItem.putString(Tag.CodingSchemeDesignator, VR.SH, code[1]);
        codeItem.putString(Tag.CodeMeaning, VR.LO, code[2]);
    }

    public void setScheduledHumanPerformerCodeValueAndScheme(String[] valueAndScheme) {
        DicomObject performerKeys = attrs.getNestedDicomObject(
                Tag.ScheduledHumanPerformersSequence);
        DicomObject codeItem = new BasicDicomObject();
        setCodeValueAndScheme(codeItem, valueAndScheme);        
        performerKeys.putNestedDicomObject(Tag.HumanPerformerCodeSequence, codeItem);
        performerKeys.putNull(Tag.HumanPerformerName, VR.PN);
        performerKeys.putNull(Tag.HumanPerformerOrganization, VR.LO);
    }
    
    public void setActualHumanPerformer(String[] code, String name, String org) {
        DicomObject performerKeys = new BasicDicomObject();
        attrs.putNestedDicomObject(Tag.ActualHumanPerformersSequence,
                performerKeys);
        BasicDicomObject codeItem = new BasicDicomObject();
        codeItem.putString(Tag.CodeValue, VR.SH, code[0]);
        codeItem.putString(Tag.CodingSchemeDesignator, VR.SH, code[1]);
        codeItem.putString(Tag.CodeMeaning, VR.LO, code[2]);
        performerKeys.putNestedDicomObject(Tag.HumanPerformerCodeSequence, codeItem);
        if (name != null) {
            performerKeys.putString(Tag.HumanPerformerName, VR.PN, name);
        }
        if (org != null) {
            performerKeys.putString(Tag.HumanPerformerOrganization,VR.LO, org);
        }
    }
    
    public void setSPSStatus(String status) {
        attrs.putString(Tag.GeneralPurposeScheduledProcedureStepStatus,
                VR.CS, status);        
    }    

    public void setPPSStatus(String status) {
        attrs.putString(Tag.GeneralPurposePerformedProcedureStepStatus,
                VR.CS, status);
        if ("COMPLETED".equals(status) || "DISCONTINUED".equals(status)) {
            Date now = new Date();
            attrs.putDate(Tag.PerformedProcedureStepEndDate, VR.DA, now);
            attrs.putDate(Tag.PerformedProcedureStepEndTime, VR.TM, now);
        }
    }    
    
    public void configureTransferCapability(String[] cuids, String[] ts) {
        TransferCapability[] tc = new TransferCapability[cuids.length];
        for (int i = 0; i < tc.length; i++) {
            tc[i] = new TransferCapability(cuids[i], ts, TransferCapability.SCU);
            if (fuzzySemanticPersonNameMatching 
                    && UID.GeneralPurposeWorklistInformationModelFIND.equals(cuids[i]))
                tc[i].setExtInfo(EXT_NEG_INFO_FUZZY_MATCHING);
        }
        ae.setTransferCapability(tc);
    }

    public void setTransactionUID(String uid) {
        attrs.putString(Tag.TransactionUID, VR.UI, uid);
    }
    
    public void open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public void close() throws InterruptedException {
        assoc.release(true);
    }

    private TransferCapability selectTransferCapability(String cuid)
    throws NoPresentationContextException {
        TransferCapability tc = assoc.getTransferCapabilityAsSCU(
                UID.GeneralPurposeWorklistManagementMetaSOPClass);
        if (tc == null) {
            tc = assoc.getTransferCapabilityAsSCU(cuid);
            if (tc == null) {
                throw new NoPresentationContextException(
                        UIDDictionary.getDictionary().prompt(cuid) +
                        "not supported by " + remoteAE.getAETitle());
            }
        }
        return tc;
    }

    public int query() throws IOException, InterruptedException {
        TransferCapability tc = selectTransferCapability(
                UID.GeneralPurposeWorklistInformationModelFIND);
        System.out.println("Send Query Request:");
        System.out.println(attrs.toString());
        DimseRSP rsp = assoc.cfind(UID.GeneralPurposeWorklistInformationModelFIND,
                priority, attrs, tc.getTransferSyntax()[0], cancelAfter);
        int count = 0;
        while (rsp.next()) {
            DicomObject cmd = rsp.getCommand();
            if (CommandUtils.isPending(cmd)) {
                DicomObject data = rsp.getDataset();
                count++;
                System.out.println("\nReceived Query Response #" + count + ":");
                System.out.println(data.toString());
                if (outDir != null) {
                    String iuid = data.getString(Tag.SOPInstanceUID);
                    File f = new File(outDir, iuid);
                    System.out.println("M-WRITE " + f);
                    FileOutputStream fos = new FileOutputStream(f);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    DicomOutputStream dos = new DicomOutputStream(bos);
                    try {
                        data.initFileMetaInformation(UID.ExplicitVRLittleEndian);
                        dos.writeDicomFile(data);
                    } finally {
                        dos.close();
                    }
                }
            }
        }
        return count;
    }
    
    private void action(String iuid) throws IOException, InterruptedException {
        TransferCapability tc = selectTransferCapability(
                UID.GeneralPurposeScheduledProcedureStepSOPClass);
        System.out.println("Send GP-SPS Modify Request:");
        System.out.println(attrs.toString());
        DimseRSP rsp = assoc.naction(
                UID.GeneralPurposeScheduledProcedureStepSOPClass, iuid,
                1, attrs, tc.getTransferSyntax()[0]);
        rsp.next();
        DicomObject cmd = rsp.getCommand();
        System.out.println("\nReceived GP-PS Modify Response:");
        System.out.println(cmd.toString());
    }

    public void createpps(String iuid)throws IOException, InterruptedException {
        TransferCapability tc = selectTransferCapability(
                UID.GeneralPurposePerformedProcedureStepSOPClass);
        System.out.println("Send GP-PPS Create Request:");
        System.out.println(attrs.toString());
        DimseRSP rsp = assoc.ncreate(
                UID.GeneralPurposePerformedProcedureStepSOPClass, iuid,
                attrs, tc.getTransferSyntax()[0]);
        rsp.next();
        DicomObject cmd = rsp.getCommand();
        System.out.println("\nReceived GP-PPS Create Response:");
        System.out.println(cmd.toString());
    }
    
    public void setpps(String iuid) throws IOException, InterruptedException {
        TransferCapability tc = selectTransferCapability(
                UID.GeneralPurposePerformedProcedureStepSOPClass);
        System.out.println("Send GP-PPS Update Request:");
        System.out.println(attrs.toString());
        DimseRSP rsp = assoc.nset(
                UID.GeneralPurposePerformedProcedureStepSOPClass, iuid,
                attrs, tc.getTransferSyntax()[0]);
        rsp.next();
        DicomObject cmd = rsp.getCommand();
        System.out.println("\nReceived GP-PPS Update Response:");
        System.out.println(cmd.toString());
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = parse(args);
        DcmGPWL dcmgpwl = new DcmGPWL(cl.hasOption("device") 
                ? cl.getOptionValue("device") : "DCMGPWL");
        final List<String> argList = cl.getArgList();
        String remoteAE = argList.get(0);
        String[] calledAETAddress = split(remoteAE, '@');
        dcmgpwl.setCalledAET(calledAETAddress[0]);
        if (calledAETAddress[1] == null) {
            dcmgpwl.setRemoteHost("127.0.0.1");
            dcmgpwl.setRemotePort(104);
        } else {
            String[] hostPort = split(calledAETAddress[1], ':');
            dcmgpwl.setRemoteHost(hostPort[0]);
            dcmgpwl.setRemotePort(toPort(hostPort[1]));
        }
        if (cl.hasOption("L")) {
            String localAE = cl.getOptionValue("L");
            String[] callingAETHost = split(localAE, '@');
            dcmgpwl.setCalling(callingAETHost[0]);
            if (callingAETHost[1] != null) {
                dcmgpwl.setLocalHost(callingAETHost[1]);
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
            dcmgpwl.setUserIdentity(userId);
        }
        if (cl.hasOption("connectTO"))
            dcmgpwl.setConnectTimeout(parseInt(cl.getOptionValue("connectTO"),
                    "illegal argument of option -connectTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("reaper"))
            dcmgpwl.setAssociationReaperPeriod(parseInt(cl.getOptionValue("reaper"),
                    "illegal argument of option -reaper", 1, Integer.MAX_VALUE));
        if (cl.hasOption("rspTO"))
            dcmgpwl.setDimseRspTimeout(parseInt(cl.getOptionValue("rspTO"),
                    "illegal argument of option -rspTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("acceptTO"))
            dcmgpwl.setAcceptTimeout(parseInt(cl.getOptionValue("acceptTO"),
                    "illegal argument of option -acceptTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("releaseTO"))
            dcmgpwl.setReleaseTimeout(parseInt(cl.getOptionValue("releaseTO"),
                    "illegal argument of option -releaseTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("soclosedelay"))
            dcmgpwl.setSocketCloseDelay(parseInt(cl.getOptionValue("soclosedelay"),
                    "illegal argument of option -soclosedelay", 1, 10000));
        if (cl.hasOption("rcvpdulen"))
            dcmgpwl.setMaxPDULengthReceive(parseInt(cl.getOptionValue("rcvpdulen"),
                    "illegal argument of option -rcvpdulen", 1, 10000) * KB);
        if (cl.hasOption("sndpdulen"))
            dcmgpwl.setMaxPDULengthSend(parseInt(cl.getOptionValue("sndpdulen"),
                    "illegal argument of option -sndpdulen", 1, 10000) * KB);
        if (cl.hasOption("sosndbuf"))
            dcmgpwl.setSendBufferSize(parseInt(cl.getOptionValue("sosndbuf"),
                    "illegal argument of option -sosndbuf", 1, 10000) * KB);
        if (cl.hasOption("sorcvbuf"))
            dcmgpwl.setReceiveBufferSize(parseInt(cl.getOptionValue("sorcvbuf"),
                    "illegal argument of option -sorcvbuf", 1, 10000) * KB);
        dcmgpwl.setPackPDV(!cl.hasOption("pdv1"));
        dcmgpwl.setTcpNoDelay(!cl.hasOption("tcpdelay"));
        
        if (cl.hasOption("o")) {
            dcmgpwl.setOutDir(new File(cl.getOptionValue("o")));
        }
        if (cl.hasOption("retrieve")) {
            dcmgpwl.setRetrieveAET(cl.getOptionValue("retrieve"));
        }
        if (cl.hasOption("action")) {
            dcmgpwl.initAction();        
            dcmgpwl.setTransactionUID(cl.getOptionValues("action")[1]);
            if (cl.hasOption("status")) {
                dcmgpwl.setSPSStatus(cl.getOptionValue("status").toUpperCase());
            }            
            if (cl.hasOption("perfcode")) {
                dcmgpwl.setActualHumanPerformer(cl.getOptionValues("perfcode"),
                        cl.getOptionValue("perfname"), cl.getOptionValue("perforg"));
            }
        } else if (cl.hasOption("createpps")) {
            dcmgpwl.initCreatePPS(cl.getOptionValues("refsps"));      
            if (cl.hasOption("A")) {
                String[] matchingKeys = cl.getOptionValues("A");
                for (int i = 1; i < matchingKeys.length; i++, i++)
                    dcmgpwl.addAttr(toTag(matchingKeys[i - 1]), matchingKeys[i]);
            }
            if (cl.hasOption("rqA")) {
                String[] matchingKeys = cl.getOptionValues("rqA");
                for (int i = 1; i < matchingKeys.length; i++, i++)
                    dcmgpwl.addRefRequestAttr(toTag(matchingKeys[i - 1]), matchingKeys[i]);
            }
            if (cl.hasOption("workitem")) {
                dcmgpwl.addCodeValueAndScheme(Tag.PerformedWorkitemCodeSequence,
                        cl.getOptionValues("workitem"));
            }
            if (cl.hasOption("application")) {
                dcmgpwl.addCodeValueAndScheme(
                        Tag.PerformedProcessingApplicationsCodeSequence,
                        cl.getOptionValues("application"));
            }
            if (cl.hasOption("station")) {
                dcmgpwl.addCodeValueAndScheme(Tag.PerformedStationNameCodeSequence,
                        cl.getOptionValues("station"));
            }
            if (cl.hasOption("class")) {
                dcmgpwl.addCodeValueAndScheme(Tag.PerformedStationClassCodeSequence,
                        cl.getOptionValues("class"));
            }
            if (cl.hasOption("location")) {
                dcmgpwl.addCodeValueAndScheme(
                        Tag.PerformedStationGeographicLocationCodeSequence,
                        cl.getOptionValues("location"));
            }
            if (cl.hasOption("perfcode")) {
                dcmgpwl.setActualHumanPerformer(cl.getOptionValues("perfcode"),
                        cl.getOptionValue("perfname"), cl.getOptionValue("perforg"));
            }
            for (int i = 1, n = argList.size(); i < n; i++) {
                addOutput(dcmgpwl, new File(argList.get(i)));
            }
        } else if (cl.hasOption("setpps")) {
            dcmgpwl.initSetPPS();
            if (cl.hasOption("status")) {
                dcmgpwl.setPPSStatus(cl.getOptionValue("status").toUpperCase());
            }
            for (int i = 1, n = argList.size(); i < n; i++) {
                addOutput(dcmgpwl, new File(argList.get(i)));
            }
        } else {
            dcmgpwl.initQuery();
            if (cl.hasOption("status")) {
                dcmgpwl.setSPSStatus(cl.getOptionValue("status").toUpperCase());
            }            
            if (cl.hasOption("C"))
                dcmgpwl.setCancelAfter(parseInt(cl.getOptionValue("C"),
                        "illegal argument of option -C", 1, Integer.MAX_VALUE));
            if (cl.hasOption("lowprior"))
                dcmgpwl.setPriority(CommandUtils.LOW);
            if (cl.hasOption("highprior"))
                dcmgpwl.setPriority(CommandUtils.HIGH);
            if (cl.hasOption("fuzzy"))
                dcmgpwl.setFuzzySemanticPersonNameMatching(true);
            if (cl.hasOption("A")) {
                String[] matchingKeys = cl.getOptionValues("A");
                for (int i = 1; i < matchingKeys.length; i++, i++)
                    dcmgpwl.addAttr(toTag(matchingKeys[i - 1]), matchingKeys[i]);
            }
            if (cl.hasOption("rqA")) {
                String[] matchingKeys = cl.getOptionValues("rqA");
                for (int i = 1; i < matchingKeys.length; i++, i++)
                    dcmgpwl.addRefRequestAttr(toTag(matchingKeys[i - 1]), matchingKeys[i]);
            }
            if (cl.hasOption("D")) {
                dcmgpwl.addAttr(Tag.ScheduledProcedureStepStartDateTime,
                        cl.getOptionValue("D"));
            }
            if (cl.hasOption("workitem")) {
                dcmgpwl.addCodeValueAndScheme(Tag.ScheduledWorkitemCodeSequence,
                        cl.getOptionValues("workitem"));
            }
            if (cl.hasOption("application")) {
                dcmgpwl.addCodeValueAndScheme(
                        Tag.ScheduledProcessingApplicationsCodeSequence,
                        cl.getOptionValues("application"));
            }
            if (cl.hasOption("station")) {
                dcmgpwl.addCodeValueAndScheme(Tag.ScheduledStationNameCodeSequence,
                        cl.getOptionValues("station"));
            }
            if (cl.hasOption("class")) {
                dcmgpwl.addCodeValueAndScheme(Tag.ScheduledStationClassCodeSequence,
                        cl.getOptionValues("class"));
            }
            if (cl.hasOption("location")) {
                dcmgpwl.addCodeValueAndScheme(
                        Tag.ScheduledStationGeographicLocationCodeSequence,
                        cl.getOptionValues("location"));
            }
            if (cl.hasOption("performer")) {
                dcmgpwl.setScheduledHumanPerformerCodeValueAndScheme(
                        cl.getOptionValues("performer"));
            }
        }
  
        dcmgpwl.configureTransferCapability(
                cl.hasOption("metasop") ? METASOP_CUID : SOP_CUIDS,
                cl.hasOption("ivrle") ? IVRLE_TS : LE_TS);
        

        if (cl.hasOption("tls")) {
            String cipher = cl.getOptionValue("tls");
            if ("NULL".equalsIgnoreCase(cipher)) {
                dcmgpwl.setTlsWithoutEncyrption();
            } else if ("3DES".equalsIgnoreCase(cipher)) {
                dcmgpwl.setTls3DES_EDE_CBC();
            } else if ("AES".equalsIgnoreCase(cipher)) {
                dcmgpwl.setTlsAES_128_CBC();
            } else {
                exit("Invalid parameter for option -tls: " + cipher);
            }
            if (cl.hasOption("tls1")) {
                dcmgpwl.setTlsProtocol(TLS1);
            } else if (cl.hasOption("ssl3")) {
                dcmgpwl.setTlsProtocol(SSL3);
            } else if (cl.hasOption("no_tls1")) {
                dcmgpwl.setTlsProtocol(NO_TLS1);
            } else if (cl.hasOption("no_ssl3")) {
                dcmgpwl.setTlsProtocol(NO_SSL3);
            } else if (cl.hasOption("no_ssl2")) {
                dcmgpwl.setTlsProtocol(NO_SSL2);
            }
            dcmgpwl.setTlsNeedClientAuth(!cl.hasOption("noclientauth"));
            if (cl.hasOption("keystore")) {
                dcmgpwl.setKeyStoreURL(cl.getOptionValue("keystore"));
            }
            if (cl.hasOption("keystorepw")) {
                dcmgpwl.setKeyStorePassword(
                        cl.getOptionValue("keystorepw"));
            }
            if (cl.hasOption("keypw")) {
                dcmgpwl.setKeyPassword(cl.getOptionValue("keypw"));
            }
            if (cl.hasOption("truststore")) {
                dcmgpwl.setTrustStoreURL(
                        cl.getOptionValue("truststore"));
            }
            if (cl.hasOption("truststorepw")) {
                dcmgpwl.setTrustStorePassword(
                        cl.getOptionValue("truststorepw"));
            }
            long t1 = System.currentTimeMillis();
            try {
                dcmgpwl.initTLS();
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
            dcmgpwl.open();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to establish association:");
            e.printStackTrace(System.err);
            System.exit(2);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Connected to " + remoteAE + " in "
                + ((t2 - t1) / 1000F) + "s");

        try {
            if (cl.hasOption("action")) {
                dcmgpwl.action(cl.getOptionValues("action")[0]);
                long t3 = System.currentTimeMillis();
                System.out.println("Modified GP-SPS in "
                        + ((t3 - t2) / 1000F) + "s");
            } else if (cl.hasOption("createpps")) {
                dcmgpwl.createpps(cl.getOptionValue("createpps"));
                long t3 = System.currentTimeMillis();
                System.out.println("Create GP-PPS in "
                        + ((t3 - t2) / 1000F) + "s");
            } else if (cl.hasOption("setpps")) {
                dcmgpwl.setpps(cl.getOptionValue("setpps"));
                long t3 = System.currentTimeMillis();
                System.out.println("Update GP-PPS in "
                        + ((t3 - t2) / 1000F) + "s");
            } else {
                int n = dcmgpwl.query();
                long t3 = System.currentTimeMillis();
                System.out.println("Received " + n  + " matching entries in " 
                        + ((t3 - t2) / 1000F) + "s");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            dcmgpwl.close();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Released connection to " + remoteAE);
    }

    private static void addOutput(DcmGPWL dcmgpwl, File file) {
        if (file.isDirectory()) {
            String[] ss = file.list();
            for (int i = 0; i < ss.length; i++) {
                addOutput(dcmgpwl, new File(file, ss[i]));
            }
        } else {
            DicomInputStream din = null;
            try {
                din = new DicomInputStream(file);
                din.setHandler(new StopTagInputHandler(Tag.StudyID));
                dcmgpwl.addOutput(din.readDicomObject());
            } catch (IOException e) {
                System.out.println("WARNING: Failed to read " + file + ": "
                        + e.getMessage());
            } finally {
                CloseUtils.safeClose(din);
            }
        }        
    }

    private static int toTag(String nameOrHex) {
        try {
            return (int) Long.parseLong(nameOrHex, 16);
        } catch (NumberFormatException e) {
            return Tag.forName(nameOrHex);
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
        
        opts.addOption("metasop", false,
                "offer General Purpose Worklist Management Meta SOP Class.");
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

        OptionBuilder.withArgName("status");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "match/set GP-SPS/GP-PPS to specified <status>");
        opts.addOption(OptionBuilder.create("status"));
        
        OptionBuilder.withArgName("iuid:tuid");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "modify status of GP-SPS with SOP Instance UID <iuid> " +
                "using Transaction UID <tuid>.");
        opts.addOption(OptionBuilder.create("action"));

        OptionBuilder.withArgName("iuid");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "create GP-PPS with SOP Instance UID <iuid>.");
        opts.addOption(OptionBuilder.create("createpps"));

        OptionBuilder.withArgName("aet");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "retrieve AET used in SOP references in Output Information" +
                "Sequence in created or updated GP-PPS.");
        opts.addOption(OptionBuilder.create("retrieve"));
        
        OptionBuilder.withArgName("iuid");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "update GP-PPS with SOP Instance UID <iuid>.");
        opts.addOption(OptionBuilder.create("setpps"));

        OptionBuilder.withArgName("iuid:tuid");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "reference GP-SPS with SOP Instance UID <iuid> and " +
                "Transaction UID <tuid> in created GP-PPS.");
        opts.addOption(OptionBuilder.create("refsps"));
        
        OptionBuilder.withArgName("attr=value");
        OptionBuilder.hasArgs();
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.withDescription(
                "specify matching key or PPS attribute. attr can be specified " +
                "by name or tag value (in hex), e.g. PatientName or 00100010.");
        opts.addOption(OptionBuilder.create("A"));
        
        OptionBuilder.withArgName("datetime");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("specify matching SPS start datetime (range)");
        opts.addOption(OptionBuilder.create("D"));        

        OptionBuilder.withArgName("attr=value");
        OptionBuilder.hasArgs();
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.withDescription(
                "specify matching Referenced Request key or PPS attribute. " +
                "attr can be specified by name or tag value (in hex)");
        opts.addOption(OptionBuilder.create("rqA"));

        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Workitem Code");
        opts.addOption(OptionBuilder.create("workitem"));
        
        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Processing Application Code");
        opts.addOption(OptionBuilder.create("application"));
        
        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Station Name Code");
        opts.addOption(OptionBuilder.create("station"));
        
        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Station Class Code");
        opts.addOption(OptionBuilder.create("class"));
        
        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Station Geographic Location Code");
        opts.addOption(OptionBuilder.create("location"));
        
        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Human Performer Code");
        opts.addOption(OptionBuilder.create("performer"));

        OptionBuilder.withArgName("code:scheme:name");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify Actual Human Performer Code");
        opts.addOption(OptionBuilder.create("perfcode"));

        OptionBuilder.withArgName("name");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify Actual Human Performer Name");
        opts.addOption(OptionBuilder.create("perfname"));
        
        OptionBuilder.withArgName("name");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify Actual Human Performer Organisation");
        opts.addOption(OptionBuilder.create("perforg"));
        
        OptionBuilder.withArgName("num");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "cancel query after receive of specified number of responses, " +
                "no cancel by default");
        opts.addOption(OptionBuilder.create("C"));

        OptionBuilder.withArgName("dir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "store query results in DICOM files in directory <dir>.");
        opts.addOption(OptionBuilder.create("o"));

        opts.addOption("lowprior", false,
                "LOW priority of the C-FIND operation, MEDIUM by default");
        opts.addOption("highprior", false,
                "HIGH priority of the C-FIND operation, MEDIUM by default");
        
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcmgpwl: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = DcmGPWL.class.getPackage();
            System.out.println("dcmgpwl v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() < 1) {
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
        System.err.println("Try 'dcmgpwl -h' for more information.");
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
            return DcmGPWL.class.getClassLoader().getResourceAsStream(
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
