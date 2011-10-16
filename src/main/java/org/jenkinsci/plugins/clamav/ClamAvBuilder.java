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

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;


/**
 * Builder for ClamAV.
 * 
 * @author Seiji Sogabe
 */
public class ClamAvBuilder extends Builder {

    private String includes;
    
    private String excludes;

    public String getExcludes() {
        return excludes;
    }

    public String getIncludes() {
        return includes;
    }
    
    
    @DataBoundConstructor
    public ClamAvBuilder(String includes, String excludes) {
        this.includes = Util.fixEmptyAndTrim(includes);
        this.excludes = Util.fixEmptyAndTrim(excludes);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return super.getRequiredMonitorService();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return super.perform(build, launcher, listener);
    }
   
   
    @Extension
    public static class ClamAvBuilderDescriptor extends Descriptor<Builder> {

        public ClamAvBuilderDescriptor() {
        }
        
        @Override
        public String getDisplayName() {
            return "ClamAV Scanner";
        }
        
    }
}
