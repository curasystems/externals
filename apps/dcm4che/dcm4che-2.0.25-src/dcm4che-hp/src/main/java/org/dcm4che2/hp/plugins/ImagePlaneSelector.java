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
import org.dcm4che2.hp.AbstractHPSelector;
import org.dcm4che2.hp.ImageOrientation;
import org.dcm4che2.hp.ImagePlane;
import org.dcm4che2.hp.PatientOrientation;

public class ImagePlaneSelector extends AbstractHPSelector
{
    public static final float DEF_MIN_COSINE = 0.9f;

    private final DicomObject filterOp;
    private float minCosine = DEF_MIN_COSINE;
    private final ImagePlane[] imagePlanes;

    public ImagePlaneSelector(DicomObject filterOp)
    {
        String vrStr = filterOp.getString(Tag.SelectorAttributeVR);
        if (vrStr == null)
        {
            throw new IllegalArgumentException(
                    "Missing (0072,0050) Selector Attribute VR");
        }
        if (!"CS".equals(vrStr))
        {
            throw new IllegalArgumentException(
                    "(0072,0050) Selector Attribute VR: " + vrStr);
        }
        String[] values = filterOp.getStrings(Tag.SelectorCSValue);
        if (values == null || values.length == 0)
            throw new IllegalArgumentException(
                    "Missing (0072,0062) AbstractHPSelector CS Value");
        this.imagePlanes = new ImagePlane[values.length];
        for (int i = 0; i < values.length; i++)
        {
            imagePlanes[i] = ImagePlane.valueOf(values[i]);
        }
        this.filterOp = filterOp;
    }

    public ImagePlaneSelector(ImagePlane[] imagePlanes)
    {
        this.imagePlanes = imagePlanes.clone();
        this.filterOp = new BasicDicomObject();
        filterOp.putString(Tag.FilterByCategory, VR.CS, "IMAGE_PLANE");
        filterOp.putString(Tag.SelectorAttributeVR, VR.CS, "CS");
        String[] values = new String[imagePlanes.length];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = imagePlanes[i].getCodeString();
        }
        filterOp.putStrings(Tag.SelectorCSValue, VR.CS, values);
    }
    
    public final DicomObject getDicomObject()
    {
        return filterOp;
    }

    public final float getMinCosine()
    {
        return minCosine;
    }

    public final void setMinCosine(float minCosine)
    {
        this.minCosine = minCosine;
    }

    public boolean matches(DicomObject dcmobj, int frame)
    {
        ImagePlane imagePlane;
        float[] floats = dcmobj.getFloats(Tag.ImageOrientationPatient);
        if (floats != null && floats.length == 6)
        {
            ImageOrientation orientation = new ImageOrientation(floats);
            imagePlane = orientation.toImagePlane(minCosine);
        }
        else
        {
            String[] ss = dcmobj.getStrings(Tag.PatientOrientation);
            if (ss != null && ss.length == 2)
            {
                PatientOrientation orientation = new PatientOrientation(ss);
                imagePlane = orientation.toImagePlane();
            }
            else
            {
                return true;
            }
        }
        for (int i = 0; i < imagePlanes.length; i++)
        {
            if (imagePlane == imagePlanes[i])
                return true;
        }
        return false;
    }

}
