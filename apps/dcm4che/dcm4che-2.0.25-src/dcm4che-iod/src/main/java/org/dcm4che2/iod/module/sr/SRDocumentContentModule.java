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
package org.dcm4che2.iod.module.sr;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.module.macro.Code;
import org.dcm4che2.iod.module.macro.SOPInstanceReference;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 11752 $
 * @since 25.07.2006
 */
public class SRDocumentContentModule extends Module {

    public SRDocumentContentModule(DicomObject dcmobj) {
	super(dcmobj);
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
        
    public MeasuredValue getMeasuredValue() {
        DicomObject item = dcmobj.getNestedDicomObject(
        	Tag.MeasuredValueSequence);
        return item != null ? new MeasuredValue(item) : null;
    }

    public void setMeasuredValue(MeasuredValue value) {
        updateSequence(Tag.MeasuredValueSequence, value);
    }
    
    public Code getNumericValueQualifierCode() {
        DicomObject item = dcmobj.getNestedDicomObject(
        	Tag.NumericValueQualifierCodeSequence);
        return item != null ? new Code(item) : null;
    }

    public void setNumericValueQualifierCode(Code code) {
        updateSequence(Tag.NumericValueQualifierCodeSequence, code);
    }
    
    public Code getConceptCode() {
	DicomObject item = dcmobj.getNestedDicomObject(Tag.ConceptCodeSequence);
	return item != null ? new Code(item) : null;
    }
    
    public void setConceptCode(Code code) {
	updateSequence(Tag.ConceptCodeSequence, code);
    }    
    
    public SOPInstanceReference getReferencedSOPInstance() {
	DicomObject item = dcmobj.getNestedDicomObject(Tag.ConceptCodeSequence);
	return item != null ? new SOPInstanceReference(item) : null;
    }
    
    public void setReferencedSOPInstance(SOPInstanceReference ref) {
	updateSequence(Tag.ConceptCodeSequence, ref);
    }
    
    //TODO
    
    public SRDocumentContent[] getContent() {
        return SRDocumentContent.toSRDocumentContent(
        	dcmobj.get(Tag.ContentSequence));
    }

    public void setContent(SRDocumentContent[] codes) {
        updateSequence(Tag.ContentSequence, codes);
    } 
    
    public String getContinuityOfContent() {
        return dcmobj.getString(Tag.ContinuityOfContent);
    }

    public void setContinuityOfContent(String s) {
        dcmobj.putString(Tag.ContinuityOfContent, VR.CS, s);
    }

    public Date getObservationDateTime() {
        return dcmobj.getDate(Tag.ObservationDateTime);
    }
    
    public void setObservationDateTime(Date d) {
        dcmobj.putDate(Tag.ObservationDateTime, VR.DT, d);
    }    

}
