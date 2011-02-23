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

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.module.macro.Code;

/**
 * Item that describes the conditions present during the acquisition of the data
 * of the SOP Instance.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class AcquisitionContext extends Module {

    public AcquisitionContext(DicomObject dcmobj) {
        super(dcmobj);
        // TODO Auto-generated constructor stub
    }

    public static AcquisitionContext[] toAcquisitionContexts(DicomElement sq) {
        if (sq == null || !sq.hasItems()) {
            return null;
        }
        AcquisitionContext[] a = new AcquisitionContext[sq.countItems()];
        for (int i = 0; i < a.length; i++) {
            a[i] = new AcquisitionContext(sq.getDicomObject(i));
        }
        return a;
    }
    
    /**
     * <p>
     * The type of the value encoded in this Item.
     * <p>
     * Defined Terms: TEXT NUMERIC CODE DATE TIME PNAME
     * <p>
     * See Section 10.2.
     * <p>
     * Type 3
     * 
     * @param cs
     */
    public void setValueType(String cs) {
        dcmobj.putString(Tag.ValueType, VR.CS, cs);
    }

    public String getValueType() {
        return dcmobj.getString(Tag.ValueType);
    }

    /**
     * Sequence A concept that constrains the meaning of (i.e. defines the role
     * of) the Observation Value. The “Name” component of a Name/Value pair.
     * <p>
     * This sequence shall contain exactly one item.
     * 
     * @param c
     */
    public void setConceptNameCode(Code c) {
        updateSequence(Tag.ConceptNameCodeSequence, c);
    }

    public Code getConceptNameCode() {
        DicomObject item = dcmobj
                .getNestedDicomObject(Tag.ConceptNameCodeSequence);
        return item != null ? new Code(item) : null;
    }

    /**
     * Numbers References one or more frames in a Multi-frame SOP Instance. The
     * first frame shall be denoted as frame number one.
     * <p>
     * 
     * Required if this SOP Instance is a Multi-frame SOP Instance and the
     * values in this sequence item do not apply to all frames.
     * <p>
     * Type 1C
     * 
     * @param us
     */
    public void setReferencedFrameNumbers(int[] us) {
        dcmobj.putInts(Tag.ReferencedFrameNumbers, VR.US, us);
    }

    public int[] getReferencedFrameNumbers() {
        return dcmobj.getInts(Tag.ReferencedFrameNumbers);
    }

    /**
     * This is the Value component of a Name/Value pair when the Concept implied
     * by Concept Name Code Sequence (0040,A043) is a set of one or more numeric
     * values.
     * <p>
     * Required if the value that Concept Name Code Sequence (0040,A043)
     * requires (implies) is a set of one or more integers or real numbers.
     * Shall not be present otherwise.
     * <p>
     * Type 1C
     * 
     * @param ds
     */
    public void setNumericValue(float ds) {
        dcmobj.putFloat(Tag.NumericValue, VR.DS, ds);
    }

    public float getNumericValue() {
        return dcmobj.getFloat(Tag.NumericValue);
    }

    /**
     * Code Sequence Units of measurement. Only a single Item shall be permitted
     * in this Sequence.
     * <p>
     * Required if Numeric Value (0040,A30A) is sent. Shall not be present
     * otherwise.
     * <p>
     * Type 1C
     * 
     * @param c
     */
    public void setMeasurementUnitsCode(Code c) {
        updateSequence(Tag.MeasurementUnitsCodeSequence, c);
    }

    public Code getMeasurementUnitsCode() {
        DicomObject item = dcmobj
                .getNestedDicomObject(Tag.MeasurementUnitsCodeSequence);
        return item != null ? new Code(item) : null;
    }

    /**
     * This is the Value component of a Name/Value pair when the Concept implied
     * by Concept Name Code Sequence (0040,A043) is a date or time.
     * <p>
     * Note: The purpose or role of the date value could be specified in Concept
     * Name Code Sequence (0040,A043).
     * <p>
     * Required if the value that Concept Name Code Sequence (0040,A043)
     * requires (implies) is a date. Shall not be present otherwise.
     * <p>
     * Type 1C
     * 
     * @return
     */
    public Date getDateTime() {
        return dcmobj.getDate(Tag.Date, Tag.Time);
    }

    public void setDateTime(Date d) {
        dcmobj.putDate(Tag.Date, VR.DA, d);
        dcmobj.putDate(Tag.Time, VR.TM, d);
    }

    /**
     * This is the Value component of a Name/Value pair when the Concept implied
     * by Concept Name Code Sequence (0040,A043) is a Person Name.
     * <p>
     * Note: The role of the person could be specified in Concept Name Code
     * Sequence (0040,A043).
     * <p>
     * Required if the value that Concept Name Code Sequence (0040,A043)
     * irequires (implies) is a person name. Shall not be present otherwise.
     * <p>
     * Type 1C
     * 
     * @return
     */
    public String getPersonName() {
        return dcmobj.getString(Tag.PersonName);
    }

    /**
     * This is the Value component of a Name/Value pair when the Concept implied
     * by Concept Name Code Sequence (0040,A043) is a Person Name.
     * <p>
     * Note: The role of the person could be specified in Concept Name Code
     * Sequence (0040,A043).
     * <p>
     * Required if the value that Concept Name Code Sequence (0040,A043)
     * irequires (implies) is a person name. Shall not be present otherwise.
     * <p>
     * Type 1C
     *  
     * @param s
     */
    public void setPersonName(String s) {
        dcmobj.putString(Tag.PersonName, VR.PN, s);
    }

    /**
     * This is the Value component of a Name/Value pair when the Concept implied
     * by Concept Name Code Sequence (0040,A043) is a Text Observation Value.
     * <p>
     * Required if Date (0040,A121), Time (0040,A122), and Person Name
     * (0040,A123) do not fully describe the concept specified by Concept Name
     * Code Sequence (0040,A043). Shall not be present otherwise.
     * <p>
     * Type 1C
     * 
     * @return
     */
    public String getTextValue() {
        return dcmobj.getString(Tag.TextValue);
    }

    /**
     * This is the Value component of a Name/Value pair when the Concept implied
     * by Concept Name Code Sequence (0040,A043) is a Text Observation Value.
     * <p>
     * Required if Date (0040,A121), Time (0040,A122), and Person Name
     * (0040,A123) do not fully describe the concept specified by Concept Name
     * Code Sequence (0040,A043). Shall not be present otherwise.
     * <p>
     * Type 1C
     * 
     * @param s
     */
    public void setTextValue(String s) {
        dcmobj.putString(Tag.TextValue, VR.UT, s);
    }

    /**
     * Sequence This is the Value component of a Name/Value pair when the
     * Concept implied by Concept Name Code Sequence (0040,A043) is a Coded
     * Value. This sequence shall contain exactly one item.
     * <p>
     * Required if Date (0040,A121), Time (0040,A122), Person Name (0040,A123),
     * Text Value (0040,A160), and the pair of Numeric Value (0040,A30A) and
     * Measurement Units Code Sequence (0040,08EA) are not present.
     * <p>
     * Type 1C
     * 
     * @return
     */
    public Code getConceptCode() {
        DicomObject item = dcmobj.getNestedDicomObject(Tag.ConceptCodeSequence);
        return item != null ? new Code(item) : null;
    }

    /**
     * Sequence This is the Value component of a Name/Value pair when the
     * Concept implied by Concept Name Code Sequence (0040,A043) is a Coded
     * Value. This sequence shall contain exactly one item.
     * <p>
     * Required if Date (0040,A121), Time (0040,A122), Person Name (0040,A123),
     * Text Value (0040,A160), and the pair of Numeric Value (0040,A30A) and
     * Measurement Units Code Sequence (0040,08EA) are not present.
     * <p>
     * Type 1C
     * 
     * 
     * @param code
     */
    public void setConceptCode(Code code) {
        updateSequence(Tag.ConceptCodeSequence, code);
    }

}
