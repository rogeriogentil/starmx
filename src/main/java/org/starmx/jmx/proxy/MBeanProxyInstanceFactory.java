package org.starmx.jmx.proxy;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.starmx.config.MBeanInfo;
import org.starmx.config.PropertySet;
import org.starmx.jmx.mbeanserver.MBeanServerConnectionException;
import org.starmx.jmx.mbeanserver.MBeanServerService;

public class MBeanProxyInstanceFactory {

	private Map<ObjectName, Object> proxyInstanceMap;
	private MBeanRegistrationListener mbeanRegListener;
	private MBeanServerService mbeanServerService;
	private MBeanProxyClassFactory proxyClassFactory;

	private static Logger logger = Logger
			.getLogger(MBeanProxyInstanceFactory.class);

	public MBeanProxyInstanceFactory(MBeanServerService mbeanServerService) {
		proxyInstanceMap = new ConcurrentHashMap<ObjectName, Object>();
		mbeanRegListener = new MBeanRegistrationListener();
		proxyClassFactory = new MBeanProxyClassFactory();
		this.mbeanServerService = mbeanServerService;

		try {
			mbeanServerService.addMBeanRegistrationListener(mbeanRegListener,
				null, null);
		} catch (MBeanServerConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	public Object getMBeanProxy(MBeanInfo mbean) throws JMException, IOException {

		MBeanServerConnection mbeanServer = mbeanServerService
				.getMBeanServer(mbean.getMbeanServerId());

		Object proxy = null;
		if (mbean.isPattern())
			proxy = getProxyList(mbean.getObjectName(), mbean
					.getMbeanInterface(), mbeanServer, mbean);
		else
			proxy = getSingleProxy(mbean.getObjectName(), mbean
					.getMbeanInterface(), mbeanServer, mbean);

		if (mbean.getMbeanInterface() == null){
			mbean.setMbeanInterface(proxy.getClass().getInterfaces()[0]);
		}
		
		return proxy;
	}

	/**
	 * Creates a proxy for the given object-name. If this method is called
	 * concurrently for the same object-name, two proxies will be created and
	 * the second one will overwrite the first one in the cache. This case is
	 * very rare and does not create any problem.
	 * 
	 */
	private <T> T getSingleProxy(ObjectName objectName,
			Class<T> mbeanInterface, MBeanServerConnection mbeanServer,
			PropertySet properties) throws JMException, IOException {

		Object mbeanProxy = null;

		if (proxyInstanceMap.containsKey(objectName))
			return (T) proxyInstanceMap.get(objectName);

		// this line will throw InstanceNotFoundException if the mbean is not registered
		javax.management.MBeanInfo info = mbeanServer.getMBeanInfo(objectName);

		if (mbeanInterface == null) {
			mbeanInterface = (Class<T>) proxyClassFactory
					.getMBeanInterfaceClass(info);
		}

		// creating the first layer proxy
		if (isPlatformMXBean(mbeanInterface)) {
			mbeanProxy = ManagementFactory.newPlatformMXBeanProxy(mbeanServer,
				objectName.getCanonicalName(), mbeanInterface);
		} else if (isMXBean(mbeanInterface)) {
			// normal MXBeans are part of java 1.6
//			mbeanProxy = JMX.newMXBeanProxy(mbeanServer, objectName, mbeanInterface);
			logger.error("Non-platform MXBeans are not supported in this version");
			throw new RuntimeException(
					"Non-Platform MXBeans are not supported in this version");
		} else {
			// normal MBean
			mbeanProxy = MBeanServerInvocationHandler.newProxyInstance(
				mbeanServer, objectName, mbeanInterface, false);
		}

		// creating StarMX proxy
		if (isStarMXProxyNeeded(properties)) {
			Class<?> starmxProxyClass = proxyClassFactory
					.getStarMXProxyClass(mbeanInterface);
			try {
				Constructor<?> constructor = starmxProxyClass
						.getConstructor(mbeanInterface);
				mbeanProxy = constructor.newInstance(mbeanProxy);
			} catch (Exception e) {
				logger.error("Can not instantiate the MBean proxy for "
						+ objectName.getCanonicalName(), e);
				if (e instanceof RuntimeException)
					throw (RuntimeException) e;

				throw new RuntimeException(e);
			}
		}

		proxyInstanceMap.put(objectName, mbeanProxy);
		logger.debug("Created proxy for MBean: object-name=" + objectName
				+ ", with interface=" + mbeanInterface.getName());

		return mbeanInterface.cast(mbeanProxy);
	}

	/**
	 * Generates a list of proxies for the mbeans that match with the given
	 * object-name pattern
	 * 
	 */
	private <T> List<T> getProxyList(ObjectName objectName,
			Class<T> mbeanInterface, MBeanServerConnection mbeanServer,
			PropertySet properties) throws JMException, IOException {
		
		List<T> proxyList = new ArrayList<T>();

		Set<ObjectName> nameSet = mbeanServer.queryNames(objectName, null);
		for (ObjectName objName : nameSet) {
			try {
				proxyList.add(getSingleProxy(objName, mbeanInterface,
					mbeanServer, properties));
			} catch (InstanceNotFoundException e) {
				// ignore it
			}
		}

		return proxyList;
	}

	private boolean isStarMXProxyNeeded(PropertySet properties) {
		// TODO check mbean properties if it is needed to do some processing
		// in the proxy
		return false;
	}

	private void mbeanUnregistered(ObjectName objectName) {
		proxyInstanceMap.remove(objectName);
	}

	private boolean isMXBean(Class<?> mbeanInterface) {
		if (mbeanInterface == null)
			return false;

		// Java 1.6
//		return JMX.isMXBeanInterface(mbeanInterface);
		return mbeanInterface.getName().endsWith("MXBean");
	}

	private boolean isPlatformMXBean(Class<?> mbeanInterface) {
		if (isMXBean(mbeanInterface))
			return mbeanInterface.getPackage().getName().equals(
				"java.lang.management");
		return false;
	}

	class MBeanRegistrationListener implements NotificationListener {

		public void handleNotification(Notification notification,
				Object handback) {
			if (notification.getType().equals(
				MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
				ObjectName objectName = ((MBeanServerNotification) notification)
						.getMBeanName();

				mbeanUnregistered(objectName);
			}
		}
	}

}
