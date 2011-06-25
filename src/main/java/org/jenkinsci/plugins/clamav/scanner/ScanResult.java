package org.jenkinsci.plugins.clamav.scanner;

/**
 * ScanResult class.
 * 
 * @author Seiji Sogabe
 */
public class ScanResult {

    public enum Status {
        PASSED, INFECTED, WARNING
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
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(status);
        if (!status.equals(Status.PASSED)) {
            buf.append(" : ");
            buf.append(getMessage());
        }
        return buf.toString();
    }
}
