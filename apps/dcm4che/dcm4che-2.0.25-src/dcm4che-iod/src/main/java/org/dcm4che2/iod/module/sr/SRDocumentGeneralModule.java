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

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.macro.Code;
import org.dcm4che2.iod.module.macro.SOPInstanceReference;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 11866 $
 * @since 25.07.2006
 */
public class SRDocumentGeneralModule extends KODocumentModule {

    public SRDocumentGeneralModule(DicomObject dcmobj) {
	super(dcmobj);
    }

    public String getCompletionFlag() {
        return dcmobj.getString(Tag.CompletionFlag);
    }

    public void setCompletionFlag(String s) {
        dcmobj.putString(Tag.CompletionFlag, VR.CS, s);
    }

    public String getCompletionFlagDescription() {
        return dcmobj.getString(Tag.CompletionFlagDescription);
    }

    public void setCompletionFlagDescription(String s) {
        dcmobj.putString(Tag.CompletionFlagDescription, VR.LO, s);
    }

    public String getVerificationFlag() {
        return dcmobj.getString(Tag.VerificationFlag);
    }

    public void setVerificationFlag(String s) {
        dcmobj.putString(Tag.VerificationFlag, VR.CS, s);
    }

    public VerifyingObserver[] getVerifyingObservers() {
        return VerifyingObserver.toVerifyingObservers(
        	dcmobj.get(Tag.VerifyingObserverSequence));
    }
    
    public void setVerifyingObservers(VerifyingObserver[] a) {
        updateSequence(Tag.VerifyingObserverSequence, a);
    }

    public IdentifiedPersonOrDevice getAuthorObserver() {
        DicomObject item = dcmobj.getNestedDicomObject(
        	Tag.AuthorObserverSequence);
        return item != null ? new IdentifiedPersonOrDevice(item) : null;
    }

    public void setAuthorObserver(IdentifiedPersonOrDevice observer) {
        updateSequence(Tag.AuthorObserverSequence, observer);
    }    

    public Participant[] getParticipants() {
        return Participant.toParticipants(dcmobj.get(Tag.ParticipantSequence));
    }

    public void setParticipants(Participant[] participants) {
        updateSequence(Tag.ParticipantSequence, participants);
    }

    public InsitutionNameAndCode getCustodialOrganization() {
        DicomObject item = dcmobj.getNestedDicomObject(
        	Tag.CustodialOrganizationSequence);
        return item != null ? new InsitutionNameAndCode(item) : null;
    }

    public void setCustodialOrganization(InsitutionNameAndCode org) {
        updateSequence(Tag.CustodialOrganizationSequence, org);
    }    

    public HierachicalSOPInstanceReference[] getPredecessorDocuments() {
        return HierachicalSOPInstanceReference.toSOPInstanceReferenceMacros(
        	dcmobj.get(Tag.PredecessorDocumentsSequence));
    }

    public void setPredecessorDocuments(HierachicalSOPInstanceReference[] refs) {
        updateSequence(Tag.PredecessorDocumentsSequence, refs);
    }
    
    public Code[] getPerformedProcedureCodes() {
        return Code.toCodes(dcmobj.get(Tag.PerformedProcedureCodeSequence));
    }

    public void setPerformedProcedureCodes(Code[] codes) {
        updateSequence(Tag.PerformedProcedureCodeSequence, codes);
    } 
    
    public HierachicalSOPInstanceReference[] getPertinentOtherEvidences() {
        return HierachicalSOPInstanceReference.toSOPInstanceReferenceMacros(
        	dcmobj.get(Tag.PertinentOtherEvidenceSequence));
    }

    public void setPertinentOtherEvidences(HierachicalSOPInstanceReference[] refs) {
        updateSequence(Tag.PertinentOtherEvidenceSequence, refs);
    }

    public SOPInstanceReference getEquivalentCDADocument() {
        DicomObject item = dcmobj.getNestedDicomObject(
        	Tag.EquivalentCDADocumentSequence);
        return item != null ? new SOPInstanceReference(item) : null;
    }
    
    public void setEquivalentCDADocument(SOPInstanceReference refSOP) {
        updateSequence(Tag.EquivalentCDADocumentSequence, refSOP);
    }
    
}
