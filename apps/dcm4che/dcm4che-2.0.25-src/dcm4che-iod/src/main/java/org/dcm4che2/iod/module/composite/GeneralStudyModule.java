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

package org.dcm4che2.iod.module.composite;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.module.macro.Code;
import org.dcm4che2.iod.module.macro.PersonIdentification;
import org.dcm4che2.iod.module.macro.SOPInstanceReference;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 720 $ $Date: 2006-11-26 18:40:54 +0100 (Sun, 26 Nov 2006) $
 * @since Jun 9, 2006
 *
 */
public class GeneralStudyModule extends Module {
    
    public GeneralStudyModule(DicomObject dcmobj) {
        super(dcmobj);
    }
    
    public String getStudyInstanceUID() {
        return dcmobj.getString(Tag.StudyInstanceUID);
    }
    
    public void setStudyInstanceUID(String s) {
        dcmobj.putString(Tag.StudyInstanceUID, VR.UI, s);
    }
    
    public Date getStudyDateTime() {
        return dcmobj.getDate(Tag.StudyDate, Tag.StudyTime);
    }
    
    public void setStudyDateTime(Date d) {
        dcmobj.putDate(Tag.StudyDate, VR.DA, d);
        dcmobj.putDate(Tag.StudyTime, VR.TM, d);
    }
    
    public String getReferringPhysiciansName() {
        return dcmobj.getString(Tag.ReferringPhysicianName);
    }
    
    public void setReferringPhysiciansName(String s) {
        dcmobj.putString(Tag.ReferringPhysicianName, VR.PN, s);
    }
    
    public PersonIdentification getReferringPhysicianIdentification() {
        DicomObject item = dcmobj.getNestedDicomObject(Tag.ReferringPhysicianIdentificationSequence);
        return item != null ? new PersonIdentification(item) : null;
    }
    
    public void setReferringPhysicianIdentification(PersonIdentification id) {
        updateSequence(Tag.ReferringPhysicianIdentificationSequence, id);
    }
    
    public String getStudyID() {
        return dcmobj.getString(Tag.StudyID);
    }
    
    public void setStudyID(String s) {
        dcmobj.putString(Tag.StudyID, VR.SH, s);
    }
    
    public String getAccessionNumber() {
        return dcmobj.getString(Tag.AccessionNumber);
    }
    
    public void setAccessionNumber(String s) {
        dcmobj.putString(Tag.AccessionNumber, VR.SH, s);
    }
    
    public String getStudyDescription() {
        return dcmobj.getString(Tag.StudyDescription);
    }
    
    public void setStudyDescription(String s) {
        dcmobj.putString(Tag.StudyDescription, VR.LO, s);
    }
    
    public String[] getPhysiciansOfRecord() {
        return dcmobj.getStrings(Tag.PhysiciansOfRecord);
    }
    
    public void setPhysiciansOfRecord(String[] ss) {
        dcmobj.putStrings(Tag.PhysiciansOfRecord, VR.PN, ss);
    }
    
    public PersonIdentification[] getPhysiciansOfRecordIdentification() {
        return PersonIdentification.toPersonIdentifications(
                dcmobj.get(Tag.PhysiciansOfRecordIdentificationSequence));
    }

    public void setPhysiciansOfRecordIdentification(PersonIdentification[] ids) {
        updateSequence(Tag.PhysiciansOfRecordIdentificationSequence, ids);
    }    
    
    public String[] getNameOfPhysiciansReadingStudy() {
        return dcmobj.getStrings(Tag.NameOfPhysiciansReadingStudy);
    }
    
    public void setNameOfPhysiciansReadingStudy(String[] ss) {
        dcmobj.putStrings(Tag.NameOfPhysiciansReadingStudy, VR.PN, ss);
    }
    
    public PersonIdentification[] getPhysiciansReadingStudyIdentification() {
        return PersonIdentification.toPersonIdentifications(
                dcmobj.get(Tag.PhysiciansReadingStudyIdentificationSequence));
    }

    public void setPhysiciansReadingStudyIdentification(PersonIdentification[] ids) {
        updateSequence(Tag.PhysiciansReadingStudyIdentificationSequence, ids);
    }    
    
    public SOPInstanceReference getReferencedStudySOPInstance() {
        DicomObject item = dcmobj.getNestedDicomObject(Tag.ReferencedStudySequence);
        return item != null ? new SOPInstanceReference(item) : null;
    }
    
    public void setReferencedStudySOPInstance(SOPInstanceReference refSOP) {
        updateSequence(Tag.ReferencedStudySequence, refSOP);
    }
    
    public Code[] getProcedureCodes() {
        return Code.toCodes(dcmobj.get(Tag.ProcedureCodeSequence));
    }

    public void setProcedureCodes(Code[] codes) {
        updateSequence(Tag.ProcedureCodeSequence, codes);
    }    
}
