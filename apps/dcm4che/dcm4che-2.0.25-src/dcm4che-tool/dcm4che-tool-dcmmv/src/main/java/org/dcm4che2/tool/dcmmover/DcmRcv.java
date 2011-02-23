package org.dcm4che2.tool.dcmmover;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.StorageService;
import org.dcm4che2.net.service.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides DICOM Storage SCP capability for the DcmMover.
 * 
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 * @see org.dcm4che2.tool.DcmRcv Based on original work by gunter
 *      zeilinger(gunterze@gmail.com). Original version - Revision: 4892 $
 *      $Date: 2007-08-21 09:36:10 +0200 (Tue, 21 Aug 2007)
 */
class DcmRcv extends StorageService {

    static Logger log = LoggerFactory.getLogger(DcmRcv.class);

    private BlockingQueue<MovedDicomObject> movedObjQueue;

    private static final String[] NON_RETIRED_LE_TS = {
        UID.JPEGLSLossless,
        UID.JPEGLossless,
        UID.JPEGLosslessNonHierarchical14,
        UID.JPEG2000LosslessOnly,
        UID.DeflatedExplicitVRLittleEndian,
        UID.RLELossless,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian,
        UID.JPEGBaseline1,
        UID.JPEGExtended24,
        UID.JPEGLSLossyNearLossless,
        UID.JPEG2000,
        UID.MPEG2, };

    private static final String[] CUIDS = {
        UID.BasicStudyContentNotificationSOPClassRetired,
        UID.StoredPrintStorageSOPClassRetired,
        UID.HardcopyGrayscaleImageStorageSOPClassRetired,
        UID.HardcopyColorImageStorageSOPClassRetired,
        UID.ComputedRadiographyImageStorage,
        UID.DigitalXRayImageStorageForPresentation,
        UID.DigitalXRayImageStorageForProcessing,
        UID.DigitalMammographyXRayImageStorageForPresentation,
        UID.DigitalMammographyXRayImageStorageForProcessing,
        UID.DigitalIntraoralXRayImageStorageForPresentation,
        UID.DigitalIntraoralXRayImageStorageForProcessing,
        UID.StandaloneModalityLUTStorageRetired,
        UID.EncapsulatedPDFStorage,
        UID.StandaloneVOILUTStorageRetired,
        UID.GrayscaleSoftcopyPresentationStateStorageSOPClass,
        UID.ColorSoftcopyPresentationStateStorageSOPClass,
        UID.PseudoColorSoftcopyPresentationStateStorageSOPClass,
        UID.BlendingSoftcopyPresentationStateStorageSOPClass,
        UID.XRayAngiographicImageStorage,
        UID.EnhancedXAImageStorage,
        UID.XRayRadiofluoroscopicImageStorage,
        UID.EnhancedXRFImageStorage,
        UID.XRayAngiographicBiPlaneImageStorageRetired,
        UID.PositronEmissionTomographyImageStorage,
        UID.StandalonePETCurveStorageRetired,
        UID.CTImageStorage,
        UID.EnhancedCTImageStorage,
        UID.NuclearMedicineImageStorage,
        UID.UltrasoundMultiframeImageStorageRetired,
        UID.UltrasoundMultiframeImageStorage,
        UID.MRImageStorage,
        UID.EnhancedMRImageStorage,
        UID.MRSpectroscopyStorage,
        UID.RTImageStorage,
        UID.RTDoseStorage,
        UID.RTStructureSetStorage,
        UID.RTBeamsTreatmentRecordStorage,
        UID.RTPlanStorage,
        UID.RTBrachyTreatmentRecordStorage,
        UID.RTTreatmentSummaryRecordStorage,
        UID.NuclearMedicineImageStorageRetired,
        UID.UltrasoundImageStorageRetired,
        UID.UltrasoundImageStorage,
        UID.RawDataStorage,
        UID.SpatialRegistrationStorage,
        UID.SpatialFiducialsStorage,
        UID.RealWorldValueMappingStorage,
        UID.SecondaryCaptureImageStorage,
        UID.MultiframeSingleBitSecondaryCaptureImageStorage,
        UID.MultiframeGrayscaleByteSecondaryCaptureImageStorage,
        UID.MultiframeGrayscaleWordSecondaryCaptureImageStorage,
        UID.MultiframeTrueColorSecondaryCaptureImageStorage,
        UID.VLImageStorageTrialRetired,
        UID.VLEndoscopicImageStorage,
        UID.VideoEndoscopicImageStorage,
        UID.VLMicroscopicImageStorage,
        UID.VideoMicroscopicImageStorage,
        UID.VLSlideCoordinatesMicroscopicImageStorage,
        UID.VLPhotographicImageStorage,
        UID.VideoPhotographicImageStorage,
        UID.OphthalmicPhotography8BitImageStorage,
        UID.OphthalmicPhotography16BitImageStorage,
        UID.StereometricRelationshipStorage,
        UID.VLMultiframeImageStorageTrialRetired,
        UID.StandaloneOverlayStorageRetired,
        UID.BasicTextSRStorage,
        UID.EnhancedSRStorage,
        UID.ComprehensiveSRStorage,
        UID.ProcedureLogStorage,
        UID.MammographyCADSRStorage,
        UID.KeyObjectSelectionDocumentStorage,
        UID.ChestCADSRStorage,
        UID.StandaloneCurveStorageRetired,
        UID._12leadECGWaveformStorage,
        UID.GeneralECGWaveformStorage,
        UID.AmbulatoryECGWaveformStorage,
        UID.HemodynamicWaveformStorage,
        UID.CardiacElectrophysiologyWaveformStorage,
        UID.BasicVoiceAudioWaveformStorage,
        UID.HangingProtocolStorage,
        UID.SiemensCSANonImageStorage };

    private Executor executor = new NewThreadExecutor("DCMRCV");

    private Device device = new Device("DCMRCV");

    private NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private NetworkConnection nc = new NetworkConnection();

    private String[] tsuids = NON_RETIRED_LE_TS;

    private int objectsReceived;

    private int rspdelay = 0;

    Executor eventExecutor = new NewThreadExecutor("DCMRCV_EVENTS");

    private ActionListener moveActionListener;

    public DcmRcv() {
        super(CUIDS);
        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(nc);
        ae.setNetworkConnection(nc);
        ae.setAssociationAcceptor(true);
        ae.register(new VerificationService());
        ae.register(this);
    }

    public final void setAEtitle(String aet) {
        ae.setAETitle(aet);
    }

    public final void setHostname(String hostname) {
        nc.setHostname(hostname);
    }

    public final void setPort(int port) {
        nc.setPort(port);
    }

    public final void setPackPDV(boolean packPDV) {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period) {
        device.setAssociationReaperPeriod(period);
    }

    public final void setTcpNoDelay(boolean tcpNoDelay) {
        nc.setTcpNoDelay(tcpNoDelay);
    }

    public final void setRequestTimeout(int timeout) {
        nc.setRequestTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout) {
        nc.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int delay) {
        nc.setSocketCloseDelay(delay);
    }

    public final void setIdleTimeout(int timeout) {
        ae.setIdleTimeout(timeout);
    }

    public final void setDimseRspTimeout(int timeout) {
        ae.setDimseRspTimeout(timeout);
    }

    public final void setMaxPDULengthSend(int maxLength) {
        ae.setMaxPDULengthSend(maxLength);
    }

    public void setMaxPDULengthReceive(int maxLength) {
        ae.setMaxPDULengthReceive(maxLength);
    }

    public final void setReceiveBufferSize(int bufferSize) {
        nc.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize) {
        nc.setSendBufferSize(bufferSize);
    }

    public void setDimseRspDelay(int delay) {
        rspdelay = delay;
    }

    public void initTransferCapability() {
        TransferCapability[] tc = new TransferCapability[CUIDS.length + 1];
        tc[0] = new TransferCapability(UID.VerificationSOPClass, DcmMover.ONLY_IVRLE_TS, TransferCapability.SCP);
        for (int i = 0; i < CUIDS.length; i++)
            tc[i + 1] = new TransferCapability(CUIDS[i], tsuids, TransferCapability.SCP);
        ae.setTransferCapability(tc);
    }

    public int getTotalReceived() {
        return objectsReceived;
    }

    public void start(BlockingQueue<MovedDicomObject> objQueue) throws IOException {
        final String fn = "start: ";

        objectsReceived = 0;
        movedObjQueue = objQueue;
        device.startListening(executor);
        log.info(fn + "Started Server listening on port " + nc.getPort());
    }

    public void stop() {
        device.stopListening();
    }

    /**
     * Overwrite {@link StorageService#cstore} to send delayed C-STORE RSP by
     * separate Thread, so reading of following received C-STORE RQs from the
     * open association is not blocked.
     */
    @Override
    public void cstore(final Association as, final int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid)
            throws DicomServiceException, IOException {
        final String fn = "cstore: ";

        log.debug(fn + "Handling a C-Store...");

        final DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
        onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
        if (rspdelay > 0) {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(rspdelay);
                        as.writeDimseRSP(pcid, rsp);
                    } catch (Exception e) {
                        log.error(fn + "Exception ocurred while writing DIMSE response:", e);
                    }
                }
            });
        } else {
            as.writeDimseRSP(pcid, rsp);
        }
        onCStoreRSP(as, pcid, rq, dataStream, tsuid, rsp);
    }

    @Override
    protected void onCStoreRQ(Association as, int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid, DicomObject rsp)
            throws IOException, DicomServiceException {
        final String fn = "onCStoreRQ: ";

        log.debug(fn + "Handling a C-Store request. Adding the received DICOM object to the queue.");

        DicomInputStream dis = new DicomInputStream(new BufferedInputStream(dataStream), tsuid);
        DicomObject dcmObj = dis.readDicomObject();

        MovedDicomObject movedDcmObj = new MovedDicomObject(dcmObj, tsuid);

        objectsReceived++;

        fireStudyObjectReceivedEvent(movedDcmObj);

        movedObjQueue.add(movedDcmObj);
    }

    public void logConfiguration(StringBuffer str) {
        str.append("\n\tAET: " + ae.getAETitle());
        str.append("\n\tHostname: " + nc.getHostname());
        str.append("\n\tPort: " + nc.getPort());
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

    public void fireStudyObjectReceivedEvent(final MovedDicomObject obj) {
        final ActionEvent event = new StudyObjectMoveEvent(this, obj, MoveEvents.STUDY_OBJECT_RECEIVED);
        eventExecutor.execute(new Runnable() {
            public void run() {
                moveActionListener.actionPerformed(event);
            }
        });
    }
}
