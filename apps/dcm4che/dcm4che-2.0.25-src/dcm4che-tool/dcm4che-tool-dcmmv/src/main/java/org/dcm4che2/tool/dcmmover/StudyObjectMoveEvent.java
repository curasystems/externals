package org.dcm4che2.tool.dcmmover;

import java.awt.event.ActionEvent;

/**
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 */
class StudyObjectMoveEvent extends ActionEvent {
    static final long serialVersionUID = 1L;

    private MovedDicomObject movedDicomObject;

    public StudyObjectMoveEvent(Object source, MovedDicomObject obj, int eventId) {
        super(source, eventId, null);
        movedDicomObject = obj;
    }

    public MovedDicomObject getMovedDicomObject() {
        return movedDicomObject;
    }
}
