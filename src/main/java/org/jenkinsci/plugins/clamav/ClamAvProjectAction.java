package org.jenkinsci.plugins.clamav;

import hudson.model.AbstractBuild;
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
