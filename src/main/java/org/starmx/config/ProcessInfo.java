package org.starmx.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.starmx.core.Process;

public class ProcessInfo extends ConfigurationItem {

	private String id;
	private String policyType;
	private String policyFile;
	private Class<Process> javaClass;
	private List<ObjectMapping> objects = new ArrayList<ObjectMapping>();
	private boolean local;

	public ProcessInfo(String id, String policyType, String policyFile) {
		this.id = id;
		this.policyType = policyType.trim();
		this.policyFile = policyFile.trim();
	}

	public ProcessInfo(String id, Class<Process> klass) {
		this.id = id;
		this.javaClass = klass;
	}

	public String getId() {
		return id;
	}

	public String getPolicyFile() {
		return policyFile;
	}

	public String getPolicyType() {
		return policyType;
	}

	public Class<Process> getJavaClass() {
		return javaClass;
	}

	public boolean isLocal() {
		return local;
	}

	void setLocal(boolean local) {
		this.local = local;
	}

	void addObjectMapping(ObjectMapping om) {
		objects.add(om);

		if (configuration != null && configuration != om.getConfiguration())
			om.setConfiguration(configuration);
	}

	void addObjectMappings(List<ObjectMapping> objectMappings) {
		objects.addAll(objectMappings);
		
		if (configuration != null) {
			for (ObjectMapping om : objectMappings) {
				if (configuration != om.getConfiguration())
					om.setConfiguration(configuration);
			}
		}
	}

	public List<ObjectMapping> getObjectMappings() {
		return objects;
	}

	public ObjectMapping getObjectMapping(String mappingName) {
		for (ObjectMapping mapping : objects) {
			if (mapping.getName().equals(mappingName))
				return mapping;
		}
		return null;
	}

	public Collection<MBeanInfo> getAllMBeanInfo() {
		Collection<MBeanInfo> mbeans = new ArrayList<MBeanInfo>();
		for (ObjectMapping om : objects) {
			if (om.getRefType() == ObjectMapping.Type.MBean)
				mbeans.add(configuration.getMBeanInfo(om.getRef()));
		}
		return mbeans;
	}

	public Collection<MBeanServerInfo> getAllMBeanServerInfo() {
		Set<String> mbeanServerIds = new HashSet<String>();

		for (ObjectMapping om : objects) {
			if (om.getRefType() == ObjectMapping.Type.MBean)
				mbeanServerIds.add(configuration.getMBeanInfo(om.getRef())
						.getMbeanServerId());
		}

		Collection<MBeanServerInfo> mbeanServers = new ArrayList<MBeanServerInfo>();
		for (String mbeanServerId : mbeanServerIds) {
			mbeanServers.add(configuration.getMBeanServerInfo(mbeanServerId));
		}

		return mbeanServers;
	}

}
