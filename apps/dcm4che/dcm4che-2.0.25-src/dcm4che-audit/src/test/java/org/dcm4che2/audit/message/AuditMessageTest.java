package org.dcm4che2.audit.message;

import junit.framework.TestCase;

public class AuditMessageTest extends TestCase {
    public void testProcessId() {
        String processId = AuditMessage.getProcessID();
        assertNotNull(processId);
        assertTrue(processId.length() > 0);
    }
}
