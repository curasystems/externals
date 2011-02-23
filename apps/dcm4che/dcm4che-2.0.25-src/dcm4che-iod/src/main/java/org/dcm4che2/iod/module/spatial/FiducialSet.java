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
import org.dcm4che2.iod.module.macro.ImageSOPInstanceReference;

/**
 * This class represents a set of Fiducials.
 * <p>
 * Since there is no Fiducial Set without Fiducials, this class extends
 * {@link org.dcm4che2.iod.module.spatial.Fiducial}.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision: 720 $ $Date: 2006-11-26 18:40:54 +0100 (Sun, 26 Nov 2006) $
 */
public class FiducialSet extends Fiducial {

    public FiducialSet() {
        super(new BasicDicomObject());
    }
    
    public FiducialSet(DicomObject dcmobj) {
        super(dcmobj);
    }
    
    public static FiducialSet[] toFiducialSets(DicomElement sq) {
        if (sq == null || !sq.hasItems()) {
            return null;
        }
        FiducialSet[] a = new FiducialSet[sq.countItems()];
        for (int i = 0; i < a.length; i++) {
            a[i] = new FiducialSet(sq.getDicomObject(i));
        }
        return a;

    }

    /**
     * Identifies a Frame of Reference that may or may not be an image set.
     * 
     * (e.g. an atlas or physical space).
     * 
     * See C.7.4.1.1.1 for further explanation. Required if Referenced Image
     * Sequence (0008,1140) is absMay be present otherwise.
     * <p>
     * Type 1C
     * 
     * @return
     */
    public String getFrameofRerenceUID() {
        return dcmobj.getString(Tag.FrameOfReferenceUID);
    }

    /**
     * Identifies a Frame of Reference that may or may not be an image set.
     * 
     * (e.g. an atlas or physical space).
     * 
     * See C.7.4.1.1.1 for further explanation. Required if Referenced Image
     * Sequence (0008,1140) is absMay be present otherwise.
     * <p>
     * Type 1C
     * 
     * @param ui
     */
    public void setFrameofReferenceUID(String ui) {
        dcmobj.putString(Tag.FrameOfReferenceUID, VR.UI, ui);
    }

    /**
     * Identifies the set of images in which the fiducials are located.
     * 
     * Required if Frame of Reference UID (0020,0052) is absent. May be present
     * otherwise. One or more Items shall be present. All referenced images
     * shall have the same Frame of Reference UID if present in the images.
     * <p>
     * Type 1C
     * 
     * @return
     */
    public ImageSOPInstanceReference[] getReferencedImages() {
        return ImageSOPInstanceReference.toImageSOPInstanceReferences(dcmobj
                .get(Tag.ReferencedImageSequence));
    }

    /**
     * Identifies the set of images in which the fiducials are located.
     * 
     * Required if Frame of Reference UID (0020,0052) is absent. May be present
     * otherwise. One or more Items shall be present. All referenced images
     * shall have the same Frame of Reference UID if present in the images.
     * <p>
     * Type 1C
     * 
     * @param sops
     */
    public void setReferencedImages(ImageSOPInstanceReference[] sops) {
        updateSequence(Tag.ReferencedImageSequence, sops);
    }

    /**
     * A sequence that specifies one or more fiducials, one item per fiducial.
     * <p>
     * Type 1
     * 
     * @return
     */
    public Fiducial[] getFiducials() {
        return Fiducial.toFiducials(dcmobj.get(Tag.FiducialSequence));
    }

    /**
     * A sequence that specifies one or more fiducials, one item per fiducial.
     * <p>
     * Type 1
     * 
     * @param fids
     */
    public void setFiducials(Fiducial[] fids) {
        updateSequence(Tag.FiducialSequence, fids);
    }
}
