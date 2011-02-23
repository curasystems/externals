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
 * Bill Wallace, <wayfarer3130@gmail.com>
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
package org.dcm4che2.iod.module.pr;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;

/**
 * C.7.6.11 Shutter to opaque areas.  Also includes C.11.12 for the colour of the
 * opaqued area, and C.7.6.15 for bitmap shutters.
 * @author Bill Wallace <wayfarer3130@gmail.com>
 * @version $Revision$ $Date$
 * @since July 19, 2007
 */
public class DisplayShutterModule extends Module {

	/** Create a display shutter IOD instance on the given dicom object data. */
	public DisplayShutterModule(DicomObject dcmobj) {
		super(dcmobj);
	}
	
	public String[] getShutterShapes() {
		return dcmobj.getStrings(Tag.ShutterShape);
	}
	
	public void setShutterShapes(String[] shapes) {
		dcmobj.putStrings(Tag.ShutterShape, VR.CS, shapes);
	}
	
	public int[] getCenterOfCircularShutter() {
		return dcmobj.getInts(Tag.CenterOfCircularShutter);
	}
	
	public void setCenterOfCircularShutter(int[] center) {
		dcmobj.putInts(Tag.CenterOfCircularShutter, VR.IS, center);
	}
	
	public int getRadiusOfCircularShutter() {
		return dcmobj.getInt(Tag.RadiusOfCircularShutter);
	}
	
	public void setRadiusOfCircularShutter(int radius) {
		dcmobj.putInt(Tag.RadiusOfCircularShutter,VR.IS,radius);
	}
	
	public int getShutterLeftVerticalEdge() {
		return dcmobj.getInt(Tag.ShutterLeftVerticalEdge);
	}
	public int getShutterRightVerticalEdge() {
		return dcmobj.getInt(Tag.ShutterRightVerticalEdge);
	}
	public int getShutterUpperHorizontalEdge() {
		return dcmobj.getInt(Tag.ShutterUpperHorizontalEdge);
	}
	public int getShutterLowerHorizontalEdge() {
		return dcmobj.getInt(Tag.ShutterLowerHorizontalEdge);
	}
	public void setShutterLeftVerticalEdge(int value) {
		dcmobj.putInt(Tag.ShutterLeftVerticalEdge,VR.IS, value);
	}
	public void setShutterRightVerticalEdge(int value) {
		dcmobj.putInt(Tag.ShutterRightVerticalEdge,VR.IS, value);
	}
	public void setShutterUpperHorizontalEdge(int value) {
		dcmobj.putInt(Tag.ShutterUpperHorizontalEdge,VR.IS, value);
	}
	public void setShutterLowerHorizontalEdge(int value) {
		dcmobj.putInt(Tag.ShutterLowerHorizontalEdge,VR.IS, value);
	}
	
	/** Returns  asingle gray unsigned value to replace occluded parts of the imatge
	 * p-values form 0 to FFFFH (white).
	 */
	public int getShutterPresentationValue() {
		return dcmobj.getInt(Tag.ShutterPresentationValue);
	}
	
	/** Returns the CIELab value as PCS-Values for the shutter colour.
	 * @return triplet L*a*b* where L is scaled from 0 to 0xFFFF correspondign to L 0 to 100
	 * and a*, b* 0x000 of -128, 0x8080 of 0.0 and 0xFFFF of 127.
	 */ 
	public int[] getShutterPresentationColorCIELabValue() {
		return dcmobj.getInts(Tag.ShutterPresentationColorCIELabValue);
	}

	/** This version of getShutterPresentationColorCIELabValue returns converted
	 * L*a*b* values on 0..100, -128..127, -128..127 respectively.
	 */
	public float[] getFloatLab() {
		int[] lab = getShutterPresentationColorCIELabValue();
		return convertToFloatLab(lab);
	}
	
	/** This method converts integer DICOM encoded L*a*b* values to CIE L*a*b* regular
	 * float encoded values.
	 * @param lab
	 * @return float array of 3 components L* on 0..1 and a*,b* on -128...127
	 */
	public static float[] convertToFloatLab(int[] lab) {
		if( lab==null || lab.length!=3 ) return null;
		float[] ret = new float[3];
		ret[0] = lab[0] / 655.35f;
		ret[1] = lab[1] / 257.0f - 128;
		ret[2] = lab[2] / 257.0f - 128;
		return ret;
	}
	
	public int[] getVerticesOfThePolygonalShutter() {
		return dcmobj.getInts(Tag.VerticesOfThePolygonalShutter);
	}
}
