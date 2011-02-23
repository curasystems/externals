package org.dcm4che2.tool.dcmmover;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.dcm4che2.data.UID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates the retrieval, transformation, and sending of a DICOM Study.
 * 
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 */
class DcmMover {

    static String log4jConfigFileName = "log4j.xml";

    static Logger log = LoggerFactory.getLogger(DcmMover.class);

    public static final String[] ONLY_IVRLE_TS = { UID.ImplicitVRLittleEndian };

    // The components this class uses to actually perform the move
    private DcmQR dcmQR;

    private DcmRcv dcmRcv;

    private DcmSnd dcmSnd;

    private DcmTransform dcmTransform;

    // Sync's the main thread with the transformer/sender thread
    private AtomicBoolean transformerSenderRunning = new AtomicBoolean(false);

    private AtomicReference<String> transformerSenderError = new AtomicReference<String>();

    private boolean abortingQR;

    private MoveResponseImpl studyMoveResponse;

    public DcmMover() {

        initializeLogging();

        dcmQR = new DcmQR();
        dcmRcv = new DcmRcv();
        dcmSnd = new DcmSnd();
        dcmTransform = new DcmTransform(dcmSnd);

        // Do some initial configuration of the receive scp
        dcmRcv.setHostname("127.0.0.1");
        dcmRcv.setPort(104);
        dcmRcv.initTransferCapability();

        // Do some initial configuration of the query/retrieve scu
        dcmQR.setRemoteHost("127.0.0.1");
        dcmQR.setRemotePort(104);

        // Do some initial configuration of the send scu
        dcmSnd.setRemoteHost("127.0.0.1");
        dcmSnd.setRemotePort(104);
        dcmSnd.setOfferDefaultTransferSyntaxInSeparatePresentationContext(false);
        dcmSnd.setStorageCommitment(false);
        dcmSnd.configureTransferCapability();
    }

    protected static void initializeLogging() {
        // Attempt to load the logging configuration file from the current
        // directory. If that
        // fails attempt to get it from the classpath. If that fails create a
        // basic configuration.
        URL log4jConfigFileUrl = null;
        File logConfigFile = new File(log4jConfigFileName);
        if (logConfigFile.exists()) {
            try {
                log4jConfigFileUrl = logConfigFile.toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            log4jConfigFileUrl = DcmMover.class.getClassLoader().getResource(log4jConfigFileName);
        }
        if (null == log4jConfigFileUrl) {
            BasicConfigurator.configure();
        } else {
            DOMConfigurator.configure(log4jConfigFileUrl);
        }
    }

    /**
     * Set this app's AET.
     * 
     * @param aet
     */
    public void setAET(String aet) {
        dcmQR.setCalling(aet + "_QR_SCU");
        dcmQR.setMoveDest(aet); // Moving it to ourselves
        dcmRcv.setAEtitle(aet);
        dcmSnd.setCalling(aet + "_STR_SCU");
    }

    /**
     * Set this mover's host name.
     * 
     * @param host
     */
    public void setLocalHost(String host) {
        dcmQR.setLocalHost(host);
        dcmRcv.setHostname(host);
        dcmSnd.setLocalHost(host);
    }

    /**
     * Set the mover's Q/R SCU called AET. (The AET of the Q/R SCP)
     * 
     * @param aet
     */
    public void setQRSCUCalledAET(String aet) {
        dcmQR.setCalledAET(aet);
    }

    /**
     * Get the move source AET. This is the same as the QR SCU called AET.
     * 
     * @return aet
     */
    public String getMoveSourceAET() {
        return dcmQR.getRemoteAE();
    }

    /**
     * Set the mover's Q/R SCU remote host name. (The name of the host the SCP
     * is running on)
     * 
     * @param host
     */
    public void setQRSCURemoteHost(String host) {
        dcmQR.setRemoteHost(host);
    }

    /**
     * Set the mover's Q/R SCU remote port. (The port the Q/R SCP is listening
     * on)
     * 
     * @param port
     */
    public void setQRSCURemotePort(int port) {
        dcmQR.setRemotePort(port);
    }

    /**
     * Set the port that the mover will listen on for C-STOREs in response to
     * the C-MOVE requests.
     * 
     * @param port
     *            The port to listen for C-Store requests that are in response
     *            to C-MOVE requests.
     */
    public void setReceiveSCPListenPort(int port) {
        dcmRcv.setPort(port);
    }

    /**
     * Set the mover's send SCU called AET. (The AET of the receive SCP)
     * 
     * @param aet
     */
    public void setSendSCUCalledAET(String aet) {
        dcmSnd.setCalledAET(aet);
    }

    /**
     * Get the move destination AET. This is the same as the QR SCU called AET.
     * 
     * @return aet
     */
    public String getMoveDestinationAET() {
        return dcmSnd.getCalledAET();
    }

    /**
     * Set the mover's send SCU remote port. (The port the receive SCP is
     * listening on)
     * 
     * @param port
     */
    public void setSendSCURemotePort(int port) {
        dcmSnd.setRemotePort(port);
    }

    /**
     * Set the mover's send SCU remote host name. (The name of the host the
     * receive SCP is running on)
     * 
     * @param host
     */
    public void setSendSCURemoteHost(String host) {
        dcmSnd.setRemoteHost(host);
    }

    public void setStorageCommitment(boolean stgcmt) {
        dcmSnd.setStorageCommitment(stgcmt);
    }

    public void setStorageCommitmentHost(String host) {
        dcmSnd.setLocalHost(host);
    }

    public void setStorageCommitmentPort(int port) {
        dcmSnd.setLocalPort(port);
    }

    public void setConnectTimeout(int ms) {
        dcmQR.setConnectTimeout(ms);
        dcmSnd.setConnectTimeout(ms);
    }

    public void setDimseRspTimeout(int ms) {
        dcmQR.setDimseRspTimeout(ms);
        dcmRcv.setDimseRspTimeout(ms);
        dcmSnd.setDimseRspTimeout(ms);
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        dcmQR.setTcpNoDelay(tcpNoDelay);
        dcmRcv.setTcpNoDelay(tcpNoDelay);
        dcmSnd.setTcpNoDelay(tcpNoDelay);
    }

    public void setAcceptTimeout(int ms) {
        dcmQR.setAcceptTimeout(ms);
        dcmSnd.setAcceptTimeout(ms);
    }

    public void setReleaseTimeout(int ms) {
        dcmQR.setReleaseTimeout(ms);
        dcmSnd.setReleaseTimeout(ms);
    }

    // 1=high, 0=low, default=medium
    public void setMovePriority(int priority) {
        dcmQR.setPriority(priority);
        dcmSnd.setPriority(priority);
    }

    public MoveResponse getStudyMoveResponseObj() {
        return studyMoveResponse;
    }

    /***************************************************************************
     * Moves the study.
     * 
     * @param studyUid
     *            Study instance uid of the study to move from source to
     *            destination.
     * @param xformObjectData
     *            DICOM object attributes to be added/removed/xformed during the
     *            move. If null no attributes are modified in the dicom image
     *            objects as they are moved.
     * @return A MoveResponse object that encapsulates the result of the study
     *         move.
     */
    public MoveResponse moveStudy(String studyUid, ObjectTransformData xformObjectData) {
        final String fn = "moveStudy: ";

        abortingQR = false;
        initStudyMoveResponse(studyUid, xformObjectData != null);

        if (log.isInfoEnabled()) {
            logConfiguration(fn);

            String str = "Beginning study move process - studyUid=" + studyUid + "\nxformObjectData is ";
            if (xformObjectData == null) {
                str += "null. Study will not be transformed during move.";
            } else {
                str += "NOT null. Study will be transformed during move. Transform object data:\n" + xformObjectData.toString();
            }
            log.info(fn + str);
        }

        MovedDicomObject.reset();

        DcmMoveActionListener dcmMoveActionListener = new DcmMoveActionListener(this);
        dcmRcv.addMoveActionListener(dcmMoveActionListener);
        dcmSnd.addMoveActionListener(dcmMoveActionListener);
        dcmTransform.addMoveActionListener(dcmMoveActionListener);
        dcmQR.addMoveActionListener(dcmMoveActionListener);

        // Create a thread safe queue to pass the received study object to the
        // transform-send sub-process
        LinkedBlockingQueue<MovedDicomObject> movedObjectQueue = new LinkedBlockingQueue<MovedDicomObject>();

        // Start the receive scp
        log.debug(fn + "Starting the Dicom Receiver (Store SCP)");
        try {
            dcmRcv.start(movedObjectQueue);
        } catch (IOException e) {
            shutdownMove();
            log.error(fn + "Exception while starting the Dicom Receiver (Store SCP).", e);
            return finalizeStudyMoveResponse(e.getMessage());
        }

        // Start the send scu
        log.debug(fn + "Starting the Dicom Sender (Store SCU)");
        try {
            dcmSnd.start();
        } catch (IOException e) {
            shutdownMove();
            log.error(fn + "Exception while starting the Dicom Sender (Store SCU).", e);
            return finalizeStudyMoveResponse(e.getMessage());
        }

        // Start the dicom transformer
        log.debug(fn + "Starting the Dicom Transformer");
        dcmTransform.start(xformObjectData, movedObjectQueue);
        log.debug(fn + "Setting the Transformer-Sender running interlock flag to 'true'");
        transformerSenderRunning.getAndSet(true);

        // Do the study move
        log.debug(fn + "Starting Query/Retrieve (Q/R SCU) of study with uid " + studyUid);
        try {
            dcmQR.qrStudy(studyUid);
        } catch (DcmMoveException e) {
            if (abortingQR) {
                log.info(fn + "Ignoring Query/Retrieve exception because that processing was "
                        + "aborted due to an exception on the Transformer-Sender.");
            } else {
                shutdownMove();
                log.error(fn + "Exception while doing the Query/Retrieve (Q/R SCU).", e);
                return finalizeStudyMoveResponse(e.getMessage());
            }
        }

        // Shutdown the receiver, transformer, and sender
        log.debug(fn + "Shutting down the study move process.");
        shutdownMove();

        log.info(fn + "Completed study move process.");

        return finalizeStudyMoveResponse(transformerSenderError.get());
    }

    public boolean studyMoveInProgress() {
        return studyMoveResponse != null;
    }

    public int getNumberOfMovedStudyObjects() {
        return dcmSnd.getTotalSent();
    }

    public int getNumberOfFoundStudyObjects() {
        return dcmQR.getTotalFound();
    }

    private void initStudyMoveResponse(String studyUid, boolean transforming) {
        studyMoveResponse = new MoveResponseImpl(studyUid, dcmQR.getRemoteAE(), dcmSnd.getCalledAET(), transforming);
    }

    private MoveResponse finalizeStudyMoveResponse(String error) {
        final String fn = "finalizeStudyMoveResponse: ";

        studyMoveResponse.setNumberOfFoundStudySeries(dcmQR.getSeriesFound());
        studyMoveResponse.setNumberOfFoundStudyObjects(getNumberOfFoundStudyObjects());
        studyMoveResponse.setNumberOfRetrievedStudyObjects(dcmQR.getTotalRetrieved());
        studyMoveResponse.setNumberOfReceivedStudyObjects(dcmRcv.getTotalReceived());
        studyMoveResponse.setNumberOfTransformedStudyObjects(dcmTransform.getTotalTransformed());
        studyMoveResponse.setNumberOfSentStudyObjects(dcmSnd.getTotalSent());
        studyMoveResponse.setNumberOfMovedStudyObjects(getNumberOfMovedStudyObjects());

        if (null == error) {
            studyMoveResponse.setMoveSuccessful();
        } else {
            studyMoveResponse.setMoveFailed();
            studyMoveResponse.setError(error);
        }

        MoveResponse tmpMoveResponse = studyMoveResponse;
        studyMoveResponse = null; // Indicates a move is no longer in progress

        log.info(fn + "Move results:" + tmpMoveResponse.toString());

        return tmpMoveResponse;
    }

    /**
     * Does not return until the study move process has been shutdown.
     */
    protected void shutdownMove() {
        final String fn = "shutdownMove: ";

        log.debug(fn + "Stopping the Dicom Receiver (Store SCP)");
        dcmRcv.stop();
        log.debug(fn + "Stopping the Dicom Transformer");
        dcmTransform.stop();

        log.debug(fn + "Waiting for the Dicom Sender to complete");
        while (transformerSenderRunning.get()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error(fn + "Exception caught while waiting for the Transformer and Sender to complete.", e);
            }
        }

        log.debug(fn + "Stopping the Dicom Sender (Store SCU)");
        dcmSnd.stop();
    }

    private void logConfiguration(String ctx) {
        String str = ctx + getConfigurationString();
        log.debug(str);
    }

    private String getConfigurationString() {
        StringBuffer str = new StringBuffer();
        str.append("Dicom Mover Configuration:");
        str.append("\nQuery/Retrieve SCU (initiates move) configuration:");
        dcmQR.logConfiguration(str);
        str.append("\nReceive SCP (intermediate destination) configuration:");
        dcmRcv.logConfiguration(str);
        str.append("\nSend SCU (send to move destination) configuration:");
        dcmSnd.logConfiguration(str);
        return str.toString();
    }

    protected void handleMoveEvent(ActionEvent e) {
        final String fn = "handleMoveEvent: ";

        switch (e.getID()) {
        case MoveEvents.STUDY_OBJECT_RECEIVED:
            log.debug(fn + "Handling a STUDY_OBJECT_RECEIVED event");
            break;
        case MoveEvents.STUDY_OBJECT_TRANSFORMED:
            log.debug(fn + "Handling a STUDY_OBJECT_TRANSFORMED event");
            break;
        case MoveEvents.STUDY_OBJECT_SENT:
            log.debug(fn + "Handling a STUDY_OBJECT_SENT event");
            if (StudyObjectMoveEvent.class.isInstance(e)) {
                // Update the uid mapping doc in the MoveReponse object
                StudyObjectMoveEvent event = (StudyObjectMoveEvent) e;
                studyMoveResponse.setStudyUidMapping(event.getMovedDicomObject().getStudyUidMap());
                if (event.getMovedDicomObject().storageCommitFailed()) {
                    // Update storage commit failures int the MoveResponse
                    // object
                    studyMoveResponse.setStorageCommitFailedReason(event.getMovedDicomObject().getInstanceUid(),
                            event.getMovedDicomObject().getStorageCommitFailedReason());
                }
            }
            break;
        case MoveEvents.STUDY_OBJECT_MOVED:
            log.debug(fn + "Handling a STUDY_OBJECT_MOVED event");
            break;
        case MoveEvents.TRANSFORMER_SENDER_COMPLETED:
            log.debug(fn + "Handling a TRANSFORMER_SENDER_COMPLETED event");
            if (!transformerSenderRunning.compareAndSet(true, false)) {
                log.error(fn + "Transformer-Sender running interlock flag already 'false'.");
            }
            if (TransformSendCompleteEvent.class.isInstance(e)) {
                TransformSendCompleteEvent event = (TransformSendCompleteEvent) e;
                String error = event.getError();
                if (null != error) {
                    log.error(fn + "Transformer Sender shutdown because of an error: " + error);
                    transformerSenderError.set(error);
                    log.debug(fn + "Aborting the Dicom Query/Retriever (Q/R SCU)");
                    abortingQR = true;
                    dcmQR.abort();
                }
            }
            break;
        }
    }

    class DcmMoveActionListener implements ActionListener {
        DcmMover rsMover;

        public DcmMoveActionListener(DcmMover mover) {
            rsMover = mover;
        }

        public void actionPerformed(ActionEvent e) {
            rsMover.handleMoveEvent(e);
        }
    }
}
