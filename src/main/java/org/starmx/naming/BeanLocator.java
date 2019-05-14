package org.starmx.naming;

import org.starmx.StarMXContext;
import org.starmx.config.BeanInfo;
import org.starmx.config.Configuration;
import org.starmx.util.BeanUtil;

public class BeanLocator implements ObjectLocator {

	private Configuration config;

	BeanLocator() {
		this.config = StarMXContext.getDefault().getConfiguration();
	}

	public Object lookup(String name) throws LookupException {
		BeanInfo bean = config.getBeanInfo(name);
		if (bean == null)
			throw new LookupException("JavaBean <" + name + "> not found.");
		//TODO cache already created bean instances, if it is not factory
		try {
			return BeanUtil.createInstance(bean.getClazz(), bean
					.getFactoryMethod(), name);
		} catch (Exception e) {
			throw new LookupException(e);
		}
	}
}
