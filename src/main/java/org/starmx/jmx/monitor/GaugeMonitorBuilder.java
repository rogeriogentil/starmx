package org.starmx.jmx.monitor;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.monitor.GaugeMonitor;
import javax.management.monitor.GaugeMonitorMBean;
import javax.management.monitor.MonitorMBean;

import org.starmx.StarMXContext;
import org.starmx.config.GaugeMonitorMBeanInfo;
import org.starmx.config.MonitorMBeanInfo;

public class GaugeMonitorBuilder extends MonitorBuilder {

	public GaugeMonitorBuilder(MonitorMBeanInfo info) {
		super(info);
	}

	protected void createMonitorMBean(ObjectName objectName,
			String mbeanServerId) throws Exception {
		MBeanServerConnection connection = StarMXContext.getDefault()
				.getMBeanServerService().getMBeanServer(mbeanServerId);

		connection.createMBean(GaugeMonitor.class.getName(), objectName);
	}

	protected void setSpecificProperties(MonitorMBean mbean,
			MonitorMBeanInfo info, Class<?> observedAttrClass) throws Exception {
		if (observedAttrClass == null)
			throw new UnknownObservedAttributeTypeException();

		GaugeMonitorMBean gaugeMBean = (GaugeMonitorMBean) mbean;
		GaugeMonitorMBeanInfo gmInfo = (GaugeMonitorMBeanInfo) info;

		// observed attribute must be a sub-class of Number
		Class<? extends Number> observedAttrNumberClass = observedAttrClass
				.asSubclass(Number.class);

		Number hiThreshold = convertNumberClass(gmInfo.getHighThreshold(), observedAttrNumberClass);
		Number loThreshold = convertNumberClass(gmInfo.getLowThreshold(), observedAttrNumberClass);

		gaugeMBean.setThresholds(hiThreshold, loThreshold);

		if (gmInfo.getNotifyHigh() != null)
			gaugeMBean.setNotifyHigh(gmInfo.getNotifyHigh());

		if (gmInfo.getNotifyLow() != null)
			gaugeMBean.setNotifyLow(gmInfo.getNotifyLow());

		if (gmInfo.getDifferenceMode() != null)
			gaugeMBean.setDifferenceMode(gmInfo.getDifferenceMode());
	}
}
