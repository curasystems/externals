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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 * Damien Evans <damien.daddy@gmail.com>
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

package org.dcm4che2.net;

import org.dcm4che2.net.pdu.ExtendedNegotiation;

/**
 * DICOM Standard, Part 15, Annex H: Transfer Capability – The description of
 * the SOP classes and syntaxes supported by a Network AE.
 * <p>
 * An instance of the <code>TransferCapability</code> class describes the
 * DICOM transfer capabilities of an SCU or SCP in terms of a single
 * presentation syntax. This includes the role selection (SCU or SCP), the
 * acceptable transfer syntaxes for a given SOP Class, and any extra
 * information.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date: 2009-06-08 07:47:01 +0200 (Mon, 08 Jun 2009) $
 * @since Oct 7, 2005
 */
public class TransferCapability {

    /** String representation of the DICOM SCU role. */
    public static final String SCU = "SCU";

    /** String representation of the DICOM SCP role. */
    public static final String SCP = "SCP";

    private static byte[] NO_EXT_INFO = {};

    protected String commonName;

    protected String sopClass;

    protected boolean scp;

    protected String[] transferSyntax = {};

    protected byte[] extInfo = {};

    /**
     * Default constructor.
     */
    public TransferCapability() {
        // empty
    }

    /**
     * Creates the <code>TransferCapability</code> instance with the specified
     * presentation context..
     * 
     * @param sopClass
     *                A String containing the SOP Class UID.
     * @param transferSyntax
     *                A String array containing the acceptable transfer syntaxes
     *                for <tt>sopClass</tt>.
     * @param role
     *                A String defining the role selection (SCU or SCP) for this
     *                <code>TransferCapability</code>instance
     */
    public TransferCapability(String sopClass, String[] transferSyntax,
            String role) {
        setSopClass(sopClass);
        setTransferSyntax(transferSyntax);
        setRole(role);
    }

    /**
     * Set the name of the Transfer Capability object. Can be a meaningful name
     * or any unique sequence of characters.
     * 
     * @return A String containing the common name.
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * Get the name of the Transfer Capability object. Can be a meaningful name
     * or any unique sequence of characters.
     * 
     * @param commonName
     *                A String containing the common name.
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * Get the role selection for this <code>TransferCapability</code>instance.
     * 
     * @return A String defining the role selection (SCU or SCP) for this
     *         <code>TransferCapability</code>instance
     */
    public String getRole() {
        return scp ? SCP : SCU;
    }

    /**
     * Set the role selection for this <code>TransferCapability</code>instance.
     * 
     * @param role
     *                A String defining the role selection (SCU or SCP) for this
     *                <code>TransferCapability</code>instance
     * @throws IllegalArgumentException
     *                 If the role is not equal to <tt>SCU</tt> or
     *                 <tt>SCP</tt>.
     */
    public void setRole(String role) {
        if (role == null)
            throw new NullPointerException("Role");

        if (role.equals(SCP))
            scp = true;
        else if (role.equals(SCU))
            scp = false;
        else
            throw new IllegalArgumentException("Role:" + role);
    }

    /**
     * Determine if this Transfer Capability object is capable of acting as an
     * SCP.
     * 
     * @return true if SCP is the selected role of this object.
     */
    public boolean isSCP() {
        return scp;
    }

    /**
     * Determine if this Transfer Capability object is capable of acting as an
     * SCU.
     * 
     * @return true if SCU is the selected role of this object.
     */
    public boolean isSCU() {
        return !scp;
    }

    /**
     * Get the SOP Class of this Transfer Capability object.
     * 
     * @return A String containing the SOP Class UID.
     */
    public String getSopClass() {
        return sopClass;
    }

    /**
     * Set the SOP Class of this Transfer Capability object.
     * 
     * @param sopClass
     *                A String containing the SOP Class UID.
     */
    public void setSopClass(String sopClass) {
        if (sopClass == null)
            throw new NullPointerException("sopClass");
        this.sopClass = sopClass;
    }

    /**
     * Get the transfer syntax(es) that may be requested as an SCU or that are
     * offered as an SCP.
     * 
     * @return String array containing the transfer syntaxes.
     */
    public String[] getTransferSyntax() {
        return transferSyntax.clone();
    }

    /**
     * Set the transfer syntax(es) that may be requested as an SCU or that are
     * offered as an SCP.
     * 
     * @param transferSyntax
     *                String array containing the transfer syntaxes.
     */
    public void setTransferSyntax(String[] transferSyntax) {
        if (transferSyntax.length == 0)
            throw new IllegalArgumentException("transferSyntax.length = 0");
        for (int i = 0; i < transferSyntax.length; i++) {
            if (transferSyntax[i] == null) {
                throw new NullPointerException("transferSyntax[" + i + "]");
            }
        }
        this.transferSyntax = transferSyntax.clone();
    }

    public byte[] getExtInfo() {
        return extInfo.clone();
    }

    public void setExtInfo(byte[] info) {
        extInfo = info != null ? (byte[]) info.clone() : NO_EXT_INFO;
    }

    /**
     * @param field
     * @return
     */
    public boolean getExtInfoBoolean(int field) {
        return extInfo != null && extInfo.length > field && extInfo[field] != 0;
    }

    /**
     * @param field
     * @return
     */
    public int getExtInfoInt(int field) {
        return extInfo != null && extInfo.length > field ? extInfo[field] & 0xff
                : 0;
    }

    /**
     * @param field
     * @param b
     */
    public void setExtInfoBoolean(int field, boolean b) {
        setExtInfoInt(field, b ? 1 : 0);
    }

    /**
     * @param field
     * @param value
     */
    public void setExtInfoInt(int field, int value) {
        extInfo[field] = (byte) value;
    }

    /**
     * Negotiate any extended negotiation items for the association.
     * 
     * @param offered
     *                The <code>ExtendedNegotiation</code> that was offered.
     * @return <code>ExtendedNegotiation</code> that was negotiated.
     */
    protected ExtendedNegotiation negotiate(ExtendedNegotiation offered) {
        if (offered == null || extInfo == null)
            return null;
        byte[] info = offered.getInformation();
        for (int i = 0; i < info.length; i++) {
            info[i] &= getExtInfoInt(i);
        }
        return new ExtendedNegotiation(sopClass, info);
    }
}
