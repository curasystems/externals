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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class VRTest extends TestCase {

    private static final byte[] SHORT_4095_LE = { (byte) 0xff, 0x0f };
    private static final byte[] SHORT_MINUS_4095_LE = { 0x01, (byte) 0xf0 };
    private static final byte[] SHORT_4095_MINUS_4095_LE = { (byte) 0xff, 0x0f,
            0x01, (byte) 0xf0 };
    private static final byte[] INT_4095_LE = { (byte) 0xff, 0x0f, 0, 0 };
    private static final byte[] INT_MINUS_4095_LE = { 0x01, (byte) 0xf0,
            (byte) 0xff, (byte) 0xff };
    private static final byte[] INT_4095_MINUS_4095_LE = { (byte) 0xff, 0x0f,
            0, 0, 0x01, (byte) 0xf0, (byte) 0xff, (byte) 0xff };
    private static final byte[] SHORT_4095_BE = { 0x0f, (byte) 0xff };
    private static final byte[] SHORT_MINUS_4095_BE = { (byte) 0xf0, 0x01 };
    private static final byte[] SHORT_4095_MINUS_4095_BE = { 0x0f, (byte) 0xff,
            (byte) 0xf0, 0x01 };
    private static final byte[] INT_4095_BE = { 0, 0, 0x0f, (byte) 0xff };
    private static final byte[] INT_MINUS_4095_BE = { (byte) 0xff, (byte) 0xff,
            (byte) 0xf0, 0x01 };
    private static final byte[] INT_4095_MINUS_4095_BE = { 0, 0, 0x0f,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xf0, 0x01 };
    private static final byte[] DATE_20030515 = { '2', '0', '0', '3', '0', '5',
            '1', '5' };
    private static final byte[] DATE_2003_05_15 = { '2', '0', '0', '3', '-',
            '0', '5', '-', '1', '5' };
    private static final byte[] TIME_10 = { '1', '0' };
    private static final byte[] TIME_1038 = { '1', '0', '3', '8' };
    private static final byte[] TIME_103845 = { '1', '0', '3', '8', '4', '5' };
    private static final byte[] TIME_103845_234 = { '1', '0', '3', '8', '4',
            '5', '.', '2', '3', '4' };
    private static final byte[] DATETIME_20030515_103845_234 = { '2', '0', '0',
            '3', '0', '5', '1', '5', '1', '0', '3', '8', '4', '5', '.', '2',
            '3', '4' };
    private static final byte[] DS_MAX_DOUBLE = { '1', '.', '7', '9', '7', '6',
            '9', '3', '1', '3', '4', '8', 'E', '3', '0', '8' };
    private static final byte[] DS_SQRT_2 = { '1', '.', '4', '1', '4', '2',
            '1', '3', '5', '6', '2', '3', '7', '3', '0', '9' };

    public static void main(String[] args) {
        junit.textui.TestRunner.run(VRTest.class);
    }

    public VRTest(String arg0) {
        super(arg0);
    }

    private void assertEquals(byte[] expected, byte[] value) {
        assertEquals("byte[].length", expected.length, value.length);
        for (int i = 0; i < value.length; i++) {
            assertEquals("byte[" + i + "]", expected[i], value[i]);
        }
    }

    public final void testVR_DA() {
        Date t = VR.DA.toDate(DATE_20030515);
        assertEqualsDate(2003, 5, 15, t);
        assertEquals(DATE_20030515, VR.DA.toBytes(t));
        assertEqualsDate(2003, 5, 15, VR.DA.toDate(DATE_2003_05_15));
    }

    private void assertEqualsDate(int year, int month, int dayOfMonth, Date t) {
        Calendar c = new GregorianCalendar();
        c.setTime(t);
        assertEquals(year, c.get(Calendar.YEAR));
        assertEquals(month, c.get(Calendar.MONTH)+1);
        assertEquals(dayOfMonth, c.get(Calendar.DAY_OF_MONTH));
    }

    public final void testVR_TM() {
        Date t = VR.TM.toDate(TIME_103845_234);
        assertEqualsTime(10, 38, 45, 234, t);
        assertEquals(TIME_103845_234, VR.TM.toBytes(t));
        assertEqualsTime(10, 0, 0, 0, VR.TM.toDate(TIME_10));
        assertEqualsTime(10, 38, 0, 0, VR.TM.toDate(TIME_1038));
        assertEqualsTime(10, 38, 45, 0, VR.TM.toDate(TIME_103845));
    }

    private void assertEqualsTime(int hourOfDay, int minute, int second,
            int millisecond, Date t) {
        Calendar c = new GregorianCalendar();
        c.setTime(t);
        assertEquals(hourOfDay, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, c.get(Calendar.MINUTE));
        assertEquals(second, c.get(Calendar.SECOND));
        assertEquals(millisecond, c.get(Calendar.MILLISECOND));
    }

    public final void testVR_DT() {
        Date t = VR.DT.toDate(DATETIME_20030515_103845_234);
        assertEqualsDate(2003, 5, 15, t);
        assertEqualsTime(10, 38, 45, 234, t);
        assertEquals(DATETIME_20030515_103845_234, VR.DT.toBytes(t));
    }

    public final void testVR_SL() {
        assertEquals(4095, VR.SL.toInt(INT_4095_LE, false));
        assertEquals(-4095, VR.SL.toInt(INT_MINUS_4095_LE, false));
        int[] is = VR.SL.toInts(INT_4095_MINUS_4095_LE, false);
        assertEquals(2, is.length);
        assertEquals(4095, is[0]);
        assertEquals(-4095, is[1]);
        assertEquals(INT_4095_LE, VR.SL.toBytes(4095, false));
        assertEquals(INT_MINUS_4095_LE, VR.SL.toBytes(-4095, false));
        assertEquals(INT_4095_MINUS_4095_LE, VR.SL.toBytes(is, false));

        assertEquals(4095, VR.SL.toInt(INT_4095_BE, true));
        assertEquals(-4095, VR.SL.toInt(INT_MINUS_4095_BE, true));
        is = VR.SL.toInts(INT_4095_MINUS_4095_BE, true);
        assertEquals(2, is.length);
        assertEquals(4095, is[0]);
        assertEquals(-4095, is[1]);
        assertEquals(INT_4095_BE, VR.SL.toBytes(4095, true));
        assertEquals(INT_MINUS_4095_BE, VR.SL.toBytes(-4095, true));
        assertEquals(INT_4095_MINUS_4095_BE, VR.SL.toBytes(is, true));

        assertEquals("4095", VR.SL.toString(INT_4095_LE, false, null));
        assertEquals("-4095", VR.SL.toString(INT_MINUS_4095_LE, false, null));
        String[] ss = VR.SL.toStrings(INT_4095_MINUS_4095_LE, false, null);
        assertEquals(2, ss.length);
        assertEquals("4095", ss[0]);
        assertEquals("-4095", ss[1]);
        assertEquals(INT_4095_LE, VR.SL.toBytes("4095", false, null));
        assertEquals(INT_MINUS_4095_LE, VR.SL.toBytes("-4095", false, null));
        assertEquals(INT_4095_MINUS_4095_LE, VR.SL.toBytes(ss, false, null));

        assertEquals("4095", VR.SL.toString(INT_4095_BE, true, null));
        assertEquals("-4095", VR.SL.toString(INT_MINUS_4095_BE, true, null));
        ss = VR.SL.toStrings(INT_4095_MINUS_4095_BE, true, null);
        assertEquals(2, ss.length);
        assertEquals("4095", ss[0]);
        assertEquals("-4095", ss[1]);
        assertEquals(INT_4095_BE, VR.SL.toBytes("4095", true, null));
        assertEquals(INT_MINUS_4095_BE, VR.SL.toBytes("-4095", true, null));
        assertEquals(INT_4095_MINUS_4095_BE, VR.SL.toBytes(ss, true, null));
    }

    public final void testVR_SS() {
        assertEquals(4095, VR.SS.toInt(SHORT_4095_LE, false));
        assertEquals(-4095, VR.SS.toInt(SHORT_MINUS_4095_LE, false));
        int[] is = VR.SS.toInts(SHORT_4095_MINUS_4095_LE, false);
        assertEquals(2, is.length);
        assertEquals(4095, is[0]);
        assertEquals(-4095, is[1]);
        assertEquals(SHORT_4095_LE, VR.SS.toBytes(4095, false));
        assertEquals(SHORT_MINUS_4095_LE, VR.SS.toBytes(-4095, false));
        assertEquals(SHORT_4095_MINUS_4095_LE, VR.SS.toBytes(is, false));

        assertEquals(4095, VR.SS.toInt(SHORT_4095_BE, true));
        assertEquals(-4095, VR.SS.toInt(SHORT_MINUS_4095_BE, true));
        is = VR.SS.toInts(SHORT_4095_MINUS_4095_BE, true);
        assertEquals(2, is.length);
        assertEquals(4095, is[0]);
        assertEquals(-4095, is[1]);
        assertEquals(SHORT_4095_BE, VR.SS.toBytes(4095, true));
        assertEquals(SHORT_MINUS_4095_BE, VR.SS.toBytes(-4095, true));
        assertEquals(SHORT_4095_MINUS_4095_BE, VR.SS.toBytes(is, true));

        assertEquals("4095", VR.SS.toString(SHORT_4095_LE, false, null));
        assertEquals("-4095", VR.SS.toString(SHORT_MINUS_4095_LE, false, null));
        String[] ss = VR.SS.toStrings(SHORT_4095_MINUS_4095_LE, false, null);
        assertEquals(2, ss.length);
        assertEquals("4095", ss[0]);
        assertEquals("-4095", ss[1]);
        assertEquals(SHORT_4095_LE, VR.SS.toBytes("4095", false, null));
        assertEquals(SHORT_MINUS_4095_LE, VR.SS.toBytes("-4095", false, null));
        assertEquals(SHORT_4095_MINUS_4095_LE, VR.SS.toBytes(ss, false, null));

        assertEquals("4095", VR.SS.toString(SHORT_4095_BE, true, null));
        assertEquals("-4095", VR.SS.toString(SHORT_MINUS_4095_BE, true, null));
        ss = VR.SS.toStrings(SHORT_4095_MINUS_4095_BE, true, null);
        assertEquals(2, ss.length);
        assertEquals("4095", ss[0]);
        assertEquals("-4095", ss[1]);
        assertEquals(SHORT_4095_BE, VR.SS.toBytes("4095", true, null));
        assertEquals(SHORT_MINUS_4095_BE, VR.SS.toBytes("-4095", true, null));
        assertEquals(SHORT_4095_MINUS_4095_BE, VR.SS.toBytes(ss, true, null));
    }

    public final void testVR_UL() {
        assertEquals(4095, VR.UL.toInt(INT_4095_LE, false));
        assertEquals(-4095, VR.UL.toInt(INT_MINUS_4095_LE, false));
        int[] is = VR.UL.toInts(INT_4095_MINUS_4095_LE, false);
        assertEquals(2, is.length);
        assertEquals(4095, is[0]);
        assertEquals(-4095, is[1]);
        assertEquals(INT_4095_LE, VR.UL.toBytes(4095, false));
        assertEquals(INT_MINUS_4095_LE, VR.UL.toBytes(-4095, false));
        assertEquals(INT_4095_MINUS_4095_LE, VR.UL.toBytes(is, false));

        assertEquals(4095, VR.UL.toInt(INT_4095_BE, true));
        assertEquals(-4095, VR.UL.toInt(INT_MINUS_4095_BE, true));
        is = VR.UL.toInts(INT_4095_MINUS_4095_BE, true);
        assertEquals(2, is.length);
        assertEquals(4095, is[0]);
        assertEquals(-4095, is[1]);
        assertEquals(INT_4095_BE, VR.UL.toBytes(4095, true));
        assertEquals(INT_MINUS_4095_BE, VR.UL.toBytes(-4095, true));
        assertEquals(INT_4095_MINUS_4095_BE, VR.UL.toBytes(is, true));

        assertEquals("4095", VR.UL.toString(INT_4095_LE, false, null));
        assertEquals("4294963201", VR.UL.toString(INT_MINUS_4095_LE, false,
                null));
        String[] ss = VR.UL.toStrings(INT_4095_MINUS_4095_LE, false, null);
        assertEquals(2, ss.length);
        assertEquals("4095", ss[0]);
        assertEquals("4294963201", ss[1]);
        assertEquals(INT_4095_LE, VR.UL.toBytes("4095", false, null));
        assertEquals(INT_MINUS_4095_LE, VR.UL
                .toBytes("4294963201", false, null));
        assertEquals(INT_4095_MINUS_4095_LE, VR.UL.toBytes(ss, false, null));

        assertEquals("4095", VR.UL.toString(INT_4095_BE, true, null));
        assertEquals("4294963201", VR.UL
                .toString(INT_MINUS_4095_BE, true, null));
        ss = VR.UL.toStrings(INT_4095_MINUS_4095_BE, true, null);
        assertEquals(2, ss.length);
        assertEquals("4095", ss[0]);
        assertEquals("4294963201", ss[1]);
        assertEquals(INT_4095_BE, VR.UL.toBytes("4095", true, null));
        assertEquals(INT_MINUS_4095_BE, VR.UL.toBytes("4294963201", true, null));
        assertEquals(INT_4095_MINUS_4095_BE, VR.UL.toBytes(ss, true, null));
    }

    public final void testVR_US() {
        assertEquals(4095, VR.US.toInt(SHORT_4095_LE, false));
        assertEquals(61441, VR.US.toInt(SHORT_MINUS_4095_LE, false));
        int[] is = VR.US.toInts(SHORT_4095_MINUS_4095_LE, false);
        assertEquals(2, is.length);
        assertEquals(4095, is[0]);
        assertEquals(61441, is[1]);
        assertEquals(SHORT_4095_LE, VR.US.toBytes(4095, false));
        assertEquals(SHORT_MINUS_4095_LE, VR.US.toBytes(-4095, false));
        assertEquals(SHORT_4095_MINUS_4095_LE, VR.US.toBytes(is, false));

        assertEquals(4095, VR.US.toInt(SHORT_4095_BE, true));
        assertEquals(61441, VR.US.toInt(SHORT_MINUS_4095_BE, true));
        is = VR.US.toInts(SHORT_4095_MINUS_4095_BE, true);
        assertEquals(2, is.length);
        assertEquals(4095, is[0]);
        assertEquals(61441, is[1]);
        assertEquals(SHORT_4095_BE, VR.US.toBytes(4095, true));
        assertEquals(SHORT_MINUS_4095_BE, VR.US.toBytes(-4095, true));
        assertEquals(SHORT_4095_MINUS_4095_BE, VR.US.toBytes(is, true));

        assertEquals("4095", VR.US.toString(SHORT_4095_LE, false, null));
        assertEquals("61441", VR.US.toString(SHORT_MINUS_4095_LE, false, null));
        String[] ss = VR.US.toStrings(SHORT_4095_MINUS_4095_LE, false, null);
        assertEquals(2, ss.length);
        assertEquals("4095", ss[0]);
        assertEquals("61441", ss[1]);
        assertEquals(SHORT_4095_LE, VR.US.toBytes("4095", false, null));
        assertEquals(SHORT_MINUS_4095_LE, VR.US.toBytes("61441", false, null));
        assertEquals(SHORT_4095_MINUS_4095_LE, VR.SS.toBytes(ss, false, null));

        assertEquals("4095", VR.US.toString(SHORT_4095_BE, true, null));
        assertEquals("61441", VR.US.toString(SHORT_MINUS_4095_BE, true, null));
        ss = VR.US.toStrings(SHORT_4095_MINUS_4095_BE, true, null);
        assertEquals(2, ss.length);
        assertEquals("4095", ss[0]);
        assertEquals("61441", ss[1]);
        assertEquals(SHORT_4095_BE, VR.US.toBytes("4095", true, null));
        assertEquals(SHORT_MINUS_4095_BE, VR.US.toBytes("61441", true, null));
        assertEquals(SHORT_4095_MINUS_4095_BE, VR.SS.toBytes(ss, true, null));
    }

    public final void testVR_DS() {
        assertEquals(DS_MAX_DOUBLE, VR.DS.toBytes(Double.MAX_VALUE, false));
        assertEquals(DS_SQRT_2, VR.DS.toBytes(Math.sqrt(2), false));
    }

}
