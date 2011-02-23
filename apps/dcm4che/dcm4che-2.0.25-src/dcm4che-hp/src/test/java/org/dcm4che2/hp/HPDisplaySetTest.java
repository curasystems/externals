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

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.SAXReader;

public class HPDisplaySetTest extends TestCase {

    private static final String CORONAL = 
            "1.000000\\0.000000\\0.000000\\0.000000\\0.000000\\-1.000000";
    private static final String SAGITAL = 
            "0.000000\\-1.000000\\0.000000\\0.000000\\0.000000\\-1.000000";
    private static final String TRANSVERSE =
            "1.000000\\0.000000\\0.000000\\0.000000\\1.000000\\0.000000";

    private final DicomObject CT_CORONAL = 
            image("ORIGINAL\\PRIMARY\\LOCALIZER", "CT", "HEAD", 
                    "-248.187592\\0.000000\\30.000000", CORONAL);
    private final DicomObject CT_SAGITAL = 
            image("ORIGINAL\\PRIMARY\\LOCALIZER", "CT", "HEAD", 
                    "0.000000\\248.187592\\30.000000", SAGITAL);
    private final DicomObject CT_TRANSVERSE1 = 
            image("ORIGINAL\\PRIMARY\\AXIAL", "CT", "HEAD", 
                    "-158.135818\\-179.035812\\-59.200001", TRANSVERSE);
    private final DicomObject CT_TRANSVERSE2 =
            image("ORIGINAL\\PRIMARY\\AXIAL", "CT", "HEAD", 
                    "-158.135818\\-179.035812\\-29.200001", TRANSVERSE);
    private final DicomObject MR_TRANSVERSE1 =
            image("ORIGINAL\\PRIMARY", "MR", "HEAD", 
                    "-120.000000\\-116.699997\\-19.799999", TRANSVERSE);
    private final DicomObject MR_TRANSVERSE2 = 
            image("ORIGINAL\\PRIMARY", "MR", "HEAD", 
                    "-120.000000\\-116.699997\\-5.800000", TRANSVERSE);


    private static File locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(cl.getResource(name).toString().substring(5));
    }


    private static DicomObject loadXML(String fname)
    throws Exception
    {
        SAXReader r = new SAXReader(locateFile(fname));
        return r.readDicomObject();
    }
    
    private static DicomObject image(String type, String modality, String bodyPart,
            String position, String orientation) {
        DicomObject o = new BasicDicomObject();
        o.putString(Tag.ImageType, VR.CS, type);
        o.putString(Tag.Modality, VR.CS, modality);
        o.putString(Tag.BodyPartExamined, VR.CS, bodyPart);
        o.putString(Tag.ImagePositionPatient, VR.DS, position);
        o.putString(Tag.ImageOrientationPatient, VR.DS, orientation);
        return o;
    }

    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(HPDisplaySetTest.class);
    }

    public HPDisplaySetTest(String name) {
        super(name);
    }

    public final void testNeurosurgeryPlan() throws Exception {
        HangingProtocol neurosurgeryPlan = 
                new HangingProtocol(loadXML("NeurosurgeryPlan.xml"));
        
        assertEquals(4, neurosurgeryPlan.getNumberOfPresentationGroups());
        List ctOnlyDisplay = 
                neurosurgeryPlan.getDisplaySetsOfPresentationGroup(1);
        assertEquals(5, ctOnlyDisplay.size());
        List mrOnlyDisplay = 
            neurosurgeryPlan.getDisplaySetsOfPresentationGroup(2);
        assertEquals(5, mrOnlyDisplay.size());
        List mrctCombined = 
            neurosurgeryPlan.getDisplaySetsOfPresentationGroup(3);
        assertEquals(6, mrctCombined.size());
        List ctNewctOldCombined = 
            neurosurgeryPlan.getDisplaySetsOfPresentationGroup(4);
        assertEquals(6, ctNewctOldCombined.size());
        
        HPDisplaySet ds5 = (HPDisplaySet) ctOnlyDisplay.get(4);
        HPImageSet is2 = ds5.getImageSet();
        assertEquals(true, is2.contains(CT_CORONAL, 0));
        assertEquals(true, is2.contains(CT_SAGITAL, 0));
        assertEquals(true, is2.contains(CT_TRANSVERSE1, 0));
        assertEquals(true, is2.contains(CT_TRANSVERSE2, 0));
        assertEquals(false, is2.contains(MR_TRANSVERSE1, 0));
        assertEquals(false, is2.contains(MR_TRANSVERSE2, 0));
        assertEquals(false, ds5.contains(CT_CORONAL, 0));
        assertEquals(false, ds5.contains(CT_SAGITAL, 0));
        assertEquals(true, ds5.contains(CT_TRANSVERSE1, 0));
        assertEquals(true, ds5.contains(CT_TRANSVERSE2, 0));
        assertEquals(true, ds5.compare(CT_TRANSVERSE1, 1, CT_TRANSVERSE2, 1) < 0);

        HPDisplaySet ds10 = (HPDisplaySet) mrOnlyDisplay.get(4);
        HPImageSet is1 = ds10.getImageSet();
        assertEquals(false, is1.contains(CT_CORONAL, 0));
        assertEquals(false, is1.contains(CT_SAGITAL, 0));
        assertEquals(false, is1.contains(CT_TRANSVERSE1, 0));
        assertEquals(false, is1.contains(CT_TRANSVERSE2, 0));
        assertEquals(true, is1.contains(MR_TRANSVERSE1, 0));
        assertEquals(true, is1.contains(MR_TRANSVERSE2, 0));
        assertEquals(true, ds10.contains(MR_TRANSVERSE1, 0));
        assertEquals(true, ds10.contains(MR_TRANSVERSE2, 0));
        assertEquals(true, ds10.compare(MR_TRANSVERSE1, 1, MR_TRANSVERSE2, 1) < 0);
        
        List filterOps = ds10.getFilterOperations();
        assertEquals(1, filterOps.size());
        HPSelector filterOp = (HPSelector) filterOps.get(0);
        assertEquals("IMAGE_PLANE", filterOp.getFilterByCategory());
        
        List sortingOps = ds10.getSortingOperations();
        assertEquals(1, sortingOps.size());
        HPComparator sortingOp = (HPComparator) sortingOps.get(0);
        assertEquals("ALONG_AXIS", sortingOp.getSortByCategory());
     }


}
