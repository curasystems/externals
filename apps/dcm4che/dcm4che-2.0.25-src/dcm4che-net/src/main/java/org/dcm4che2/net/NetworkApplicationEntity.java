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

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.ExtendedNegotiation;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.pdu.RoleSelection;
import org.dcm4che2.net.service.DicomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DICOM Part 15, Annex H compliant description of a DICOM network service.
 * <p>
 * A Network AE is an application entity that provides services on a network. A
 * Network AE will have the 16 same functional capability regardless of the
 * particular network connection used. If there are functional differences based
 * on selected network connection, then these are separate Network AEs. If there
 * are 18 functional differences based on other internal structures, then these
 * are separate Network AEs.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 12489 $ $Date: 2007-11-23 06:23:46 -0600 (Fri, 23 Nov
 *          2007) $
 * @since Nov 25, 2005
 * 
 */
public class NetworkApplicationEntity {

    private static Logger log = LoggerFactory
            .getLogger(NetworkApplicationEntity.class);

    private boolean associationAcceptor;

    private boolean associationInitiator;

    private String aeTitle;

    private String description;

    private Object[] vendorData = {};

    private String[] applicationCluster = {};

    private String[] preferredCallingAETitle = {};

    private String[] preferredCalledAETitle = {};

    private String[] supportedCharacterSet = {};

    private Boolean installed;

    private int maxOpsInvoked;

    private int maxOpsPerformed;

    private int maxPDULengthReceive = 0x4000; // =16384

    private int maxPDULengthSend = 0x4000;

    private boolean packPDV;

    private int dimseRspTimeout = 10000;

    private int retrieveRspTimeout = 600000;

    private int idleTimeout = 60000;

    private List<String> reuseAssocationToAETitle = Collections.emptyList();

    private List<String> reuseAssocationFromAETitle = Collections.emptyList();

    private NetworkConnection[] networkConnection = {};

    private TransferCapability[] transferCapability = {};

    private boolean offerDefaultTSInSeparatePC;

    private UserIdentity userIdentity;

    private Device device;

    private AssociationListener[] als = {};

    private final DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();

    private final List<Association> pool = new ArrayList<Association>();

    private AtomicInteger messageID = new AtomicInteger();

    /**
     * Get the device that is identified by this application entity.
     * 
     * @return The owning <code>Device</code>.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Set the device that is identified by this application entity.
     * 
     * @param device
     *                The owning <code>Device</code>.
     */
    void setDevice(Device device) {
        this.device = device;
    }

    /**
     * Get the AE title for this Network AE.
     * 
     * @return A String containing the AE title.
     */
    public String getAETitle() {
        return aeTitle;
    }

    /**
     * Set the AE title for this Network AE.
     * 
     * @param aetitle
     *                A String containing the AE title.
     */
    public void setAETitle(String aetitle) {
        this.aeTitle = aetitle;
    }

    /**
     * Get the locally defined names for a subset of related applications. E.g.
     * neuroradiology.
     * 
     * @return A String array containing the names.
     */
    public String[] getApplicationCluster() {
        return applicationCluster;
    }

    /**
     * Set the locally defined names for a subset of related applications. E.g.
     * neuroradiology.
     * 
     * @param cluster
     *                A String array containing the names.
     */
    public void setApplicationCluster(String[] cluster) {
        this.applicationCluster = cluster;
    }

    /**
     * Determine whether or not this network AE can accept associations.
     * 
     * @return A boolean value. True if the Network AE can accept associations,
     *         false otherwise.
     */
    public boolean isAssociationAcceptor() {
        return associationAcceptor;
    }

    /**
     * Set whether or not this network AE can accept associations.
     * 
     * @param acceptor
     *                A boolean value. True if the Network AE can accept
     *                associations, false otherwise.
     */
    public void setAssociationAcceptor(boolean acceptor) {
        this.associationAcceptor = acceptor;
    }

    /**
     * Determine whether or not this network AE can initiate associations.
     * 
     * @return A boolean value. True if the Network AE can accept associations,
     *         false otherwise.
     */
    public boolean isAssociationInitiator() {
        return associationInitiator;
    }

    /**
     * Set whether or not this network AE can initiate associations.
     * 
     * @param initiator
     *                A boolean value. True if the Network AE can accept
     *                associations, false otherwise.
     */
    public void setAssociationInitiator(boolean initiator) {
        this.associationInitiator = initiator;
    }

    /**
     * Get the description of this network AE
     * 
     * @return A String containing the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set a description of this network AE.
     * 
     * @param description
     *                A String containing the description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Determine whether or not this network AE is installed on a network.
     * 
     * @return A Boolean value. True if the AE is installed on a network. If not
     *         present, information about the installed status of the AE is
     *         inherited from the device
     */
    public boolean isInstalled() {
        return installed != null ? installed.booleanValue() : device == null
                || device.isInstalled();
    }

    /**
     * Set whether or not this network AE is installed on a network.
     * 
     * @param installed
     *                A Boolean value. True if the AE is installed on a network.
     *                If not present, information about the installed status of
     *                the AE is inherited from the device
     */
    public void setInstalled(boolean installed) {
        this.installed = Boolean.valueOf(installed);
    }

    /**
     * Get the <code>NetworkConnection</code> objects associated with this
     * network AE.
     * 
     * @return An array of <code>NetworkConnection</code> objects.
     */
    public NetworkConnection[] getNetworkConnection() {
        return networkConnection;
    }

    /**
     * Set the <code>NetworkConnection</code> object associated with this
     * network AE.
     * 
     * @param nc
     *                A <code>NetworkConnection</code> object.
     */
    public void setNetworkConnection(NetworkConnection nc) {
        setNetworkConnection(new NetworkConnection[] { nc });
    }

    /**
     * Set the <code>NetworkConnection</code> objects associated with this
     * network AE.
     * 
     * @param nc
     *                An array of <code>NetworkConnection</code> objects.
     */
    public void setNetworkConnection(NetworkConnection[] nc) {
        this.networkConnection = nc;
    }

    /**
     * Get the AE Title(s) (SCPs) that are preferred for initiating associations
     * from this network AE (SCU).
     * 
     * @return A String array of the preferred called AE titles.
     */
    public String[] getPreferredCalledAETitle() {
        return preferredCalledAETitle;
    }

    /**
     * Determine whether or not this network AE (SCU) has an AE Title (SCP) that
     * is preferred for initiating associations to.
     * 
     * @return A boolean value. True if there is a preferred called AE title.
     */
    public boolean hasPreferredCalledAETitle() {
        return preferredCalledAETitle != null
                && preferredCalledAETitle.length > 0;
    }

    /**
     * Determine whether or not the parameter is a preferred called AE title
     * (SCP) for this network AE (SCU). The called AE title defines authorized
     * SCPs that this network AE title may interact with.
     * 
     * @param aet
     *                A String containing the AE title to test.
     * @return A boolean value. True if the parameter is a preferred called AE
     *         title.
     */
    public boolean isPreferredCalledAETitle(String aet) {
        return contains(preferredCalledAETitle, aet);
    }

    private static boolean contains(String[] a, String s) {
        for (int i = 0; i < a.length; i++)
            if (s.equals(a[i]))
                return true;
        return false;
    }

    /**
     * Set the AE title(s) (SCPs) that are preferred for initiating associations
     * from this network AE (SCU). The called AE title(s) defines authorized
     * SCPs that this network AE title may interact with.
     * 
     * @param aets
     *                A String array containing the preferred called AE titles.
     */
    public void setPreferredCalledAETitle(String[] aets) {
        this.preferredCalledAETitle = aets;
    }

    /**
     * Get the AE title(s) that are preferred for accepting associations from.
     * The calling AE title(s) defines authorized SCUs that this network AE
     * title may interact with, but does not prohibit other SCUs from making
     * associations.
     * 
     * @return A String array containing the preferred calling AE titles.
     */
    public String[] getPreferredCallingAETitle() {
        return preferredCallingAETitle;
    }

    /**
     * Determine whether or not this network AE has a preferred calling AE
     * title. The calling AE title(s) defines authorized SCUs that this network
     * AE title may interact with, but does not prohibit other SCUs from making
     * associations.
     * 
     * @return A boolean value. True if there is a preferred AE title.
     */
    public boolean hasPreferredCallingAETitle() {
        return preferredCallingAETitle != null
                && preferredCallingAETitle.length > 0;
    }

    /**
     * Determine whether or not the parameter is a preferred calling AE title
     * for accepting associations from. The calling AE title(s) defines
     * authorized SCUs that this network AE title may interact with, but does
     * not prohibit other SCUs from making associations.
     * 
     * @param aet
     *                A String containing the AE title to test.
     * @return A boolean value. True if the parameter is a preferred calling AE
     *         title.
     */
    public boolean isPreferredCallingAETitle(String aet) {
        return contains(preferredCallingAETitle, aet);
    }

    /**
     * Set the AE title(s) that are preferred for accepting associations from.
     * The calling AE title(s) defines authorized SCUs that this network AE
     * title may interact with, but does not prohibit other SCUs from making
     * associations.
     * 
     * @param aets
     *                A String array containing the preferred calling AE titles.
     */
    public void setPreferredCallingAETitle(String[] aets) {
        this.preferredCallingAETitle = aets;
    }

    /**
     * Get the Character Set(s) supported by the Network AE for data sets it
     * receives. The value shall be selected from the Defined Terms for Specific
     * Character Set (0008,0005) in PS3.3. If no values are present, this
     * implies that the Network AE supports only the default character
     * repertoire (ISO IR 6).
     * 
     * @return A String array of the supported character sets.
     */
    public String[] getSupportedCharacterSet() {
        return supportedCharacterSet;
    }

    /**
     * Set the Character Set(s) supported by the Network AE for data sets it
     * receives. The value shall be selected from the Defined Terms for Specific
     * Character Set (0008,0005) in PS3.3. If no values are present, this
     * implies that the Network AE supports only the default character
     * repertoire (ISO IR 6).
     * 
     * @param characterSets
     *                A String array of the supported character sets.
     */
    public void setSupportedCharacterSet(String[] characterSets) {
        this.supportedCharacterSet = characterSets;
    }

    /**
     * Get the transfer capabilities (presentation contexts, extended
     * information, etc.) that this network AE may make use of.
     * 
     * @return An array of <code>TransferCapability</code> objects.
     */
    public TransferCapability[] getTransferCapability() {
        return transferCapability;
    }

    /**
     * Set the transfer capabilities (presentation contexts, extended
     * information, etc.) that this network AE may make use of.
     * 
     * @param transferCapability
     *                An array of <code>TransferCapability</code> objects.
     */
    public void setTransferCapability(
            TransferCapability[] transferCapability) {
        this.transferCapability = transferCapability;
    }

    public boolean isOfferDefaultTransferSyntaxInSeparatePresentationContext() {
        return offerDefaultTSInSeparatePC;
    }

    public void setOfferDefaultTransferSyntaxInSeparatePresentationContext(
            boolean offerDefaultTSInSeparatePC) {
        this.offerDefaultTSInSeparatePC = offerDefaultTSInSeparatePC;
    }

    /**
     * Get any vendor information or configuration specific to this network AE.
     * 
     * @return An Object array of the vendor data.
     */
    public Object[] getVendorData() {
        return vendorData;
    }

    /**
     * Set any vendor information or configuration specific to this network AE
     * 
     * @param vendorData
     *                An Object array of the vendor data.
     */
    public void setVendorData(Object[] vendorData) {
        this.vendorData = vendorData;
    }

    /**
     * Get maximum number of outstanding operations this network AE may invoke
     * asynchronously as an SCU. Default is 0 (unlimited).
     * 
     * @return An int value containing the max ops.
     */
    public int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    /**
     * Set maximum number of outstanding operations this network AE may invoke
     * asynchronously as an SCU. Default is 0 (unlimited).
     * 
     * @param maxOpsInvoked
     *                An int value containing the max ops.
     */
    public void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    /**
     * Get the maximum number of operation this network AE may perform
     * asynchronously as an SCP. Default is 0 (unlimited).
     * 
     * @return An int value containing the max ops.
     */
    public int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    /**
     * Set the maximum number of operation this network AE may perform
     * asynchronously as an SCP. Default is 0 (unlimited).
     * 
     * @param maxOpsPerformed
     *                An int value containing the max ops.
     */
    public void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    /**
     * Determine whether or not this network AE is capable of asynchronous
     * operations.
     * 
     * @return A boolean value. True if this network AE is capable of async
     *         operations.
     */
    public boolean isAsyncOps() {
        return maxOpsInvoked != 1 || maxOpsPerformed != 1;
    }

    /**
     * Get the maximum length (in bytes) of P-DATA-TF PDUs that can be received.
     * Defaults to 16364.
     * 
     * @return An int signifying the max PDU length.
     */
    public int getMaxPDULengthReceive() {
        return maxPDULengthReceive;
    }

    /**
     * Get the maximum length (in bytes) of P-DATA-TF PDUs that can be received.
     * Defaults to 16364.
     * 
     * @param maxPDULengthReceive
     *                An int signifying the max PDU length.
     */
    public void setMaxPDULengthReceive(int maxPDULengthReceive) {
        this.maxPDULengthReceive = maxPDULengthReceive;
    }

    /**
     * Set the maximum length (in bytes) of P-DATA-TF PDUs that will be sent.
     * Defaults to 16364.
     * 
     * @return An int signifying the max PDU length.
     */
    public int getMaxPDULengthSend() {
        return maxPDULengthSend;
    }

    /**
     * Get the maximum length (in bytes) of P-DATA-TF PDUs that will be sent.
     * Defaults to 16364.
     * 
     * @param maxPDULengthSend
     *                An int signifying the max PDU length.
     */
    public void setMaxPDULengthSend(int maxPDULengthSend) {
        this.maxPDULengthSend = maxPDULengthSend;
    }

    /**
     * Get whether or not this network AE will send only one PDV in one
     * P-Data-TF PDU. Defaults to false.
     * 
     * @return A boolean value. If true, this network AE will pack command and
     *         data PDV in one P-DATA-TF PDU when sending.
     */
    public boolean isPackPDV() {
        return packPDV;
    }

    /**
     * Set whether or not to send only one PDV in one P-Data-TF PDU. Defaults to
     * false.
     * 
     * @param packPDV
     *                A boolean value. If true, this network AE will pack
     *                command and data PDV in one P-DATA-TF PDU when sending.
     */
    public void setPackPDV(boolean packPDV) {
        this.packPDV = packPDV;
    }

    /**
     * Get the timeout in milliseconds for receiving DIMSE-RSP on an open
     * association. Default 60 seconds (60000 milliseconds).
     * 
     * @return An int value signifying the timeout in milliseconds.
     */
    public int getDimseRspTimeout() {
        return dimseRspTimeout;
    }

    /**
     * Set the timeout in milliseconds for receiving DIMSE-RSP on an open
     * association. Default 60 seconds (60000 milliseconds).
     * 
     * @param dimseRspTimeout
     *                An int value signifying the timeout in milliseconds.
     */
    public void setDimseRspTimeout(int dimseRspTimeout) {
        this.dimseRspTimeout = dimseRspTimeout;
    }

    /**
     * Get the maximum time in milliseconds that an association may remain idle.
     * Default 60 seconds (60000 milliseconds).
     * 
     * @return An int value signifying the max idle period in milliseconds.
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Set the maximum time in milliseconds that an association may remain idle.
     * Default 60 seconds (60000 milliseconds).
     * 
     * @param idleTimeout
     *                An int value signifying the max idle period in
     *                milliseconds.
     */
    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * Get the timeout in milliseconds for receiving DIMSE-RSP for an open C-GET
     * or C-MOVE request. Other types of associations use the DimseRspTimeout.
     * Default 600 seconds (600000 milliseconds).
     * 
     * @return An int value signifying the timeout in milliseconds.
     * @deprecated use {@link #getRetrieveRspTimeout}
     */
    @Deprecated
    public int getMoveRspTimeout() {
        return retrieveRspTimeout;
    }

    /**
     * Set the timeout in milliseconds for receiving DIMSE-RSP for an open C-GET
     * or C-MOVE request. Other types of associations use the DimseRspTimeout.
     * Default 600 seconds (600000 milliseconds).
     * 
     * @param moveRspTimeout
     *                An int value signifying the timeout in milliseconds.
     * @deprecated use {@link #setRetrieveRspTimeout}
     */
    @Deprecated
    public void setMoveRspTimeout(int moveRspTimeout) {
        this.retrieveRspTimeout = moveRspTimeout;
    }

    /**
     * Get the timeout in milliseconds for receiving DIMSE-RSP for an open C-GET
     * or C-MOVE request. Other types of associations use the DimseRspTimeout.
     * Default 600 seconds (600000 milliseconds).
     * 
     * @return An int value signifying the timeout in milliseconds.
     */
    public int getRetrieveRspTimeout() {
        return retrieveRspTimeout;
    }

    /**
     * Set the timeout in milliseconds for receiving DIMSE-RSP on for open C-GET
     * or C-MOVE request. Other types of associations use the DimseRspTimeout.
     * Default 600 seconds (600000 milliseconds).
     * 
     * @param retrieveRspTimeout
     *                An int value signifying the timeout in milliseconds.
     */
    public void setRetrieveRspTimeout(int retrieveRspTimeout) {
        this.retrieveRspTimeout = retrieveRspTimeout;
    }

    /**
     * Get an array of AE titles. If there is an open association from an AE
     * title (the SCU) contained in this array, this network AE (the SCP) will
     * reuse an existing association object as opposed to creating a new one.
     * 
     * @return A String array containing the AE titles that association reuse
     *         will be enabled for.
     */
    public String[] getReuseAssocationFromAETitle() {
        return reuseAssocationFromAETitle
                .toArray(new String[reuseAssocationFromAETitle.size()]);
    }

    /**
     * Set an array of AE titles. If there is an open association from an AE
     * title (the SCU) contained in this array, this network AE (the SCP) will
     * reuse an existing association object as opposed to creating a new one.
     * 
     * @param reuseAssocationFromAETitle
     *                String array containing the AE titles that association
     *                reuse will be enabled for.
     */
    public void setReuseAssocationFromAETitle(
            String[] reuseAssocationFromAETitle) {
        this.reuseAssocationFromAETitle = Arrays
                .asList(reuseAssocationFromAETitle);
    }

    /**
     * Get an array of AE titles. If there is an open association to an AE title
     * (the SCP) contained in this array, this network AE (the SCU) will reuse
     * an existing association object as opposed to creating a new one.
     * 
     * @return String array containing the AE titles that association reuse will
     *         be enabled for.
     */
    public String[] getReuseAssocationToAETitle() {
        return reuseAssocationToAETitle
                .toArray(new String[reuseAssocationToAETitle.size()]);
    }

    /**
     * Get an array of AE titles. If there is an open association to an AE title
     * (the SCP) contained in this array, this network AE (the SCU) will reuse
     * an existing association object as opposed to creating a new one.
     * 
     * @param reuseAssocationToAETitle
     *                String array containing the AE titles that association
     *                reuse will be enabled for.
     */
    public void setReuseAssocationToAETitle(
            String[] reuseAssocationToAETitle) {
        this.reuseAssocationToAETitle = Arrays.asList(reuseAssocationToAETitle);

    }

    /**
     * Get User Identity used for initiating associations, if no User Identity
     * is specified in the connect method. Returns <code>null</code>, if no
     * User Identity is associated with this NetworkApplicationEntity.
     * 
     * @return User Identity associated with this NetworkApplicationEntity.
     * 
     * @see #setUserIdentity
     * @see #connect(NetworkApplicationEntity, Executor)
     * @see #connect(NetworkApplicationEntity, Executor, boolean)
     */
    public UserIdentity getUserIdentity() {
        return userIdentity;
    }

    /**
     * Set User Identity used for initiating associations, if no User Identity
     * is specified in the connect method.
     * 
     * @param User
     *                Identity associated with this NetworkApplicationEntity.
     * 
     * @see #getUserIdentity
     * @see #connect(NetworkApplicationEntity, Executor)
     * @see #connect(NetworkApplicationEntity, Executor, boolean)
     */
    public void setUserIdentity(UserIdentity userIdentity) {
        this.userIdentity = userIdentity;
    }

    /**
     * Open a connection to the remote AE, using the passed in threading model.
     * If a User Identity was associated with this NetworkApplicationEntity by
     * {@link #setUserIdentity(UserIdentity)}, corresponding User Identity
     * Negotiation will be performed. This method will result in an association
     * being opened (or re-used if so configured). This association is then
     * returned for use.
     * 
     * @param remoteAE
     *                A <code>NetworkApplicationEntity</code> to connect to.
     * @param executor
     *                An <code>Executor</code> implementation containing the
     *                threading model to use for this connection/association.
     * @return An open <code>Association</code> object.
     * @throws ConfigurationException
     *                 If there is no compatible network connection between this
     *                 AE title and the one that it is connecting to.
     * @throws IOException
     * @throws InterruptedException
     */
    public Association connect(NetworkApplicationEntity remoteAE,
            Executor executor) throws ConfigurationException, IOException,
            InterruptedException {
        return connect(userIdentity, remoteAE, executor, false);
    }

    /**
     * Open a connection to the remote AE, using the passed in user identity
     * Negotiation will be performed. This method will result in an association
     * being opened (or re-used if so configured). This association is then
     * returned for use.
     * 
     * @param userIdentity
     *                The <code>UserIdentity</code> notified to the
     *                Association Acceptor by User Identity Negotiation.
     * @param remoteAE
     *                A <code>NetworkApplicationEntity</code> to connect to.
     * @param executor
     *                An <code>Executor</code> implementation containing the
     *                threading model to use for this connection/association.
     * @return An open <code>Association</code> object.
     * @throws ConfigurationException
     *                 If there is no compatible network connection between this
     *                 AE title and the one that it is connecting to.
     * @throws IOException
     * @throws InterruptedException
     */
    public Association connect(UserIdentity userIdentity,
            NetworkApplicationEntity remoteAE, Executor executor)
            throws ConfigurationException, IOException, InterruptedException {
        return connect(userIdentity, remoteAE, executor, false);
    }

    /**
     * Open a connection to the remote AE, using the passed in threading model.
     * If a User Identity was associated with this NetworkApplicationEntity by
     * {@link #setUserIdentity(UserIdentity)}, corresponding User Identity
     * Negotiation will be performed. This method will result in an association
     * being opened (or reused if so configured, and the "forceNew" parameter is
     * false). This association is then returned for use.
     * 
     * @param remoteAE
     *                A <code>NetworkApplicationEntity</code> to connect to.
     * @param executor
     *                An <code>Executor</code> implementation containing the
     *                threading model to use for this connection/association.
     * @param forceNew
     *                A boolean value. If true, always create a new association,
     *                ignoring any existing association re-use configuration
     *                that has been set.
     * @return An open <code>Association</code> object.
     * @throws ConfigurationException
     *                 If there is no compatible network connection between this
     *                 AE title and the one that it is connecting to.
     * @throws IOException
     * @throws InterruptedException
     */
    public Association connect(NetworkApplicationEntity remoteAE,
            Executor executor, boolean forceNew) throws ConfigurationException,
            IOException, InterruptedException {
        return connect(userIdentity, remoteAE, executor, forceNew);
    }

    /**
     * Open a connection to the remote AE, using the passed in user identity and
     * threading model. This method will result in an association being opened
     * (or reused if so configured, and the "forceNew" parameter is false). This
     * association is then returned for use.
     * 
     * @param userIdentity
     *                The <code>UserIdentity</code> notified to the
     *                Association Acceptor by User Identity Negotiation.
     * @param remoteAE
     *                A <code>NetworkApplicationEntity</code> to connect to.
     * @param executor
     *                An <code>Executor</code> implementation containing the
     *                threading model to use for this connection/association.
     * @param forceNew
     *                A boolean value. If true, always create a new association,
     *                ignoring any existing association re-use configuration
     *                that has been set.
     * @return An open <code>Association</code> object.
     * @throws ConfigurationException
     *                 If there is no compatible network connection between this
     *                 AE title and the one that it is connecting to.
     * @throws IOException
     * @throws InterruptedException
     */
    public Association connect(UserIdentity userIdentity,
            NetworkApplicationEntity remoteAE, Executor executor,
            boolean forceNew) throws ConfigurationException, IOException,
            InterruptedException {
        final String remoteAET = remoteAE.getAETitle();
        if (!forceNew
                && !pool.isEmpty()
                && !(reuseAssocationToAETitle.isEmpty() && reuseAssocationFromAETitle
                        .isEmpty())) {
            synchronized (pool) {
                for (Association as : pool) {
                    if (!remoteAET.equals(as.getRemoteAET()))
                        continue;
                    if (userIdentity != as.getUserIdentity()) {
                        continue;
                    }
                    if (as.isReadyForDataTransfer()
                            && (as.isRequestor() ? reuseAssocationToAETitle
                                    : reuseAssocationFromAETitle)
                                    .contains(remoteAET))
                        return as;
                }
            }
        }
        NetworkConnection[] remoteConns = remoteAE.getNetworkConnection();
        for (int i = 0; i < networkConnection.length; i++) {
            NetworkConnection c = networkConnection[i];
            if (!networkConnection[i].isInstalled())
                continue;
            for (int j = 0; j < remoteConns.length; j++) {
                NetworkConnection nc = remoteConns[j];
                if (nc.isInstalled() && nc.isListening()
                        && c.isTLS() == nc.isTLS()) {
                    AAssociateRQ rq = makeAAssociateRQ(userIdentity, remoteAE);
                    Socket s = c.connect(nc);
                    Association a = Association.request(s, c, this,
                            userIdentity);
                    executor.execute(a);
                    a.negotiate(rq);
                    addToPool(a);
                    associationAccepted(a);
                    return a;
                }
            }
        }
        throw new ConfigurationException(
                "No compatible Network Connection between local AE " + aeTitle
                        + " and remote AE " + remoteAET);
    }

    protected AAssociateRQ makeAAssociateRQ(UserIdentity userIdentity,
            NetworkApplicationEntity remoteAE) {
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.setCallingAET(aeTitle);
        aarq.setCalledAET(remoteAE.getAETitle());
        aarq.setMaxPDULength(maxPDULengthReceive);
        aarq.setMaxOpsInvoked(minZeroAsMax(maxOpsInvoked,
                remoteAE.maxOpsPerformed));
        aarq.setMaxOpsPerformed(minZeroAsMax(maxOpsPerformed,
                remoteAE.maxOpsInvoked));

        Map<String, Collection<String>> cuid2ts = 
                new LinkedHashMap<String, Collection<String>>();
        Collection<String> scu = new HashSet<String>();
        Collection<String> scp = new HashSet<String>();
        evaluateTC(aarq, cuid2ts, scu, scp, remoteAE.getTransferCapability());
        if (cuid2ts.isEmpty()) {
            log.info("No common Transfer Capability between local AE "
                    + getAETitle() + " and remote AE " + remoteAE.getAETitle());
            PresentationContext pctx = new PresentationContext();
            pctx.setPCID(aarq.nextPCID());
            pctx.setAbstractSyntax(UID.VerificationSOPClass);
            pctx.addTransferSyntax(UID.ImplicitVRLittleEndian);
            aarq.addPresentationContext(pctx);
            return aarq;
        }
        initPCs(aarq, cuid2ts);
        if (!cuid2ts.isEmpty()) {
            log.info("Maximum number (128) of offered Presentation Context reached"
                    + " -  cannot offer all Transfer Capabilities in A-ASSOCIATE-RQ");
        }
        for (String cuid : scp) {
            aarq.addRoleSelection(new RoleSelection(cuid, scu.contains(cuid),
                    true));
        }
        if (userIdentity != null) {
            aarq.setUserIdentity(userIdentity.getUserIdentityRQ());
        }
        return aarq;
    }

    private void evaluateTC(AAssociateRQ aarq,
            Map<String, Collection<String>> cuid2ts, Collection<String> scu,
            Collection<String> scp, TransferCapability[] remoteTCs) {
        for (int i = 0; i < transferCapability.length; i++) {
            TransferCapability tc = transferCapability[i];
            String cuid = tc.getSopClass();
            LinkedHashSet<String> ts1 = new LinkedHashSet<String>(Arrays
                    .asList(tc.getTransferSyntax()));
            if (remoteTCs.length != 0) {
                TransferCapability remoteTC = findTC(remoteTCs, cuid, tc
                        .isSCU());
                if (remoteTC == null) {
                    continue;
                }
                ts1.retainAll(Arrays.asList(remoteTC.getTransferSyntax()));
                if (ts1.isEmpty()) {
                    continue;
                }
            }
            Collection<String> ts = cuid2ts.get(cuid);
            if (ts == null) {
                cuid2ts.put(cuid, ts1);
            }
            else {
                ts.addAll(ts1);
            }
            (tc.isSCP() ? scp : scu).add(cuid);
            byte[] extInfo = tc.getExtInfo();
            if (extInfo.length != 0) {
                ExtendedNegotiation extneg = new ExtendedNegotiation(cuid,
                        extInfo);
                aarq.addExtendedNegotiation(extneg);
            }
        }
    }

    /* Initialize offered Presentation Context in AA-RQ according given
     * Transfer Capabilities, each with only one offered Transfer Syntax.
     * Because the maximal number of offered Presentation Contexts in an AA-RQ
     * is limited to 128, it may be not possible to offer all Transfer Syntaxes
     * of supported Transfer Capabilities in the AA-RQ. */
    private void initPCs(AAssociateRQ aarq,
            Map<String, Collection<String>> cuid2ts) {
        String[] tsdefs = { UID.ImplicitVRLittleEndian, UID.JPEGBaseline1,
                UID.JPEGExtended24, UID.JPEGLossless, UID.JPEGLSLossless };
        while (!cuid2ts.isEmpty() &&
                aarq.getNumberOfPresentationContexts() < 128)
            for (Iterator<Map.Entry<String,Collection<String>>> iter =
                    cuid2ts.entrySet().iterator(); iter.hasNext() &&
                    aarq.getNumberOfPresentationContexts() < 128;) {
                Map.Entry<String, Collection<String>> e = iter.next();
                String cuid = e.getKey();
                Collection<String> ts = e.getValue();
                PresentationContext pctx = new PresentationContext();
                pctx.setPCID(aarq.nextPCID());
                pctx.setAbstractSyntax(cuid);
                pctx.addTransferSyntax(selectTS(ts, tsdefs));
                aarq.addPresentationContext(pctx);
                if (ts.isEmpty())
                    iter.remove();
            }
    }


    private String selectTS(Collection<String> ts, String[] tsdefs) {
        for (String tsdef : tsdefs)
            if (ts.remove(tsdef))
                return tsdef;
        Iterator<String> tsiter = ts.iterator();
        String uid = tsiter.next();
        tsiter.remove();
        return uid;
    }

    private TransferCapability findTC(TransferCapability[] tcs, String cuid,
            boolean scp) {
        TransferCapability tc;
        for (int i = 0; i < tcs.length; i++) {
            tc = tcs[i];
            if (tc.isSCP() == scp && tc.getSopClass().equals(cuid))
                return tc;
        }
        return null;
    }

    /**
     * Register a <code>DicomService</code> with this network AE.
     * 
     * @param service
     *                The <code>DicomService</code> that will respond to DICOM
     *                requests.
     */
    public void register(DicomService service) {
        serviceRegistry.register(service);
    }

    /**
     * Unregister (remove) a <code>DicomService</code> from this network AE.
     * 
     * @param service
     *                The <code>DicomService</code> to unregister.
     */
    public void unregister(DicomService service) {
        serviceRegistry.unregister(service);
    }

    /**
     * Add the given <code>Association</code> object to this network AE's pool
     * of associations.
     * 
     * @param a
     *                The <code>Association</code> to add.
     */
    void addToPool(Association a) {
        synchronized (pool) {
            pool.add(a);
        }
    }

    /**
     * Remove the given <code>Association</code> object from this network AE's
     * pool of associations.
     * 
     * @param a
     *                The <code>Association</code> to remove.
     */
    void removeFromPool(Association a) {
        synchronized (pool) {
            pool.remove(a);
        }
    }

    /**
     * Perform the action associated with the given DICOM command object.
     * 
     * @param as
     *                The <code>Association</code> to perform the operation
     *                within.
     * @param pcid
     *                The presentation context ID for this operation.
     * @param cmd
     *                The <code>DicomObject</code> representing the command to
     *                execute.
     * @param dataStream
     *                The <code>PDVInputStream</code> used to interpret the
     *                incoming/outgoing PDUs.
     * @param tsuid
     *                A String containing the transfer syntax that will be used
     *                in this operation.
     * @throws IOException
     */
    void perform(Association as, int pcid, DicomObject cmd,
            PDVInputStream dataStream, String tsuid) throws IOException {
        serviceRegistry.process(as, pcid, cmd, dataStream, tsuid);
    }

    /**
     * Negotiate a DICOM association as an SCP.
     * 
     * @param a
     *                The <code>Association</code> object containing the SCU
     *                and network information.
     * @param rq
     *                The <code>AAssociationRQ</code> object that was created
     *                when the <code>PDVInputStream</code> sensed an
     *                association request.
     * @return An <code>AAssociateAC</code> response object.
     * @throws AAssociateRJ
     *                 Thrown if the association request is rejected.
     */
    AAssociateAC negotiate(Association a, AAssociateRQ rq) throws AAssociateRJ {
        if (!isAssociationAcceptor())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_NO_REASON_GIVEN);
        String[] calling = getPreferredCallingAETitle();
        if (calling.length != 0
                && Arrays.asList(calling).indexOf(rq.getCallingAET()) == -1)
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLING_AET_NOT_RECOGNIZED);
        if (!isInstalled())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_TRANSIENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_NO_REASON_GIVEN);
        AAssociateAC ac = new AAssociateAC();
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        ac.setMaxPDULength(maxPDULengthReceive);
        ac.setMaxOpsInvoked(minZeroAsMax(rq.getMaxOpsInvoked(),
                maxOpsPerformed));
        ac.setMaxOpsPerformed(minZeroAsMax(rq.getMaxOpsPerformed(),
                maxOpsInvoked));
        Collection<PresentationContext> pcs = rq.getPresentationContexts();
        for (PresentationContext rqpc : pcs) {
            PresentationContext acpc = negPresCtx(rq, ac, rqpc);
            ac.addPresentationContext(acpc);
        }
        return ac;
    }

    private PresentationContext negPresCtx(AAssociateRQ rq, AAssociateAC ac,
            PresentationContext rqpc) {
        String asuid = rqpc.getAbstractSyntax();
        RoleSelection rqrs = rq.getRoleSelectionFor(asuid);
        TransferCapability tcscp = findTC(transferCapability, asuid, true);
        TransferCapability tcscu = findTC(transferCapability, asuid, false);
        RoleSelection acrs = ac.getRoleSelectionFor(asuid);
        if (rqrs != null && acrs == null) {
            boolean scp = rqrs.isSCP() && tcscu != null;
            boolean scu = rqrs.isSCU() && tcscp != null;
            acrs = new RoleSelection(asuid, scu, scp);
            ac.addRoleSelection(acrs);
        }
        TransferCapability tc = rqrs == null || acrs.isSCU() ? tcscp : tcscu;

        PresentationContext acpc = new PresentationContext();
        acpc.setPCID(rqpc.getPCID());
        if (tc != null) {
            Set<String> rqts = rqpc.getTransferSyntaxes();
            String[] acts = tc.getTransferSyntax();
            for (int i = 0; i < acts.length; i++) {
                if (rqts.contains(acts[i])) {
                    acpc.addTransferSyntax(acts[i]);
                    if (ac.getExtendedNegotiationFor(asuid) == null) {
                        ExtendedNegotiation extNeg = tc.negotiate(rq
                                .getExtendedNegotiationFor(asuid));
                        if (extNeg != null)
                            ac.addExtendedNegotiation(extNeg);
                    }
                    return acpc;
                }
            }
            acpc.setResult(PresentationContext.TRANSFER_SYNTAX_NOT_SUPPORTED);
        }
        else {
            acpc.setResult(PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED);
        }
        acpc.addTransferSyntax(rqpc.getTransferSyntax());
        return acpc;
    }

    private int minZeroAsMax(int i1, int i2) {
        return i1 == 0 ? i2 : i2 == 0 ? i1 : Math.min(i1, i2);
    }

    public void addAssociationListener(AssociationListener l) {
        if (l == null) {
            throw new NullPointerException();
        }
        synchronized (als) {
            AssociationListener[] tmp = 
                    new AssociationListener[als.length + 1];
            System.arraycopy(als, 0, tmp, 0, als.length);
            tmp[als.length] = l;
            als = tmp;
        }
    }

    public void removeAssociationListener(AssociationListener l) {
        if (l == null) {
            throw new NullPointerException();
        }
        synchronized (als) {
            for (int i = 0; i < als.length; i++) {
                if (als[i].equals(l)) {
                    AssociationListener[] tmp = 
                            new AssociationListener[als.length - 1];
                    System.arraycopy(als, 0, tmp, 0, i);
                    System.arraycopy(als, i+1, tmp, i, tmp.length - i);
                    als = tmp;
                    return;
                }
            }
        }
    }

    void associationAccepted(Association a) {
        synchronized (als) {
            for (AssociationListener l : als) {
                l.associationAccepted(new AssociationAcceptEvent(this, a));
            }
        }
    }

    void associationClosed(Association a) {
        synchronized (als) {
            for (AssociationListener l : als) {
                l.associationClosed(new AssociationCloseEvent(this, a));
            }
        }
    }

    int nextMessageID() {
        return messageID.incrementAndGet() & 0xFFFF;
    }
}
