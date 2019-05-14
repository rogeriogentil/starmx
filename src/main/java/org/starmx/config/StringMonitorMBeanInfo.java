package org.starmx.config;

import javax.management.ObjectName;
import javax.management.monitor.StringMonitorMBean;

public class StringMonitorMBeanInfo extends MonitorMBeanInfo {

	private String stringToCompare;
	private Boolean notifyMatch = null;
	private Boolean notifyDiffer = null;
	
	public StringMonitorMBeanInfo(String id, ObjectName objectName,
			String mbeanServerId, long granularityPeriod,
			String observedAttribute, String stringToCompare) {
		super(id, objectName, mbeanServerId, StringMonitorMBean.class,
				granularityPeriod, observedAttribute);

		this.stringToCompare = stringToCompare;
	}

	public Boolean getNotifyMatch() {
		return notifyMatch;
	}

	public void setNotifyMatch(Boolean notifyMatch) {
		this.notifyMatch = notifyMatch;
	}

	public Boolean getNotifyDiffer() {
		return notifyDiffer;
	}

	public void setNotifyDiffer(Boolean notifyDiffer) {
		this.notifyDiffer = notifyDiffer;
	}

	public String getStringToCompare() {
		return stringToCompare;
	}
}
