package org.starmx.jmx.mbeanserver;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.starmx.config.Configuration;
import org.starmx.config.MBeanServerInfo;
import org.starmx.util.BeanUtil;

/**
 * Deals with remote and local mbean-servers
 * 
 */
public class MBeanServerProvider implements MBeanServerService {

	private Map<String, MBeanServerComposite> mbeanServers;
	private Map<String, JMXConnector> connectors;
	private ObjectName mbeanSvrDlgObjectName;

	private static Logger logger = Logger.getLogger(MBeanServerProvider.class);

	public MBeanServerProvider() {
		mbeanServers = new HashMap<String, MBeanServerComposite>();
		connectors = new HashMap<String, JMXConnector>();

		try {
			mbeanSvrDlgObjectName = new ObjectName(
					"JMImplementation:type=MBeanServerDelegate");
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
	}

	public void init(Configuration config)
			throws MBeanServerConnectionException {
		for (MBeanServerInfo mbeanServerInfo : config.getAllMBeanServerInfo()) {
			initMBeanServerConnection(mbeanServerInfo);
		}
	}

	public void destroy() throws MBeanServerConnectionException {
		for (MBeanServerComposite server : mbeanServers.values()) {
			removeMBeanRegistrationListener(server);
		}

		for (String mbeanServerId : connectors.keySet()) {
			try {
				connectors.get(mbeanServerId).close();
			} catch (IOException e) {
				throw new MBeanServerConnectionException(
						"Cannot close JMXConnector to MBeanServer: "
								+ mbeanServerId, e);
			}
		}

		mbeanServers.clear();
		connectors.clear();
	}

	public MBeanServerConnection getMBeanServer(String id) {
		return mbeanServers.get(id).connection;
	}

	private void initMBeanServerConnection(MBeanServerInfo mbeanServerInfo)
			throws MBeanServerConnectionException {
		MBeanServerConnection mbeanServer = null;
		switch (mbeanServerInfo.getLookupType()) {
		case Platform:
			mbeanServer = getPlatformMBeanServer();
			break;
		case LocalFind:
			mbeanServer = getLocalMBeanServer(mbeanServerInfo);
			break;
		case JNDI:
			mbeanServer = getMBeanServerByJNDI(mbeanServerInfo);
			break;
		case JMXRemoting:
			mbeanServer = getMBeanServerByJMXRemoting(mbeanServerInfo);
			break;
		case FactoryMethod:
			mbeanServer = getMBeanServerByFactoryMethod(mbeanServerInfo);
			break;
		}

		mbeanServers.put(mbeanServerInfo.getId(), new MBeanServerComposite(
				mbeanServerInfo, mbeanServer));

		logger.info("MBeanServerConnection created : mbeanServer id="
				+ mbeanServerInfo.getId() + ", type="
				+ mbeanServerInfo.getLookupType());
	}

	private MBeanServerConnection getPlatformMBeanServer() {
		return ManagementFactory.getPlatformMBeanServer();
	}

	private MBeanServerConnection getLocalMBeanServer(MBeanServerInfo info) {
		String agentId = info.getProperty(MBeanServerInfo.LOCALFIND_AGENT_ID);
		int index = 0;
		if (info.getProperty(MBeanServerInfo.LOCALFIND_SERVER_INDEX) != null)
			index = Integer.parseInt(info
					.getProperty(MBeanServerInfo.LOCALFIND_SERVER_INDEX));

		return (MBeanServerConnection) MBeanServerFactory.findMBeanServer(
			agentId).get(index);
	}

	private MBeanServerConnection getMBeanServerByJNDI(MBeanServerInfo info)
			throws MBeanServerConnectionException {
		Hashtable<String, String> env = new Hashtable<String, String>(info
				.getProperties());
		try {
			InitialContext ctx = new InitialContext(env);
			return (MBeanServerConnection) ctx.lookup(info
					.getProperty(MBeanServerInfo.JNDI_NAME));
		} catch (NamingException e) {
			throw new MBeanServerConnectionException(
					"Can not create MBeanServerConnection for mbeanserver id="
							+ info.getId() + " by jndi lookup", e);
		}
	}

	private MBeanServerConnection getMBeanServerByJMXRemoting(
			MBeanServerInfo info) throws MBeanServerConnectionException {
		try {
			JMXServiceURL jmxServiceURL = null;
			if (info.getProperty(MBeanServerInfo.JMX_REMOTE_SERVICE_URL) != null)
				jmxServiceURL = new JMXServiceURL(info
						.getProperty(MBeanServerInfo.JMX_REMOTE_SERVICE_URL));
			else {
				String protocol = info
						.getProperty(MBeanServerInfo.JMX_REMOTE_PROTOCOL);
				String hostname = info
						.getProperty(MBeanServerInfo.JMX_REMOTE_HOST);
				int port = Integer.parseInt(info
						.getProperty(MBeanServerInfo.JMX_REMOTE_PORT));
				String urlPath = info
						.getProperty(MBeanServerInfo.JMX_REMOTE_URLPATH);

				jmxServiceURL = new JMXServiceURL(protocol, hostname, port,
						urlPath);
			}

			Hashtable<String, String> env = new Hashtable<String, String>(info
					.getProperties());
			JMXConnector connector = JMXConnectorFactory.connect(jmxServiceURL,
				env);
			connectors.put(info.getId(), connector);
			return connector.getMBeanServerConnection();
		} catch (Exception e) {
			throw new MBeanServerConnectionException(
					"Can not create MBeanServerConnection for mbeanserver id="
							+ info.getId() + " by jmx remoting", e);
		}
	}

	private MBeanServerConnection getMBeanServerByFactoryMethod(
			MBeanServerInfo info) throws MBeanServerConnectionException {
		try {
			Class<?> mbeanServerFactoryClass = Class.forName(info
					.getProperty(MBeanServerInfo.FACTORY_METHOD_CLASS_NAME));
			String factoryMethod = info
					.getProperty(MBeanServerInfo.FACTORY_METHOD_METHOD_NAME);

			return (MBeanServerConnection) BeanUtil.createInstance(
				mbeanServerFactoryClass, factoryMethod, null);

		} catch (Exception e) {
			throw new MBeanServerConnectionException(
					"Can not create MBeanServerConnection for mbeanserver id="
							+ info.getId() + " by factory method", e);
		}
	}

	public void addMBeanRegistrationListener(NotificationListener listener,
			NotificationFilter filter, Object handback)
			throws MBeanServerConnectionException {

		for (MBeanServerComposite server : mbeanServers.values()) {
			addMBeanRegistrationListener(server, listener, filter, handback);
		}
	}

	public void addMBeanRegistrationListener(String mbeanServerId,
			NotificationListener listener, NotificationFilter filter,
			Object handback) throws MBeanServerConnectionException {

		addMBeanRegistrationListener(mbeanServers.get(mbeanServerId), listener,
			filter, handback);
	}

	private void addMBeanRegistrationListener(MBeanServerComposite server,
			NotificationListener listener, NotificationFilter filter,
			Object handback) throws MBeanServerConnectionException {
		try {
			server.connection.addNotificationListener(mbeanSvrDlgObjectName,
				listener, filter, handback);
			server.mbeanRegListeners.add(listener);
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.error("Can not add notification listener for mbean registrations on "
							+ "mbean-server: id="
							+ server.info.getId()
							+ ", lookupType"
							+ server.info.getLookupType()
							+ "Maybe the mbean-server connection does not support remote notification listeners.");

			throw new MBeanServerConnectionException(e);
		}
	}

	public void removeMBeanRegistrationListener(NotificationListener listener)
			throws MBeanServerConnectionException {
		for (MBeanServerComposite server : mbeanServers.values()) {
			removeMBeanRegistrationListener(server, listener);
		}
	}

	public void removeMBeanRegistrationListener(String mbeanServerId,
			NotificationListener listener)
			throws MBeanServerConnectionException {
		removeMBeanRegistrationListener(mbeanServers.get(mbeanServerId),
			listener);
	}

	private void removeMBeanRegistrationListener(MBeanServerComposite server)
			throws MBeanServerConnectionException {
		while (!server.mbeanRegListeners.isEmpty()) {
			NotificationListener listener = server.mbeanRegListeners.get(0);
			removeMBeanRegistrationListener(server, listener);
		}
	}

	private void removeMBeanRegistrationListener(MBeanServerComposite server,
			NotificationListener listener)
			throws MBeanServerConnectionException {
		try {
			server.connection.removeNotificationListener(mbeanSvrDlgObjectName,
				listener);
			server.mbeanRegListeners.remove(listener);
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException(e);
		} catch (ListenerNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new MBeanServerConnectionException(e);
		}
	}

	class MBeanServerComposite {
		private MBeanServerInfo info;
		private MBeanServerConnection connection;
		private List<NotificationListener> mbeanRegListeners;

		MBeanServerComposite(MBeanServerInfo info,
				MBeanServerConnection connection) {
			this.info = info;
			this.connection = connection;
			mbeanRegListeners = new ArrayList<NotificationListener>();
		}
	}
}
