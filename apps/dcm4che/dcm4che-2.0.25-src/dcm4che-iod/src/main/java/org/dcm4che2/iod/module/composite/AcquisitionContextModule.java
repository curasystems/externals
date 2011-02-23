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

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

/**
 * Table C.7.6.14-1 specifies Attributes for description of the conditions
 * present during data acquisition.
 * <p>
 * This Module shall not contain descriptions of conditions that replace those
 * that are already described in specific Modules or Attributes that are also
 * contained within the IOD that contains this Module.
 * <p>
 * Notes:
 * <ol>
 * <li> Each item of the Acquisition Context Sequence (0040,0555) contains one
 * item of the Concept Name Code Sequence (0040,A043) and one of the
 * mutually-exclusive Observation-value Attributes: Concept Code Sequence
 * (0040,A168), the pair of Numeric Value (0040,A30A) and Measurement Units Code
 * Sequence (0040,08EA), Date (0040,A121), Time (0040,A122), Person Name
 * (0040,A123) or Text Value (0040,A160).
 * <li> Acquisition Context includes concepts such as: “pre-contrast”,
 * “inspiration”, “valgus stress”, “post-void”, and date and time of contrast
 * administration.
 * <li> If this SOP Instance is a Multi-frame SOP Instance, each item of the
 * Acquisition Context Sequence (0040,0555) may be configured to describe one
 * frame, all frames, or any specifically enumerated subset set of frames of the
 * Multi-frame SOP Instance.
 * </ol>
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class AcquisitionContextModule extends AcquisitionContext {

    /**
     * 
     */
    public AcquisitionContextModule(DicomObject dcmobj) {
        super(dcmobj);
        // TODO Auto-generated constructor stub
    }

    /**
     * Sequence A sequence of Items that describes the conditions present during
     * the acquisition of the data of the SOP Instance. Zero or more items may
     * be included in this sequence.
     * <p>
     * Type 2
     * 
     * @param ac
     */
    public void setAcquisitionContexts(AcquisitionContext[] ac) {
        updateSequence(Tag.AcquisitionContextSequence, ac);
    }

    /**
     * Sequence A sequence of Items that describes the conditions present during
     * the acquisition of the data of the SOP Instance. Zero or more items may
     * be included in this sequence.
     * <p>
     * Type 2
     * 
     * @return
     */
    public AcquisitionContext[] getAcquisitionContexts() {
        return (AcquisitionContext.toAcquisitionContexts(dcmobj
                .get(Tag.AcquisitionContextSequence)));
    }

    /**
     * Description Free-text description of the image-acquisition context.
     * <p>
     * Type 3
     * 
     * @param st
     */
    public void setAcquisitionContextDescription(String st) {
        dcmobj.putString(Tag.AcquisitionContextDescription, VR.ST, st);
    }

    /**
     * Description Free-text description of the image-acquisition context.
     * <p>
     * Type 3
     * 
     * @return
     */
    public String getAcquisitionContextDescription() {
        return dcmobj.getString(Tag.AcquisitionContextDescription);
    }

}
