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
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;

public class DicomInputStreamTest extends TestCase {

    private static InputStream locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream(name);
        assert is!=null;
        return new BufferedInputStream(is);
    }

    private static DicomObject load(String fname) throws IOException
    {
        DicomInputStream dis = new DicomInputStream(locateFile(fname));
        return dis.readDicomObject();
    }
    
    public static void main(String[] args) {
		junit.textui.TestRunner.run(DicomInputStreamTest.class);
	}

	public DicomInputStreamTest(String arg0) {
		super(arg0);
	}

	public final void testReadExplicitVRLE() throws IOException {
		DicomObject attrs = load("DICOMDIR");		
		DicomElement attr = attrs.get(0x00041220);
		assertEquals(1203, attr.countItems());
	}

    public final void testReadImplicitVRLE() throws IOException {
        DicomObject attrs = load("misc/view400.dcm");
        assertEquals(37, attrs.size());
    }
    
    public final void testReadRawImplicitVRLE() throws IOException {
		DicomObject attrs = load("OT-PAL-8-face");
        assertEquals(28, attrs.size());
	}
	
}
