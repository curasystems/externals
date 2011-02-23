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
 * Rick Riemer <rick.riemer@forcare.nl>
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

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>AssociationReaper</code> is responsible for monitoring DICOM
 * associations by testing them periodically for idleness. The
 * <code>Association</code> contains the maximum idle period, so when the
 * reaper tests the association object for idleness, it may release the
 * association if the idle period has been exceeded.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 12932 $ $Date: 2010-03-16 11:26:10 +0100 (Tue, 16 Mar 2010) $
 * @since Dec 10, 2005
 * 
 */
public class AssociationReaper {
    private static final float MILLISECONDS = 1000f;

    private static final Logger log = LoggerFactory
            .getLogger(AssociationReaper.class);

    private static Timer timer = new Timer(true);

    private Map<Association, TimerTask> timerTasks = new ConcurrentHashMap<Association, TimerTask>();

    private final int period;

    /**
     * Constructor which sets the max idle test period.
     * 
     * @param period
     *                An int signifying the time period in milliseconds in which
     *                associations will be tested for idleness..
     */
    public AssociationReaper(int period) {
        if (log.isDebugEnabled())
            log.debug("Check for idle Associations every "
                    + (period / MILLISECONDS) + "s.");

        this.period = period;
    }

    /**
     * Register an <code>Association</code> with this reaper.
     * 
     * @param a
     *                The Association to register.
     */
    public void register(final Association a) {
        log.debug("Start check for idle {}", a);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                a.checkIdle(System.currentTimeMillis());
            }
        };

        TimerTask previous = timerTasks.put(a, task);
        if (previous != null) {
            previous.cancel();
        }

        schedule(task);
    }

    private void schedule(TimerTask task) {
        try {
            timer.schedule(task, period, period);
        } catch (IllegalStateException e) {
            timer = new Timer(true);
            timer.schedule(task, period, period);
        }
    }

    /**
     * Unregister an <code>Association</code> from this reaper.
     * 
     * @param a
     *                The <code>Association</code> to unregister.
     */
    public void unregister(Association a) {
        log.debug("Stop check for idle {}", a);

        TimerTask task = timerTasks.remove(a);
        if (task != null) {
            task.cancel();
        }
    }
}
