package org.dcm4che2.hp;

import java.util.Arrays;
import java.util.List;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import junit.framework.TestCase;

public class HangingProtocolTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HangingProtocolTest.class);
    }

    public HangingProtocolTest(String arg0) {
        super(arg0);
    }

    public void testGetHPSelectorSpi() {
        assertNotNull(HangingProtocol.getHPSelectorSpi("IMAGE_PLANE"));
    }

    public void testGetHPComparatorSpi() {
        assertNotNull(HangingProtocol.getHPComparatorSpi("ALONG_AXIS"));
        assertNotNull(HangingProtocol.getHPComparatorSpi("BY_ACQ_TIME"));
    }

    public void testGetSupportedHPSelectorCategories() {
        String[] ss = HangingProtocol.getSupportedHPSelectorCategories();
        List list = Arrays.asList(ss);
        assertEquals(true, list.contains("IMAGE_PLANE"));
    }

    public void testGetSupportedHPComparatorCategories() {
        String[] ss = HangingProtocol.getSupportedHPComparatorCategories();
        List list = Arrays.asList(ss);
        assertEquals(true, list.contains("ALONG_AXIS"));
        assertEquals(true, list.contains("BY_ACQ_TIME"));
    }
    
    public void testAddImageSet() {
        HangingProtocol hp = new HangingProtocol();
        HPImageSet is1 = hp.addNewImageSet(null);
        HPImageSet is2 = hp.addNewImageSet(is1);
        assertEquals(is1.getImageSetSelectors(), is2.getImageSetSelectors());        
        assertEquals(is1.getDicomObject().getParent(), is2.getDicomObject().getParent());        
        List imageSets = hp.getImageSets();
        assertEquals(2, imageSets.size());
        DicomObject dcmobj = hp.getDicomObject();
        DicomElement isseq = dcmobj.get(Tag.ImageSetsSequence);
        assertNotNull(isseq);
        assertEquals(1, isseq.countItems());
        DicomObject is = isseq.getDicomObject();
        DicomElement tbissq = is.get(Tag.TimeBasedImageSetsSequence);
        assertNotNull(tbissq);
        assertEquals(2, tbissq.countItems());        
    }
    
    public void testRemoveImageSet() {
        HangingProtocol hp = new HangingProtocol();
        HPImageSet is1 = hp.addNewImageSet(null);
        HPImageSet is2 = hp.addNewImageSet(is1);
        assertEquals(true, hp.removeImageSet(is2));
        DicomObject dcmobj = hp.getDicomObject();
        DicomElement isseq = dcmobj.get(Tag.ImageSetsSequence);
        assertNotNull(isseq);
        assertEquals(1, isseq.countItems());
        DicomObject is = isseq.getDicomObject();
        DicomElement tbissq = is.get(Tag.TimeBasedImageSetsSequence);
        assertNotNull(tbissq);
        assertEquals(1, tbissq.countItems());               
        assertEquals(false, hp.removeImageSet(is2));
        assertEquals(true, hp.removeImageSet(is1));
        assertEquals(0, isseq.countItems());
   }

    public void testCopy() {
        HangingProtocol src = new HangingProtocol();
        new HangingProtocol(src);
    }
}
