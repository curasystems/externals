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

package org.dcm4che2.iod.module.spatial;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.macro.Code;
import org.dcm4che2.iod.module.spatial.GraphicCoordinatesData;

/**
 * This class represents a Fiducial.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision: 720 $ $Date: 2006-11-26 18:40:54 +0100 (Sun, 26 Nov 2006) $
 */
public class Fiducial extends GraphicCoordinatesData {

	public Fiducial(DicomObject dcmobj) {
		super(dcmobj);
	}

	public Fiducial() {
		super(new BasicDicomObject());
	}

	public static Fiducial[] toFiducials(DicomElement sq) {
		if (sq == null || !sq.hasItems()) {
			return null;
		}
		Fiducial[] a = new Fiducial[sq.countItems()];
		for (int i = 0; i < a.length; i++) {
			a[i] = new Fiducial(sq.getDicomObject(i));
		}
		return a;

	}

	/**
	 * A fiducial assignment identifier.
	 * 
	 * A fiducial assignment identifier that is unique within this Fiducial
	 * Sequence item but may match the fiducial identifier of an equivalent
	 * feature in another item.
	 * <p>
	 * Type 1
	 * 
	 * @return
	 */
	public String getFiducialIdentifier() {
		return dcmobj.getString(Tag.FiducialIdentifier);
	}

	/**
	 * A fiducial assignment identifier.
	 * 
	 * A fiducial assignment identifier that is unique within this Fiducial
	 * Sequence item but may match the fiducial identifier of an equivalent
	 * feature in another item.
	 * <p>
	 * Type 1
	 * 
	 * @param s
	 */
	public void setFiducialIdentifier(String sh) {
		dcmobj.putString(Tag.FiducialIdentifier, VR.SH, sh);
	}

	/**
	 * Sequence A code sequence for a term that identifies a well-known fiducial
	 * type (potentially including methodology, anatomy, tools, etc.). Only one
	 * item shall be present. Required if Identifier (0070,0310) isabsent. May
	 * be present otherwise.
	 * <p>
	 * Type 1C
	 * 
	 * @param code
	 */
	public void setFiducialIdentifierCode(Code code) {
		updateSequence(Tag.FiducialIdentifierCodeSequence, code);
	}

	/**
	 * Sequence A code sequence for a term that identifies a well-known fiducial
	 * type (potentially including methodology, anatomy, tools, etc.). Only one
	 * item shall be present. Required if Identifier (0070,0310) isabsent. May
	 * be present otherwise.
	 * <p>
	 * Type 1C
	 * 
	 * @return
	 */
	public Code getFiducialIdentifierCode() {
		DicomObject item = dcmobj
				.getNestedDicomObject(Tag.FiducialIdentifierCodeSequence);
		return item != null ? new Code(item) : null;
	}

	/**
	 * Globally unique identifier for the fiducial instance of this fiducial
	 * assignment.
	 * <p>
	 * Type 3
	 * 
	 * @return
	 */
	public String getFiducialUID() {
		return dcmobj.getString(Tag.FiducialUID);
	}

	/**
	 * Globally unique identifier for the fiducial instance of this fiducial
	 * assignment.
	 * <p>
	 * Type 3
	 * 
	 * @param ui
	 */
	public void setFiducialUID(String ui) {
		dcmobj.putString(Tag.FiducialUID, VR.UI, ui);
	}

	/**
	 * User description or comments about the fiducial.
	 * <p>
	 * Type 3
	 * 
	 * @return
	 */
	public String getFiducialDescription() {
		return dcmobj.getString(Tag.FiducialDescription);
	}

	/**
	 * User description or comments about the fiducial.
	 * <p>
	 * Type 3
	 * 
	 * @param st
	 */
	public void setFiducialDescription(String st) {
		dcmobj.putString(Tag.FiducialDescription, VR.ST, st);
	}

	/**
	 * Geometric interpretation of countour and graphic data.
	 * 
	 * Shape Type (0070,0306) defines the geometric interpretation of the
	 * Contour Data (3006,0050) and Graphic Data (0070,0022). A point is defined
	 * as a triplet (x,y,z) in the case of spatial data or a pair (x,y) in the
	 * case of graphic data.
	 * <p>
	 * Type 1
	 * 
	 * @return
	 */
	public String getShapeType() {
		return dcmobj.getString(Tag.ShapeType);
	}

	/**
	 * Geometric interpretation of countour and graphic data.
	 * 
	 * Shape Type (0070,0306) defines the geometric interpretation of the
	 * Contour Data (3006,0050) and Graphic Data (0070,0022). A point is defined
	 * as a triplet (x,y,z) in the case of spatial data or a pair (x,y) in the
	 * case of graphic data.
	 * <p>
	 * Type 1
	 * 
	 * @param cs
	 */
	public void setShapeType(String cs) {
		dcmobj.putString(Tag.ShapeType, VR.CS, cs);
	}

	/**
	 * Number of points (triplets) in Contour Data (3006,0050). Required if
	 * Contour Data is present.
	 * <p>
	 * Type 1C
	 * 
	 * @return
	 */
	public int getNumberOfContourPoints() {
		return dcmobj.getInt(Tag.NumberOfContourPoints);
	}

	/**
	 * Number of points (triplets) in Contour Data (3006,0050). Required if
	 * Contour Data is present.
	 * <p>
	 * Type 1C
	 * 
	 * @param is
	 */
	public void setNumberOfContourPoints(int is) {
		dcmobj.putInt(Tag.NumberOfContourPoints, VR.IS, is);
	}

	/**
	 * Specifies the coordinates of this item's fiducial. One triplet (x,y,z)
	 * shall be present for each point in the fiducial. See C.21.2.1.2 for
	 * further explanation. Required if Frame of Reference UID (0020,0052) is
	 * present in this item of the Fiducial Set Sequence (0070,031C). Shall not
	 * be present otherwise.
	 * <p>
	 * Note: Contour Data may not be properly encoded if Explicit-VR transfer
	 * syntax is used and the VL of this attribute exceeds 65534 bytes.
	 * <p>
	 * Type 1C
	 * 
	 * @return
	 */
	public float[] getContourData() {
		return dcmobj.getFloats(Tag.ContourData);
	}

	/**
	 * Specifies the coordinates of this item's fiducial. One triplet (x,y,z)
	 * shall be present for each point in the fiducial. See C.21.2.1.2 for
	 * further explanation. Required if Frame of Reference UID (0020,0052) is
	 * present in this item of the Fiducial Set Sequence (0070,031C). Shall not
	 * be present otherwise.
	 * <p>
	 * Note: Contour Data may not be properly encoded if Explicit-VR transfer
	 * syntax is used and the VL of this attribute exceeds 65534 bytes.
	 * <p>
	 * Type 1C
	 * 
	 * @param ds
	 */
	public void setContourData(float[] ds) {
		dcmobj.putFloats(Tag.ContourData, VR.DS, ds);
	}

	/**
	 * The estimated uncertainty radius.
	 * 
	 * The estimated uncertainty radius for the Contour Data in mm. See
	 * C.21.2.1.3
	 * <p>
	 * Type 3
	 * 
	 * @return
	 */
	public double getContourUncertaintyRadius() {
		return dcmobj.getDouble(Tag.ContourUncertaintyRadius);
	}

	/**
	 * The estimated uncertainty radius.
	 * 
	 * The estimated uncertainty radius for the Contour Data in mm. See
	 * C.21.2.1.3
	 * <p>
	 * Type 3
	 * 
	 * @param fd
	 */
	public void setContourUncertaintyRadius(double fd) {
		dcmobj.putDouble(Tag.ContourUncertaintyRadius, VR.FD, fd);
	}

	/**
	 * The image pixel locations of the fiducial�s points. Shall contain one or
	 * more items. More than one item shall be present only if a fiducial spans
	 * more than one image. Required if Contour Data is not present. May be
	 * present otherwise.
	 * <p>
	 * Type 1C
	 * 
	 * @return
	 */
	public GraphicCoordinatesData[] getGraphicCoordinatesData() {
		return GraphicCoordinatesData.toGraphicCoordinatesData(dcmobj
				.get(Tag.GraphicCoordinatesDataSequence));
	}

	/**
	 * The image pixel locations of the fiducial�s points. Shall contain one or
	 * more items. More than one item shall be present only if a fiducial spans
	 * more than one image. Required if Contour Data is not present. May be
	 * present otherwise.
	 * <p>
	 * Type 1C
	 * 
	 * @param gcds
	 */
	public void setGraphicCoordinatesData(GraphicCoordinatesData[] gcds) {
		updateSequence(Tag.GraphicCoordinatesDataSequence, gcds);
	}

}
