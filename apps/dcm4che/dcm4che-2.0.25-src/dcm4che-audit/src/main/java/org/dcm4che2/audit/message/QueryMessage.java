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
 * This message describes the event of a Query being issued or received.
 * The message does not record the response to the query, but merely
 * records the fact that a query was issued. For example, this would report
 * queries using the DICOM SOP Classes:
 * <ul>
 * <li>Modality Worklist</li>
 * <li>General Purpose Worklist</li>
 * <li>Composite Instance Query</li>
 * </ul>
  * <blockquote>
 * Notes:
 * <ol>
 * <li>The response to a query may result in one or more Instances Transferred 
 * or Instances Accessed messages, depending on what events transpire after the
 * query. If there were security-related failures, such as access violations,
 * when processing a query, those failures should show up in other audit 
 * messages, such as a Security Alert message.</li>
 * <li>Non-DICOM queries may also be captured by this message. The Participant
 * Object ID Type Code, the Participant Object ID, and the Query fields may 
 * have values related to such non-DICOM queries.</li>
 * </ol>
 * </blockquote>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 5724 $ $Date: 2008-01-21 12:56:19 +0100 (Mon, 21 Jan 2008) $
 * @since Nov 23, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.13 Query</a>
 */
public class QueryMessage extends AuditMessage {

    public QueryMessage() {
        super(new AuditEvent(AuditEvent.ID.QUERY,
                AuditEvent.ActionCode.EXECUTE));
    }
 
    public ActiveParticipant addSourceProcess(String processID, String[] aets,
            String processName, String hostname, boolean requestor) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, requestor)
                .addRoleIDCode(ActiveParticipant.RoleIDCode.SOURCE));
    }
    
    public ActiveParticipant addDestinationProcess(String processID, String[] aets, 
            String processName, String hostname, boolean requestor) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, requestor)
                .addRoleIDCode(ActiveParticipant.RoleIDCode.DESTINATION));
    }
        
    public ActiveParticipant addOtherParticipantPerson(String userID,
            String altUserID, String userName, String hostname, boolean requestor) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, hostname, requestor));
    }
    
    public ActiveParticipant addOtherParticipantProcess(String processID,
            String[] aets, String processName, String hostname, boolean requestor) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, requestor));
    }

    public ParticipantObject addQuerySOPClass(String cuid, String tsuid, 
           byte[] query) {
        return addParticipantObject(
                ParticipantObject.createQuerySOPClass(cuid, tsuid, query));
    }
    
    @Override
    public void validate() {
        super.validate();
        
        ActiveParticipant source = null;
        ActiveParticipant dest = null;
        ActiveParticipant requestor = null;
        for (ActiveParticipant ap : activeParticipants) {
            List<ActiveParticipant.RoleIDCode> roleIDCodeIDs =
                    ap.getRoleIDCodes();
            if (roleIDCodeIDs.contains(
                ActiveParticipant.RoleIDCode.SOURCE)) {
                if (source != null) {
                    throw new IllegalStateException(
                            "Multiple Source identification");
                }
                source = ap;               
            } else if (roleIDCodeIDs.contains(
                ActiveParticipant.RoleIDCode.DESTINATION)) {
                if (dest != null) {
                    throw new IllegalStateException(
                            "Multiple Destination identification");
                }
                dest = ap;               
            }
            if (ap.isUserIsRequestor()) {
                requestor = ap;
            }            

        }
        if (source == null) {
            throw new IllegalStateException("No Source identification");
        }
        if (dest == null) {
            throw new IllegalStateException("No Destination identification");
        }
        if (requestor == null) {
            throw new IllegalStateException("No Requesting User");
        }
       
        ParticipantObject sopClass = null;        
        for (ParticipantObject po : participantObjects) {
            if (ParticipantObject.TypeCodeRole.REPORT
                        == po.getParticipantObjectTypeCodeRole()
                    && ParticipantObject.IDTypeCode.SOP_CLASS_UID
                        == po.getParticipantObjectIDTypeCode()) {
                if (sopClass != null) {
                    throw new IllegalStateException(
                            "Multiple Query SOP Class identification");
                }
                sopClass = po;
            }        
        }
        if (sopClass == null) {
            throw new IllegalStateException("No Query SOP Class identification");
        }
    }        
}
