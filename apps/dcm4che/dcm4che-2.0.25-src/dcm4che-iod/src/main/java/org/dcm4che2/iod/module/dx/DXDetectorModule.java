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

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version Revision $Date: 2006-11-26 18:40:54 +0100 (Sun, 26 Nov 2006) $
 * @since 30.06.2006
 */

public class DXDetectorModule extends DXDetector {

    public DXDetectorModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    public float getDetectorActiveTime() {
        return dcmobj.getFloat(Tag.DetectorActiveTime);
    }
    
    public void setDetectorActiveTime(float f) {
        dcmobj.putFloat(Tag.DetectorActiveTime, VR.DS, f);
    }
  
    public float getDetectorActivationOffsetFromExposure() {
        return dcmobj.getFloat(Tag.DetectorActivationOffsetFromExposure);
    }
    
    public void setDetectorActivationOffsetFromExposure(float s) {
        dcmobj.putFloat(Tag.DetectorActivationOffsetFromExposure, VR.DS, s);
    }

    public String getFieldOfViewShape() {
        return dcmobj.getString(Tag.FieldOfViewShape);
    }
    
    public void setFieldOfViewShape(String s) {
        dcmobj.putString(Tag.FieldOfViewShape, VR.CS, s);
    }

    public int[] getFieldOfViewDimensions() {
        return dcmobj.getInts(Tag.FieldOfViewDimensions);
    }
    
    public void setFieldOfViewDimensions(int[] ints) {
        dcmobj.putInts(Tag.FieldOfViewDimensions, VR.IS, ints);
    }

    public float[] getFieldOfViewOrigin() {
        return dcmobj.getFloats(Tag.FieldOfViewOrigin);
    }
    
    public void setFieldOfViewOrigin(float[] floats) {
        dcmobj.putFloats(Tag.FieldOfViewOrigin, VR.DS, floats);
    }
    
    public float getFieldOfViewRotation() {
        return dcmobj.getFloat(Tag.FieldOfViewRotation);
    }
    
    public void setFieldOfViewRotation(float f) {
        dcmobj.putFloat(Tag.FieldOfViewRotation, VR.DS, f);
    }

    public String getFieldOfViewHorizontalFlip() {
        return dcmobj.getString(Tag.FieldOfViewHorizontalFlip);
    }
    
    public void setFieldOfViewHorizontalFlip(String s) {
        dcmobj.putString(Tag.FieldOfViewHorizontalFlip, VR.CS, s);
    }

    public float[] getImagerPixelSpacing() {
        return dcmobj.getFloats(Tag.ImagerPixelSpacing);
    }
    
    public void setImagerPixelSpacing(float[] floats) {
        dcmobj.putFloats(Tag.ImagerPixelSpacing, VR.DS, floats);
    }

    public float[] getPixelSpacing() {
        return dcmobj.getFloats(Tag.PixelSpacing);
    }
    
    public void setPixelSpacing(float[] floats) {
        dcmobj.putFloats(Tag.PixelSpacing, VR.DS, floats);
    }

    public String getPixelSpacingCalibrationType() {
        return dcmobj.getString(Tag.PixelSpacingCalibrationType);
    }
    
    public void setPixelSpacingCalibrationType(String s) {
        dcmobj.putString(Tag.PixelSpacingCalibrationType, VR.CS, s);
    }

    public String getPixelSpacingCalibrationDescription() {
        return dcmobj.getString(Tag.PixelSpacingCalibrationDescription);
    }
    
    public void setPixelSpacingCalibrationDescription(String s) {
        dcmobj.putString(Tag.PixelSpacingCalibrationDescription, VR.LO, s);
    }
}
