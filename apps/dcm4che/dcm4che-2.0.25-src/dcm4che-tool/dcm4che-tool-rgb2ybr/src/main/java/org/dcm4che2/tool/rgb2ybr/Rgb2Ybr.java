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

package org.dcm4che2.tool.rgb2ybr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputHandler;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.util.CloseUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 5551 $ $Date: 2007-11-27 15:24:27 +0100 (Tue, 27 Nov 2007) $
 * @since May 14, 2006
 *
 */
public class Rgb2Ybr implements DicomInputHandler  {

    private static final String USAGE =
        "rgb2ybr [-piVh] <infile> <outfile>";
    private static final String DESCRIPTION = 
        "Convert pixel data of DICOM file from RGB to YBR\nOptions:";
    private static final String EXAMPLE = null;
    private static final String RGB = "RGB";
    private static final String YBR_FULL = "YBR_FULL";
    private static final String YBR_PARTIAL = "YBR_PARTIAL";
    private static double[] TO_YBR_FULL = {
        .2990, .5870, .1140, 0.5,
        -.1687, -.3313, .5000, 128.5,
        .5000, -.4187, -.0813, 128.5
    };
    private static double[] TO_YBR_PARTIAL = {
        .2568, .5041, .0979, 16.5,
        -.1482, -.2910, .4392, 128.5,
        .4392, -.3678, -.0714, 128.5
    };
    private static final double[] FROM_YBR_FULL = {
        1, -3.681999032610751E-5, 1.4019875769352639, -178.94969688895202, 
        1, -0.34411328131331737, -0.7141038211151132, 135.95178911083912, 
        1, 1.7719781167370596, -1.345834129159976E-4, -226.29597226549038
    };

    private static final double[] FROM_YBR_PARTIAL = {
        1.1644154634373545, -9.503599204778129E-5, 1.5960018776303868, -222.40672314470507, 
        1.1644154634373545, -0.39172456367367336, -0.8130133682767554, 135.97580787465722, 
        1.1644154634373545, 2.017290682233469, -1.3527300480981362E-4, -276.42653979626605
    };
    
    private boolean partial = false;
    private boolean invers = false;
    private DicomOutputStream dos;
    private File ofile;
   
    public final void setInvers(boolean invers) {
        this.invers = invers;
    }

    public final void setPartial(boolean partial) {
        this.partial = partial;
    }


    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        opts.addOption("p", "partial", false,
                "convert to YBR_PARTIAL instead to YBR_FULL (=default)");
        opts.addOption("i", "invers", false, "convert from YBR_* to RGB");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new PosixParser().parse(opts, args);
        } catch (ParseException e) {
            exit("rgb2ybr: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = Rgb2Ybr.class.getPackage();
            System.out.println("rgb2ybr v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() != 2) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        return cl;
    }

   
    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'rgb2ybr -h' for more information.");
        System.exit(1);
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parse(args);
            List argList = cl.getArgList();
            File ifile = new File((String) argList.get(0));
            File ofile = new File((String) argList.get(1));
            Rgb2Ybr rgb2ybr = new Rgb2Ybr();
            rgb2ybr.setPartial(cl.hasOption("p"));
            rgb2ybr.setInvers(cl.hasOption("i"));
            rgb2ybr.setOutput(ofile);
            long start = System.currentTimeMillis();
            rgb2ybr.convert(ifile);
            long fin = System.currentTimeMillis();
            System.out.println("Convert " + ifile + " to " + ofile + " in "
                    + (fin - start) + "ms.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setOutput(File ofile) {
        this.ofile = ofile;
    }

    public void convert(File ifile) throws IOException {
        DicomInputStream dis = new DicomInputStream(ifile);
        try {
            dis.setHandler(this);
            DicomObject dcmobj = dis.readDicomObject();
            if (dos == null) {
                throw new IOException("No Pixel Data");
            }
            if (!dcmobj.isEmpty()) {
                dos.writeDataset(dcmobj, dis.getTransferSyntax());
            }
        } finally {
            dis.close();
            CloseUtils.safeClose(dos);
        }
    }

    public boolean readValue(DicomInputStream in) throws IOException {
        if ((in.tag() & 0xffffffffL) == Tag.PixelData && in.level() == 0) {
            convert(in);
            return true;
        }
        return in.readValue(in);
    }

    private void convert(DicomInputStream in) throws IOException {
        if (in.valueLength() == -1) {
            throw new IOException("Encapsulated Pixel Data");
        }
        DicomObject attrs = in.getDicomObject();
        String pmi = attrs.getString(Tag.PhotometricInterpretation);
        if (invers) {
            if (YBR_FULL.equals(pmi)) {
                convert(in, FROM_YBR_FULL, RGB);
            } else if (YBR_PARTIAL.equals(pmi)) {
                convert(in, FROM_YBR_PARTIAL, RGB);
            } else {
                throw new IOException("Wrong Photometric Interpretation: "
                        + pmi);
            }
        } else {
            if (RGB.equals(pmi)) {
                if (partial) {
                    convert(in, TO_YBR_PARTIAL, YBR_PARTIAL);
                } else {
                    convert(in, TO_YBR_FULL, YBR_FULL);                    
                }
            } else {
                throw new IOException("Wrong Photometric Interpretation: "
                        + pmi);
            }
        }
    }

    private void convert(DicomInputStream in, double[] k, String pmi) 
            throws IOException {
        DicomObject attrs = in.getDicomObject();
        check("Unsupported Bits Allocated: ", 8,
                attrs.getInt(Tag.BitsAllocated));
        check("Wrong Samples per Pixel: ", 3,
                attrs.getInt(Tag.SamplesPerPixel));
        int valLen = in.valueLength();
        int planeLen = attrs.getInt(Tag.Columns) * attrs.getInt(Tag.Rows);
        int nFrames = attrs.getInt(Tag.NumberOfFrames, 1);
        int padded = valLen - planeLen * nFrames * 3;
        if (padded < 0) {
            throw new IOException("Too short Pixel Data: " + valLen);
        }
        boolean byPlane = attrs.getInt(Tag.PlanarConfiguration) != 0;
        attrs.putString(Tag.PhotometricInterpretation, VR.CS, pmi);
        FileOutputStream fos = new FileOutputStream(ofile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        dos = new DicomOutputStream(bos);
        dos.writeDicomFile(attrs);
        attrs.clear();
        dos.writeHeader(in.tag(), in.vr(), valLen);        
        if (byPlane) {
            byte[] r = new byte[planeLen];
            byte[] g = new byte[planeLen];
            byte[] b = new byte[planeLen];
            int r1, g1, b1;
            for (int i = 0; i < nFrames; i++) {
                in.readFully(r);
                in.readFully(g);
                in.readFully(b);
                for (int j = 0; j < r.length; j++) {
                    r1 = r[j] & 0xff;
                    g1 = g[j] & 0xff;
                    b1 = b[j] & 0xff;
                    r[j] = (byte) Math.max(0,Math.min(255,k[0]*r1 + k[1]*g1 + k[2]*b1 + k[3]));
                    g[j] = (byte) Math.max(0,Math.min(255,k[4]*r1 + k[5]*g1 + k[6]*b1 + k[7]));
                    b[j] = (byte) Math.max(0,Math.min(255,k[8]*r1 + k[9]*g1 + k[10]*b1 + k[11]));
                }
                dos.write(r);
                dos.write(g);
                dos.write(b);
            }
        } else {
            int r, g, b;
            for (int i = 0; i < nFrames; i++) {
                for (int j = 0; j < planeLen; j++) {
                    r = in.read();
                    g = in.read();
                    b = in.read();
                    dos.write((int) Math.max(0,Math.min(255,k[0]*r + k[1]*g + k[2]*b + k[3])));
                    dos.write((int) Math.max(0,Math.min(255,k[4]*r + k[5]*g + k[6]*b + k[7])));
                    dos.write((int) Math.max(0,Math.min(255,k[8]*r + k[9]*g + k[10]*b + k[11])));
                }
            }
        }
        for (int i = 0; i < padded; i++) {
            dos.write(in.read());
        }
    }

    private void check(String errmsg, int exp, int val) throws IOException {
        if (exp != val)
            throw new IOException(errmsg + val);           
    }

}
