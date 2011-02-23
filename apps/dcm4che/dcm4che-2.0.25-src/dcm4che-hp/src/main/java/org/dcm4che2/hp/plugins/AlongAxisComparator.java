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

package org.dcm4che2.hp.plugins;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.hp.AbstractHPComparator;
import org.dcm4che2.hp.CodeString;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 12635 $ $Date: 2010-01-18 14:15:50 +0100 (Mon, 18 Jan 2010) $
 * @since Aug 7, 2005
 * 
 */
public class AlongAxisComparator
extends AbstractHPComparator
{

    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    private static final int RX = 0;
    private static final int RY = 1;
    private static final int RZ = 2;
    private static final int CX = 3;
    private static final int CY = 4;
    private static final int CZ = 5;

    private final DicomObject sortOp;
    private final int sign;

    public AlongAxisComparator(DicomObject sortOp)
    {
        this.sortOp = sortOp;
        String cs = sortOp.getString(Tag.SortingDirection);
        if (cs == null)
        {
            throw new IllegalArgumentException(
                    "Missing (0072,0604) Sorting Direction");
        }
        try
        {
            this.sign = CodeString.sortingDirectionToSign(cs);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(
                    "Invalid (0072,0604) Sorting Direction: " + cs);
        }
    }

    public AlongAxisComparator(String sortingDirection)
    {
        this.sign = CodeString.sortingDirectionToSign(sortingDirection);
        this.sortOp = new BasicDicomObject();
        sortOp.putString(Tag.SortByCategory, VR.CS, CodeString.ALONG_AXIS);
        sortOp.putString(Tag.SortingDirection, VR.CS, sortingDirection);
    }
    
    public final DicomObject getDicomObject()
    {
        return sortOp;
    }
    
    public int compare(DicomObject o1, int frame1, DicomObject o2, int frame2) {
        try {
            float v1 = dot(o1, frame1);
            float v2 = dot(o2, frame2);
            if (v1 < v2)
                return -sign;
            if (v1 > v2)
                return sign;
        } catch (NullPointerException ignore) {
            // missing image position/orientation information - treat as equal
        } catch (NumberFormatException ignore) {
            // invalid image position/orientation information - treat as equal
        } catch (IndexOutOfBoundsException ignore) {
            // invalid image position/orientation information - treat as equal
        }
        return 0;
    }

    private float dot(DicomObject o, int frame)
    {
        float[] ipp = getImagePositionPatient(o, frame);
        float[] iop = getImageOrientationPatient(o, frame);
        float nx = iop[RY] * iop[CZ] - iop[RZ] * iop[CY];
        float ny = iop[RZ] * iop[CX] - iop[RX] * iop[CZ];
        float nz = iop[RX] * iop[CY] - iop[RY] * iop[CX];
        return ipp[X] * nx + ipp[Y] * ny + ipp[Z] * nz;
    }

    private float[] getImageOrientationPatient(DicomObject o, int frame)
    {
        float[] iop;
        if ((iop = o.getFloats(Tag.ImageOrientationPatient)) != null)
            return iop;
        
        // Check the shared first in the case of image orientation
        int[] tagPath = { 
                Tag.SharedFunctionalGroupsSequence, 0,
                Tag.PlaneOrientationSequence, 0,
                Tag.ImageOrientationPatient };
        if ((iop = o.getFloats(tagPath)) != null)
            return iop;
        
        tagPath[0] = Tag.PerFrameFunctionalGroupsSequence;
        tagPath[1] = frame;
        return o.getFloats(tagPath);
    }

    private float[] getImagePositionPatient(DicomObject o, int frame)
    {
        float[] ipp;
        if ((ipp = o.getFloats(Tag.ImagePositionPatient)) != null)
            return ipp;
        
        // Check the per frame first in the case of image position
        int[] tagPath = { 
                Tag.PerFrameFunctionalGroupsSequence, frame,
                Tag.PlanePositionSequence, 0,
                Tag.ImagePositionPatient };
        if ((ipp = o.getFloats(tagPath)) != null)
            return ipp;
        
        tagPath[0] = Tag.SharedFunctionalGroupsSequence;
        tagPath[1] = 0;
        return o.getFloats(tagPath);
    }

}
