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

package org.dcm4che2.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.io.DicomCodingException;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AAssociateRQAC;
import org.dcm4che2.net.pdu.CommonExtendedNegotiation;
import org.dcm4che2.net.pdu.ExtendedNegotiation;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.pdu.RoleSelection;
import org.dcm4che2.net.pdu.UserIdentityAC;
import org.dcm4che2.net.pdu.UserIdentityRQ;
import org.dcm4che2.util.CloseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 11916 $ $Date: 2009-07-10 10:03:03 +0200 (Fri, 10 Jul 2009) $
 * @since Nov 25, 2005
 *
 */
class PDUDecoder extends PDVInputStream
{
    static Logger log = LoggerFactory.getLogger(PDUDecoder.class);
    private static final int DEF_PDU_LEN = 0x4000; // 16KB
    private static final int MAX_PDU_LEN = 0x1000000; // 16MB

    private final Association as;
    private final InputStream in;
    private final Thread th;
    private byte[] buf = new byte[6 + DEF_PDU_LEN];
    private int pos;
    private int pdutype;
    private int pdulen;
    private int pcid = -1;
    private int pdvmch;
    private int pdvend;

    public PDUDecoder(Association as, InputStream in)
    {
        this.as = as;
        this.in = in;
        this.th = Thread.currentThread();
    }

    private void readFully(int off, int len) throws IOException {
        int n = 0;
        while (n < len)
        {
            int count = in.read(buf, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

    private int remaining()
    {
        return pdulen + 6 - pos;
    }

    private boolean hasRemaining()
    {
        return pos < pdulen + 6;
    }
        
    int get()
    {
        if (!hasRemaining())
            throw new IndexOutOfBoundsException();
        return buf[pos++] & 0xFF;
    }

    void get(byte[] b, int off, int len)
    {
        if (len > remaining())
            throw new IndexOutOfBoundsException();
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
    }

    void skip(int len)
    {
        if (len > remaining())
            throw new IndexOutOfBoundsException();
        pos += len;
    }
        
    private int getUnsignedShort()
    {
        return (get() << 8) | get();
    }

    private int getInt()
    {
        return (get() << 24) | (get() << 16) | (get() << 8) | get();
    }
    
    public void nextPDU() throws IOException
    {
        log.debug("{} waiting for PDU", as);
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        readFully(0, 10);
        pos = 0;
        pdutype = get();
        get();
        pdulen = getInt();
        if (pdutype < PDUType.A_ASSOCIATE_RQ || pdutype > PDUType.A_ABORT)
        {
            log.warn(as.toString() + " >> unrecognized PDU[type=" + pdutype + ", len="
                    + (pdulen & 0xFFFFFFFFL) + "]");
            throw new AAbort(AAbort.UL_SERIVE_PROVIDER,
                    AAbort.UNRECOGNIZED_PDU);
        }
        if (log.isDebugEnabled())
            log.debug(as.toString() + " >> PDU[type=" + pdutype + ", len="
                    + (pdulen & 0xFFFFFFFFL) + "]");
        if (pdutype == PDUType.A_ASSOCIATE_RJ
                || pdutype == PDUType.A_RELEASE_RQ 
                || pdutype == PDUType.A_RELEASE_RP
                || pdutype == PDUType.A_ABORT)
        {
            if (pdulen != 4)
            {
                log.warn(as.toString() + ": Invalid length of PDU[type=" + pdutype + "len="
                        + (pdulen & 0xFFFFFFFFL) + "]");
                throw new AAbort(AAbort.UL_SERIVE_PROVIDER,
                        AAbort.INVALID_PDU_PARAMETER_VALUE);
            }
            switch (pdutype)
            {
                case PDUType.A_ASSOCIATE_RJ:
                    get();            
                    as.receivedAssociateRJ(new AAssociateRJ(get(), get(), get()));
                    break;
                case PDUType.A_RELEASE_RQ:
                    as.receivedReleaseRQ();
                    break;
                case PDUType.A_RELEASE_RP:
                    as.receivedReleaseRP();
                    break;
                case PDUType.A_ABORT:
                    get();
                    get();
                    as.receivedAbort(new AAbort(get(), get()));
                    break;
                default:
                    throw new RuntimeException("Unexpected pdutype:" + pdutype);
            }
        } else {
            if (pdulen < 0 || pdulen > MAX_PDU_LEN)
            {
                log.warn(as.toString() + ": Length of PDU[type=" + pdutype + "[len="
                        + (pdulen & 0xFFFFFFFFL) + "] exceeds "
                        + MAX_PDU_LEN + " limit");
                log.warn(as.toString() + ": Length of PDU[type=" + pdutype + "[len="
                        + (pdulen & 0xFFFFFFFFL) + "] exceeds "
                        + MAX_PDU_LEN + " limit");
                throw new AAbort(AAbort.UL_SERIVE_PROVIDER,
                        AAbort.INVALID_PDU_PARAMETER_VALUE);
            }
            if (6 + pdulen > buf.length)
            {
                byte[] tmp = new byte[6 + pdulen];
                System.arraycopy(buf, 0, tmp, 0, 10);
                buf = tmp;
            }
            readFully(10, pdulen - 4);
            switch (pdutype)
            {
                case PDUType.A_ASSOCIATE_RQ:
                    as.receivedAssociateRQ((AAssociateRQ) decode(new AAssociateRQ()));
                    break;
                case PDUType.A_ASSOCIATE_AC:
                    as.receivedAssociateAC((AAssociateAC) decode(new AAssociateAC()));
                    break;
                case PDUType.P_DATA_TF:
                    log.debug("{} >> P-DATA_TF[len={}]", as, new Integer(pdulen));
                    as.receivedPDataTF();
                    break;
                default:
                    throw new RuntimeException("Unexpected pdutype:" + pdutype);
            }
        }
     }

    private byte[] decodeBytes()
    {
        return decodeBytes(getUnsignedShort());
    }

    private byte[] decodeBytes(int len)
    {
        byte[] bs = new byte[len];
        get(bs, 0, len);
        return bs;
    }

    private AAssociateRQAC decode(AAssociateRQAC rqac)
    throws IOException
    {
        try
        {
            rqac.setProtocolVersion(getUnsignedShort());
            get();
            get();
            rqac.setCalledAET(decodeASCIIString(16).trim());
            rqac.setCallingAET(decodeASCIIString(16).trim());
            rqac.setReservedBytes(decodeBytes(32));
            while (pos < pdulen)
                decodeItem(rqac);
            if (pos != pdulen + 6)
            {
                log.warn(as.toString() + ": Invalid length of PDU[type=" + pdutype + ", len="
                        + (pdulen & 0xFFFFFFFFL) + "]");
                throw new AAbort(AAbort.UL_SERIVE_PROVIDER,
                        AAbort.INVALID_PDU_PARAMETER_VALUE);
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            log.warn(as.toString() + ": Invalid length of PDU[type=" + pdutype + ", len="
                    + (pdulen & 0xFFFFFFFFL) + "]");
            throw new AAbort(AAbort.UL_SERIVE_PROVIDER,
                    AAbort.INVALID_PDU_PARAMETER_VALUE);
        }
        return rqac;
    }

    private String decodeASCIIString()
    {
        return decodeASCIIString(getUnsignedShort());
    }

    private String decodeASCIIString(int len)
    {
    	try {
            if (pos + len > pdulen + 6)
                throw new IndexOutOfBoundsException();
            String s;
            // Skip illegal trailing NULL
            int len0 = len;
            while (len0 > 0 && buf[pos + len0 - 1] == 0) {
                len0--;
            }
            s = new String(buf, pos, len0, "US-ASCII");
            pos += len;
            return s;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "unreachable; US-ASCII is always available", e);
        }
    }
    
    private void decodeItem(AAssociateRQAC rqac) throws AAbort
    {
        int itemType = get();
        get(); // skip reserved byte
        int itemLength = getUnsignedShort();
        switch (itemType)
        {
        case ItemType.APP_CONTEXT:
            rqac.setApplicationContext(decodeASCIIString(itemLength));
            break;
        case ItemType.RQ_PRES_CONTEXT:
        case ItemType.AC_PRES_CONTEXT:
            rqac.addPresentationContext(decodePC(itemLength));
            break;
        case ItemType.USER_INFO:
            decodeUserInfo(itemLength, rqac);
            break;
        default:
            skip(itemLength);
        }
    }

    private PresentationContext decodePC(int itemLength)
    {
        PresentationContext pc = new PresentationContext();
        pc.setPCID(get());
        get(); // skip reserved byte
        pc.setResult(get() & 0xff);
        get(); // skip reserved byte
        int endpos = pos + itemLength - 4;
        while (pos < endpos)
            decodePCSubItem(pc);
        return pc;
    }

    private void decodePCSubItem(PresentationContext pc)
    {
        int itemType = get() & 0xff;
        get(); // skip reserved byte
        int itemLength = getUnsignedShort();
        switch (itemType)
        {
        case ItemType.ABSTRACT_SYNTAX:
            pc.setAbstractSyntax(decodeASCIIString(itemLength));
            break;
        case ItemType.TRANSFER_SYNTAX:
            pc.addTransferSyntax(decodeASCIIString(itemLength));
            break;
        default:
            skip(itemLength);
        }
    }

    private void decodeUserInfo(int itemLength, AAssociateRQAC rqac) throws AAbort
    {
        int endpos = pos + itemLength;
        while (pos < endpos)
            decodeUserInfoSubItem(rqac);
    }

    private void decodeUserInfoSubItem(AAssociateRQAC rqac) throws AAbort
    {
        int itemType = get();
        get(); // skip reserved byte
        int itemLength = getUnsignedShort();
        switch (itemType)
        {
        case ItemType.MAX_PDU_LENGTH:
            rqac.setMaxPDULength(getInt());
            break;
        case ItemType.IMPL_CLASS_UID:
            rqac.setImplClassUID(decodeASCIIString(itemLength));
            break;
        case ItemType.ASYNC_OPS_WINDOW:
            rqac.setMaxOpsInvoked(getUnsignedShort());
            rqac.setMaxOpsPerformed(getUnsignedShort());
            break;
        case ItemType.ROLE_SELECTION:
            rqac.addRoleSelection(decodeRoleSelection(itemLength));
            break;
        case ItemType.IMPL_VERSION_NAME:
            rqac.setImplVersionName(decodeASCIIString(itemLength));
            break;
        case ItemType.EXT_NEG:
            rqac.addExtendedNegotiation(decodeExtendedNegotiation(itemLength));
            break;
        case ItemType.COMMON_EXT_NEG:
            rqac.addCommonExtendedNegotiation(decodeCommonExtendedNegotiation(itemLength));
            break;
        case ItemType.RQ_USER_IDENTITY:
        case ItemType.AC_USER_IDENTITY:
            if (rqac instanceof AAssociateRQ) {
                ((AAssociateRQ) rqac).setUserIdentity(decodeUserIdentityRQ(itemLength));               
            } else {
                ((AAssociateAC) rqac).setUserIdentity(decodeUserIdentityAC(itemLength));                
            }
            break;
        default:
            skip(itemLength);
        }
    }

    private RoleSelection decodeRoleSelection(int itemLength)
    {
        RoleSelection rs = new RoleSelection();
        rs.setSOPClassUID(decodeASCIIString());
        rs.setSCU(get() != 0);
        rs.setSCP(get() != 0);
        return rs;
    }

    private ExtendedNegotiation decodeExtendedNegotiation(int itemLength)
    {
        ExtendedNegotiation extNeg = new ExtendedNegotiation();
        int uidLength = getUnsignedShort();
        extNeg.setSOPClassUID(decodeASCIIString(uidLength));
        extNeg.setInformation(decodeBytes(itemLength - uidLength - 2));
        return extNeg;
    }

    private CommonExtendedNegotiation decodeCommonExtendedNegotiation(int itemLength)
    throws AAbort
    {
        int endPos = pos + itemLength;
        CommonExtendedNegotiation extNeg = new CommonExtendedNegotiation();
        extNeg.setSOPClassUID(decodeASCIIString(getUnsignedShort()));
        extNeg.setServiceClassUID(decodeASCIIString(getUnsignedShort()));
        decodeRelatedGeneralSOPClassUIDs(getUnsignedShort(), extNeg);
        if (pos != endPos)
        {
            log.warn(as.toString() + ": Mismatch of encoded (" + itemLength 
                    + ") with actual (" + (itemLength + pos - itemLength)
                    + ") Common Extended Negotiation item length");
            throw new AAbort(AAbort.UL_SERIVE_PROVIDER,
                    AAbort.INVALID_PDU_PARAMETER_VALUE);
        }
        return extNeg;
    }

    private void decodeRelatedGeneralSOPClassUIDs(int totlen,
            CommonExtendedNegotiation extNeg)
    {
        int endPos = pos + totlen;
        while (pos < endPos)
            extNeg.addRelatedGeneralSOPClassUID(decodeASCIIString());
    }

    private UserIdentityRQ decodeUserIdentityRQ(int itemLength)
    throws AAbort
    {
        int endPos = pos + itemLength;
        UserIdentityRQ user = new UserIdentityRQ();
        user.setUserIdentityType(get() & 0xff);
        user.setPositiveResponseRequested(get() != 0);
        user.setPrimaryField(decodeBytes());
        user.setSecondaryField(decodeBytes());
        if (pos != endPos)
        {
            log.warn(as.toString() + ": Mismatch of encoded (" + itemLength 
                    + ") with actual (" + (itemLength + pos - itemLength)
                    + ") User Identity item length");
            throw new AAbort(AAbort.UL_SERIVE_PROVIDER,
                    AAbort.INVALID_PDU_PARAMETER_VALUE);
        }
        return user;
    }

    private UserIdentityAC decodeUserIdentityAC(int itemLength)
            throws AAbort
    {
        int endPos = pos + itemLength;
        UserIdentityAC user = new UserIdentityAC();
        user.setServerResponse(decodeBytes());
        if (pos != endPos)
        {
            log.warn(as.toString() + ": Mismatch of encoded (" + itemLength 
                    + ") with actual (" + (itemLength + pos - itemLength)
                    + ") User Identity item length");
            throw new AAbort(AAbort.UL_SERIVE_PROVIDER,
                    AAbort.INVALID_PDU_PARAMETER_VALUE);
        }
        return user;
    }

    
    public void decodeDIMSE() throws IOException
    {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (pcid != - 1)
            return; // already inside decodeDIMSE
        
        nextPDV(PDVType.COMMAND, -1);
        
        PresentationContext pc = as.getAssociateAC().getPresentationContext(pcid);
        if (pc == null)
        {
            log.warn(as.toString() + ": No Presentation Context with given ID - " + pcid);
            throw new AAbort();
        }
        if (!pc.isAccepted())
        {
            log.warn(as.toString() + ": No accepted Presentation Context with given ID - " + pcid);
            throw new AAbort();
        }
        String tsuid = pc.getTransferSyntax();
        DicomObject cmd = readDicomObject(TransferSyntax.ImplicitVRLittleEndian);
        if (log.isInfoEnabled())
            log.info(as.toString() + " >> " + CommandUtils.toString(cmd, pcid, tsuid));
        if (log.isDebugEnabled())
            log.debug("Command:\n" + cmd);
        if (CommandUtils.hasDataset(cmd))
        {
            nextPDV(PDVType.DATA, pcid);
            if (CommandUtils.isResponse(cmd))
            {
                DicomObject data = readDicomObject(TransferSyntax.valueOf(tsuid));
                if (log.isDebugEnabled())
                    log.debug("Dataset:\n" + data);
                as.onDimseRSP(cmd, data);
            }
            else
            {
                as.onDimseRQ(pcid, cmd, this, tsuid);
                long skipped = skipAll();
                if (log.isDebugEnabled() && skipped > 0)
                    log.debug(as.toString() + ": Service User did not consume "
                            + skipped + " bytes of DIMSE data.");
            }
        }
        else
        {
            if (CommandUtils.isResponse(cmd))
            {
                as.onDimseRSP(cmd, null);
            }
            else
            {
                as.onDimseRQ(pcid, cmd, null, null);
            }
        }
        pcid = -1;
    }

    @Override
    public DicomObject readDataset() throws IOException {
        PresentationContext pc = as.getAssociateAC().getPresentationContext(
                pcid);
        String tsuid = pc.getTransferSyntax();
        return readDicomObject(TransferSyntax.valueOf(tsuid));
    }
    
    private DicomObject readDicomObject(TransferSyntax ts)
    throws IOException
    {
        DicomObject dcm = new BasicDicomObject();
        DicomInputStream din = new DicomInputStream(this, ts);
        try
        {
            din.readDicomObject(dcm, -1);
        }
        catch (DicomCodingException e)
        {
            log.warn(as.toString() + ": Failed to decode dicom object: " + e.getMessage());
            throw new AAbort();
        }
        finally {
            CloseUtils.safeClose(din);
        }
        return dcm;
    }

    private void nextPDV(int command, int pcid1)
    throws IOException
    {
        if (!hasRemaining())
        {
            nextPDU();
            if (pdutype != PDUType.P_DATA_TF)
            {
                log.warn(as.toString() + ": Expected P-DATA-TF but received PDU[type="
                        + pdutype + ", len=" + pdulen + "]");
                throw new AAbort();
            }
        }
        if (remaining() < 6)
        {
            log.warn(as.toString() + ": PDV does not fit in remaining "
                    + remaining() + " bytes of P-DATA_TF[len=" + pdulen + "]");
            throw new AAbort();
        }
        int pdvlen = getInt();
        this.pdvend = pos + pdvlen;
        if (pdvlen < 2 || pdvlen > remaining())
        {
            log.warn(as.toString() + ": Invalid PDV item length: " + pdvlen);
            throw new AAbort();
        }
        this.pcid = get();
        this.pdvmch = get();
        if (log.isDebugEnabled())
            log.debug(as.toString() + " >> PDV[len = " + pdvlen
                    + ", pcid = " + pcid + ", mch = " + pdvmch + "]");
        if ((pdvmch & PDVType.COMMAND) != command)
        {
            log.warn(as.toString() + (command == 0 
                    ? ": Expected Data but received Command PDV"
                    : ": Expected Command but received Data PDV"));
            throw new AAbort();
        }
        if (pcid1 != -1 && pcid != pcid1)
        {
            log.warn(as.toString() + ": Expected PDV with pcid: " + pcid1 
                    + " but received with pcid: " + pcid);
            throw new AAbort();
        }
    }

    private boolean isEOF() throws IOException
    {
        while (pos == pdvend) {
            if ((pdvmch & PDVType.LAST) != 0)
                return true;
            try
            {
                nextPDV(pdvmch & PDVType.COMMAND, pcid);
            }
            catch (AAbort e)
            {
                as.abort(e); // send abort before exit of read, skip, close 
                throw e;
            }
         }
        return false;
    }
    
    @Override
    public int read() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (isEOF())
            return -1;

        return get();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (isEOF())
            return -1;

        int read = Math.min(len, pdvend - pos);
        get(b, off, read);
        return read;
    }

    @Override
    public final int available() {
        return pdvend - pos;
    }

    @Override
    public long skip(long n) throws IOException
    {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (n <= 0 || isEOF())
            return 0;

        int skipped = (int) Math.min(n, pdvend - pos);
        skip(skipped);
        return skipped;
    }
    
    @Override
    public void close() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        skipAll();
    }

    @Override
    public long skipAll() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        long n = 0;
        while (!isEOF()) {
            n += pdvend - pos;
            pos = pdvend;
        }
        return n;
    }

    @Override
    public void copyTo(OutputStream out, int length) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        int remaining = length;
        while (remaining > 0) {
            if (isEOF())
                throw new EOFException("remaining: " + remaining);
            int read = Math.min(remaining, pdvend - pos);
            out.write(buf, pos, read);
            remaining -= read;
            pos += read;
        }
    }

    @Override
    public void copyTo(OutputStream out) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        while (!isEOF()) {
            out.write(buf, pos, pdvend - pos);
            pos = pdvend;
        }
    }
}
