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

import java.util.concurrent.Executor;

/**
 * <code>Executor</code> implementation which executes a <code>Runnable</code>
 * object in a new thread.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date: 2008-09-06 14:52:22 +0200 (Sat, 06 Sep 2008) $
 * @since Oct 2, 2005
 */
public class NewThreadExecutor implements Executor
{
    private static int threadId = 0;

    private final String threadNamePrefix;

    /**
     * Constructor.
     * 
     * @param threadNamePrefix A String containing the prefix that should be
     *            given to the created thread.
     */
    public NewThreadExecutor(String threadNamePrefix)
    {
        if (threadNamePrefix == null)
        {
            throw new NullPointerException("threadNamePrefix");
        }
        threadNamePrefix = threadNamePrefix.trim();
        if (threadNamePrefix.length() == 0)
        {
            throw new IllegalArgumentException("threadNamePrefix is empty.");
        }
        this.threadNamePrefix = threadNamePrefix;
    }

    /**
     * Get the prefix that is prepended to threads created by this
     * <code>Executor</code>
     * 
     * @return A String containing the prefix.
     */
    public final String getThreadNamePrefix()
    {
        return threadNamePrefix;
    }

    /**
     * This implementatin creates a new thread every time it is called.
     * 
     * @see org.dcm4che2.net.Executor#execute(java.lang.Runnable)
     */
    public void execute(Runnable runnable)
    {
        new Thread(runnable, threadNamePrefix + "-" + (++threadId)).start();
    }

}
