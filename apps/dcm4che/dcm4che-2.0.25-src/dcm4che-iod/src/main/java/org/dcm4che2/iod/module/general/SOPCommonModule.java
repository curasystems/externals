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

package org.dcm4che2.iod.module.general;

import java.util.Date;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 12635 $ $Date: 2010-01-18 14:15:50 +0100 (Mon, 18 Jan 2010) $
 * @since Jun 9, 2006
 * 
 */
public class SOPCommonModule extends Module {

    public SOPCommonModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    public SOPCommonModule() {
        super(new BasicDicomObject());
    }

    public String getSOPClassUID() {
        return dcmobj.getString(Tag.SOPClassUID);
    }
    
    public void setSOPClassUID(String uid) {
        dcmobj.putString(Tag.SOPClassUID, VR.UI, uid);
    }
    
    public String getSOPInstanceUID() {
        return dcmobj.getString(Tag.SOPInstanceUID);
    }

    public void setSOPInstanceUID(String uid) {
        dcmobj.putString(Tag.SOPInstanceUID, VR.UI, uid);
    }

    public String[] getSpecificCharacterSet() {
        return dcmobj.getStrings(Tag.SpecificCharacterSet);
    }

    public void setSpecificCharacterSet(String[] ss) {
        dcmobj.putStrings(Tag.SpecificCharacterSet, VR.CS, ss);
    }

    public Date getInstanceCreationDateTime() {
        return dcmobj.getDate(Tag.InstanceCreationDate, Tag.InstanceCreationTime);
    }
    
    public void setInstanceCreationDateTime(Date d) {
        dcmobj.putDate(Tag.InstanceCreationDate, VR.DA, d);
        dcmobj.putDate(Tag.InstanceCreationTime, VR.TM, d);
    }
        
    public String getInstanceCreatorUID() {
        return dcmobj.getString(Tag.InstanceCreatorUID);
    }
    
    public void setInstanceCreatorUID(String s) {
        dcmobj.putString(Tag.InstanceCreatorUID, VR.UI, s);
    }
   
    public String getRelatedGeneralSOPClassUID() {
        return dcmobj.getString(Tag.RelatedGeneralSOPClassUID);
    }
    
    public void setRelatedGeneralSOPClassUID(String s) {
        dcmobj.putString(Tag.RelatedGeneralSOPClassUID, VR.UI, s);
    }
   
    public String getOriginalSpecializedSOPClassUID() {
        return dcmobj.getString(Tag.OriginalSpecializedSOPClassUID);
    }
    
    public void setOriginalSpecializedSOPClassUID(String s) {
        dcmobj.putString(Tag.OriginalSpecializedSOPClassUID, VR.UI, s);
    }
    
    public CodingSchemeIdentification[] getCodingSchemeIdentifications() {
        return CodingSchemeIdentification.toCodingSchemeIdentifications(
                dcmobj.get(Tag.CodingSchemeIdentificationSequence));
    }

    public void setCodingSchemeIdentifications(CodingSchemeIdentification[] ids) {
        updateSequence(Tag.CodingSchemeIdentificationSequence, ids);
    }    
   
    public String getTimezoneOffsetFromUTC() {
        return dcmobj.getString(Tag.TimezoneOffsetFromUTC);
    }
    
    public void setTimezoneOffsetFromUTC(String s) {
        dcmobj.putString(Tag.TimezoneOffsetFromUTC, VR.SH, s);
    }
      
    public ContributingEquipment[] getContributingEquipments() {
        return ContributingEquipment.toContributingEquipments(
                dcmobj.get(Tag.ContributingEquipmentSequence));
    }

    public void setContributingEquipments(ContributingEquipment[] codes) {
        updateSequence(Tag.ContributingEquipmentSequence, codes);
    }    

    /**
     * A number that identifies this composite object instance.
     * <p>
     * Please not that this is an IS DICOM value, which is supposed to be
     * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
     * because:
     * <ul>
     * <li> I have already seen objects, which uses non-numeric values for this
     * identifiers.
     * <li>For identifiers, the non-numeric value may still of some
     * use/information as opposed to e.g. a non-numeric Frame Number..
     * </ul>
     * <p>
     * Type 3
     * 
     * @return
     */
    public String getInstanceNumber() {
        return dcmobj.getString(Tag.InstanceNumber);
    }

    /**
     * A number that identifies this composite object instance.
     * <p>
     * Please not that this is an IS DICOM value, which is supposed to be
     * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
     * because:
     * <ul>
     * <li> I have already seen objects, which uses non-numeric values for this
     * identifiers.
     * <li>For identifiers, the non-numeric value may still of some
     * use/information as opposed to e.g. a non-numeric Frame Number..
     * </ul>
     * <p>
     * Type 3
     * 
     * @param s
     */
    public void setInstanceNumber(String s) {
        dcmobj.putString(Tag.InstanceNumber, VR.IS, s);
    }

    public String getSOPInstanceStatus() {
        return dcmobj.getString(Tag.SOPInstanceStatus);
    }
    
    public void setSOPInstanceStatus(String s) {
        dcmobj.putString(Tag.SOPInstanceStatus, VR.CS, s);
    }
    
    public Date getSOPAuthorizationDateTime() {
        return dcmobj.getDate(Tag.SOPAuthorizationDateTime);
    }
    
    public void setSOPAuthorizationDateTime(Date d) {
        dcmobj.putDate(Tag.SOPAuthorizationDateTime, VR.DT, d);
    }

    public String getSOPAuthorizationComment() {
        return dcmobj.getString(Tag.SOPAuthorizationComment);
    }
    
    public void setSOPAuthorizationComment(String s) {
        dcmobj.putString(Tag.SOPAuthorizationComment, VR.LT, s);
    }

    public String getAuthorizationEquipmentCertificationNumber() {
        return dcmobj.getString(Tag.AuthorizationEquipmentCertificationNumber);
    }
    
    public void setAuthorizationEquipmentCertificationNumber(String s) {
        dcmobj.putString(Tag.AuthorizationEquipmentCertificationNumber, VR.LO, s);
    }

    public MACParameters[] getMACParameters() {
        return MACParameters.toMACParameters(
                dcmobj.get(Tag.MACParametersSequence));
    }

    public void setMACParameters(MACParameters[] mac) {
        updateSequence(Tag.MACParametersSequence, mac);
    }    

    public DigitalSignatures[] getDigitalSignatures() {
        return DigitalSignatures.toDigitalSignatures(
                dcmobj.get(Tag.DigitalSignaturesSequence));
    }

    public void setDigitalSignatures(DigitalSignatures[] signatures) {
        updateSequence(Tag.DigitalSignaturesSequence, signatures);
    }    

    public EncryptedAttributes[] getEncryptedAttributes() {
        return EncryptedAttributes.toEncryptedAttributes(
                dcmobj.get(Tag.EncryptedAttributesSequence));
    }

    public void setEncryptedAttributes(EncryptedAttributes[] attrs) {
        updateSequence(Tag.EncryptedAttributesSequence, attrs);
    }    

    public HL7StructuredDocumentReference[] getHL7StructuredDocumentReferences() {
        return HL7StructuredDocumentReference.toHL7StructuredDocumentReferences(
                dcmobj.get(Tag.HL7StructuredDocumentReferenceSequence));
    }

    public void setHL7StructuredDocumentReferences(HL7StructuredDocumentReference[] hl7srref) {
        updateSequence(Tag.HL7StructuredDocumentReferenceSequence, hl7srref);
    }    

}
