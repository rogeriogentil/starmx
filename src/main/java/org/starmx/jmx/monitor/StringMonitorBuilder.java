package org.starmx.jmx.monitor;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.monitor.MonitorMBean;
import javax.management.monitor.StringMonitor;
import javax.management.monitor.StringMonitorMBean;

import org.starmx.StarMXContext;
import org.starmx.config.MonitorMBeanInfo;
import org.starmx.config.StringMonitorMBeanInfo;

public class StringMonitorBuilder extends MonitorBuilder {

	public StringMonitorBuilder(MonitorMBeanInfo info){
		super(info);
	}

	protected void createMonitorMBean(ObjectName objectName,
			String mbeanServerId) throws Exception {
		MBeanServerConnection connection = StarMXContext.getDefault()
				.getMBeanServerService().getMBeanServer(mbeanServerId);

		connection.createMBean(StringMonitor.class.getName(), objectName);
	}

	protected void setSpecificProperties(MonitorMBean mbean,
			MonitorMBeanInfo info, Class<?> observedAttrClass) throws Exception {
		StringMonitorMBean stringMBean = (StringMonitorMBean) mbean;
		StringMonitorMBeanInfo smInfo = (StringMonitorMBeanInfo) info;

		stringMBean.setStringToCompare(smInfo.getStringToCompare());
		
		if (smInfo.getNotifyDiffer() != null)
			stringMBean.setNotifyDiffer(smInfo.getNotifyDiffer());

		if (smInfo.getNotifyMatch() != null)
			stringMBean.setNotifyMatch(smInfo.getNotifyMatch());
	}
}
