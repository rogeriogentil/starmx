package org.starmx.jmx.monitor;

import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.monitor.MonitorMBean;

import org.apache.log4j.Logger;
import org.starmx.StarMXContext;
import org.starmx.config.MonitorMBeanInfo;
import org.starmx.jmx.mbeanserver.MBeanServerService;
import org.starmx.jmx.proxy.MBeanProxyInstanceFactory;

public abstract class MonitorBuilder {

	private static Logger logger = Logger.getLogger(MonitorBuilder.class);

	private MonitorMBeanInfo info;
	private MBeanServerConnection connection;
	private MonitorMBean monitorProxy;
	private boolean unknownObservedAttributeType = false;
	private MBeanRegistrationListener mbeanRegListener = new MBeanRegistrationListener();

	public MonitorBuilder(MonitorMBeanInfo info) {
		this.info = info;
		connection = StarMXContext.getDefault().getMBeanServerService()
				.getMBeanServer(info.getMbeanServerId());
	}

	public MonitorMBean createMonitor() {
		try {
			createMonitorMBean(info.getObjectName(), info.getMbeanServerId());
			monitorProxy = createMonitorMBeanProxy(info);

			// install a listener to detect registration of observed objects
			MBeanServerService mbeanServerService = StarMXContext.getDefault()
					.getMBeanServerService();
			mbeanServerService.addMBeanRegistrationListener(info
					.getMbeanServerId(), mbeanRegListener, null, null);

			setGeneralProperties(monitorProxy, info);

			try {
				setSpecificProperties(monitorProxy, info,
					getObservedAttributeType(monitorProxy));
				monitorProxy.start();

			} catch (UnknownObservedAttributeTypeException e) {
				unknownObservedAttributeType = true;
			}

			return monitorProxy;
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;

			throw new RuntimeException(e);
		}
	}

	public void destroyMonitor() {
		try {
			monitorProxy.stop();

			// remove the listener
			MBeanServerService mbeanServerService = StarMXContext.getDefault()
					.getMBeanServerService();
			mbeanServerService.removeMBeanRegistrationListener(info
					.getMbeanServerId(), mbeanRegListener);

			connection.unregisterMBean(info.getObjectName());
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;

			throw new RuntimeException(e);
		}
	}

	private void setGeneralProperties(MonitorMBean mbean, MonitorMBeanInfo info)
			throws Exception {
		mbean.setGranularityPeriod(info.getGranularityPeriod());
		mbean.setObservedAttribute(info.getObservedAttribute());

		for (ObjectName objName : info.getObservedObjects()) {
			if (!objName.isPattern() && connection.isRegistered(objName))
				mbean.addObservedObject(objName);
			else {
				Set<ObjectName> nameSet = connection.queryNames(objName, null);
				for (ObjectName objName2 : nameSet) {
					mbean.addObservedObject(objName2);
				}
			}
		}
	}

	private Class<?> getObservedAttributeType(MonitorMBean mbean)
			throws Exception {
		String observedAttr = mbean.getObservedAttribute();
		String attrType = null;
		for (ObjectName objName : mbean.getObservedObjects()) {
			try {
				MBeanInfo observedObjectInfo = connection.getMBeanInfo(objName);
				for (MBeanAttributeInfo attrInfo : observedObjectInfo
						.getAttributes()) {
					if (attrInfo.getName().equals(observedAttr)) {
						attrType = attrInfo.getType();
						break;
					}
				}
			} catch (InstanceNotFoundException e) {
				// go to the next observed object
			}
		}

		if (attrType != null) {
			if (attrType.equals("java.lang.Long") || attrType.equals("long"))
				return Long.class;
			if (attrType.equals("java.lang.Integer") || attrType.equals("int"))
				return Integer.class;
			if (attrType.equals("java.lang.Short") || attrType.equals("short"))
				return Short.class;
			if (attrType.equals("java.lang.Byte") || attrType.equals("byte"))
				return Byte.class;
			if (attrType.equals("java.lang.Float") || attrType.equals("float"))
				return Float.class;
			if (attrType.equals("java.lang.Double")
					|| attrType.equals("double"))
				return Double.class;
			if (attrType.equals("java.lang.String"))
				return String.class;

			throw new RuntimeException(
					"Invalid observed attribute type for <"
							+ observedAttr
							+ ">. Check JMX specification for the supported attribute types for monitors.");
		}
		return null;
	}

	private MonitorMBean createMonitorMBeanProxy(MonitorMBeanInfo info)
			throws Exception {
		MBeanProxyInstanceFactory proxyFactory = StarMXContext.getDefault()
				.getMBeanProxyInstanceFactory();
		return (MonitorMBean) proxyFactory.getMBeanProxy(info);
	}

	protected abstract void createMonitorMBean(ObjectName objectName,
			String mbeanServerId) throws Exception;

	protected abstract void setSpecificProperties(MonitorMBean mbean,
			MonitorMBeanInfo info, Class<?> observedAttrClass) throws Exception;

	<T extends Number> T convertNumberClass(Number n, Class<T> c) {
		Number retNum = null;

		if (c.isInstance(n))
			retNum = n;
		else if (c.isAssignableFrom(Long.class))
			retNum = new Long(n.longValue());
		else if (c.isAssignableFrom(Integer.class))
			retNum = new Integer(n.intValue());
		else if (c.isAssignableFrom(Short.class))
			retNum = new Short(n.shortValue());
		else if (c.isAssignableFrom(Byte.class))
			retNum = new Byte(n.byteValue());
		else if (c.isAssignableFrom(Float.class))
			retNum = new Float(n.floatValue());
		else if (c.isAssignableFrom(Double.class))
			retNum = new Double(n.doubleValue());

		if (retNum != null)
			return c.cast(retNum);

		throw new RuntimeException("Unsuported Number type - " + c.getName());
	}

	class MBeanRegistrationListener implements NotificationListener {

		public void handleNotification(Notification notification,
				Object handback) {
			ObjectName sourceMBean = ((MBeanServerNotification) notification)
					.getMBeanName();

			if (notification.getType().equals(
				MBeanServerNotification.REGISTRATION_NOTIFICATION)) {

				if (!isObservedObject(sourceMBean))
					return;

				monitorProxy.addObservedObject(sourceMBean);

				try {
					if (unknownObservedAttributeType) {
						setSpecificProperties(monitorProxy, info,
							getObservedAttributeType(monitorProxy));

						monitorProxy.start();
						unknownObservedAttributeType = false;
					}

				} catch (UnknownObservedAttributeTypeException e) {
					// do nothing
				} catch (Exception e) {
					logger.error(e);
				}
			} else {
				if (info.getObjectName().equals(sourceMBean)){
					// OH! too bad, the monitor was unregistered by other means
					return;
				}
					
				// just remove the unregistered mbean from the list of observed objects
				// this avoids generating jmx.monitor.error.mbean events
				monitorProxy.removeObservedObject(sourceMBean);
			}
		}

		private boolean isObservedObject(ObjectName objName) {
			for (ObjectName observedObj : info.getObservedObjects()) {
				if (observedObj.apply(objName))
					return true;
			}
			return false;
		}
	}

}
