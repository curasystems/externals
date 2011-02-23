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

package org.dcm4che2.iod.module.dx;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.macro.GeneralAnatomy;
import org.dcm4che2.iod.validation.ValidationContext;
import org.dcm4che2.iod.validation.ValidationResult;
import org.dcm4che2.iod.value.ImageLaterality;

/**
 * DX Anatomy Imaged Module 2006 PS3.3 - C.8.11.2
 * 
 * Table C.8-69 contains IOD Attributes that describe the anatomy contained in a
 * DX IOD.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class DXAnatomyImagedModule extends GeneralAnatomy {

    public DXAnatomyImagedModule(DicomObject dcmobj) {
        super(dcmobj);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void validate(ValidationContext ctx, ValidationResult result){
        super.validate(ctx, result);
        if (!ImageLaterality.isValid(getImageLaterality())) {
            result.logInvalidValue(Tag.ImageLaterality, dcmobj);
        }

    }
    
    /**
     * Laterality of (possibly paired) body part (as described in Anatomic
     * Region Sequence (0008,2218)) examined.
     * 
     * Enumerated Values:
     * 
     * R = right
     * 
     * L = left
     * 
     * U = unpaired
     * 
     * B = both left and right
     * 
     * Note: This Attribute is mandatory, in order to ensure that images may be
     * positioned correctly relative to one another for display.
     * 
     * Shall be consistent with any laterality information contained in Primary
     * Anatomic Structure Modifier Sequence (0008,2230), if present.
     * 
     * Note: Laterality (0020,0060) is a Series level Attribute and must be the
     * same for all Images in the Series, hence it must be absent.
     * 
     * @return
     */
    public String getImageLaterality() {
        return dcmobj.getString(Tag.ImageLaterality);
    }

    /**
     * Laterality of (possibly paired) body part (as described in Anatomic
     * Region Sequence (0008,2218)) examined.
     * 
     * Enumerated Values:
     * 
     * R = right
     * 
     * L = left
     * 
     * U = unpaired
     * 
     * B = both left and right
     * 
     * Note: This Attribute is mandatory, in order to ensure that images may be
     * positioned correctly relative to one another for display.
     * 
     * Shall be consistent with any laterality information contained in Primary
     * Anatomic Structure Modifier Sequence (0008,2230), if present.
     * 
     * Note: Laterality (0020,0060) is a Series level Attribute and must be the
     * same for all Images in the Series, hence it must be absent.
     * 
     * @param s
     */
    public void setImageLaterality(String s) {
        dcmobj.putString(Tag.ImageLaterality, VR.CS, s);
    }

}
