package org.starmx.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.starmx.config.ObjectMapping.Type;

public class Configuration implements PropertySet {

	private Map<String, String> properties = new HashMap<String, String>();
	private Map<String, MBeanServerInfo> mbeanServers = new HashMap<String, MBeanServerInfo>();
	private Map<String, MonitorMBeanInfo> monitorMBeans = new HashMap<String, MonitorMBeanInfo>();
	private Map<String, MBeanInfo> mbeans = new HashMap<String, MBeanInfo>();
	private Map<String, BeanInfo> beans = new HashMap<String, BeanInfo>();
	private Map<String, ProcessInfo> processes = new HashMap<String, ProcessInfo>();
	private Map<String, Object> allAnchorObjects = new HashMap<String, Object>();
	private Collection<ExecutionChainInfo> execChains = new ArrayList<ExecutionChainInfo>();

	Configuration() {
	}

	public String getProperty(String name) {
		return properties.get(name);
	}

	public void setProperty(String name, String value) {
		properties.put(name, value);
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public MBeanServerInfo getMBeanServerInfo(String id) {
		return mbeanServers.get(id);
	}

	public Collection<MBeanServerInfo> getAllMBeanServerInfo() {
		return mbeanServers.values();
	}

	public void addMBeanServerInfo(MBeanServerInfo mbeanServer) {
		mbeanServers.put(mbeanServer.getId(), mbeanServer);
		mbeanServer.setConfiguration(this);
	}

	public MBeanInfo getMBeanInfo(String id) {
		return mbeans.get(id);
	}

	public Collection<MBeanInfo> getAllMBeanInfo() {
		return mbeans.values();
	}

	public void addMBeanInfo(MBeanInfo mbean) {
		mbeans.put(mbean.getId(), mbean);
		allAnchorObjects.put(mbean.getId(), mbean);

		mbean.setConfiguration(this);
	}

	public BeanInfo getBeanInfo(String id) {
		return beans.get(id);
	}

	public Collection<BeanInfo> getAllBeanInfo() {
		return beans.values();
	}

	public void addBeanInfo(BeanInfo bean) {
		beans.put(bean.getId(), bean);
		allAnchorObjects.put(bean.getId(), bean);
		bean.setConfiguration(this);
	}

	public ProcessInfo getProcessInfo(String name) {
		return processes.get(name);
	}

	public Collection<ProcessInfo> getAllProcessInfo() {
		return processes.values();
	}

	public void addProcessInfo(ProcessInfo process) {
		processes.put(process.getId(), process);
		process.setConfiguration(this);
		for (ObjectMapping om : process.getObjectMappings()) {
			om.setConfiguration(this);
		}
	}

	public Collection<ExecutionChainInfo> getAllExecutionChainInfo() {
		return execChains;
	}

	public void addExecutionChainInfo(ExecutionChainInfo execChain) {
		execChains.add(execChain);
	}

	public Object getAnchorObjectInfo(String objectId) {
		return allAnchorObjects.get(objectId);
	}

	public void addMonitorMBeanInfo(MonitorMBeanInfo info) {
		addMBeanInfo(info);
		monitorMBeans.put(info.getId(), info);
	}

	public Collection<MonitorMBeanInfo> getAllMonitorMBeanInfo() {
		return monitorMBeans.values();
	}

	public void addObjectMapping(String processId, String mappingName,
			String refId) throws ConfigurationException {
		
		Object anchorObjectInfo = getAnchorObjectInfo(refId);
		if (anchorObjectInfo == null) {
			throw new ConfigurationException("No anchor object found with id: "
					+ refId);
		}

		Type refType = null;
		if (anchorObjectInfo instanceof MBeanInfo) {
			refType = Type.MBean;
		} else if (anchorObjectInfo instanceof BeanInfo) {
			refType = Type.Bean;
		} else {
			throw new ConfigurationException(
					"Internal error in detecting referenced anchor object type");
		}

		ObjectMapping om = new ObjectMapping(mappingName, refType, refId);

		ProcessInfo pInfo = getProcessInfo(processId);
		if (pInfo == null)
			throw new ConfigurationException("No process found with id: "
					+ processId);

		pInfo.addObjectMapping(om);
	}
}
