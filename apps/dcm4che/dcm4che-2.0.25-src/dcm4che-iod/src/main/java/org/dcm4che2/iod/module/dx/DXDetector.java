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

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version Revision $Date: 2006-11-26 18:40:54 +0100 (Sun, 26 Nov 2006) $
 * @since 30.06.2006
 */

public class DXDetector extends Module {

    public DXDetector(DicomObject dcmobj) {
        super(dcmobj);
    }

    public String getDetectorType() {
        return dcmobj.getString(Tag.DetectorType);
    }
    
    public void setDetectorType(String s) {
        dcmobj.putString(Tag.DetectorType, VR.CS, s);
    }

    public String getDetectorConfiguration() {
        return dcmobj.getString(Tag.DetectorConfiguration);
    }
    
    public void setDetectorConfiguration(String s) {
        dcmobj.putString(Tag.DetectorConfiguration, VR.CS, s);
    }

    public String getDetectorDescription() {
        return dcmobj.getString(Tag.DetectorDescription);
    }
    
    public void setDetectorDescription(String s) {
        dcmobj.putString(Tag.DetectorDescription, VR.LT, s);
    }

    public String getDetectorMode() {
        return dcmobj.getString(Tag.DetectorMode);
    }
    
    public void setDetectorMode(String s) {
        dcmobj.putString(Tag.DetectorMode, VR.LT, s);
    }

    public String getDetectorID() {
        return dcmobj.getString(Tag.DetectorID);
    }
    
    public void setDetectorID(String s) {
        dcmobj.putString(Tag.DetectorID, VR.SH, s);
    }

    public Date getDateTimeOfLastDetectorCalibration() {
        return dcmobj.getDate(Tag.DateOfLastDetectorCalibration,
                Tag.TimeOfLastDetectorCalibration);
    }

    public void setDateTimeOfLastDetectorCalibration(Date d) {
        dcmobj.putDate(Tag.DateOfLastDetectorCalibration, VR.DA, d);
        dcmobj.putDate(Tag.TimeOfLastDetectorCalibration, VR.TM, d);
    }

    public int getExposuresOnDetectorSinceLastCalibration() {
        return dcmobj.getInt(Tag.ExposuresOnDetectorSinceLastCalibration);
    }
    
    public void setExposuresOnDetectorSinceLastCalibration(int i) {
        dcmobj.putInt(Tag.ExposuresOnDetectorSinceLastCalibration, VR.IS, i);
    }

    public int getExposuresOnDetectorSinceManufactured() {
        return dcmobj.getInt(Tag.ExposuresOnDetectorSinceManufactured);
    }
    
    public void setExposuresOnDetectorSinceManufactured(int i) {
        dcmobj.putInt(Tag.ExposuresOnDetectorSinceManufactured, VR.IS, i);
    }

    public float getDetectorTimeSinceLastExposure() {
        return dcmobj.getFloat(Tag.DetectorTimeSinceLastExposure);
    }
    
    public void setDetectorTimeSinceLastExposure(float f) {
        dcmobj.putFloat(Tag.DetectorTimeSinceLastExposure, VR.DS, f);
    }

    public float[] getDetectorBinning() {
        return dcmobj.getFloats(Tag.DetectorBinning);
    }
    
    public void setDetectorBinning(float[] f) {
        dcmobj.putFloats(Tag.DetectorBinning, VR.DS, f);
    }

    public String getDetectorManufacturerName() {
        return dcmobj.getString(Tag.DetectorManufacturerName);
    }
    
    public void setDetectorManufacturerName(String s) {
        dcmobj.putString(Tag.DetectorManufacturerName, VR.LO, s);
    }

    public String getDetectorManufacturerModelName() {
        return dcmobj.getString(Tag.DetectorManufacturerModelName);
    }
    
    public void setDetectorManufacturerModelName(String s) {
        dcmobj.putString(Tag.DetectorManufacturerModelName, VR.LO, s);
    }

    public String getDetectorConditionsNominalFlag() {
        return dcmobj.getString(Tag.DetectorConditionsNominalFlag);
    }
    
    public void setDetectorConditionsNominalFlag(String s) {
        dcmobj.putString(Tag.DetectorConditionsNominalFlag, VR.CS, s);
    }

    public float getDetectorTemperature() {
        return dcmobj.getFloat(Tag.DetectorTemperature);
    }
    
    public void setDetectorTemperature(float f) {
        dcmobj.putFloat(Tag.DetectorTemperature, VR.DS, f);
    }

    public float getSensitivity() {
        return dcmobj.getFloat(Tag.Sensitivity);
    }
    
    public void setSensitivity(float s) {
        dcmobj.putFloat(Tag.Sensitivity, VR.DS, s);
    }

    public float[] getDetectorElementPhysicalSize() {
        return dcmobj.getFloats(Tag.DetectorElementPhysicalSize);
    }
    
    public void setDetectorElementPhysicalSize(float[] f) {
        dcmobj.putFloats(Tag.DetectorElementPhysicalSize, VR.DS, f);
    }

    public float[] getDetectorElementSpacing() {
        return dcmobj.getFloats(Tag.DetectorElementSpacing);
    }
    
    public void setDetectorElementSpacing(float[] f) {
        dcmobj.putFloats(Tag.DetectorElementSpacing, VR.DS, f);
    }

    public String getDetectorActiveShape() {
        return dcmobj.getString(Tag.DetectorActiveShape);
    }
    
    public void setDetectorActiveShape(String s) {
        dcmobj.putString(Tag.DetectorActiveShape, VR.CS, s);
    }

    public float[] getDetectorActiveDimensions() {
        return dcmobj.getFloats(Tag.DetectorActiveDimensions);
    }
    
    public void setDetectorActiveDimensions(float[] f) {
        dcmobj.putFloats(Tag.DetectorActiveDimensions, VR.DS, f);
    }

    public float[] getDetectorActiveOrigin() {
        return dcmobj.getFloats(Tag.DetectorActiveOrigin);
    }
    
    public void setDetectorActiveOrigin(float[] f) {
        dcmobj.putFloats(Tag.DetectorActiveOrigin, VR.DS, f);
    }
}
