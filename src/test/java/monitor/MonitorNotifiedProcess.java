package monitor;

import javax.management.monitor.MonitorNotification;

import org.starmx.core.ExecutionContext;
import org.starmx.core.Process;
import org.starmx.core.ProcessConfig;

public class MonitorNotifiedProcess implements Process {

	public void destroy() {
	}

	public void execute(ExecutionContext context) {
		MonitorNotification mn = (MonitorNotification)context.getNotification();
		System.out.println("Process called - notification trigger="+mn.getTrigger()+", attr-value="+mn.getDerivedGauge());
	}

	public void init(ProcessConfig config) {
	}

}
