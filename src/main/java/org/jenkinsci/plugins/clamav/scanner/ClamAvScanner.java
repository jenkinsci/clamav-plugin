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

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;
import static org.jenkinsci.plugins.clamav.scanner.ScanResult.Status;

/**
 * ClamAv Scanner
 * 
 * @see http://www.clamav.net/doc/latest/html/node28.html
 * @author Seiji Sogabe
 */
public class ClamAvScanner {

    private static final int BUFFER_SIZE = 2048;

    private static final int CHUNK_SIZE = 2048;

    private static final int DEFAULT_TIMEOUT = 5000;

    private static final int DEFAULT_PORT = 3310;

    private static final byte[] PING = "zPING\0".getBytes();

    private static final byte[] INSTREAM = "zINSTREAM\0".getBytes();

    private String host;

    private int port;

    private int timeout;

    public ClamAvScanner(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public ClamAvScanner(String host, int port) {
        this(host, port, DEFAULT_TIMEOUT);
    }

    public ClamAvScanner(String host) {
        this(host, DEFAULT_PORT);
    }

    public boolean ping() {
        String response;
        try {
            response = sendReceive(PING);
        } catch (IOException e) {
            return false;
        }
        return "PONG\0".equals(response);
    }

    public ScanResult scan(InputStream file) {
        if (file == null) {
            throw new IllegalArgumentException("file is null.");
        }
        String response;
        try {
            response = instream(file);
        } catch (IOException e) {
            return new ScanResult(Status.WARNING, e.getMessage());
        }
        if (response.contains("FOUND\0")) {
            String sig = response.substring("stream: ".length(), response.lastIndexOf("FOUND") - 1);
            return new ScanResult(Status.INFECTED, sig);
        }
        return new ScanResult(Status.PASSED);
    }

    public ScanResult scan(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file is null.");
        }
        return scan(new FileInputStream(file));
    }

    private String sendReceive(byte[] cmd) throws IOException {

        Socket socket = new Socket();
        OutputStream os = null;
        InputStream is = null;
        StringBuilder res = new StringBuilder();

        try {
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.setSoTimeout(timeout);

            os = new DataOutputStream(socket.getOutputStream());
            os.write(cmd);
            os.flush();

            is = socket.getInputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int read = is.read(buffer);
                if (read == -1) {
                    break;
                }
                res.append(new String(buffer, 0, read));
            }
            LOGGER.fine("Response from server: " + res);

        } finally {
            closeSocketQuietly(socket);
            closeQuietly(os);
            closeQuietly(is);
        }

        return res.toString();
    }

    private String instream(InputStream file) throws IOException {
        Socket socket = new Socket();
        DataOutputStream dos = null;
        InputStream is = null;
        StringBuilder res = new StringBuilder();

        try {
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.setSoTimeout(timeout);

            dos = new DataOutputStream(socket.getOutputStream());
            dos.write(INSTREAM);
            dos.flush();

            int read = CHUNK_SIZE;
            byte[] buf = new byte[CHUNK_SIZE];
            while (read == CHUNK_SIZE) {
                read = file.read(buf);
                dos.writeInt(read);
                dos.write(buf, 0, read);
            }

            dos.writeInt(0);
            dos.flush();

            is = socket.getInputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                read = is.read(buffer);
                if (read == -1) {
                    break;
                }
                res.append(new String(buffer, 0, read));
            }
            LOGGER.fine("Response from server: " + res);

        } finally {
            closeSocketQuietly(socket);
            closeQuietly(dos);
            closeQuietly(is);
        }

        return res.toString();
    }

    private void closeSocketQuietly(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    private void closeQuietly(Closeable target) {
        if (target == null) {
            return;
        }
        try {
            target.close();
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ClamAvScanner.class.getName());
}
