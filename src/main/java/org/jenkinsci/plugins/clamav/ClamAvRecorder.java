package org.jenkinsci.plugins.clamav;

import hudson.Extension;
import hudson.FilePath;
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
import java.io.PrintStream;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.clamav.scanner.ClamAvScanner;
import org.jenkinsci.plugins.clamav.scanner.ScanResult;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * ClamAvRecorder
 * 
 * @author Seiji Sogabe
 */
public class ClamAvRecorder extends Recorder {

    private final String includes;
    
    private final String excludes;

    public String getIncludes() {
        return includes;
    }

    public String getExcludes() {
        return excludes;
    }
    
    @DataBoundConstructor
    public ClamAvRecorder(String includes, String excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        PrintStream logger = listener.getLogger();

        FilePath ws = build.getWorkspace();
        if (ws == null) {
            return true;
        }

        DescriptorImpl d = (DescriptorImpl) getDescriptor();
        ClamAvScanner scanner = new ClamAvScanner(d.getHost(), d.getPort(), d.getTimeout());

        long start = System.currentTimeMillis();
        FilePath[] targets = ws.list(includes, excludes);
        for (FilePath target : targets) {
            ScanResult r = scanner.scan(target.read());
            logger.println(buildMessage(target, r));
        }
        logger.println("[ClamAv] " + (System.currentTimeMillis() - start) + "ms took.");

        return true;
    }

    private String buildMessage(FilePath target, ScanResult r) {
        StringBuilder msg = new StringBuilder("[ClamAv] Scanned ");
        msg.append(target.getRemote());
        msg.append(" ");
        msg.append(r.toString());
        return msg.toString();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String host;

        private int port = 3310;

        private int timeout = 5000;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public int getTimeout() {
            return timeout;
        }

        public DescriptorImpl() {
            load();
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
            timeout = json.optInt("timeout", 5000);
            save();
            return super.configure(req, json);
        }

        /**
         * Check ClamAV host.
         * 
         * exposed to global.jelly.
         * 
         * @param host host name or IP address of ClamAV Host.
         * @param port port of ClamAv host. 
         * @return {@link FormValidation} 
         */
        public FormValidation doCheckHost(@QueryParameter String host, @QueryParameter int port) {
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

        /**
         * Check timeout
         * 
         * exposed to global.jelly
         * 
         * @param value timeout
         * @return {@link FormValidation}
         */
        public FormValidation doCheckTimeout(@QueryParameter String value) {
            return FormValidation.validateNonNegativeInteger(value);
        }

        /**
         * Check includes and host.
         * 
         * exposed to config.jelly.
         * 
         * @param includes
         * @return {@link FormValidation} 
         */
        public FormValidation doCheckIncludes(StaplerRequest req, @QueryParameter String includes) {
            if (host == null) {
                return FormValidation.errorWithMarkup(Messages.ClamAvRecorder_NotHostConfigured(req.getContextPath()));
            }
            return FormValidation.validateRequired(includes);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ClamAvRecorder.class.getName());
}
