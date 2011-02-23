package org.dcm4che2.tool.dcmmover;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.DataWriterAdapter;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.UserIdentity;
import org.dcm4che2.net.service.StorageCommitmentService;
import org.dcm4che2.util.StringUtils;
import org.dcm4che2.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides DICOM Storage SCU capability for the DcmMover.
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 * @see org.dcm4che2.tool.DcmSnd
 * Based on original work by gunter zeilinger(gunterze@gmail.com).
 * Original version - Revision: 4892 $ $Date: 2007-08-21 09:36:10 +0200 (Tue, 21 Aug 2007) 
 */
class DcmSnd extends StorageCommitmentService {

    static Logger log = LoggerFactory.getLogger(DcmSnd.class);

    private static final String[] IVLE_TS = { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian, };

    private static final String[] EVLE_TS = { UID.ExplicitVRLittleEndian, UID.ImplicitVRLittleEndian, UID.ExplicitVRBigEndian, };

    private static final String[] EVBE_TS = { UID.ExplicitVRBigEndian, UID.ExplicitVRLittleEndian, UID.ImplicitVRLittleEndian, };

    private static final int STG_CMT_ACTION_TYPE = 1;

    private Executor executor = new NewThreadExecutor("DCMSND");

    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();

    private NetworkConnection remoteConn = new NetworkConnection();

    private Device device = new Device("DCMSND");

    private NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private NetworkConnection conn = new NetworkConnection();

    private HashMap<String, HashSet<String>> as2ts = new HashMap<String, HashSet<String>>();

    private Association assoc;

    private int priority = 0;

    private int objectsSent = 0;

    private long totalSize = 0L;

    private boolean stgcmt = false;

    private long shutdownDelay = 1000L;

    private DicomObject stgCmtResult;

    Executor eventExecutor = new NewThreadExecutor("DCMSND_EVENTS");

    private ActionListener moveActionListener;

    private String storeResponseError;

    public DcmSnd() {
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAssociationAcceptor(true);
        ae.register(this);
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

    public final void setCalledAET(String called) {
        remoteAE.setAETitle(called);
    }

    public final String getCalledAET() {
        return remoteAE.getAETitle();
    }

    public final void setCalling(String calling) {
        ae.setAETitle(calling);
    }

    public final void setUserIdentity(UserIdentity userIdentity) {
        ae.setUserIdentity(userIdentity);
    }

    public final void setOfferDefaultTransferSyntaxInSeparatePresentationContext(boolean enable) {
        ae.setOfferDefaultTransferSyntaxInSeparatePresentationContext(enable);
    }

    public final void setStorageCommitment(boolean stgcmt) {
        this.stgcmt = stgcmt;
    }

    public final boolean isStorageCommitment() {
        return stgcmt;
    }

    public final void setShutdownDelay(int shutdownDelay) {
        this.shutdownDelay = shutdownDelay;
    }

    public final void setConnectTimeout(int connectTimeout) {
        conn.setConnectTimeout(connectTimeout);
    }

    public final void setMaxPDULengthReceive(int maxPDULength) {
        ae.setMaxPDULengthReceive(maxPDULength);
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        ae.setMaxOpsInvoked(maxOpsInvoked);
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

    public final void setPriority(int priority) {
        this.priority = priority;
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

    public final int getTotalSent() {
        return objectsSent;
    }

    public final long getTotalSizeSent() {
        return totalSize;
    }

    private synchronized DicomObject waitForStgCmtResult() throws InterruptedException {
        while (stgCmtResult == null)
            wait();
        return stgCmtResult;
    }

    private void addTransferCapability(String cuid, String tsuid) {
        HashSet<String> ts = as2ts.get(cuid);
        if (ts == null) {
            ts = new HashSet<String>();
            ts.add(UID.ImplicitVRLittleEndian);
            as2ts.put(cuid, ts);
        }
        ts.add(tsuid);
    }

    public void configureTransferCapability() {
        int off = stgcmt ? 1 : 0;
        TransferCapability[] tc = new TransferCapability[off + as2ts.size()];
        if (stgcmt) {
            tc[0] = new TransferCapability(UID.StorageCommitmentPushModelSOPClass, DcmMover.ONLY_IVRLE_TS, TransferCapability.SCU);
        }
        Iterator<Entry<String, HashSet<String>>> iter = as2ts.entrySet().iterator();
        for (int i = off; i < tc.length; i++) {
            Entry<String, HashSet<String>> e = iter.next();
            String cuid = e.getKey();
            HashSet<String> ts = e.getValue();
            tc[i] = new TransferCapability(cuid, ts.toArray(new String[ts.size()]), TransferCapability.SCU);
        }
        ae.setTransferCapability(tc);
    }

    public void start() throws IOException {
        final String fn = "start: ";

        if (conn.isListening()) {
            conn.bind(executor);

            log.info(fn + "Started Server listening on port " + conn.getPort());
        }
    }

    public void stop() {
        final String fn = "stop: ";

        if (conn.isListening()) {
            try {
                Thread.sleep(shutdownDelay);
            } catch (InterruptedException e) {
                log.error(fn + "Exception while shutting down Listener.", e);
            }
            conn.unbind();
        }
    }

    private void open() throws IOException, ConfigurationException, InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public void send(MovedDicomObject movedDcmObject) throws DcmMoveException, InterruptedException {
        final String fn = "send: ";

        storeResponseError = null;

        String objTsUid = movedDcmObject.getTransferSyntax();
        DicomObject dcmObj = movedDcmObject.getDicomObject();
        String objClassUid = movedDcmObject.getClassUid();
        String objInstanceUid = movedDcmObject.getInstanceUid();

        if (log.isDebugEnabled()) {
            log.debug(fn + "Adding moved object's transfer syntax to this SCU's capability. Class UID=["
                    + UIDDictionary.getDictionary().nameOf(objClassUid) + "], Transfer Syntax UID=["
                    + UIDDictionary.getDictionary().nameOf(objTsUid) + "]");
        }
        addTransferCapability(objClassUid, objTsUid);
        configureTransferCapability();
        if (log.isDebugEnabled()) {
            StringBuffer str = new StringBuffer("Send SCU Transfer Capability:");
            for (TransferCapability tc : ae.getTransferCapability()) {
                String sopClassName = UIDDictionary.getDictionary().nameOf(tc.getSopClass());
                str.append("\n\tSOP Class[" + sopClassName + "]  Transfer Syntaxes:");
                for (String xferSyntaxUid : tc.getTransferSyntax()) {
                    str.append("\n\t\t" + UIDDictionary.getDictionary().nameOf(xferSyntaxUid));
                }
            }
            log.debug(fn + str.toString());
        }

        log.debug(fn + "Opening association");
        try {
            open();
        } catch (InterruptedException e) {
            log.error(fn + "Failed to establish association.", e);
            throw e;
        } catch (Exception e) {
            log.error(fn + "Failed to establish association.", e);
            throw new DcmMoveException("Send SCU Failed to establish association.", e);
        }

        log.debug(fn + "Checking transfer syntax compatibility");
        TransferCapability tc = assoc.getTransferCapabilityAsSCU(objClassUid);
        if (tc == null) {
            String str = UIDDictionary.getDictionary().prompt(objTsUid) + " not supported by " + remoteAE.getAETitle();
            log.error(fn + str);
            throw new DcmMoveException("Send SCU - " + str);
        }

        String selectedTsUid = selectTransferSyntax(tc.getTransferSyntax(), objTsUid);
        if (selectedTsUid == null) {
            String str = UIDDictionary.getDictionary().prompt(objClassUid) + " with " + UIDDictionary.getDictionary().prompt(objTsUid)
                    + " not supported by" + remoteAE.getAETitle();
            log.error(fn + str);
            throw new DcmMoveException("Send SCU - " + str);
        }

        try {
            log.debug(fn + "Starting the C-STORE response listener.");
            DimseRSPHandler rspHandler = new DimseRSPHandler() {
                @Override
                public void onDimseRSP(Association as, DicomObject cmd, DicomObject data) {
                    DcmSnd.this.onDimseRSP(as, cmd, data);
                }
            };

            log.debug(fn + "Sending the Dicom object...");
            assoc.cstore(objClassUid, objInstanceUid, priority, new DataWriterAdapter(dcmObj), selectedTsUid, rspHandler);

        } catch (InterruptedException e) {
            log.error(fn + "Failed to send " + objInstanceUid, e);
            throw e;
        } catch (Exception e) {
            log.error(fn + "Failed to send " + objInstanceUid, e);
            throw new DcmMoveException("Send SCU - Failed to send " + objInstanceUid, e);
        }

        log.debug(fn + "Waiting for response");
        try {
            assoc.waitForDimseRSP();
        } catch (InterruptedException e) {
            log.error(fn + "Exception ocurred while waiting for C-STORE response.", e);
            throw e;
        }

        if (null != storeResponseError) {
            close();
            log.error(fn + "Received error in store response - throwing exception.");
            throw new DcmMoveException(storeResponseError);
        }

        // Initiate storage commitment and wait for response
        if (isStorageCommitment()) {
            stgCmtResult = null;
            log.debug(fn + "Performing storage commitment");
            if (commit(objClassUid, objInstanceUid)) {
                DicomObject cmtrslt;
                try {
                    log.debug(fn + "Waiting for storage commitment result (N-EVENT-REPORT) ...");
                    cmtrslt = waitForStgCmtResult();
                } catch (InterruptedException e) {
                    log.error(fn + "Exception ocurred while waiting for storage commitment result (N-EVENT-REPORT).", e);
                    throw e;
                }
                int resultType = cmtrslt.getInt(Tag.EventTypeID);
                if (resultType == 2) {
                    DicomObject failedItem = cmtrslt.get(Tag.FailedSOPSequence).getDicomObject();
                    String failedInstanceUid = failedItem.getString(Tag.ReferencedSOPInstanceUID);
                    // failedInstanceUid should equal objInstanceUid
                    Integer reason = new Integer(failedItem.getInt(Tag.FailureReason));
                    log.error(fn + "Storage commitment for SOP instance '" + failedInstanceUid + "' failed for reason " + reason);
                    movedDcmObject.setStorageCommitFailedReason(reason);
                } else {
                    log.debug(fn + "Storage commitment result - success.");
                }
            }
        }

        // Check if occurred during storage commit
        if (null != storeResponseError) {
            close();
            log.error(fn + "Detected a store response error.");
            throw new DcmMoveException(storeResponseError);
        }

        log.debug(fn + "Closing association");
        close();

        fireStudyObjectSentEvent(movedDcmObject);
    }

    private boolean commit(String cUid, String iUid) throws DcmMoveException, InterruptedException {
        final String fn = "commit: ";

        log.debug(fn + "Performing storage commitment request (N-ACTION) ... ");

        DicomObject actionInfo = new BasicDicomObject();
        actionInfo.putString(Tag.TransactionUID, VR.UI, UIDUtils.createUID());
        DicomElement refSOPSq = actionInfo.putSequence(Tag.ReferencedSOPSequence);
        BasicDicomObject refSOP = new BasicDicomObject();
        refSOP.putString(Tag.ReferencedSOPClassUID, VR.UI, cUid);
        refSOP.putString(Tag.ReferencedSOPInstanceUID, VR.UI, iUid);
        refSOPSq.addDicomObject(refSOP);

        try {
            DimseRSP rsp = assoc.naction(UID.StorageCommitmentPushModelSOPClass, UID.StorageCommitmentPushModelSOPInstance,
                    STG_CMT_ACTION_TYPE, actionInfo, UID.ImplicitVRLittleEndian);
            rsp.next();
            DicomObject cmd = rsp.getCommand();
            int status = cmd.getInt(Tag.Status);
            if (status == 0) {
                log.debug(fn + "Storage commitment request succeeded.");
                return true;
            }
            storeResponseError = "Storage Commitment request failed with status: " + StringUtils.shortToHex(status) + "H. Response:\n"
                    + cmd.toString() + "\nError msg:\n" + cmd.getString(Tag.ErrorComment);
            log.error(fn + storeResponseError);
        } catch (InterruptedException e) {
            log.error(fn + "Failed to perform storage commitment request.", e);
            throw e;
        } catch (NoPresentationContextException e) {
            log.error(fn + "Cannot request storage commitment.", e);
            throw new DcmMoveException("Cannot request storage commitment.", e);
        } catch (IOException e) {
            log.error(fn + "Failed to send Storage Commitment request.", e);
            throw new DcmMoveException("Failed to send Storage Commitment request.", e);
        }

        return false;
    }

    private String selectTransferSyntax(String[] available, String tsuid) {
        if (tsuid.equals(UID.ImplicitVRLittleEndian))
            return selectTransferSyntax(available, IVLE_TS);
        if (tsuid.equals(UID.ExplicitVRLittleEndian))
            return selectTransferSyntax(available, EVLE_TS);
        if (tsuid.equals(UID.ExplicitVRBigEndian))
            return selectTransferSyntax(available, EVBE_TS);
        return tsuid;
    }

    private String selectTransferSyntax(String[] available, String[] tsuids) {
        for (int i = 0; i < tsuids.length; i++)
            for (int j = 0; j < available.length; j++)
                if (available[j].equals(tsuids[i]))
                    return available[j];
        return null;
    }

    public void close() throws InterruptedException {
        assoc.release(false);
    }

    private void onDimseRSP(Association as, DicomObject cmd, DicomObject data) {
        final String fn = "onDimseRSP: ";

        int status = cmd.getInt(Tag.Status);
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        switch (status) {
        case 0:
            ++objectsSent;
            log.debug(fn + "Successfully sent object with message id " + Integer.toString(msgId));
            break;
        case 0xB000:
        case 0xB006:
        case 0xB007:
            ++objectsSent;
            log.warn(fn + "Received WARNING RSP with Status " + Integer.toString(status) + " and message id " + Integer.toString(msgId));
            break;
        default:
            storeResponseError = "Received RSP with Status " + Integer.toString(status) + " and message id " + Integer.toString(msgId)
                    + " and error msg:\n" + cmd.getString(Tag.ErrorComment);
            log.error(fn + storeResponseError);
        }
    }

    @Override
    protected synchronized void onNEventReportRSP(Association as, int pcid, DicomObject rq, DicomObject info, DicomObject rsp) {
        stgCmtResult = info;
        // Add the event type id from the command set
        stgCmtResult.putInt(Tag.EventTypeID, VR.US, rq.getInt(Tag.EventTypeID));
        notifyAll();
    }

    public void logConfiguration(StringBuffer str) {
        str.append("\n\tCalled AET: " + remoteAE.getAETitle());
        str.append("\n\tCalling AET: " + ae.getAETitle());
        str.append("\n\tPerform Storage Commit: " + (stgcmt ? "true" : "false"));
        str.append("\n\tHostname (for Storage Commit): " + conn.getHostname());
        str.append("\n\tPort (for Storage Commit): " + conn.getPort());
        str.append("\n\tRemote Hostname: " + remoteConn.getHostname());
        str.append("\n\tRemote Port: " + remoteConn.getPort());
        str.append("\n\tTransfer Capability:");
        for (TransferCapability tc : ae.getTransferCapability()) {
            String sopClassName = UIDDictionary.getDictionary().nameOf(tc.getSopClass());
            str.append("\n\t\tSOP Class[" + sopClassName + "]  Transfer Syntaxes:");
            for (String xferSyntaxUid : tc.getTransferSyntax()) {
                str.append("\n\t\t\t" + UIDDictionary.getDictionary().nameOf(xferSyntaxUid));
            }
        }
    }

    public void addMoveActionListener(ActionListener al) {
        moveActionListener = al;
    }

    private void fireStudyObjectSentEvent(final MovedDicomObject obj) {
        final ActionEvent event = new StudyObjectMoveEvent(this, obj, MoveEvents.STUDY_OBJECT_SENT);
        eventExecutor.execute(new Runnable() {
            public void run() {
                moveActionListener.actionPerformed(event);
            }
        });
    }
}
