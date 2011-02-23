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

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.iod.module.Module;

/**
 * Defines Tables 10-5 to 10-7 in 2006 - PS3.3
 * 
 * Tables 10-5 through 10-7 describe the attributes for identifying the general
 * region of the patient anatomy examined using coded terminology, as well as
 * the principal structure(s) within that region that is the target of the
 * current SOP Instance. The only difference between the three macros is the
 * Type of the Anatomic Region Sequence (0008,2218) attribute. Table 10-8
 * describe the attributes for the coding of the principal structure only.
 * 
 * The invocation of these macros may specify Baseline or Defined Context IDs
 * for the Anatomic Region Sequence (0008,2218), the Anatomic Region Modifier
 * Sequence (0008,2220), and/or the Primary Anatomic Structure Sequence
 * (0008,2228).
 * 
 * The general region of the body (e.g. the anatomic region, organ, or body
 * cavity being examined) is identified by the Anatomic Region Sequence
 * (0008,2218). Characteristics of the anatomic region being examined, such as
 * sub-region (e.g. medial, lateral, superior, inferior, lobe, quadrant) and
 * laterality (e.g. right, left, both), may be refined by the Anatomic Region
 * Modifier Sequence (0008,2220).
 * 
 * Note: These Attributes allow the specification of the information encoded by
 * the Body Part Examined (0018,0015) in the General Series Module in a more
 * robust, consistent way.
 * 
 * The specific anatomic structures of interest within the image (e.g., a
 * particular artery within the anatomic region) is identified by the Primary
 * Anatomic Structure Sequence (0008,2228). Characteristics of the anatomic
 * structure, such as its location (e.g. subcapsular, peripheral, central),
 * configuration (e.g. distended, contracted), and laterality (e.g. right, left,
 * both), and so on, may be refined by the Primary Anatomic Structure Modifier
 * Sequence (0008,2230)
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class GeneralAnatomy extends Module {

    public GeneralAnatomy(DicomObject dcmobj) {
        super(dcmobj);
    }

    /**
     * Sequence that identifies the anatomic region of interest in this Instance
     * (i.e. external anatomy, surface anatomy, or general region of the body).
     * 
     * @return
     */
    public AnatomicRegionCode getAnatomicRegionCode() {
        DicomObject item = dcmobj.getNestedDicomObject(
                Tag.AnatomicRegionSequence);
        return item != null ? new AnatomicRegionCode(item) : null;
    }

    /**
     * Sequence that identifies the anatomic region of interest in this Instance
     * (i.e. external anatomy, surface anatomy, or general region of the body).
     * 
     * @param codes
     */
    public void setAnatomicRegionCode(AnatomicRegionCode code) {
        updateSequence(Tag.AnatomicRegionSequence, code);
    }

    /**
     * Sequence Sequence of Items that identifies the primary anatomic
     * structure(s) of interest in this Instance.
     * 
     * One or more Items may be included in this Sequence.
     * 
     * @return
     */
    public PrimaryAnatomicStructureCode[] getPrimaryAnatomicStructureCodes() {
        return PrimaryAnatomicStructureCode
            .toPrimaryAnatomicStructureCodes(dcmobj.get(
                    Tag.PrimaryAnatomicStructureSequence));
    }

    /**
     * Sequence Sequence of Items that identifies the primary anatomic
     * structure(s) of interest in this Instance.
     * 
     * One or more Items may be included in this Sequence.
     * 
     * @param codes
     */
    public void setPrimaryAnatomicStructureCodes(
            PrimaryAnatomicStructureCode[] codes) {
        updateSequence(Tag.PrimaryAnatomicStructureSequence, codes);
    }
}
