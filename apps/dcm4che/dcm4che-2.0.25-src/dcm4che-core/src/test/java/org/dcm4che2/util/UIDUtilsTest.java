package org.dcm4che2.util;

import junit.framework.TestCase;

public class UIDUtilsTest extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UIDUtilsTest.class);
    }

    public void testCreateUID()
    {
        UIDUtils.verifyUID(UIDUtils.createUID());
    }

    /*
     * Test method for 'org.dcm4che2.util.UIDUtils.verifyUID(String)'
     */
    public void testVerifyUID()
    {
        UIDUtils.verifyUID("1.0.3");
        try {
            UIDUtils.verifyUID("1.00.3");
            fail("Should raise an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }           
        try {
            UIDUtils.verifyUID(".0.3");
            fail("Should raise an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }           
       try {
            UIDUtils.verifyUID("1.0.3.");
            fail("Should raise an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }           
    }

}
