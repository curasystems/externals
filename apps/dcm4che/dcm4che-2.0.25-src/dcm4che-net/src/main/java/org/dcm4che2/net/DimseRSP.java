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

package org.dcm4che2.net;

import java.io.IOException;

import org.dcm4che2.data.DicomObject;

/**
 * API contract for DIMSE response objects (FutureDimseRSP, MultiFindRSP,
 * SingleDimseRSP).
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 673 $ $Date: 2006-08-07 07:11:16 +0200 (Mon, 07 Aug 2006) $
 * @since Jan 22, 2006
 */
public interface DimseRSP
{
    /**
     * Send the next response, returning false when complete.
     * 
     * @return boolean True if there are more responses to send.
     * @throws IOException If there was a problem in the network interaction.
     * @throws InterruptedException If the thread was interrupted.
     */
    boolean next() throws IOException, InterruptedException;

    /**
     * Get the response command object.
     * 
     * @return DicomObject The command object.
     */
    DicomObject getCommand();

    /**
     * Get the dataset contained within this response, null if there is no
     * dataset.
     * 
     * @return DicomObject The dataset contained in this response, if any.
     */
    DicomObject getDataset();

    /**
     * Cancel the operation, if this is a DIMSE action that can be cancelled
     * (such as C-FIND).
     * 
     * @param a Association The active association object.
     * @throws IOException If there was a problem in the network interaction.
     */
    void cancel(Association a) throws IOException;

}
