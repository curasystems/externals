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

package org.dcm4che2.tool.dcm2xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.SAXWriter;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 14178 $ $Date: 2010-10-19 17:28:01 +0200 (Tue, 19 Oct 2010) $
 * @since Aug 15, 2005
 */
public class Dcm2Xml {

    private static final String USAGE = 
        "dcm2xml [-VXcCh] [-o <xmlfile>] [-x <tag>]... [-d <basedir>] " +
        "[-T <xslurl> [-I] [-P <param=value>]] <dcmfile>";
    private static final String DESCRIPTION = 
        "Convert DICOM file in XML presentation and optionally apply " +
        "XSL stylesheet on it. Values of attributes specified by -x <tag> " +
        "are excluded from the generated XML. With -o <xmlfile>, the " +
        "excluded values are stored into files named according the hex " +
        "value <ggggeeee> of the attribute tag, into the same directory as " +
        "the XML output. Files with extracted values of nested attributes " +
        "are stored into sub-directories named according the sequence tag " +
        "and the item number <ggggeeee>/<item#>/. Without -o <xmlfile>, but " +
        "given -d <basedir>, excluded values are stored into files under " +
        "specified <basedir>. If neither -o <xmlfile> nor -d <basedir> is " +
        "specified, excluded values from the XML output are not stored.\n" +
        "Options:";
    private static final String EXAMPLE = 
        "\nExample: dcm2xml -Xi image.dcm -o image.xml\n=> Store XML " +
        "presentation of image.dcm to image.xml, excluding  pixel data from " +
        "XML presentation, but extracting it to file 7FE00010.";

    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        Option basedir = new Option("d", true,
                "store extracted values in files under <basedir>. Cannot be " +
                "specified together with option -o <xmlfile>.");
        basedir.setArgName("basedir");
        opts.addOption(basedir);
        Option xmlfile = new Option("o", true,
                "file to write XML to, standard output by default");
        xmlfile.setArgName("xmlfile");
        opts.addOption(xmlfile);
        Option exclude = new Option("x", true,
                "tag (e.g.: 7FE00010) or name (e.g.: PixelData) of attribute " +
                "to exclude from XML output");
        exclude.setArgName("tag");
        opts.addOption(exclude);
        opts.addOption("X", false, "exclude pixel data from XML output " +
                "(= shortcut for -x 7FE00010).");
        Option xslurl = new Option("T", true,
                "transform XML output by applying specified XSL stylesheet.");
        xslurl.setArgName("xslurl");
        opts.addOption(xslurl);
        Option xsltparams = new Option("P", true,
                "pass specified parameters to the XSL stylesheet.");
        xsltparams.setArgName("param=value,...");
        xsltparams.setValueSeparator('=');
        xsltparams.setArgs(2);
        opts.addOption(xsltparams);
        opts.addOption("I", "incxslt", false, "enable incremental XSLT");
        opts.addOption("c", "compact", false,
                "suppress additional whitespaces in XML output");
        opts.addOption("C", "comments", false,
                "include attribute names as comments in XML output");
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
            Package p = Dcm2Xml.class.getPackage();
            System.out.println("dcm2xml v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().isEmpty()) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        if (cl.hasOption("o") && cl.hasOption("d"))
            exit("dcm2xml: Option -o <xmlfile> and -d <basedir> are mutual" +
                    "exclusive");
        return cl;
    }

   
    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcm2xml -h' for more information.");
        System.exit(1);
    }


    public static void main(String[] args) {
        CommandLine cl = parse(args);
        Dcm2Xml dcm2xml = new Dcm2Xml();
        File ifile = new File((String) cl.getArgList().get(0));
        File ofile = null;
        if (cl.hasOption("o")) {
            ofile = new File(cl.getOptionValue("o"));
            dcm2xml.setBaseDir(ofile.getAbsoluteFile().getParentFile());
        }
        if (cl.hasOption("d")) {
            dcm2xml.setBaseDir(new File(cl.getOptionValue("d")));
        }
        boolean x = cl.hasOption("X");
        if (cl.hasOption("x")) {
            String[] tagStr = cl.getOptionValues("x");
            int[] excludes = new int[x ? tagStr.length + 1 : tagStr.length];
            for (int i = 0; i < tagStr.length; i++) {
                try {
                    excludes[i] = (int) Long.parseLong(tagStr[i], 16);
                } catch (NumberFormatException e) {
                    excludes[i] = Tag.forName(tagStr[i]);
                }
            }
            if (x) {
                excludes[tagStr.length] = Tag.PixelData;
            }
            dcm2xml.setExclude(excludes);
        } else if (x) {
            dcm2xml.setExclude(new int[] {Tag.PixelData});
        }
        if (cl.hasOption("T")) {
            final String xslurl = cl.getOptionValue("T");
            try {
                dcm2xml.setXslt(new URL(xslurl));
            } catch (MalformedURLException e) {
                System.err.println("dcm2xml: invalid xsl URL: " + xslurl);
                System.exit(1);
            }
            dcm2xml.setXsltInc(cl.hasOption("I"));
            dcm2xml.setXsltParams(cl.getOptionValues("P"));
        }
        dcm2xml.setComments(cl.hasOption("C"));
        dcm2xml.setIndent(!cl.hasOption("c"));
        long t1 = System.currentTimeMillis();
        try {
            dcm2xml.convert(ifile, ofile);
        } catch (TransformerConfigurationException e) {
            System.err.println("dcm2xml: Configuration Error: " 
                    + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("dcm2xml: Failed to convert " 
                    + ifile + ": " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
        long t2 = System.currentTimeMillis();
        if (ofile != null)
            System.out.println("Finished conversion of " + ifile + "to "
                        + ofile + " in " + (t2 - t1) + "ms");          
    }

    private URL xslt;
    private String[] xsltParams;
    private File baseDir;
    private int[] exclude;
    private boolean xsltInc = false;
    private boolean indent = true;
    private boolean comments = false;

    public final void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public final void setExclude(int[] exclude) {
        this.exclude = exclude;
    }

    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    public final void setComments(boolean comments) {
        this.comments = comments;
    }
    
    public final void setXslt(URL xslt) {
        this.xslt = xslt;
    }

    public final void setXsltInc(boolean xsltInc) {
        this.xsltInc = xsltInc;
    }

    public final void setXsltParams(String[] xsltParam) {
        xsltParams = (xsltParam != null ? xsltParam.clone() : null);
    }

    public void convert(File ifile, File ofile) throws IOException,
            TransformerConfigurationException {
        DicomInputStream dis = new DicomInputStream(ifile);
        FileOutputStream fos = null;
        try {
            TransformerHandler th = getTransformerHandler();
            th.getTransformer().setOutputProperty(OutputKeys.INDENT, 
                    indent ? "yes" : "no");
            th.setResult(ofile != null 
                    ? new StreamResult(fos = new FileOutputStream(ofile))
                    : new StreamResult(System.out));
            final SAXWriter writer = new SAXWriter(th, comments ? th : null);
            writer.setBaseDir(baseDir);
            writer.setExclude(exclude);
            dis.setHandler(writer);
            dis.readDicomObject(new BasicDicomObject(), -1);
        } finally {
            if (fos != null)
                fos.close();
            dis.close();
        }
    }

    private TransformerHandler getTransformerHandler()
            throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        if (xslt == null) {
            return tf.newTransformerHandler();
        }
        if (xsltInc) {
            tf.setAttribute(
                    "http://xml.apache.org/xalan/features/incremental",
                    Boolean.TRUE);
        }
        TransformerHandler th = tf.newTransformerHandler(new StreamSource(xslt.openStream(),
                xslt.toExternalForm()));
        Transformer t = th.getTransformer();
        if (xsltParams != null) {
            for (int i = 0; i+1 < xsltParams.length; i++,i++) {
                 t.setParameter(xsltParams[i], xsltParams[i+1]);
            }
        }
        return th;
    }

}
