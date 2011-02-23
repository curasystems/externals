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

package org.dcm4che2.iod.module.macro;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * A macro for the primary anatomic structure modifier.
 * 
 * This macro is embedded by the Primary Anatomic Structure Macro, and is not an
 * official DICOM macro. In DICOM it appears as attributes of the Primary
 * Anatomic Strucure Macro (Table 10-8 PS3.3 2006).
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class PrimaryAnatomicStructureCode extends Code {

	public PrimaryAnatomicStructureCode(DicomObject dcmobj) {
		super(dcmobj);
	}

	public static PrimaryAnatomicStructureCode[]
            toPrimaryAnatomicStructureCodes(DicomElement sq) {
		if (sq == null || !sq.hasItems()) {
			return null;
		}
		PrimaryAnatomicStructureCode[] a = 
            new PrimaryAnatomicStructureCode[sq.countItems()];
		for (int i = 0; i < a.length; i++) {
			a[i] = new PrimaryAnatomicStructureCode(
                    sq.getDicomObject(i));
		}
		return a;
	}

	/**
	 * Sets codes for this primary anatomic structure modifier.
	 * 
	 * See Table 10-8 PS3.3 2006
	 * 
	 * @param codes
	 */
	public void setPrimaryAnatomicStructureModifierCodes(Code[] codes) {
		updateSequence(Tag.PrimaryAnatomicStructureModifierSequence, codes);
	}

	/**
	 * Gets codes for this primary anatomic structure modifier.
	 * 
	 * See Table 10-8 PS3.3 2006
	 * 
	 * @return
	 */
	public Code[] getPrimaryAnatomicStructureModifierCodes() {
		return Code.toCodes(dcmobj
				.get(Tag.PrimaryAnatomicStructureModifierSequence));
	}

}
