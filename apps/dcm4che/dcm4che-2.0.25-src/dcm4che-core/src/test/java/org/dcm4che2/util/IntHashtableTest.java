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

package org.dcm4che2.util;

import java.util.Iterator;

import junit.framework.TestCase;

public class IntHashtableTest extends TestCase {

	private IntHashtable<Integer> table;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(IntHashtableTest.class);
	}

	public IntHashtableTest(String arg0) {
		super(arg0);
	}

	@Override
        protected void setUp() {
		this.table = new IntHashtable<Integer>();
		for (int i = -10; i < 10; i++) {
			table.put(i, new Integer(i));
		}
	}

	public final void testPut() {
		for (int i = -100; i < 100; i++) {
			table.put(i, new Integer(i));
		}
		assertEquals(200, table.size());
	}

	public final void testGet() {
		for (int i = -10; i < 10; i++) {
			assertEquals(new Integer(i), table.get(i));
		}
		assertNull(table.get(11));
		assertNull(table.get(-11));
	}

	public final void testRemove1() {
		for (int i = -10; i < 10; i++, i++) {
			assertEquals(new Integer(i), table.remove(i));
		}
		assertEquals(10, table.size());
		for (int i = -10; i < 10; i++, i++) {
			assertNull(table.get(i));
		}
		for (int i = -9; i < 10; i++, i++) {
			assertEquals(new Integer(i), table.get(i));
		}
		Iterator it = table.iterator(0, -1);
        for (int i = 1; i < 10; i++, i++) {
            assertEquals(true, it.hasNext());
            assertEquals(new Integer(i), it.next());
        }
        for (int i = -9; i < 0; i++, i++) {
            assertEquals(true, it.hasNext());
            assertEquals(new Integer(i), it.next());
        }
        assertEquals(false, it.hasNext());
		for (int i = -9; i < 10; i++, i++) {
			assertEquals(new Integer(i), table.remove(i));
		}
		assertEquals(true, table.isEmpty());
		assertEquals(0, table.size());
		for (int i = -10; i < 10; i++) {
			assertNull(table.get(i));
		}
	}

    public final void testRemove2() {
        for (int i = -9; i < 10; i++, i++) {
            assertEquals(new Integer(i), table.remove(i));
        }
        assertEquals(10, table.size());
        for (int i = -9; i < 10; i++, i++) {
            assertNull(table.get(i));
        }
        for (int i = -10; i < 10; i++, i++) {
            assertEquals(new Integer(i), table.get(i));
        }
        Iterator it = table.iterator(0, -1);
        for (int i = 0; i < 10; i++, i++) {
            assertEquals(true, it.hasNext());
            assertEquals(new Integer(i), it.next());
        }
        for (int i = -10; i < 0; i++, i++) {
            assertEquals(true, it.hasNext());
            assertEquals(new Integer(i), it.next());
        }
        assertEquals(false, it.hasNext());
        for (int i = -10; i < 10; i++, i++) {
            assertEquals(new Integer(i), table.remove(i));
        }
        assertEquals(true, table.isEmpty());
        assertEquals(0, table.size());
        for (int i = -10; i < 10; i++) {
            assertNull(table.get(i));
        }
    }

	public final void testAccept() {
		table.accept(new IntHashtable.Visitor() {
			public boolean visit(int key, Object value) {
				assertEquals(new Integer(key), value);
				return true;
			}});
	}

	public final void testIterator() {
		doTestIterator(0, -1);
		doTestIterator(1, -2);
		doTestIterator(0, -3);
		doTestIterator(2, -1);
		doTestIterator(9, -10);
		doTestIterator(10, -10);
		doTestIterator(10, -11);
		doTestIterator(0, 1);
		doTestIterator(-2, -1);
		doTestIterator(0, 0);
		doTestIterator(1, 1);
		doTestIterator(-1, -1);
		doTestIterator(10, 10);
	}

	private void doTestIterator(int start, int end) {
		Iterator itr = table.iterator(start, end);
		if (start >= 0) {
			for (int i = start, n = (end >= 0 && end < 10) ? end : 9; i <= n; i++) {
				assertEquals(true, itr.hasNext());
				assertEquals(new Integer(i), itr.next());
			}
		}
		if (end < 0) {
			for (int i = (start > -10 && start < 0) ? start : -10; i <= end; i++) {
				assertEquals(true, itr.hasNext());
				assertEquals(new Integer(i), itr.next());
			}
		}
		assertEquals(false, itr.hasNext());		
	}
}
