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

import org.dcm4che2.data.DicomObject;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date: 2008-08-26 13:01:09 +0200 (Tue, 26 Aug 2008) $
 * @since Oct 8, 2005
 * 
 */
class FutureDimseRSP extends DimseRSPHandler implements DimseRSP {
    private static class Entry {
        final DicomObject command;
        final DicomObject dataset;
        Entry next;

        public Entry(DicomObject command, DicomObject dataset) {
            this.command = command;
            this.dataset = dataset;
        }
    }

    private Entry entry = new Entry(null, null);
    private boolean finished;
    private int autoCancel;
    private IOException ex;

    @Override
    public synchronized void onDimseRSP(Association as, DicomObject cmd,
            DicomObject data) {
        super.onDimseRSP(as, cmd, data);
        Entry last = entry;
        while (last.next != null)
            last = last.next;

        last.next = new Entry(cmd, data);
        if (CommandUtils.isPending(cmd)) {
            if (autoCancel > 0 && --autoCancel == 0)
                try {
                    super.cancel(as);
                } catch (IOException e) {
                    ex = e;
                }
        } else {
            finished = true;
        }
        notifyAll();
    }

    @Override
    public synchronized void onClosed(Association as) {
        if (!finished) {
            ex = as.getException();
            if (ex == null) {
                ex = new IOException("Association to " + as.getRemoteAET()
                        + " closed before receive of outstanding DIMSE RSP");
            }
            notifyAll();
        }
    }

    public final void setAutoCancel(int autoCancel) {
        this.autoCancel = autoCancel;
    }

    @Override
    public void cancel(Association a) throws IOException {
        if (ex != null)
            throw ex;
        if (!finished)
            super.cancel(a);
    }

    public final DicomObject getCommand() {
        return entry.command;
    }

    public final DicomObject getDataset() {
        return entry.dataset;
    }

    public synchronized boolean next() throws IOException, InterruptedException {
        if (entry.next == null) {
            if (finished)
                return false;

            while (entry.next == null && ex == null)
                wait();

            if (ex != null)
                throw ex;
        }
        entry = entry.next;
        return true;
    }

}
