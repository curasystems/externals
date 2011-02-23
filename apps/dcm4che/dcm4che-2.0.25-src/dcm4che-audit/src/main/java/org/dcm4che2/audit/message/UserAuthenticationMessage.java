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

/**
 * This message describes the event of a user has attempting to log on or
 * log off, whether successful or not.  No Participant Objects are needed
 * for this message.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 5516 $ $Date: 2007-11-23 12:42:30 +0100 (Fri, 23 Nov 2007) $
 * @since Nov 23, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.15 User Authentication</a>
 */
public class UserAuthenticationMessage extends AuditMessage {
    
    /**
     * Action Type code for {@link #UserAuthenticationMessage}.
     */
    public static final AuditEvent.TypeCode LOGIN = AuditEvent.TypeCode.LOGIN;
    
    /**
     * Action Type code for {@link #UserAuthenticationMessage}.
     */
    public static final AuditEvent.TypeCode LOGOUT = AuditEvent.TypeCode.LOGOUT;
   
    /**
     * Constructs an User Authentication message. 
     * Use {@link #setOutcomeIndicator} to modify default success indicator
     * to describe a failed user login.
     * 
     * @param typeCode indicator for type of action,
     *                {@link #LOGIN} or {@link #LOGOUT}
     * @throws NullPointerException If <code>typeCode=null</code>
     * @throws IllegalArgumentException If <code>typeCode</code> is neither
     *                {@link #LOGIN} nor {@link #LOGOUT}
     */
    public UserAuthenticationMessage(AuditEvent.TypeCode typeCode) {
        super(new AuditEvent(AuditEvent.ID.USER_AUTHENTICATION, 
                AuditEvent.ActionCode.EXECUTE)
            .addEventTypeCode(check(typeCode)));
    }

    private static AuditEvent.TypeCode check(AuditEvent.TypeCode typeCode) {
        if (typeCode == null) {
            throw new NullPointerException("typeCode");
        }        
        if (typeCode != AuditEvent.TypeCode.LOGIN 
                && typeCode != AuditEvent.TypeCode.LOGOUT) {
            throw new IllegalArgumentException(typeCode.toString());
        }
        return typeCode;
    }
        
    public ActiveParticipant addUserPerson(String userID, String altUserID,
            String userName, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, hostname, true));
    }

    public ActiveParticipant addNode(String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActiveNode(hostname, false));
    }

    
    @Override
    public void validate() {
        super.validate();
        ActiveParticipant user = getRequestingActiveParticipants();
        if (user == null) {
            throw new IllegalStateException("No Requesting User Information");
        }
    }    
}
