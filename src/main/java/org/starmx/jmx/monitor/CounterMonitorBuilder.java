package org.starmx.jmx.monitor;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.monitor.CounterMonitor;
import javax.management.monitor.CounterMonitorMBean;
import javax.management.monitor.MonitorMBean;

import org.starmx.StarMXContext;
import org.starmx.config.CounterMonitorMBeanInfo;
import org.starmx.config.MonitorMBeanInfo;

public class CounterMonitorBuilder extends MonitorBuilder {

	public CounterMonitorBuilder(MonitorMBeanInfo info) {
		super(info);
	}

	protected void createMonitorMBean(ObjectName objectName,
			String mbeanServerId) throws Exception {
		MBeanServerConnection connection = StarMXContext.getDefault()
				.getMBeanServerService().getMBeanServer(mbeanServerId);

		connection.createMBean(CounterMonitor.class.getName(), objectName);
	}

	protected void setSpecificProperties(MonitorMBean mbean,
			MonitorMBeanInfo info, Class<?> observedAttrClass) throws Exception {

		if (observedAttrClass == null)
			throw new UnknownObservedAttributeTypeException();

		CounterMonitorMBean counterMBean = (CounterMonitorMBean) mbean;
		CounterMonitorMBeanInfo cmInfo = (CounterMonitorMBeanInfo) info;

		// observed attribute must be a sub-class of Number
		Class<? extends Number> observedAttrNumberClass = observedAttrClass
				.asSubclass(Number.class);

		counterMBean.setInitThreshold(convertNumberClass(cmInfo
				.getInitThreshold(), observedAttrNumberClass));
		counterMBean.setNotify(cmInfo.isNotify());

		if (cmInfo.getModulus() != null)
			counterMBean.setModulus(convertNumberClass(cmInfo.getModulus(),
				observedAttrNumberClass));

		if (cmInfo.getOffset() != null)
			counterMBean.setOffset(convertNumberClass(cmInfo.getOffset(),
				observedAttrNumberClass));

		if (cmInfo.getDifferenceMode() != null)
			counterMBean.setDifferenceMode(cmInfo.getDifferenceMode());

	}
}
