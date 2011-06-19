package org.jenkinsci.plugins.clamav.scanner;

/**
 * ScanResult class.
 * 
 * @author Seiji Sogabe
 */
public class ScanResult {

    public enum Status {
        PASSED, FAILED, ERROR
    }

    private Status status;

    private String message;
    
    public ScanResult(Status status) {
        this(status, null);
    }

    public ScanResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }
}
