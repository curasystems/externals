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
 * This message describes the event of a system, such as a mobile device, 
 * entering or leaving the network as a normal part of operations. It is not
 * intended to report network problems, loose cables, or other unintentional
 * detach and reattach situations.
 * 
 * <blockquote>
 * Note: The machine should attempt to send this message prior to detaching.
 * If this is not possible, it should retain the message in a local buffer so
 * that it can be sent later. The mobile machine can then capture audit
 * messages in a local buffer while it is outside the secure domain. When it is
 * reconnected to the secure domain, it can send the detach message (if buffered),
 * followed by the buffered messages, followed by a mobile machine message for
 * rejoining the secure domain. The timestamps on these messages is  the time
 * that the event occurred, not the time that the message is sent. 
 * </blockquote>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 750 $ $Date: 2007-02-12 18:37:51 +0100 (Mon, 12 Feb 2007) $
 * @since Nov 23, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.9 Network Entry</a>
 */
public class NetworkEntryMessage extends AuditMessage {
    
    public static final AuditEvent.TypeCode ATTACH = 
            AuditEvent.TypeCode.ATTACH;
    public static final AuditEvent.TypeCode DETACH = 
            AuditEvent.TypeCode.DETACH;
    
    public NetworkEntryMessage(AuditEvent.TypeCode type) {
        super(new AuditEvent(AuditEvent.ID.NETWORK_ENTRY,
                    AuditEvent.ActionCode.EXECUTE)
                .addEventTypeCode(check(type)));
    }

    private static AuditEvent.TypeCode check(AuditEvent.TypeCode type) {
        if (type != AuditEvent.TypeCode.ATTACH 
                && type != AuditEvent.TypeCode.DETACH) {
            throw new IllegalArgumentException(type.toString());
        }
        return type;
    }
    
    public ActiveParticipant addNode(String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActiveNode(hostname, false));
    }    

}