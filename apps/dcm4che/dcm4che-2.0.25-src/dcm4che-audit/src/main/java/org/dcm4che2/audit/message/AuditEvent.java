/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
 
package org.dcm4che2.audit.message;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Event Identification. Identifies the name, action type, time, and
 * disposition of the audited event.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 14557 $ $Date: 2010-12-16 13:10:55 +0100 (Thu, 16 Dec 2010) $
 * @since Nov 17, 2006
 */
public class AuditEvent extends BaseElement {

    private final ID eventID;
    private final ArrayList<TypeCode> eventTypeCodes = new ArrayList<TypeCode>(
            1);

    public AuditEvent(ID eventID, ActionCode action) {
        super("EventIdentification");
        if (eventID == null) {
            throw new NullPointerException();
        }
        this.eventID = eventID;
        addAttribute("EventActionCode", action, true);
        setEventDateTime(new Date());
        setOutcomeIndicator(OutcomeIndicator.SUCCESS);
    }

    public ID getEventID() {
        return eventID;
    }

    public ActionCode getEventActionCode() {
        return (ActionCode) getAttribute("EventActionCode");
    }

    public Date getEventDateTime() {
        return (Date) getAttribute("EventDateTime");
    }

    public AuditEvent setEventDateTime(Date datetime) {
        addAttribute("EventDateTime", datetime, false);
        return this;
    }
    
    public OutcomeIndicator getEventOutcomeIndicator() {
        return (OutcomeIndicator) getAttribute("EventOutcomeIndicator");
    }
    
    public AuditEvent setOutcomeIndicator(OutcomeIndicator outcome) {
        addAttribute("EventOutcomeIndicator", outcome, false);
        return this;
    }
    
    public List<TypeCode> getEventTypeCodes() {
        return Collections.unmodifiableList(eventTypeCodes);
    }
    
    public AuditEvent addEventTypeCode(TypeCode code) {
        if (code == null) {
            throw new NullPointerException("code");
        }
        eventTypeCodes.add(code);
        return this;
    }

    @Override
    protected boolean isEmpty() {
        return false;
    }
    
    @Override
    protected void outputContent(Writer out) throws IOException {
        eventID.output(out);
        outputChilds(out, eventTypeCodes);
    }

    /**
     * Identifier for a specific audited event, e.g., a menu item, program, 
     * rule, policy, function code, application name, or URL. 
     * It identifies the performed function.
     * <p>
     * Extended by DICOM defining a list of codes for event ids.
     */
    public static class ID extends CodeElement {

        /**
         * Event ID of {@link ApplicationActivityMessage}.
         */
        public static final ID APPLICATION_ACTIVITY = 
                new ID("110100", "DCM", "Application Activity");
        
        /**
         * Event ID of {@link AuditLogUsedMessage}.
         */
        public static final ID AUDIT_LOG_USED = 
                new ID("110101", "DCM", "Audit Log Used");
        
        /**
         * Event ID of {@link BeginTransferringMessage}.
         */
        public static final ID BEGIN_TRANSFERRING_DICOM_INSTANCES = 
                new ID("110102", "DCM", "Begin Transferring DICOM Instances");
        
        /**
         * Event ID of {@link InstancesAccessedMessage}.
         */
        public static final ID DICOM_INSTANCES_ACCESSED = 
                new ID("110103", "DCM", "DICOM Instances Accessed");
        
        /**
         * Event ID of {@link InstancesTransferredMessage}.
         */
        public static final ID DICOM_INSTANCES_TRANSFERRED = 
                new ID("110104", "DCM", "DICOM Instances Transferred");
        
        /**
         * Event ID of {@link StudyDeletedMessage}.
         */
        public static final ID DICOM_STUDY_DELETED = 
                new ID("110105", "DCM", "DICOM Study Deleted");
        
        /**
         * Event ID of {@link DataExportMessage}.
         */
        public static final ID EXPORT = new ID("110106", "DCM", "Export");
        
        /**
         * Event ID of {@link DataImportMessage}.
         */
        public static final ID IMPORT = new ID("110107", "DCM", "Import");
        
        /**
         * Event ID of {@link NetworkEntryMessage}.
         */
        public static final ID NETWORK_ENTRY = 
                new ID("110108", "DCM", "Network Entry");
        
        /**
         * Event ID of {@link OrderRecordMessage}.
         */
        public static final ID ORDER_RECORD = 
                new ID("110109", "DCM", "Order Record");
        
        /**
         * Event ID of {@link PatientRecordMessage}.
         */
        public static final ID PATIENT_RECORD = 
                new ID("110110", "DCM", "Patient Record");
        
        /**
         * Event ID of {@link ProcedureRecordMessage}.
         */
        public static final ID PROCEDURE_RECORD = 
                new ID("110111", "DCM", "Procedure Record");
        
        /**
         * Event ID of {@link QueryMessage}.
         */
        public static final ID QUERY = new ID("110112", "DCM", "Query");
        
        /**
         * Event ID of {@link SecurityAlertMessage}.
         */
        public static final ID SECURITY_ALERT = 
                new ID("110113", "DCM", "Security Alert");
        
        /**
         * Event ID used for {@link UserAuthenticationMessage}.
         */
        public static final ID USER_AUTHENTICATION = 
                new ID("110114", "DCM", "User Authentication");

        /**
         * Event ID used for {@link HealthServicesProvisionEventMessage}.
         */
        public static final ID HEALTH_SERVICE_PROVISION_EVENT = 
                new ID("IHE0001", "IHE", "Health Services Provision Event");
        
        /**
         * Event ID used for {@link MedicationEventMessage}.
         */
        public static final ID MEDICATION_EVENT = 
                new ID("IHE0002", "IHE", "Medication Event");
        
        /**
         * Event ID used for {@link PatientCareResourceAssignmentMessage}.
         */
        public static final ID PATIENT_CARE_RESOURCE_ASSIGNMENT = 
                new ID("IHE0003", "IHE", "Patient Care Resource Assignment");
        
        /**
         * Event ID used for {@link PatientCareEpisodeMessage}.
         */
        public static final ID PATIENT_CARE_EPISODE = 
                new ID("IHE0004", "IHE", "Patient Care Episode");
        
        /**
         * Event ID used for {@link PatientCareProtocolMessage}.
         */
        public static final ID PATIENT_CARE_PROTOCOL = 
                new ID("IHE0005", "IHE", "Patient Care Protocol");
        
        
        public ID(String code) {
            super("EventID", code);
        }

        
        public ID(String code, String codeSystemName, 
                String displayName) {
            super("EventID", code, codeSystemName, displayName);        
        }
    }
    
    /**
     * Enumeration of types of action performed during the event that
     * generated the audit. 
     */
    public static class ActionCode {
        
        /**
         * Create, e.g: create a new database object, such as Placing an Order.
         */
        public static final ActionCode CREATE = new ActionCode("C");
        
        /**
         * Read/View/Print/Query, e.g: sisplay or print data, such as a Doctor
         * Census.
         */
        public static final ActionCode READ = new ActionCode("R");
        
        /**
         * Update, e.g: Update data, such as Revise Patient Information.
         */
        public static final ActionCode UPDATE = new ActionCode("U");
        
        /**
         * Delete, e.g: Delete items, such as a doctor master file record.
         */
        public static final ActionCode DELETE = new ActionCode("D");
        
        /**
         * Execute, e.g: Perform a system or application function such as
         * log-on, program execution, or use of an object's method.
         */
        public static final ActionCode EXECUTE = new ActionCode("E");
        
        private final String value;

        private ActionCode(final String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Enumeration of values to indicate whether the event succeeded or 
     * failed.
     */
    public static class OutcomeIndicator {
        
        private final String value;
        
        /**
         * Success.
         */
        public static final OutcomeIndicator SUCCESS = 
                new OutcomeIndicator("0");
        
        /**
         * Minor failure; action restarted, e.g., invalid password with first
         * retry.
         */
        public static final OutcomeIndicator MINOR_FAILURE = 
                new OutcomeIndicator("4");
        
        /**
         * Serious failure; action terminated, e.g., invalid password with
         * excess retries.
         */
        public static final OutcomeIndicator SERIOUS_FAILURE = 
                new OutcomeIndicator("8");
        
        /**
         * Major failure; action made unavailable, e.g., user account
         * disabled due to excessive invalid log-on attempts.
         */
        public static final OutcomeIndicator MAJOR_FAILURE = 
                new OutcomeIndicator("12");
        
        private OutcomeIndicator(final String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Identifier for the category of event.
     * <p>
     * Extended by DICOM defining a list of event type codes.
     */
    public static class TypeCode extends CodeElement {

        /**
         * Application Start.
         */
        public static final TypeCode APPLICATION_START = 
                new TypeCode("110120", "DCM", "Application Start");
        
        public static final TypeCode APPLICATION_STOP = 
                new TypeCode("110121", "DCM", "Application Stop");
        
        public static final TypeCode LOGIN = 
                new TypeCode("110122", "DCM", "Login");
        
        public static final TypeCode LOGOUT = 
                new TypeCode("110123", "DCM", "Logout");
        
        public static final TypeCode ATTACH = 
                new TypeCode("110124", "DCM", "Attach");
        
        public static final TypeCode DETACH = 
                new TypeCode("110125", "DCM", "Detach");
        
        public static final TypeCode NODE_AUTHENTICATION = 
                new TypeCode("110126", "DCM", "Node Authentication");
        
        public static final TypeCode EMERGENCY_OVERRIDE_STARTED = 
                new TypeCode("110127", "DCM", "Emergency Override Started");
        
        public static final TypeCode NETWORK_CONFIGURATION = 
                new TypeCode("110128", "DCM", "Network Configuration");
        
        public static final TypeCode SECURITY_CONFIGURATION = 
                new TypeCode("110129", "DCM", "Security Configuration");
        
        public static final TypeCode HARDWARE_CONFIGURATION = 
                new TypeCode("110130", "DCM", "Hardware Configuration");
        
        public static final TypeCode SOFTWARE_CONFIGURATION = 
                new TypeCode("110131", "DCM", "Software Configuration");
        
        public static final TypeCode USE_OF_RESTRICTED_FUNCTION = 
                new TypeCode("110132", "DCM", "Use of Restricted Function");
        
        public static final TypeCode AUDIT_RECORDING_STOPPED = 
                new TypeCode("110133", "DCM", "Audit Recording Stopped");
        
        public static final TypeCode AUDIT_RECORDING_STARTED = 
                new TypeCode("110134", "DCM", "Audit Recording Started");
        
        public static final TypeCode OBJECT_SECURITY_ATTRIBUTES_CHANGED = 
                new TypeCode("110135", "DCM", 
                        "Object Security Attributes Changed");
        
        public static final TypeCode SECURITY_ROLES_CHANGED = 
                new TypeCode("110136", "DCM", "Security Roles Changed");
        
        public static final TypeCode USER_SECURITY_ATTRIBUTES_CHANGED = 
                new TypeCode("110137", "DCM", 
                        "User security Attributes Changed");

        public static final TypeCode EMERGENCY_OVERRIDE_STOPPED = 
                new TypeCode("110138", "DCM", "Emergency Override Stopped");

        public static final TypeCode REMOTE_SERVICE_OPERATION_STARTED = 
                new TypeCode("110139", "DCM",
                        "Remote Service Operation Started");

        public static final TypeCode REMOTE_SERVICE_OPERATION_STOPPED = 
                new TypeCode("110140", "DCM",
                        "Remote Service Operation Stopped");

        public static final TypeCode LOCAL_SERVICE_OPERATION_STARTED = 
                new TypeCode("110141", "DCM",
                        "Local Service Operation Started");

        public static final TypeCode LOCAL_SERVICE_OPERATION_STOPPED = 
                new TypeCode("110142", "DCM",
                        "Local Service Operation Stopped");

        public TypeCode(String code) {
            super("EventTypeCode", code);
        }

        public TypeCode(String code, String codeSystemName, 
                String displayName) {
            super("EventTypeCode", code, codeSystemName, displayName);
        }
    }    
}
