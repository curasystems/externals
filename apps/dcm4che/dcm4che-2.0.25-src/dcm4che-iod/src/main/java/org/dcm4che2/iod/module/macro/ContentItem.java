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

import java.util.Date;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 720 $ $Date: 2006-11-26 18:40:54 +0100 (Sun, 26 Nov 2006) $
 * @since Jun 10, 2006
 *
 */
public class ContentItem extends Module {

    public ContentItem(DicomObject dcmobj) {
        super(dcmobj);
    }

    public ContentItem() {
        super(new BasicDicomObject());
    }
    
    public static ContentItem[] toContentItems(DicomElement sq) {
        if (sq == null || !sq.hasItems()) {
            return null;
        }
        ContentItem[] a = new ContentItem[sq.countItems()];
        for (int i = 0; i < a.length; i++) {
            a[i] = new ContentItem(sq.getDicomObject(i));
        }
        return a;
    }    

    public String getValueType() {
        return dcmobj.getString(Tag.ValueType);
    }
    
    public void setValueType(String s) {
        dcmobj.putString(Tag.ValueType, VR.CS, s);
    }
    
    public Code getConceptNameCode() {
        DicomObject item = dcmobj.getNestedDicomObject(Tag.ConceptNameCodeSequence);
        return item != null ? new Code(item) : null;
    }

    public void setConceptNameCode(Code code) {
         updateSequence(Tag.ConceptNameCodeSequence, code);
    }    
    
    public Date getDateTime() {
        return dcmobj.getDate(Tag.DateTime);
    }
    
    public void setDateTime(Date d) {
        dcmobj.putDate(Tag.DateTime, VR.DT, d);
    }
    
    public Date getDate() {
        return dcmobj.getDate(Tag.Date);
    }
    
    public void setDate(Date d) {
        dcmobj.putDate(Tag.Date, VR.DA, d);
    }
    
    public Date getTime() {
        return dcmobj.getDate(Tag.Time);
    }
    
    public void setTime(Date d) {
        dcmobj.putDate(Tag.Time, VR.TM, d);
    }
    
    public String getPersonName() {
        return dcmobj.getString(Tag.PersonName);
    }
    
    public void setPersonName(String s) {
        dcmobj.putString(Tag.PersonName, VR.PN, s);
    }
    
    public String getUID() {
        return dcmobj.getString(Tag.UID);
    }
    
    public void setUID(String s) {
        dcmobj.putString(Tag.UID, VR.UI, s);
    }
    
    public String getTextValue() {
        return dcmobj.getString(Tag.TextValue);
    }
    
    public void setTextValue(String s) {
        dcmobj.putString(Tag.TextValue, VR.UT, s);
    }
        
    public Code getConceptCode() {
        DicomObject item = dcmobj.getNestedDicomObject(Tag.ConceptCodeSequence);
        return item != null ? new Code(item) : null;
    }

    public void setConceptCode(Code code) {
        updateSequence(Tag.ConceptCodeSequence, code);
    }    

    public float getNumericValue() {
        return dcmobj.getFloat(Tag.NumericValue);
    }
    
    public void setNumericValue(float f) {
        dcmobj.putFloat(Tag.NumericValue, VR.DS, f);
    }
            
    public Code getMeasurementUnitsCode() {
        DicomObject item = dcmobj.getNestedDicomObject(
        	Tag.MeasurementUnitsCodeSequence);
        return item != null ? new Code(item) : null;
    }

    public void setMeasurementUnitsCode(Code code) {
        updateSequence(Tag.MeasurementUnitsCodeSequence, code);
    }
}
