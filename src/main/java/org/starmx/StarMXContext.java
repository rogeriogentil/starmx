package org.starmx;

import org.starmx.config.Configuration;
import org.starmx.jmx.mbeanserver.MBeanServerService;
import org.starmx.jmx.proxy.MBeanProxyInstanceFactory;
import org.starmx.naming.ObjectLocatorFactory;

public class StarMXContext {

	private Configuration configuration;
	private MBeanServerService mBeanServerService;
	private ObjectLocatorFactory objectLocatorFactory;
	private MBeanProxyInstanceFactory mBeanProxyInstanceFactory;
	private Scope starmxScope;

	private static StarMXContext theInstance;
	
	private StarMXContext(){
	}

	public static StarMXContext getDefault(){
		return theInstance; 
	}
	
	static StarMXContext createInstance(){
		theInstance = new StarMXContext();
		return theInstance;
	}
	
	void destroyInstance(){
		theInstance = null;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}

	public MBeanServerService getMBeanServerService() {
		return mBeanServerService;
	}

	public ObjectLocatorFactory getObjectLocatorFactory() {
		return objectLocatorFactory;
	}

	public MBeanProxyInstanceFactory getMBeanProxyInstanceFactory() {
		return mBeanProxyInstanceFactory;
	}

	public Scope getStarmxScope() {
		return starmxScope;
	}

	void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	void setMBeanServerService(MBeanServerService mbeanServerService) {
		this.mBeanServerService = mbeanServerService;
	}

	void setObjectLocatorFactory(ObjectLocatorFactory objectLocatorFactory) {
		this.objectLocatorFactory = objectLocatorFactory;
	}

	void setMBeanProxyInstanceFactory(MBeanProxyInstanceFactory mbeanProxyInstanceFactory) {
		this.mBeanProxyInstanceFactory = mbeanProxyInstanceFactory;
	}

	void setStarmxScope(Scope starmxScope) {
		this.starmxScope = starmxScope;
	}
}
