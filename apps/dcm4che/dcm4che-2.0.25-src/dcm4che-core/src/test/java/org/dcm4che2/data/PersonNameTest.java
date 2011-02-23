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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 27, 2008
 */

public class PersonNameTest extends TestCase {

    private static final String[] NAMES = {
        "Adams^John Robert Quincy^^Rev.^B.A. M.Div.",
        "Morrison-Jones^Susan^^^Ph.D., Chief Executive Office",
        "Doe^John",
        "Yamada^Tarou=山田^太郎=やまだ^たろう",
        "ﾔﾏﾀﾞ^ﾀﾛｳ=山田^太郎=やまだ^たろう",
        "Hong^Gildong=洪^吉洞=홍^길동",
        "Wang^XiaoDong=王^小東"};

    public void testToString() {
        for (int i = 0; i < NAMES.length; i++) {
            assertEquals(NAMES[i], new PersonName(NAMES[i]).toString());
        }
    }

    public void testComponentGroupString() {
        PersonName pn = new PersonName("Wang^XiaoDong=王^小東");
        assertEquals("Wang^XiaoDong",
                pn.componentGroupString(PersonName.SINGLE_BYTE, true));
        assertEquals("王^小東",
                pn.componentGroupString(PersonName.IDEOGRAPHIC, true));
        assertEquals("",
                pn.componentGroupString(PersonName.PHONETIC, true));
        assertEquals("Wang^XiaoDong^^^",
                pn.componentGroupString(PersonName.SINGLE_BYTE, false));
        assertEquals("王^小東^^^",
                pn.componentGroupString(PersonName.IDEOGRAPHIC, false));
        assertEquals("",
                pn.componentGroupString(PersonName.PHONETIC, false));
    }

}
