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
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.module.macro.Code;
import org.dcm4che2.iod.module.macro.PatientOrientationCode;
import org.dcm4che2.iod.module.macro.ViewCode;

/**
 * 
 * Table C.8-72 contains IOD Attributes that describe the positioning used in
 * acquiring Digital X-Ray Images.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision: 720 $ $Date: 2006-11-26 18:40:54 +0100 (Sun, 26 Nov 2006) $
 */
public class DXPositioningModule extends Module {

    public DXPositioningModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    /**
     * Sequence A Sequence that describes the radiographic method of patient,
     * tube and detector positioning to achieve a well described projection or
     * view.
     * <p>
     * Only a single Item shall be permitted in this Sequence.
     * <p>
     * Shall be consistent with the other Attributes in this Module, if present,
     * but may more specifically describe the image acquisition.
     * 
     * @param codes
     */
    public void setProjectionEponymousNameCode(Code code) {
        updateSequence(Tag.ProjectionEponymousNameCodeSequence, code);
    }

    public Code getProjectionEponymousNameCode() {
        DicomObject item = dcmobj.getNestedDicomObject(
                Tag.ProjectionEponymousNameCodeSequence);
        return item != null ? new Code(item) : null;
    }

    /**
     * Description of imaging subjectÕs position relative to the equipment.
     * <p>
     * See C.7.3.1.1.2 for Defined Terms and further explanation.
     * <p>
     * If present, shall be consistent with Patient Gantry Relationship Code
     * Sequence (0054,0414) and Patient Orientation Modifier Code Sequence
     * (0054,0412).
     * <p>
     * Type 3
     * 
     * @param cs
     */
    public void setPatientPosition(String cs) {
        dcmobj.putString(Tag.PatientPosition, VR.CS, cs);
    }

    public String getPatientPosition() {
        return dcmobj.getString(Tag.PatientPosition);
    }

    /**
     * Radiographic view of the image relative to the imaging subjectÕs
     * orientation.
     * <p>
     * Shall be consistent with View Code Sequence (0054,0220). See C.8.11.5.1.1
     * for further explanation.
     * <p>
     * Type 3
     * 
     * @param cs
     */
    public void setViewPosition(String cs) {
        dcmobj.putString(Tag.ViewPosition, VR.CS, cs);
    }

    public String getViewPosition() {
        return dcmobj.getString(Tag.ViewPosition);
    }
    
    public void setViewCode(ViewCode code) {
        updateSequence(Tag.ViewCodeSequence, code);        
    }

    public ViewCode getViewCode() {
        DicomObject item = dcmobj.getNestedDicomObject(
                Tag.ViewCodeSequence);
        return item != null ? new ViewCode(item) : null;        
    }

    public void setPatientOrientationCode(PatientOrientationCode code) {
        updateSequence(Tag.PatientOrientationCodeSequence, code);        
    }

    public PatientOrientationCode getPatientOrientationCode() {
        DicomObject item = dcmobj.getNestedDicomObject(
                Tag.PatientOrientationCodeSequence);
        return item != null ? new PatientOrientationCode(item) : null;        
    }


    /**
     * Sequence Sequence which describes the orientation of the patient with
     * respect to the gantry.
     * <p>
     * Only a single Item shall be permitted in this Sequence.
     * <p>
     * Type 3
     */
    public void setPatientGantryRelationshipCode(Code code) {
        updateSequence(Tag.PatientGantryRelationshipCodeSequence, code);
    }

    public Code getPatientGantryRelationshipCode() {
        DicomObject item = dcmobj.getNestedDicomObject(
                Tag.PatientGantryRelationshipCodeSequence);
        return item != null ? new Code(item) : null;
    }

    /**
     * Distance in mm from source to the table, support or bucky side that is
     * closest to the Imaging Subject, as measured along the central ray of the
     * X-Ray beam.
     * <p>
     * Note:
     * <ol>
     * <li> This definition is less useful in terms of estimating geometric
     * magnification than a measurement to a defined point within the Imaging
     * Subject, but accounts for what is realistically measurable in an
     * automated fashion in a clinical setting.
     * <li> This measurement does not take into account any air gap between the
     * Imaging Subject and the ÒfrontÓ of the table or bucky.
     * <li> If the detector is not mounted in a table or bucky, then the actual
     * position relative to the patient is implementation or operator defined.
     * <li> This value is traditionally referred to as Source Object Distance
     * (SOD).
     * </ol>
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * <p>
     * Type 3
     * <p>
     * @param f
     */
    public void setDistanceSourceToPatient(float f) {
        dcmobj.putFloat(Tag.DistanceSourceToPatient, VR.DS, f);
    }

    public float getDistanceSourceToPatient() {
        return dcmobj.getFloat(Tag.DistanceSourceToPatient);
    }

    /**
     * Distance in mm from source to detector center.
     * <p>
     * Note: This value is traditionally referred to as Source Image Receptor
     * Distance (SID).
     * <p>
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * <p>
     * Type 3
     * 
     * @param f
     */
    public void setDistanceSourceToDetector(float f) {
        dcmobj.putFloat(Tag.DistanceSourceToDetector, VR.DS, f);
    }

    public float getDistanceSourceToDetector() {
        return dcmobj.getFloat(Tag.DistanceSourceToDetector);
    }

    /**
     * Factor Ratio of Source Image Receptor Distance (SID) over Source Object
     * Distance (SOD).
     * <p>
     * Type 3
     * <p>
     * @param f
     */
    public void setEstimatedRadiographicMagnificationFactor(float f) {
        dcmobj.putFloat(Tag.EstimatedRadiographicMagnificationFactor, VR.DS, f);
    }

    public float getEstimatedRadiographicMagnificationFactor() {
        return dcmobj.getFloat(Tag.EstimatedRadiographicMagnificationFactor);
    }

    /**
     * Defined Terms: CARM COLUMN MAMMOGRAPHIC PANORAMIC CEPHALOSTAT RIGID NONE
     * Notes:
     * <ol>
     * <li> The term CARM can apply to any positioner with 2 degrees of freedom
     * of rotation of the X-Ray beam about the Imaging Subject.
     * <li> The term COLUMN can apply to any positioner with 1 degree of freedom
     * of rotation of the X-Ray beam about the Imaging Subject.
     * </ol>
     * 
     * Type 3
     * 
     * @param cs
     */
    public void setPositionerType(String cs) {
        dcmobj.putString(Tag.PositionerType, VR.CS, cs);
    }

    public String getPositionerType() {
        return dcmobj.getString(Tag.PositionerType);
    }

    /**
     * Position of the X-Ray beam about the patient from the RAO to LAO
     * direction where movement from RAO to vertical is positive, if Positioner
     * Type (0018,1508) is CARM.
     * <p>
     * See C.8.7.5 XA Positioner Module for further explanation if Positioner
     * Type (0018,1508) is CARM.
     * <p>
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * <p>
     * Type 3
     * 
     * @param f
     */
    public void setPositonerPrimaryAngle(float f) {
        dcmobj.putFloat(Tag.PositionerPrimaryAngle, VR.DS, f);
    }

    public float getPositionerPrimaryAngle() {
        return dcmobj.getFloat(Tag.PositionerPrimaryAngle);
    }

    /**
     * Position of the X-Ray beam about the patient from the CAU to CRA
     * direction where movement from CAU to vertical is positive, if Positioner
     * Type (0018,1508) is CARM.
     * <p>
     * See C.8.7.5 XA Positioner Module for further explanation if Positioner
     * Type (0018,1508) is CARM.
     * <p>
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * <p>
     * Type 3
     * 
     * @param f
     */
    public void setPositonerSecondaryAngle(float f) {
        dcmobj.putFloat(Tag.PositionerSecondaryAngle, VR.DS, f);
    }

    public float getPositionerSecondaryAngle() {
        return dcmobj.getFloat(Tag.PositionerSecondaryAngle);
    }

    /**
     * Angle of the X-Ray beam in the row direction in degrees relative to the
     * normal to the detector plane. Positive values indicate that the X-Ray
     * beam is tilted toward higher numbered columns. Negative values indicate
     * that the X-Ray beam is tilted toward lower numbered columns.
     * <p>
     * See C.8.7.5 XA Positioner Module for further explanation.
     * <p>
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * <p>
     * Type 3
     * 
     * @param f
     */
    public void setDetectorPrimaryAngle(float f) {
        dcmobj.putFloat(Tag.DetectorPrimaryAngle, VR.DS, f);
    }

    public float getDetectorPrimaryAngle() {
        return dcmobj.getFloat(Tag.DetectorPrimaryAngle);
    }

    /**
     * Angle of the X-Ray beam in the column direction in degrees relative to
     * the normal to the detector plane. Positive values indicate that the X-Ray
     * beam is tilted toward lower numbered rows. Negative values indicate that
     * the X-Ray beam is tilted toward higher numbered rows.
     * <p>
     * See C.8.7.5 XA Positioner Module for further explanation.
     * <p>
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * <p>
     * Type 3
     * 
     * @param f
     */
    public void setDetectorSecondaryAngle(float f) {
        dcmobj.putFloat(Tag.DetectorSecondaryAngle, VR.DS, f);
    }

    public float getDetectorSecondaryAngle() {
        return dcmobj.getFloat(Tag.DetectorSecondaryAngle);
    }

    /**
     * Angle of the X-Ray beam in degree relative to an orthogonal axis to the
     * detector plane. Positive values indicate that the tilt is toward the head
     * of the table.
     * <p>
     * Note: The detector plane is assumed to be parallel to the table plane.
     * <p>
     * Only meaningful if Positioner Type (0018,1508) is COLUMN.
     * <p>
     * Type 3
     * 
     * @param f
     */
    public void setColumnAngulation(float f) {
        dcmobj.putFloat(Tag.ColumnAngulation, VR.DS, f);
    }

    public float getColumnAngulation() {
        return dcmobj.getFloat(Tag.ColumnAngulation);
    }

    /**
     * Defined Terms: FIXED TILTING NONE
     * <p>
     * Type 3
     * 
     * @param cs
     */
    public void setTableType(String cs) {
        dcmobj.putString(Tag.TableType, VR.CS, cs);
    }

    public String getTableType() {
        return dcmobj.getString(Tag.TableType);
    }

    /**
     * Angle of table plane in degrees relative to horizontal plane [Gravity
     * plane]. Positive values indicate that the head of the table is upward.
     * <p>
     * Only meaningful if Table Type (0018,113A) is TILTING.
     * <p>
     * Type 3
     * 
     * @param f
     */
    public void setTableAngle(float f) {
        dcmobj.putFloat(Tag.TableAngle, VR.DS, f);
    }

    public float getTableAngle() {
        return dcmobj.getFloat(Tag.TableAngle);
    }

    /**
     * The average thickness in mm of the body part examined when compressed, if
     * compression has been applied during exposure.
     * <p>
     * Type 3
     * 
     * @param f
     */
    public void setBodyPartThickness(float f) {
        dcmobj.putFloat(Tag.BodyPartThickness, VR.DS, f);
    }

    public float getBodyPartThickness() {
        return dcmobj.getFloat(Tag.BodyPartThickness);
    }

    /**
     * The compression force applied to the body part during exposure, measured
     * in Newtons.
     * <p>
     * Type 3
     * 
     * @param f
     */
    public void setCompressionForce(float f) {
        dcmobj.putFloat(Tag.CompressionForce, VR.DS, f);
    }

    public float getCompressionForce() {
        return dcmobj.getFloat(Tag.CompressionForce);
    }

}
