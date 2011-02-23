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
 * The value of Photometric Interpretation (0028,0004) specifies the intended
 * interpretation of the image pixel data.
 * <p>
 * See PS 3.5 for restrictions imposed by compressed Transfer Syntaxes.
 * <p>
 * The following values are defined. Other values are permitted but the meaning
 * is not defined by this Standard.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class PhotometricInterpretation {

	/**
	 * Pixel data represent a single monochrome image plane. The minimum sample
	 * value is intended to be displayed as white after any VOI gray scale
	 * transformations have been performed. See PS 3.4. This value may be used
	 * only when Samples per Pixel (0028,0002) has a value of 1.
	 */
	public static String MONOCHROME1 = "MONOCHROME1";

	/**
	 * Pixel data represent a single monochrome image plane. The minimum sample
	 * value is intended to be displayed as black after any VOI gray scale
	 * transformations have been performed. See PS 3.4. This value may be used
	 * only when Samples per Pixel (0028,0002) has a value of 1.
	 */
	public static String MONOCHROME2 = "MONOCHROME2";

	/**
	 * Pixel data describe a color image with a single sample per pixel (single
	 * image plane). The pixel value is used as an index into each of the Red,
	 * Blue, and Green Palette Color Lookup Tables (0028,1101-1103&1201-1203).
	 * This value may be used only when Samples per Pixel (0028,0002) has a
	 * value of 1. When the Photometric Interpretation is Palette Color; Red,
	 * Blue, and Green Palette Color Lookup Tables shall be present. RGB = Pixel
	 * data represent a color image described by red, green, and blue image
	 * planes. The minimum sample value for each color plane represents minimum
	 * intensity of the color. This value may be used only when Samples per
	 * Pixel (0028,0002) has a value of 3.
	 */
	public static String PALETTE_COLOR = "PALETTE COLOR";

	/**
	 * Pixel data represent a color image described by one luminance (Y) and two
	 * chrominance planes (CB and CR). This photometric interpretation may be
	 * used only when Samples per Pixel (0028,0002) has a value of 3. Black is
	 * represented by Y equal to zero. The absence of color is represented by
	 * both CB and CR values equal to half full scale.
	 */
	public static String YBR_FULL = "YBR_FULL";

	/**
	 * The same as YBR_FULL except that the CB and CR values are sampled
	 * horizontally at half the Y rate and as a result there are half as many CB
	 * and CR values as Y values.
	 */
	public static String YBR_FULL_422 = "YBR_FULL_422";

	/**
	 * The same as YBR_FULL_422 except that:
	 * <ol>
	 * <li> black corresponds to Y = 16;
	 * <li> Y is restricted to 220 levels (i.e. the maximum value is 235);
	 * <li> CB and CR each has a minimum value of 16;
	 * <li> CB and CR are restricted to 225 levels (i.e. the maximum value is
	 * 240);
	 * <li> lack of color is represented by CB and CR equal to 128.
	 * </ol>
	 */
	public static String YBR_PARTIAL_422 = "YBR_PARTIAL_422";

	/**
	 * The same as YBR_PARTIAL_422 except that the CB and CR values are sampled
	 * horizontally and vertically at half the Y rate and as a result there are
	 * four times less CB and CR values than Y values, versus twice less for
	 * YBR_PARTIAL_422.
	 */
	public static String YBR_PARTIAL_420 = "YBR_PARTIAL_420";

	/**
	 * Pixel data represent a color image described by one luminance (Y) and two
	 * chrominance planes (CB and CR). This photometric interpretation may be
	 * used only when Samples per Pixel (0028,0002) has a value of 3. Black is
	 * represented by Y equal to zero. The absence of color is represented by
	 * both CB and CR values equal to zero.
	 */
	public static String YBR_ICT = "YBR_ICT";

	/**
	 * Pixel data represent a color image described by one luminance (Y) and two
	 * chrominance planes (CB and CR). This photometric interpretation may be
	 * used only when Samples per Pixel (0028,0002) has a value of 3. Black is
	 * represented by Y equal to zero. The absence of color is represented by
	 * both CB and CR values equal to zero.
	 */
	public static String YBR_RCT = "YBR_RCT";

	/**
	 * The following values are defined. Other values are permitted but the
	 * meaning is not defined by this Standard.
	 * 
	 * @param ss
	 * @return True unless ss is null.
	 */
	public static boolean isValid(String ss) {
		return ss != null;
	}
}
