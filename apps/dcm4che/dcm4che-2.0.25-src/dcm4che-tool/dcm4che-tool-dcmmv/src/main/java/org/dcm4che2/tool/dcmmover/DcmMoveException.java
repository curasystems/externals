package org.dcm4che2.tool.dcmmover;

/**
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 */
class DcmMoveException extends Exception {
    static final long serialVersionUID = 1L;

    public DcmMoveException(String message, Throwable cause) {
        super(message, cause);
    }

    public DcmMoveException(Throwable cause) {
        super(cause);
    }

    public DcmMoveException(String message) {
        super(message, null);
    }
}
