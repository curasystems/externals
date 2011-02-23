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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Identifies instances of data or objects that have been accessed.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 6823 $ $Date: 2008-08-21 14:58:03 +0200 (Thu, 21 Aug 2008) $
 * @since Nov 17, 2006
 */
public class ParticipantObject extends BaseElement {

    private final IDTypeCode idTypeCode;
    private Name name;
    private Query query;
    private final ArrayList<Detail> details = new ArrayList<Detail>();
    private final ArrayList<ParticipantObjectDescription> descs =
            new ArrayList<ParticipantObjectDescription>();
    
    /** Create a participant object identified by the given id,
     * of type specified by idTypeCode.  Additionally, participant details
     * can be added afterwards to specify the participant more completely.
     */
    public ParticipantObject(String id, IDTypeCode idTypeCode) {
        super("ParticipantObjectIdentification");
        if (idTypeCode == null) {
            throw new NullPointerException("idTypeCode");
        }
        addAttribute("ParticipantObjectID", id, false);
        this.idTypeCode = idTypeCode;
    }

    /** Returns the id specified for this object when it was created */
    public final String getParticipantObjectID() {
        return (String) getAttribute("ParticipantObjectID");
    }
    
    public final IDTypeCode getParticipantObjectIDTypeCode() {
        return idTypeCode;
    }
    
    public final TypeCode getParticipantObjectTypeCode() {
        return (TypeCode) getAttribute("ParticipantObjectTypeCode");
    }
    
    public final ParticipantObject setParticipantObjectTypeCode(TypeCode code) {
        addAttribute("ParticipantObjectTypeCode", code, true);
        return this;
    }
    
    public final TypeCodeRole getParticipantObjectTypeCodeRole() {
        return (TypeCodeRole) getAttribute("ParticipantObjectTypeCodeRole");
    }
    
    public final ParticipantObject setParticipantObjectTypeCodeRole(
            TypeCodeRole code) {
        addAttribute("ParticipantObjectTypeCodeRole", code, true);
        return this;
    }
    
    public final DataLifeCycle getParticipantObjectDataLifeCycle() {
        return (DataLifeCycle) getAttribute("ParticipantObjectDataLifeCycle");
    }
    
    public final ParticipantObject setParticipantObjectDataLifeCycle(
            DataLifeCycle code) {
        addAttribute("ParticipantObjectDataLifeCycle", code, true);
        return this;
    }
    
    public final String getParticipantObjectSensitivity() {
        return (String) getAttribute("ParticipantObjectSensitivity");
    }
    
    public final ParticipantObject setParticipantObjectSensitivity(
            String sensitivity) {
        addAttribute("ParticipantObjectSensitivity", sensitivity, true);
        return this;
    }

    public final String getParticipantObjectName() {
        return name != null ? name.value() : null;
    }
    
    public final ParticipantObject setParticipantObjectName(String name) {
        if (name == null || name.length() == 0) {
            this.name = null;
        } else {
            if (this.query != null) {
                throw new IllegalStateException(
                        "Cannot set ParticipantObjectName and ParticipantObjectQuery");
            }
            this.name = new Name(name);
        }
        return this;
    }
    
    public final byte[] getParticipantObjectQuery() {
        return query != null ? query.value() : null;
    }
    
    public final ParticipantObject setParticipantObjectQuery(byte[] query) {
        if (query == null || query.length == 0) {
            this.query = null;
        } else {
            if (this.name != null) {
                throw new IllegalStateException("Cannot set ParticipantObjectName " +
                            "and ParticipantObjectQuery");
            }
            this.query = new Query(query);
        }
        return this;
    }

    public List<Detail> getParticipantObjectDetails() {
        return Collections.unmodifiableList(details);
    }
    
    public ParticipantObject addParticipantObjectDetail(String type,
            String value) {
        details.add(new Detail(type, value));
        return this;
    }

    public ParticipantObject addParticipantObjectDetail(String type,
            byte[] value) {
        details.add(new Detail(type, value));
        return this;
    }

    public List<ParticipantObjectDescription>
            getParticipantObjectDescriptions() {
        return Collections.unmodifiableList(descs);
    }
        
    public ParticipantObject addParticipantObjectDescription(
            ParticipantObjectDescription desc) {
        if (desc == null) {
            throw new NullPointerException();
        }
        descs.add(desc);
        return this;
    }
    
    @Override
    protected boolean isEmpty() {
        return false;
    }
        
    @Override
    protected void outputContent(Writer out) throws IOException {
        idTypeCode.output(out);
        if (name != null) {
            name.output(out);
        }
        if (query != null) {
            query.output(out);
        }
        outputChilds(out, details);
        outputChilds(out, descs);
    }
    
    public static ParticipantObject createPatient(String id, String name) {
        ParticipantObject pat = new ParticipantObject(id, IDTypeCode.PATIENT_ID);
        pat.setParticipantObjectTypeCode(TypeCode.PERSON);
        pat.setParticipantObjectTypeCodeRole(TypeCodeRole.PATIENT);            
        pat.setParticipantObjectName(name);
        return pat;
    }

    /**
     * Create a participant object with type code STUDY_INSTANCE_UID,
     * and the specified description (which maybe null).
     * @param uid Study Instance UID
     * @param desc (null) Description including patient name/id
     */
    public static ParticipantObject createStudy(String uid,
            ParticipantObjectDescription desc) {
        ParticipantObject study = new ParticipantObject(uid, 
                IDTypeCode.STUDY_INSTANCE_UID);
        study.setParticipantObjectTypeCode(TypeCode.SYSTEM);
        study.setParticipantObjectTypeCodeRole(TypeCodeRole.REPORT);
        if (desc != null) {
            study.addParticipantObjectDescription(desc);
        }
        return study;
    }


    public static ParticipantObject createDataRepository(String uri) {
        ParticipantObject obj = new ParticipantObject(uri, IDTypeCode.URI);
        obj.setParticipantObjectTypeCode(TypeCode.SYSTEM);
        obj.setParticipantObjectTypeCodeRole(TypeCodeRole.DATA_REPOSITORY);
        return obj;
    }
    
    public static ParticipantObject createQuerySOPClass(String cuid, 
            String tsuid, byte[] query) {
        ParticipantObject queryObj = new ParticipantObject(cuid, 
                IDTypeCode.SOP_CLASS_UID);
        queryObj.setParticipantObjectTypeCode(TypeCode.SYSTEM);
        queryObj.setParticipantObjectTypeCodeRole(TypeCodeRole.REPORT);
        queryObj.setParticipantObjectQuery(query);
        queryObj.addParticipantObjectDetail("TransferSyntax", tsuid);
        return queryObj;
    }
  
    public static ParticipantObject createSecurityAuditLog(String uri) {
        ParticipantObject obj = new ParticipantObject(uri, IDTypeCode.URI);
        obj.setParticipantObjectTypeCode(TypeCode.SYSTEM);
        obj.setParticipantObjectTypeCodeRole(TypeCodeRole.SECURITY_RESOURCE);
        obj.setParticipantObjectName("Security Audit Log");
        return obj;
    }
    
    public static ParticipantObject createAlertSubject(String id, 
            IDTypeCode idTypeCode, String desc) {
        ParticipantObject obj = new ParticipantObject(id, idTypeCode);
        obj.setParticipantObjectTypeCode(TypeCode.SYSTEM);
        obj.addParticipantObjectDetail("AlertDescription", desc);
        return obj;
    }

    public static ParticipantObject createAlertSubjectWithURI(String uri, 
            String desc) {
        return createAlertSubject(uri, IDTypeCode.URI, desc);
    }
    
    public static ParticipantObject createAlertSubjectWithNodeID(String nodeID, 
            String desc) {
        return createAlertSubject(nodeID, IDTypeCode.NODE_ID, desc);
    }

    public static class IDTypeCode extends CodeElement {

        public static final IDTypeCode MEDIAL_RECORD_NUMBER = 
                new IDTypeCode("1");
        public static final IDTypeCode PATIENT_ID = new IDTypeCode("2");
        public static final IDTypeCode ENCOUNTER_NUMBER = new IDTypeCode("3");
        public static final IDTypeCode ENROLLEE_NUMBER = new IDTypeCode("4");
        public static final IDTypeCode SOCIAL_SECURITY_NUMBER = 
                new IDTypeCode("5");
        public static final IDTypeCode ACCOUNT_NUMBER = new IDTypeCode("6");
        public static final IDTypeCode GUARANTOR_NUMBER = new IDTypeCode("7");
        public static final IDTypeCode REPORT_NAME = new IDTypeCode("8");    
        public static final IDTypeCode REPORT_NUMBER = new IDTypeCode("9");
        public static final IDTypeCode SEARCH_CRITERIA = new IDTypeCode("10");
        public static final IDTypeCode USER_IDENTIFIER = new IDTypeCode("11");
        public static final IDTypeCode URI = new IDTypeCode("12");
        public static final IDTypeCode STUDY_INSTANCE_UID = 
                new IDTypeCode("110180","DCM","Study Instance UID");
        public static final IDTypeCode SOP_CLASS_UID = 
                new IDTypeCode("110181","DCM","SOP Class UID");
        public static final IDTypeCode NODE_ID = 
                new IDTypeCode("110182","DCM","Node ID");
        
        private IDTypeCode(String typeCode) {
            super("ParticipantObjectIDTypeCode", typeCode);
        }

        public IDTypeCode(String code, String codeSystemName, 
                String displayName) {
            super("ParticipantObjectIDTypeCode", code, codeSystemName, displayName);        
        }
    }

    public static class TypeCode {

        private final String value;
        
        public static final TypeCode PERSON = new TypeCode("1");
        public static final TypeCode SYSTEM = new TypeCode("2");
        public static final TypeCode ORGANIZATION = new TypeCode("3");
        public static final TypeCode OTHER = new TypeCode("4");
        
        private TypeCode(final String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    public static class TypeCodeRole {

        private final String value;
        
        public static final TypeCodeRole PATIENT = new TypeCodeRole("1");
        public static final TypeCodeRole LOCATION = new TypeCodeRole("2");
        public static final TypeCodeRole REPORT = new TypeCodeRole("3");
        public static final TypeCodeRole RESOURCE = new TypeCodeRole("4");
        public static final TypeCodeRole MASTER_FILE = new TypeCodeRole("5");
        public static final TypeCodeRole USER = new TypeCodeRole("6");
        public static final TypeCodeRole LIST = new TypeCodeRole("7");
        public static final TypeCodeRole DOCTOR = new TypeCodeRole("8");
        public static final TypeCodeRole SUBSCRIBER = new TypeCodeRole("9");
        public static final TypeCodeRole GUARANTOR = new TypeCodeRole("10");
        public static final TypeCodeRole SECURITY_USER_ENTITY = 
                new TypeCodeRole("11");
        public static final TypeCodeRole SECURITY_USER_GROUP = 
                new TypeCodeRole("12");
        public static final TypeCodeRole SECURITY_RESOURCE = 
                new TypeCodeRole("13");
        public static final TypeCodeRole SECURITY_GRANULARITY_DEFINITION = 
                new TypeCodeRole("14");
        public static final TypeCodeRole PROVIDER = new TypeCodeRole("15");
        public static final TypeCodeRole DATA_DESTINATION =  
                new TypeCodeRole("16");
        public static final TypeCodeRole DATA_REPOSITORY = 
                new TypeCodeRole("17");
        public static final TypeCodeRole SCHEDULE = new TypeCodeRole("18");
        public static final TypeCodeRole CUSTOMER = new TypeCodeRole("19");
        public static final TypeCodeRole JOB = new TypeCodeRole("20");
        public static final TypeCodeRole JOB_STREAM = new TypeCodeRole("21");
        public static final TypeCodeRole TABLE = new TypeCodeRole("22");
        public static final TypeCodeRole ROUTING_CRITERIA = 
                new TypeCodeRole("23");
        public static final TypeCodeRole QUERY = new TypeCodeRole("24");
        
        private TypeCodeRole(final String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    public static class DataLifeCycle {

        private final String value;
        
        public static final DataLifeCycle CREATION = new DataLifeCycle("1");
        public static final DataLifeCycle IMPORT = new DataLifeCycle("2");
        public static final DataLifeCycle AMENDMENT = new DataLifeCycle("3");
        public static final DataLifeCycle VERIFICATION = new DataLifeCycle("4");
        public static final DataLifeCycle TRANSLATION = new DataLifeCycle("5");
        public static final DataLifeCycle ACCESS = new DataLifeCycle("6");
        public static final DataLifeCycle DE_IDENTIFICATION = 
                new DataLifeCycle("7");
        public static final DataLifeCycle AGGREGATION = new DataLifeCycle("8");
        public static final DataLifeCycle REPORT = new DataLifeCycle("9");
        public static final DataLifeCycle EXPORT = new DataLifeCycle("10");
        public static final DataLifeCycle DISCLOSURE = new DataLifeCycle("11");
        public static final DataLifeCycle RECEIPT_OF_DISCLOSURE = 
                new DataLifeCycle("12");
        public static final DataLifeCycle ARCHIVING = new DataLifeCycle("13");
        public static final DataLifeCycle LOGICAL_DELETION = 
                new DataLifeCycle("14");
        public static final DataLifeCycle PHYSICAL_DESTRUCTION = 
                new DataLifeCycle("15");
        
        private DataLifeCycle(final String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }

    private static class Name extends BaseElement {

        private final String value;
        
        private Name(String value) {
            super("ParticipantObjectName");
            if (value.length() == 0) {
                throw new IllegalArgumentException("value cannot be empty");
            }
            this.value = value;
        }
        
        public final String value() {
            return value;
        }

        @Override
        protected boolean isEmpty() {
            return false;
        }

        @Override
        protected void outputContent(Writer out) throws IOException {
            outputEscaped(out, value, "'");
        }
    }
    
    private static class Query extends BaseElement {

        private final byte[] value;
        
        private Query(byte[] value) {
            super("ParticipantObjectQuery");
            if (value.length == 0) {
                throw new IllegalArgumentException("value cannot be empty");
            }
            this.value = value.clone();
        }

        public final byte[] value() {
            return value.clone();
        }
        
        @Override
        protected boolean isEmpty() {
            return false;
        }
        
        @Override
        protected void outputContent(Writer out) throws IOException {
            out.write(Base64Encoder.encode(value));
        }                    
    }
   
    public static class Detail extends BaseElement {

        public Detail(String type, byte[] value) {
            super("ParticipantObjectDetail");
            addAttribute("type", type, false);
            addAttribute("value", value.clone(), false);        
        }
        
        public Detail(String type, String value) {
            super("ParticipantObjectDetail");
            addAttribute("type", type, false);
            try {
                addAttribute("value", value.getBytes("UTF-8"), false);
            } catch (UnsupportedEncodingException e) {
                throw new Error(e);
            }        
        }
        
        public final String getType() {
            return (String) getAttribute("type");
        }
        
        public final byte[] getValue() {
            return ((byte[]) getAttribute("value")).clone();
        }
        
        public final String getValueAsString() {
            try {
                return new String((byte[]) getAttribute("value"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                 throw new Error(e);
            }
        }
    }
    
 }
