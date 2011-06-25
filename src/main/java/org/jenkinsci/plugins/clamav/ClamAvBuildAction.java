package org.jenkinsci.plugins.clamav;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.clamav.scanner.ScanResult.Status;
import hudson.model.Action;
import java.util.Collections;
import java.util.List;
import static org.jenkinsci.plugins.clamav.scanner.ScanResult.Status.*;

/**
 * ClamAVBuildAction
 * 
 * @author Seiji Sogabe
 */
public class ClamAvBuildAction implements Action {

    private final AbstractBuild<?, ?> build;
    
    private final int total;

    private final int passed;

    private final int infected;

    private final int warning;

    private final List<ClamAvResult> results;

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }
    
    public int getwarning() {
        return warning;
    }

    public int getInfected() {
        return infected;
    }

    public int getPassed() {
        return passed;
    }

    public int getTotal() {
        return total;
    }

    public ClamAvBuildAction(AbstractBuild<?, ?> build, List<ClamAvResult> results) {
        this.results = Collections.unmodifiableList(results);
        this.build = build;
        total = results.size();
        passed = computeCount(PASSED);
        infected = computeCount(INFECTED);
        warning = computeCount(WARNING);
    }

    private int computeCount(Status status) {
        int count = 0;
        for (ClamAvResult r : results) {
            if (r.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    public List<ClamAvResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    public String getDisplayName() {
        return "ClamAV Virus Report";
    }

    public String getIconFileName() {
        return "/plugin/clamav/img/clamav_48x48.png";
    }

    public String getUrlName() {
        return "clamav";
    }
}
