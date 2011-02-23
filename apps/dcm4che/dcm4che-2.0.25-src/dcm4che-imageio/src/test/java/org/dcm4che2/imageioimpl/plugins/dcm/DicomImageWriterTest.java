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
package org.dcm4che2.imageioimpl.plugins.dcm;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import junit.framework.TestCase;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.ConfigurationError;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.image.ColorModelFactory;
import org.dcm4che2.imageio.ImageWriterFactory;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;

/**
 * Tests the DICOM Image Writer
 * 
 * @author bwallace
 * @author jhpoelen
 */
public class DicomImageWriterTest extends TestCase {

    static {
        ImageIO.scanForPlugins();
    }

    static final boolean eraseDicom = false;

    private boolean hasWriterForTransferSyntax(String uid) {
        try {
                ImageWriterFactory.getInstance().getWriterForTransferSyntax(uid);
        } catch (ConfigurationError error) {
                return false;
        }
        return true;
}

    /**
     * Test to see that we can find an image writer, and that it is of the
     * correct class. If multiple DICOM writers are available, then the dcm4che2
     * must currently be the first one.
     * 
     */
    public void testFindDicomImageWriter() {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("DICOM")
                .next();
        assertNotNull(writer);
        assertTrue(writer instanceof DicomImageWriter);
    }
    

    /**
     * Tests single frame lossless writing.
     */
    public void testSingleFrameLossless() throws IOException {
        if (!hasWriterForTransferSyntax(UID.JPEGLSLossless)) {
            return;
        }
        String name = "ct-write";
        DicomImageReader reader = createImageReader("/misc/ct.dcm");
        BufferedImage bi = readRawBufferedImage(reader, 1);
        DicomStreamMetaData newMeta = copyMeta(reader, UID.JPEGLSLossless);
        ImageInputStream iis = writeImage(newMeta, bi, name);
        DicomImageReader readerNew = createImageReader(iis);
        BufferedImage biNew = readRawBufferedImage(readerNew, 1);
        ImageDiff diff = new ImageDiff(bi, biNew, "target/"+name, 0);
        assertEquals("Max Pixel Difference must be 0", 0,diff.getMaxDiff());
    }
    
    public void testColorMultiFrameLEI() throws IOException {
        testColorMultiFrame("multicolorLEI", "/misc/multicolor.dcm", UID.ImplicitVRLittleEndian );
    }

    public void testColorMultiFrameJPEG() throws IOException {
        if (!hasWriterForTransferSyntax(UID.JPEGLossless)) {
            return;
        }
        testColorMultiFrame("multicolorJPEG", "/misc/multicolorjpeg.dcm", UID.JPEGLossless );
    }
    
    private void testColorMultiFrame(String name, String objectName, String transferSyntax ) throws IOException {
        DicomImageReader reader = createImageReader(objectName);

        DicomStreamMetaData copyMeta = copyMeta(reader, transferSyntax);
        int numberOfFrames = copyMeta.getDicomObject().getInt(Tag.NumberOfFrames);
        List<BufferedImage> bufferedImages = new ArrayList<BufferedImage>();
        
        for( int frame = 1; frame <= numberOfFrames; frame++ )
        {
        	BufferedImage biNew = readRawBufferedImage(reader, frame);
        	bufferedImages.add(biNew);
        }

        ImageInputStream inputStream =  writeImageFrames(copyMeta,
        		bufferedImages,  name);        
        DicomImageReader readerNew = createImageReader(inputStream);
        
        int frameNumber = 1;
        for( BufferedImage bufferedImage : bufferedImages  )
        {
        	BufferedImage biNew = readRawBufferedImage(readerNew, frameNumber++);
            ImageDiff diff = new ImageDiff(bufferedImage, biNew, "target/"+name, 0 );
            assertTrue("Color Frame "+frameNumber+" is different",diff.getMaxDiff() == 0);
        }
    }
    
    /** Returns an input stream containing the written data */
    private ImageInputStream writeImage(DicomStreamMetaData newMeta,
            BufferedImage bi, String name) throws IOException {
        File f = new File(name + ".dcm");
        if (f.exists())
            f.delete();
        ImageOutputStream imageOutput = new FileImageOutputStream(f);
        DicomImageWriter writer = (DicomImageWriter) new DicomImageWriterSpi()
                .createWriterInstance();
        IIOImage iioimage = new IIOImage(bi, null, null);
        writer.setOutput(imageOutput);
        writer.write(newMeta, iioimage, null);
        imageOutput.close();
        return new FileImageInputStream(f);
    }

    /** Returns an input stream containing the written data */
    private ImageInputStream writeImageFrames(DicomStreamMetaData newMeta,
            List<BufferedImage> bis, String name) throws IOException {
        File f = new File(name + ".dcm");
        if (f.exists())
            f.delete();
        ImageOutputStream imageOutput = new FileImageOutputStream(f);
        DicomImageWriter writer = (DicomImageWriter) new DicomImageWriterSpi()
                .createWriterInstance();
        
        writer.setOutput(imageOutput);
        writer.prepareWriteSequence(newMeta);
        
        for( BufferedImage bi : bis )
        {
	        IIOImage iioimage = new IIOImage(bi, null, null);
	        writer.setOutput(imageOutput);
	        writer.writeToSequence(iioimage, null);
        }
        
        imageOutput.close();
        return new FileImageInputStream(f);
    }
    
    private DicomStreamMetaData copyMeta(DicomImageReader reader, String tsuid)
            throws IOException {
        DicomStreamMetaData oldMeta = (DicomStreamMetaData) reader
                .getStreamMetadata();
        DicomStreamMetaData ret = new DicomStreamMetaData();
        DicomObject ds = oldMeta.getDicomObject();
        DicomObject newDs = new BasicDicomObject();
        ds.copyTo(newDs);
        newDs.putString(Tag.TransferSyntaxUID, VR.UI, tsuid);
        ret.setDicomObject(newDs);
        return ret;
    }

    public static BufferedImage readRawBufferedImage(DicomImageReader reader,
            int frameNumber) throws IOException {
    	
    	int imageIndexFromZero = frameNumber - 1;
        WritableRaster raster = (WritableRaster) reader.readRaster(imageIndexFromZero, null);
        DicomObject ds = ((DicomStreamMetaData) reader.getStreamMetadata())
                .getDicomObject();
        ColorModel cm = ColorModelFactory.createColorModel(ds);
        return new BufferedImage(cm, raster, false, null);
    }

    /**
     * Return an image reader on the given resource Example resources are:
     * mr.dcm ct.dcm cr-multiframe.dcm cr-monochrome1.dcm mlut_*.dcm
     */
    public static DicomImageReader createImageReader(String resource)
            throws IOException {
        InputStream is = DicomImageWriterTest.class
                .getResourceAsStream(resource);
        assertNotNull(is);
        return createImageReader(ImageIO.createImageInputStream(is));
    }

    /** Returns an image reader on the given filename */
    public static DicomImageReader createImageReader(ImageInputStream is) {
        assertNotNull(is);
        DicomImageReaderSpi spi = new DicomImageReaderSpi();
        DicomImageReader reader = (DicomImageReader) spi
                .createReaderInstance(null);
        reader.setInput(is, true);
        return reader;
    }

}
