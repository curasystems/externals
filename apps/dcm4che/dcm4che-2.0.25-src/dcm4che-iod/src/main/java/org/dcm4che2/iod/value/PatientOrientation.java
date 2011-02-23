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

package org.dcm4che2.iod.value;

/**
 * A Class to represent possible Patient Orientation (0020,0020) values.
 * <p>
 * The Patient Orientation relative to the image plane shall be specified by two
 * values that designate the anatomical direction of the positive row axis (left
 * to right) and the positive column axis (top to bottom). The first entry is
 * the direction of the rows, given by the direction of the last pixel in the
 * first row from the first pixel in that row. The second entry is the direction
 * of the columns, given by the direction of the last pixel in the first column
 * from the first pixel in that column.
 * <p>
 * Anatomical direction shall be designated by the capital letters: A
 * (anterior), P (posterior), R (right), L (left), H (head), F (foot). Each
 * value of the orientation attribute shall contain at least one of these
 * characters. If refinements in the orientation descriptions are to be
 * specified, then they shall be designated by one or two additional letters in
 * each value. Within each value, the letters shall be ordered with the
 * principal orientation designated in the first character.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class PatientOrientation {
	public static final String[] AF = { "A", "F" };

	public static final String[] PF = { "P", "F" };

	public static final String[] LF = { "L", "F" };

	public static final String[] RF = { "R", "F" };

	public static final String[] AH = { "A", "H" };

	public static final String[] PH = { "P", "H" };

	public static final String[] LH = { "L", "H" };

	public static final String[] RH = { "R", "H" };

	public static final String[] FA = { "F", "A" };

	public static final String[] HA = { "H", "A" };

	public static final String[] LA = { "L", "A" };

	public static final String[] RA = { "R", "A" };

	public static final String[] FP = { "F", "P" };

	public static final String[] HP = { "H", "P" };

	public static final String[] LP = { "L", "P" };

	public static final String[] RP = { "R", "P" };

	public static final String[] AL = { "A", "L" };

	public static final String[] PL = { "P", "L" };

	public static final String[] FL = { "F", "L" };

	public static final String[] HL = { "H", "L" };

	public static final String[] AR = { "A", "R" };

	public static final String[] PR = { "P", "R" };

	public static final String[] FR = { "F", "R" };

	public static final String[] HR = { "H", "R" };

}
