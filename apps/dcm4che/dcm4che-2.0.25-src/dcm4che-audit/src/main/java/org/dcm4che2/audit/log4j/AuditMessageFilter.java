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
 
package org.dcm4che2.audit.log4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.dcm4che2.audit.message.ActiveParticipant;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.CodeElement;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 6225 $ $Date: 2008-04-28 12:54:28 +0200 (Mon, 28 Apr 2008) $
 * @since Nov 23, 2006
 */
public class AuditMessageFilter extends Filter {

    private boolean acceptOnMatch = false;
    private String eventIDToMatch;
    private String eventActionCodesToMatch;
    private List<AuditEvent.OutcomeIndicator> outcomeIndicatorsToMatch;
    private String eventTypeCodeToMatch;
    private String userIDToMatch;
    private Boolean userIsRequestorToMatch;
    private String aeTitleToMatch;
    private String roleIDCodeToMatch;
    private String machineNameToMatch;
    private String ipAddressToMatch;
 
    @Override
    public int decide(LoggingEvent event) {
        Object msg = event.getMessage();
        if (!(msg instanceof AuditMessage)) {
            return Filter.NEUTRAL;
        }
        AuditMessage auditMsg = (AuditMessage) msg;
        if (matchEvent(auditMsg.getAuditEvent())
                && matchActiveParticipant(auditMsg.getActiveParticipants())) {
            return acceptOnMatch ? Filter.ACCEPT : Filter.DENY;
        }
        return Filter.NEUTRAL;
    }

    private boolean matchEvent(AuditEvent auditEvent) {
        if (eventIDToMatch != null && eventIDToMatch.length() != 0) {
            if (!eventIDToMatch.equals(codeToString(auditEvent.getEventID()))) {
                return false;
            }
        }        
        if (eventActionCodesToMatch != null
                && eventActionCodesToMatch.length() != 0) {
            AuditEvent.ActionCode code = auditEvent.getEventActionCode();
            if (code == null 
                    || eventActionCodesToMatch.indexOf(code.toString()) == -1) {
                return false;                
            }
        }
        if (outcomeIndicatorsToMatch != null
                && !outcomeIndicatorsToMatch.isEmpty()) {
            if (outcomeIndicatorsToMatch.indexOf(
                    auditEvent.getEventOutcomeIndicator()) == -1) {
                return false;
            }
        }
        if (eventTypeCodeToMatch != null
                && eventTypeCodeToMatch.length() != 0) {
            if (!matchEventTypeCode(auditEvent.getEventTypeCodes())) {
                return false;
            }
        }
        return true;
    }

    private String codeToString(CodeElement code) {
        String codeSystemName = code.getCodeSystemName();
        return codeSystemName == null ? code.getCode() 
                                      : code.getCode() + '^' + codeSystemName;
    }

    private boolean matchEventTypeCode(
            List<AuditEvent.TypeCode> eventTypeCodes) {
        for (AuditEvent.TypeCode typeCode : eventTypeCodes) {
            if (eventTypeCodeToMatch.equals(codeToString(typeCode))) {
                return true;
            }           
        }
        return false;
    }

    private boolean matchActiveParticipant(
            List<ActiveParticipant> activeParticipants) {
        for (ActiveParticipant participant : activeParticipants) {
            if (matchActiveParticipant(participant)) {
                return true;
            }            
        }
        return false;
    }

    private boolean matchActiveParticipant(ActiveParticipant participant) {
        if (userIDToMatch != null && userIDToMatch.length() != 0) {
            if (!userIDToMatch.equals(participant.getUserID())) {
                return false;
            }
        }
        if (aeTitleToMatch != null && aeTitleToMatch.length() != 0) {
            if (Arrays.asList(
                    AuditMessage.altUserIDToAETs(
                            participant.getAlternativeUserID()))
                    .indexOf(aeTitleToMatch) == -1) {
                return false;
            }
        }
        if (userIsRequestorToMatch != null) {
            if (userIsRequestorToMatch.booleanValue() 
                    != participant.isUserIsRequestor()) {
                return false;
            }
        }
        if (roleIDCodeToMatch != null && roleIDCodeToMatch.length() != 0) {
            if (!matchRoleIDCode(participant.getRoleIDCodes())) {
                return false;
            }
        }
        String napid = participant.getNetworkAccessPointID();
        ActiveParticipant.NetworkAccessPointTypeCode naptype = 
            participant.getNetworkAccessPointTypeCode();
        if (machineNameToMatch != null && machineNameToMatch.length() != 0) {
            if (naptype != ActiveParticipant.NetworkAccessPointTypeCode.MACHINE_NAME
                    || !machineNameToMatch.equals(napid)) {
                        return false;
            }
        }
        if (ipAddressToMatch != null && ipAddressToMatch.length() != 0) {
            if (naptype != ActiveParticipant.NetworkAccessPointTypeCode.IP_ADDRESS
                    || !ipAddressToMatch.equals(napid)) {
                        return false;
            }
        }
        return true;
    }

    private boolean matchRoleIDCode(
            List<ActiveParticipant.RoleIDCode> roleIDCodeIDs) {
        for (ActiveParticipant.RoleIDCode roleIDCode : roleIDCodeIDs) {
            if (roleIDCodeToMatch.equals(codeToString(roleIDCode))) {
                return true;
            }           
        }
        return false;
    }

    public final boolean isAcceptOnMatch() {
        return acceptOnMatch;
    }

    public final void setAcceptOnMatch(boolean acceptOnMatch) {
        this.acceptOnMatch = acceptOnMatch;
    }

    public final String getEventIDToMatch() {
        return eventIDToMatch;
    }

    public final void setEventIDToMatch(String eventIDToMatch) {
        this.eventIDToMatch = eventIDToMatch;
    }

    public final String getEventActionCodesToMatch() {
        return eventActionCodesToMatch;
    }

    public final void setEventActionCodesToMatch(String actionCodes) {
        this.eventActionCodesToMatch = actionCodes.toUpperCase();
    }

    public String getEventOutcomeIndicatorsToMatch() {
        if (outcomeIndicatorsToMatch == null 
                || outcomeIndicatorsToMatch.isEmpty()) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (AuditEvent.OutcomeIndicator outcome : outcomeIndicatorsToMatch) {
            sb.append(tostr(outcome)).append(',');
        }
        return sb.substring(0, sb.length()-1);
    }

    private String tostr(AuditEvent.OutcomeIndicator indicator) {
        if (indicator == AuditEvent.OutcomeIndicator.SUCCESS) {
            return "SUCCESS";
        } else if (indicator == AuditEvent.OutcomeIndicator.MINOR_FAILURE) {
            return "WARN";
        } else if (indicator == AuditEvent.OutcomeIndicator.SERIOUS_FAILURE) {
            return "ERROR";
        } else if (indicator == AuditEvent.OutcomeIndicator.MAJOR_FAILURE) {
            return "FATAL";
        } else {
            throw new Error("Unexpected AuditEvent.OutcomeIndicator(" 
                    + indicator + ")");
        }
    }

    public final void setEventOutcomeIndicatorsToMatch(String value) {
        StringTokenizer stk = new StringTokenizer(value, ",; \t");
        if (!stk.hasMoreTokens()) {
            outcomeIndicatorsToMatch = null;
        } else {
            outcomeIndicatorsToMatch = 
                new ArrayList<AuditEvent.OutcomeIndicator>(stk.countTokens());
            do {
                try {
                    outcomeIndicatorsToMatch.add(
                            toOutcomeIndicator(stk.nextToken()));
                } catch (IllegalArgumentException e) {
                    // ignore illegal param values 
                }
            } while (stk.hasMoreTokens());
        }
    }

    private AuditEvent.OutcomeIndicator toOutcomeIndicator(String value) {
        if ("SUCCESS".equalsIgnoreCase(value)) {
            return AuditEvent.OutcomeIndicator.SUCCESS;
        } else if ("WARN".equalsIgnoreCase(value)) {
            return AuditEvent.OutcomeIndicator.MINOR_FAILURE;
        } else if ("ERROR".equalsIgnoreCase(value)) {
            return AuditEvent.OutcomeIndicator.SERIOUS_FAILURE;
        } else if ("FATAL".equalsIgnoreCase(value)) {
            return AuditEvent.OutcomeIndicator.MAJOR_FAILURE;
        } else {
            throw new IllegalArgumentException(value);
        }
    }

    public final String getEventTypeCodeToMatch() {
        return eventTypeCodeToMatch;
    }

    public final void setEventTypeCodeToMatch(String eventTypeCode) {
        this.eventTypeCodeToMatch = eventTypeCode;
    }

    public final String getUserIDToMatch() {
        return userIDToMatch;
    }

    public final void setUserIDToMatch(String userID) {
        this.userIDToMatch = userID;
    }

    public final String getAETitleToMatch() {
        return aeTitleToMatch;
    }
    
    public final void setAETitlesToMatch(String aeTitleToMatch) {
        this.aeTitleToMatch = aeTitleToMatch;
    }
    
    public final String getRoleIDCodeToMatch() {
        return roleIDCodeToMatch;
    }

    public final void setRoleIDCodeToMatch(String roleIDCodeToMatch) {
        this.roleIDCodeToMatch = roleIDCodeToMatch;
    }

    public final Boolean getUserIsRequestorToMatch() {
        return userIsRequestorToMatch;
    }

    public final void setUserIsRequestorToMatch(Boolean userIsRequestor) {
        this.userIsRequestorToMatch = userIsRequestor;
    }
    
    public final String getMachineNameToMatch() {
        return machineNameToMatch;
    }

    public final void setMachineNameToMatch(String machineName) {
        this.machineNameToMatch = machineName;
    }

    public final String getIPAddressToMatch() {
        return ipAddressToMatch;
    }

    public final void setIPAddressToMatch(String ipAddressToMatch) {
        this.ipAddressToMatch = ipAddressToMatch;
    }

}
