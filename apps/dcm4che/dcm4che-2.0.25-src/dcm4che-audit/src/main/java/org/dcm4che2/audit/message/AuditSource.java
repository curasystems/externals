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
 * Identifies the system that detected the auditable event and created 
 * the audit message.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 5685 $ $Date: 2008-01-15 21:05:18 +0100 (Tue, 15 Jan 2008) $
 * @since Nov 17, 2006
 */
public class AuditSource extends BaseElement {

    private static AuditSource defAuditSource;
    private final ArrayList<TypeCode> auditSourceTypeCodes = new ArrayList<TypeCode>(
            1);
    
    public AuditSource(String id) {
        super("AuditSourceIdentification");
        setAuditSourceID(id);
    }

    public final void setAuditSourceID(String id) {
        super.addAttribute("AuditSourceID", id, false);
    }
    
    public final String getAuditSourceID() {
        return (String) getAttribute("AuditSourceID");
    }
    
    public final String getAuditEnterpriseSiteID() {
        return (String) getAttribute("AuditEnterpriseSiteID");
    }
    
    public final AuditSource setAuditEnterpriseSiteID(String id) {
        addAttribute("AuditEnterpriseSiteID", id, true);
        return this;
    }

    public final List<TypeCode> getAuditSourceTypeCodes() {
        return Collections.unmodifiableList(auditSourceTypeCodes);
    }
           
    public AuditSource addAuditSourceTypeCode(TypeCode code) {
        if (code == null) {
            throw new NullPointerException();
        }
        auditSourceTypeCodes.add(code);
        return this;
    }
    
    public AuditSource clearAuditSourceTypeCodes() {
        auditSourceTypeCodes.clear();
        return this;        
    }

    @Override
    protected boolean isEmpty() {
        return auditSourceTypeCodes.isEmpty();
    }
        
    @Override
    protected void outputContent(Writer out) throws IOException {
        outputChilds(out, auditSourceTypeCodes);
    }

    public static class TypeCode extends CodeElement {

        private TypeCode(String code) {
            super("AuditSourceTypeCode", code);
        }
        
        public static final TypeCode END_USER_DISPLAY_DEVICE = 
                new TypeCode("1");
        public static final TypeCode DATA_ACQUISITION_DEVICE = 
                new TypeCode("2");
        public static final TypeCode WEB_SERVER_PROCESS = 
                new TypeCode("3");
        public static final TypeCode APPLICATION_SERVER_PROCESS = 
                new TypeCode("4");
        public static final TypeCode DATABASE_SERVER_PROCESS = 
                new TypeCode("5");
        public static final TypeCode SECURITY_SERVER = 
                new TypeCode("6");
        public static final TypeCode ISO_LEVEL_1_3_NETWORK_COMPONENT = 
                new TypeCode("7");
        public static final TypeCode ISO_LEVEL_4_6_OPERATING_SOFTWARE = 
                new TypeCode("8");
        public static final TypeCode OTHER = 
                new TypeCode("9");
        
        private static TypeCode[] TYPE_CODES = { 
                END_USER_DISPLAY_DEVICE,
                DATA_ACQUISITION_DEVICE,
                WEB_SERVER_PROCESS,
                APPLICATION_SERVER_PROCESS,
                DATABASE_SERVER_PROCESS,
                SECURITY_SERVER,
                ISO_LEVEL_1_3_NETWORK_COMPONENT,
                ISO_LEVEL_4_6_OPERATING_SOFTWARE,
                OTHER,
        };
        
        public static TypeCode valueOf(String code) {
            try {
                return TYPE_CODES[Integer.parseInt(code) - 1];
            } catch (Exception e) {
                throw new IllegalArgumentException("code:" + code);
            }            
        }
        
    }
 
    public static AuditSource getDefaultAuditSource() {
        if (defAuditSource == null) {
            defAuditSource = new AuditSource(AuditMessage.getLocalHostName());
        }
        return defAuditSource;
    }
    
    public static void setDefaultAuditSource(AuditSource defAuditSource) {
        if (defAuditSource == null) {
            throw new NullPointerException("defAuditSource");
        }
        AuditSource.defAuditSource = defAuditSource;
    }

}
