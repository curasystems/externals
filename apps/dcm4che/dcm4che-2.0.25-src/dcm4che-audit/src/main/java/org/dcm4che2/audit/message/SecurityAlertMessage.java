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

import org.dcm4che2.audit.message.AuditEvent.TypeCode;

/**
 * This message describes any event for which a node needs to report a 
 * security alert, e.g., a node authentication failure when establishing a 
 * secure communications channel.
 * 
 * <blockquote>
 * Note: The Node Authentication event can be used to report both successes
 * and failures. If reporting of success is done, this could generate a very
 * large number of audit messages, since every authenticated DICOM association,
 * HL7 transaction, and HTML connection should result in a successful node
 * authentication. It is expected that in most situations only the node
 * authentication failures will be reported.
 * </blockquote>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 14557 $ $Date: 2010-12-16 13:10:55 +0100 (Thu, 16 Dec 2010) $
 * @since Nov 23, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.14 Security Alert</a>
 */
public class SecurityAlertMessage extends AuditMessage {
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode NODE_AUTHENTICATION =
            AuditEvent.TypeCode.NODE_AUTHENTICATION;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode EMERGENCY_OVERRIDE_STARTED =
            AuditEvent.TypeCode.EMERGENCY_OVERRIDE_STARTED;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode NETWORK_CONFIGURATION =
            AuditEvent.TypeCode.NETWORK_CONFIGURATION;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode SECURITY_CONFIGURATION =
            AuditEvent.TypeCode.SECURITY_CONFIGURATION;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode HARDWARE_CONFIGURATION =
            AuditEvent.TypeCode.HARDWARE_CONFIGURATION;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode SOFTWARE_CONFIGURATION =
            AuditEvent.TypeCode.SOFTWARE_CONFIGURATION;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode USE_OF_RESTRICTED_FUNCTION =
            AuditEvent.TypeCode.USE_OF_RESTRICTED_FUNCTION;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode AUDIT_RECORDING_STOPPED =
            AuditEvent.TypeCode.AUDIT_RECORDING_STOPPED;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode AUDIT_RECORDING_STARTED =
            AuditEvent.TypeCode.AUDIT_RECORDING_STARTED;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode OBJECT_SECURITY_ATTRIBUTES_CHANGED =
            AuditEvent.TypeCode.OBJECT_SECURITY_ATTRIBUTES_CHANGED;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode SECURITY_ROLES_CHANGED =
            AuditEvent.TypeCode.SECURITY_ROLES_CHANGED;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode USER_SECURITY_ATTRIBUTES_CHANGED =
            AuditEvent.TypeCode.USER_SECURITY_ATTRIBUTES_CHANGED;
    
    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode EMERGENCY_OVERRIDE_STOPPED = 
            AuditEvent.TypeCode.EMERGENCY_OVERRIDE_STOPPED;

    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode REMOTE_SERVICE_OPERATION_STARTED =
            AuditEvent.TypeCode.REMOTE_SERVICE_OPERATION_STARTED;

    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode REMOTE_SERVICE_OPERATION_STOPPED =
            AuditEvent.TypeCode.REMOTE_SERVICE_OPERATION_STOPPED;

    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode LOCAL_SERVICE_OPERATION_STARTED =
            AuditEvent.TypeCode.LOCAL_SERVICE_OPERATION_STARTED;

    /**
     * Action Type code for {@link #SecurityAlertMessage}.
     */
    public static final AuditEvent.TypeCode LOCAL_SERVICE_OPERATION_STOPPED = 
            AuditEvent.TypeCode.LOCAL_SERVICE_OPERATION_STOPPED;

    /**
     * Constructs an Security Alert message. Use {@link #setOutcomeIndicator}
     * to modify default success indicator to describe failures.
     * 
     * @param typeCode indicator for type of security alert
     * @throws NullPointerException If <code>typeCode=null</code>
     */
    public SecurityAlertMessage(AuditEvent.TypeCode type) {
        super(new AuditEvent(AuditEvent.ID.SECURITY_ALERT,
                AuditEvent.ActionCode.EXECUTE)
            .addEventTypeCode(check(type)));        
    }

    private static AuditEvent.TypeCode check(AuditEvent.TypeCode typeCode) {
        if (typeCode == null) {
            throw new NullPointerException("typeCode");
        }
        return typeCode;
    }

    
    public ActiveParticipant addReportingPerson(String userID, String altUserID, 
            String userName, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, hostname, true));
    }
    
    public ActiveParticipant addReportingProcess(String processID, String[] aets, 
            String processName, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, true));
    }
    
    public ActiveParticipant addPerformingPerson(String userID, String altUserID, 
            String userName, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, hostname, false));
    }
    
    public ActiveParticipant addPerformingProcess(String processID, String[] aets, 
            String processName, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, false));
    }
    
    public ActiveParticipant addPerformingNode(String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActiveNode(hostname, false));
    }
    
    public ParticipantObject addAlertSubjectWithURI(String uri, 
            String desc) {
        return addParticipantObject(
                ParticipantObject.createAlertSubjectWithURI(uri, desc));
    }
    
    public ParticipantObject addAlertSubjectWithNodeID(String nodeID, 
            String desc) {
        return addParticipantObject(
                ParticipantObject.createAlertSubjectWithNodeID(nodeID, desc));
    }
   
    @Override
    public void validate() {
        super.validate();
        ActiveParticipant user = getRequestingActiveParticipants();
        if (user == null) {
            throw new IllegalStateException("No Reporting User");
        }
    }    
}
