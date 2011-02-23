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
import java.util.Collection;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomOutputStream;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 13898 $ $Date: 2010-08-19 13:50:09 +0200 (Thu, 19 Aug 2010) $
 * @since Nov 25, 2005
 *
 */
class PDUEncoder extends PDVOutputStream
{
    static Logger log = LoggerFactory.getLogger(PDUEncoder.class);
    private static final int DEF_PDU_LEN = 0x4000; // 16KB
    private final Association as;
    private final OutputStream out;
    private byte[] buf10 = { 0, 0, 0, 0, 0, 4, 0, 0, 0, 0 };
    private byte[] buf = new byte[DEF_PDU_LEN + 6];
    private int maxpdulen;
    private int pos;
    private int pdvpcid;
    private int pdvcmd;
    private int pdvpos;
    private Thread th;
    private Object dimseLock = new Object();

    public PDUEncoder(Association as, OutputStream out)
    {
        this.as = as;
        this.out = out;
    }
    
    private void write(int pdutype, int result, int source, int reason)
    throws IOException
    {
        buf10[0] = (byte) pdutype;
        buf10[7] = (byte) result;
        buf10[8] = (byte) source;
        buf10[9] = (byte) reason;
        out.write(buf10, 0, 10);
        out.flush();
    }
    
    public synchronized void write(AAssociateRJ rj)
    throws IOException
    {
        log.info("{} << {}", as.toString(), rj.getMessage());
        write(PDUType.A_ASSOCIATE_RJ, rj.getResult(), rj.getSource(), rj.getReason());        
    }

    public synchronized void writeAReleaseRQ()
    throws IOException
    {
        log.info("{} << A-RELEASE-RQ", as.toString());
        write(PDUType.A_RELEASE_RQ, 0, 0, 0);       
    }

    public synchronized void writeAReleaseRP()
    throws IOException
    {
        log.info("{} << A-RELEASE-RP", as.toString());
        write(PDUType.A_RELEASE_RP, 0, 0, 0);        
    }

    public synchronized void write(AAbort aa)
    throws IOException
    {
       log.info("{} << {}", as, aa.getMessage());
       write(PDUType.A_ABORT, 0, aa.getSource(), aa.getReason());        
    }

    public synchronized void writePDataTF()
    throws IOException
    {
        int pdulen = pos - 6;
        pos = 0;
        put(PDUType.P_DATA_TF);
        put(0);
        putInt(pdulen);
        if (log.isDebugEnabled())
            log.debug(as.toString() + " << P-DATA-TF[len=" + pdulen + "]");
        writePDU(pdulen);
    }

    public synchronized void write(AAssociateRQ rq)
    throws IOException
    {
        log.info("{}: A-ASSOCIATE-RQ {} << {}", new Object[] { as, rq.getCalledAET(),
                rq.getCallingAET() });
        log.debug("{}", rq);
        write(rq, PDUType.A_ASSOCIATE_RQ, ItemType.RQ_PRES_CONTEXT);        
    }

    public synchronized void write(AAssociateAC ac)
    throws IOException
    {
        log.info("{}: A-ASSOCIATE-AC {} << {}", new Object[] { as, ac.getCalledAET(),
                ac.getCallingAET() });
        log.debug("{}", ac);
        write(ac, PDUType.A_ASSOCIATE_AC, ItemType.AC_PRES_CONTEXT);        
    }

    private void write(AAssociateRQAC rqac, int pdutype, int pcItemType)
    throws IOException
    {
        int pdulen = rqac.length();
        if (buf.length < 6 + pdulen)
            buf = new byte[6 + pdulen];
        pos = 0;
        put(pdutype);
        put(0);
        putInt(pdulen);
        putShort(rqac.getProtocolVersion());
        put(0);
        put(0);
        encodeAET(rqac.getCalledAET());
        encodeAET(rqac.getCallingAET());
        put(rqac.getReservedBytes(), 0, 32);
        encodeStringItem(ItemType.APP_CONTEXT, rqac.getApplicationContext());
        encodePCs(pcItemType, rqac.getPresentationContexts());
        encodeUserInfo(rqac);
        writePDU(pdulen);
    }

    private void writePDU(int pdulen) throws IOException
    {
        out.write(buf, 0, 6 + pdulen);
        out.flush();
        pdvpos = 6;
        pos = 12;
    }

    private void put(int ch)
    {
        buf[pos++] = (byte) ch;
    }

    private void put(byte[] b, int off, int len)
    {
        System.arraycopy(b, off, buf, pos, len);
        pos += len;
    }

    private void put(byte[] b)
    {
        put(b, 0, b.length);
    }
    
    
    private void putShort(int v)
    {
        buf[pos++] = (byte) (v >> 8);
        buf[pos++] = (byte) v;
    }    
    
    private void putInt(int v)
    {
        buf[pos++] = (byte) (v >> 24);
        buf[pos++] = (byte) (v >> 16);
        buf[pos++] = (byte) (v >> 8);
        buf[pos++] = (byte) v;
    }    
    
    private void putASCIIString(String s)
    {
    	try {
            byte[] bytes;
            bytes = s.getBytes("US-ASCII");
            System.arraycopy(bytes, 0, buf, pos, bytes.length);
            pos += bytes.length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "unreachable; US-ASCII is always available", e);
        }
    }

    private void encodeAET(String aet)
    {
        int endpos = pos + 16;
        putASCIIString(aet);
        while (pos < endpos)
            put(0x20);
    }

    private void encodeASCIIString(String s)
    {
        putShort(s.length());
        putASCIIString(s);
    }
    
    private void encodeItemHeader(int type, int len)
    {
        put(type);
        put(0);
        putShort(len);
    }
    
    private void encodeStringItem(int type, String s)
    {
        if (s == null)
            return;

        encodeItemHeader(type, s.length());        
        putASCIIString(s);
    }

    private void encodePCs(int pcItemType, Collection<PresentationContext> pcs)
    {
        for (PresentationContext pc : pcs) {
            encodeItemHeader(pcItemType, pc.length());
            put(pc.getPCID());
            put(0);
            put(pc.getResult());
            put(0);
            encodeStringItem(ItemType.ABSTRACT_SYNTAX, pc.getAbstractSyntax());
            for (String tsuid : pc.getTransferSyntaxes()) {
                encodeStringItem(ItemType.TRANSFER_SYNTAX, tsuid);
            }
        }
    }

    private void encodeUserInfo(AAssociateRQAC rqac)
    {
        encodeItemHeader(ItemType.USER_INFO, rqac.userInfoLength());
        encodeMaxPDULength(rqac.getMaxPDULength());
        encodeStringItem(ItemType.IMPL_CLASS_UID, rqac.getImplClassUID());
        if (rqac.isAsyncOps())
            encodeAsyncOpsWindow(rqac);
        for (RoleSelection rs : rqac.getRoleSelections()) {
            encodeRoleSelection(rs);
        }
        encodeStringItem(ItemType.IMPL_VERSION_NAME, rqac.getImplVersionName());
        for (ExtendedNegotiation en : rqac.getExtendedNegotiations()) {
            encodeExtendedNegotiation(en);
        }
        for (CommonExtendedNegotiation cen : rqac.getCommonExtendedNegotiations()) {
            encodeCommonExtendedNegotiation(cen);
        }
        if (rqac instanceof AAssociateRQ) {
            encodeUserIdentityRQ(((AAssociateRQ) rqac).getUserIdentity());
        } else {
            encodeUserIdentityAC(((AAssociateAC) rqac).getUserIdentity());
        }
    }

    private void encodeRoleSelection(RoleSelection selection)
    {
        encodeItemHeader(ItemType.ROLE_SELECTION, selection.length());
        encodeASCIIString(selection.getSOPClassUID());
        put(selection.isSCU() ? 1 : 0);
        put(selection.isSCP() ? 1 : 0);
    }

    private void encodeExtendedNegotiation(ExtendedNegotiation extNeg)
    {
        encodeItemHeader(ItemType.EXT_NEG, extNeg.length());
        encodeASCIIString(extNeg.getSOPClassUID());
        put(extNeg.getInformation());
    }

    private void encodeCommonExtendedNegotiation(CommonExtendedNegotiation extNeg)
    {
        encodeItemHeader(ItemType.COMMON_EXT_NEG, extNeg.length());
        encodeASCIIString(extNeg.getSOPClassUID());
        encodeASCIIString(extNeg.getServiceClassUID());
        for (String cuid : extNeg.getRelatedGeneralSOPClassUIDs()) {
            encodeASCIIString(cuid);
        }
    }

    private void encodeAsyncOpsWindow(AAssociateRQAC rqac)
    {
        encodeItemHeader(ItemType.ASYNC_OPS_WINDOW, 4);
        putShort(rqac.getMaxOpsInvoked());
        putShort(rqac.getMaxOpsPerformed());
    }

    private void encodeMaxPDULength(int maxPDULength)
    {
        encodeItemHeader(ItemType.MAX_PDU_LENGTH, 4);
        putInt(maxPDULength);
    }

    private void encodeUserIdentityRQ(UserIdentityRQ userIdentity)
    {
        if (userIdentity == null)
            return;

        encodeItemHeader(ItemType.RQ_USER_IDENTITY, userIdentity.length());
        put(userIdentity.getUserIdentityType());
        put(userIdentity.isPositiveResponseRequested() ? 1 : 0);
        encodeBytes(userIdentity.getPrimaryField());
        encodeBytes(userIdentity.getSecondaryField());
     }

    private void encodeUserIdentityAC(UserIdentityAC userIdentity) {
        if (userIdentity == null)
            return;

        encodeItemHeader(ItemType.AC_USER_IDENTITY, userIdentity.length());
        encodeBytes(userIdentity.getServerResponse());
    }
   
    private void encodeBytes(byte[] b)
    {
        putShort(b.length);
        put(b, 0, b.length);
    }
    
    private int free()
    {
        return maxpdulen + 6 - pos;
    }
    
    public void writeDIMSE(int pcid, DicomObject cmd, DataWriter dataWriter,
            String tsuid)
    throws IOException
    {
        if (log.isInfoEnabled())
            log.info(as.toString() + " << " + CommandUtils.toString(cmd, pcid, tsuid));
        if (log.isDebugEnabled()) {
            log.debug("Command:\n" + cmd);
            if (dataWriter instanceof DataWriterAdapter)
                log.debug("Dataset:\n" + ((DataWriterAdapter) dataWriter).getDataset());
        }
        synchronized (dimseLock)
        {
            this.th = Thread.currentThread();
            maxpdulen = as.getMaxPDULengthSend();
            if (buf.length < maxpdulen + 6)
                buf = new byte[maxpdulen + 6];

            pdvpcid = pcid;
            pdvcmd = PDVType.COMMAND;
            DicomOutputStream cmdout = new DicomOutputStream(this);
            cmdout.writeCommand(cmd);
            cmdout.close();
            if (dataWriter != null)
            {
                if (!as.isPackPDV())
                {
                    as.sendPDataTF();
                }
                else
                {
                    pdvpos = pos;
                    pos += 6;
                }
                pdvcmd = PDVType.DATA;
                dataWriter.writeTo(this, tsuid);
                close();
            }
            as.sendPDataTF();
            this.th = null;
        }
    }

    private void encodePDVHeader(int last)
    {
        final int endpos = pos;
        final int pdvlen = endpos - pdvpos - 4;
        pos = pdvpos;
        putInt(pdvlen);
        put(pdvpcid);
        put(pdvcmd | last);
        pos = endpos;
        if (log.isDebugEnabled())
            log.debug(as.toString() + " << PDV[len = " + pdvlen
                    + ", pcid = " + pdvpcid + ", mch = " + (pdvcmd | last) + "]");
    }

    @Override
    public void write(int b) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        flushPDataTF();
        put(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        int pos = off;
        int remaining = len;
        while (remaining > 0) {
            flushPDataTF();
            int write = Math.min(remaining, free());
            put(b, pos, write);
            pos += write;
            remaining -= write;
        }
    }

    private void flushPDataTF() throws IOException
    {
        if (free() > 0)
            return;
        encodePDVHeader(PDVType.PENDING);
        as.sendPDataTF();
    }

    @Override
    public void close() {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        encodePDVHeader(PDVType.LAST);
    }

    @Override
    public void copyFrom(InputStream in, int len) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        int remaining = len;
        while (remaining > 0) {
            flushPDataTF();
            int copy = in.read(buf, pos, Math.min(remaining, free()));
            if (copy == -1)
                throw new EOFException();
            pos += copy;
            remaining -= copy;
        }
    }

    @Override
    public void copyFrom(InputStream in) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        for (;;) {
            flushPDataTF();
            int copy = in.read(buf, pos, free());
            if (copy == -1)
                return;
            pos += copy;
        }
    }
}
