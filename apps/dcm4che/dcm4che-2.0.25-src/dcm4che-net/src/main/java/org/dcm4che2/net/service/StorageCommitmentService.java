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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2007
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

package org.dcm4che2.net.service;

import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DicomServiceException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 808 $ $Date: 2007-03-26 19:12:08 +0200 (Mon, 26 Mar 2007) $
 * @since Mar 26, 2007
 */
public class StorageCommitmentService extends DicomService implements
        NActionSCP, NEventReportSCU {

    private static final String[] sopClasses = { UID.StorageCommitmentPushModelSOPClass };

    public StorageCommitmentService() {
        super(sopClasses, null);
    }
    
    public void naction(Association as, int pcid, DicomObject rq,
            DicomObject info) throws DicomServiceException, IOException {
        DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
        onNActionRQ(as, pcid, rq, info, rsp);
        as.writeDimseRSP(pcid, rsp);
        onNActionRSP(as, pcid, rq, info, rsp);
    }

    protected void onNActionRQ(Association as, int pcid, DicomObject rq,
            DicomObject info, DicomObject rsp) {
        // overwrite by actual StgCmt SCP        
    }

    protected void onNActionRSP(Association as, int pcid, DicomObject rq,
            DicomObject info, DicomObject rsp) {
        // overwrite by actual StgCmt SCP        
    }
    
    public void neventReport(Association as, int pcid, DicomObject rq,
            DicomObject info) throws DicomServiceException, IOException {
        DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
        onNEventReportRQ(as, pcid, rq, info, rsp);
        as.writeDimseRSP(pcid, rsp);
        onNEventReportRSP(as, pcid, rq, info, rsp);
    }

    protected void onNEventReportRQ(Association as, int pcid, DicomObject rq,
            DicomObject info, DicomObject rsp) {
        // overwrite by actual StgCmt SCU        
    }

    protected void onNEventReportRSP(Association as, int pcid, DicomObject rq,
            DicomObject info, DicomObject rsp) {
        // overwrite by actual StgCmt SCU        
    }
}
