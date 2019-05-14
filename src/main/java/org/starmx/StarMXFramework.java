package org.starmx;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.starmx.config.Configuration;
import org.starmx.config.ConfigurationException;
import org.starmx.config.ConfigurationLoader;
import org.starmx.config.ExecutionChainInfo;
import org.starmx.config.MonitorMBeanInfo;
import org.starmx.config.ProcessInfo;
import org.starmx.core.ExecutionChainDeployer;
import org.starmx.jmx.mbeanserver.MBeanServerConnectionException;
import org.starmx.jmx.mbeanserver.MBeanServerProvider;
import org.starmx.jmx.monitor.JMXMonitorEnabler;
import org.starmx.jmx.proxy.MBeanProxyInstanceFactory;
import org.starmx.naming.ObjectLocatorFactory;

public class StarMXFramework {

	private boolean enabled = true;
	private Configuration config;
	private MBeanServerProvider mbeanServerProvider;
	private StarMXContext context;
	private ExecutionChainDeployer execChainDeployer;
	private JMXMonitorEnabler monitorEnabler;

	private static Logger logger = Logger.getLogger(StarMXFramework.class);

	private static StarMXFramework theActiveInstance;

	private StarMXFramework() {
	}

	/**
	 * 
	 * @return the current active instance of StarMXFramework
	 */
	public static StarMXFramework activeInstance() {
		return theActiveInstance;
	}

	/**
	 * It starts the framework.
	 * @return The created instance of the StarMXFramework object 
	 * @throws StarMXException if an exception occurs at startup
	 */
	public static StarMXFramework createInstance() throws StarMXException {
		if (theActiveInstance != null)
			return theActiveInstance;

		try {
			StarMXFramework starmx = new StarMXFramework();

			// load starmx.xml
			starmx.loadConfiguration();

			// initialize log4j
			starmx.initLogger();
			
			logger.debug("Starting StarMX...");

			// initialize MBeanServerProvider and add notification listener
			starmx.initMBeanServers();

			starmx.initContext();

			starmx.createMonitors();
			
			// deploy policies (create policy life cycle manager)
			starmx.deployProcesses();

			theActiveInstance = starmx;

			logger.info("StarMX started successfully");
			return theActiveInstance;

		} catch (Exception e) {
			logger.error("Failed to start up StarMX. Exception: "
					+ e.getMessage());
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			else if (e instanceof StarMXException)
				throw (StarMXException) e;
			else
				throw new StarMXException("StarMX start up failed.", e);
		}
	}

	/**
	 * Initializes log4j logger. It appends starmx logger properties to the
	 * pre-configured log4j, if exists
	 */
	private void initLogger() throws StarMXException {
		InputStream in = StarMXFramework.class.getClassLoader()
				.getResourceAsStream("starmx.log4j.properties");
		Properties prop = new Properties();
		try {
			prop.load(in);

			String logDir = config.getProperty(StarMXProperties.LOG_DIR);
			if (logDir == null) {
				logDir = ".";
			}
			prop.put(StarMXProperties.LOG_DIR, logDir);

			// load log settings
			PropertyConfigurator.configure(prop);

			Logger starmxLogger = Logger.getLogger("org.starmx");

			// overwrite the log-level based on starmx.xml properties
			String logLevel = config.getProperty(StarMXProperties.LOG_LEVEL);
			if (logLevel != null) {
				Level level = Level.toLevel(logLevel);

				// set the log level of the parent logger of all starmx loggers
				starmxLogger.setLevel(level);
			}

			// replace starmx-root-logger console appender with that of root
			// logger
			Enumeration<Appender> parentAppenders = starmxLogger.getParent()
					.getAllAppenders();
			while (parentAppenders.hasMoreElements()) {
				Appender appender = parentAppenders.nextElement();
				if (appender instanceof ConsoleAppender) {
					// replace starmx console appender with this appender
					starmxLogger.removeAppender("stdout");
					starmxLogger.addAppender(appender);
					break;
				}
			}
		} catch (IOException e) {
			throw new StarMXException(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
	}

	private void loadConfiguration() throws ConfigurationException {
		config = ConfigurationLoader.loadConfig();
	}

	private void initMBeanServers() throws MBeanServerConnectionException {
		mbeanServerProvider = new MBeanServerProvider();
		mbeanServerProvider.init(config);
	}

	private void initContext() {
		context = StarMXContext.createInstance();
		context.setConfiguration(config);
		context.setMBeanServerService(mbeanServerProvider);
		context.setMBeanProxyInstanceFactory(new MBeanProxyInstanceFactory(
				mbeanServerProvider));
		context.setObjectLocatorFactory(new ObjectLocatorFactory());
		context.setStarmxScope(new StarMXScope());
	}

	private void createMonitors(){
		monitorEnabler = new JMXMonitorEnabler();
		for (MonitorMBeanInfo info : config.getAllMonitorMBeanInfo()){
			monitorEnabler.createMonitor(info);
		}
	}
	
	private void deployProcesses() {
		execChainDeployer = new ExecutionChainDeployer();
		for (ExecutionChainInfo chainInfo : config.getAllExecutionChainInfo()){
			execChainDeployer.deployExecutionChain(chainInfo);
		}
	}

	private void shutdownMBeanServers() throws MBeanServerConnectionException {
		mbeanServerProvider.destroy();
	}

	private void destroyMonitors(){
		for (MonitorMBeanInfo info : config.getAllMonitorMBeanInfo()){
			monitorEnabler.destroyMonitor(info);
		}
	}
	
	private void undeployProcesses() {
		for (ExecutionChainInfo chainInfo : config.getAllExecutionChainInfo()){
			execChainDeployer.undeployExecutionChain(chainInfo);
		}
		
		for (ProcessInfo pInfo : config.getAllProcessInfo()){
			execChainDeployer.undeployProcess(pInfo);
		}
	}

	/**
	 * It shuts down the framework.
	 * @throws StarMXException if an exception occurs during shut down
	 */
	public void shutdown() throws StarMXException {

		logger.debug("Shutting down StarMX ...");

		try {
			undeployProcesses();

			destroyMonitors();
			
			shutdownMBeanServers();

			logger.info("StarMX shut down successfully");
		} catch (Exception e) {
			logger.error("Failed to shut down StarMX. Exception: "
					+ e.getMessage());
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			else if (e instanceof StarMXException)
				throw (StarMXException) e;
			else
				throw new StarMXException("StarMX shut down failed.", e);
		} finally {
			context.destroyInstance();
			execChainDeployer = null;
			theActiveInstance = null;
		}
	}

//	public Object executePolicy(String policyName) throws PolicyException {
//		PolicyManager pm = context.getPolicyManager(policyName);
//		if (pm == null)
//			throw new PolicyException("Policy <" + policyName + "> not found");
//
//		return pm.executePolicy();
//	}
//
	private void enable() {
		execChainDeployer.enableAll();
		enabled = true;
	}

	private void disable() {
		execChainDeployer.disableAll();
		enabled = false;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (enabled)
			enable();
		else
			disable();
	}

	public static void main(String[] args) {
		try {
			final StarMXFramework starmx = createInstance();

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						starmx.shutdown();
					} catch (Exception e) {
						logger.error("", e);
					}

					// starmx.notify();
				}
			});

			Thread.currentThread().join();
			// synchronized(starmx){
			// starmx.wait();
			// }
			// starmx.shutdown();
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
		}
	}
}
