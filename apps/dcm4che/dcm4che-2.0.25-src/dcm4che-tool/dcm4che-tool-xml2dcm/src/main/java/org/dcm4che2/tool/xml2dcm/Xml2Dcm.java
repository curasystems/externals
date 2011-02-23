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

package org.dcm4che2.tool.xml2dcm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.io.ContentHandlerAdapter;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.util.CloseUtils;
import org.xml.sax.SAXException;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 11969 $ $Date: 2009-07-23 14:28:34 +0200 (Thu, 23 Jul 2009) $
 * @since Aug 20, 2005
 *
 */
public class Xml2Dcm {

    private static final String USAGE = "xml2dcm [-geEuUVh] [-a|-d] [-t <tsuid>] " +
            "[-i <dcmfile>] [-x [<xmlfile>] -d <basedir>] -o <dcmfile>";
    private static final String DESCRIPTION = "Modify existing or create " +
            "new DICOM file according given XML presentation and store result " +
            "as ACR/NEMA-2 dump (option: -a) or DICOM Part 10 file " +
            "(option: -d). If neither option -a nor -d is specified, " +
            "inclusion of Part 10 File Meta Information depends, if the input " +
            "DICOM file or the XML presentation already includes File Meta " +
            "Information attributes (0002,eeee). Either option -i <dcmfile> or" +
            "-x [<xmlfile>] (or both) must be specified.\n" +
            "Options:";
    private static final String EXAMPLE = "\nExample: xml2dcm -x in.xml -o out.dcm\n" +
            " => Convert XML presentation in.xml to DICOM file out.dcm\n" +
            "xml2dcm -d -t 1.2.840.10008.1.2.1.99 -i in.dcm -o out.dcm\n" +
            " => Load DICOM object from file in.dcm and store it as " +
            "DICOM (Part 10) file encoded with Deflated Explicit VR Little " +
            "Endian Transfer Syntax.";

    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        Option ifile = new Option("i", true,
                "Update attributes in specified DICOM file instead " +
                "generating new one.");
        ifile.setArgName("dcmfile");
        opts.addOption(ifile);
        Option xmlfile = new Option("x", true,
                "XML input, used to update or generate new DICOM file." +
                "Without <xmlfile>, read from standard input.");
        xmlfile.setOptionalArg(true);
        xmlfile.setArgName("xmlfile");
        opts.addOption(xmlfile);
        Option basedir = new Option("d", true,
                "Directory to resolve external attribute values referenced by " +
                "XML read from standard input.");
        basedir.setArgName("basedir");
        opts.addOption(basedir);
        Option ofile = new Option("o", true, 
                "Generated DICOM file or ACR/NEMA-2 dump");
        ofile.setArgName("dcmfile");
        opts.addOption(ofile);
        Option tsuid = new Option("t", true,
                "Store result with specified Transfer Syntax.");
        tsuid.setArgName("tsuid");
        opts.addOption(tsuid);
        opts.addOption("a", "acrnema2", false,
                "Store result as ACR/NEMA 2 dump. Mutual exclusive " +
                "with option -d");
        opts.addOption("d", "dicom", false,
                "Store result as DICOM Part 10 File. Mutual exclusive " +
                "with option -a");
        opts.addOption("g", "grlen", false, 
                "Include (gggg,0000) Group Length attributes." +
                "By default, optional Group Length attributes are excluded.");
        opts.addOption("E", "explseqlen", false, 
                "Encode sequences with explicit length. At default, non-empty " +
                "sequences are encoded with undefined length.");
        opts.addOption("e", "explitemlen", false, 
                "Encode sequence items with explicit length. At default, " +
                "non-empty sequence items are encoded with undefined length.");
        opts.addOption("U", "undefseqlen", false, 
                "Encode all sequences with undefined length. Mutual exclusive " +
                "with option -E.");
        opts.addOption("u", "undefitemlen", false, 
                "Encode all sequence items with undefined length. Mutual " +
                "exclusive with option -e.");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new PosixParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcm2xml: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = Xml2Dcm.class.getPackage();
            System.out.println("dcm2xml v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || !cl.hasOption("o")
                || (!cl.hasOption("x") && !cl.hasOption("i"))) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        if (cl.hasOption("a") && cl.hasOption("d"))
            exit("xml2dcm: Option -a and -d are mutual exclusive");
        if (cl.hasOption("e") && cl.hasOption("u"))
            exit("xml2dcm: Option -e and -u are mutual exclusive");
        if (cl.hasOption("E") && cl.hasOption("U"))
            exit("xml2dcm: Option -E and -U are mutual exclusive");
        return cl;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'xml2dcm -h' for more information.");
        System.exit(1);
    }


    public static void main(String[] args) {
        CommandLine cl = parse(args);
        DicomObject dcmobj = new BasicDicomObject();
        if (cl.hasOption("i")) {
            File ifile = new File(cl.getOptionValue("i"));
            try {
                loadDicomObject(ifile, dcmobj);
            } catch (IOException e) {
                System.err.println("xml2dcm: failed to load DICOM file: " 
                        + ifile+ ": " + e.getMessage());
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        if (cl.hasOption("x")) {
            String xmlFile = cl.getOptionValue("x");
            try {
                parseXML(xmlFile, dcmobj, cl.getOptionValue("d"));
            } catch (FactoryConfigurationError e) {
                System.err.println("xml2dcm: Configuration Error: " 
                        + e.getMessage());
                System.exit(1);
            } catch (ParserConfigurationException e) {
                System.err.println("xml2dcm: Configuration Error: " 
                        + e.getMessage());
                System.exit(1);
            } catch (SAXException e) {
                System.err.println("xml2dcm: failed to parse XML from " +
                        (xmlFile != null ? xmlFile : " standard input") 
                        + ": " + e.getMessage());
                e.printStackTrace(System.err);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("xml2dcm: failed to parse XML from " +
                        (xmlFile != null ? xmlFile : " standard input") 
                        + ": " + e.getMessage());
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        File ofile = new File(cl.getOptionValue("o"));
        DicomOutputStream dos = null;
        try {
            dos = new DicomOutputStream(new BufferedOutputStream(
                    new FileOutputStream(ofile)));
            if (!dcmobj.command().isEmpty()) {
                dos.writeCommand(dcmobj);
                System.out.println("Created DICOM Command Set " + ofile);
            } else {
                dos.setIncludeGroupLength(cl.hasOption("g"));
                dos.setExplicitItemLength(cl.hasOption("e"));
                dos.setExplicitItemLengthIfZero(!cl.hasOption("u"));
                dos.setExplicitSequenceLength(cl.hasOption("E"));
                dos.setExplicitSequenceLengthIfZero(!cl.hasOption("U"));
                String tsuid = cl.getOptionValue("t");
                if (cl.hasOption("d")) {
                    if (tsuid == null)
                        tsuid = TransferSyntax.ExplicitVRLittleEndian.uid();
                    dcmobj.initFileMetaInformation(tsuid);
                }
                if (cl.hasOption("a") || dcmobj.fileMetaInfo().isEmpty()) {
                    if (tsuid == null)
                        tsuid = TransferSyntax.ImplicitVRLittleEndian.uid();
                    dos.writeDataset(dcmobj, TransferSyntax.valueOf(tsuid));
                    System.out.println("Created ACR/NEMA Dump " + ofile);
                } else {
                    dos.writeDicomFile(dcmobj);
                    System.out.println("Created DICOM File " + ofile);
                }
            }
        } catch (IOException e) {
            System.err.println("xml2dcm: failed to create " + ofile + ": "
                    + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        } finally {
            CloseUtils.safeClose(dos);
        }
     }

    private static void parseXML(String xmlFile, DicomObject dcmobj, String baseDir)
            throws FactoryConfigurationError, ParserConfigurationException, 
                    SAXException, IOException {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(dcmobj);
        if (xmlFile != null) {
            p.parse(new File(xmlFile), ch);
        } else if (baseDir != null ){
            String uri = "file:" + new File(baseDir, "STDIN").getAbsolutePath();
            if (File.separatorChar == '\\') {
                uri = uri.replace('\\', '/');
            }
            p.parse(System.in, ch, uri);
        } else {
            p.parse(System.in, ch);
        }
    }

    private static void loadDicomObject(File ifile, DicomObject dcmobj) throws IOException {
        DicomInputStream in = new DicomInputStream(ifile);
        try {
            in.readDicomObject(dcmobj, -1);
        } finally {
            in.close();
        }
    }

 }
