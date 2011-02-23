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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Identifies a user for the purpose of documenting accountability for the
 * audited event. A user may be a person, or a hardware device or software
 * process for events that are not initiated by a person.
 * 
 * Optionally, the user's network access location may be specified.
 *
 * There may be more than one user per event, for example, in cases of
 * actions initiated by one user for other users, or in events that
 * involve more than one user, hardware device, or system process.
 * However, only one user may be the initiator/requestor for the event.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 5685 $ $Date: 2008-01-15 21:05:18 +0100 (Tue, 15 Jan 2008) $
 * @since Nov 17, 2006
 */
public class ActiveParticipant extends BaseElement {

    private static final Pattern IP4 = Pattern.compile("\\d+(\\.\\d+){3}");
    private static final Pattern IP6 = Pattern
            .compile("[0-9a-fA-F]*(\\:[0-9a-fA-F]*){7}");
    private static final Pattern TEL_NO = Pattern
            .compile("(\\++\\d+\\s*)?(\\(\\d+\\)\\s*)?\\d+"
                    + "([-\\s]\\d+)*(X\\d+)?(B\\d+)?(C.*)?");
    private static boolean encodeUserIsRequestorTrue = false;

    private ArrayList<RoleIDCode> roleIDCodes = new ArrayList<RoleIDCode>(1);
    
    /**
     * Constructs an Active Participant with given user ID. Use 
     * {@link #setAlternativeUserID}, {@link #setUserName},
     * {@link #setNetworkAccessPointID}, {@link #addRoleIDCode} to initalize
     * other attributes.
     * 
     * @param userID
     *            unique identifier for the user
     * @param userIsRequestor
     *            <code>true</code>, if the user is the initiator/requestor for
     *            the event, otherwise <code>false</code>.
     */
    public ActiveParticipant(String userID, boolean userIsRequestor) {
        super("ActiveParticipant");
        if (userID.length() == 0) {
            throw new IllegalArgumentException("userID=\"\"");
        }
        addAttribute("UserID", userID, false);
        if (!userIsRequestor || encodeUserIsRequestorTrue) {
            addAttribute("UserIsRequestor", Boolean.valueOf(userIsRequestor),
                    false);
        }
    }

    /**
     * Creates an Active Participant identifying a person with the specified
     * User ID, Alternative User ID, User Name and Network Access Point ID.
     * 
     * @param userID
     *            unique identifier for the person used by the application
     * @param altUserID
     *            Alternative User ID, used for authentication (e.g. SSO)
     * @param userName
     *            person's name
     * @param napID
     *            identifier for the user's network access point (Machine Name,
     *            including DNS name, IP Address or Telephone Number)
     * @param requestor
     *            <code>true</code>, if the user is the initiator/requestor
     *            for the event, otherwise <code>false</code>.
     * @return Active Participant with the specified User ID, Alternative User
     *         ID, User Name and Network Access Point ID.
     */
    public static ActiveParticipant createActivePerson(String userID,
            String altUserID, String userName, String napID, boolean requestor) {
        ActiveParticipant ap = new ActiveParticipant(userID, requestor);
        ap.setAlternativeUserID(altUserID);
        ap.setUserName(userName);
        ap.setNetworkAccessPointID(napID);
        return ap;
    }

    /**
     * Creates an Active Participant identifying a process with the specified
     * Process ID, AETs, Process Name and Node ID.
     * 
     * @param processID
     * @param aets
     * @param processName
     * @param nodeID
     * @param requestor
     *            <code>true</code>, if the process is the initiator/requestor
     *            for the event, otherwise <code>false</code>.
     * @return Active Participant with the specified Process ID, AETs, Process
     *         Name and Network Access Point ID.
     */
    public static ActiveParticipant createActiveProcess(String processID,
            String[] aets, String processName, String nodeID, boolean requestor) {
        return createActivePerson(processID, AuditMessage.aetsToAltUserID(aets), 
                processName, nodeID, requestor) ;
    }

    public static ActiveParticipant createActiveNode(String nodeID,
            boolean requestor) {
        ActiveParticipant ap = new ActiveParticipant(nodeID, requestor);
        ap.setNetworkAccessPointID(nodeID);
        return ap;
    }
    
    public static ActiveParticipant createMedia(String mediaID,
            String mediaUID) {
        ActiveParticipant ap = new ActiveParticipant(mediaID, false);
        ap.setAlternativeUserID(mediaUID);
        return ap;
    }

    public static boolean isEncodeUserIsRequestorTrue() {
        return encodeUserIsRequestorTrue;
    }

    public static void setEncodeUserIsRequestorTrue(boolean enable) {
        ActiveParticipant.encodeUserIsRequestorTrue = enable;
    }

    public String getUserID() {
        return (String) getAttribute("UserID");
    }
    
    public String getAlternativeUserID() {
        return (String) getAttribute("AlternativeUserID");
    }
    
    public ActiveParticipant setAlternativeUserID(String id) {
        addAttribute("AlternativeUserID", id, true);
        return this;
    }
    
    public String getUserName() {
        return (String) getAttribute("UserName");
    }
    
    public ActiveParticipant setUserName(String name) {
        addAttribute("UserName", name, true);
        return this;
    }
    
    public boolean isUserIsRequestor() {
        Boolean requestor = (Boolean) getAttribute("UserIsRequestor");
        return requestor == null || requestor.booleanValue();
    }
    
    public String getNetworkAccessPointID() {
        return (String) getAttribute("NetworkAccessPointID");
    }

    public NetworkAccessPointTypeCode getNetworkAccessPointTypeCode() {
        return (NetworkAccessPointTypeCode) 
                getAttribute("NetworkAccessPointTypeCode");
    }

    public ActiveParticipant setNetworkAccessPointID(String id, 
            NetworkAccessPointTypeCode type) {
        addAttribute("NetworkAccessPointID", id, false);
        addAttribute("NetworkAccessPointTypeCode", type, false);
        return this;
    }

    public ActiveParticipant setNetworkAccessPointID(String id) {
            addAttribute("NetworkAccessPointID", id, true);
            addAttribute("NetworkAccessPointTypeCode", 
                    NetworkAccessPointTypeCode.valueOf(id), true);
        return this;
    }
    
    public void setNetworkAccessPointID(InetAddress ip) {
        setNetworkAccessPointID(AuditMessage.hostNameOf(ip));       
    }

    public List<RoleIDCode> getRoleIDCodes() {
        return Collections.unmodifiableList(roleIDCodes);
    }
    
    public ActiveParticipant addRoleIDCode(RoleIDCode code) {
        if (code == null) {
            throw new NullPointerException();
        }
        roleIDCodes.add(code);
        return this;
    }
    
    @Override
    protected boolean isEmpty() {
        return roleIDCodes.isEmpty();
    }
        
    @Override
    protected void outputContent(Writer out) throws IOException {
        outputChilds(out, roleIDCodes);
    }

    public static class RoleIDCode extends CodeElement {
        
        public static final RoleIDCode APPLICATION = 
                new RoleIDCode("110150","DCM","Application");
        public static final RoleIDCode APPLICATION_LAUNCHER = 
                new RoleIDCode("110151","DCM","Application Launcher");
        public static final RoleIDCode DESTINATION = 
                new RoleIDCode("110152","DCM","Destination");
        public static final RoleIDCode SOURCE = 
                new RoleIDCode("110153","DCM","Source");
        public static final RoleIDCode DESTINATION_MEDIA = 
                new RoleIDCode("110154","DCM","Destination Media");
        public static final RoleIDCode SOURCE_MEDIA = 
                new RoleIDCode("110155","DCM","Source Media");


        public RoleIDCode(String code) {
            super("RoleIDCode", code);
        }

        public RoleIDCode(String code, String codeSystemName, 
                String displayName) {
            super("RoleIDCode", code, codeSystemName, displayName);
        }
    }

    public static boolean isIP(String id) {
        return IP4.matcher(id).matches() || IP6.matcher(id).matches();
    }

    public static boolean isTelNo(String id) {
        return TEL_NO.matcher(id).matches();
    }

    
    public static class NetworkAccessPointTypeCode {
        private final String value;

        public static final NetworkAccessPointTypeCode MACHINE_NAME = 
                new NetworkAccessPointTypeCode("1");
        public static final NetworkAccessPointTypeCode IP_ADDRESS = 
                new NetworkAccessPointTypeCode("2");
        public static final NetworkAccessPointTypeCode TEL_NR = 
                new NetworkAccessPointTypeCode("3");
        
        private NetworkAccessPointTypeCode(final String value) {
            this.value = value;
        }
        
        public static NetworkAccessPointTypeCode valueOf(String id) {
            return (id == null || id.length() == 0) ? null
                    : isIP(id) ? IP_ADDRESS 
                    : isTelNo(id) ? TEL_NR : MACHINE_NAME;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    
}
