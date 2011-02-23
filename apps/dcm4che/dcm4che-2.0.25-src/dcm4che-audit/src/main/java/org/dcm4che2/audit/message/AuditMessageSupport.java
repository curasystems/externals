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

public class AuditMessageSupport extends AuditMessage {

    public static final AuditEvent.ActionCode CREATE = 
            AuditEvent.ActionCode.CREATE;
    public static final AuditEvent.ActionCode READ = 
            AuditEvent.ActionCode.READ;
    public static final AuditEvent.ActionCode UPDATE = 
            AuditEvent.ActionCode.UPDATE;
    public static final AuditEvent.ActionCode DELETE = 
            AuditEvent.ActionCode.DELETE;
    
    protected AuditMessageSupport(AuditEvent.ID id,
            AuditEvent.ActionCode action) {
        super(new AuditEvent(id, check(action)));
    }      
    
    private static AuditEvent.ActionCode check(AuditEvent.ActionCode action) {
        if (action == AuditEvent.ActionCode.EXECUTE) {
            throw new IllegalArgumentException("action=Execute");
        }
        return action;
    }

    public ActiveParticipant addUserPerson(String userID, String altUserID, 
            String userName, String hostname, boolean requestor) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, hostname, requestor));
    }
    
    public ActiveParticipant addUserProcess(String processID, String[] aets, 
            String processName, String hostname, boolean requestor) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, requestor));
    }
        
    public ParticipantObject addPatient(String id, String name) {
        return addParticipantObject(ParticipantObject.createPatient(id, name));
    }

    @Override
    public void validate() {
        super.validate();
        ActiveParticipant user = getRequestingActiveParticipants();
        if (user == null) {
            throw new IllegalStateException("No Requesting User");
        }
        ParticipantObject patient = null;
        for (ParticipantObject po : participantObjects) {
            if (ParticipantObject.TypeCodeRole.PATIENT
                    == po.getParticipantObjectTypeCodeRole()) {
                if (patient != null) {
                    throw new IllegalStateException(
                            "Multiple Patient identification");
                }
                patient = po;
            }        
        }
        if (patient == null) {
            throw new IllegalStateException("No Patient identification");
        }
    }   
}
