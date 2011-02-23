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
 * Damien Evans <damien@theevansranch.com>
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

import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;

/**
 * Implementation of the DICOM part 8 TCP/IP state machine. This defines the
 * various states that a a TCP/IP connection may be in when a DICOM association
 * is using it.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 13898 $ $Date: 2010-08-19 13:50:09 +0200 (Thu, 19 Aug 2010) $
 * @since Nov 25, 2005
 * 
 */
public class State
{
    /** State 1 = Idle. */
    public static final State STA1 = new Sta1();

    /** State 2 = Transport connection open (Awaiting A-ASSOCIATE-RQ PDU). */
    public static final State STA2 = new Sta2();

    /**
     * State 3 = Awaiting local A-ASSOCIATE response primitive (from local
     * user).
     */
    public static final State STA3 = new Sta3();

    /**
     * State 4 = Awaiting transport connection opening to complete (from local
     * transport service).
     */
    public static final State STA4 = new Sta4();

    /** State 5 = Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU. */
    public static final State STA5 = new Sta5();

    /** State 6 = Association established and ready for data transfer. */
    public static final State STA6 = new Sta6();

    /** State 7 = Awaiting A-RELEASE-RP PDU. */
    public static final State STA7 = new Sta7();

    /** State 8 = Awaiting local A-RELEASE response primitive (from local user). */
    public static final State STA8 = new Sta8();

    /*
     * State 9 (not implemented) = Release collision requestor side; awaiting
     * A-RELEASE response (from local user).
     */
    // public static final State STA9 = new Sta9();
    
    /** State 10 = Release collision acceptor side; awaiting A-RELEASE-RP PDU. */
    public static final State STA10 = new Sta10();

    /** State 11 = Release collision requestor side; awaiting A-RELEASE-RP PDU. */
    public static final State STA11 = new Sta11();

    /*
     * State 12 (not implemented) = Release collision acceptor side; awaiting
     * A-RELEASE response primitive (from local user).
     */
    // public static final State STA12 = new Sta12();
    
    /**
     * State 13 = Awaiting transport connection close indication (association no
     * longer exists).
     */
    public static final State STA13 = new Sta13();

    protected final String name;

    State(String name)
    {
        this.name = name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    /** Sta1 - Idle */
    private static class Sta1 extends State
    {

        Sta1()
        {
            super("Sta1");
        }

        /** 
         * @see org.dcm4che2.net.State#abort(org.dcm4che2.net.Association, org.dcm4che2.net.pdu.AAbort)
         */
        @Override
        void abort(Association as, AAbort aa) {
            // NOOP
        }
    }

    /** Sta2 - Transport connection open (Awaiting A-ASSOCIATE-RQ PDU) */
    private static class Sta2 extends State
    {

        Sta2()
        {
            super("Sta2");
        }

        /** 
         * @see org.dcm4che2.net.State#receivedAssociateRQ(org.dcm4che2.net.Association, org.dcm4che2.net.pdu.AAssociateRQ)
         */
        @Override
        void receivedAssociateRQ(Association as, AAssociateRQ rq)
                throws IOException {
            as.onAAssociateRQ(rq);
        }
    }

    /** Sta3 - Awaiting local A-ASSOCIATE response primitive */
    private static class Sta3 extends State
    {

        Sta3()
        {
            super("Sta3");
        }

    }

    /** Sta4 - Awaiting local A-ASSOCIATE request primitive. */
    private static class Sta4 extends State
    {

        Sta4()
        {
            super("Sta4");
        }

        /** 
         * @see org.dcm4che2.net.State#sendAssociateRQ(org.dcm4che2.net.Association, org.dcm4che2.net.pdu.AAssociateRQ)
         */
        @Override
        void sendAssociateRQ(Association as, AAssociateRQ rq)
                throws IOException {
            as.writeAssociationRQ(rq);
        }
    }

    /** Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU */
    private static class Sta5 extends State
    {

        Sta5()
        {
            super("Sta5");
        }

        /** 
         * @see org.dcm4che2.net.State#receivedAssociateAC(org.dcm4che2.net.Association, org.dcm4che2.net.pdu.AAssociateAC)
         */
        @Override
        void receivedAssociateAC(Association as, AAssociateAC ac)
                throws IOException {
            as.onAssociateAC(ac);
        }

        /** 
         * @see org.dcm4che2.net.State#receivedAssociateRJ(org.dcm4che2.net.Association, org.dcm4che2.net.pdu.AAssociateRJ)
         */
        @Override
        void receivedAssociateRJ(Association as, AAssociateRJ rj)
                throws IOException {
            as.onAssociateRJ(rj);
        }
    }

    /** Sta6 - Association established and ready for data transfer */
    private static class Sta6 extends State
    {

        Sta6()
        {
            super("Sta6");
        }

        /** 
         * @see org.dcm4che2.net.State#receivedPDataTF(org.dcm4che2.net.Association)
         */
        @Override
        void receivedPDataTF(Association as) throws IOException {
            as.onPDataTF();
        }

        /** 
         * @see org.dcm4che2.net.State#sendPDataTF(org.dcm4che2.net.Association)
         */
        @Override
        void sendPDataTF(Association as) throws IOException {
            as.writePDataTF();
        }

        /** 
         * @see org.dcm4che2.net.State#receivedReleaseRQ(org.dcm4che2.net.Association)
         */
        @Override
        void receivedReleaseRQ(Association as) throws IOException {
            as.onReleaseRQ();
        }

        /** 
         * @see org.dcm4che2.net.State#sendReleaseRQ(org.dcm4che2.net.Association)
         */
        @Override
        void sendReleaseRQ(Association as) throws IOException {
            as.writeReleaseRQ();
        }

        /** 
         * @see org.dcm4che2.net.State#isReadyForDataTransfer()
         */
        @Override
        boolean isReadyForDataTransfer() {
            return true;
        }

        /** 
         * @see org.dcm4che2.net.State#isReadyForDataSend()
         */
        @Override
        boolean isReadyForDataSend() {
            return true;
        }

        /** 
         * @see org.dcm4che2.net.State#isReadyForDataReceive()
         */
        @Override
        boolean isReadyForDataReceive() {
            return true;
        }

    }

    /** Sta7 - Awaiting A-RELEASE-RP PDU */
    private static class Sta7 extends State
    {

        Sta7()
        {
            super("Sta7");
        }

        /** 
         * @see org.dcm4che2.net.State#receivedPDataTF(org.dcm4che2.net.Association)
         */
        @Override
        void receivedPDataTF(Association as) throws IOException {
            as.onPDataTF();
        }

        /** 
         * @see org.dcm4che2.net.State#receivedReleaseRQ(org.dcm4che2.net.Association)
         */
        @Override
        void receivedReleaseRQ(Association as) throws IOException {
            as.onCollisionReleaseRQ();
        }

        /** 
         * @see org.dcm4che2.net.State#receivedReleaseRP(org.dcm4che2.net.Association)
         */
        @Override
        void receivedReleaseRP(Association as) throws IOException {
            as.onReleaseRP();
        }

        /** 
         * @see org.dcm4che2.net.State#isReadyForDataReceive()
         */
        @Override
        boolean isReadyForDataReceive() {
            return true;
        }

    }

    /** Sta8 - Awaiting local A-RELEASE response primitive */
    private static class Sta8 extends State
    {

        public Sta8()
        {
            super("Sta8");
        }

        /** 
         * @see org.dcm4che2.net.State#sendPDataTF(org.dcm4che2.net.Association)
         */
        @Override
        void sendPDataTF(Association as) throws IOException {
            as.writePDataTF();
        }

        /** 
         * @see org.dcm4che2.net.State#isReadyForDataSend()
         */
        @Override
        boolean isReadyForDataSend() {
            return true;
        }

    }

    /*
     * Sta9 - Release collision requestor side; awaiting A-RELEASE response
     * primitive
     */
    /*
     * private static class Sta9 extends State {
     * 
     * public Sta9() { super("Sta9 - Release collision requestor side; " +
     * "awaiting A-RELEASE response primitive"); } }
     */
    
    /**
     * Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU
     */
    private static class Sta10 extends State
    {

        public Sta10()
        {
            super("Sta10");
        }

        /** 
         * @see org.dcm4che2.net.State#receivedReleaseRP(org.dcm4che2.net.Association)
         */
        @Override
        void receivedReleaseRP(Association as) throws IOException {
            as.onCollisionReleaseRP();
        }
    }

    /**
     * Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU
     */
    private static class Sta11 extends State
    {

        public Sta11()
        {
            super("Sta11");
        }

        /** 
         * @see org.dcm4che2.net.State#receivedReleaseRP(org.dcm4che2.net.Association)
         */
        @Override
        void receivedReleaseRP(Association as) throws IOException {
            as.onReleaseRP();
        }
    }

    /**
     * Sta12 - Release collision acceptor side; awaiting A-RELEASE response
     * primitive
     */
    /*
     * private static class Sta12 extends State {
     * 
     * public Sta12() { super("Sta12"); } }
     */
    /** Sta13 - Awaiting Transport Connection Close Indication */
    private static class Sta13 extends State
    {

        public Sta13()
        {
            super("Sta13 ");
        }

        /** 
         * @see org.dcm4che2.net.State#abort(org.dcm4che2.net.Association, org.dcm4che2.net.pdu.AAbort)
         */
        @Override
        void abort(Association as, AAbort aa) {
            // NOOP
        }
    }

    /**
     * Notify the association that an A-ASSOCIATE-RQ PDU has been received.
     * 
     * @param as The <code>Association</code> object.
     * @param rq The <code>AAssociateRQ</code> object.
     * @throws IOException If there was a problem in the network interaction.
     */
    void receivedAssociateRQ(Association as, AAssociateRQ rq)
            throws IOException
    {
        as.unexpectedPDU("A-ASSOCIATE-RQ");
    }

    /**
     * Notify the association that an A-ASSOCIATE-AC PDU has been received.
     * 
     * @param as The <code>Association</code> object.
     * @param ac The <code>AAssociateAC</code> object.
     * @throws IOException If there was a problem in the network interaction.
     */
    void receivedAssociateAC(Association as, AAssociateAC ac)
            throws IOException
    {
        as.unexpectedPDU("A-ASSOCIATE-AC");
    }

    /**
     * Notify the association that an A-ASSOCIATE-RJ PDU has been received.
     * 
     * @param as The <code>Association</code> object.
     * @param rj The <code>AAssociateRJ</code> object.
     * @throws IOException If there was a problem in the network interaction.
     */
    void receivedAssociateRJ(Association as, AAssociateRJ rj)
            throws IOException
    {
        as.unexpectedPDU("A-ASSOCIATE-RJ");
    }

    /**
     * Notify the association that a P-DATA-TF PDU has been received.
     * 
     * @param as The <code>Association</code> object.
     * @throws IOException If there was a problem in the network interaction.
     */
    void receivedPDataTF(Association as) throws IOException
    {
        as.unexpectedPDU("P-DATA-TF");
    }

    /**
     * Notify the association that an A-RELEASE-RP PDU has been received.
     * 
     * @param as The <code>Association</code> object.
     * @throws IOException If there was a problem in the network interaction.
     */
    void receivedReleaseRQ(Association as) throws IOException
    {
        as.unexpectedPDU("A-RELEASE-RQ");
    }

    /**
     * Notify the association that an A-RELEASE-RP PDU has been received.
     * 
     * @param as The <code>Association</code> object.
     * @throws IOException If there was a problem in the network interaction.
     */
    void receivedReleaseRP(Association as) throws IOException
    {
        as.unexpectedPDU("A-RELEASE-RP");
    }

    /**
     * Send an A-ASSOCIATE-RQ PDU on the association.
     * 
     * @param as The {@link Association} object.
     * @param rq The {@link AAssociateRQ} object.
     * @throws IOException If there was a problem in the network interaction.
     */
    @SuppressWarnings("unused")
    void sendAssociateRQ(Association as, AAssociateRQ rq) throws IOException {
        // as.illegalStateForSending("A-ASSOCIATE-RQ");
        throw new IllegalStateException(toString());
    }

    /**
     * Send a P-DATA-TF PDU on the association.
     * 
     * @param as The active DICOM <code>Association</code> object.
     * @throws IOException If there was a problem in the network interaction.
     */
    void sendPDataTF(Association as) throws IOException
    {
        as.illegalStateForSending("P-DATA-TF");
    }

    /**
     * Send a release request to the device on the other end of the association.
     * 
     * @param as The active DICOM <code>Association</code> object.
     * @throws IOException If there was a problem in the network interaction.
     */
    void sendReleaseRQ(Association as) throws IOException
    {
        as.illegalStateForSending("A-RELEASE-RQ");
    }

    /**
     * Abort an association.
     * 
     * @param as The active DICOM <code>Association</code> object.
     * @param aa The <code>AAbort</code> object that should be written to the association.
     */
    void abort(Association as, AAbort aa)
    {
        as.writeAbort(aa);
    }

    /**
     * Determine if the current state allows data transfer.
     * 
     * @return boolean True if the current state allows data transfer.
     */
    boolean isReadyForDataTransfer()
    {
        return false;
    }

    /**
     * Determine if the current state allows data to be sent.
     * 
     * @return boolean True if the current state allows data to be sent.
     */
    boolean isReadyForDataSend()
    {
        return false;
    }

    /**
     * Determine if the current state allows data to be received.
     * 
     * @return boolean True if the current state allows data to be received.
     */
    boolean isReadyForDataReceive()
    {
        return false;
    }
}
