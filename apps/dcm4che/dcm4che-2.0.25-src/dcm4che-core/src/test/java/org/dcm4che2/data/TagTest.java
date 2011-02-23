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
package org.dcm4che2.data;

import junit.framework.TestCase;

public class TagTest extends TestCase {
    public void testToTagPathOneTag() {
        int[] tagPath = Tag.toTagPath("0020000D");
        assertEquals(1, tagPath.length);
        assertEquals(0x0020000D, tagPath[0]);
    }

    public void testToTagPathHierarchy() {
        int[] tagPath = Tag.toTagPath("000100020/00100030/00100040");
        assertEquals(5, tagPath.length);
        assertEquals(0x00100020, tagPath[0]);
        assertEquals(0, tagPath[1]);
        assertEquals(0x00100030, tagPath[2]);
        assertEquals(0, tagPath[3]);
        assertEquals(0x00100040, tagPath[4]);
    }

    public void testToTagPathArray() {
        int[] tagPath = Tag.toTagPath("00100020[9]");
        assertEquals(2, tagPath.length);
        assertEquals(0x00100020, tagPath[0]);
        assertEquals(8, tagPath[1]);
    }

    public void testToTagPathTagName() {
        int[] tagPath = Tag.toTagPath("PatientID");
        assertEquals(1, tagPath.length);
        assertEquals(0x00100020, tagPath[0]);
    }

    public void testToTagPathFull() {
        int[] tagPath = Tag.toTagPath("00100020/PatientBirthDate[9]/0020000D");
        assertEquals(5, tagPath.length);
        assertEquals(0x00100020, tagPath[0]);
        assertEquals(0, tagPath[1]);
        assertEquals(0x00100030, tagPath[2]);
        assertEquals(8, tagPath[3]);
        assertEquals(0x0020000D, tagPath[4]);
        // TODO 2007-11-23 gunter.zeilinger Slashes cause a 0 to be emitted
        // unless preceded by an array index or when at the end of a string.
        // Should we add a trailing 0 to be more consistent? (rick.riemer)
    }

    public void testToTagPathNoSlashes() {
        // TODO 2007-11-23 gunter.zeilinger should this behavior remain
        // supported? (rick.riemer)
        int[] tagPath = Tag.toTagPath("00100020[9]0020000E");
        assertEquals(3, tagPath.length);
        assertEquals(0x00100020, tagPath[0]);
        assertEquals(8, tagPath[1]);
        assertEquals(0x0020000E, tagPath[2]);
    }

    public void testIncorrectTagName() {
        try {
            Tag.toTagPath("wrong");
            fail("expected " + IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown Tag Name: wrong", e.getMessage());
        }
    }

    public void testInvalidArrayIndex() {
        try {
            Tag.toTagPath("0020000D[a]");
            fail("expected " + IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("For input string: \"a\"", e.getMessage());
        }
    }
}
