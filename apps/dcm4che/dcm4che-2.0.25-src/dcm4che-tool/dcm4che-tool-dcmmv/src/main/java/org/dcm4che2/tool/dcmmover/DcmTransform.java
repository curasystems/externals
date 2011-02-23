package org.dcm4che2.tool.dcmmover;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.util.TagUtils;
import org.dcm4che2.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides study object anonymization capability for the DcmMover.
 * 
 * @author gpotter (gcac96@gmail.com)
 */
class DcmTransform {

    static Logger log = LoggerFactory.getLogger(DcmTransform.class);

    private ObjectTransformData xformObjectData;

    private BlockingQueue<MovedDicomObject> movedObjQueue;

    private DcmSnd dcmSnd;

    private boolean stop;

    private int objectsTransformed;

    Executor eventExecutor = new NewThreadExecutor("TRNSFRM_SND_EVENTS");

    private ActionListener moveActionListener;

    public DcmTransform(DcmSnd dcmSnd) {
        this.dcmSnd = dcmSnd;
    }

    /**
     * Pops study objects off the queue, anonymizes them (if so configured), and
     * passes them to the Storage SCU.
     * 
     * @param objIod
     *            Patient and study data to anonymize the objects with.
     * @param dcmObjectQueue
     *            Queue that is monitored for study objects.
     */
    public void start(ObjectTransformData objIod, BlockingQueue<MovedDicomObject> dcmObjectQueue) {
        movedObjQueue = dcmObjectQueue;
        this.xformObjectData = objIod;
        stop = false;
        objectsTransformed = 0;

        new NewThreadExecutor("TRNSFRM_SND").execute(new Runnable() {
            public void run() {
                final String fn = "run: ";
                Exception exception = null;
                try {
                    while (!stop || !movedObjQueue.isEmpty()) {
                        log.debug(fn + "Popping object off the queue.");
                        Object queuedObj = movedObjQueue.take();
                        if (QueueWakeupObject.class.isInstance(queuedObj)) {
                            log.info(fn + "Popped Wakeup object off the queue. (this.stop=" + new Boolean(stop).toString() + ")");
                            if (!movedObjQueue.isEmpty()) {
                                log.info(fn + "Queue is not empty! Pushing Wakeup object back to end of the queue.");
                                movedObjQueue.put((QueueWakeupObject) queuedObj);
                            }
                        } else {
                            log.debug(fn + "Popped Dicom object off the queue.");
                            MovedDicomObject xformedObj = transform((MovedDicomObject) queuedObj);
                            log.debug(fn + "Passing the object to the Dicom Sender...");
                            dcmSnd.send(xformedObj);
                        }
                    }
                } catch (DcmMoveException e) {
                    log.error(fn + "Exception ocurred while transforming and sending.", e);
                    exception = e;
                } catch (InterruptedException e) {
                    log.error(fn + "Thread InterruptedException ocurred.", e);
                    exception = e;
                }
                log.debug(fn + "Breaking out of TRNSFRM_SND thread and releasing the moved object queue.");
                movedObjQueue = null;
                stop = true;
                log.info(fn + "Signaling end of transform-send thread.");
                fireTransformSendCompletedEvent(exception);
            }
        });
    }

    class QueueWakeupObject extends MovedDicomObject {
        public QueueWakeupObject() {
            super(null, null);
        }
    }

    public void stop() {
        if (stop == true) {
            return;
        }
        stop = true;
        if (null != movedObjQueue) {
            movedObjQueue.add(new QueueWakeupObject());
        }
    }

    /**
     * Reads the dicom object from the input stream, transforms it, and returns
     * a new input stream containing the transformed object.
     * 
     * @param dcmObjStream
     * @return
     */
    private MovedDicomObject transform(MovedDicomObject movedDcmObj) {
        final String fn = "transform: ";

        if (null == xformObjectData) {
            log.info(fn + "Data transformation object is null - not modifying DICOM object.");
            return movedDcmObj;
        }

        log.info(fn + "Transforming the Dicom Object with attributes from the data transformation object.");

        // Replace elements in the moved dicom object with elements from
        // xformObjectData
        DicomObject dcmObj = movedDcmObj.getDicomObject();
        for (Iterator dsi = xformObjectData.getDicomObject().datasetIterator(); dsi.hasNext();) {
            DicomElement newDcmElem = (DicomElement) dsi.next();

            if (log.isDebugEnabled()) {
                DicomElement oldElem = dcmObj.get(newDcmElem.tag());
                if (oldElem == null) {
                    log.debug(fn + "Adding attribute [" + newDcmElem.toString() + "]");
                } else {
                    log.debug(fn + "Replacing attribute [" + oldElem.toString() + "] with [" + newDcmElem.toString() + "]");
                }
            }

            dcmObj.remove(newDcmElem.tag());
            dcmObj.add(newDcmElem);
        }

        // Remove object attributes
        for (Iterator<Integer> li = xformObjectData.getAttrsToRemoveList().iterator(); li.hasNext();) {
            int tag = li.next().intValue();
            log.debug(fn + "Removing attribute [" + TagUtils.toString(tag) + "]");
            dcmObj.remove(tag);
        }

        log.info(fn + "Generating new UID's for moved dicom object");

        // Generate a new study uid
        DicomElement studyUidElem = dcmObj.remove(Tag.StudyInstanceUID);
        String oldStudyUid = (studyUidElem == null) ? UIDUtils.createUID() : studyUidElem.getString(null, true);
        String newStudyUid = movedDcmObj.getNewStudyUid(oldStudyUid);
        // Add a new study uid element
        dcmObj.putString(Tag.StudyInstanceUID, VR.UI, newStudyUid);
        log.debug(fn + "Replaced Study UID [" + oldStudyUid + "] with new Study UID [" + newStudyUid + "]");

        // Generate a new series uid
        DicomElement seriesUidElem = dcmObj.remove(Tag.SeriesInstanceUID);
        String oldSeriesUid = (seriesUidElem == null) ? UIDUtils.createUID() : seriesUidElem.getString(null, true);
        String newSeriesUid = movedDcmObj.getNewSeriesUid(oldSeriesUid, oldStudyUid);
        // Add a new study uid element
        dcmObj.putString(Tag.SeriesInstanceUID, VR.UI, newSeriesUid);
        log.debug(fn + "Replaced Series UID [" + oldSeriesUid + "] with new Series UID [" + newSeriesUid + "]");

        // Generate a new object uid
        DicomElement objectUidElem = dcmObj.remove(Tag.SOPInstanceUID);
        String oldObjectUid = (objectUidElem == null) ? UIDUtils.createUID() : objectUidElem.getString(null, true);
        String newObjectUid = movedDcmObj.getNewObjectUid(oldObjectUid, oldSeriesUid, oldStudyUid);
        // Add a new study uid element
        dcmObj.putString(Tag.SOPInstanceUID, VR.UI, newObjectUid);
        log.debug(fn + "Replaced Object UID [" + oldObjectUid + "] with new Object UID [" + newObjectUid + "]");

        objectsTransformed++;

        fireStudyObjectTransformedEvent(movedDcmObj);

        return movedDcmObj;
    }

    public void addMoveActionListener(ActionListener al) {
        moveActionListener = al;
    }

    public void fireTransformSendCompletedEvent(Exception e) {
        final ActionEvent event = new TransformSendCompleteEvent(this, (null == e ? null : e.getMessage()));
        eventExecutor.execute(new Runnable() {
            public void run() {
                moveActionListener.actionPerformed(event);
            }
        });
    }

    public void fireStudyObjectTransformedEvent(final MovedDicomObject obj) {
        final ActionEvent event = new StudyObjectMoveEvent(this, obj, MoveEvents.STUDY_OBJECT_TRANSFORMED);
        eventExecutor.execute(new Runnable() {
            public void run() {
                moveActionListener.actionPerformed(event);
            }
        });
    }

    public int getTotalTransformed() {
        return objectsTransformed;
    }
}
