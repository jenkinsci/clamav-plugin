package org.jenkinsci.plugins.clamav.scanner;

/**
 * ScanResult class.
 * 
 * @author Seiji Sogabe
 */
public class ScanResult {

    public enum Status {
        PASSED, FAILED
    }

    private Status status;

    private String siganture;

    public ScanResult(Status status) {
        this(status, null);
    }

    public ScanResult(Status status, String siganture) {
        this.status = status;
        this.siganture = siganture;
    }

    public String getSiganture() {
        return siganture;
    }

    public Status getStatus() {
        return status;
    }
}
