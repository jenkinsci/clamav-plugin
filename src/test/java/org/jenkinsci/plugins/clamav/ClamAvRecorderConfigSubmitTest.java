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
package org.jenkinsci.plugins.clamav;

import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import hudson.model.Hudson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.JenkinsRule;

public class ClamAvRecorderConfigSubmitTest  {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private WebClient webClient;

    @Before
    public void setUp() throws Exception {
        webClient = j.createWebClient();
        webClient.setCssEnabled(false);
        webClient.setJavaScriptEnabled(true);
        webClient.setThrowExceptionOnFailingStatusCode(false);
    }

    @Test
    public void testDefaultPort() throws Exception {
        HtmlForm form = webClient.goTo("configure").getFormByName("config");
        form.getInputByName("host").setValue("localhost");
        form.getInputByName("port").setValue("");
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        
        ClamAvRecorder.DescriptorImpl desc = (ClamAvRecorder.DescriptorImpl) 
                Hudson.getInstance().getDescriptor(ClamAvRecorder.class);
        assertNotNull(desc);
        assertEquals(3310, desc.getPort());
    }

    @Test
    public void testDefaultTimeout() throws Exception {
        HtmlForm form = webClient.goTo("configure").getFormByName("config");
        form.getInputByName("host").setValue("localhost");
        form.getInputByName("port").setValue("3310");
        form.getInputByName("timeout").setValue("");
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        
        ClamAvRecorder.DescriptorImpl desc = (ClamAvRecorder.DescriptorImpl) 
                Hudson.getInstance().getDescriptor(ClamAvRecorder.class);
        assertNotNull(desc);
        assertEquals(10000, desc.getTimeout());
    }

    @Test
    @Ignore
    public void testDoCheckHost() throws Exception {
        MockServer mockServer = new MockServer(9999);
        mockServer.start();
 
        HtmlPage p = webClient.goTo("configure");
        HtmlForm form = p.getFormByName("config");
        form.getInputByName("host").setValue("localhost");
        form.getInputByName("port").setValue("9999");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(!p.asText().contains("No response from "));
        ClamAvRecorder.DescriptorImpl rec = (ClamAvRecorder.DescriptorImpl) 
                Hudson.getInstance().getDescriptor(ClamAvRecorder.class);
        assertNotNull(rec);
    }

    @Test
    public void testDoCheckHost_NoHost() throws Exception {
        HtmlPage p = webClient.goTo("configure");
        HtmlForm form = p.getFormByName("config");
        form.getInputByName("host").setValue("");
        form.getInputByName("port").setValue("9999");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(!p.asText().contains("No response from "));
    }

    @Test
    public void testDoCheckHost_InvalidPort() throws Exception {
        MockServer mockServer = new MockServer(9999);
        mockServer.start();

        HtmlPage p = webClient.goTo("configure");
        HtmlForm form = p.getFormByName("config");
        form.getInputByName("host").setValue("localhost");
        form.getInputByName("port").setValue("9998");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("No response from "));
    }

    @Test
    public void testDoCheckHost_NegativePort() throws Exception {
        HtmlPage p = webClient.goTo("configure");
        HtmlForm form = p.getFormByName("config");
        form.getInputByName("host").setValue("localhost");
        form.getInputByName("port").setValue("-1");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("Port should be in the range from 0 to 65535"));
    }

    @Test
    public void testDoCheckHost_OutOfRangPort() throws Exception {
        HtmlPage p = webClient.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValue("localhost");
        f.getInputByName("port").setValue("65536");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("Port should be in the range from 0 to 65535"));
    }

    @Test
    public void testDoCheckHost_NotIntegerPort() throws Exception {
        HtmlPage p = webClient.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValue("localhost");
        f.getInputByName("port").setValue("port");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("For input string: \"port\""));
    }

    @Test
    public void testDoCheckTimeout_Timeout() throws Exception {
        MockServer mockServer = new MockServer(9999);
        mockServer.start();

        HtmlPage p = webClient.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValue("localhost");
        f.getInputByName("port").setValue("9999");
        f.getInputByName("timeout").setValue("5000");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(!p.asText().contains("Number may not be negative"));
    }

    @Test
    public void testDoCheckTimeout_NegativeTimeout() throws Exception {
        HtmlPage p = webClient.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValue("localhost");
        f.getInputByName("port").setValue("9998");
        f.getInputByName("timeout").setValue("-1");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("Number may not be negative"));
    }

    @Test
    public void testDoCheckTimeout_NotIntegerTimeout() throws Exception {
        HtmlPage p = webClient.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValue("localhost");
        f.getInputByName("port").setValue("9998");
        f.getInputByName("timeout").setValue("timeout");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("Not a number"));
    }

    private static class MockServer extends Thread {

        private int port;

        public MockServer(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            Socket socket = null;
            OutputStream os = null;
            InputStream is = null;
            try {
                serverSocket = new ServerSocket(port);
                socket = serverSocket.accept();

                is = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int read = is.read(buffer);
                os = socket.getOutputStream();
                os.write("PONG\0".getBytes());
            } catch (IOException e) {
                //
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                    }
                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException ex) {
                    }
                }
                try {
                    is.close();
                } catch (IOException ex) {
                }
                try {
                    os.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
