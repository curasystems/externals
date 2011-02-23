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
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 6735 $ $Date: 2008-08-04 11:43:58 +0200 (Mon, 04 Aug 2008) $
 * @since Aug 8, 2005
 * 
 */
public class HPDefinition {
    private final DicomObject dcmobj;

    public HPDefinition(DicomObject item) {
        this.dcmobj = item;
    }

    public HPDefinition() {
        this.dcmobj = new BasicDicomObject();
        dcmobj.putSequence(Tag.ProcedureCodeSequence);
        dcmobj.putSequence(Tag.ReasonForRequestedProcedureCodeSequence);
    }

    /**
     * Returns the <tt>DicomObject</tt> that backs this <tt>HPDefinition</tt>.
     * 
     * Direct modifications of the returned <tt>DicomObject</tt> is strongly
     * discouraged as it may cause inconsistencies in the internal state
     * of this object.
     * 
     * @return the <tt>DicomObject</tt> that backs this <tt>HPDefinition</tt>
     */
    public DicomObject getDicomObject() {
        return dcmobj;
    }

    public String getModality() {
        return dcmobj.getString(Tag.Modality);
    }

    public void setModality(String modality) {
        dcmobj.putString(Tag.Modality, VR.CS, modality);
    }

    public String getLaterality() {
        return dcmobj.getString(Tag.Laterality);
    }

    public void setLaterality(String laterality) {
        dcmobj.putString(Tag.Laterality, VR.CS, laterality);
    }

    public Code[] getAnatomicRegionCode() {
        DicomElement sq = dcmobj.get(Tag.AnatomicRegionSequence);
        return sq != null && sq.hasItems() ? Code.toArray(sq) : null;
    }

    public void addAnatomicRegionCodes(Code code) {
        addCode(Tag.AnatomicRegionSequence, code);
    }

    public Code[] getProcedureCodes() {
        DicomElement sq = dcmobj.get(Tag.ProcedureCodeSequence);
        return sq != null && sq.hasItems() ? Code.toArray(sq) : null;
    }

    public void addProcedureCode(Code code) {
        addCode(Tag.ProcedureCodeSequence, code);
    }

    public Code[] getReasonForRequestedProcedureCodes() {
        DicomElement sq = dcmobj
                .get(Tag.ReasonForRequestedProcedureCodeSequence);
        return sq != null && sq.hasItems() ? Code.toArray(sq) : null;
    }

    public void addReasonForRequestedProcedureCode(Code code) {
        addCode(Tag.ReasonForRequestedProcedureCodeSequence, code);
    }

    private void addCode(int tag, Code code) {
        DicomElement sq = dcmobj.get(tag);
        if (sq == null)
            sq = dcmobj.putSequence(tag);

        sq.addDicomObject(code.getDicomObject());
    }

}
