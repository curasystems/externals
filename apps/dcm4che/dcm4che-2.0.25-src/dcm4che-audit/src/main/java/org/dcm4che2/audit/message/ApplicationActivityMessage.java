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

import java.util.List;

/**
 * This audit message describes the event of an Application Entity 
 * starting or stopping.
 * 
 * <h4>Message Structure</h4>
 * <ul>
 * <li>Event</li>
 * <li>ID of the Application started/stopped (1)</li>
 * <li>ID of person or process that started/stopped the Application (0..1)</li>
 * </ul>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 5685 $ $Date: 2008-01-15 21:05:18 +0100 (Tue, 15 Jan 2008) $
 * @since Nov 21, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.1 Application Activity</a>
 */
public class ApplicationActivityMessage extends AuditMessage {

    /**
     * Action Type code for {@link #ApplicationActivityMessage}.
     */
    public static final AuditEvent.TypeCode APPLICATION_START =
            AuditEvent.TypeCode.APPLICATION_START;
    
    /**
     * Action Type code for {@link #ApplicationActivityMessage}.
     */
    public static final AuditEvent.TypeCode APPLICATION_STOP =
            AuditEvent.TypeCode.APPLICATION_STOP;
    
    
    /**
     * Constructs an Application Entity message.
     * 
     * @param typeCode indicator for type of action, typically
     *                {@link #APPLICATION_START} or {@link #APPLICATION_STOP}
     * @throws NullPointerException If <code>typeCode=null</code>
     */
    public ApplicationActivityMessage(AuditEvent.TypeCode typeCode) {
        super(new AuditEvent(AuditEvent.ID.APPLICATION_ACTIVITY, 
                AuditEvent.ActionCode.EXECUTE)
            .addEventTypeCode(check(typeCode)));
    }
       
    private static AuditEvent.TypeCode check(AuditEvent.TypeCode typeCode) {
        if (typeCode == null) {
            throw new NullPointerException("typeCode");
        }
        return typeCode;
    }

    /**
     * Adds {@link ActiveParticipant} identifying the Application.
     * 
     * @param processID
     *            the identity of the process started or stopped. Use
     *            {@link AuditMessageUtils#getProcessID()} for this Java VM.
     * @param aets
     *            if the process supports DICOM, then the AE Titles supported,
     *            otherwise <code>null</code>
     * @param processName
     *            process name
     * @param nodeID
     *            DNS name or IP address of the node. Use
     *            {@link AuditMessageUtils#getLocalHostName()} for this node.
     *            
     * @return added {@link ActiveParticipant} identifying the Application.
     */
    public ActiveParticipant addApplication(String processID, String[] aets,
            String processName, String nodeID) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets,
                        processName, nodeID, false)
                .addRoleIDCode(ActiveParticipant.RoleIDCode.APPLICATION));
    }

    /**
     * Adds {@link ActiveParticipant} identifying the person or process that 
     * started/stopped the Application.
     * 
     * @param userID
     *            unique identifier for the person used by the application
     * @param altUserID
     *            Alternative User ID, used for authentication (e.g. SSO),
     *            - if available, otherwise <code>null</code>.
     * @param userName
     *            person's name - if available, otherwise <code>null</code>
     * @param napID
     *            identifier for the user's network access point (Machine Name,
     *            DNS name, IP Address or Telephone Number) - if available,
     *            otherwise <code>null</code>.
     * 
     * @return {@link ActiveParticipant} identifying the person or process.
     */
    public ActiveParticipant addApplicationLauncher(String userID,
            String altUserID, String userName, String napID) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, napID, true)
                .addRoleIDCode(ActiveParticipant.RoleIDCode.APPLICATION_LAUNCHER));
    }

    @Override
    public void validate() {
        super.validate();
        ActiveParticipant app = null;
        for (ActiveParticipant ap : activeParticipants) {
            List<ActiveParticipant.RoleIDCode> roleIDCodeIDs
                    = ap.getRoleIDCodes();
            if (roleIDCodeIDs.contains(
                ActiveParticipant.RoleIDCode.APPLICATION)) {
                if (ap.isUserIsRequestor()) {
                    throw new IllegalStateException(
                            "Application is Requesting User");
                }
                if (app != null) {
                    throw new IllegalStateException(
                            "Multiple Application identification");
                }
                app = ap;               
            }
        }
        if (app == null) {
            throw new IllegalStateException("No Application Identification");
        }
    }    
    
}