
package com.ingenotech.hudson;


import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Lava Lamp {@link Notifier}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link LavaLampNotifier} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)} 
 * method will be invoked. 
 *
 * @author Ed Randall
 */
public class LavaLampNotifier extends Notifier
                           implements Serializable
{
	private static final long serialVersionUID = 3019486479572911590L;

	private static final Logger LOG = Logger.getLogger(LavaLampNotifier.class.getName());

	/** The LavaLamp name when "default" is selected */
	private static final String DEFAULTNAME = "(Default)";

	/** The name of the LavaLamp in use */
	private final String	name;
	
    /** Update lamp only when the build state changes */
    private final Boolean changesOnly;
    
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public LavaLampNotifier(String name, Boolean changesOnly) {
    	this.name = name;
    	if (changesOnly == null)
    		changesOnly = false;
    	this.changesOnly = changesOnly;
    }
    

    public String getName() {
    	return this.name;
    }
    
    public boolean getChangesOnly() {
    	return this.changesOnly;
    }


    /**
     * Gets the LavaLampInstallation to use.
     */
    public LavaLampInstallation getLavaLamp() {
    	DescriptorImpl descr = getDescriptor();
    	LavaLampInstallation[] all = descr.getInstallations();
        for ( LavaLampInstallation i : all ) {
            if (name != null && name.equals(i.getName())) {
                return i;
            }
        }
        
        if (DEFAULTNAME.equals(name) && all.length > 0)
        	return all[0]; // use the default installation

        return null;
    }
    
    
    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }
    
    
    /**
     * This class does explicit check pointing.
     */
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }


    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException
    {
    	LavaLampInstallation ll = getLavaLamp();
    	
    	Result result = build.getResult();
    	Result prevResult = null;
    	AbstractBuild<?,?> prevBuild = build.getPreviousBuild();
    	if (prevBuild != null)
    		prevResult = prevBuild.getResult();
    	
    	if (result == prevResult && changesOnly) {
    		LOG.fine("perform() - changesOnly set and result unchanged");
    		return true;
    	}
    	
    	Connection c = null;
    	try {
    		c = ll.newConnection();
    		c.open();
    		c.sendResult(build.getFullDisplayName(), result);
    		
    	} catch (Exception ex) {
    		LOG.log(Level.SEVERE, "Failed to connect to LavaLamp server", ex);
    		
    	} finally {
    		if (c != null) {
    			try {
    				c.close();
    			} catch (IOException ix) {
    			}
    		}
    	}
    	
        return true;
    }

    
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
    
    
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

    	/** The name of the LavaLamp in use */
    	private LavaLampNotifier lamp;
        
        
        public DescriptorImpl() {
        	super(LavaLampNotifier.class);
            load();
        }

        
        @Override
		public String toString() {
			return "DescriptorImpl@"+Integer.toHexString(System.identityHashCode(this))
			                        +"[name="+lamp.getName()+",changes="+lamp.getChangesOnly()+"]";
		}


        public String getDisplayName() {
            return "Lava Lamp Notifications";
        }
        
        
        public String getName() {
        	if (this.lamp == null)
        		return null;
        	return this.lamp.getName();
        }
        
        public boolean getChangesOnly() {
        	if (this.lamp == null)
        		return false;
        	return this.lamp.getChangesOnly();
        }

        
        /**
         * indicates that this build step can be used with all types of projects
         */ 
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        
        public LavaLampInstallation[] getInstallations() {
            LavaLampInstallation[] ins = Hudson.getInstance().getDescriptorByType(LavaLampInstallation.DescriptorImpl.class).getInstallations();
            return ins;
        }
        
        
        @Override
        public LavaLampNotifier newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return (LavaLampNotifier)super.newInstance(req, formData);
        }
        

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
        	this.lamp = newInstance(req, formData);
            save();
            return super.configure(req,formData);
        }
        
        
        public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
        	boolean found = false;
        	for (LavaLampInstallation i : getInstallations()) {
        		if (value != null && value.equals(i.getName())) {
        			found = true;
        		}
        	}
        	if (!found) {
                return FormValidation.error("LavaLamp name:"+value+" not found in global config");
        	}
            return FormValidation.ok();
        }
        

    }

}

