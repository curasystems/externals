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

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 14182 $ $Date: 2010-10-20 12:07:53 +0200 (Wed, 20 Oct 2010) $
 * @since Aug 7, 2005
 * 
 */
public abstract class FilterOp
{
    public static final FilterOp MEMBER_OF = new MemberOf();
    public static final FilterOp NOT_MEMBER_OF = new NotMemberOf();
    public static final FilterOp RANGE_INCL = new RangeIncl();
    public static final FilterOp RANGE_EXCL = new RangeExcl();
    public static final FilterOp GREATER_OR_EQUAL = new GreaterOrEqual();
    public static final FilterOp LESS_OR_EQUAL = new LessOrEqual();
    public static final FilterOp GREATER_THAN = new GreaterThan();
    public static final FilterOp LESS_THAN = new LessThan();

    protected final int numParams;
    protected final String codeString;

    protected FilterOp(String codeString, int numParams)
    {
        this.codeString = codeString;
        this.numParams = numParams;
    }

    public static FilterOp valueOf(String codeString)
    {
        try
        {
            return (FilterOp) FilterOp.class.getField(codeString).get(null);
        }
        catch (IllegalAccessException e)
        {
            throw new Error(e);
        }
        catch (NoSuchFieldException e)
        {
            throw new IllegalArgumentException("codeString: " + codeString);
        }
   }

    public final int getNumParams()
    {
        return numParams;
    }

    public final boolean isNumeric()
    {
        return numParams != 0;
    }

    public boolean op(String[] values, int valueNumber, String[] params)
    {
        throw new UnsupportedOperationException();
    }

    public boolean op(DicomElement values, DicomElement params)
    {
        throw new UnsupportedOperationException();
    }

    public abstract boolean op(int[] values, int valueNumber, int[] params);

    public abstract boolean op(int[] values, int valueNumber, long[] params);

    public abstract boolean op(float[] values, int valueNumber, float[] params);

    public abstract boolean op(double[] values, int valueNumber, double[] params);

    public final String getCodeString()
    {
        return codeString;
    }

    static boolean memberOf(String value, String[] params)
    {
        for (int i = 0; i < params.length; i++)
        {
            if (value.equals(params[i]))
                return true;
        }
        return false;
    }

    static boolean memberOf(int value, int[] params)
    {
        for (int i = 0; i < params.length; i++)
        {
            if (value == params[i])
                return true;
        }
        return false;
    }

    static boolean memberOf(int value, long[] params)
    {
        for (int i = 0; i < params.length; i++)
        {
            if (value == (int) params[i])
                return true;
        }
        return false;
    }

    static boolean memberOf(float value, float[] params)
    {
        for (int i = 0; i < params.length; i++)
        {
            if (value == params[i])
                return true;
        }
        return false;
    }

    static boolean memberOf(double value, double[] params)
    {
        for (int i = 0; i < params.length; i++)
        {
            if (value == params[i])
                return true;
        }
        return false;
    }

    static boolean memberOf(DicomObject value, DicomElement params)
    {
        for (int i = 0, n = params.countItems(); i < n; i++)
        {
            if (codeEquals(params.getDicomObject(i), value))
                return true;
        }
        return false;
    }

    static boolean codeEquals(DicomObject item1, DicomObject item2)
    {
        if (!equals(item1.getString(Tag.CodeValue),
                item2.getString(Tag.CodeValue)))
            return false;
        if (!equals(item1.getString(Tag.CodingSchemeDesignator),
                item2.getString(Tag.CodingSchemeDesignator)))
            return false;
        if (!item1.containsValue(Tag.CodingSchemeVersion)
                || !item2.containsValue(Tag.CodingSchemeVersion))
            return true;
        return equals(item1.getString(Tag.CodingSchemeVersion),
                item2.getString(Tag.CodingSchemeVersion));
    }

    static boolean equals(Object o1, Object o2)
    {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    static class MemberOf extends FilterOp
    {

        public MemberOf()
        {
            super("MEMBER_OF", 0);
        }

        @Override
        public boolean op(String[] values, int valueNumber, String[] params)
        {
            if (valueNumber != 0)
                return memberOf(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (memberOf(values[i], params))
                    return true;
            }
            return false;
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params)
        {
            if (valueNumber != 0)
                return memberOf(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (memberOf(values[i], params))
                    return true;
            }
            return false;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params)
        {
            if (valueNumber != 0)
                return memberOf(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (memberOf(values[i], params))
                    return true;
            }
            return false;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params)
        {
            if (valueNumber != 0)
                return memberOf(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (memberOf(values[i], params))
                    return true;
            }
            return false;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params)
        {
            if (valueNumber != 0)
                return memberOf(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (memberOf(values[i], params))
                    return true;
            }
            return false;
        }

        @Override
        public boolean op(DicomElement values, DicomElement params)
        {
            for (int i = 0, n = values.countItems(); i < n; i++)
            {
                if (memberOf(values.getDicomObject(i), params))
                    return true;
            }
            return false;
        }
    }

    static class NotMemberOf extends FilterOp
    {

        public NotMemberOf()
        {
            super("NOT_MEMBER_OF", 0);
        }

        @Override
        public boolean op(String[] values, int valueNumber, String[] params)
        {
            if (valueNumber != 0)
                return !memberOf(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (memberOf(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params)
        {
            if (valueNumber != 0)
                return !memberOf(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (memberOf(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params)
        {
            if (valueNumber != 0)
                return !memberOf(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (memberOf(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params)
        {
            if (valueNumber != 0)
                return !memberOf(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (memberOf(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params)
        {
            if (valueNumber != 0)
                return !memberOf(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (memberOf(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(DicomElement values, DicomElement params)
        {
            for (int i = 0, n = values.countItems(); i < n; i++)
            {
                if (memberOf(values.getDicomObject(i), params))
                    return false;
            }
            return true;
        }
    }

    static boolean inRange(int value, int[] params)
    {
        return value >= params[0] && value <= params[1];
    }

    static boolean inRange(int value, long[] params)
    {
        long l = value & 0xffffffffL;
        return l >= params[0] && l <= params[1];
    }

    static boolean inRange(float value, float[] params)
    {
        return value >= params[0] && value <= params[1];
    }

    static boolean inRange(double value, double[] params)
    {
        return value >= params[0] && value <= params[1];
    }

    static class RangeIncl extends FilterOp
    {

        public RangeIncl()
        {
            super("RANGE_INCL", 2);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params)
        {
            if (valueNumber != 0)
                return inRange(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (!inRange(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params)
        {
            if (valueNumber != 0)
                return inRange(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (!inRange(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params)
        {
            if (valueNumber != 0)
                return inRange(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (!inRange(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params)
        {
            if (valueNumber != 0)
                return inRange(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (!inRange(values[i], params))
                    return false;
            }
            return true;
        }
    }

    static class RangeExcl extends FilterOp
    {

        public RangeExcl()
        {
            super("RANGE_EXCL", 2);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params)
        {
            if (valueNumber != 0)
                return !inRange(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (inRange(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params)
        {
            if (valueNumber != 0)
                return !inRange(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (inRange(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params)
        {
            if (valueNumber != 0)
                return !inRange(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (inRange(values[i], params))
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params)
        {
            if (valueNumber != 0)
                return !inRange(values[valueNumber - 1], params);
            for (int i = 0; i < values.length; i++)
            {
                if (inRange(values[i], params))
                    return false;
            }
            return true;
        }
    }

    static int compare(int value, int[] params)
    {
        return value < params[0] ? -1 : value > params[0] ? 1 : 0;
    }

    static int compare(int value, long[] params)
    {
        long l = value & 0xffffffffL;
        return l < params[0] ? -1 : l > params[0] ? 1 : 0;
    }

    static int compare(float value, float[] params)
    {
        return value < params[0] ? -1 : value > params[0] ? 1 : 0;
    }

    static int compare(double value, double[] params)
    {
        return value < params[0] ? -1 : value > params[0] ? 1 : 0;
    }

    static class GreaterOrEqual extends FilterOp
    {

        public GreaterOrEqual()
        {
            super("GREATER_OR_EQUAL", 1);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) >= 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) < 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) >= 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) < 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) >= 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) < 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) >= 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) < 0)
                    return false;
            }
            return true;
        }
    }

    static class LessOrEqual extends FilterOp
    {

        public LessOrEqual()
        {
            super("LESS_OR_EQUAL", 1);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) <= 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) > 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) <= 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) > 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) <= 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) > 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) <= 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) > 0)
                    return false;
            }
            return true;
        }
    }

    static class GreaterThan extends FilterOp
    {

        public GreaterThan()
        {
            super("GREATER_THAN", 1);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) > 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) <= 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) > 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) <= 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) > 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) <= 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) > 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) <= 0)
                    return false;
            }
            return true;
        }
    }

    static class LessThan extends FilterOp
    {

        public LessThan()
        {
            super("LESS_THAN", 1);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) < 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) >= 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) < 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) >= 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) < 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) >= 0)
                    return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params)
        {
            if (valueNumber != 0)
                return compare(values[valueNumber - 1], params) < 0;
            for (int i = 0; i < values.length; i++)
            {
                if (compare(values[i], params) >= 0)
                    return false;
            }
            return true;
        }
    }

}
