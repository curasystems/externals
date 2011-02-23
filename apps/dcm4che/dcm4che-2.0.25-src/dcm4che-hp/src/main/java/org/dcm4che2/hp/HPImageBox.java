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

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 6735 $ $Date: 2008-08-04 11:43:58 +0200 (Mon, 04 Aug 2008) $
 * @since Aug 8, 2005
 * 
 */
public class HPImageBox {

    private final DicomObject dcmobj;

    public HPImageBox(DicomObject item, int tot) {
        if (item.getInt(Tag.ImageBoxNumber) != item.getItemPosition())
            throw new IllegalArgumentException(""
                    + item.get(Tag.ImageBoxNumber));
        if (tot > 1) {
            if (!CodeString.TILED
                    .equals(item.getString(Tag.ImageBoxLayoutType)))
                throw new IllegalArgumentException(""
                        + item.get(Tag.ImageBoxLayoutType));
        }
        this.dcmobj = item;
    }

    public HPImageBox() {
        this.dcmobj = new BasicDicomObject();
    }

    /**
     * Returns the <tt>DicomObject</tt> that backs this <tt>HPImageBox</tt>.
     * 
     * Direct modifications of the returned <tt>DicomObject</tt> is strongly
     * discouraged as it may cause inconsistencies in the internal state
     * of this object.
     * 
     * @return the <tt>DicomObject</tt> that backs this <tt>HPImageBox</tt>
     */
    public DicomObject getDicomObject() {
        return dcmobj;
    }

    public int getImageBoxNumber() {
        return dcmobj.getInt(Tag.ImageBoxNumber);
    }

    public void setImageBoxNumber(int value) {
        dcmobj.putInt(Tag.ImageBoxNumber, VR.US, value);
    }

    public double[] getDisplayEnvironmentSpatialPosition() {
        return dcmobj.getDoubles(Tag.DisplayEnvironmentSpatialPosition);
    }

    public void setDisplayEnvironmentSpatialPosition(double[] values) {
        dcmobj.putDoubles(Tag.DisplayEnvironmentSpatialPosition, VR.FD, values);
    }

    public String getImageBoxLayoutType() {
        return dcmobj.getString(Tag.ImageBoxLayoutType);
    }

    public void setImageBoxLayoutType(String type) {
        dcmobj.putString(Tag.ImageBoxLayoutType, VR.CS, type);
    }

    public int getImageBoxTileHorizontalDimension() {
        return dcmobj.getInt(Tag.ImageBoxTileHorizontalDimension);
    }

    public void setImageBoxTileHorizontalDimension(int value) {
        dcmobj.putInt(Tag.ImageBoxTileHorizontalDimension, VR.US, value);
    }

    public int getImageBoxTileVerticalDimension() {
        return dcmobj.getInt(Tag.ImageBoxTileVerticalDimension);
    }

    public void setImageBoxTileVerticalDimension(int value) {
        dcmobj.putInt(Tag.ImageBoxTileVerticalDimension, VR.US, value);
    }

    public String getImageBoxScrollDirection() {
        return dcmobj.getString(Tag.ImageBoxScrollDirection);
    }

    public void setImageBoxScrollDirection(String value) {
        dcmobj.putString(Tag.ImageBoxScrollDirection, VR.CS, value);
    }

    public String getImageBoxSmallScrollType() {
        return dcmobj.getString(Tag.ImageBoxSmallScrollType);
    }

    public void setImageBoxSmallScrollType(String value) {
        dcmobj.putString(Tag.ImageBoxSmallScrollType, VR.CS, value);
    }

    public int getImageBoxSmallScrollAmount() {
        return dcmobj.getInt(Tag.ImageBoxSmallScrollAmount);
    }

    public void setImageBoxSmallScrollAmount(int value) {
        dcmobj.putInt(Tag.ImageBoxSmallScrollAmount, VR.US, value);
    }

    public String getImageBoxLargeScrollType() {
        return dcmobj.getString(Tag.ImageBoxLargeScrollType);
    }

    public void setImageBoxLargeScrollType(String value) {
        dcmobj.putString(Tag.ImageBoxLargeScrollType, VR.CS, value);
    }

    public int getImageBoxOverlapPriority() {
        return dcmobj.getInt(Tag.ImageBoxOverlapPriority);
    }

    public void setImageBoxOverlapPriority(int value) {
        dcmobj.putInt(Tag.ImageBoxOverlapPriority, VR.US, value);
    }

    public int getPreferredPlaybackSequencing() {
        return dcmobj.getInt(Tag.PreferredPlaybackSequencing);
    }

    public void setPreferredPlaybackSequencing(int value) {
        dcmobj.putInt(Tag.PreferredPlaybackSequencing, VR.US, value);
    }

    public int getRecommendedDisplayFrameRate() {
        return dcmobj.getInt(Tag.RecommendedDisplayFrameRate);
    }

    public void setRecommendedDisplayFrameRate(int value) {
        dcmobj.putInt(Tag.RecommendedDisplayFrameRate, VR.IS, value);
    }

    public double getCineRelativeToRealTime() {
        return dcmobj.getDouble(Tag.CineRelativeToRealTime);
    }

    public void setCineRelativeToRealTime(double value) {
        dcmobj.putDouble(Tag.CineRelativeToRealTime, VR.FD, value);
    }

}
