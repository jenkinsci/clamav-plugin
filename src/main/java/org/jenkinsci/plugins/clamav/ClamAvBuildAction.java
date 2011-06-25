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
