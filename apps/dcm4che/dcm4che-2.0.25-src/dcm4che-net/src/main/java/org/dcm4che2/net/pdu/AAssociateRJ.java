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

import java.io.IOException;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 7318 $ $Date: 2008-10-01 13:41:58 +0200 (Wed, 01 Oct 2008) $
 * @since Sep 15, 2005
 */
public class AAssociateRJ extends IOException {
    private static final long serialVersionUID = 7878137718648065429L;

    public static final int RESULT_REJECTED_PERMANENT = 1;
    public static final int RESULT_REJECTED_TRANSIENT = 2;

    public static final int SOURCE_SERVICE_USER = 1;
    public static final int SOURCE_SERVICE_PROVIDER_ACSE = 2;
    public static final int SOURCE_SERVICE_PROVIDER_PRES = 3;

    public static final int REASON_NO_REASON_GIVEN = 1;
    public static final int REASON_APP_CTX_NAME_NOT_SUPPORTED = 2;
    public static final int REASON_CALLING_AET_NOT_RECOGNIZED = 3;
    public static final int REASON_CALLED_AET_NOT_RECOGNIZED = 7;

    public static final int REASON_PROTOCOL_VERSION_NOT_SUPPORTED = 2;

    public static final int REASON_TEMPORARY_CONGESTION = 1;
    public static final int REASON_LOCAL_LIMIT_EXCEEDED = 2;

    /**
     * Holds descriptions for common codes.
     */
    private static class Lookup {
        private int source;
        private int reason;
        private String description;

        public Lookup(int source, int reason, String desc) {
            this.source = source;
            this.reason = reason;
            this.description = desc;
        }

        public boolean matches(int sourceIn, int reasonIn) {
            return this.source == sourceIn && this.reason == reasonIn;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final Lookup[] DESCRIPTIONS = new Lookup[] {
            new Lookup(SOURCE_SERVICE_USER, REASON_NO_REASON_GIVEN,
                    "no-reason-given"),
            new Lookup(SOURCE_SERVICE_USER, REASON_APP_CTX_NAME_NOT_SUPPORTED,
                    "application-context-name-not-supported"),
            new Lookup(SOURCE_SERVICE_USER, REASON_CALLING_AET_NOT_RECOGNIZED,
                    "calling-AE-title-not-recognized"),
            new Lookup(SOURCE_SERVICE_USER, REASON_CALLED_AET_NOT_RECOGNIZED,
                    "called-AE-title-not-recognized"),

            new Lookup(SOURCE_SERVICE_PROVIDER_ACSE, REASON_NO_REASON_GIVEN,
                    "no-reason-given"),
            new Lookup(SOURCE_SERVICE_PROVIDER_ACSE,
                    REASON_PROTOCOL_VERSION_NOT_SUPPORTED,
                    "protocol-version-not-supported"),

            new Lookup(SOURCE_SERVICE_PROVIDER_PRES,
                    REASON_TEMPORARY_CONGESTION, "temporary-congestion"),
            new Lookup(SOURCE_SERVICE_PROVIDER_PRES,
                    REASON_LOCAL_LIMIT_EXCEEDED, "local-limit-exceeded") };

    private final int result;
    private final int source;
    private final int reason;

    public AAssociateRJ(int result, int source, int reason) {
        super(createDescription(result, source, reason));
        this.result = result;
        this.source = source;
        this.reason = reason;
    }

    public final int getResult() {
        return result;
    }

    public final int getSource() {
        return source;
    }

    public final int getReason() {
        return reason;
    }

    private static String createDescription(int result, int source, int reason) {
        StringBuilder msg = new StringBuilder();
        msg.append("A-ASSOCIATE-RJ[result=");
        msg.append(result);
        msg.append(", source=");
        msg.append(source);
        msg.append(", reason=");
        msg.append(reason);
        msg.append("]: ");

        if (result == 1) {
            msg.append("permanent ");
        } else if (result == 2) {
            msg.append("transient ");
        }

        for (Lookup desc : DESCRIPTIONS) {
            if (desc.matches(source, reason)) {
                msg.append(desc.getDescription());
                return msg.toString();
            }
        }

        msg.append("reserved");
        return msg.toString();
    }
}
