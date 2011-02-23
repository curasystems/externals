package org.dcm4che2.tool.dcmmover;

import java.util.HashMap;
import java.util.List;

/**
 * Concrete implemenation of the MoveResponse interface.
 * 
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 */
class MoveResponseImpl implements MoveResponse {

    final static String STUDY_ELEMENT_NAME = "study";

    final static String SERIES_ELEMENT_NAME = "series";

    final static String OBJECT_ELEMENT_NAME = "object";

    final static String FAILED_STORAGE_COMMIT_ELEMENT_NAME = "failedStorageCommitment";

    String uidOfStudyToMove;

    String uidMappingDoc;

    String storageCommitFailuresDoc;

    String error;

    String moveSourceAeTitle;

    String moveDestinationAeTitle;

    int numberOfFoundObjects;

    int numberOfRetrievedStudyObjects;

    int numberOfReceivedObjects;

    int numberOfTransformedObjects;

    int numberOfSentObjects;

    int numberOfMovedObjects;

    int numberOfFoundSeries;

    int numberOfStorageCommitFailures;

    boolean transforming = false;

    boolean moveSuccessful = false;

    public MoveResponseImpl(String StudyUid, String moveSourceAe, String moveDestAe, boolean xforming) {
        uidOfStudyToMove = StudyUid;
        moveSourceAeTitle = moveSourceAe;
        moveDestinationAeTitle = moveDestAe;
        transforming = xforming;
    }

    public void setNumberOfFoundStudyObjects(int num) {
        numberOfFoundObjects = num;
    }

    public void setNumberOfRetrievedStudyObjects(int num) {
        numberOfRetrievedStudyObjects = num;
    }

    public void setNumberOfReceivedStudyObjects(int num) {
        numberOfReceivedObjects = num;
    }

    public void setNumberOfTransformedStudyObjects(int num) {
        numberOfTransformedObjects = num;
    }

    public void setNumberOfSentStudyObjects(int num) {
        numberOfSentObjects = num;
    }

    public void setNumberOfMovedStudyObjects(int num) {
        numberOfMovedObjects = num;
    }

    public void setNumberOfFoundStudySeries(int num) {
        numberOfFoundSeries = num;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setMoveSuccessful() {
        moveSuccessful = true;
    }

    public void setMoveFailed() {
        moveSuccessful = false;
    }

    public void setStorageCommitFailedReason(String objectUid, String reason) {
        StringBuffer stgCmmtFailuresBuffer = new StringBuffer((storageCommitFailuresDoc == null ? "" : storageCommitFailuresDoc));
        stgCmmtFailuresBuffer.append("<" + FAILED_STORAGE_COMMIT_ELEMENT_NAME + " objectUid='" + objectUid + "' reason='" + reason + "'/>");
        storageCommitFailuresDoc = stgCmmtFailuresBuffer.toString();
        numberOfStorageCommitFailures++;
    }

    public void setStudyUidMapping(HashMap<String, UidTreePair> uidMap) {
        if (uidMap == null) {
            return;
        }
        UidTreePair uidMapping = uidMap.get(uidOfStudyToMove);
        StringBuffer uidMappingDocBuffer = new StringBuffer(openStudyUidElem(uidMapping.getOldUid(), uidMapping.getNewUid()));
        createSeriesUidMappingElements(uidMapping.getChildren(), uidMappingDocBuffer);
        uidMappingDocBuffer.append(closeStudyUidElem());
        uidMappingDoc = uidMappingDocBuffer.toString();
    }

    private void createSeriesUidMappingElements(List<UidTreePair> seriesUids, StringBuffer docBuf) {
        for (UidTreePair seriesUid : seriesUids) {
            docBuf.append(openSeriesUidElem(seriesUid.getOldUid(), seriesUid.getNewUid()));
            createObjectUidMappingElements(seriesUid.getChildren(), docBuf);
            docBuf.append(closeSeriesUidElem());
        }
    }

    private void createObjectUidMappingElements(List<UidTreePair> objectUids, StringBuffer docBuf) {
        for (UidTreePair objectUid : objectUids) {
            docBuf.append(createObjectUidElem(objectUid.getOldUid(), objectUid.getNewUid()));
        }
    }

    private String openStudyUidElem(String oldUid, String newUid) {
        return "<" + STUDY_ELEMENT_NAME + " oldUid='" + oldUid + "' newUid='" + newUid + "'>";
    }

    private String closeStudyUidElem() {
        return "</" + STUDY_ELEMENT_NAME + ">";
    }

    private String openSeriesUidElem(String oldUid, String newUid) {
        return "<" + SERIES_ELEMENT_NAME + " oldUid='" + oldUid + "' newUid='" + newUid + "'>";
    }

    private String closeSeriesUidElem() {
        return "</" + SERIES_ELEMENT_NAME + ">";
    }

    private String createObjectUidElem(String oldUid, String newUid) {
        return "<" + OBJECT_ELEMENT_NAME + " oldUid='" + oldUid + "' newUid='" + newUid + "'/>";
    }

    /*
     * MoveResponse interface implementation
     */

    public boolean moveSuccessful() {
        return moveSuccessful;
    }

    public String getError() {
        return (error == null) ? "" : error;
    }

    public String getMoveDestinationAeTitle() {
        return moveSourceAeTitle;
    }

    public String getMoveSourceAeTitle() {
        return moveDestinationAeTitle;
    }

    public int getNumberOfStudyObjectsMoved() {
        return numberOfMovedObjects;
    }

    public String getUidMappingDoc() {
        return uidMappingDoc;
    }

    public String getStorageCommitFailuresDoc() {
        return storageCommitFailuresDoc;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("\n\tStudy Move Successful: " + new Boolean(moveSuccessful).toString());
        if (null != error)
            str.append("\n\tError: " + error);
        str.append("\n\tMove Source AE TItle: " + moveSourceAeTitle);
        str.append("\n\tMove Destination AE Title: " + moveDestinationAeTitle);
        str.append("\n\tNumber of Study Series Found: " + numberOfFoundSeries);
        str.append("\n\tNumber of Study Objects Found: " + numberOfFoundObjects);
        str.append("\n\tNumber of Retrieved Study Objects: " + numberOfRetrievedStudyObjects);
        str.append("\n\tNumber of Received Study Objects: " + numberOfReceivedObjects);
        if (transforming) {
            str.append("\n\tNumber of Transformed Study Objects: " + numberOfTransformedObjects);
        }
        str.append("\n\tNumber of Study Objects Moved: " + numberOfMovedObjects);
        if (numberOfStorageCommitFailures > 0) {
            str.append("\n\tNumber of Storage Commit Failures: " + numberOfStorageCommitFailures);
        }

        return str.toString();
    }

}
