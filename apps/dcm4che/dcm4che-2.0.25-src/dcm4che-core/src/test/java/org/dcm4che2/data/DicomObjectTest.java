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

package org.dcm4che2.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.dcm4che2.io.DicomInputStream;

public class DicomObjectTest extends TestCase {

    private static File locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(cl.getResource(name).toString().substring(5));
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public DicomObjectTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(DicomObjectTest.class);
    }

    public void testSerialize() throws IOException, ClassNotFoundException {
        DicomInputStream dis = new DicomInputStream(locateFile("DICOMDIR"));
        DicomObject dicomdir = dis.readDicomObject();
        File ofile = new File("target/test-out/DICOMDIR.dcm.ser");
        ofile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(ofile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(dicomdir);
        oos.close();
        FileInputStream fis = new FileInputStream(ofile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        DicomObject dicomdir2 = (DicomObject) ois.readObject();
        ois.close();
        assertEquals(dicomdir, dicomdir2);
    }

    public void testSerializeElements() throws IOException,
            ClassNotFoundException {
        DicomInputStream dis = new DicomInputStream(locateFile("DICOMDIR"));
        DicomObject dicomdir = dis.readDicomObject();
        File ofile = new File("target/test-out/DICOMDIR.attr.ser");
        ofile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(ofile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        dicomdir.serializeElements(oos);
        oos.close();
        FileInputStream fis = new FileInputStream(ofile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        DicomObject dicomdir2 = (DicomObject) ois.readObject();
        ois.close();
        assertEquals(dicomdir, dicomdir2);
    }

    public void testPut() {
        DicomObject dcm = new BasicDicomObject();
        dcm.putNull(Tag.AcquisitionDate, null);
        assertEquals(VR.DA, dcm.get(Tag.AcquisitionDate).vr());
    }

    public void testPutStringMultiValue() {
        DicomObject dcm = new BasicDicomObject();
        dcm.cachePut(true);
        dcm.putString(Tag.ImageType, VR.CS, "ORIGINAL\\PRIMARY\\AXIAL");
        assertEquals(IMAGE_TYPES.length, dcm.vm(Tag.ImageType));
        assertEquals(IMAGE_TYPES[0], dcm.getString(Tag.ImageType));
        dcm.putStrings(Tag.ImageType, VR.CS, new String[] { "ORIGINAL\\PRIMARY", "AXIAL" });
        String[] val = dcm.getStrings(Tag.ImageType);
        assertEquals(IMAGE_TYPES.length, val.length);
   }

    private static final String[] IMAGE_TYPES = { "ORIGINAL", "PRIMARY",
            "AXIAL" };

    public void testVm() {
        DicomObject dcm = new BasicDicomObject();
        dcm.putStrings(Tag.ImageType, VR.CS, IMAGE_TYPES);
        assertEquals(IMAGE_TYPES.length, dcm.vm(Tag.ImageType));
    }

    public void testGetPrivateWithSpecifiedVR() throws IOException {
        DicomInputStream dis = new DicomInputStream(
                locateFile("VEPRO_BROKER.dcm"));
        DicomObject dcmobj = dis.readDicomObject();
        dis.close();
        DicomElement sq1 = dcmobj.get(
                dcmobj.resolveTag(0x00570010, "VEPRO BROKER 1.0"), VR.SQ);
        assertNotNull(sq1);
        assertEquals(1, sq1.countItems());
        DicomObject item1 = sq1.getDicomObject();
        DicomElement sq2 = item1.get(
                item1.resolveTag(0x00570030, "VEPRO BROKER 1.0 DATA REPLACE"),
                VR.SQ);
        assertNotNull(sq2);
        assertEquals(1, sq2.countItems());
        DicomObject item2 = sq2.getDicomObject();
        assertEquals("NOID1431920080528182346", item2.getString(Tag.PatientID));
        assertEquals("20080528", item1.getString(
                item1.resolveTag(0x00570040, "VEPRO BROKER 1.0 DATA REPLACE"),
                VR.DA));
        assertEquals("182346", item1.getString(
                item1.resolveTag(0x00570041, "VEPRO BROKER 1.0 DATA REPLACE"),
                VR.TM));
        assertEquals("Filter_CT_4", item1.getString(
                item1.resolveTag(0x00570042, "VEPRO BROKER 1.0 DATA REPLACE"),
                VR.SH));
    }
    
    public void testCopyPrivate() {
        DicomObject dcmobj1 = new BasicDicomObject();
        DicomObject dcmobj2 = new BasicDicomObject();
        dcmobj1.putString(
                dcmobj1.resolveTag(0x00990010, "PrivateCreator1", true),
                VR.LT, "PrivateValue1");
        dcmobj1.putString(
                dcmobj1.resolveTag(0x00990010, "PrivateCreator2", true),
                VR.LT, "PrivateValue2");
        dcmobj2.putString(
                dcmobj2.resolveTag(0x00990010, "PrivateCreator2", true),
                VR.LT, "PrivateValue2");
        DicomElement sq2 = dcmobj2.putSequence(
                dcmobj2.resolveTag(0x00990011, "PrivateCreator2", true));
        BasicDicomObject item2 = new BasicDicomObject();
        item2.putString(
                dcmobj1.resolveTag(0x00990010, "PrivateCreator2", true),
                VR.LT, "PrivateValue3");
        sq2.addDicomObject(item2);
        dcmobj2.copyTo(dcmobj1);
        assertEquals("PrivateValue1", dcmobj1.getString(
                dcmobj1.resolveTag(0x00990010, "PrivateCreator1")));
        assertEquals("PrivateValue2", dcmobj1.getString(
                dcmobj1.resolveTag(0x00990010, "PrivateCreator2")));
        DicomObject item1 = dcmobj1.getNestedDicomObject(
                dcmobj1.resolveTag(0x00990011, "PrivateCreator2"));
        assertNotNull(item1);
        assertEquals("PrivateValue3", item1.getString(
                dcmobj1.resolveTag(0x00990010, "PrivateCreator2")));
    }
}
