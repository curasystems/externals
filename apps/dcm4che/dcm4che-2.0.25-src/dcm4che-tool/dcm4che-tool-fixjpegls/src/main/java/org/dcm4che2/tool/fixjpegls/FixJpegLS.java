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
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2010
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

package org.dcm4che2.tool.fixjpegls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.io.DicomInputHandler;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.ByteUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Rev$ $Date:: 0000-00-00 $
 * @since Oct 22, 2010
 */
public class FixJpegLS {

    private static final String USAGE ="fixjpegls [Options] SOURCE DEST";
    private static final String DESCRIPTION = "    or fixjpegls [Options] "
            + "SOURCE... DIRECTORY\n.\n"
            + "Patch faulty DICOM JPEG-LS images compressed by JAI-IMAGEIO "
            + "JPEG-LS encoder by inserting a LSE marker segment with "
            + "encoder parameter values T1, T2 and T3 actually used by "
            + "JAI-IMAGEIO JPEG-LS encoder.\n.\n"
            + "Options:";
    private static final String NO_CHECK_IMPL_CUID = "fix also DICOM files "
            + "with different Implementation Class UID than specified by "
            + "option --check-impl-cuid";
    private static final String CHECK_IMPL_CUID = "Implementation Class UID "
            + "of files to fix; default: `1.2.40.0.13.1.1'";
    private static final String NO_NEW_IMPL_CUID = "do not replace "
            + "Implementation Class UID in fixed files with UID specified by "
            + "option --new-impl-cuid";
    private static final String NEW_IMPL_CUID =  "Implementation Class UID "
            + "inserted in fixed files; default: `1.2.40.0.13.1.1.1'";
    private static final String EXAMPLE = null;

    private static final int SOI = 0xffd8;
    private static final int SOF55 = 0xfff7;
    private static final int LSE = 0xfff8;
    private static final int SOS = 0xffda;
    private static final byte[] LSE_13 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0x1f, (byte) 0xff,
        (byte) 0x00, (byte) 0x22,  // T1 = 34
        (byte) 0x00, (byte) 0x83,  // T2 = 131
        (byte) 0x02, (byte) 0x24,  // T3 = 548
        (byte) 0x00, (byte) 0x40
    };
    private static final byte[] LSE_14 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0x3f, (byte) 0xff,
        (byte) 0x00, (byte) 0x42, // T1 = 66
        (byte) 0x01, (byte) 0x03, // T2 = 259
        (byte) 0x04, (byte) 0x44, // T3 = 1092
        (byte) 0x00, (byte) 0x40
    };
    private static final byte[] LSE_15 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0x7f, (byte) 0xff,
        (byte) 0x00, (byte) 0x82, // T1 = 130
        (byte) 0x02, (byte) 0x03, // T2 = 515
        (byte) 0x08, (byte) 0x84, // T3 = 2180
        (byte) 0x00, (byte) 0x40
    };
    private static final byte[] LSE_16 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0xff, (byte) 0xff,
        (byte) 0x01, (byte) 0x02, // T1 = 258
        (byte) 0x04, (byte) 0x03, // T2 = 1027
        (byte) 0x11, (byte) 0x04, // T3 = 4356
        (byte) 0x00, (byte) 0x40
    };
    private static final byte[] PADDING_BYTE = { 0 };

    private static final UIDDictionary DICT = UIDDictionary.getDictionary();
    
    private String implClassUID = "1.2.40.0.13.1.1";
    private String newImplClassUID = "1.2.40.0.13.1.1.1";

    @SuppressWarnings("serial")
    private static final class NoFixException extends IOException {

        public NoFixException(String message) {
            super(message);
        }
    }

    private static final class Replacement {
        final long pos;
        final int len;
        final byte[] val;
        Replacement(long pos, int len, byte[] val) {
            super();
            this.pos = pos;
            this.len = len;
            this.val = val;
        }
    }

    private final class Replacements implements DicomInputHandler {

        boolean pixelData;
        boolean fmi = true;
        List<Replacement> replacements;
        int numItems;
        int bitsStored;

        public boolean readValue(DicomInputStream in) throws IOException {
            int tag = in.tag();
            int len = in.valueLength();
            long pos = in.getStreamPosition();
            DicomObject attrs = in.getDicomObject();
            String uid;
            if (fmi && tag >= 0x00080000) {
                if (replacements == null)
                    throw new NoFixException(
                            "File Meta Information (0002,eeee) is missing");
                if (FixJpegLS.this.implClassUID != null) {
                    uid = attrs.getString(Tag.ImplementationClassUID);
                    if (!FixJpegLS.this.implClassUID.equals(uid))
                        throw new NoFixException(
                                "Implementation Class UID (0002,0012) = "
                                + uid);
                }
                fmi = false;
            }
            switch(tag) {
            case Tag.FileMetaInformationGroupLength:
            case Tag.ItemDelimitationItem:
            case Tag.SequenceDelimitationItem:
                return in.readValue(in);
            case Tag.TransferSyntaxUID:
                in.readValue(in);
                uid = attrs.getString(Tag.TransferSyntaxUID);
                if (!UID.JPEGLSLossless.equals(uid))
                    throw new NoFixException(
                            "Transfer Syntax UID (0002,0010) = " 
                            + DICT.prompt(uid));
                replacements = new ArrayList<Replacement>();
                return true;
            case Tag.ImplementationClassUID:
                if (replacements == null)
                    throw new NoFixException(
                        "File Meta Information (0002,eeee) is missing");

                in.readValue(in);
                if (FixJpegLS.this.newImplClassUID != null)
                    addImplClassUIDReplacements(pos, len,
                            (int) in.getEndOfFileMetaInfoPosition());
                return true;
            case Tag.PixelData:
                if (in.level() == 0) {
                    if (len != -1)
                        throw new NoFixException(
                                "Pixel Data is not encapsulated into Data Fragments");

                    pixelData = true;
                }
                return in.readValue(in);
            case Tag.Item:
                if (pixelData) {
                    if (len == 0)
                        return true;
                    byte[] jpegheader = new byte[17];
                    in.readFully(jpegheader);
                    byte[] lse = selectLSE(jpegheader);
                    in.skipFully(len - 18);
                    addItemReplacements(pos, len, lse, in.read() == 0);
                    numItems++;
                    return true;
                }
            }
            pixelData = false;
            if (len == -1)
                return in.readValue(in);
            in.skipFully(len);
            return true;
        }

        @SuppressWarnings("deprecation")
        private void addImplClassUIDReplacements(long pos, int len,
                int eoffmipos) {
            int uidlen = FixJpegLS.this.newImplClassUID.length();
            int newlen = (uidlen + 1) & ~1;
            if (eoffmipos > 0) {
                byte[] newfmilen = new byte[4];
                ByteUtils.int2bytesLE(
                        eoffmipos - 144 - len + newlen, newfmilen, 0);
                replacements.add(new Replacement(140, 4, newfmilen));
            }
            byte[] newval = new byte[newlen + 2];
            ByteUtils.ushort2bytesLE(newlen, newval, 0);
            FixJpegLS.this.newImplClassUID.getBytes(0, uidlen, newval, 2);
            replacements.add(new Replacement(pos - 2, len + 2, newval));
        }

        private void addItemReplacements(long pos, int len, byte[] lse,
                boolean padded) {
            int newlen = len + (padded ? 14 : 15);
            replacements.add(new Replacement(pos - 4, 4, 
                    ByteUtils.int2bytesLE((newlen + 1) & ~1,
                            new byte[4], 0)));
            replacements.add(new Replacement(pos + 15, 0, lse));
            boolean newPadded = (newlen & 1) != 0;
            if (newPadded != padded)
                replacements.add(newPadded
                        ? new Replacement(pos + len, 0, PADDING_BYTE)
                        : new Replacement(pos + len - 1, 1, null));
        }

        public void applyTo(File source, File target) throws IOException {
            boolean failed = true;
            FileInputStream fin = new FileInputStream(source);
            try {
                FileChannel in = fin.getChannel();
                FileOutputStream fos = new FileOutputStream(target);
                try {
                    FileChannel out = fos.getChannel();
                    long pos = 0L;
                    for (Replacement replacement : replacements) {
                        in.transferTo(pos, replacement.pos - pos, out);
                        if (replacement.val != null)
                            out.write(ByteBuffer.wrap(replacement.val));
                        pos = replacement.pos + replacement.len;
                    }
                    in.transferTo(pos, source.length() - pos, out);
                    failed = false;
                } finally {
                    fos.close();
                    if (failed == true)
                        target.delete();
                }
            } finally {
                fin.close();
            }
        }

        private byte[] selectLSE(byte[] jpegheader) throws NoFixException {
            if (ByteUtils.bytesBE2ushort(jpegheader, 0) != SOI)
                throw new NoFixException("SOI marker is missing");
            if (ByteUtils.bytesBE2ushort(jpegheader, 2) != SOF55)
                throw new NoFixException(
                        "SOI marker is not followed by JPEG-LS SOF marker");
            if (ByteUtils.bytesBE2ushort(jpegheader, 4) != 11)
                throw new NoFixException(
                        "unexpected length of JPEG-LS SOF marker segment");
            int marker = ByteUtils.bytesBE2ushort(jpegheader, 15);
            if (marker != SOS) {
                throw new NoFixException(marker == LSE
                    ? "contains already LSE marker segment"
                    : "JPEG-LS SOF marker segment is not followed by SOS marker" );
            }
            switch (bitsStored = jpegheader[6]) {
            case 13:
                return LSE_13;
            case 14:
                return LSE_14;
            case 15:
                return LSE_15;
            case 16:
                return LSE_16;
            }
            throw new NoFixException("JPEG-LS " + bitsStored + "-bit");
        }

    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseCommandLine(args);
            if (cl == null) return;
            List<String> argList = cl.getArgList();
            File target = removeTarget(argList);
            FixJpegLS fixJpegLS = new FixJpegLS();
            if (cl.hasOption("no-check-impl-cuid"))
                fixJpegLS.setImplClassUID(null);
            else if (cl.hasOption("check-impl-cuid"))
                fixJpegLS.setImplClassUID(
                        cl.getOptionValue("check-impl-cuid"));
            if (cl.hasOption("no-new-impl-cuid"))
                fixJpegLS.setNewImplClassUID(null);
            else if (cl.hasOption("new-impl-cuid"))
                fixJpegLS.setNewImplClassUID(
                        cl.getOptionValue("new-impl-cuid"));
            int[] counts = new int[2];
            long start = System.currentTimeMillis();
            for (String arg : argList)
                fixJpegLS.fix(new File(arg), target, counts);
            long end = System.currentTimeMillis();
            System.out.println();
            System.out.println("Fix " + counts[1] + " of " + counts[0]
                    + " scanned files in " + ((end - start) / 1000.f) + " s.");
        } catch (ParseException e) {
            System.err.println("fixjpegls: " + e.getMessage());
            System.err.println("Try `fixjpegls --help' for more information.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("fixjpegls: " + e.getMessage());
            System.exit(1);
        }

    }

    @SuppressWarnings("static-access")
    private static CommandLine parseCommandLine(String[] args)
            throws ParseException {
        Options options = new Options();
        options.addOption(
            OptionBuilder.withLongOpt("no-check-impl-cuid")
                         .withDescription(NO_CHECK_IMPL_CUID)
                         .create());
        options.addOption(
            OptionBuilder.withLongOpt("check-impl-cuid")
                         .hasArg()
                         .withArgName("uid")
                         .withDescription(CHECK_IMPL_CUID)
                         .create());
        options.addOption(
            OptionBuilder.withLongOpt("no-new-impl-cuid")
                         .withDescription(NO_NEW_IMPL_CUID)
                         .create());
        options.addOption(
            OptionBuilder.withLongOpt("new-impl-cuid")
                         .hasArg()
                         .withArgName("uid")
                         .withDescription(NEW_IMPL_CUID)
                         .create());
        options.addOption(
            OptionBuilder.withLongOpt("help")
                         .withDescription("display this help and exit")
                         .create());
        options.addOption(
            OptionBuilder.withLongOpt("version")
                         .withDescription("output version information and exit")
                         .create());
        CommandLineParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, args);
        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, options, EXAMPLE);
            return null;
        }
        if (cl.hasOption("version")) {
            System.out.println("fixjpegls "
                    + FixJpegLS.class.getPackage().getImplementationVersion());
            return null;
        }
        return cl;
    }

    private static File removeTarget(List<String> argList) 
            throws ParseException, IOException {
        int size = argList.size();
        if (size < 2)
            throw new ParseException(size == 0
                    ? "missing file operand"
                    : "missing destination file operand after `"
                        + argList.get(0) + "'");
        String targetName = argList.get(size-1);
        File target = new File(targetName);
        if (size > 2 && !target.isDirectory()) {
            throw new IOException("target `" + targetName 
                    + "' is not a directory");
        }
        argList.remove(size-1);
        return target;
    }

    public final String getImplClassUID() {
        return implClassUID;
    }

    public final void setImplClassUID(String implClassUID) {
        this.implClassUID = implClassUID;
    }

    public final String getNewImplClassUID() {
        return newImplClassUID;
    }

    public final void setNewImplClassUID(String newImplClassUID) {
        this.newImplClassUID = newImplClassUID;
    }

    public void fix(File source, File target, int[] counts) {
        if (target.isDirectory() && source.isFile())
            target = new File(target, source.getName());
        fixRecursive(source, target, counts);
    }

    private void fixRecursive(File source, File target, int[] counts) {
        if (!source.exists()) {
            System.err.println("no such file or directory `" + source + "'");
        } else if (source.isDirectory()) {
            if (!target.exists()) {
                if (!target.mkdir())
                    System.err.println("failed to create directory `"
                            + target + "'");
            } if (target.isFile()) {
                System.err.println("cannot overwrite non-directory `"
                        + target + "' with directory `" + source + "'");
                return;
            }
            String[] ss = source.list();
            for (String s : ss) {
                fixRecursive(new File(source, s), new File(target, s), counts);
            }
        } else if (target.isDirectory()) {
            System.err.println("cannot overwrite directory `"
                    + target + "' with non-directory `" + source + "'");
        } else {
            Replacements replacements = new Replacements();
            try {
                counts[0]++;
                DicomInputStream din = new DicomInputStream(source);
                try {
                    din.setHandler(replacements);
                    din.readDicomObject();
                } finally {
                    try { din.close(); } catch (IOException ignore) {}
                }
                if (replacements.numItems == 0)
                    throw new NoFixException("no Pixel Data Fragments");
                replacements.applyTo(source, target);
                counts[1]++;
                System.out.println("PATCHED " + source + ": JPEG-LS "
                        + replacements.bitsStored + "-bit -> " + target);
            } catch (Exception e) {
                System.out.println("skipped " + source + ": " + e.getMessage());
            }
        }
    }


}
