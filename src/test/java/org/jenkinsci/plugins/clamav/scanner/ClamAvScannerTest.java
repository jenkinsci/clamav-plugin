/*
 * The MIT License
 *
 * Copyright (c) 2011, Seiji Sogabe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
        assertEquals(ScanResult.Status.INFECTED, result.getStatus());
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
