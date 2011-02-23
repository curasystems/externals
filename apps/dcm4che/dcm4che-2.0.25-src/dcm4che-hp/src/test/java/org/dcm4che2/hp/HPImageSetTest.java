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
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.SAXReader;

public class HPImageSetTest extends TestCase {

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
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(HPImageSetTest.class);
    }

    public HPImageSetTest(String name) {
        super(name);
    }
    
    public final void testContains1() throws Exception {
        HangingProtocol hp = new HangingProtocol(loadXML("NeurosurgeryPlan.xml"));
        List list = hp.getImageSets();
        assertEquals(3, list.size());
        HPImageSet is1 = (HPImageSet) list.get(0);
        HPImageSet is2 = (HPImageSet) list.get(1);
        HPImageSet is3 = (HPImageSet) list.get(2);
        DicomObject o = new BasicDicomObject();
        assertEquals(false, is1.contains(o, 0));
        assertEquals(false, is2.contains(o, 0));
        assertEquals(false, is3.contains(o, 0));
        o.putString(Tag.BodyPartExamined, VR.CS, "HEAD");
        assertEquals(false, is1.contains(o, 0));
        assertEquals(false, is2.contains(o, 0));
        assertEquals(false, is3.contains(o, 0));
        o.putString(Tag.Modality, VR.CS, "CT");
        assertEquals(false, is1.contains(o, 0));
        assertEquals(true, is2.contains(o, 0));
        assertEquals(true, is3.contains(o, 0));
        o.putString(Tag.Modality, VR.CS, "MR");
        assertEquals(true, is1.contains(o, 0));
        assertEquals(false, is2.contains(o, 0));
        assertEquals(false, is3.contains(o, 0));
    }
    
    public void testGetImageSetSelectorSequence() throws Exception {
        HangingProtocol hp = new HangingProtocol(loadXML("NeurosurgeryPlan.xml"));
        List list = hp.getImageSets();
        assertEquals(3, list.size());
        HPImageSet is1 = (HPImageSet) list.get(0);
        HPImageSet is2 = (HPImageSet) list.get(1);
        HPImageSet is3 = (HPImageSet) list.get(2);
        DicomElement is1selSeq = is1.getImageSetSelectorSequence();
        assertNotNull(is1selSeq);
        assertEquals(2, is1selSeq.countItems());
        DicomElement is2selSeq = is2.getImageSetSelectorSequence();
        assertNotNull(is2selSeq);
        assertEquals(2, is2selSeq.countItems());
        DicomElement is3selSeq = is3.getImageSetSelectorSequence();
        assertNotNull(is3selSeq);
        assertEquals(is2selSeq, is3selSeq);
    }
    
    
    public void testGetTimeBasedImageSetsSequence() throws Exception {
        HangingProtocol hp = new HangingProtocol(loadXML("NeurosurgeryPlan.xml"));
        List list = hp.getImageSets();
        assertEquals(3, list.size());
        HPImageSet is1 = (HPImageSet) list.get(0);
        HPImageSet is2 = (HPImageSet) list.get(1);
        HPImageSet is3 = (HPImageSet) list.get(2);
        DicomElement tbis1Seq = is1.getTimeBasedImageSetsSequence();
        assertNotNull(tbis1Seq);
        assertEquals(1, tbis1Seq.countItems());
        DicomElement tbis2lSeq = is2.getTimeBasedImageSetsSequence();
        assertNotNull(tbis2lSeq);
        assertEquals(2, tbis2lSeq.countItems());
        DicomElement tbis3Seq = is3.getTimeBasedImageSetsSequence();
        assertNotNull(tbis3Seq);
        assertEquals(tbis2lSeq, tbis3Seq);
    }

}
