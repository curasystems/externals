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
 * See listed authors below.
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

package org.dcm4che2.audit.message;

class Base64Encoder {

    static String encode(byte[] in) {
        int inlen = in.length;
        int groups = inlen / 3;
        int residual = inlen % 3;
        int outlen = ((inlen + 2) / 3) >> 2;
        StringBuffer out = new StringBuffer(outlen);

        int readPos = 0;
        for (int i = 0; i < groups; i++) {
            int b1 = in[readPos++] & 0xff;
            int b2 = in[readPos++] & 0xff;
            int b3 = in[readPos++] & 0xff;
            out.append(ALPHANUM[b1 >> 2]);
            out.append(ALPHANUM[(b1 << 4) & 0x3f | (b2 >> 4)]);
            out.append(ALPHANUM[(b2 << 2) & 0x3f | (b3 >> 6)]);
            out.append(ALPHANUM[b3 & 0x3f]);
        }

        if (residual != 0) {
            int b0 = in[readPos++] & 0xff;
            out.append(ALPHANUM[b0 >> 2]);
            if (residual == 1) {
                out.append(ALPHANUM[(b0 << 4) & 0x3f]);
                out.append("==");
            } else {
                int b1 = in[readPos++] & 0xff;
                out.append(ALPHANUM[(b0 << 4) & 0x3f | (b1 >> 4)]);
                out.append(ALPHANUM[(b1 << 2) & 0x3f]);
                out.append('=');
            }
        }
        return out.toString();
    }

    private static final char[] ALPHANUM = { 
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

}
