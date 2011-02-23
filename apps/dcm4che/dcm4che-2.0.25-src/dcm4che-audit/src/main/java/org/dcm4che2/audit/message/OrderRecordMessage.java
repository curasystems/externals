package org.dcm4che2.audit.message;

/**
 * This message describes the event of an order being  created, modified,
 * accessed, or deleted.  This message may only include information about 
 * a single patient.
 * 
 * <blockquote>
 * Note: An order record typically is managed by a non-DICOM system.
 * However, DICOM applications often manipulate order records, and thus
 * may be obligated by site security policies to record such events in 
 * the audit logs.
 * </blockquote>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 750 $ $Date: 2007-02-12 18:37:51 +0100 (Mon, 12 Feb 2007) $
 * @since Nov 23, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.10 Order Record</a>
 */
public class OrderRecordMessage extends AuditMessageSupport {

    public OrderRecordMessage(AuditEvent.ActionCode action) {
        super(AuditEvent.ID.ORDER_RECORD, action);
    }

}