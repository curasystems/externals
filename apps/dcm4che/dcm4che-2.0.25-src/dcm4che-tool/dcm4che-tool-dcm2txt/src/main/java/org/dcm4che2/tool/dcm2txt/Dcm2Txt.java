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

package org.dcm4che2.tool.dcm2txt;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputHandler;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.TagUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 5551 $ $Date: 2007-11-27 15:24:27 +0100 (Tue, 27 Nov 2007) $
 * @since Aug 15, 2005
 */
public class Dcm2Txt implements DicomInputHandler {

    private static final int DEF_MAX_WIDTH = 78;
    private static final int MIN_MAX_WIDTH = 32;
    private static final int MAX_MAX_WIDTH = 512;
    private static final int DEF_MAX_VAL_LEN = 64;
    private static final int MIN_MAX_VAL_LEN = 16;
    private static final int MAX_MAX_VAL_LEN = 512;    
    private static final String USAGE =
        "dcm2txt [-cVh] [-l <max>] [-w <max>] <dcmfile>";
    private static final String DESCRIPTION = 
        "Dump DICOM file and data set\nOptions:";
    private static final String EXAMPLE = null;

    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        Option width = new Option("w", "width", true, 
                "maximal number of characters per line, by default: 80");
        width.setArgName("max");
        opts.addOption(width);
        Option vallen = new Option("l", "vallen", true, 
                "limit value prompt to <maxlen> characters, by default: 64");
        vallen.setArgName("max");
        opts.addOption(vallen);
        opts.addOption("c", "compact", false, "dump without attribute names");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new PosixParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcm2txt: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = Dcm2Txt.class.getPackage();
            System.out.println("dcm2txt v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().isEmpty()) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        return cl;
    }

   
    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcm2txt -h' for more information.");
        System.exit(1);
    }

    public static void main(String[] args) {
        CommandLine cl = parse(args);
        Dcm2Txt dcm2txt = new Dcm2Txt();
        dcm2txt.setWithNames(!cl.hasOption("c"));
        if (cl.hasOption("w"))
            dcm2txt.setMaxWidth(parseInt(cl.getOptionValue("w"), "w",
                    MIN_MAX_WIDTH, MAX_MAX_WIDTH));
        if (cl.hasOption("l"))
            dcm2txt.setMaxValLen(parseInt(cl.getOptionValue("l"), "l",
                    MIN_MAX_VAL_LEN, MAX_MAX_VAL_LEN));
        File ifile = new File((String) cl.getArgList().get(0));
        try {
            dcm2txt.dump(ifile);
        } catch (IOException e) {
            System.err.println("dcm2txt: Failed to dump " + ifile + ": "
                    + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
    
    private static int parseInt(String s, String opt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {
            // parameter is not a valid integer; fall through to exit
        }
        exit("illegal argument for option -" + opt);
        throw new RuntimeException();
    }

    private StringBuffer line = new StringBuffer();
    private char[] cbuf = new char[64];
    private boolean withNames = true;
    private int maxWidth = DEF_MAX_WIDTH;
    private int maxValLen = DEF_MAX_VAL_LEN;

    public final void setMaxValLen(int maxValLen) {
        this.maxValLen = maxValLen;
    }


    public final void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }


    public final void setWithNames(boolean withNames) {
        this.withNames = withNames;
    }

    public void dump(File ifile) throws IOException {
        DicomInputStream dis = new DicomInputStream(ifile);
        try {
            dis.setHandler(this);
            dis.readDicomObject(new BasicDicomObject(), -1);
        } finally {
            dis.close();
        }
    }

    public boolean readValue(DicomInputStream in) throws IOException {
            switch (in.tag()) {
            case Tag.Item:
                if (in.sq().vr() != VR.SQ && in.valueLength() != -1) {
                    outFragment(in);
                } else {
                    outItem(in);                    
                }
                break;
            case Tag.ItemDelimitationItem:
            case Tag.SequenceDelimitationItem: 
                if (in.level() > 0)
                    outItem(in);
                break;
            default:
                outElement(in);
            }
        return true;
    }


    private void outElement(DicomInputStream in) throws IOException {
        outTag(in);
        outVR(in);
        outLen(in);
        if (hasItems(in)) {
            outLine(in);
            readItems(in);
        } else {
            outValue(in);
            outLine(in);
        }
    }

    private void outValue(DicomInputStream in) throws IOException {
        int tag = in.tag();
        VR vr = in.vr();
        byte[] val = in.readBytes(in.valueLength());
        DicomObject dcmobj = in.getDicomObject();
        boolean bigEndian = in.getTransferSyntax().bigEndian();
        line.append(" [");
        vr.promptValue(val, bigEndian, dcmobj.getSpecificCharacterSet(),
                cbuf, maxValLen, line);
        line.append("]");
        if (tag == Tag.SpecificCharacterSet
                || tag == Tag.TransferSyntaxUID
                || TagUtils.isPrivateCreatorDataElement(tag)) {
            dcmobj.putBytes(tag, vr, val, bigEndian);
        }
        if (tag == 0x00020000) {
            in.setEndOfFileMetaInfoPosition(
                    in.getStreamPosition() + vr.toInt(val, bigEndian));
        }
    }

    private boolean hasItems(DicomInputStream in) {
        return in.valueLength() == -1 || in.vr() == VR.SQ;
    }

    private void readItems(DicomInputStream in) throws IOException {
        in.readValue(in);
        in.getDicomObject().remove(in.tag());
    }

    private void outItem(DicomInputStream in) throws IOException {
        outTag(in);
        outLen(in);
        outLine(in);
        in.readValue(in);
    }

    private void outFragment(DicomInputStream in) throws IOException {
        outTag(in);
        outLen(in);
        in.readValue(in);
        DicomElement sq = in.sq();
        byte[] data = sq.removeFragment(0);
        boolean bigEndian = in.getTransferSyntax().bigEndian();
        line.append(" [");
        sq.vr().promptValue(data, bigEndian, null, cbuf, maxValLen, line);
        line.append("]");
        outLine(in);
    }
    
    private void outTag(DicomInputStream in) {
        line.setLength(0);
        line.append(in.tagPosition()).append(':');
        for (int i = in.level(); i > 0; --i)
            line.append('>');
        TagUtils.toStringBuffer(in.tag(), line);
    }

    private void outVR(DicomInputStream in) {
        line.append(" ").append(in.vr());
    }
    
    private void outLen(DicomInputStream in) {
        line.append(" #").append(in.valueLength());
    }

    private void outLine(DicomInputStream in) {
        if (withNames)
            line.append(" ").append(in.getDicomObject().nameOf(in.tag()));
        if (line.length() > maxWidth)
            line.setLength(maxWidth);
        System.out.println(line);
    }


}
