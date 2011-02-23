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
 * This message describes the event of exporting data from a system, implying
 * that the data is leaving control of the system's security domain.
 * Examples of exporting include printing to paper, recording on film, creation
 * of a .pdf or HTML file, conversion to another format for storage in an EHR,
 * writing to removable media, or sending via e-mail. Multiple patients may be
 * described in one event message.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 5685 $ $Date: 2008-01-15 21:05:18 +0100 (Tue, 15 Jan 2008) $
 * @since Nov 23, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.4 Data Export</a>
 */
public class DataExportMessage extends AuditMessage {

    public DataExportMessage() {
        super(new AuditEvent(AuditEvent.ID.EXPORT, AuditEvent.ActionCode.READ));
    }
       
    public ActiveParticipant addExporterPerson(String userID, String altUserID, 
            String userName, boolean requestor, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, hostname, requestor))
                .addRoleIDCode(ActiveParticipant.RoleIDCode.SOURCE);
    }
    
    public ActiveParticipant addExporterProcess(String processID, String[] aets, 
            String processName, boolean requestor, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, requestor)
                .addRoleIDCode(ActiveParticipant.RoleIDCode.SOURCE));
    }
    
    public ActiveParticipant addDestinationMedia(String mediaID, String mediaUID) {
        return addActiveParticipant(
                ActiveParticipant.createMedia(mediaID, mediaUID)
                .addRoleIDCode(ActiveParticipant.RoleIDCode.DESTINATION_MEDIA));
    }

    public ActiveParticipant addDestinationMedia(String userID, String altUserID, 
            String userName, boolean requestor, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, hostname, requestor))
                .addRoleIDCode(ActiveParticipant.RoleIDCode.DESTINATION_MEDIA);
    }    
    
    public ParticipantObject addPatient(String id, String name) {
        return addParticipantObject(ParticipantObject.createPatient(id, name));
    }

    public ParticipantObject addStudy(String uid,
            ParticipantObjectDescription desc) {
        return addParticipantObject(ParticipantObject.createStudy(uid, desc));
    }

    public ParticipantObject addDataRepository(String uri) {
        return addParticipantObject(ParticipantObject.createDataRepository(uri));
    }
    
    @Override
    public void validate() {
        super.validate();
        ActiveParticipant exporter = null;
        ActiveParticipant dest = null;
        ActiveParticipant requestor = null;
        for (ActiveParticipant ap : activeParticipants) {
            List<ActiveParticipant.RoleIDCode> roleIDCodeIDs =
                    ap.getRoleIDCodes();
            if (roleIDCodeIDs.contains(
                ActiveParticipant.RoleIDCode.SOURCE)) {
                exporter = ap;               
            } else if (roleIDCodeIDs.contains(
                ActiveParticipant.RoleIDCode.DESTINATION_MEDIA)) {
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
        if (exporter == null) {
            throw new IllegalStateException("No Exporter identification");
        }
        if (dest == null) {
            throw new IllegalStateException("No Destination identification");
        }
        if (requestor == null) {
            throw new IllegalStateException("No Requesting User");
        }
    }    
    
}