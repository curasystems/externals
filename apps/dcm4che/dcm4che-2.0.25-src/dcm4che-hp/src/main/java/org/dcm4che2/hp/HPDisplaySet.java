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

package org.dcm4che2.hp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 12635 $ $Date: 2010-01-18 14:15:50 +0100 (Mon, 18 Jan 2010) $
 * @since Jul 30, 2005
 * 
 */
public class HPDisplaySet {
    private final DicomObject dcmobj;
    private HPImageSet imageSet;
    private final List<HPImageBox> imageBoxes;
    private final List<HPSelector> filters;
    private final List<HPComparator> cmps;

    protected HPDisplaySet(DicomObject dcmobj, HPImageSet imageSet) {
        this.imageSet = imageSet;
        this.dcmobj = dcmobj;
        DicomElement imageBoxesSeq = dcmobj.get(Tag.ImageBoxesSequence);
        if (imageBoxesSeq == null || imageBoxesSeq.isEmpty())
            throw new IllegalArgumentException(
                    "Missing (0072,0300) Image Boxes Sequence");
        int numImageBoxes = imageBoxesSeq.countItems();
        this.imageBoxes = new ArrayList<HPImageBox>(numImageBoxes);
        for (int i = 0; i < numImageBoxes; i++) {
            imageBoxes.add(createHPImageBox(imageBoxesSeq.getDicomObject(i),
                    numImageBoxes));
        }
        DicomElement filterOpSeq = dcmobj.get(Tag.FilterOperationsSequence);
        if (filterOpSeq == null || filterOpSeq.isEmpty()) {
            this.filters = new ArrayList<HPSelector>(0);
        } else {
            int n = filterOpSeq.countItems();
            this.filters = new ArrayList<HPSelector>(n);
            for (int i = 0; i < n; i++) {
                filters.add(HPSelectorFactory
                        .createDisplaySetFilter(filterOpSeq.getDicomObject(i)));
            }
        }
        DicomElement sortingOpSeq = dcmobj.get(Tag.SortingOperationsSequence);
        if (sortingOpSeq == null || sortingOpSeq.isEmpty()) {
            this.cmps = new ArrayList<HPComparator>(0);
        } else {
            int n = sortingOpSeq.countItems();
            this.cmps = new ArrayList<HPComparator>();
            for (int i = 0; i < n; i++) {
                cmps.add(HPComparatorFactory.createHPComparator(sortingOpSeq
                        .getDicomObject(i)));
            }
        }
    }

    public HPDisplaySet() {
        imageBoxes = new ArrayList<HPImageBox>();
        filters = new ArrayList<HPSelector>();
        cmps = new ArrayList<HPComparator>();
        dcmobj = new BasicDicomObject();
        dcmobj.putSequence(Tag.ImageBoxesSequence);
        dcmobj.putSequence(Tag.FilterOperationsSequence);
        dcmobj.putSequence(Tag.SortingOperationsSequence);
    }

    protected HPImageBox createHPImageBox(DicomObject item, int numImageBoxes) {
        return new HPImageBox(item, numImageBoxes);
    }
    
    /**
     * Returns the <tt>DicomObject</tt> that backs this <tt>HPDisplaySet</tt>.
     * 
     * Direct modifications of the returned <tt>DicomObject</tt> is strongly
     * discouraged as it may cause inconsistencies in the internal state
     * of this object.
     * 
     * @return the <tt>DicomObject</tt> that backs this <tt>HPDisplaySet</tt>
     */
    public final DicomObject getDicomObject() {
        return dcmobj;
    }

    public final HPImageSet getImageSet() {
        return imageSet;
    }

    public void setImageSet(HPImageSet imageSet) {
        dcmobj.putInt(Tag.ImageSetNumber, VR.US, imageSet.getImageSetNumber());
        this.imageSet = imageSet;
    }

    public List<HPImageBox> getImageBoxes() {
        return Collections.unmodifiableList(imageBoxes);
    }

    public void addImageBox(HPImageBox imageBox) {
        imageBox.setImageBoxNumber(imageBoxes.size() + 1);
        dcmobj.get(Tag.ImageBoxesSequence).addDicomObject(
                imageBox.getDicomObject());
        imageBoxes.add(imageBox);
    }
    
    public boolean removeImageBox(HPImageBox imageBox) {
        int index = imageBoxes.indexOf(imageBox);
        if (index == -1) {
            return false;
        }
        DicomElement imageBoxesSeq = dcmobj.get(Tag.ImageBoxesSequence);
        imageBoxesSeq.removeDicomObject(index);
        imageBoxes.remove(index);
        for (; index < imageBoxes.size(); ++index) {
            imageBoxes.get(index).setImageBoxNumber(index+1);
        }
        return true;
    }    
    
    public void removeAllImageBoxes() {
        dcmobj.putSequence(Tag.ImageBoxesSequence);
        imageBoxes.clear();
    }    

    public List<HPSelector> getFilterOperations() {
        return Collections.unmodifiableList(filters);
    }

    public void addFilterOperation(HPSelector selector) {
        dcmobj.get(Tag.FilterOperationsSequence).addDicomObject(
                selector.getDicomObject());
        filters.add(selector);
    }

    public boolean removeFilterOperation(HPSelector cmp) {
        int index = filters.indexOf(cmp);
        if (index == -1) {
            return false;
        }
        dcmobj.get(Tag.FilterOperationsSequence).removeDicomObject(index);
        filters.remove(index);
        return true;
    }

    public void removeAllFilterOperations() {
        dcmobj.putSequence(Tag.FilterOperationsSequence);
        filters.clear();
    }

    public List<HPComparator> getSortingOperations() {
        return Collections.unmodifiableList(cmps);
    }

    public void addSortingOperation(HPComparator cmp) {
        dcmobj.get(Tag.SortingOperationsSequence).addDicomObject(
                cmp.getDicomObject());
        cmps.add(cmp);
    }

    public boolean removeSortingOperation(HPComparator cmp) {
        int index = cmps.indexOf(cmp);
        if (index == -1) {
            return false;
        }
        dcmobj.get(Tag.SortingOperationsSequence).removeDicomObject(index);
        cmps.remove(index);
        return true;
    }

    public void removeAllSortingOperations() {
        dcmobj.putSequence(Tag.SortingOperationsSequence);
        cmps.clear();
    }

    
    public boolean contains(DicomObject o, int frame) {
        for (int i = 0, n = filters.size(); i < n; i++) {
            HPSelector selector = filters.get(i);
            if (!selector.matches(o, frame))
                return false;
        }
        return true;
    }

    public int compare(DicomObject o1, int frame1, DicomObject o2, int frame2) {
        int result = 0;
        for (int i = 0, n = cmps.size(); result == 0 && i < n; i++) {
            HPComparator cmp = cmps.get(i);
            result = cmp.compare(o1, frame1, o2, frame2);
        }
        return result;
    }

    public int getDisplaySetNumber() {
        return dcmobj.getInt(Tag.DisplaySetNumber);
    }

    public void setDisplaySetNumber(int displaySetNumber) {
        dcmobj.putInt(Tag.DisplaySetNumber, VR.US, displaySetNumber);
    }

    public int getDisplaySetPresentationGroup() {
        return dcmobj.getInt(Tag.DisplaySetPresentationGroup);
    }

    public void setDisplaySetPresentationGroup(int group) {
        dcmobj.putInt(Tag.DisplaySetPresentationGroup, VR.US, group);
    }

    public String getBlendingOperationType() {
        return dcmobj.getString(Tag.BlendingOperationType);
    }

    public void setBlendingOperationType(String type) {
        dcmobj.putString(Tag.BlendingOperationType, VR.CS, type);
    }

    public String getReformattingOperationType() {
        return dcmobj.getString(Tag.ReformattingOperationType);
    }

    public void setReformattingOperationType(String type) {
        dcmobj.putString(Tag.ReformattingOperationType, VR.CS, type);
    }

    public double getReformattingThickness() {
        return dcmobj.getDouble(Tag.ReformattingThickness);
    }

    public void setReformattingThickness(double thickness) {
        dcmobj.putDouble(Tag.ReformattingThickness, VR.FD, thickness);
    }

    public double getReformattingInterval() {
        return dcmobj.getDouble(Tag.ReformattingInterval);
    }

    public void setReformattingInterval(double interval) {
        dcmobj.putDouble(Tag.ReformattingInterval, VR.FD, interval);
    }

    public String getReformattingOperationInitialViewDirection() {
        return dcmobj.getString(Tag.ReformattingOperationInitialViewDirection);
    }

    public void setReformattingOperationInitialViewDirection(String direction) {
        dcmobj.putString(Tag.ReformattingOperationInitialViewDirection, VR.CS,
                direction);
    }

    public String[] get3DRenderingType() {
        return dcmobj.getStrings(Tag.ThreeDRenderingType);
    }

    public void set3DRenderingType(String[] type) {
        dcmobj.putStrings(Tag.ThreeDRenderingType, VR.CS, type);
    }

    public PatientOrientation getDisplaySetPatientOrientation() {
        String[] orientation = dcmobj
                .getStrings(Tag.DisplaySetPatientOrientation);
        return orientation == null ? null : new PatientOrientation(orientation);
    }

    public void setDisplaySetPatientOrientation(PatientOrientation orientation) {
        dcmobj.putStrings(Tag.DisplaySetPatientOrientation, VR.CS, orientation
                .values());
    }

    public String getVOIType() {
        return dcmobj.getString(Tag.VOIType);
    }

    public void setVOIType(String type) {
        dcmobj.putString(Tag.VOIType, VR.CS, type);
    }

    public String getPseudoColorType() {
        return dcmobj.getString(Tag.PseudoColorType);
    }

    public void setPseudoColorType(String type) {
        dcmobj.putString(Tag.PseudoColorType, VR.CS, type);
    }

    public String getShowGrayscaleInverted() {
        return dcmobj.getString(Tag.ShowGrayscaleInverted);
    }

    public void setShowGrayscaleInverted(String flag) {
        dcmobj.putString(Tag.ShowGrayscaleInverted, VR.CS, flag);
    }

    public String getShowImageTrueSizeFlag() {
        return dcmobj.getString(Tag.ShowImageTrueSizeFlag);
    }

    public void setShowImageTrueSizeFlag(String flag) {
        dcmobj.putString(Tag.ShowImageTrueSizeFlag, VR.CS, flag);
    }

    public String getShowGraphicAnnotationFlag() {
        return dcmobj.getString(Tag.ShowGraphicAnnotationFlag);
    }

    public void setShowGraphicAnnotationFlag(String flag) {
        dcmobj.putString(Tag.ShowGraphicAnnotationFlag, VR.CS, flag);
    }

    public String getShowAcquisitionTechniquesFlag() {
        return dcmobj.getString(Tag.ShowAcquisitionTechniquesFlag);
    }

    public void setShowAcquisitionTechniquesFlag(String flag) {
        dcmobj.putString(Tag.ShowAcquisitionTechniquesFlag, VR.CS, flag);
    }

    public String getDisplaySetPresentationGroupDescription() {
        return dcmobj.getString(Tag.DisplaySetPresentationGroupDescription);
    }

    public void setDisplaySetPresentationGroupDescription(String description) {
        dcmobj.putString(Tag.DisplaySetPresentationGroupDescription, VR.CS,
                description);
    }

}
