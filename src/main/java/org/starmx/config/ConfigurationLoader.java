package org.starmx.config;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.starmx.config.MBeanServerInfo.LookupType;
import org.starmx.config.ObjectMapping.Type;
import org.starmx.config.TimerInfo.IntervalUnit;
import org.starmx.core.ExecutionChainListener;
import org.starmx.core.Process;
import org.starmx.util.ResourceFinder;

public class ConfigurationLoader {

	private Configuration config;

	private ConfigurationLoader() {
	}

	public static Configuration loadConfig() throws ConfigurationException {
		InputStream ins = null;
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty("javax.xml.stream.isValidating", Boolean.TRUE);

			factory.setXMLReporter(new XMLReporter() {
				public void report(String message, String errorType,
						Object relatedInformation, Location location)
						throws XMLStreamException {
					throw new XMLStreamException(message);
				}
			});

			factory.setXMLResolver(new XMLResolver() {
				public Object resolveEntity(String publicID, String systemID,
						String baseURI, String namespace)
						throws XMLStreamException {
					int dtdStart = systemID.indexOf("starmx-");
					int dtdEnd = systemID.lastIndexOf(".dtd");
					if (dtdStart != -1 && dtdEnd != -1 && dtdStart < dtdEnd) {
						String starmx_dtd = systemID.substring(dtdStart,
							dtdEnd + 4);
						return Configuration.class.getClassLoader()
								.getResourceAsStream(starmx_dtd);
					}
					throw new XMLStreamException(
							"No DTD specified as SYSTEM ID in starmx.xml");
				}
			});

			ins = ResourceFinder.getResourceAsStream("starmx.xml");
			if (ins == null)
				throw new ConfigurationException("Can not find starmx.xml file");

			XMLStreamReader reader = factory.createXMLStreamReader(ins);

			ConfigurationLoader cl = new ConfigurationLoader();
			return cl.parseConfiguration(reader);
		} catch (XMLStreamException e) {
			throw new ConfigurationException(e);
		} catch (FactoryConfigurationError e) {
			throw new ConfigurationException(e);
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void toStartTag(XMLStreamReader reader) throws XMLStreamException {
		while (!reader.isStartElement() && reader.hasNext())
			reader.next();
	}

	private Configuration parseConfiguration(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {
		config = new Configuration();

		toStartTag(reader);

		while (reader.hasNext()) {
			if ("mbeanserver".equals(reader.getLocalName())) {
				config.addMBeanServerInfo(parseMBeanServer(reader));
			} else if ("mbean".equals(reader.getLocalName())) {
				config.addMBeanInfo(parseMBean(reader));
			} else if ("bean".equals(reader.getLocalName())) {
				config.addBeanInfo(parseBean(reader));
			} else if ("monitor-mbean".equals(reader.getLocalName())) {
				config.addMonitorMBeanInfo(parseMonitorMBeanInfo(reader));
			} else if ("process".equals(reader.getLocalName())) {
				ProcessInfo pInfo = parseProcess(reader);
				pInfo.setLocal(false);
				config.addProcessInfo(pInfo);
			} else if ("execute".equals(reader.getLocalName())) {
				config.addExecutionChainInfo(parseExecutionChain(reader));
			} else if ("property".equals(reader.getLocalName())) {
				parseProperty(reader, config);
			}
			reader.next();
			toStartTag(reader);
		}

		return config;
	}

	private MBeanServerInfo parseMBeanServer(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {
		String id = reader.getAttributeValue(null, "id");
		String lookupTypeStr = reader.getAttributeValue(null, "lookup-type");
		LookupType lkpType = null;
		if (lookupTypeStr.equals("platform")) {
			lkpType = LookupType.Platform;
		} else if (lookupTypeStr.equals("find")) {
			lkpType = LookupType.LocalFind;
		} else if (lookupTypeStr.equals("jndi")) {
			lkpType = LookupType.JNDI;
		} else if (lookupTypeStr.equals("jmx")) {
			lkpType = LookupType.JMXRemoting;
		} else if (lookupTypeStr.equals("factory")) {
			lkpType = LookupType.FactoryMethod;
		} else {
			throw new ConfigurationException("Invalid lookup-type: "
					+ lookupTypeStr + " for mbeanserver " + id + " at line "
					+ reader.getLocation().getLineNumber());
		}
		MBeanServerInfo mbeanserver = new MBeanServerInfo(id, lkpType);

		// handle mbeanserver params
		if (lkpType != LookupType.Platform) {
			reader.next();
			toStartTag(reader);
		}

		if (lkpType == LookupType.LocalFind) {
			if ("find-param".equals(reader.getLocalName())) {
				String agentId = reader.getAttributeValue(null, "agent-id");
				String index = reader.getAttributeValue(null, "index");

				mbeanserver.setProperty(MBeanServerInfo.LOCALFIND_AGENT_ID,
					agentId);
				if (index != null)
					mbeanserver.setProperty(
						MBeanServerInfo.LOCALFIND_SERVER_INDEX, index);
			} else {
				throw new ConfigurationException(
						"Expected to see <find-param> tag in mbeanserver " + id
								+ " at line "
								+ reader.getLocation().getLineNumber());
			}
		} else if (lkpType == LookupType.JNDI) {
			if ("jndi-param".equals(reader.getLocalName())) {
				String jndiName = reader.getAttributeValue(null, "jndi-name");

				mbeanserver.setProperty(MBeanServerInfo.JNDI_NAME, jndiName);

				parseProperties(reader, mbeanserver);
			} else {
				throw new ConfigurationException(
						"Expected to see <jndi-param> tag in mbeanserver " + id
								+ " at line "
								+ reader.getLocation().getLineNumber());
			}
		} else if (lkpType == LookupType.JMXRemoting) {
			if ("jmx-param".equals(reader.getLocalName())) {
				String serviceUrl = reader.getAttributeValue(null,
					"service-url");
				if (serviceUrl != null) {
					mbeanserver.setProperty(
						MBeanServerInfo.JMX_REMOTE_SERVICE_URL, serviceUrl);
				} else {
					String protocol = reader
							.getAttributeValue(null, "protocol");
					String host = reader.getAttributeValue(null, "host");
					String port = reader.getAttributeValue(null, "port");
					String urlPath = reader.getAttributeValue(null, "url-path");

					mbeanserver.setProperty(
						MBeanServerInfo.JMX_REMOTE_PROTOCOL, protocol);
					mbeanserver.setProperty(MBeanServerInfo.JMX_REMOTE_HOST,
						host);
					mbeanserver.setProperty(MBeanServerInfo.JMX_REMOTE_PORT,
						port);
					mbeanserver.setProperty(MBeanServerInfo.JMX_REMOTE_URLPATH,
						urlPath);
				}

				parseProperties(reader, mbeanserver);
			} else {
				throw new ConfigurationException(
						"Expected to see <jndi-param> tag in mbeanserver " + id
								+ " at line "
								+ reader.getLocation().getLineNumber());
			}

		} else if (lkpType == LookupType.FactoryMethod) {
			if ("factory-param".equals(reader.getLocalName())) {
				String className = reader.getAttributeValue(null, "class");
				String methodName = reader.getAttributeValue(null, "method");

				mbeanserver.setProperty(
					MBeanServerInfo.FACTORY_METHOD_CLASS_NAME, className);
				mbeanserver.setProperty(
					MBeanServerInfo.FACTORY_METHOD_METHOD_NAME, methodName);
			} else {
				throw new ConfigurationException(
						"Expected to see <factory-param> tag in mbeanserver "
								+ id + " at line "
								+ reader.getLocation().getLineNumber());
			}
		}

		while (reader.next() != XMLStreamConstants.END_ELEMENT) {
			// skip non-related content
		}

		return mbeanserver;
	}

	private MBeanInfo parseMBean(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {
		String id = reader.getAttributeValue(null, "id");
		String objectNameStr = reader.getAttributeValue(null, "object-name");
		ObjectName objName = null;
		try {
			objName = new ObjectName(objectNameStr);
		} catch (MalformedObjectNameException e) {
			throw new ConfigurationException("Invalid ObjectName for mbean "
					+ id + " at line " + reader.getLocation().getLineNumber(),
					e);
		}

		String mbeanServerId = reader.getAttributeValue(null, "mbeanserver");
		if (config.getMBeanServerInfo(mbeanServerId) == null) {
			throw new ConfigurationException("Unknown mbeanserver "
					+ mbeanServerId + " at line "
					+ reader.getLocation().getLineNumber());
		}

		String intfName = reader.getAttributeValue(null, "interface");
		Class<?> mbeanInterface = null;
		try {
			if (intfName != null) {
				mbeanInterface = Class.forName(intfName);
				if (!mbeanInterface.isInterface()) {
					throw new ConfigurationException("Class " + intfName
							+ " is not an interface, at line "
							+ reader.getLocation().getLineNumber());
				}
			}
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException(e.getMessage() + " at line "
					+ reader.getLocation().getLineNumber(), e);
		}

		MBeanInfo mbeanInfo = new MBeanInfo(id, objName, mbeanServerId,
				mbeanInterface);

		// handle mbean properties
		parseProperties(reader, mbeanInfo);

		return mbeanInfo;
	}

	private BeanInfo parseBean(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {
		String id = reader.getAttributeValue(null, "id");
		String klass = reader.getAttributeValue(null, "class");
		String factoryMethod = reader.getAttributeValue(null, "factory-method");
		try {
			Class<?> clazz = Class.forName(klass);
			if (factoryMethod != null && factoryMethod.trim().length() > 0) {
				return new BeanInfo(id, clazz, factoryMethod);
			} else {
				return new BeanInfo(id, clazz);
			}
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException(e);
		}
	}

	private ProcessInfo parseProcess(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {
		String id = reader.getAttributeValue(null, "id");
		String classname = reader.getAttributeValue(null, "javaclass");
		Class<Process> processClass = null;
		try {
			if (classname != null) {
				processClass = (Class<Process>) Class.forName(classname);
			}
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException(e.getMessage() + " at line "
					+ reader.getLocation().getLineNumber(), e);
		} catch (ClassCastException e) {
			throw new ConfigurationException("Class " + classname
					+ " does not implement " + Process.class.getName()
					+ "interface; at line "
					+ reader.getLocation().getLineNumber());
		}

		String type = reader.getAttributeValue(null, "policy-type");
		String filename = reader.getAttributeValue(null, "policy-file");

		if (classname == null && (type == null || filename == null)) {
			throw new ConfigurationException(
					"Insufficient parameter for <process> tag, at line "
							+ reader.getLocation().getLineNumber());
		}

		if (classname != null && type != null && filename != null) {
			throw new ConfigurationException(
					"Either (javaclass) or (policy-type and policy-file) must "
							+ "be specified for <process> tag, at line "
							+ reader.getLocation().getLineNumber());
		}

		List<ObjectMapping> objectMappings = new ArrayList<ObjectMapping>();

		while (true) {
			if (reader.next() == XMLStreamConstants.END_ELEMENT
					&& reader.getLocalName().equals("process"))
				break;
			if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				if ("object".equals(reader.getLocalName()))
					objectMappings.add(parseObjectMapping(reader));
			}
		}

		ProcessInfo processInfo = null;
		if (classname != null) {
			processInfo = new ProcessInfo(id, processClass);
		} else
			processInfo = new ProcessInfo(id, type, filename);

		processInfo.addObjectMappings(objectMappings);

		return processInfo;
	}

	private ObjectMapping parseObjectMapping(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {
		String name = reader.getAttributeValue(null, "name");
		String ref = reader.getAttributeValue(null, "ref");

		Object anchorObjectInfo = config.getAnchorObjectInfo(ref);
		if (anchorObjectInfo == null) {
			throw new ConfigurationException("Invalid object ref " + ref
					+ " at line " + reader.getLocation().getLineNumber());
		}

		Type refType = null;
		if (anchorObjectInfo instanceof MBeanInfo) {
			refType = Type.MBean;
		} else if (anchorObjectInfo instanceof BeanInfo) {
			refType = Type.Bean;
		} else {
			throw new ConfigurationException(
					"Internal error in detecting referenced object type at line "
							+ reader.getLocation().getLineNumber());
		}

		return new ObjectMapping(name, refType, ref);
	}

	private ExecutionChainInfo parseExecutionChain(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {
		String name = reader.getAttributeValue(null, "name");
		String listener = reader.getAttributeValue(null, "listener");
		Class<ExecutionChainListener> listenerClass = null;
		try {
			if (listener != null) {
				listenerClass = (Class<ExecutionChainListener>) Class
						.forName(listener);
			}
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException(e.getMessage() + " at line "
					+ reader.getLocation().getLineNumber(), e);
		} catch (ClassCastException e) {
			throw new ConfigurationException("Class " + listener
					+ " does not implement "
					+ ExecutionChainListener.class.getName()
					+ " interface; at line "
					+ reader.getLocation().getLineNumber());
		}

		reader.next();
		toStartTag(reader);
		ExecutionChainInfo chainInfo = null;
		if ("timer-info".equals(reader.getLocalName())) {
			chainInfo = new ExecutionChainInfo(name, listenerClass,
					parseTimerInfo(reader));
		} else if ("notification-info".equals(reader.getLocalName())) {
			chainInfo = new ExecutionChainInfo(name, listenerClass,
					parseNotificationInfo(reader));
		} else {
			// To be handled
		}

		while (true) {
			if (reader.next() == XMLStreamConstants.END_ELEMENT
					&& reader.getLocalName().equals("execute"))
				break;

			if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				ProcessInfo pInfo = null;
				if ("process".equals(reader.getLocalName())) {
					pInfo = parseProcess(reader);
					pInfo.setLocal(true);
					config.addProcessInfo(pInfo);
				} else if ("processref".equals(reader.getLocalName()))
					pInfo = parseProcessRef(reader);

				if (pInfo != null) {
					chainInfo.addProcessInfo(pInfo);
				}
			}
		}

		return chainInfo;
	}

	private TimerInfo parseTimerInfo(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {
		String interval = reader.getAttributeValue(null, "interval");
		String unit = reader.getAttributeValue(null, "unit");
		String firstExecTimeStr = reader.getAttributeValue(null,
			"first-exec-time");
		String firstExecDelayStr = reader.getAttributeValue(null,
			"first-exec-delay");
		long period = Long.parseLong(interval);
		if (period <= 0) {
			throw new ConfigurationException(
					"Negative value found for interval attribute at line "
							+ reader.getLocation().getLineNumber());
		}

		IntervalUnit iunit = null;
		if (unit == null) {
			iunit = IntervalUnit.Second;
		} else if (unit.equals("second")) {
			iunit = IntervalUnit.Second;
		} else if (unit.equals("minute")) {
			iunit = IntervalUnit.Minute;
		} else if (unit.equals("hour")) {
			iunit = IntervalUnit.Hour;
		} else if (unit.equals("day")) {
			iunit = IntervalUnit.Day;
		} else if (unit.equals("week")) {
			iunit = IntervalUnit.Week;
		} else if (unit.equals("month")) {
			iunit = IntervalUnit.Month;
		} else {
			throw new ConfigurationException("Invalid interval unit " + unit
					+ " at line " + reader.getLocation().getLineNumber());
		}

		Date firstExecTime = null;
		if (firstExecTimeStr != null) {
			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
			try {
				firstExecTime = df.parse(firstExecTimeStr);
			} catch (ParseException e) {
				throw new ConfigurationException(
						"Invalid format for first-exec-time attribute. It must be in HH:mm format, where HH={0-23}, mm={0,59}. Error at line "
								+ reader.getLocation().getLineNumber());
			}
		}

		int firstExecDelay = -1;
		if (firstExecDelayStr != null) {
			firstExecDelay = Integer.parseInt(firstExecDelayStr);
			if (firstExecDelay < 0)
				throw new ConfigurationException(
						"Negative value found for first-exec-delay attribute at line "
								+ reader.getLocation().getLineNumber());
		}

		if (firstExecTimeStr != null && firstExecDelayStr != null)
			throw new ConfigurationException(
					"Only one of the first-exec-time and first-exec-delay must have value, at line "
							+ reader.getLocation().getLineNumber());

		if (firstExecTimeStr != null) {
			return new TimerInfo(period, iunit, firstExecTime);

		} else if (firstExecDelayStr != null) {
			return new TimerInfo(period, iunit, firstExecDelay);
		}
		return new TimerInfo(period, iunit);
	}

	private NotificationInfo parseNotificationInfo(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {

		String mbeanName = reader.getAttributeValue(null, "emitter-mbean-ref");
		String eventType = reader.getAttributeValue(null, "event-type");
		String eventHandling = reader.getAttributeValue(null, "event-handling");
		String eventClassName = reader.getAttributeValue(null, "event-class");

		boolean synchHandling;
		if ("synch".equals(eventHandling) || eventHandling == null) {
			synchHandling = true;
		} else if ("asynch".equals(eventHandling)) {
			synchHandling = false;
		} else {
			throw new ConfigurationException("Invalid event-handling "
					+ eventHandling + " at line "
					+ reader.getLocation().getLineNumber());
		}

		if (eventClassName != null) {
			try {
				Class.forName(eventClassName);
			} catch (ClassNotFoundException e) {
				throw new ConfigurationException(
						"ClassNotFoundException for class " + eventClassName
								+ eventHandling + " at line "
								+ reader.getLocation().getLineNumber());
			}
		}

		if (config.getMBeanInfo(mbeanName) == null) {
			throw new ConfigurationException("mbean with id <" + mbeanName
					+ "> not defined, at line "
					+ reader.getLocation().getLineNumber());
		}

		return new NotificationInfo(eventType, config.getMBeanInfo(mbeanName),
				synchHandling, eventClassName);
	}

	private ProcessInfo parseProcessRef(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {
		String refid = reader.getAttributeValue(null, "refid");
		ProcessInfo pInfo = config.getProcessInfo(refid);
		if (pInfo != null && !pInfo.isLocal())
			return pInfo;

		throw new ConfigurationException("Could not find process with id="
				+ refid + ", at line " + reader.getLocation().getLineNumber());
	}

	private void parseProperties(XMLStreamReader reader, PropertySet propset)
			throws ConfigurationException, XMLStreamException {
		while (reader.next() != XMLStreamConstants.END_ELEMENT) {
			if (reader.getEventType() == XMLStreamConstants.START_ELEMENT
					&& "property".equals(reader.getLocalName())) {
				parseProperty(reader, propset);
			}
		}
	}

	private void parseProperty(XMLStreamReader reader, PropertySet propset)
			throws ConfigurationException, XMLStreamException {
		String name = reader.getAttributeValue(null, "name");
		String value = reader.getElementText();
		propset.setProperty(name, value.trim());
	}

	private MonitorMBeanInfo parseMonitorMBeanInfo(XMLStreamReader reader)
			throws ConfigurationException, XMLStreamException {
		String id = reader.getAttributeValue(null, "id");
		String objectNameStr = reader.getAttributeValue(null, "object-name");
		ObjectName objectName = null;
		try {
			objectName = new ObjectName(objectNameStr);
		} catch (MalformedObjectNameException e) {
			throw new ConfigurationException("Invalid ObjectName for mbean "
					+ id + " at line " + reader.getLocation().getLineNumber(),
					e);
		}

		String mbeanServerId = reader.getAttributeValue(null, "mbeanserver");
		if (config.getMBeanServerInfo(mbeanServerId) == null) {
			throw new ConfigurationException("Unknown mbeanserver "
					+ mbeanServerId + " at line "
					+ reader.getLocation().getLineNumber());
		}

		String observedAttribute = reader.getAttributeValue(null,
			"observed-attribute");
		String periodStr = reader.getAttributeValue(null, "granularity-period");
		long granularityPeriod = Long.parseLong(periodStr);
		if (granularityPeriod <= 0) {
			throw new ConfigurationException(
					"Negative value found for granularity-period attribute at line "
							+ reader.getLocation().getLineNumber());
		}

		reader.next();
		toStartTag(reader);

		MonitorMBeanInfo monitorInfo = null;
		if ("counter-monitor".equals(reader.getLocalName())) {
			monitorInfo = parseCounterMonitorMBeanInfo(reader, id, objectName,
				mbeanServerId, granularityPeriod, observedAttribute);
		} else if ("gauge-monitor".equals(reader.getLocalName())) {
			monitorInfo = parseGaugeMonitorMBeanInfo(reader, id, objectName,
				mbeanServerId, granularityPeriod, observedAttribute);
		} else {
			monitorInfo = parseStringMonitorMBeanInfo(reader, id, objectName,
				mbeanServerId, granularityPeriod, observedAttribute);
		}

		while (true) {
			if (reader.next() == XMLStreamConstants.END_ELEMENT
					&& reader.getLocalName().equals("monitor-mbean"))
				break;

			if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				ObjectName observedObj = null;
				if ("observed-object".equals(reader.getLocalName())) {
					objectNameStr = reader.getAttributeValue(null,
						"object-name");
					try {
						observedObj = new ObjectName(objectNameStr);
					} catch (MalformedObjectNameException e) {
						throw new ConfigurationException(
								"Invalid ObjectName found at line "
										+ reader.getLocation().getLineNumber(),
								e);
					}
				}

				if (observedObj != null) {
					monitorInfo.addObservedObject(observedObj);
				}
			}
		}
		return monitorInfo;
	}

	private CounterMonitorMBeanInfo parseCounterMonitorMBeanInfo(
			XMLStreamReader reader, String id, ObjectName objectName,
			String mbeanServerId, long granularityPeriod,
			String observedAttribute) throws ConfigurationException,
			XMLStreamException {

		Number initThreshold = Long.parseLong(reader.getAttributeValue(null,
			"init-threshold"));
		boolean notify = Boolean.parseBoolean(reader.getAttributeValue(null,
			"notify"));

		CounterMonitorMBeanInfo info = new CounterMonitorMBeanInfo(id,
				objectName, mbeanServerId, granularityPeriod,
				observedAttribute, initThreshold, notify);

		String attrVal = reader.getAttributeValue(null, "modulus");
		if (attrVal != null)
			info.setModulus(Long.parseLong(attrVal));

		attrVal = reader.getAttributeValue(null, "offset");
		if (attrVal != null)
			info.setOffset(Long.parseLong(attrVal));

		attrVal = reader.getAttributeValue(null, "difference-mode");
		if (attrVal != null)
			info.setDifferenceMode(Boolean.parseBoolean(attrVal));

		return info;
	}

	private GaugeMonitorMBeanInfo parseGaugeMonitorMBeanInfo(
			XMLStreamReader reader, String id, ObjectName objectName,
			String mbeanServerId, long granularityPeriod,
			String observedAttribute) throws ConfigurationException,
			XMLStreamException {

		String hiThresholdStr = reader
				.getAttributeValue(null, "high-threshold");
		String loThresholdStr = reader.getAttributeValue(null, "low-threshold");

		Number highThreshold, lowThreshold = null;
		if (hiThresholdStr.indexOf('.') > -1) 
			highThreshold = Double.parseDouble(hiThresholdStr); 
		else
			highThreshold = Long.parseLong(hiThresholdStr);
		if (loThresholdStr.indexOf('.') > -1)
			lowThreshold = Double.parseDouble(loThresholdStr);
		else
			lowThreshold = Long.parseLong(loThresholdStr);

		GaugeMonitorMBeanInfo info = new GaugeMonitorMBeanInfo(id, objectName,
				mbeanServerId, granularityPeriod, observedAttribute,
				highThreshold, lowThreshold);

		String attrVal = reader.getAttributeValue(null, "difference-mode");
		if (attrVal != null)
			info.setDifferenceMode(Boolean.parseBoolean(attrVal));

		attrVal = reader.getAttributeValue(null, "notify-high");
		if (attrVal != null)
			info.setNotifyHigh(Boolean.parseBoolean(attrVal));

		attrVal = reader.getAttributeValue(null, "notify-low");
		if (attrVal != null)
			info.setNotifyLow(Boolean.parseBoolean(attrVal));

		return info;
	}

	private StringMonitorMBeanInfo parseStringMonitorMBeanInfo(
			XMLStreamReader reader, String id, ObjectName objectName,
			String mbeanServerId, long granularityPeriod,
			String observedAttribute) throws ConfigurationException,
			XMLStreamException {

		String stringToCompare = reader.getAttributeValue(null,
			"string-to-compare");

		StringMonitorMBeanInfo info = new StringMonitorMBeanInfo(id,
				objectName, mbeanServerId, granularityPeriod,
				observedAttribute, stringToCompare);

		String attrVal = reader.getAttributeValue(null, "notify-match");
		if (attrVal != null)
			info.setNotifyMatch(Boolean.parseBoolean(attrVal));

		attrVal = reader.getAttributeValue(null, "notify-differ");
		if (attrVal != null)
			info.setNotifyDiffer(Boolean.parseBoolean(attrVal));

		return info;
	}
}
