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

package org.dcm4che2.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.xml.sax.SAXException;

public class SAXWriterTest extends TestCase {

    private static InputStream locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream(name);
        assert is!=null;
        return new BufferedInputStream(is);
    }

    private static DicomObject load(String fname) throws IOException
    {
        DicomInputStream dis = new DicomInputStream(locateFile(fname));
        return dis.readDicomObject();
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SAXWriterTest.class);
    }

    public SAXWriterTest(String arg0) {
        super(arg0);
    }

    public final void testWrite() 
            throws IOException, TransformerConfigurationException, 
            TransformerFactoryConfigurationError, SAXException {
        DicomObject attrs = load("sr/511/sr_511_ct.dcm");
        File ofile = new File("target/test-out/sr_511_ct-1.xml");
        ofile.getParentFile().mkdirs();
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult(new StreamResult(ofile));
        new SAXWriter(th, th).write(attrs);
    }

    public final void testReadValue() throws IOException,
            TransformerConfigurationException,
            TransformerFactoryConfigurationError {
        File ofile = new File("target/test-out/sr_511_ct-2.xml");
        ofile.getParentFile().mkdirs();
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult(new StreamResult(ofile));
        SAXWriter w = new SAXWriter(th, th);
        DicomInputStream dis = new DicomInputStream(locateFile("sr/511/sr_511_ct.dcm"));
        dis.setHandler(w);
        DicomObject attrs = new BasicDicomObject();
        dis.readDicomObject(attrs, -1);
        dis.close();
    }


    public final void testReadValue2() throws IOException,
            TransformerConfigurationException,
            TransformerFactoryConfigurationError {
        File ofile = new File("target/test-out/view400.xml");
        ofile.getParentFile().mkdirs();
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult(new StreamResult(ofile));
        SAXWriter w = new SAXWriter(th, th);
        DicomInputStream dis = new DicomInputStream(locateFile("misc/view400.dcm"));
        dis.setHandler(w);
        DicomObject attrs = new BasicDicomObject();
        dis.readDicomObject(attrs, -1);
        dis.close();
    }

    public final void testReadValue3() throws IOException,
            TransformerConfigurationException,
            TransformerFactoryConfigurationError {
        File ofile = new File("target/test-out/DICOMDIR.xml");
        ofile.getParentFile().mkdirs();
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult(new StreamResult(ofile));
        SAXWriter w = new SAXWriter(th, th);
        DicomInputStream dis = new DicomInputStream(locateFile("DICOMDIR"));
        dis.setHandler(w);
        DicomObject attrs = new BasicDicomObject();
        dis.readDicomObject(attrs, -1);
        dis.close();
    }

    public final void testXSLT() throws IOException, TransformerConfigurationException, 
            TransformerFactoryConfigurationError, SAXException {
        DicomObject attrs = load("sr/511/sr_511_ct.dcm");
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler(
                new StreamSource(locateFile("pat_info.xsl"), null));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        th.setResult(new StreamResult(os));
        new SAXWriter(th, null).write(attrs);
        assertEquals("XSL Transformation:","PID:CT5|Name:CTFIVE^JIM", os.toString());
    }
}
