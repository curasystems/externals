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

package org.dcm4che2.tool.pdf2dcm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.util.UIDUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 12456 $ $Date: 2009-11-26 13:51:53 +0100 (Thu, 26 Nov 2009) $
 * @since Apr 1, 2006
 *
 */
public class Pdf2Dcm {

    private static final String USAGE = 
        "pdf2dcm [Options] <pdffile> <dcmfile>";
    private static final String DESCRIPTION = 
        "Encapsulate PDF Document into DICOM Object.\nOptions:";
    private static final String EXAMPLE = 
        "pdf2dcm -c pdf2dcm.cfg report.pdf report.dcm\n" +
        "=> Encapulate PDF Document report.pdf into DICOM Object stored to " +
        "report.dcm using DICOM Attribute values specified in Configuration " +
        "file pdf2dcm.cfg.";
    
    private String transferSyntax = UID.ExplicitVRLittleEndian;
    private String charset = "ISO_IR 100";
    private int bufferSize = 8192;
    private Properties cfg = new Properties();

    public Pdf2Dcm() {
        try {
            cfg.load(Pdf2Dcm.class.getResourceAsStream("pdf2dcm.cfg"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public final void setCharset(String charset) {
        this.charset = charset;
    }

    public final void setBufferSize(int bufferSize) {
        if (bufferSize < 64) {
            throw new IllegalArgumentException("bufferSize: " + bufferSize);
        }
        this.bufferSize = bufferSize;
    }

    public final void setTransferSyntax(String transferSyntax) {
        this.transferSyntax = transferSyntax;
    }

    private void loadConfiguration(File cfgFile) throws IOException {
        Properties tmp = new Properties(cfg);
        InputStream in = new BufferedInputStream(new FileInputStream(cfgFile));
        try {
            tmp.load(in);
        } finally {
            in.close();
        }
        cfg = tmp;
    }
    
    public void convert(File pdfFile, File dcmFile) throws IOException { 
        DicomObject attrs = new BasicDicomObject();
        attrs.putString(Tag.SpecificCharacterSet, VR.CS, charset);
        attrs.putSequence(Tag.ConceptNameCodeSequence);
        for (Enumeration en = cfg.propertyNames(); en.hasMoreElements();) {
            String key = (String) en.nextElement();
            attrs.putString(Tag.toTagPath(key), null, cfg.getProperty(key));           
        }
        ensureUID(attrs, Tag.StudyInstanceUID);
        ensureUID(attrs, Tag.SeriesInstanceUID);
        ensureUID(attrs, Tag.SOPInstanceUID);
        Date now = new Date();
        attrs.putDate(Tag.InstanceCreationDate, VR.DA, now);
        attrs.putDate(Tag.InstanceCreationTime, VR.TM, now);
        attrs.initFileMetaInformation(transferSyntax);
        FileInputStream pdfInput = new FileInputStream(pdfFile);
        FileOutputStream fos = new FileOutputStream(dcmFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DicomOutputStream dos = new DicomOutputStream(bos);
        try {
            dos.writeFileMetaInformation(attrs);
            dos.writeDataset(attrs.subSet(Tag.SpecificCharacterSet, 
                    Tag.EncapsulatedDocument), transferSyntax);
            int pdfLen = (int) pdfFile.length();
            dos.writeHeader(Tag.EncapsulatedDocument, VR.OB, (pdfLen+1)&~1);
            byte[] b = new byte[bufferSize];
            int r;
            while ((r = pdfInput.read(b)) > 0) {
                dos.write(b, 0, r);
            }
            if ((pdfLen&1) != 0) {
                dos.write(0);
            }
            dos.writeDataset(attrs.subSet(Tag.EncapsulatedDocument, -1), 
                    transferSyntax);
        } finally {
            dos.close();
            pdfInput.close();
        }
    }    

    private void ensureUID(DicomObject attrs, int tag) {
        if (!attrs.containsValue(tag)) {
            attrs.putString(tag, VR.UI, UIDUtils.createUID());
        }        
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parse(args);
            Pdf2Dcm pdf2Dcm = new Pdf2Dcm();
            if (cl.hasOption("ivrle")) {
                pdf2Dcm.setTransferSyntax(UID.ImplicitVRLittleEndian);
            }
            if (cl.hasOption("cs")) {
                pdf2Dcm.setCharset(cl.getOptionValue("cs"));
            }
            if (cl.hasOption("bs")) {
                pdf2Dcm.setBufferSize(Integer.parseInt(cl.getOptionValue("bs")));
            }
            if (cl.hasOption("c")) {
                pdf2Dcm.loadConfiguration(new File(cl.getOptionValue("c")));
            }
            if (cl.hasOption("uid")) {
                UIDUtils.setRoot(cl.getOptionValue("uid"));
            }
            List argList = cl.getArgList();
            File pdfFile = new File((String) argList.get(0));
            File dcmFile = new File((String) argList.get(1));
            long start = System.currentTimeMillis();
            pdf2Dcm.convert(pdfFile, dcmFile);
            long fin = System.currentTimeMillis();
            System.out.println("Encapsulated " + pdfFile + " to " + dcmFile 
                    + " in " + (fin - start) +  "ms.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        
        OptionBuilder.withArgName("charset");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Specific Character Set, ISO_IR 100 by default");
        opts.addOption(OptionBuilder.create("cs"));

        OptionBuilder.withArgName("size");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Buffer size used for copying PDF to DICOM file, 8192 by default");
        opts.addOption(OptionBuilder.create("bs"));
        
        OptionBuilder.withArgName("file");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Configuration file specifying DICOM attribute values");
        opts.addOption(OptionBuilder.create("c"));
        
        opts.addOption("ivrle", false, "use Implicit VR Little Endian instead " +
                "Explicit VR Little Endian Transfer Syntax for DICOM encoding.");
        opts.addOption("h", "help", false, "print this message");
        
        OptionBuilder.withArgName("prefix");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Generate UIDs with given prefix," +
                "1.2.40.0.13.1.<host-ip> by default.");
        opts.addOption(OptionBuilder.create("uid"));
        
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("pdf2dcm: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = Pdf2Dcm.class.getPackage();
            System.out.println("pdf2dcm v" + p.getImplementationVersion());
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
        System.err.println("Try 'pdf2dcm -h' for more information.");
        System.exit(1);
    }
    
}
