package org.starmx.config;

import javax.management.ObjectName;
import javax.management.monitor.GaugeMonitorMBean;

public class GaugeMonitorMBeanInfo extends MonitorMBeanInfo {

	private Number highThreshold;
	private Number lowThreshold;
	private Boolean notifyHigh = null;
	private Boolean notifyLow = null;
	private Boolean differenceMode = null;
	
	public GaugeMonitorMBeanInfo(String id, ObjectName objectName,
			String mbeanServerId, long granularityPeriod,
			String observedAttribute, Number highThreshold, Number lowThreshold) {
		super(id, objectName, mbeanServerId, GaugeMonitorMBean.class,
				granularityPeriod, observedAttribute);

		this.highThreshold = highThreshold;
		this.lowThreshold = lowThreshold;
	}
	
	public Number getHighThreshold() {
		return highThreshold;
	}
	public Number getLowThreshold() {
		return lowThreshold;
	}
	public Boolean getNotifyHigh() {
		return notifyHigh;
	}
	public void setNotifyHigh(Boolean notifyHigh) {
		this.notifyHigh = notifyHigh;
	}
	public Boolean getNotifyLow() {
		return notifyLow;
	}
	public void setNotifyLow(Boolean notifyLow) {
		this.notifyLow = notifyLow;
	}
	public Boolean getDifferenceMode() {
		return differenceMode;
	}
	public void setDifferenceMode(Boolean differenceMode) {
		this.differenceMode = differenceMode;
	}

}
