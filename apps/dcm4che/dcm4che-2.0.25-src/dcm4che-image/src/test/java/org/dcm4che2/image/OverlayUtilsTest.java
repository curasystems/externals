package org.dcm4che2.image;

import static org.junit.Assert.*;

import org.junit.Test;

public class OverlayUtilsTest {

    @Test
    public void padToFixRowByteBoundary_WhenInputIs5BytesRows5Cols7_ShouldPadCorrectly() {
        byte[] inputArray = new byte[] { (byte) 0xAB, (byte)0xCD, (byte) 0xEF, (byte) 0xBA, (byte) 0xDC}; 
        byte[] expectedArray = new byte[] { (byte) 0xAB, (byte)0x9B, (byte) 0xBF, (byte) 0xD7, (byte) 0xCB}; 
        byte[] actualArray = OverlayUtils.padToFixRowByteBoundary(inputArray, 5, 7);
        
        checkArrays(expectedArray, actualArray);
    }

    @Test
    public void padToFixRowByteBoundary_WhenInputIs9BytesRows5Cols13_ShouldPadCorrectly() {
        byte[] inputArray = new byte[] { (byte) 0xFF, 0x00, (byte) 0xAA, 0x55, (byte) 0xFF, 0x00, (byte) 0xAA ,0x55, (byte)0xFF}; 
        byte[] expectedArray = new byte[] { (byte) 0xFF, 0x00, 0x50, (byte)0xAD, (byte)0xD5, 0x3F, 0x01, 0x54, 0x5A, (byte)0xF5};
        byte[] actualArray = OverlayUtils.padToFixRowByteBoundary(inputArray, 5, 13);
        
        checkArrays(expectedArray, actualArray);
    }

    @Test
    public void padToFixRowByteBoundary_WhenInputIs10BytesRows5Cols15_ShouldPadCorrectly() {
        byte[] inputArray = new byte[] { (byte) 0xAB, (byte)0xCD, (byte) 0xEF, (byte) 0xBA, (byte) 0xDC, (byte) 0xAB, (byte)0xCD, (byte) 0xEF, (byte) 0xBA, (byte) 0xDC }; 
        byte[] expectedArray = new byte[] { (byte) 0xAB, (byte)0xCD, (byte) 0xDF, (byte) 0x75, (byte) 0x72, (byte) 0xAF, (byte)0x6D, (byte) 0x7E, (byte) 0xAE, (byte) 0xCB }; 
        byte[] actualArray = OverlayUtils.padToFixRowByteBoundary(inputArray, 5, 15);

        checkArrays(expectedArray, actualArray);
    }

    @Test
    public void padToFixRowByteBoundary_WhenInputIs12BytesRows5Cols18_ShouldPadCorrectly() {
        byte[] inputArray = new byte[] { (byte) 0xAB, (byte)0xCD, (byte) 0xEF, (byte) 0xBA, (byte) 0xDC, (byte) 0xAB, (byte)0xCD, (byte) 0xEF, (byte) 0xBA, (byte) 0xDC, (byte) 0xAB, (byte)0xCD }; 
        byte[] expectedArray = new byte[] { (byte) 0xAB, (byte)0xCD, (byte) 0xEF, (byte) 0xBB, (byte) 0x2E, (byte) 0xF7, (byte)0xBD, (byte) 0xDA, (byte) 0xFC, (byte) 0xBF, (byte) 0xEB, (byte)0x72, (byte)0xDC, (byte)0xAB, (byte)0xCD }; 
        byte[] actualArray = OverlayUtils.padToFixRowByteBoundary(inputArray, 5, 18);
        
        checkArrays(expectedArray, actualArray);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void padToFixRowByteBoundary_WhenInputIs8BytesRows5Cols16_ShouldThrowException() {
        byte[] inputArray = new byte[] { (byte) 0xAB, (byte)0xCD, (byte) 0xEF, (byte) 0xBA, (byte) 0xDC, (byte) 0xAB, (byte)0xCD, (byte) 0xEF}; 
        byte[] expectedArray = new byte[] { (byte) 0xAB, (byte)0xCD, (byte) 0xEF, (byte) 0xBA, (byte) 0xDC, (byte) 0xAB, (byte)0xCD, (byte) 0xEF}; 
        byte[] actualArray = OverlayUtils.padToFixRowByteBoundary(inputArray, 5, 16);
        
        checkArrays(expectedArray, actualArray);
    }

    private void checkArrays(byte[] expectedArray, byte[] actualArray) {
        assertEquals("Array lengths do not match", expectedArray.length, actualArray.length);
        
        for (int x = 0; x < actualArray.length; x++) {
            assertEquals("Did not get correct byte at outputArray[" + x + "]", expectedArray[x], actualArray[x]);
        }
    }

}
