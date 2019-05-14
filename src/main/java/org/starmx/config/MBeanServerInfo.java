package org.starmx.config;

import java.util.HashMap;
import java.util.Map;

public class MBeanServerInfo extends ConfigurationItem implements PropertySet {
	
	public static final String LOCALFIND_AGENT_ID = "starmx.mbeanserver.localFind.agentId";
	public static final String LOCALFIND_SERVER_INDEX = "starmx.mbeanserver.localFind.index";

	public static final String JNDI_NAME = "starmx.mbeanserver.jndi.jndiname";

	public static final String JMX_REMOTE_PROTOCOL = "starmx.mbeanserver.jmxRemoting.protocol";
	public static final String JMX_REMOTE_HOST = "starmx.mbeanserver.jmxRemoting.host";
	public static final String JMX_REMOTE_PORT = "starmx.mbeanserver.jmxRemoting.port";
	public static final String JMX_REMOTE_URLPATH = "starmx.mbeanserver.jmxRemoting.urlPath";
	public static final String JMX_REMOTE_SERVICE_URL = "starmx.mbeanserver.jmxRemoting.serviceUrl";

	public static final String FACTORY_METHOD_CLASS_NAME = "starmx.mbeanserver.factoryMethod.className";
	public static final String FACTORY_METHOD_METHOD_NAME = "starmx.mbeanserver.factoryMethod.methodName";

	public enum LookupType { Platform, LocalFind, JNDI, JMXRemoting, FactoryMethod };
	
	private String id;
	private LookupType lookupType;
	private Map<String, String> properties = new HashMap<String, String>();

	public MBeanServerInfo(String id, LookupType lookupType) {
		this.id = id;
		this.lookupType = lookupType;
	}
	
	public String getId(){
		return id;
	}
	
	public String getProperty(String name){
		return properties.get(name);
	}
	
	public void setProperty(String name, String value){
		properties.put(name, value);
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public LookupType getLookupType() {
		return lookupType;
	}

}
