package org.jenkinsci.plugins.clamav.scanner;

import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClamAvScannerTest {
    
    public ClamAvScannerTest() {
    }

    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of ping method, of class ClamAvScanner.
     */
    @Test
    public void testPing() throws Exception {
        ClamAvScanner target = new ClamAvScanner("localhost");
        boolean result = target.ping();
        assertTrue(result);
    }

    /**
     * Test of ping method, of class ClamAvScanner.
     */
    @Test
    public void testPing_Port() throws Exception {
        ClamAvScanner target = new ClamAvScanner("127.0.0.1", 3310);
        boolean result = target.ping();
        result = target.ping();
        assertTrue(result);
    }

    /**
     * Test of scan method, of class ClamAvScanner.
     */
    @Test
    public void testScan_Virus() throws Exception {
        ClamAvScanner target = new ClamAvScanner("localhost");
        InputStream is = getClass().getResourceAsStream("eicar.com.txt");        
        ScanResult result = target.scan(is);
        assertNotNull(result);
        assertEquals(ScanResult.Status.FAILED, result.getStatus());
        assertEquals("Eicar-Test-Signature", result.getMessage());
    }

    /**
     * Test of scan method, of class ClamAvScanner.
     */
    @Test
    public void testScan() throws Exception {
        ClamAvScanner target = new ClamAvScanner("localhost");
        InputStream is = getClass().getResourceAsStream("ClamAvScannerTest.class");        
        ScanResult result = target.scan(is);
        assertNotNull(result);
        assertEquals(ScanResult.Status.PASSED, result.getStatus());
    }    
}
