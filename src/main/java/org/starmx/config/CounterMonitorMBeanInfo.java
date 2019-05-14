package org.starmx.config;

import javax.management.ObjectName;
import javax.management.monitor.CounterMonitorMBean;

public class CounterMonitorMBeanInfo extends MonitorMBeanInfo {

	private Number initThreshold;
	private boolean notify;
	private Number modulus = null;
	private Number offset = null;
	private Boolean differenceMode = null;

	public CounterMonitorMBeanInfo(String id, ObjectName objectName,
			String mbeanServerId, long granularityPeriod,
			String observedAttribute, Number initThreshold, boolean notify) {
		super(id, objectName, mbeanServerId, CounterMonitorMBean.class,
				granularityPeriod, observedAttribute);

		this.initThreshold = initThreshold;
		this.notify = notify;
	}

	public Number getModulus() {
		return modulus;
	}

	public void setModulus(Number modulus) {
		this.modulus = modulus;
	}

	public Number getOffset() {
		return offset;
	}

	public void setOffset(Number offset) {
		this.offset = offset;
	}

	public Boolean getDifferenceMode() {
		return differenceMode;
	}

	public void setDifferenceMode(Boolean differenceMode) {
		this.differenceMode = differenceMode;
	}

	public Number getInitThreshold() {
		return initThreshold;
	}

	public boolean isNotify() {
		return notify;
	}
}
