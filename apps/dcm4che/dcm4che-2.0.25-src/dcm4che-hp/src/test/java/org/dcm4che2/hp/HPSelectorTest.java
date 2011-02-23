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
package org.dcm4che2.hp;

import java.io.File;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;

import junit.framework.TestCase;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 * @version $Revision$ $Date$
 * @since Jul 22, 2009
 */
public class HPSelectorTest extends TestCase {

    private DicomObject dcmobj;

    private static File locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(cl.getResource(name).toString().substring(5));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HPSelectorTest.class);
    }

    public HPSelectorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        DicomInputStream in = new DicomInputStream(
                locateFile("GEMS_PARM_01.dcm"));
        try {
            dcmobj = in.readDicomObject();
        } finally {
            in.close();
        }
    }

    public void testImageTypeDisplaySetFilter() {
        HPSelector sel = HPSelectorFactory.createAttributeValueSelector(
                null, Tag.ImageType, 3, VR.CS, new String[]{"AXIAL"},
                FilterOp.MEMBER_OF);
        assertEquals(true, sel.matches(dcmobj, 0));
    }

    public void testPrivateSHDisplaySetFilter() {
        HPSelector sel = HPSelectorFactory.createAttributeValueSelector(
                "GEMS_PARM_01", 0x00430027, 0, VR.SH, new String[]{"/1.0:1"},
                FilterOp.MEMBER_OF);
        assertEquals(true, sel.matches(dcmobj, 0));
    }

    public void testPrivateSSDisplaySetFilter() {
        HPSelector sel = HPSelectorFactory.createAttributeValueSelector(
                "GEMS_PARM_01", 0x00430012, 1, VR.SS, new int[]{ 14 },
                FilterOp.MEMBER_OF);
        assertEquals(true, sel.matches(dcmobj, 0));
    }

    public void testPrivateUSDisplaySetFilter() {
        HPSelector sel = HPSelectorFactory.createAttributeValueSelector(
                "GEMS_PARM_01", 0x00430026, 2, VR.US, new int[]{ 1 },
                FilterOp.MEMBER_OF);
        assertEquals(true, sel.matches(dcmobj, 0));
    }

    public void testPrivateSLDisplaySetFilter() {
        HPSelector sel = HPSelectorFactory.createAttributeValueSelector(
                "GEMS_PARM_01", 0x0043001A, 0, VR.SL, new int[]{ 7 },
                FilterOp.MEMBER_OF);
        assertEquals(true, sel.matches(dcmobj, 0));
    }

    public void testPrivateDSDisplaySetFilter() {
        HPSelector sel = HPSelectorFactory.createAttributeValueSelector(
                "GEMS_PARM_01", 0x00430018, 2, VR.DS, new float[]{ 1f },
                FilterOp.GREATER_THAN);
        assertEquals(true, sel.matches(dcmobj, 0));
    }

    public void testPrivateFLDisplaySetFilter() {
        HPSelector sel = HPSelectorFactory.createAttributeValueSelector(
                "GEMS_PARM_01", 0x00430040, 0, VR.FL, new float[]{ 178f, 179f },
                FilterOp.RANGE_INCL);
        assertEquals(true, sel.matches(dcmobj, 0));
    }

}
