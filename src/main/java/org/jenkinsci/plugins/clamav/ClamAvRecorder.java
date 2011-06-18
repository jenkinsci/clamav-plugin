package org.jenkinsci.plugins.clamav;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.clamav.scanner.ClamAvScanner;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * ClamAvRecorder
 * 
 * @author Seiji Sogabe
 */
public class ClamAvRecorder extends Recorder {

    private final String artifacts;

    public String getArtifacts() {
        return artifacts;
    }

    @DataBoundConstructor
    public ClamAvRecorder(String artifacts) {
        this.artifacts = artifacts;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) 
            throws InterruptedException, IOException {
        return super.perform(build, launcher, listener);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String host;

        private int port = 3310;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String getDisplayName() {
            return "Check for viruses";
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(ClamAvRecorder.class, formData);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            host = Util.fixEmptyAndTrim(json.getString("host"));
            port = json.optInt("port", 3310);
            save();
            return true;
        }

        public FormValidation doPing(@QueryParameter String host,@QueryParameter int port) {
            host = Util.fixEmptyAndTrim(host);
            if (host == null) {
                return FormValidation.ok();
            }
            if (port < 0 || port > 65535) {
                return FormValidation.error("Port should be in the range from 0 to 65535");
            }
            ClamAvScanner scanner = new ClamAvScanner(host, port);
            if (!scanner.ping()) {
                return FormValidation.error("No response from " + host + ":" + port);
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ClamAvRecorder.class.getName());
}
