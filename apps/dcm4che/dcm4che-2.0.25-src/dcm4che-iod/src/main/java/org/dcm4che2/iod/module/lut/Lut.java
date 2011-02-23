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
package org.dcm4che2.iod.module.lut;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.value.PixelRepresentation;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version Revision $Date: 2007-07-24 15:09:43 +0200 (Tue, 24 Jul 2007) $
 * @since 02.07.2006
 */

public class Lut extends Module {

    public Lut(DicomObject dcmobj) {
        super(dcmobj);
    }

    public static Lut[] toLUTs(DicomElement sq) {
        if (sq == null || !sq.hasItems()) {
            return null;
        }
        Lut[] a = new Lut[sq.countItems()];
        for (int i = 0; i < a.length; i++) {
            a[i] = new Lut(sq.getDicomObject(i));
        }
        return a;
    }

    public int[] getLUTDescriptor() {
        return dcmobj.getInts(Tag.LUTDescriptor);
    }

    public void setLUTDescriptor(final int[] ints) {
        dcmobj.putInts(Tag.LUTDescriptor, 
                PixelRepresentation.isSigned(dcmobj.getParent()) 
                        ? VR.SS : VR.US, ints);
    }
    
    /** Returns the number of entries in this table */
    public int getNumberOfEntries() {
    	final int ret = getLUTDescriptor()[0];
    	if( ret==0 ) return 65536;
    	return ret;
    }
    
    /** Get the first stored pixel - this allows a smaller LUT to be used */
    public int getFirstStoredPixel() {
    	return getLUTDescriptor()[1];
    }
    
    /** Get the number of bits stored per pixel.
     * Should return 1 or 2 depending on how many bytes are used per entry. */
    public int getBytesPerEntry() {
    	byte[] lutData = getLUTData();
    	return lutData.length / getNumberOfEntries();
    }
    
    public String getLUTExplanation() {
        return dcmobj.getString(Tag.LUTExplanation);
    }

    public void setLUTExplanation(String lo) {
        dcmobj.putString(Tag.LUTExplanation, VR.LO, lo);
    }

    public byte[] getLUTData() {
        return dcmobj.getBytes(Tag.LUTData);
    }

    public void setLUTData(byte[] ow) {
        dcmobj.putBytes(Tag.LUTData, VR.OW, ow);
    }
}
