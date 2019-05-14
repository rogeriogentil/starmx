package org.starmx.core.impl;

import java.io.IOException;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.starmx.StarMXContext;
import org.starmx.config.ExecutionChainInfo;
import org.starmx.config.MBeanInfo;
import org.starmx.config.NotificationInfo;
import org.starmx.core.Activator;
import org.starmx.core.ExecutionChain;
import org.starmx.jmx.mbeanserver.MBeanServerConnectionException;
import org.starmx.jmx.mbeanserver.MBeanServerService;

public class NotificationActivator implements Activator {

	private static Logger logger = Logger.getLogger(NotificationActivator.class);

	private ExecutionChain executionChain;
	private ExecutionChainInfo chainInfo;
	private NotificationInfo notificationInfo;
	private NotificationListener listener;
	private MBeanRegistrationListener mbeanRegListener;

	public NotificationActivator(ExecutionChain executionChain) {
		this.executionChain = executionChain;
		chainInfo = executionChain.getChainInfo();
		notificationInfo = chainInfo.getNotificationInfo();
		
		mbeanRegListener = new MBeanRegistrationListener();
	}

	public void start() {
		MBeanInfo emitterMbean = notificationInfo.getEmitterMbean();
		MBeanServerConnection mbeanServer = getMBeanServer(emitterMbean
				.getMbeanServerId());
		if (listener == null) {
			listener = new MBeanEventListener(notificationInfo.getEventType(),
					notificationInfo.getEventClassName(), notificationInfo
							.isSynchronous(), executionChain);
		}

		try {
			if (emitterMbean.isPattern()) {
				Set<ObjectName> nameSet = mbeanServer.queryNames(emitterMbean
						.getObjectName(), null);
				for (ObjectName objName : nameSet) {
					mbeanServer.addNotificationListener(objName, listener,
						null, null);
				}
			} else {
				mbeanServer.addNotificationListener(emitterMbean
						.getObjectName(), listener, null, null);
			}

			logger.debug("Added notification listener for chain: "
					+ chainInfo.getInternalId() + " on MBean(s) <"
					+ emitterMbean.getObjectName().getCanonicalName()
					+ ">, Event: " + notificationInfo.getEventType());

		} catch (InstanceNotFoundException e) {
			logger.warn("MBean not found to add notification listener: "
					+ e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try {
			getMBeanSvrSrvice().addMBeanRegistrationListener(emitterMbean
					.getMbeanServerId(), mbeanRegListener,	null, null);
		} catch (MBeanServerConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		
		MBeanInfo emitterMbean = notificationInfo.getEmitterMbean();
		MBeanServerConnection mbeanServer = getMBeanServer(emitterMbean
				.getMbeanServerId());
		try {
			getMBeanSvrSrvice().removeMBeanRegistrationListener(emitterMbean
					.getMbeanServerId(), mbeanRegListener);
		} catch (MBeanServerConnectionException e) {
			throw new RuntimeException(e);
		}
		
		try {
			if (emitterMbean.isPattern()) {
				Set<ObjectName> nameSet = mbeanServer.queryNames(emitterMbean
						.getObjectName(), null);
				for (ObjectName objName : nameSet) {
					mbeanServer.removeNotificationListener(objName, listener);
				}
			} else {
				mbeanServer.removeNotificationListener(emitterMbean
						.getObjectName(), listener);
			}
			logger.debug("Removed notification listener of chain: "
					+ chainInfo.getInternalId());
		} catch (InstanceNotFoundException e) {
			// ignore it
		} catch (ListenerNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void addListenerOnMBean(ObjectName objectName) {
		if (listener == null)
			return;
		
		MBeanServerConnection mbeanServer = getMBeanServer(chainInfo
				.getNotificationInfo().getEmitterMbean().getMbeanServerId());
		try {
			mbeanServer.addNotificationListener(objectName, listener, null,	null);
			logger.debug("Added notification listener for chain: "
					+ chainInfo.getInternalId() + " on MBean <" + objectName + ">");
		} catch (InstanceNotFoundException e) {
			logger.warn("MBean not found to add notification listener: "
					+ e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private MBeanServerService getMBeanSvrSrvice(){
		return StarMXContext.getDefault().getMBeanServerService();
	}
	
	private MBeanServerConnection getMBeanServer(String mbeanServerId) {
		return getMBeanSvrSrvice().getMBeanServer(mbeanServerId);
	}
	
	class MBeanRegistrationListener implements NotificationListener {

		private MBeanInfo emitterMbean = notificationInfo.getEmitterMbean();
		
		public void handleNotification(Notification notification,
				Object handback) {
			if (notification.getType().equals(
				MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
				ObjectName objectName = ((MBeanServerNotification) notification)
						.getMBeanName();

				if (emitterMbean.getObjectName().apply(objectName)){
					addListenerOnMBean(objectName);
				}
			}
		}
	}
}
