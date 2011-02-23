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

package org.dcm4che2.audit.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 5518 $ $Date: 2007-11-23 13:23:46 +0100 (Fri, 23 Nov 2007) $
 * @since Mar 1, 2007
 */
public class InstanceSorter {
    
    private LinkedHashMap<String,
            LinkedHashMap<String, LinkedHashMap<String, Object>>> suidMap =
                new LinkedHashMap<String, 
                        LinkedHashMap<String,
                                LinkedHashMap<String, Object>>>();

    public void clear() {
        suidMap.clear();
    }

    public Object addInstance(String suid, String cuid, String iuid,
            Object obj) {
        LinkedHashMap<String, LinkedHashMap<String, Object>> cuidMap =
                suidMap.get(suid);
        if (cuidMap == null) {
            cuidMap = new LinkedHashMap<String, LinkedHashMap<String, Object>>();
            suidMap.put(suid, cuidMap);
        }
        LinkedHashMap<String, Object> iuidMap = cuidMap.get(cuid);
        if (iuidMap == null) {
            iuidMap = new LinkedHashMap<String, Object>();
            cuidMap.put(cuid, iuidMap);
        }
        return iuidMap.put(iuid, obj);
    }

    public Set<String> getSUIDs() {
        return Collections.unmodifiableSet(suidMap.keySet());
    }    

    public Set<String> getCUIDs(String suid) {
        LinkedHashMap<String, LinkedHashMap<String, Object>> cuidMap =
                suidMap.get(suid);
        if (cuidMap == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(cuidMap.keySet());
    }

    public Set<String> getIUIDs(String suid, String cuid) {
        LinkedHashMap<String, LinkedHashMap<String, Object>> cuidMap =
                suidMap.get(suid);
        if (cuidMap == null) {
            return Collections.emptySet();
        }
        LinkedHashMap<String, Object> iuidMap = cuidMap.get(cuid);
        if (iuidMap == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(iuidMap.keySet());
    }

    public int countInstances(String suid, String cuid) {
        LinkedHashMap<String, LinkedHashMap<String, Object>> cuidMap =
                suidMap.get(suid);
        if (cuidMap == null) {
            return 0;
        }
        LinkedHashMap<String, Object> iuidMap = cuidMap.get(cuid);
        if (iuidMap == null) {
            return 0;
        }
        return iuidMap.size();
    }
    
    public Object getInstance(String suid, String cuid, String iuid) {
        LinkedHashMap<String, LinkedHashMap<String, Object>> cuidMap =
            suidMap.get(suid);
        if (cuidMap == null) {
            return null;
        }
        LinkedHashMap<String, Object> iuidMap = cuidMap.get(cuid);
        if (iuidMap == null) {
            return null;
        }
        return iuidMap.get(iuid);
    }

}
