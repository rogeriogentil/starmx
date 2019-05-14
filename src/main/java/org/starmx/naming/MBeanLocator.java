package org.starmx.naming;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;

import org.starmx.StarMXContext;
import org.starmx.config.Configuration;
import org.starmx.config.MBeanInfo;
import org.starmx.jmx.proxy.MBeanProxyInstanceFactory;

public class MBeanLocator implements ObjectLocator {

	private Configuration config;
	private MBeanProxyInstanceFactory proxyFactory;

	MBeanLocator() {
		this.config = StarMXContext.getDefault().getConfiguration();
		this.proxyFactory = StarMXContext.getDefault()
				.getMBeanProxyInstanceFactory();
	}

	public Object lookup(String name) throws LookupException {

		MBeanInfo mbean = config.getMBeanInfo(name);
		if (mbean == null)
			throw new LookupException("MBean <" + name + "> not found.");

		try {
			return proxyFactory.getMBeanProxy(mbean);
		} catch (JMException e) {
			if (e instanceof InstanceNotFoundException)
				throw new MBeanNotFoundException(mbean, e);

			throw new LookupException(e);
		} catch (IOException e) {
			throw new LookupException(e);
		}
	}
}
