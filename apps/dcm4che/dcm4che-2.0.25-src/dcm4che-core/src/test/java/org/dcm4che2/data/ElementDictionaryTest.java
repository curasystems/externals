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

import junit.framework.TestCase;

public class ElementDictionaryTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ElementDictionaryTest.class);
    }

    public ElementDictionaryTest(String name) {
        super(name);
    }

    public final void testGetDictionary() {
        ElementDictionary dict = ElementDictionary.getDictionary();
        assertNull(dict.getPrivateCreator());
        assertEquals(ElementDictionary.GROUP_LENGTH, dict.nameOf(0x00000000));
        assertEquals("Affected SOP Class UID", dict.nameOf(0x00000002));
        assertEquals(ElementDictionary.GROUP_LENGTH, dict.nameOf(0x00020000));
        assertEquals("File Meta Information Version", dict.nameOf(0x00020001));
        assertEquals("Specific Character Set", dict.nameOf(0x00080005));
        assertEquals(ElementDictionary.PRIVATE_CREATOR, dict.nameOf(0x00090010));
        assertEquals(ElementDictionary.getUnkown(), dict.nameOf(0x00091010));
        assertEquals("Overlay Rows", dict.nameOf(0x60000010));
        assertEquals("Overlay Rows", dict.nameOf(0x60020010));
        assertEquals(ElementDictionary.GROUP_LENGTH, dict.nameOf(0x7FE00000));
        assertEquals("Pixel Data", dict.nameOf(0x7FE00010));
    }

    public final void testGetPrivateDictionary() {
        ElementDictionary dict = ElementDictionary
                .getPrivateDictionary("dcm4che2");
        assertEquals("dcm4che2", dict.getPrivateCreator());
        assertEquals(ElementDictionary.PRIVATE_CREATOR, dict.nameOf(0x00990010));
        assertEquals(ElementDictionary.PRIVATE_CREATOR, dict.nameOf(0x00990011));
        assertEquals(ElementDictionary.PRIVATE_CREATOR, dict.nameOf(0x009900E0));
        assertEquals("Private US", dict.nameOf(0x00991000));
        assertEquals("Private US", dict.nameOf(0x00991100));
        assertEquals("Private UL", dict.nameOf(0x00991010));
        assertEquals("Private UL", dict.nameOf(0x00991110));
        assertEquals("Private OB", dict.nameOf(0x009910E0));
        assertEquals("Private OB", dict.nameOf(0x009911E0));
        assertEquals(ElementDictionary.getUnkown(), dict.nameOf(0x00991111));
    }

    public final void testTagNameOfStdDictionary() {
        ElementDictionary dict = ElementDictionary.getDictionary();
        assertEquals("org.dcm4che2.data.Tag", dict.getTagClassName());
        assertEquals(0x00000002, dict.tagForName("AffectedSOPClassUID"));
        assertEquals(0x00020001, dict.tagForName("FileMetaInformationVersion"));
        assertEquals(0x00080005, dict.tagForName("SpecificCharacterSet"));
        assertEquals(0x60000010, dict.tagForName("OverlayRows"));
        assertEquals(0x7FE00010, dict.tagForName("PixelData"));
    }

    public final void testTagNameOfPrivateDictionary() {
        ElementDictionary dict = ElementDictionary
                .getPrivateDictionary("dcm4che2");
        assertEquals("your.PrivateTag", dict.getTagClassName());
        assertEquals(0x00990000, dict.tagForName("PrivateUS"));
        assertEquals(0x00990010, dict.tagForName("PrivateUL"));
        assertEquals(0x009900E0, dict.tagForName("PrivateOB"));
    }

}
