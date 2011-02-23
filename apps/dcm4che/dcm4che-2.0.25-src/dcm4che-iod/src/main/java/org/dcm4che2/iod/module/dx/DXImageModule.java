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
import org.dcm4che2.iod.module.lut.LutModule;
import org.dcm4che2.iod.module.lut.ModalityLutModule;
import org.dcm4che2.iod.validation.ValidationContext;
import org.dcm4che2.iod.validation.ValidationResult;
import org.dcm4che2.iod.value.Flag;
import org.dcm4che2.iod.value.PixelIntensityRelationship;
import org.dcm4che2.iod.value.RescaleType;
import org.dcm4che2.iod.value.Sign;

/**
 * 
 * A specialized class that represents the DX Image Module.
 * <p>
 * Table C.8-70 contains IOD Attributes that describe a DX Image by specializing
 * Attributes of the General Image and Image Pixel Modules, and adding
 * additional Attributes.
 * <p>
 * This class is the son of
 * {@link org.dcm4che2.iod.module.composite.GeneralImageModule} and grandson of
 * {@link org.dcm4che2.iod.module.composite.ImagePixel}. Therefore, make use of
 * this class, and you will not need to worry about the other two modules
 * (C.7.3.1 and C.7.6.3).
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision: 5516 $ $Date: 2007-11-23 12:42:30 +0100 (Fri, 23 Nov 2007) $
 */
public class DXImageModule extends LutModule {

    public DXImageModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    @Override
    public void init() {
        super.init();
        ModalityLutModule modalityLutModule = getModalityLutModule();
        modalityLutModule.setRescaleIntercept(0.f);
        modalityLutModule.setRescaleSlope(1.f);
        modalityLutModule.setRescaleType(RescaleType.US);
    }

    @Override
    public void validate(ValidationContext ctx, ValidationResult result) {
        super.validate(ctx, result);
        if (!PixelIntensityRelationship
                .isValid(getPixelIntensityRelationship())) {
            result.logInvalidValue(Tag.PixelIntensityRelationship, dcmobj);
        }
        if (!Sign.isValid(getPixelIntensityRelationshipSign())) {
            if (dcmobj.containsValue(Tag.PixelIntensityRelationshipSign)) {
                result.logInvalidValue(Tag.PixelIntensityRelationshipSign,
                        dcmobj);
            }
        }
        if (!Flag.isValid(getCalibrationImage())) {
            result.logInvalidValue(Tag.CalibrationImage, dcmobj);
        }
    }

    /**
     * The relationship between the Pixel sample values and the X-Ray beam
     * intensity.
     * <p>
     * Enumerated Values: LIN = Linearly proportional to X-Ray beam intensity
     * LOG = Logarithmically proportional to X- Ray beam intensity See
     * C.8.11.3.1.2 for further explanation.
     * 
     * @param s
     */
    public void setPixelIntensityRelationship(String s) {
        dcmobj.putString(Tag.PixelIntensityRelationship, VR.CS, s);
    }

    public String getPixelIntensityRelationship() {
        return dcmobj.getString(Tag.PixelIntensityRelationship);
    }

    /**
     * The sign of the relationship between the Pixel sample values stored in
     * Pixel Data (7FE0,0010) and the X-Ray beam intensity.
     * <p>
     * Enumerated Values; 1 = Lower pixel values correspond to less X-Ray beam
     * intensity -1 = Higher pixel values correspond to less X-Ray beam
     * intensity See C.8.11.3.1.2 for further explanation.
     * 
     * @param ss
     */
    public void setPixelIntensityRelationshipSign(int ss) {
        dcmobj.putInt(Tag.PixelIntensityRelationshipSign, VR.SS, ss);
    }

    public int getPixelIntensityRelationshipSign() {
        return dcmobj.getInt(Tag.PixelIntensityRelationshipSign);
    }

    /**
     * Description Indicates any visual processing performed on the images prior
     * to exchange.
     * <p>
     * See C.8.11.3.1.3 for further explanation.
     * 
     * @param lo
     */
    public void setAcquisitionDeviceProcessingDescription(String lo) {
        dcmobj.putString(Tag.AcquisitionDeviceProcessingDescription, VR.LO, lo);
    }

    /**
     * Description Indicates any visual processing performed on the images prior
     * to exchange.
     * <p>
     * See C.8.11.3.1.3 for further explanation.
     * 
     * @return
     */
    public String getAcquisitionDeviceProcessingDescription() {
        return dcmobj.getString(Tag.AcquisitionDeviceProcessingDescription);
    }

    /**
     * Code representing the device-specific processing associated with the
     * image (e.g. Organ Filtering code)
     * <p>
     * Note: This Code is manufacturer specific but provides useful annotation
     * information to the knowledgeable observer.
     * 
     * @param lo
     */
    public void setAcquisitionDeviceProcessingCode(String lo) {
        dcmobj.putString(Tag.AcquisitionDeviceProcessingCode, VR.LO, lo);
    }

    /**
     * Code representing the device-specific processing associated with the
     * image (e.g. Organ Filtering code)
     * <p>
     * Note: This Code is manufacturer specific but provides useful annotation
     * information to the knowledgeable observer.
     * 
     * @return
     */
    public String getAcquisitionDeviceProcessingCode() {
        return dcmobj.getString(Tag.AcquisitionDeviceProcessingCode);
    }

    /**
     * Indicates whether a reference object (phantom) of known size is present
     * in the image and was used for calibration.
     * <p>
     * 
     * Enumerated Values:
     * 
     * YES NO
     * <p>
     * Device is identified using the Device module. See C.7.6.12 for further
     * explanation.
     * 
     * @param cs
     */
    public void setCalibrationImage(String cs) {
        dcmobj.putString(Tag.CalibrationImage, VR.CS, cs);
    }

    /**
     * Indicates whether a reference object (phantom) of known size is present
     * in the image and was used for calibration.
     * 
     * <p>
     * Enumerated Values:
     * 
     * YES NO
     * <p>
     * Device is identified using the Device module. See C.7.6.12 for further
     * explanation.
     * 
     * @return
     */
    public String getCalibrationImage() {
        return dcmobj.getString(Tag.CalibrationImage);
    }

}
