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
 * @version $Revision: 6735 $ $Date: 2008-08-04 11:43:58 +0200 (Mon, 04 Aug 2008) $
 * @since Jul 30, 2005
 * 
 */
public class HPImageSet {
    private final DicomObject dcmobj;
    private final List<HPSelector> selectors;

    protected HPImageSet(List<HPSelector> selectors, DicomObject dcmobj) {
        this.selectors = selectors;
        this.dcmobj = dcmobj;
    }

    protected HPImageSet() {
        this.selectors = new ArrayList<HPSelector>(4);
        this.dcmobj = new BasicDicomObject();
        DicomObject is = new BasicDicomObject();
        is.putSequence(Tag.ImageSetSelectorSequence);
        DicomElement tbissq = is.putSequence(Tag.TimeBasedImageSetsSequence);
        tbissq.addDicomObject(dcmobj);
    }

    protected HPImageSet(HPImageSet shareSelectors) {
        this.selectors = shareSelectors.selectors;
        this.dcmobj = new BasicDicomObject();
        DicomElement tbissq = shareSelectors.getTimeBasedImageSetsSequence();
        tbissq.addDicomObject(dcmobj);
    }

    /**
     * Returns the <tt>DicomObject</tt> that backs this <tt>HPImageSet</tt>.
     * 
     * Direct modifications of the returned <tt>DicomObject</tt> is strongly
     * discouraged as it may cause inconsistencies in the internal state
     * of this object.
     * 
     * @return the <tt>DicomObject</tt> that backs this <tt>HPImageSet</tt>
     */
    public DicomObject getDicomObject() {
        return dcmobj;
    }

    public boolean contains(DicomObject o, int frame) {
        for (int i = 0, n = selectors.size(); i < n; i++) {
            HPSelector selector = selectors.get(i);
            if (!selector.matches(o, frame))
                return false;
        }
        return true;
    }

    public int getImageSetNumber() {
        return dcmobj.getInt(Tag.ImageSetNumber);
    }

    public void setImageSetNumber(int imageSetNumber) {
        dcmobj.putInt(Tag.ImageSetNumber, VR.US, imageSetNumber);
    }

    public String getImageSetLabel() {
        return dcmobj.getString(Tag.ImageSetLabel);
    }

    public void setImageSetLabel(String imageSetLabel) {
        dcmobj.putString(Tag.ImageSetLabel, VR.LO, imageSetLabel);
    }

    public String getImageSetSelectorCategory() {
        return dcmobj.getString(Tag.ImageSetSelectorCategory);
    }

    public boolean hasRelativeTime() {
        return dcmobj.containsValue(Tag.RelativeTime);
    }

    public RelativeTime getRelativeTime() {
        RelativeTimeUnits units = RelativeTimeUnits.valueOf(dcmobj
                .getString(Tag.RelativeTimeUnits));
        return new RelativeTime(dcmobj.getInts(Tag.RelativeTime), units);
    }

    public void setRelativeTime(RelativeTime relativeTime) {
        dcmobj.putString(Tag.ImageSetSelectorCategory, VR.CS,
                CodeString.RELATIVE_TIME);
        dcmobj.putInts(Tag.RelativeTime, VR.US, relativeTime.getValues());
        dcmobj.putString(Tag.RelativeTimeUnits, VR.CS, relativeTime.getUnits()
                .getCodeString());
    }

    public boolean hasAbstractPriorValue() {
        return dcmobj.containsValue(Tag.AbstractPriorValue);
    }

    public AbstractPriorValue getAbstractPriorValue() {
        return new AbstractPriorValue(dcmobj.getInts(Tag.AbstractPriorValue));
    }

    public void setAbstractPriorValue(AbstractPriorValue abstractPriorValue) {
        dcmobj.putString(Tag.ImageSetSelectorCategory, VR.CS,
                CodeString.ABSTRACT_PRIOR);
        dcmobj.putInts(Tag.AbstractPriorValue, VR.SS, abstractPriorValue
                .getValues());
    }

    public boolean hasAbstractPriorCode() {
        return dcmobj.containsValue(Tag.AbstractPriorCodeSequence);
    }

    public Code getAbstractPriorCode() {
        return new Code(dcmobj
                .getNestedDicomObject(Tag.AbstractPriorCodeSequence));
    }

    public void setAbstractPriorCode(Code code) {
        dcmobj.putString(Tag.ImageSetSelectorCategory, VR.CS,
                CodeString.ABSTRACT_PRIOR);
        dcmobj.putNestedDicomObject(Tag.AbstractPriorCodeSequence, code
                .getDicomObject());
    }

    public DicomElement getImageSetSelectorSequence() {
        return dcmobj.getParent().get(Tag.ImageSetSelectorSequence);
    }

    public DicomElement getTimeBasedImageSetsSequence() {
        return dcmobj.getParent().get(Tag.TimeBasedImageSetsSequence);
    }

    public List<HPSelector> getImageSetSelectors() {
        return Collections.unmodifiableList(selectors);
    }

    public void addImageSetSelector(HPSelector selector) {
        getImageSetSelectorSequence().addDicomObject(selector.getDicomObject());
        selectors.add(selector);
    }
}
