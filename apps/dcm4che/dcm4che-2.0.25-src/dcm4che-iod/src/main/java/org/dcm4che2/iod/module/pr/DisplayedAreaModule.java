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
 * Bill Wallace, <wayfarer3130@gmail.com>
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

package org.dcm4che2.iod.module.pr;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.module.macro.ImageSOPInstanceReference;

/**
 * C.10.4 IOD for Displayed Area information.
 * @author Bill Wallace <wayfarer3130@gmail.com>
 * @version $Revision$ $Date$
 * @since July 19, 2007
 */
public class DisplayedAreaModule extends Module {

	/** Create a display shutter IOD instance on the given dicom object data. */
	public DisplayedAreaModule(DicomObject dcmobj) {
		super(dcmobj);
	}
	
	public static DisplayedAreaModule[] toDisplayedAreaModules(DicomObject dcmobj) {
		DicomElement sq = dcmobj.get(Tag.DisplayedAreaSelectionSequence);
        if (sq == null || !sq.hasItems()) {
            return null;
        }
        DisplayedAreaModule[] a = new DisplayedAreaModule[sq.countItems()];
        for (int i = 0; i < a.length; i++) {
            a[i] = new DisplayedAreaModule(sq.getDicomObject(i));
        }
        return a;
	}
	
	/** Gets the image sop instance references, null if none (means to apply everywhere). */
	public ImageSOPInstanceReference[] getImageSOPInstanceReferences() {
		return ImageSOPInstanceReference.toImageSOPInstanceReferences(dcmobj.get(Tag.ReferencedImageSequence));
	}
	
    public int[] getDisplayedAreaTopLeftHandCorner() {
    	return dcmobj.getInts(Tag.DisplayedAreaTopLeftHandCorner);
    }
    
    public void setDisplayedAreaTopLeftHandCorner(int[] tlhc) {
    	dcmobj.putInts(Tag.DisplayedAreaTopLeftHandCorner, VR.IS, tlhc);
    }
    
    public int[] getDisplayedAreaBottomRightHandCorner() {
    	return dcmobj.getInts(Tag.DisplayedAreaBottomRightHandCorner);
    }
    
    public void setDisplayedAreaBottomrRightHandCorner(int[] brhc) {
    	dcmobj.putInts(Tag.DisplayedAreaBottomRightHandCorner, VR.IS, brhc);
    }
    
    public String getPresentationSizeMode() {
    	return dcmobj.getString(Tag.PresentationSizeMode);
    }
    
    public void setPresentationSizeMode(String mode) {
    	dcmobj.putString(Tag.PresentationSizeMode, VR.CS, mode);
    }
    
    public float[] getPresentationPixelSpacing() {
    	return dcmobj.getFloats(Tag.PresentationPixelSpacing);
    }
    
    public void setPresentationPixelSpacing(float[] spacing) {
    	dcmobj.putFloats(Tag.PresentationPixelSpacing, VR.DS, spacing);
    }
    
    public int[] getPresentationPixelAspectRatio() {
    	return dcmobj.getInts(Tag.PresentationPixelAspectRatio);
    }
    
    public void setPresentationPixelAspectRatio(int[] aspect) {
    	dcmobj.putInts(Tag.PresentationPixelSpacing, VR.IS, aspect);
    }
    
    public float getPresentationPixelMagnificationRatio() {
    	return dcmobj.getFloat(Tag.PresentationPixelMagnificationRatio);
    }
    
    public void setPresentationPixelMagnificationRatio(float magnify) {
    	dcmobj.putFloat(Tag.PresentationPixelMagnificationRatio, VR.DS, magnify);
    }
}
