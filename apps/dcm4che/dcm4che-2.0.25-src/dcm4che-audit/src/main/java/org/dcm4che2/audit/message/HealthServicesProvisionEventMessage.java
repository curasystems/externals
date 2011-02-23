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
 * This message may be generated whenever health services are scheduled and
 * performed within an instance or episode of care. These include scheduling, 
 * initiation, updates or amendments, performing or completing an act, and 
 * cancellation.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 750 $ $Date: 2007-02-12 18:37:51 +0100 (Mon, 12 Feb 2007) $
 * @since Nov 29, 2006
 * 
 * @see <a href="http://www.ihe.net/Technical_Framework/upload/ihe_iti_tf_2.0_vol2_FT_2005-08-15.pdf">
 * IHE IT Infrastructure Technical Framework, vol. 2: Transactions,
 * 3.20.7.3.1 Health Services Provision Event</a>
 */
public class HealthServicesProvisionEventMessage extends AuditMessageSupport {

    public HealthServicesProvisionEventMessage(AuditEvent.ActionCode action) {
        super(AuditEvent.ID.HEALTH_SERVICE_PROVISION_EVENT, action);
    }      


}
