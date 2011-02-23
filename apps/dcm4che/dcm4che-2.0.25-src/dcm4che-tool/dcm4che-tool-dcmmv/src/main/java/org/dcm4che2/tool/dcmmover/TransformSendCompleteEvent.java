package org.dcm4che2.tool.dcmmover;

import java.awt.event.ActionEvent;

/**
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 */
class TransformSendCompleteEvent extends ActionEvent {
    static final long serialVersionUID = 1L;

    String errorThatOccurred;

    public TransformSendCompleteEvent(Object source, String error) {
        super(source, MoveEvents.TRANSFORMER_SENDER_COMPLETED, null);
        errorThatOccurred = error;
    }

    public String getError() {
        return errorThatOccurred;
    }
}
