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

package org.dcm4che2.iod.module.composite;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.macro.Code;
import org.dcm4che2.iod.module.macro.ImageSOPInstanceReferenceAndPurpose;
import org.dcm4che2.iod.module.macro.SOPInstanceReferenceAndPurpose;
import org.dcm4che2.iod.validation.ValidationContext;
import org.dcm4che2.iod.validation.ValidationResult;
import org.dcm4che2.iod.value.Flag;
import org.dcm4che2.iod.value.LossyImageCompression;
import org.dcm4che2.iod.value.PatientOrientation;
import org.dcm4che2.iod.value.PresentationLUTShape;

/**
 * Class to represent the General Image Module (C.7.6.1)
 * <p>
 * This class is the parent class for all Image Modules, as it contains
 * attributes that are image specific. It extends
 * {@link org.dcm4che2.iod.module.composite.ImagePixel}, so the child classes
 * have all necessary attributes to correctly describe images.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 6915 $ $Date: 2008-08-31 23:36:20 +0200 (Sun, 31 Aug 2008) $
 * @since Jun 9, 2006
 * 
 */
public class GeneralImageModule extends ImagePixel {

	public GeneralImageModule(DicomObject dcmobj) {
		super(dcmobj);
	}

	@Override
        public void init() {
		super.init();
		setSamplesPerPixel(1);
		setPixelRepresentation(0);
	}

	@Override
        public void validate(ValidationContext ctx, ValidationResult result) {
		super.validate(ctx, result);
		if (!PresentationLUTShape.isValidSoftCopy(getPresentationLUTShape())) {
			result.logInvalidValue(Tag.PresentationLUTShape, dcmobj);
		}
		if (!LossyImageCompression.isValid(getLossyImageCompression())) {
			result.logInvalidValue(Tag.LossyImageCompression, dcmobj);
		}
		if (!Flag.isValid(getBurnedInAnnotation())) {
			result.logInvalidValue(Tag.BurnedInAnnotation, dcmobj);
		}
	}

	/**
	 * A number that identifies this image.
	 * <p>
	 * Please not that this is an IS DICOM value, which is supposed to be
	 * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
	 * because:
	 * <ul>
	 * <li> I have already seen objects, which uses non-numeric values for this
	 * identifiers.
	 * <li>For identifiers, the non-numeric value may still of some
	 * use/information as opposed to e.g. a non-numeric Frame Number..
	 * </ul>
	 * <p>
	 * Type 2
	 * 
	 * @return
	 */
	public String getInstanceNumber() {
		return dcmobj.getString(Tag.InstanceNumber);
	}

	/**
	 * A number that identifies this image.
	 * <p>
	 * Please not that this is an IS DICOM value, which is supposed to be
	 * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
	 * because:
	 * <ul>
	 * <li> I have already seen objects, which uses non-numeric values for this
	 * identifiers.
	 * <li>For identifiers, the non-numeric value may still of some
	 * use/information as opposed to e.g. a non-numeric Frame Number..
	 * </ul>
	 * <p>
	 * Type 2
	 * 
	 * @param s
	 */
	public void setInstanceNumber(String s) {
		dcmobj.putString(Tag.InstanceNumber, VR.IS, s);
	}

	/**
	 * @see PatientOrientation
	 * @return
	 */
	public String[] getPatientOrientation() {
		return dcmobj.getStrings(Tag.PatientOrientation);
	}

	/**
	 * @see PatientOrientation
	 * @param s
	 */
	public void setPatientOrientation(String[] s) {
		dcmobj.putStrings(Tag.PatientOrientation, VR.CS, s);
	}

	public Date getContentDateTime() {
		return dcmobj.getDate(Tag.ContentDate, Tag.ContentTime);
	}

	public void setContentDateTime(Date d) {
		dcmobj.putDate(Tag.ContentDate, VR.DA, d);
		dcmobj.putDate(Tag.ContentTime, VR.TM, d);
	}

	public String[] getImageType() {
		return dcmobj.getStrings(Tag.ImageType);
	}

	public void setImageType(String[] s) {
		dcmobj.putStrings(Tag.ImageType, VR.CS, s);
	}

	/**
	 * A number identifying the single continuous gathering of data over a
	 * period of time that resulted in this image.
	 * <p>
	 * Please not that this is an IS DICOM value, which is supposed to be
	 * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
	 * because:
	 * <ul>
	 * <li> I have already seen objects, which uses non-numeric values for this
	 * identifiers.
	 * <li>For identifiers, the non-numeric value may still of some
	 * use/information as opposed to e.g. a non-numeric Frame Number..
	 * </ul>
	 * <p>
	 * Type 3
	 * 
	 * @return
	 */
	public String getAcquisitionNumber() {
		return dcmobj.getString(Tag.AcquisitionNumber);
	}

	/**
	 * A number identifying the single continuous gathering of data over a
	 * period of time that resulted in this image.
	 * <p>
	 * Please not that this is an IS DICOM value, which is supposed to be
	 * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
	 * because:
	 * <ul>
	 * <li> I have already seen objects, which uses non-numeric values for this
	 * identifiers.
	 * <li>For identifiers, the non-numeric value may still of some
	 * use/information as opposed to e.g. a non-numeric Frame Number..
	 * </ul>
	 * <p>
	 * Type 3
	 * 
	 * @param s
	 */
	public void setAcquisitionNumber(String s) {
		dcmobj.putString(Tag.AcquisitionNumber, VR.IS, s);
	}

	public Date getAcquisitionDateAndTime() {
		return dcmobj.getDate(Tag.AcquisitionDate, Tag.AcquisitionTime);
	}

	public void setAcquisitionDateAndTime(Date d) {
		dcmobj.putDate(Tag.AcquisitionDate, VR.DA, d);
		dcmobj.putDate(Tag.AcquisitionTime, VR.TM, d);
	}

	public Date getAcquisitionDateTime() {
		return dcmobj.getDate(Tag.AcquisitionDateTime);
	}

	public void setAcquisitionDateTime(Date d) {
		dcmobj.putDate(Tag.AcquisitionDateTime, VR.DT, d);
	}

	public ImageSOPInstanceReferenceAndPurpose[] getReferencedImages() {
		return ImageSOPInstanceReferenceAndPurpose
				.toImageSOPInstanceReferenceAndPurposes(dcmobj
						.get(Tag.ReferencedImageSequence));
	}

	public void setReferencedImages(ImageSOPInstanceReferenceAndPurpose[] sops) {
		updateSequence(Tag.ReferencedImageSequence, sops);
	}

	public String getDerivationDescription() {
		return dcmobj.getString(Tag.DerivationDescription);
	}

	public void setDerivationDescription(String s) {
		dcmobj.putString(Tag.DerivationDescription, VR.ST, s);
	}

	public Code[] getDerivationCodes() {
		return Code.toCodes(dcmobj.get(Tag.DerivationCodeSequence));
	}

	public void setDerivationCodes(Code[] codes) {
		updateSequence(Tag.DerivationCodeSequence, codes);
	}

	public SourceImage[] getSourceImages() {
		return SourceImage.toSourceImages(dcmobj.get(Tag.SourceImageSequence));
	}

	public void setSourceImages(SourceImage[] sops) {
		updateSequence(Tag.SourceImageSequence, sops);
	}

	public SOPInstanceReferenceAndPurpose[] getReferencedInstances() {
		return SOPInstanceReferenceAndPurpose
				.toSOPInstanceReferenceAndPurposes(dcmobj
						.get(Tag.ReferencedInstanceSequence));
	}

	public void setReferencedInstances(SOPInstanceReferenceAndPurpose[] sops) {
		updateSequence(Tag.ReferencedInstanceSequence, sops);
	}

	public int getImagesInAcquisition() {
		return dcmobj.getInt(Tag.ImagesInAcquisition);
	}

	public void setImagesInAcquisition(int i) {
		dcmobj.putInt(Tag.ImagesInAcquisition, VR.IS, i);
	}

	public String getImageComments() {
		return dcmobj.getString(Tag.ImageComments);
	}

	public void setImageComments(String s) {
		dcmobj.putString(Tag.ImageComments, VR.LT, s);
	}

	public String getQualityControlImage() {
		return dcmobj.getString(Tag.QualityControlImage);
	}

	public void setQualityControlImage(String s) {
		dcmobj.putString(Tag.QualityControlImage, VR.CS, s);
	}

	public String getBurnedInAnnotation() {
		return dcmobj.getString(Tag.BurnedInAnnotation);
	}

	public void setBurnedInAnnotation(String s) {
		dcmobj.putString(Tag.BurnedInAnnotation, VR.CS, s);
	}

	public String getLossyImageCompression() {
		return dcmobj.getString(Tag.LossyImageCompression);
	}

	public void setLossyImageCompression(String s) {
		dcmobj.putString(Tag.LossyImageCompression, VR.CS, s);
	}

	public float[] getLossyImageCompressionRatio() {
		return dcmobj.getFloats(Tag.LossyImageCompressionRatio);
	}

	public void setLossyImageCompression(float[] floats) {
		dcmobj.putFloats(Tag.LossyImageCompressionRatio, VR.DS, floats);
	}

	public String[] getLossyImageCompressionMethod() {
		return dcmobj.getStrings(Tag.LossyImageCompressionMethod);
	}

	public void setLossyImageCompressionMethod(String[] ss) {
		dcmobj.putStrings(Tag.LossyImageCompressionMethod, VR.CS, ss);
	}

	public ImagePixel getIconImage() {
		DicomObject item = dcmobj.getNestedDicomObject(Tag.IconImageSequence);
		return item != null ? new ImagePixel(item) : null;
	}

	public void setIconImage(ImagePixel icon) {
		updateSequence(Tag.IconImageSequence, icon);
	}

	public String getPresentationLUTShape() {
		return dcmobj.getString(Tag.PresentationLUTShape);
	}

	public void setPresentationLUTShape(String s) {
		dcmobj.putString(Tag.PresentationLUTShape, VR.CS, s);
	}

	public String getIrradiationEventUID() {
		return dcmobj.getString(Tag.IrradiationEventUID);
	}

	public void setIrradiationEventUID(String s) {
		dcmobj.putString(Tag.IrradiationEventUID, VR.UI, s);
	}

	public String getPixelDataProviderURL() {
		return dcmobj.getString(Tag.PixelDataProviderURL);
	}

	public void setPixelDataProviderURL(String s) {
		dcmobj.putString(Tag.PixelDataProviderURL, VR.UT, s);
	}
}
