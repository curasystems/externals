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
 
package org.dcm4che2.audit.message;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DICOM Extension of ParticipantObject identifier.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 5685 $ $Date: 2008-01-15 21:05:18 +0100 (Tue, 15 Jan 2008) $
 * @since Nov 17, 2006
 * @see ParticipantObject
 */
public class ParticipantObjectDescription extends BaseElement {

    private final ArrayList<BaseElement> mppss = new ArrayList<BaseElement>();
    private final ArrayList<BaseElement> accessions =
            new ArrayList<BaseElement>();
    private final ArrayList<SOPClass> sopClasses = new ArrayList<SOPClass>();
    private final ArrayList<BaseElement> studies = new ArrayList<BaseElement>();
    
    public ParticipantObjectDescription() {
        super("ParticipantObjectDescription");
    }

    public final boolean isEncrypted() {
        Boolean encrypted = (Boolean) getAttribute("Encrypted");
        return encrypted != null && encrypted.booleanValue();
    }

    public final ParticipantObjectDescription setEncrypted(boolean encrypted) {
        addAttribute("Encrypted", Boolean.valueOf(encrypted), false);
        return this;
    }

    public final boolean isAnonymized() {
        Boolean anonymized = (Boolean) getAttribute("Anonymized");
        return anonymized != null && anonymized.booleanValue();
    }

    public final ParticipantObjectDescription setAnonymized(boolean anonymized) {
        addAttribute("Anonymized", Boolean.valueOf(anonymized), true);
        return this;
    }

    private static List<String> toStringList(List<BaseElement> elements,
            String attrName) {
        ArrayList<String> list = new ArrayList<String>(elements.size());
        for (BaseElement el : elements) {
            list.add(el.getAttribute(attrName).toString());
        }
        return list;
    }        
            
    public List<String> getMPPSs() {
        return toStringList(mppss, "UID");
    }

    public ParticipantObjectDescription addMPPS(String iuid) {
        mppss.add(new BaseElement("MPPS", "UID", iuid));
        return this;
    }

    public List<String> getAccessions() {
        return toStringList(accessions, "Number");
    }

    public ParticipantObjectDescription addAccession(String accno) {
        accessions.add(new BaseElement("Accession", "Number", accno));
        return this;
    }

    public List<SOPClass> getSOPClasses() {
        return Collections.unmodifiableList(sopClasses);
    }        
    
    public ParticipantObjectDescription addSOPClass(SOPClass sopClass) {
        if (sopClass == null) {
            throw new NullPointerException();
        }
        sopClasses.add(sopClass);
        return this;
    }

    public ParticipantObjectDescription addStudy(String suid) {
        studies.add(new BaseElement("Study", "UID", suid));
        return this;
    }

    @Override
    protected boolean isEmpty() {
        return false;
    }
    
    @Override
    protected void outputContent(Writer out) throws IOException {
        outputChilds(out, mppss);
        outputChilds(out, accessions);
        outputChilds(out, sopClasses);
        outputChilds(out, studies);
    }

    public static class SOPClass extends BaseElement {

        private final ArrayList<BaseElement> instances = new ArrayList<BaseElement>();

        public SOPClass(String uid) {
            super("SOPClass", "UID", uid);
        }

        public SOPClass setNumberOfInstances(int n) {
            addAttribute("NumberOfInstances", n > 0 ? new Integer(n) : null, true);
            return this;
        }

        public int getNumberOfInstances() {
            Integer n = (Integer) getAttribute("NumberOfInstances");
            return n != null ? n.intValue() : 0;
        }
        
        public SOPClass addInstance(String iuid) {
            instances.add(new BaseElement("Instance", "UID", iuid));
            return this;
        }

        @Override
        protected boolean isEmpty() {
            return instances.isEmpty();
        }
        
        @Override
        protected void outputContent(Writer out) throws IOException {
            outputChilds(out, instances);
        }
    }
}
