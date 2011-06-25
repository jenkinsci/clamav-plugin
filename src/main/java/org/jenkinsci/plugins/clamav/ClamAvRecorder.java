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

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.model.Result;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
        this.includes = Util.fixEmptyAndTrim(includes);
        this.excludes = Util.fixEmptyAndTrim(excludes);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        PrintStream logger = listener.getLogger();

        FilePath ws = build.getWorkspace();
        if (ws == null) {
            return false;
        }
        DescriptorImpl d = (DescriptorImpl) getDescriptor();
        if (d.getHost() == null) {
            return false;
        }

        EnvVars envVars = build.getEnvironment(listener); 
        
        // get artifacts from global and project configuration.
        FilePath[] artifacts1 = new FilePath[0];
        ArtifactArchiver archiver = build.getProject().getPublishersList().get(ArtifactArchiver.class);
        if (archiver != null) {
            artifacts1 = getArtifacts(ws, envVars, archiver.getArtifacts(), archiver.getExcludes());
        }
        FilePath[] artifacts2 = getArtifacts(ws, envVars, includes, excludes);
        FilePath[] artifacts = mergeArtifacts(artifacts1, artifacts2);

        // scan artifacts
        List<ClamAvResult> results = new ArrayList<ClamAvResult>();
        ClamAvScanner scanner = new ClamAvScanner(d.getHost(), d.getPort(), d.getTimeout());
        long start = System.currentTimeMillis();
        for (FilePath file : artifacts) {
            ScanResult r = scanner.scan(file.read());
            results.add(new ClamAvResult(file.getName(), r.getStatus(), r.getMessage()));
            if (!(r.getStatus().equals(ScanResult.Status.PASSED))) {
                build.setResult(Result.UNSTABLE);
            }
            logger.println(buildMessage(file, r));
        }
        logger.println("[ClamAV] " + (System.currentTimeMillis() - start) + "ms took.");

        build.getActions().add(new ClamAvBuildAction(build, results));

        return true;
    }

    private FilePath[] getArtifacts(FilePath workspace, EnvVars vars, String includes, String excludes)
            throws IOException, InterruptedException {
        if (includes == null) {
            return new FilePath[0];
        }
        includes = vars.expand(includes);
        excludes = vars.expand(excludes);
        return workspace.list(includes, excludes);
    }

    private FilePath[] mergeArtifacts(FilePath[] src, FilePath[] dst) {
        Map<String, FilePath> map = new TreeMap<String, FilePath>();
        for (FilePath f : src) {
            map.put(f.getRemote(), f);
        }
        for (FilePath f : dst) {
            map.put(f.getRemote(), f);
        }
        List<FilePath> l = new ArrayList<FilePath>();
        for (String key : map.keySet()) {
            l.add(map.get(key));
        }
        return l.toArray(new FilePath[0]);
    }

    private String buildMessage(FilePath target, ScanResult r) {
        StringBuilder msg = new StringBuilder("[ClamAV] Scanned ");
        msg.append(target.getRemote());
        msg.append(" ");
        msg.append(r.toString());
        return msg.toString();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return Collections.singletonList(new ClamAvProjectAction(project));
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String host;

        private int port = 3310;

        private int timeout = 5000;

        private boolean scanArchivedArtifacts;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public int getTimeout() {
            return timeout;
        }

        public boolean isScanArchivedArtifacts() {
            return scanArchivedArtifacts;
        }

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.ClamAvRecorder_DisplayName();
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
            scanArchivedArtifacts = json.optBoolean("scanArchivedArtifacts");
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
        public FormValidation doCheckHost(@QueryParameter String host, @QueryParameter String port) {
            host = Util.fixEmptyAndTrim(host);
            port = Util.fixEmptyAndTrim(port);
            if (host == null || port == null) {
                return FormValidation.ok();
            }

            int p;
            try {
                p = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                return FormValidation.error(e.getMessage());
            }
            if (p < 0 || p > 65535) {
                return FormValidation.error(Messages.ClamAvRecorder_doCheckHost_OutOfRangePort());
            }
            ClamAvScanner scanner = new ClamAvScanner(host, p);
            if (!scanner.ping()) {
                return FormValidation.error(Messages.ClamAvRecorder_doCheckHost_NoResponse(host, p));
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

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ClamAvRecorder.class.getName());
}
