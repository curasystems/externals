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

package org.dcm4che2.net.pdu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.dcm4che2.data.Implementation;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.util.IntHashtable;
import org.dcm4che2.util.StringUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date: 2008-08-21 12:21:32 +0200 (Thu, 21 Aug 2008) $
 * @since Sep 15, 2005
 */
public abstract class AAssociateRQAC {
    public static final int DEF_MAX_PDU_LENGTH = 16384;
    private static final String DEF_CALLED_AET = "ANONYMOUS";
    private static final String DEF_CALLING_AET = "ANONYMOUS";

    protected byte[] reservedBytes = new byte[32];
    protected int protocolVersion = 1;
    protected int maxPDULength = DEF_MAX_PDU_LENGTH;
    protected int maxOpsInvoked = 1;
    protected int maxOpsPerformed = 1;
    protected String calledAET = DEF_CALLED_AET;
    protected String callingAET = DEF_CALLING_AET;
    protected String applicationContext = UID.DICOMApplicationContextName;
    protected String implClassUID = Implementation.classUID();
    protected String implVersionName = Implementation.versionName();
    protected final ArrayList<PresentationContext>
            pcs = new ArrayList<PresentationContext>();
    protected final IntHashtable<PresentationContext>
            pcidMap = new IntHashtable<PresentationContext>();
    protected final LinkedHashMap<String, RoleSelection>
            roleSelMap = new LinkedHashMap<String, RoleSelection>();
    protected final LinkedHashMap<String, ExtendedNegotiation>
            extNegMap = new LinkedHashMap<String, ExtendedNegotiation>();
    protected final LinkedHashMap<String, CommonExtendedNegotiation>
            commonExtNegMap = new LinkedHashMap<String, CommonExtendedNegotiation>();

    public final int getProtocolVersion() {
        return protocolVersion;
    }

    public final void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public final byte[] getReservedBytes() {
        return reservedBytes.clone();
    }

    public final void setReservedBytes(byte[] reservedBytes) {
        if (reservedBytes.length != 32)
            throw new IllegalArgumentException("reservedBytes.length: "
                    + reservedBytes.length);
        System.arraycopy(reservedBytes, 0, this.reservedBytes, 0, 32);
    }

    public final String getCalledAET() {
        return calledAET;
    }

    public final void setCalledAET(String calledAET) {
        if (calledAET.length() > 16)
            throw new IllegalArgumentException("calledAET: " + calledAET);
        this.calledAET = calledAET;
    }

    public final String getCallingAET() {
        return callingAET;
    }

    public final void setCallingAET(String callingAET) {
        if (callingAET.length() > 16)
            throw new IllegalArgumentException("callingAET: " + callingAET);
        this.callingAET = callingAET;
    }

    public final String getApplicationContext() {
        return applicationContext;
    }

    public final void setApplicationContext(String applicationContext) {
        if (applicationContext == null)
            throw new NullPointerException();

        this.applicationContext = applicationContext;
    }

    public final int getMaxPDULength() {
        return maxPDULength;
    }

    public final void setMaxPDULength(int maxPDULength) {
        this.maxPDULength = maxPDULength;
    }

    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    public final boolean isAsyncOps() {
        return maxOpsInvoked != 1 || maxOpsPerformed != 1;
    }

    public final String getImplClassUID() {
        return implClassUID;
    }

    public final void setImplClassUID(String implClassUID) {
        if (implClassUID == null)
            throw new NullPointerException();

        this.implClassUID = implClassUID;
    }

    public final String getImplVersionName() {
        return implVersionName;
    }

    public final void setImplVersionName(String implVersionName) {
        this.implVersionName = implVersionName;
    }

    public Collection<PresentationContext> getPresentationContexts() {
        return Collections.unmodifiableCollection(pcs);
    }

    public int getNumberOfPresentationContexts() {
        return pcs.size();
    }

    public PresentationContext getPresentationContext(int pcid) {
        return pcidMap.get(pcid);
    }

    public synchronized void addPresentationContext(PresentationContext pc) {
        int pcid = pc.getPCID();
        PresentationContext prev = (PresentationContext) pcidMap.remove(pcid);
        if (prev != null)
            pcs.remove(prev);
        pcidMap.put(pcid, pc);
        pcs.add(pc);
    }

    public synchronized boolean removePresentationContext(PresentationContext pc) {
        if (!pcs.remove(pc))
            return false;

        pcidMap.remove(pc.getPCID());
        return true;
    }

    public Collection<RoleSelection> getRoleSelections() {
        return Collections.unmodifiableCollection(roleSelMap.values());
    }

    public RoleSelection getRoleSelectionFor(String cuid) {
        return roleSelMap.get(cuid);
    }

    public RoleSelection addRoleSelection(RoleSelection rs) {
        return roleSelMap.put(rs.getSOPClassUID(), rs);
    }

    public RoleSelection removeRoleSelectionFor(String cuid) {
        return roleSelMap.remove(cuid);
    }

    public Collection<ExtendedNegotiation> getExtendedNegotiations() {
        return Collections.unmodifiableCollection(extNegMap.values());
    }

    public ExtendedNegotiation getExtendedNegotiationFor(String cuid) {
        return extNegMap.get(cuid);
    }

    public ExtendedNegotiation addExtendedNegotiation(ExtendedNegotiation extNeg) {
        return extNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    public ExtendedNegotiation removeExtendedNegotiationFor(String cuid) {
        return extNegMap.remove(cuid);
    }

    public Collection<CommonExtendedNegotiation> getCommonExtendedNegotiations() {
        return Collections.unmodifiableCollection(commonExtNegMap.values());
    }

    public CommonExtendedNegotiation getCommonExtendedNegotiationFor(String cuid) {
        return commonExtNegMap.get(cuid);
    }

    public CommonExtendedNegotiation addCommonExtendedNegotiation(
            CommonExtendedNegotiation extNeg) {
        return commonExtNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    public CommonExtendedNegotiation removeCommonExtendedNegotiationFor(
            String cuid) {
        return commonExtNegMap.remove(cuid);
    }

    public int length() {
        int len = 68; // Fix AA-RQ/AC PDU fields
        len += 4 + applicationContext.length();
        for (PresentationContext pc : pcs) {
            len += 4 + pc.length();
        }
        len += 4 + userInfoLength();
        return len;
    }

    public int userInfoLength() {
        int len = 8; // Max Length Sub-Item
        len += 4 + implClassUID.length();
        if (isAsyncOps())
            len += 8; // Asynchronous Operations Window Sub-Item
        for (RoleSelection rs : roleSelMap.values()) {
            len += 4 + rs.length();
        }
        if (implVersionName != null)
            len += 4 + implVersionName.length();
        for (ExtendedNegotiation en : extNegMap.values()) {
            len += 4 + en.length();
        }
        for (CommonExtendedNegotiation cen : commonExtNegMap.values()) {
            len += 4 + cen.length();
        }
        return len;
    }

    private static StringBuffer promptUID(String uid, StringBuffer sb) {
        return sb.append(uid).append(" - ").append(
                UIDDictionary.getDictionary().nameOf(uid));
    }

    protected String toString(String type) {
        StringBuffer sb = new StringBuffer(512);
        sb.append(type);
        sb.append("[\n  calledAET = ").append(calledAET);
        sb.append("\n  callingAET = ").append(callingAET);
        sb.append("\n  applicationContext = ");
        promptUID(applicationContext, sb);
        sb.append("\n  implClassUID = ").append(implClassUID);
        sb.append("\n  implVersionName = ").append(implVersionName);
        sb.append("\n  maxPDULength = ").append(maxPDULength);
        sb.append("\n  maxOpsInvoked/maxOpsPerformed = ").append(maxOpsInvoked)
                .append("/").append(maxOpsPerformed);
        appendUserIdentity(sb);
        promptPresentationContext(sb);
        promptRoleSelection(sb);
        promptExtendedNegotiation(sb);
        promptCommonExtendedNegotiation(sb);
        sb.append("\n]");
        return sb.toString();
    }

    protected abstract void appendUserIdentity(StringBuffer sb);

    private void promptPresentationContext(StringBuffer sb) {
        ArrayList<PresentationContext> tmp = new ArrayList<PresentationContext>(pcs);
        for (int i = 0, n = tmp.size(); i < n; ++i) {
            sb.append("\n  ");
            tmp.get(i).toStringBuffer(sb);
        }
    }

    private void promptRoleSelection(StringBuffer sb) {
        ArrayList<RoleSelection> tmp = new ArrayList<RoleSelection>(roleSelMap.values());
        final int n = tmp.size();
        sb.append("\n  Role Selection(").append(n).append("):");
        for (int i = 0; i < n; ++i) {
            RoleSelection rs = tmp.get(i);
            sb.append("\n    ");
            promptUID(rs.getSOPClassUID(), sb);
            sb.append("\n      SCU/SCP = ");
            sb.append(rs.isSCU()).append("/").append(rs.isSCP());
        }
    }

    private static void promptBytes(byte[] b, StringBuffer sb) {
        for (int i = 0; i < b.length; i++) {
            StringUtils.byteToHex(b[i], sb);
            sb.append(' ');
        }
    }

    private void promptExtendedNegotiation(StringBuffer sb) {
        ArrayList<ExtendedNegotiation> tmp = new ArrayList<ExtendedNegotiation>(extNegMap.values());
        final int n = tmp.size();
        sb.append("\n  Extended Negotiation(").append(n).append("):");
        for (int i = 0; i < n; ++i) {
            ExtendedNegotiation extNeg = tmp.get(i);
            sb.append("\n    ");
            promptUID(extNeg.getSOPClassUID(), sb);
            sb.append("\n      info = ");
            promptBytes(extNeg.getInformation(), sb);
        }
    }

    private void promptCommonExtendedNegotiation(StringBuffer sb) {
        ArrayList<CommonExtendedNegotiation> tmp = new ArrayList<CommonExtendedNegotiation>(
                commonExtNegMap.values());
        final int n = tmp.size();
        sb.append("\n  Common Extended Negotiation(").append(n).append("):");
        for (int i = 0; i < n; ++i) {
            CommonExtendedNegotiation extNeg = tmp.get(i);
            sb.append("\n    ");
            promptUID(extNeg.getSOPClassUID(), sb);
            sb.append("\n      serviceClass = ");
            promptUID(extNeg.getServiceClassUID(), sb);
            ArrayList<String> uids = new ArrayList<String>(extNeg
                    .getRelatedGeneralSOPClassUIDs());
            if (uids.isEmpty())
                return;

            final int m = uids.size();
            sb.append("\n      Related General SOP Classes(").append(m).append(
                    "):");
            for (int j = 0; j < m; j++) {
                sb.append("\n      ");
                promptUID(uids.get(j), sb);
            }
        }
    }

}
