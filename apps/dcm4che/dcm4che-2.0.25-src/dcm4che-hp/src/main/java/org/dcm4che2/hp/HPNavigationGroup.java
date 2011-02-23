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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 6735 $ $Date: 2008-08-04 11:43:58 +0200 (Mon, 04 Aug 2008) $
 * @since Oct 23, 2005
 * 
 */
public class HPNavigationGroup {
    private final DicomObject dcmobj;
    private HPDisplaySet navDisplaySet;
    private final List<HPDisplaySet> refDisplaySets;

    public HPNavigationGroup() {
        dcmobj = new BasicDicomObject();
        refDisplaySets = new ArrayList<HPDisplaySet>();
    }

    public HPNavigationGroup(DicomObject dcmobj, List<HPDisplaySet> displaySets) {
        this.dcmobj = dcmobj;
        int[] group = dcmobj.getInts(Tag.ReferenceDisplaySets);
        if (group == null)
            throw new IllegalArgumentException(
                    "Missing (0072,0218) Reference Display Sets");
        if (group.length == 0) {
            throw new IllegalArgumentException(
                    "Empty (0072,0218) Reference Display Sets");           
        }
        int nds = dcmobj.getInt(Tag.NavigationDisplaySet);
        if (nds != 0) {
            try {
                navDisplaySet = displaySets.get(nds - 1);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException(
                        "Navigation Display Set does not exists: "
                                + dcmobj.get(Tag.NavigationDisplaySet));
            }
        } else {
            if (group.length == 1) {
                throw new IllegalArgumentException(
                        "Singular Reference Display Set without Navigation Display Set: "
                                + dcmobj.get(Tag.ReferenceDisplaySets));
            }
        }
        refDisplaySets = new ArrayList<HPDisplaySet>(group.length);
        for (int j = 0; j < group.length; j++) {
            try {
                refDisplaySets.add(displaySets.get(group[j] - 1));
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException(
                        "Reference Display Set does not exists: "
                                + dcmobj.get(Tag.ReferenceDisplaySets));
            }
        }
    }

    /**
     * Returns the <tt>DicomObject</tt> that backs this <tt>HPNavigationGroup</tt>.
     * 
     * Direct modifications of the returned <tt>DicomObject</tt> is strongly
     * discouraged as it may cause inconsistencies in the internal state
     * of this object.
     * 
     * @return the <tt>DicomObject</tt> that backs this <tt>HPNavigationGroup</tt>
     */
    public DicomObject getDicomObject() {
        return dcmobj;
    }

    public final HPDisplaySet getNavigationDisplaySet() {
        return navDisplaySet;
    }

    public final void setNavigationDisplaySet(HPDisplaySet displaySet) {
        if (displaySet == null) {
            dcmobj.remove(Tag.NavigationDisplaySet);
        } else {
            int dsn = displaySet.getDisplaySetNumber();
            if (dsn == 0) {
                throw new IllegalArgumentException("Missing Display Set Number");
            }
            dcmobj.putInt(Tag.NavigationDisplaySet, VR.US, dsn);
        }
        this.navDisplaySet = displaySet;
    }

    public List<HPDisplaySet> getReferenceDisplaySets() {
        return Collections.unmodifiableList(refDisplaySets);
    }

    public void addReferenceDisplaySet(HPDisplaySet displaySet) {
        if (displaySet.getDisplaySetNumber() == 0) {
            throw new IllegalArgumentException("Missing Display Set Number");
        }
        refDisplaySets.add(displaySet);
        updateReferenceDisplaySets();
    }

    public boolean removeReferenceDisplaySet(HPDisplaySet displaySet) {
        if (displaySet == null)
            throw new NullPointerException();
       
        if (!refDisplaySets.remove(displaySet)) {
            return false;            
        }
        updateReferenceDisplaySets();
        return true;
    }
    
    public void updateDicomObject() {
        if (navDisplaySet != null) {
            dcmobj.remove(Tag.NavigationDisplaySet);
        } else {
            dcmobj.putInt(Tag.NavigationDisplaySet, VR.US,
                    navDisplaySet.getDisplaySetNumber());
        }        
        updateReferenceDisplaySets();        
    }
    
    private void updateReferenceDisplaySets() {
        int[] val = new int[refDisplaySets.size()];
        for (int i = 0; i < val.length; i++) {
			val[i] = refDisplaySets.get(i).getDisplaySetNumber();
		}
        dcmobj.putInts(Tag.ReferenceDisplaySets, VR.US, val);
    }

    
    public boolean isValid() {
        return refDisplaySets.size() >= (navDisplaySet != null ? 1 : 2);
    }
}
