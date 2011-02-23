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

package org.dcm4che2.hp;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 567 $ $Date: 2006-05-15 12:16:32 +0200 (Mon, 15 May 2006) $
 * @since Oct 22, 2005
 *
 */
public class CodeString
{
    public static final String YES = "YES";
    public static final String NO = "NO";

    public static final String MATCH = "MATCH";
    public static final String NO_MATCH = "NO_MATCH";
    
    public static final String PRESENT = "PRESENT";
    public static final String NOT_PRESENT = "NOT_PRESENT";
    
    public static final String ABSTRACT_PRIOR = "ABSTRACT_PRIOR";
    public static final String RELATIVE_TIME = "RELATIVE_TIME";
    
    public static final String INCREASING = "INCREASING";
    public static final String DECREASING = "DECREASING";
    
    public static final String ALONG_AXIS = "ALONG_AXIS";
    public static final String BY_ACQ_TIME = "BY_ACQ_TIME";

    public static final String MAINTAIN_LAYOUT = "MAINTAIN_LAYOUT";
    public static final String ADAPT_LAYOUT = "ADAPT_LAYOUT";
    
    public static final String MANUFACTURER = "MANUFACTURER";
    public static final String SITE = "SITE";
    public static final String SINGLE_USER = "SINGLE_USER";
    public static final String USER_GROUP = "USER_GROUP";

    public static final String COLOR = "COLOR";

    public static final String MPR = "MPR";
    public static final String _3D_RENDERING = "3D_RENDERING";
    public static final String SLAB = "SLAB";

    public static final String SAGITTAL = "SAGITTAL";
    public static final String TRANSVERSE = "TRANSVERSE";
    public static final String CORONAL = "CORONAL";
    public static final String OBLIQUE = "OBLIQUE";

    public static final String LUNG = "LUNG";
    public static final String MEDIASTINUM = "MEDIASTINUM";
    public static final String ABDO_PELVIS = "ABDO_PELVIS";
    public static final String LIVER = "LIVER";
    public static final String SOFT_TISSUE = "SOFT_TISSUE";
    public static final String BONE = "BONE";
    public static final String BRAIN = "BRAIN";
    public static final String POST_FOSSA = "POST_FOSSA";

    public static final String BLACK_BODY = "BLACK_BODY";
    public static final String HOT_IRON = "HOT_IRON";
    public static final String DEFAULT = "DEFAULT";

    public static final String TILED = "TILED";
    public static final String STACK = "STACK";
    public static final String CINE = "CINE";
    public static final String PROCESSED = "PROCESSED";
    public static final String SINGLE = "SINGLE";
    
    public static final String VERTICAL = "VERTICAL";
    public static final String HORIZONTAL = "HORIZONTAL";
    
    public static final String PAGE = "PAGE";
    public static final String ROW_COLUMN = "ROW_COLUMN";
    public static final String IMAGE = "IMAGE";
    
    public static int sortingDirectionToSign(String cs)
    {
        if (cs.equals(INCREASING))
            return 1;
        if (cs.equals(DECREASING))
            return -1;
        throw new IllegalArgumentException(
            "Invalid (0072,0604) Sorting Direction: " + cs);
    }

}
