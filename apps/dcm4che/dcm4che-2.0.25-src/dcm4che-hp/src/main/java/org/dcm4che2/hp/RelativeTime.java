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

package org.dcm4che2.hp;

import java.util.Calendar;
import java.util.Date;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 444 $ $Date: 2005-10-21 14:02:11 +0200 (Fri, 21 Oct 2005) $
 * @since Oct 21, 2005
 *
 */
public class RelativeTime
{
    private static final RelativeTimeUnits CURRENT_TIME_UNITS = 
            RelativeTimeUnits.SECONDS;
    private final int[] values;
    private final RelativeTimeUnits units;

    public RelativeTime()
    {
        this(0, 0, CURRENT_TIME_UNITS);
    }
    
    public RelativeTime(int start, int end, RelativeTimeUnits units)
    {
        this(new int[]{ start, end }, units);
    }
    
    RelativeTime(int[] value, RelativeTimeUnits units)
    {
        this.values = value;
        this.units = units;
    }
    
    final int[] getValues()
    {
        return values;
    }

    public final int getStart()
    {
        return values[0];
    }

    public final int getEnd()
    {
        return values[1];
    }

    public Date getStartDate()
    {
        return toDate(values[0]);
    }

    public Date getEndDate()
    {
        return toDate(values[1]);
    }

    private Date toDate(int value)
    {
        Calendar c = Calendar.getInstance();
        c.roll(units.getCalendarField(), -value);
        return c.getTime();
    }

    public final boolean isCurrentTime()
    {
        return values[0] == 0 && values[1] == 0;
    }

    public final RelativeTimeUnits getUnits()
    {
        return units;
    }
    
    

}
