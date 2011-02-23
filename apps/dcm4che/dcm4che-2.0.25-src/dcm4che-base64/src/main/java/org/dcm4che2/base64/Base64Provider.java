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
 * Portions created by the Initial Developer are Copyright (C) 2010
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

package org.dcm4che2.base64;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collections;
import java.util.Iterator;

public class Base64Provider extends CharsetProvider {

    private static final char[] BASE64 = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    private static final byte INV_BASE64[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
        35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

    private final Charset base64 = new Charset("x-base64", null) {

        @Override
        public boolean contains(Charset cs) {
            return false;
        }

        @Override
        public CharsetDecoder newDecoder() {
            return new Decoder(this);
        }

        @Override
        public CharsetEncoder newEncoder() {
            return new Encoder(this);
        }
    };

    @Override
    public Charset charsetForName(String charsetName) {
        return charsetName.equalsIgnoreCase(base64.name()) ? base64 : null;
    }

    @Override
    public Iterator<Charset> charsets() {
        return Collections.singleton(base64).iterator();
    }

    private static final class Decoder extends CharsetDecoder {

        private byte[] buf = new byte[3];
        private int bufLen;

        protected Decoder(Charset cs) {
            super(cs, 4.f/3.f, 4.f);
        }

        @Override
        protected void implReset() {
            bufLen = 0;
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            if (in.hasArray() && out.hasArray())
                return decodeLoopArray(in, out);
            return decodeLoopBuffer(in, out);
        }

        @Override
        protected CoderResult implFlush(CharBuffer out) {
            if (bufLen > 0) {
                if (out.remaining() < 4)
                    return CoderResult.OVERFLOW;
                
                base64(out);
            }
            bufLen = 0;
            return CoderResult.UNDERFLOW;
        }

        private CoderResult decodeLoopArray(ByteBuffer in, CharBuffer out) {
            byte[] buf = this.buf;
            byte[] src = in.array();
            int srcPos = in.arrayOffset() + in.position();
            int srcLimit = in.arrayOffset() + in.limit();
            char[] dest = out.array();
            int destPos = out.arrayOffset() + out.position();
            int destLimit = out.arrayOffset() + out.limit();
            try {
                if (bufLen > 0) {
                    int consume = 3 - bufLen;
                    if (srcPos + consume <= srcLimit) {
                        if (destPos + 4 > destLimit)
                            return CoderResult.OVERFLOW;
                        
                        System.arraycopy(src, srcPos, buf, bufLen, consume);
                        bufLen = 0;
                        base64(buf, 0, dest, destPos);
                        srcPos += consume;
                        destPos += 4;
                    }
                }
                if (bufLen == 0) {
                    while (srcPos + 3 <= srcLimit) {
                        if (destPos + 4 > destLimit)
                            return CoderResult.OVERFLOW;
                        
                        base64(src, srcPos, dest, destPos);
                        srcPos += 3;
                        destPos += 4;
                    }
                }
                while (srcPos < srcLimit) {
                    if (bufLen < 2) {
                        buf[bufLen++] = src[srcPos++];
                    } else {
                        if (destPos + 4 > destLimit)
                            return CoderResult.OVERFLOW;
                        
                        buf[bufLen] = src[srcPos++];
                        bufLen = 0;
                        base64(buf, 0, dest, destPos);
                        destPos += 4;
                    }
                }
                return CoderResult.UNDERFLOW;
            } finally {
                in.position(srcPos - in.arrayOffset());
                out.position(destPos - out.arrayOffset());
            }
        }

        private CoderResult decodeLoopBuffer(ByteBuffer in, CharBuffer out) {
            byte[] buf = this.buf;
            while (in.remaining() + bufLen >= 3) {
                if (out.remaining() < 4)
                    return CoderResult.OVERFLOW;
                
                in.get(buf, bufLen, 3 - bufLen);
                bufLen = 0;
                base64(out);
            }
            while (in.hasRemaining()) {
                if (bufLen < 2) {
                    buf[bufLen++] = in.get();
                } else {
                    if (out.remaining() < 4)
                        return CoderResult.OVERFLOW;
                    
                    buf[bufLen] = in.get();
                    bufLen = 0;
                    base64(out);
                }
            }
            return CoderResult.UNDERFLOW;
        }

        private void base64(CharBuffer out) {
            byte[] buf = this.buf;
            byte b1, b2, b3;
            out.put(BASE64[((b1 = buf[0]) >>> 2) & 0x3F]);
            switch (bufLen) {
            case 0:
                out.put(BASE64[((b1 & 0x03) << 4)
                               | (((b2 = buf[1]) >>> 4) & 0x0F)]);
                out.put(BASE64[((b2 & 0x0F) << 2)
                               | (((b3 = buf[2]) >>> 6) & 0x03)]);
                out.put(BASE64[b3 & 0x3F]);
                return;
            case 1:
                out.put(BASE64[((b1 & 0x03) << 4)]);
                out.put('=');
                break;
            case 2:
                out.put(BASE64[((b1 & 0x03) << 4)
                               | (((b2 = buf[1]) >>> 4) & 0x0F)]);
                out.put(BASE64[(b2 & 0x0F) << 2]);
                break;
            }
            out.put('=');
        }

        private void base64(byte[] src, int srcPos, char[] dest, int destPos) {
            byte b1, b2, b3;
            dest[destPos] = BASE64[((b1 = src[srcPos]) >>> 2) & 0x3F];
            dest[destPos+1] = BASE64[((b1 & 0x03) << 4)
                                   | (((b2 = src[srcPos+1]) >>> 4) & 0x0F)];
            dest[destPos+2] = BASE64[((b2 & 0x0F) << 2)
                                   | (((b3 = src[srcPos+2]) >>> 6) & 0x03)];
            dest[destPos+3] = BASE64[b3 & 0x3F];
        }
    }

    private static final class Encoder extends CharsetEncoder {

        private char[] chBuf = new char[4];

        public Encoder(Charset cs) {
            super(cs, 3.f/4.f, 1.f, new byte[] { 0 });
        }

        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            if (in.hasArray() && out.hasArray())
                return encodeLoopArray(in, out);
            return encodeLoopBuffer(in, out);
        }

        private CoderResult encodeLoopArray(CharBuffer in, ByteBuffer out) {
            char[] src = in.array();
            int srcPos = in.arrayOffset() + in.position();
            int srcLimit = in.arrayOffset() + in.limit();
            byte[] dest = out.array();
            int destPos = out.arrayOffset() + out.position();
            int destLimit = out.arrayOffset() + out.limit();
            try {
                while (srcPos + 4 <= srcLimit) {
                    if (destPos + 3 > destLimit)
                        return CoderResult.OVERFLOW;
                    
                    destPos += invbase64(src, srcPos, dest, destPos);
                    srcPos += 4;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                in.position(srcPos - in.arrayOffset());
                out.position(destPos - out.arrayOffset());
            }
        }

        public CoderResult encodeLoopBuffer(CharBuffer in, ByteBuffer out) {
            char[] chBuf = this.chBuf;
            while (in.remaining() >= 4) {
                if (out.remaining() < 3)
                    return CoderResult.OVERFLOW;
                
                in.get(chBuf, 0, 4);
                invbase64(out);
            }
            return CoderResult.UNDERFLOW;
        }

        private void invbase64(ByteBuffer out) {
            char[] chBuf = this.chBuf;
            byte b2, b3;
            out.put((byte)((INV_BASE64[chBuf[0]] << 2)
                    | ((b2 = INV_BASE64[chBuf[1]]) >>> 4)));
            switch (chBuf[3] == '=' ? chBuf[2] == '=' ? 2 : 1 : 0) {
            case 0:
                out.put((byte)((b2 << 4)
                        | ((b3 = INV_BASE64[chBuf[2]]) >>> 2)));
                out.put((byte)((b3 << 6) | INV_BASE64[chBuf[3]]));
                break;
            case 1:
                out.put((byte)((b2 << 4)
                        | (INV_BASE64[chBuf[2]]) >>> 2));
                break;
            }
        }

        private int invbase64(char[] src, int srcPos, byte[] dest, int destPos) {
            byte b2, b3;
            dest[destPos] = (byte)((INV_BASE64[src[srcPos]] << 2)
                    | ((b2 = INV_BASE64[src[srcPos+1]]) >>> 4));
            switch (src[srcPos+3] == '=' ? src[srcPos+2] == '=' ? 2 : 1 : 0) {
            case 0:
                dest[destPos+1] = (byte)((b2 << 4)
                        | ((b3 = INV_BASE64[src[srcPos+2]]) >>> 2));
                dest[destPos+2] = (byte)((b3 << 6) | INV_BASE64[src[srcPos+3]]);
                return 3;
            case 1:
                dest[destPos+1] = (byte)((b2 << 4)
                        | (INV_BASE64[src[srcPos+2]]) >>> 2);
                return 2;
            default:
                return 1;
            }
        }
    }
}
