package com.ingenotech.hudson;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;


/**
 * Represents a single LavaLamp installation on the system.
 */
public class LavaLampInstallation extends Notifier
                                  implements Serializable
{
	private static final long serialVersionUID = -1502300463467252323L;

	private static final Logger LOG = Logger.getLogger(LavaLampInstallation.class.getName());

	private static final String	DEFAULT_ADDRESS = "localhost";
	private static final int	DEFAULT_PORT 	= 1999;
	
	private static final String PROTOCOL_TCP = "TCP";
	private static final String PROTOCOL_UDP = "UDP";
	
	private final String	name;
	private final String	address;
    private final int		port;
    private final String    protocol;

    
    @DataBoundConstructor
    public LavaLampInstallation(String name, String address, int port, String protocol) {
    	this.name = name;
    	this.address = address;
    	this.port = port;
    	this.protocol = protocol;
    }
    
    
    public String getName() {
    	return this.name;
    }

    
    public String getDescription() {
    	StringBuilder sb = new StringBuilder(this.name);
   		sb.append(" (");
   		sb.append(getAddress());
   		sb.append(":");
   		sb.append(getProtocol());
   		sb.append(":");
   		sb.append(getPort());
   		sb.append(")");
    	return sb.toString();
    }
    
    
    public String getAddress() {
    	return address;
    }
    
    private static InetAddress parseAddress(String value) throws UnknownHostException {
    	InetAddress ia = InetAddress.getByName(value);
    	return ia;
    }
    
    public InetAddress getInetAddress() {
    	InetAddress ia = null;
    	try {
    		ia = parseAddress(this.address);
    	} catch (Exception ex) {
    	}
    	return ia;
    }
    
    
    public int getPort() {
    	return port;
    }

    private static int parsePort(String value) throws Exception {
    	int p = Integer.parseInt(value);
    	if (p < 1 || p > 65535) {
    		throw new IllegalArgumentException("Port must be in the range 1..65535");
    	}
    	return p;
    }
    
    
    public String getProtocol() {
    	return parseProtocol(this.protocol);
    }
    
    private static String parseProtocol(String value) {
    	String pc = PROTOCOL_UDP;
    	if (value != null && value.trim().equalsIgnoreCase(PROTOCOL_TCP))
    		pc = PROTOCOL_TCP;
    	return pc;
    }
    
	public Connection newConnection() {
		return newConnection(getInetAddress(), getPort(), getProtocol());
    }

	private static Connection newConnection(InetAddress addr, int port, String protocol) {
    	LOG.fine("newConnection("+protocol+")");
    	if (PROTOCOL_TCP.equalsIgnoreCase(protocol))
    		return new TCPConnection(addr, port);
    	else
    		return new UDPConnection(addr, port);
    }
    
    
    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }
    
    
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    
    public DescriptorImpl getDescriptor() {
    	DescriptorImpl descr = (DescriptorImpl)super.getDescriptor();
        return descr;
    }


    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

    	private LavaLampInstallation[] installations = new LavaLampInstallation[0];
    	
    	
        public DescriptorImpl() {
        	super(LavaLampInstallation.class);
            load();
        }

        
        @Override
		public String toString() {
			return "DescriptorImpl@"+Integer.toHexString(System.identityHashCode(this))
			                        +"["+Arrays.asList(installations)+"]";
		}

        
        public String getDefaultAddress() {
        	return DEFAULT_ADDRESS;
        }
        
        public int getDefaultPort() {
        	return DEFAULT_PORT;
        }

        
        public LavaLampInstallation[] getInstallations() {
            return installations.clone();
        }


        public void setInstallations(LavaLampInstallation... installations) {
            this.installations = installations.clone();
        }

        
        @Override
        public String getDisplayName() {
            return "Lava Lamp Installations";
        }
       
        
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // indicates that this should not show up in per-project configuration
            return false;
        }


        @Override
        public LavaLampInstallation newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return (LavaLampInstallation)super.newInstance(req, formData);
        }
       
        
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            setInstallations(req.bindJSONToList(clazz, formData.get("tool")).toArray((LavaLampInstallation[]) Array.newInstance(clazz, 0)));
            save();
            return super.configure(req,formData);
        }
        

        /**
         * Performs on-the-fly validation of the form field 'address'.
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is returned to the browser.
         */
        public FormValidation doCheckAddress(@QueryParameter String value) throws IOException, ServletException {
        	try {
        		parseAddress(value);
        	} catch (Exception ex) {
                return FormValidation.error("Unable to resolve server name");
        	}
            return FormValidation.ok();
        }

        public FormValidation doCheckPort(@QueryParameter String value) throws IOException, ServletException {
        	try {
        		parsePort(value);
        	} catch (Exception ex) {
                return FormValidation.error("LavaLamp server port must be an integer 1..65535");
        	}
            return FormValidation.ok();
        }
        
        public FormValidation doCheckProtocol(@QueryParameter String value) throws IOException, ServletException {
        	try {
        		parseProtocol(value);
        	} catch (Exception ex) {
                return FormValidation.error("LavaLamp server protocol must be [TCP,UDP]");
        	}
            return FormValidation.ok();
        }
        
        
        public FormValidation doTestServerConnection(
        			@QueryParameter String address,
        			@QueryParameter String port,
        			@QueryParameter String protocol)
        {
        	Connection c = null;
        	try {
        		InetAddress s = parseAddress(address);
        		int p = parsePort(port);
        		String u = parseProtocol(protocol);
        		c = newConnection(s, p, u);
        		c.open();
        		boolean ok = c.ping(); 
        		String ident = c.getServerIdent();
        		if (ok) {
        			return FormValidation.ok("Connection to LavaLamp server:"+ident+" tested successfully.");
        		} else {
        			return FormValidation.error("Connected to LavaLamp server:"+ident+" but ping failed.");
        		}
        	} catch (Exception ex) {
                return FormValidation.error("Failed to connect to LavaLamp server at: "+address+":"+port);
        	} finally {
        		if (c != null) {
        			try {
        				c.close();
        			} catch (IOException ix) {
        			}
        		}
        	}
        }

    }

}
