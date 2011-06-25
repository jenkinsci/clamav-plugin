package org.jenkinsci.plugins.clamav;

import org.jenkinsci.plugins.clamav.scanner.ScanResult.Status;

/**
 * Scanning results
 * 
 * @author Seiji Sogabe
 */
public class ClamAvResult {

    private String file;

    private Status status;

    private String description;

    public ClamAvResult(String file, Status status, String description) {
        this.file = file;
        this.status = status;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getFile() {
        return file;
    }

    public Status getStatus() {
        return status;
    }
}
