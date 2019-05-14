package org.starmx.jmx.monitor;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.starmx.config.CounterMonitorMBeanInfo;
import org.starmx.config.GaugeMonitorMBeanInfo;
import org.starmx.config.MonitorMBeanInfo;

public class JMXMonitorEnabler {

	private static Logger logger = Logger.getLogger(JMXMonitorEnabler.class);
	private Map<String, MonitorBuilder> monitorBuilderMap = new HashMap<String, MonitorBuilder>();

	public void createMonitor(MonitorMBeanInfo info) {
		MonitorBuilder mb = createMonitorBuilder(info);

		mb.createMonitor();

		monitorBuilderMap.put(info.getId(), mb);
		logger.debug("Created MonitorMBean " + info.getId());
	}

	public void destroyMonitor(MonitorMBeanInfo info) {
		MonitorBuilder mb = monitorBuilderMap.get(info.getId());

		if (mb != null) {
			try {
				mb.destroyMonitor();
				logger.debug("Removed MonitorMBean " + info.getId());
			} catch (RuntimeException e) {
				// catch the exception to avoid failure during shutdown process
				logger.error("Failed to destroy MonitorMBean " + info.getId(),
					e);
			}
		}
	}

	private MonitorBuilder createMonitorBuilder(MonitorMBeanInfo info) {
		if (info instanceof CounterMonitorMBeanInfo)
			return new CounterMonitorBuilder(info);
		else if (info instanceof GaugeMonitorMBeanInfo)
			return new GaugeMonitorBuilder(info);

		return new StringMonitorBuilder(info);
	}
}
