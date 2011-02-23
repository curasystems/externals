package org.dcm4che2.audit.message;

/**
 * This message describes the event of a procedure record being created,
 * accessed, modified, accessed, or deleted.  This message may only include
 * information about a single Procedure.
 * 
 * <blockquote>
 * Notes:<ol>
 * <li>DICOM applications often manipulate procedure records, e.g. with
 * MPPS update.  Modality Worklist query events are described by the Query
 * event.</li>
 * <li>The same accession number may appear with several order numbers.
 * The Study participant fields or the entire message may be repeated to
 * capture such many to many relationships.</li>
 * </ol>
 * </blockquote>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 750 $ $Date: 2007-02-12 18:37:51 +0100 (Mon, 12 Feb 2007) $
 * @since Nov 23, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.12 Procedure Record</a>
 */
public class ProcedureRecordMessage extends AuditMessageSupport {
    
    public ProcedureRecordMessage(AuditEvent.ActionCode action) {
        super(AuditEvent.ID.PROCEDURE_RECORD, action);
    }

    public ParticipantObject addStudy(String uid,
            ParticipantObjectDescription desc) {
        return addParticipantObject(ParticipantObject.createStudy(uid, desc));
    }
}