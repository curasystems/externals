package org.dcm4che2.image;

import static org.junit.Assert.*;

import org.junit.Test;

public class LookupTableTest {
    @Test
    public void testCreateRampLutSigned16C2048W4096Output8() {
        // from DICOM PS3.3 - 2009, chapter C.11.2.1.2, note 3
        LookupTable lut = LookupTable.createLut(16, true, 8, 1.0f, 0.0f, 2048.0f, 4096.0f,
                "LINEAR", false, null, null, null);
        assertEquals(0, lut.lookup(0));
        assertEquals(0, lut.lookup(-1));
        assertEquals(33, lut.lookup(527));
        assertEquals(128, lut.lookup(2048));
        assertEquals(255, lut.lookup(4095));
        assertEquals(255, lut.lookup(4096));
    }

    @Test
    public void testCreateRampLutSigned16C2048W1Output8() {
        // from DICOM PS3.3 - 2009, chapter C.11.2.1.2, note 3
        LookupTable lut = LookupTable.createLut(16, true, 8, 1.0f, 0.0f, 2048.0f, 1.0f, "LINEAR",
                false, null, null, null);
        assertEquals(0, lut.lookup(0));
        assertEquals(0, lut.lookup(-1));
        assertEquals(0, lut.lookup(527));
        assertEquals(0, lut.lookup(2047));
        assertEquals(255, lut.lookup(2048));
        assertEquals(255, lut.lookup(4095));
        assertEquals(255, lut.lookup(4096));
    }

    @Test
    public void testCreateRampLutSigned16C0W100Output8() {
        // from DICOM PS3.3 - 2009, chapter C.11.2.1.2, note 3
        LookupTable lut = LookupTable.createLut(16, true, 8, 1.0f, 0.0f, 0.0f, 100.0f, "LINEAR",
                false, null, null, null);
        assertEquals(0, lut.lookup(-51));
        assertEquals(0, lut.lookup(-50));
        assertEquals(3, lut.lookup(-49));
        assertEquals(167, lut.lookup(15));
        assertEquals(255, lut.lookup(50));
        assertEquals(255, lut.lookup(51));
    }

    @Test
    public void testCreateRampLutSigned16C0W1Output8() {
        // from DICOM PS3.3 - 2009, chapter C.11.2.1.2, note 3
        LookupTable lut = LookupTable.createLut(16, true, 8, 1.0f, 0.0f, 0.0f, 1.0f, "LINEAR",
                false, null, null, null);
        assertEquals(0, lut.lookup(-1));
        assertEquals(255, lut.lookup(0));
        assertEquals(255, lut.lookup(1));
    }

    @Test
    public void testCreateRampLutIdentityUnsigned8C128W256Output8() {
        // from DICOM PS3.3 - 2009, chapter C.11.2.1.2, note 4
        LookupTable lut = LookupTable.createLut(8, false, 8, 1.0f, 0.0f, 128.0f, 256.0f, "LINEAR",
                false, null, null, null);
        assertEquals(0, lut.lookup(0));
        assertEquals(33, lut.lookup(33));
        assertEquals(128, lut.lookup(128));
        assertEquals(255, lut.lookup(255));
    }

    @Test
    public void testCreateRampLutSigned16C2048W2Output8() {
        // from DICOM PS3.3 - 2009, chapter C.11.2.1.2, note 5
        LookupTable lut = LookupTable.createLut(16, true, 8, 1.0f, 0.0f, 2048.0f, 2.0f, "LINEAR",
                false, null, null, null);
        assertEquals(0, lut.lookup(0));
        assertEquals(0, lut.lookup(-1));
        assertEquals(0, lut.lookup(527));
        assertEquals(0, lut.lookup(2047));
        assertEquals(255, lut.lookup(2048));
        assertEquals(255, lut.lookup(4095));
        assertEquals(255, lut.lookup(4096));
    }

    @Test
    public void testDCM417CreateRampLutUnsigned12C0_5W1Output8() {
        // special case for an image with only black (0) pixels - the autoWindowing code will set
        // the center and width to 0.5 and 1.0 respectively. The black pixels should render as
        // black.
        LookupTable lut = LookupTable.createLut(12, false, 8, 1.0f, 0.0f, 0.5f, 1.0f, "LINEAR",
                false, null, null, null);
        assertEquals(0, lut.lookup(0));
        assertEquals(255, lut.lookup(1));
    }
    
    @Test
    public void testCreateRampLutSigned16C2048W4096WithPixelPaddingValueOutput8() {
        // from DICOM PS3.3 - 2009, chapter C.11.2.1.2, note 3
        LookupTable lut = LookupTable.createLut(16, true, 8, 1.0f, 0.0f, 2048.0f, 4096.0f,
                "LINEAR", false, null, 4090, null);
        assertEquals(0, lut.lookup(0));
        assertEquals(0, lut.lookup(-1));
        assertEquals(22, lut.lookup(350));
        assertEquals(33, lut.lookup(527));
        assertEquals(128, lut.lookup(2048));
        assertEquals(0, lut.lookup(4090));
        assertEquals(255, lut.lookup(4095));
    }
}
