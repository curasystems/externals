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

package org.dcm4che2.iod.module.lut;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 4738 $ $Date: 2007-07-24 15:39:05 +0200 (Tue, 24 Jul 2007) $
 * @since Jun 9, 2006
 *
 */
public class VoiLutModule extends Module {

    public VoiLutModule(DicomObject dcmobj) {
        super(dcmobj);
    }

	/** Gets the named VOI LUT.  Null means the first one found.
	 * Throws a runtime exception if the explanation for a window level isn't associated
	 * with a window center and width (either they aren't present, or there aren't
	 * enough elemetns.)
	 * @param name to look for the VOI LUT for.
	 * @return the VOI LUT with explanation the same as name, or null if not found.
	 */
	public ILut getVOILut(String name) {
		String[] wlExplanations = getWindowCenterWidthExplanations();
		if (wlExplanations != null && wlExplanations.length > 0) {
			if (name == null)
				return new WindowLevelLut(getWindowCenter()[0],
						getWindowWidth()[0], wlExplanations[0]);
			for (int i = 0; i < wlExplanations.length; i++) {
				if (name.equals(wlExplanations[i])) {
					return new WindowLevelLut(getWindowCenter()[i],
							getWindowWidth()[i], wlExplanations[i]);
				}
			}
		}
		Lut[] luts = getVOILUTs();
		if (luts == null || luts.length == 0)
			return null;
		if (name == null) {
			throw new UnsupportedOperationException(
					"Still need to implement Lut class generally.");
		}
		for (int i = 0; i < luts.length; i++) {
			Lut lut = luts[i];
			if (name.equals(lut.getLUTExplanation())) {
				throw new UnsupportedOperationException(
						"Still need to implement Lut class generally.");
			}
		}
		// Not found.
		return null;
	}

	public Lut[] getVOILUTs() {
		return Lut.toLUTs(dcmobj.get(Tag.VOILUTSequence));
	}

	public void setVOILUTs(Lut[] luts) {
		updateSequence(Tag.VOILUTSequence, luts);
	}

	/**
	 * Defines a Window Center for display.
	 * <p>
	 * See C.8.11.3.1.5 for further explanation.
	 * <p>
	 * Required if Presentation Intent Type (0008,0068) is FOR PRESENTATION and
	 * VOI LUT Sequence (0028,3010) is not present. May also be present if VOI
	 * LUT Sequence (0028,3010) is present.
	 * @param centers associated with each window level (explanation).
	 */
	public void setWindowCenter(float[] centers) {
		dcmobj.putFloats(Tag.WindowCenter, VR.DS, centers);
	}

	/**
	 * Defines a Window Center for display.
	 * <p>
	 * See C.8.11.3.1.5 for further explanation.
	 * <p>
	 * Required if Presentation Intent Type (0008,0068) is FOR PRESENTATION and
	 * VOI LUT Sequence (0028,3010) is not present. May also be present if VOI
	 * LUT Sequence (0028,3010) is present.
	 * 
	 * @return
	 */
	public float[] getWindowCenter() {
		return dcmobj.getFloats(Tag.WindowCenter);
	}

	/**
	 * Window Width for display.
	 * <p>
	 * See C.8.11.3.1.5 for further explanation.
	 * <p>
	 * Required if Window Center (0028,1050) is sent.
	 * 
	 * @param windowWidths are the window widths to put into the dicom object.
	 */
	public void setWindowWidth(float[] widths) {
		dcmobj.putFloats(Tag.WindowWidth, VR.DS, widths);
	}

	/**
	 * Window Width for display.
	 * <p>
	 * See C.8.11.3.1.5 for further explanation.
	 * <p>
	 * Required if Window Center (0028,1050) is sent.
	 * 
	 * @return An array of window widths corresponding to the window center/width explanation.
	 */
	public float[] getWindowWidth() {
		return dcmobj.getFloats(Tag.WindowWidth);
	}

	/**
	 * Free form explanation of the meaning of the Window Center and Width.
	 * <p>
	 * Multiple values correspond to multiple Window Center and Width values.
	 * 
	 * @param explanations to associate with window levels.
	 */
	public void setWindowCenterWidthExplanation(String[] explanations) {
		dcmobj
				.putStrings(Tag.WindowCenterWidthExplanation, VR.LO,
						explanations);
	}

	/**
	 * Free form explanation of the meaning of the Window Center and Width.
	 * <p>
	 * Multiple values correspond to multiple Window Center and Width values.
	 * 
	 * @return an array of explanatory names for each window level (width/center)
	 */
	public String[] getWindowCenterWidthExplanations() {
		return dcmobj.getStrings(Tag.WindowCenterWidthExplanation);
	}

}
