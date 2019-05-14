package org.starmx.config;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

public class MonitorMBeanInfo extends MBeanInfo {

	private long granularityPeriod;
	private String observedAttribute;
	private List<ObjectName> observedObjects = new ArrayList<ObjectName>();

	protected MonitorMBeanInfo(String id, ObjectName objectName,
			String mbeanServerId, Class<?> mbeanInterface,
			long granularityPeriod, String observedAttribute) {
		super(id, objectName, mbeanServerId, mbeanInterface);
		this.granularityPeriod = granularityPeriod;
		this.observedAttribute = observedAttribute;
	}

	public long getGranularityPeriod() {
		return granularityPeriod;
	}

	public String getObservedAttribute() {
		return observedAttribute;
	}

	public void addObservedObject(ObjectName object) {
		observedObjects.add(object);
	}

	public List<ObjectName> getObservedObjects() {
		return observedObjects;
	}
}
