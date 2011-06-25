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

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;

/**
 * Project level Action
 * 
 * @author Seiji Sogabe
 */
public class ClamAvProjectAction extends Actionable implements ProminentProjectAction {

    private final AbstractProject<?, ?> project;

    public AbstractProject<?, ?> getProject() {
        return project;
    }
    
    public ClamAvBuildAction getLastSuccessfulBuildAction() {
        return project.getLastSuccessfulBuild().getAction(ClamAvBuildAction.class);
    }

    public ClamAvProjectAction(AbstractProject<?, ?> project) {
        this.project = project;
    }
        
    public String getDisplayName() {
        return "ClamAV Virus Report";
    }

    public String getSearchUrl() {
        return getDisplayName();
    }

    public String getIconFileName() {
        return "/plugin/clamav/img/clamav_48x48.png";
    }

    public String getUrlName() {
        return "clamav";
    }
}
