package org.jenkinsci.plugins.clamav;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.jvnet.hudson.test.HudsonTestCase;

public class ClamAvRecorderTest extends HudsonTestCase {

    public void testDoCheckHost() throws Exception {
        MockServer mockServer = new MockServer(9999);
        mockServer.start();
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);

        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValueAttribute("localhost");
        f.getInputByName("port").setValueAttribute("9999");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(!p.asText().contains("No response from "));
    }

    public void testDoCheckHost_NoHost() throws Exception {
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);

        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValueAttribute("");
        f.getInputByName("port").setValueAttribute("9999");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(!p.asText().contains("No response from "));
    }

    public void testDoCheckHost_InvalidPort() throws Exception {
        MockServer mockServer = new MockServer(9999);
        mockServer.start();
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);

        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValueAttribute("localhost");
        f.getInputByName("port").setValueAttribute("9998");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("No response from "));
    }

    public void testDoCheckHost_NegativePort() throws Exception {
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);

        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValueAttribute("localhost");
        f.getInputByName("port").setValueAttribute("-1");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("Port should be in the range from 0 to 65535"));
    }

    public void testDoCheckHost_OutOfRangPort() throws Exception {
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);

        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValueAttribute("localhost");
        f.getInputByName("port").setValueAttribute("65536");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("Port should be in the range from 0 to 65535"));
    }

    public void testDoCheckHost_NotIntegerPort() throws Exception {
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);

        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValueAttribute("localhost");
        f.getInputByName("port").setValueAttribute("port");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("For input string: \"port\""));
    }

    public void testDoCheckTimeout_Timeout() throws Exception {
        MockServer mockServer = new MockServer(9999);
        mockServer.start();

        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);

        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValueAttribute("localhost");
        f.getInputByName("port").setValueAttribute("9999");
        f.getInputByName("timeout").setValueAttribute("5000");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(!p.asText().contains("Number may not be negative"));
    }

    public void testDoCheckTimeout_NegativeTimeout() throws Exception {
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);

        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValueAttribute("localhost");
        f.getInputByName("port").setValueAttribute("9998");
        f.getInputByName("timeout").setValueAttribute("-1");
        synchronized (p) {
            p.wait(500);
        }
        assertTrue(p.asText().contains("Number may not be negative"));
    }

    public void testDoCheckTimeout_NotIntegerTimeout() throws Exception {
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);

        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByName("host").setValueAttribute("localhost");
        f.getInputByName("port").setValueAttribute("9998");
        f.getInputByName("timeout").setValueAttribute("timeout");
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
