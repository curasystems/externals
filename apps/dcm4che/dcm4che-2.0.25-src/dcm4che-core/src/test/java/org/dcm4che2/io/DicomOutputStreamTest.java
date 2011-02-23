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

package org.dcm4che2.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.media.FileMetaInformation;

public class DicomOutputStreamTest extends TestCase {

    private static InputStream locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream(name);
        assert is!=null;
        return new BufferedInputStream(is);
    }

    private static DicomObject load(String fname) throws IOException
    {
        DicomInputStream dis = new DicomInputStream(locateFile(fname));
        try {
            return dis.readDicomObject();
        } finally {
            dis.close();
        }
    }
    
	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	
	public DicomOutputStreamTest(String testName) {
		super(testName);
	}

    public static Test suite() {
        return new TestSuite( DicomOutputStreamTest.class );
    }

    public void testWriteDICOMDIR() throws IOException {
        DicomObject attrs = load("DICOMDIR");
        File ofile = new File("target/test-out/DICOMDIR");
        ofile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(ofile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DicomOutputStream dos = new DicomOutputStream(bos);
        dos.setExplicitItemLength(true);
        dos.setExplicitSequenceLength(true);
        dos.writeDicomFile(attrs);
        dos.close();
        assertEquals(locateFile("DICOMDIR"), ofile);
    }

    public void testWriteDatasetImplicitVRLE() throws IOException {
        DicomObject attrs = load("sr/511/sr_511_ct.dcm");
        File ofile = new File("target/test-out/sr_511_ct_impl_vr_le.dcm");
        ofile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(ofile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DicomOutputStream dos = new DicomOutputStream(bos);
        dos.setExplicitSequenceLengthIfZero(false);
        dos.writeDataset(attrs, TransferSyntax.ImplicitVRLittleEndian);
        dos.close();
        assertEquals(locateFile("sr/511/sr_511_ct.dcm"), ofile);
    }

    public void testWriteDatasetExplicitVRLE() throws IOException {
        DicomObject attrs = load("sr/511/sr_511_ct.dcm");
        File ofile = new File("target/test-out/sr_511_ct_expl_vr_le.dcm");
        ofile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(ofile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DicomOutputStream dos = new DicomOutputStream(bos);
        dos.writeDataset(attrs, TransferSyntax.ExplicitVRLittleEndian);
        dos.close();
        assertEquals(locateFile("misc/sr_511_ct_expl_vr_le.dcm"), ofile);
    }

    public void testWriteFile() throws IOException {
        DicomObject attrs = load("sr/511/sr_511_ct.dcm");
        File ofile = new File("target/test-out/sr_511_ct_file.dcm");
        ofile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(ofile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DicomOutputStream dos = new DicomOutputStream(bos);
        new FileMetaInformation(attrs).init();
        dos.writeDicomFile(attrs);
        dos.close();
        assertEquals(locateFile("misc/sr_511_ct_file.dcm"), ofile);
    }

    public void testWriteDeflatedFileWithoutPreamble() throws IOException {
        DicomObject attrs = load("sr/511/sr_511_ct.dcm");
        File ofile = new File("target/test-out/sr_511_ct_deflated.dcm");
        ofile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(ofile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DicomOutputStream dos = new DicomOutputStream(bos);
        attrs.putString(Tag.TransferSyntaxUID, VR.UI,
                UID.DeflatedExplicitVRLittleEndian);
        dos.setPreamble(null);
        dos.writeDicomFile(attrs);
        dos.close();
        assertEquals(locateFile("misc/sr_511_ct_deflated.dcm"), ofile);
    }

    private void assertEquals(InputStream expected, File actual) throws IOException {
        assertEquals(loadBytes(expected), loadBytes(actual));
    }

    private void assertEquals(byte[] expected, byte[] actual) {
        for (int i = 0, n = Math.min(expected.length, actual.length); i < n; i++) {
             assertEquals("byte at offset " + i, expected[i], actual[i]);
        }
        assertEquals("file length", expected.length, actual.length);
    }

    private byte[] loadBytes(File f) throws IOException {
    	return loadBytes(new FileInputStream(f));
    }
    
    private byte[] loadBytes(InputStream in) throws IOException {
        int remain = in.available();
        byte[] b = new byte[remain];
        try {
            while (remain > 0) {
                remain -= in.read(b, b.length - remain, remain);
            }
        } finally {
            in.close();
        }
        return b;
    }
}
