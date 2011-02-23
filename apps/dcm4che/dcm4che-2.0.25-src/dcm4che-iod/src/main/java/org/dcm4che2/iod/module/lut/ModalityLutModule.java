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
public class ModalityLutModule extends Module {

    public ModalityLutModule(DicomObject dcmobj) {
        super(dcmobj);
    }

	/**
	 * The value b in the relationship between stored values (SV) in Pixel Data
	 * (7FE0,0010) and the output units specified in Rescale Type (0028,1054).
	 * <p>
	 * Output units = m*SV + b.
	 * <p>
	 * Enumerated Value: 0
	 * <p>
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param ds
	 *            0
	 */
	public void setRescaleIntercept(float intercept) {
		dcmobj.putFloat(Tag.RescaleIntercept, VR.DS, intercept);
	}

	/**
	 * Get the rescale intercept value.
	 * @return Rescale intercept value.
	 */
	public float getRescaleIntercept() {
		return dcmobj.getFloat(Tag.RescaleIntercept);
	}

	/**
	 * m in the equation specified by Rescale Intercept (0028,1052).
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param slope for the rescale intercept.
	 */
	public void setRescaleSlope(float slope) {
		dcmobj.putFloat(Tag.RescaleSlope, VR.DS, slope);
	}

	/**
	 * m in the equation specified by Rescale Intercept (0028,1052).
	 * 
	 * @return the recale slope.
	 */
	public float getRescaleSlope() {
		return dcmobj.getFloat(Tag.RescaleSlope);
	}

	/**
	 * Specifies the output units of Rescale Slope (0028,1053) and Rescale
	 * Intercept (0028,1052).
	 * <p>
	 * Enumerated Value: US = Unspecified
	 * Enumerated Value: OD = Optical Density
	 * Enumerated Value: HU = Houndsfield Units
	 * <p>
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param type, US = Unspecified, OD optical density, HU houndsfield units.
	 */
	public void setRescaleType(String type) {
		dcmobj.putString(Tag.RescaleType, VR.CS, type);
	}

	/**
	 * Specifies the output units of Rescale Slope (0028,1053) and Rescale
	 * Intercept (0028,1052).
	 * <p>
	 * Enumerated Value: US = Unspecified
	 * <p>
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @return
	 */
	public String getRescaleType() {
		return dcmobj.getString(Tag.RescaleType);
	}

	/** Gets the modality LUT to use 
	 * @todo implement setModalityLut to set the modality lut instead.
	 */
	public ILut getModalityLut() {
		if (dcmobj.contains(Tag.RescaleSlope)) {
			return new RescaleLut(getRescaleSlope(), getRescaleIntercept(),
					getRescaleType());
		}
		return null;
	}

}
