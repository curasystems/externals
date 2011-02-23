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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.media.DicomDirReader;

public class DicomDirReaderTest extends TestCase {

    private static File locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(cl.getResource(name).toString().substring(5));
    }
    
	public static void main(String[] args) {
		junit.textui.TestRunner.run(DicomDirReaderTest.class);
	}

	public DicomDirReaderTest(String arg0) {
		super(arg0);
	}

	public void testFindNextSiblingRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		DicomObject rec = r.findFirstRootRecord();
		int count = 0;
		while (rec != null) {
			rec = r.findNextSiblingRecord(rec);
			++count;
		}
		r.close();
		assertEquals(81, count);
	}

	public void testFindFirstMatchingRootRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		DicomObject filter = new BasicDicomObject();
		filter.putString(Tag.DirectoryRecordType, VR.CS, "PATIENT");
		filter.putString(Tag.PatientName, VR.PN, "CHEST*");
		DicomObject rec = r.findFirstMatchingRootRecord(filter, true);
		assertEquals("Chest^Portable", rec.getString(Tag.PatientName));
	}

	public void testFindNextMatchingSiblingRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		DicomObject filter = new BasicDicomObject();
		filter.putString(Tag.DirectoryRecordType, VR.CS, "STUDY");
		filter.putString(Tag.StudyDate, VR.DA, "-19931213");
		DicomObject pat = r.findFirstRootRecord();
		int count = 0;
		while (pat != null) {
			DicomObject sty = r.findFirstMatchingChildRecord(pat, filter, true);
			while (sty != null) {
				++count;
				sty = r.findNextMatchingSiblingRecord(sty, filter, true);
			}
			pat = r.findNextSiblingRecord(pat);
		}
		r.close();
		assertEquals(3, count);
	}

	public void testFindFirstChildRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		DicomObject rec = r.findFirstRootRecord();
		int count = 0;
		while (rec != null) {
			rec = r.findFirstChildRecord(rec);
			++count;
		}
		r.close();
		assertEquals(4, count);
	}

}
