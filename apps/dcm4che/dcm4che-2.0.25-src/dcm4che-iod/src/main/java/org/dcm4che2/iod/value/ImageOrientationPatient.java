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

package org.dcm4che2.iod.value;

/**
 * A Class to represent Image Orientation (0020,0037) values.
 * <p>
 * Image Orientation (0020,0037) specifies the direction cosines of the first
 * row and the first column with respect to the patient. These Attributes shall
 * be provide as a pair. Row value for the x, y, and z axes respectively
 * followed by the Column value for the x, y, and z axes respectively.
 * <p>
 * The fields are named after the orientation of the patient, as in
 * {@link PatientOrientation}.
 * 
 * @see PatientOrientation
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class ImageOrientationPatient {
	public static final float[] AF = { 0,-1, 0, 0, 0,-1 };

	public static final float[] PF = { 0, 1, 0, 0, 0,-1 };

	public static final float[] LF = { 1, 0, 0, 0, 0,-1 };

	public static final float[] RF = {-1, 0, 0, 0, 0,-1 };
	
	public static final float[] FP = { 0, 0,-1, 0, 1, 0 };
	
	// TODO finish the rest.

}
