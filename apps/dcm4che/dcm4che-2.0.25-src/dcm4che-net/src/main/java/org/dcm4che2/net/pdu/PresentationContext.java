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

package org.dcm4che2.net.pdu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.dcm4che2.data.UIDDictionary;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date: 2008-08-21 12:25:53 +0200 (Thu, 21 Aug 2008) $
 * @since Sep 15, 2005
 */
public class PresentationContext
{

    public static final int ACCEPTANCE = 0;
    public static final int USER_REJECTION = 1;
    public static final int PROVIDER_REJECTION = 2;
    public static final int ABSTRACT_SYNTAX_NOT_SUPPORTED = 3;
    public static final int TRANSFER_SYNTAX_NOT_SUPPORTED = 4;
    private static final String UNDEFINED = "undefined";

    private static final String[] RESULT =
    {
            "acceptance",
            "user-rejection",
            "no-reason (provider rejection)",
            "abstract-syntax-not-supported (provider rejection)",
            "transfer-syntaxes-not-supported (provider rejection)"
    };

    private int pcid;
    private int result;
    private String abstractSyntax;
    private Set<String> transferSyntaxes = new LinkedHashSet<String>();

    public final int getPCID()
    {
        return pcid;
    }

    public final void setPCID(int pcid)
    {
        this.pcid = pcid;
    }

    public final int getResult()
    {
        return result;
    }

    public boolean isAccepted()
    {
        return result == ACCEPTANCE;
    }

    public final void setResult(int result)
    {
        this.result = result;
    }

    public final String getAbstractSyntax()
    {
        return abstractSyntax;
    }

    public final void setAbstractSyntax(String abstractSyntax)
    {
        this.abstractSyntax = abstractSyntax;
    }

    public final Set<String> getTransferSyntaxes()
    {
        return Collections.unmodifiableSet(transferSyntaxes);
    }

    public String getTransferSyntax() {
        return transferSyntaxes.iterator().next();
    }

    public final boolean addTransferSyntax(String tsuid)
    {
        if (tsuid == null)
            throw new NullPointerException();

        return transferSyntaxes.add(tsuid);
    }

    public final boolean removeTransferSyntax(String tsuid)
    {
        return transferSyntaxes.remove(tsuid);
    }

    public int length()
    {
        int len = 4;
        if (abstractSyntax != null)
            len += 4 + abstractSyntax.length();
        for (String tsuid : transferSyntaxes) {
            len += 4 + tsuid.length();
        }
        return len;
    }

    public String getResultAsString()
    {
        try
        {
            return RESULT[result];
        } catch (IndexOutOfBoundsException e)
        {
            return UNDEFINED;
        }
    }
    
    private static StringBuffer promptUID(String uid, StringBuffer sb)
    {
        return sb.append(uid).append(" - ").append(
                UIDDictionary.getDictionary().nameOf(uid));
    }

    public StringBuffer toStringBuffer(StringBuffer sb)
    {
        sb.append("PresentationContext[id = ").append(pcid);
        if (abstractSyntax != null)
        {
            sb.append(", as = ");
            promptUID(abstractSyntax, sb);
        } else
            sb.append(", result = ").append(result).append(" - ")
                    .append(getResultAsString());
        ArrayList<String> tsuids = new ArrayList<String>(transferSyntaxes);
        for (int j = 0, m = tsuids.size(); j < m; j++)
        {
            sb.append("\n    ts = ");
            promptUID(tsuids.get(j), sb);
        }
        sb.append("\n    ]");
        return sb;
    }

    @Override
    public String toString() {
        return toStringBuffer(new StringBuffer()).toString();
    }

}
