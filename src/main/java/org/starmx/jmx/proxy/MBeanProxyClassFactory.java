package org.starmx.jmx.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanInfo;

class MBeanProxyClassFactory {

	private Map<String, Class<?>> interfaceMap;
	private Map<String, Class<?>> proxyMap;

	MBeanProxyClassFactory(){
		interfaceMap = new HashMap<String, Class<?>>();
		proxyMap = new ConcurrentHashMap<String, Class<?>>();
	}
	
	synchronized Class<?> getMBeanInterfaceClass(MBeanInfo mbeanInfo) {
		Class<?> infClass = interfaceMap.get(mbeanInfo.getClassName());
		if (infClass == null) {
			infClass = MBeanInterfaceBuilder.createMBeanInterface(mbeanInfo);
			interfaceMap.put(mbeanInfo.getClassName(), infClass);
		}
		return infClass;
	}

	<T> Class<T> getStarMXProxyClass(Class<T> mbeanInterface) {
		Class<?> proxy;
		synchronized (mbeanInterface) {
			proxy = proxyMap.get(mbeanInterface.getName());
			if (proxy == null) {
				proxy = StarMXProxyClassBuilder
						.createProxyClass(mbeanInterface);
				proxyMap.put(mbeanInterface.getName(), proxy);
			}
		}
		return (Class<T>) proxy;
	}

}
