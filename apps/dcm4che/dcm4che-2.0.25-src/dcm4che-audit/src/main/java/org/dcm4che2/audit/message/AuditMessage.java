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
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Generic Audit Message according RFC 3881. Typically an event type specific 
 * sub-class will be initiated.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 10568 $ $Date: 2009-03-31 15:36:47 +0200 (Tue, 31 Mar 2009) $
 * @since Nov 17, 2006
 */
public class AuditMessage extends BaseElement {

    private static final String XML_VERSION_1_0_ENCODING_UTF_8 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final int millisPerMinute = 60 * 1000;

    private static boolean incXMLDecl = false;
    private static boolean enableDNSLookups = false;
    private static boolean timezonedDateTime = true;
    private static boolean utcDateTime = false;
    private static boolean qualifyHostname = false;
    private static boolean allowMultipleRequestors = false;

    private static InetAddress localHost;
    static {
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            try {
                localHost = InetAddress.getByName(null);
            } catch (UnknownHostException e1) {
                // should never happen
                throw new RuntimeException(e1);
            }
        }            
    }
    
    private static String processID;
    static {
        processID = System.getProperty("app.pid");
        if (processID == null) {
            String s = ManagementFactory.getRuntimeMXBean().getName();
            int atPos = s.indexOf('@');
            processID = atPos > 0 ? s.substring(0, atPos) : s;
        }
    }
    
    private static String processName = System.getProperty("app.name");
    private static String[] localAETitles; 

    protected final AuditEvent event;
    protected final ArrayList<ActiveParticipant> activeParticipants =
            new ArrayList<ActiveParticipant>(3);
    protected final ArrayList<AuditSource> auditSources =
            new ArrayList<AuditSource>(1);
    protected final ArrayList<ParticipantObject> participantObjects =
            new ArrayList<ParticipantObject>(3);
    
    public AuditMessage(AuditEvent event) {
        super("AuditMessage");
        if (event == null) {
            throw new NullPointerException();
        }
        this.event = event;
    }
        
    public final AuditEvent getAuditEvent() {
        return event;
    }
    
    public void setEventDateTime(Date datetime) {
        event.setEventDateTime(datetime);
    }

    public void setOutcomeIndicator(AuditEvent.OutcomeIndicator outcome) {
        event.setOutcomeIndicator(outcome);
    }

    public List<AuditSource> getAuditSources() {
        return Collections.unmodifiableList(auditSources);
    }
       
    public AuditSource addAuditSource(AuditSource sourceId) {
        if (sourceId == null) {
            throw new NullPointerException();
        }
        auditSources.add(sourceId);
        return sourceId;
    }

    public List<ActiveParticipant> getActiveParticipants() {
        return Collections.unmodifiableList(activeParticipants);
    }
           
    public ActiveParticipant getRequestingActiveParticipants() {
        for (ActiveParticipant ap : activeParticipants) {
            if (ap.isUserIsRequestor()) {
                return ap;
            }            
        }
        return null;
    }
           
    public ActiveParticipant addActiveParticipant(ActiveParticipant apart) {
        if (!allowMultipleRequestors && apart.isUserIsRequestor()
                && getRequestingActiveParticipants() != null) {
            throw new IllegalStateException(
                    "Message already contains requesting Active Participant");
        }
        activeParticipants.add(apart);
        return apart;
    }
    
    public List<ParticipantObject> getParticipantObjects() {
        return Collections.unmodifiableList(participantObjects);
    }
           
    public ParticipantObject addParticipantObject(ParticipantObject obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        participantObjects.add(obj);
        return obj;
    }
    
    @Override
    protected boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return toString(1024);
    }    
    
    @Override
    public void output(Writer out) throws IOException {
        if (incXMLDecl) {
            out.write(XML_VERSION_1_0_ENCODING_UTF_8);
        }
        super.output(out);
    }

    public void validate() {
        if (activeParticipants.isEmpty()) {
            throw new IllegalStateException("No Active Participant");
        }
    }
    
    @Override
    protected void outputContent(Writer out) throws IOException {
        event.output(out);
        outputChilds(out, activeParticipants);
        if (auditSources.isEmpty()) {
            AuditSource.getDefaultAuditSource().output(out);
        } else {
            outputChilds(out, auditSources);
        }
        outputChilds(out, participantObjects);
    }

    public static final boolean isIncludeXMLDeclaration() {
        return incXMLDecl;
    }

    public static final void setIncludeXMLDeclaration(boolean incXMLDecl) {
        AuditMessage.incXMLDecl = incXMLDecl;
    }

    public static final boolean isTimezonedDateTime() {
        return timezonedDateTime;
    }

    public static final void setTimezonedDateTime(boolean timezonedDateTime) {
        AuditMessage.timezonedDateTime = timezonedDateTime;
    }

    public static final boolean isUtcDateTime() {
        return utcDateTime;
    }

    public static final void setUtcDateTime(boolean utcDateTime) {
        AuditMessage.utcDateTime = utcDateTime;
    }

    public static final boolean isQualifyHostname() {
        return qualifyHostname;
    }

    public static final void setQualifyHostname(boolean qualifyHostname) {
        AuditMessage.qualifyHostname = qualifyHostname;
    }

    public static String getProcessID() {
        return processID;
    }

    public static String getProcessName() {
        return processName;
    }

    public static void setProcessName(String processName) {
        AuditMessage.processName = processName;
    }

    public static String[] getLocalAETitles() {
        return (localAETitles != null ? localAETitles.clone() : null);
    }

    public static void setLocalAETitles(String[] aets) {
        AuditMessage.localAETitles = (aets != null ? aets.clone() : null);
    }
    
    public static boolean isEnableDNSLookups() {
        return enableDNSLookups;
    }

    public static void setEnableDNSLookups(boolean enableDNSLookups) {
        AuditMessage.enableDNSLookups = enableDNSLookups;
    }

    public static String hostNameOf(InetAddress node) {
        return enableDNSLookups ? skipDomain(node.getHostName())
                : node.getHostAddress();
    }

    public static String nodeIDOf(InetAddress node) {
        if (!enableDNSLookups) {
            return node.getHostAddress();
        }
        String hname = node.getCanonicalHostName();
        if (ActiveParticipant.isIP(hname)) {
            return hname;
        }
        int dotPos = hname.indexOf('.');
        if (dotPos == -1) {
            return hname;
        }
        char[] cs = hname.toCharArray();
        cs[dotPos] = '@';
        return new String(cs);
    }
    
    public static InetAddress getLocalHost() {
        return localHost;
    }
    
    public static String getLocalHostName() {
        return skipDomain(getLocalHost().getHostName());
    }
    
    public static String getLocalNodeID() {
        return nodeIDOf(localHost);
    }
    
    public static String aetToAltUserID(String aet) {
        if (aet == null || aet.length() == 0) {
            return null;
        }
        return "AETITLES=" + aet;
    }

    public static String aetsToAltUserID(String[] aets) {
        if (aets == null || aets.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer("AETITLES=");
        for (int i = 0; i < aets.length; i++) {
            if (aets[i] == null) {
                throw new NullPointerException("aets[" + i + "]");
            }                      
            if (aets[i].length() == 0) {
                throw new IllegalArgumentException("aets[" + i + "]=\"\"");
            }
            if (i > 0) {
                sb.append(';');
            }
            sb.append(aets[i]);
        }
        return sb.toString();
    }
    
    public static String[] altUserIDToAETs(String altUserID) {
        if (altUserID == null || !altUserID.startsWith("AETITLES=")) {
            return new String[0];
        }
        return altUserID.substring(9).split(";");
    }

    public static String toDateTimeStr(Date date) {
        Calendar c = Calendar.getInstance(Locale.ENGLISH);
        c.setTime(date);
        if (utcDateTime) {
            c.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        StringBuffer sb = new StringBuffer(30);
        sb.append(c.get(Calendar.YEAR));
        appendNNTo('-', c.get(Calendar.MONTH)+1, sb);
        appendNNTo('-', c.get(Calendar.DAY_OF_MONTH), sb);
        appendNNTo('T', c.get(Calendar.HOUR_OF_DAY), sb);
        appendNNTo(':', c.get(Calendar.MINUTE), sb);
        appendNNTo(':', c.get(Calendar.SECOND) 
                      + c.get(Calendar.MILLISECOND) / 1000f, sb);
        if (timezonedDateTime) {
            appendTZTo(c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET),
                    sb);
        }
        return sb.toString();
    }

    private static void appendNNTo(char c, int i, StringBuffer sb) {
        sb.append(c);
        if (i < 10) sb.append('0');
        sb.append(i);
    }

    private static void appendNNTo(char c, float f, StringBuffer sb) {
        sb.append(c);
        if (f < 10) sb.append('0');
        sb.append(f);
    }

    private static void appendTZTo(int millis, StringBuffer sb) {
        int mm = millis / millisPerMinute;
        if (mm == 0) {
            sb.append('Z');
            return;
        }
        char sign;
        if (mm > 0) {
            sign = '+';
        } else {
            sign = '-';
            mm = -mm;
        }
        int hh = mm / 60;
        mm -= hh * 60;
        appendNNTo(sign, hh, sb);
        appendNNTo(':', mm, sb);
    }
    
    private static String skipDomain(String hostname) {
        if (qualifyHostname)
            return hostname;
        int dotpos = hostname.indexOf('.');
        return dotpos > 0 && !Character.isDigit(hostname.charAt(0))
                ? hostname.substring(0, dotpos) : hostname;
    }

    public static void setAllowMultipleRequestors(boolean allowMultipleRequestors) {
        AuditMessage.allowMultipleRequestors = allowMultipleRequestors;
    }

    public static boolean isAllowMultipleRequestors() {
        return allowMultipleRequestors;
    }
}
