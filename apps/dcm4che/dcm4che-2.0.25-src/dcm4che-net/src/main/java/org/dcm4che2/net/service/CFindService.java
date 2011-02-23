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
 * Gunter Zeilinger <gunterze@gmail.com>
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
import java.util.concurrent.Executor;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.SingleDimseRSP;
import org.dcm4che2.net.Status;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 13902 $ $Date: 2008-09-06 14:52:22 +0200 (za, 06 sep 2008)
 *          $
 * @since Jan 22, 2006
 * 
 */
public class CFindService extends DicomService implements CFindSCP {
    private final Executor executor;

    public CFindService(String[] sopClasses, Executor executor) {
        super(sopClasses);
        this.executor = executor;
    }

    public CFindService(String sopClass, Executor executor) {
        super(sopClass);
        this.executor = executor;
    }

    public void cfind(Association as, int pcid, DicomObject rq, DicomObject data)
            throws DicomServiceException, IOException {
        DicomObject cmdrsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
        DimseRSP rsp = doCFind(as, pcid, rq, data, cmdrsp);
        try {
            rsp.next();
        } catch (InterruptedException e) {
            throw new DicomServiceException(rq, Status.ProcessingFailure);
        }
        cmdrsp = rsp.getCommand();
        if (CommandUtils.isPending(cmdrsp)) {
            as.registerCancelRQHandler(rq, rsp);
            executor.execute(new WriteMultiDimseRsp(as, pcid, rsp));
        } else {
            as.writeDimseRSP(pcid, cmdrsp, rsp.getDataset());
        }
    }

    @SuppressWarnings("unused")
    protected DimseRSP doCFind(Association as, int pcid, DicomObject cmd,
            DicomObject data, DicomObject rsp) throws DicomServiceException {
        return new SingleDimseRSP(rsp);
    }

}
