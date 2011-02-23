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

package org.dcm4che2.iod.module.dx;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.composite.GeneralSeriesModule;

/**
 * A class that represents the DX Series Module.
 * 
 * The Digital X-Ray IODs use the General Series module described in section
 * C.7.3.1, specialized by the DX Series Module, to describe the DICOM Series
 * Entity described in A.1.2.3, and to define what constitutes a Series for the
 * context of projection Digital X-Ray.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision: 720 $ $Date: 2006-11-26 18:40:54 +0100 (Sun, 26 Nov 2006) $
 * 
 */
public class DXSeriesModule extends GeneralSeriesModule {

    public DXSeriesModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    public String getPresentationIntentType() {
        return dcmobj.getString(Tag.PresentationIntentType);
    }

    /**
     * Identifies the intent of the images that are contained within this
     * Series.
     * 
     * Enumerated Values:
     * 
     * FOR PRESENTATION
     * 
     * FOR PROCESSING
     * 
     * See C.8.11.1.1.1 for further explanation.
     * 
     * Type 1
     * 
     * @param s
     */
    public void setPresentationIntentType(String s) {
        dcmobj.putString(Tag.PresentationIntentType, VR.CS, s);
    }

}
