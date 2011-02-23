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

package org.dcm4che2.iod.module.cr;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.composite.GeneralSeriesModule;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 12635 $ $Date: 2010-01-18 14:15:50 +0100 (Mon, 18 Jan 2010) $
 * @since Jun 9, 2006
 *
 */
public class CRSeriesModule extends GeneralSeriesModule {

    public CRSeriesModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    @Override
    public String getBodyPartExamined() {
        return dcmobj.getString(Tag.BodyPartExamined);
    }
    
    @Override
    public void setBodyPartExamined(String s) {
        dcmobj.putString(Tag.BodyPartExamined, VR.CS, s);
    }

    public String getViewPosition() {
        return dcmobj.getString(Tag.ViewPosition);
    }
    
    public void setViewPosition(String s) {
        dcmobj.putString(Tag.ViewPosition, VR.CS, s);
    }

    public String getFilterType() {
        return dcmobj.getString(Tag.FilterType);
    }
    
    public void setFilterType(String s) {
        dcmobj.putString(Tag.FilterType, VR.SH, s);
    }

    public String getCollimatorGridName() {
        return dcmobj.getString(Tag.CollimatorGridName);
    }
    
    public void setCollimatorGridName(String s) {
        dcmobj.putString(Tag.CollimatorGridName, VR.SH, s);
    }

    public float[] getFocalSpots() {
        return dcmobj.getFloats(Tag.FocalSpots);
    }
    
    public void setFocalSpots(float[] fs) {
        dcmobj.putFloats(Tag.FocalSpots, VR.DS, fs);
    }

    public String getPlateType() {
        return dcmobj.getString(Tag.PlateType);
    }
    
    public void setPlateType(String s) {
        dcmobj.putString(Tag.PlateType, VR.SH, s);
    }

    public String getPhosphorType() {
        return dcmobj.getString(Tag.PhosphorType);
    }
    
    public void setPhosphorType(String s) {
        dcmobj.putString(Tag.PhosphorType, VR.LO, s);
    }
    
}
