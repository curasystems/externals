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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package org.dcm4che2.image;

import java.awt.color.ColorSpace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author  gunter.zeilinger@tiani.com
* @version $Revision$ $Date$
*/
public final class SimpleYBRColorSpace extends ColorSpace {
    private static final Logger log = LoggerFactory.getLogger(SimpleYBRColorSpace.class);

    private static final long serialVersionUID = 94133999660773774L;

    private static double[] TO_YBR_FULL = {
        0.2990, 0.5870, 0.1140, 0.0,
        -0.1687, -0.3313, 0.5, 0.5,
        0.5, -0.4187, -0.0813, 0.5
    };
    
    private static double[] TO_YBR_PARTIAL = {
        0.2568, 0.5041, 0.0979, 0.0625,
        -0.1482, -0.2910, 0.4392, 0.5,
        0.4392, -0.3678, -0.0714, 0.5
    };
    
    private static final double[] FROM_YBR_FULL = {
        1.0, -3.681999032610751E-5, 1.4019875769352639, -0.7009753784724688, 
        1.0, -0.34411328131331737, -0.7141038211151132, 0.5291085512142153, 
        1.0, 1.7719781167370596, -1.345834129159976E-4, -0.8859217666620718, 
    };

    private static final double[] FROM_YBR_PARTIAL = {
        1.1644154634373545, -9.503599204778129E-5, 1.5960018776303868, -0.8707293872840042, 
        1.1644154634373545, -0.39172456367367336, -0.8130133682767554, 0.5295929995103797, 
        1.1644154634373545, 2.017290682233469, -1.3527300480981362E-4, -1.0813536710791642, 
    };
    
    private final ColorSpace csRGB;
    private final double[] toYBR;
    private final double[] toRGB;
    
    public static ColorSpace createYBRFullColorSpace(ColorSpace rgbCS) {
        return new SimpleYBRColorSpace(rgbCS, TO_YBR_FULL, FROM_YBR_FULL);
    }
    
    public static ColorSpace createYBRPartialColorSpace(ColorSpace rgbCS) {
        return new SimpleYBRColorSpace(rgbCS, TO_YBR_PARTIAL, FROM_YBR_PARTIAL);
    }
   
    private SimpleYBRColorSpace(ColorSpace csRGB, double[] toYBR, double[] fromYBR) {
        super(TYPE_YCbCr, 3);
        this.csRGB = csRGB;
        this.toYBR = toYBR;
        this.toRGB = fromYBR;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof SimpleYBRColorSpace;
    }
    
    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public float[] toRGB(float[] ybr) {
        float r = (float) Math.max(0.0, Math.min(1.0, toRGB[0] * ybr[0] 
                         + toRGB[1] * ybr[1]
                         + toRGB[2] * ybr[2]
                         + toRGB[3]));
        float g = (float) Math.max(0.0, Math.min(1.0, toRGB[4] * ybr[0] 
                        + toRGB[5] * ybr[1]
                        + toRGB[6] * ybr[2]
                        + toRGB[7]));
        float b = (float) Math.max(0.0, Math.min(1.0, toRGB[8] * ybr[0] 
                        + toRGB[9] * ybr[1]
                        + toRGB[10] * ybr[2]
                        + toRGB[11]));
        //log.info("Convert "+ybr[0]+","+ybr[1]+","+ybr[2]+" to "+r+","+g+","+b);
        return new float[] {r, g, b};
    }

    @Override
    public float[] fromRGB(float[] rgb) {
        float y = (float) Math.max(0.0, Math.min(1.0, toYBR[0] * rgb[0] 
                        + toYBR[1] * rgb[1]
                        + toYBR[2] * rgb[2]
                        + toYBR[3]));
        float cb = (float) Math.max(0.0, Math.min(1.0, toYBR[4] * rgb[0] 
                       + toYBR[5] * rgb[1]
                       + toYBR[6] * rgb[2]
                       + toYBR[7]));
        float cr = (float) Math.max(0.0, Math.min(1.0, toYBR[8] * rgb[0] 
                       + toYBR[9] * rgb[1]
                       + toYBR[10] * rgb[2]
                       + toYBR[11]));
        return new float[] {y, cb, cr};
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        return csRGB.toCIEXYZ(toRGB(colorvalue));
    }

    @Override
    public float[] fromCIEXYZ(float[] xyzvalue) {
        return fromRGB(csRGB.fromCIEXYZ(xyzvalue));
    }
    
    public static void main(String[] args) {
        out("FROM_YBR_FULL", fromYBR(TO_YBR_FULL));
        out("FROM_YBR_PARTIAL", fromYBR(TO_YBR_PARTIAL));
    }

    private static void out(String label, double[] a) {
    	StringBuffer sb = new StringBuffer();
        sb.append("\n    private static final double[] ");
        sb.append(label);
        sb.append(" = {");
        for (int i = 0; i < a.length; i++) {
            if (i % 4 == 0)
            	sb.append("\n        ");
            sb.append(a[i]);
            sb.append(", ");
        }
        sb.append("\n    };");
        log.info(sb.toString());
    }

    private static double[] fromYBR(double[] a) {
        double[] toRGB = new double[12];
        double det = a[0]*a[5]*a[10] + a[1]*a[6]*a[8] + a[2]*a[4]*a[9]
                   - a[2]*a[5]*a[8] - a[1]*a[4]*a[10] - a[0]*a[6]*a[9];
        toRGB[0] = (a[5]*a[10] - a[6]*a[9]) / det;
        toRGB[1] = (a[2]*a[9] - a[1]*a[10]) / det;
        toRGB[2] = (a[1]*a[6] - a[2]*a[5]) / det;
        toRGB[3] = (a[2]*a[5]*a[11] + a[1]*a[7]*a[10] + a[3]*a[6]*a[9]
                  - a[3]*a[5]*a[10] - a[1]*a[6]*a[11] - a[2]*a[7]*a[9]) / det;
        toRGB[4] = (a[6]*a[8] - a[4]*a[10]) / det;
        toRGB[5] = (a[0]*a[10] - a[2]*a[8]) / det;
        toRGB[6] = (a[2]*a[4] - a[0]*a[6]) / det;
        toRGB[7] = (a[2]*a[7]*a[8] + a[3]*a[4]*a[10] + a[0]*a[6]*a[11]
                  - a[0]*a[7]*a[10] - a[3]*a[6]*a[8] - a[2]*a[4]*a[11]) / det;
        toRGB[8] = (a[4]*a[9] - a[5]*a[8]) / det;
        toRGB[9] = (a[1]*a[8] - a[0]*a[9]) / det;
        toRGB[10] = (a[0]*a[5] - a[1]*a[4]) / det;
        toRGB[11] = (a[3]*a[5]*a[8] + a[1]*a[4]*a[11] + a[0]*a[7]*a[9]
                   - a[0]*a[5]*a[11] - a[1]*a[7]*a[8] - a[3]*a[4]*a[9]) / det;
        return toRGB;
    }
    
}
