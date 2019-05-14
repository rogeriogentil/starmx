package org.starmx.naming;

import org.starmx.config.ObjectMapping;

public class ObjectLocatorFactory {

	private ObjectLocator mbeanLocator;
	private ObjectLocator beanLocator;

	public ObjectLocatorFactory() {
		mbeanLocator = new MBeanLocator();
		beanLocator = new BeanLocator();
	}

	public ObjectLocator getObjectLocator(ObjectMapping.Type type) {
		if (type == ObjectMapping.Type.MBean)
			return mbeanLocator;
		if (type == ObjectMapping.Type.Bean)
			return beanLocator;

		return null;
	}
}
