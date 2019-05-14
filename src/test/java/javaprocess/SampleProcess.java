package javaprocess;

import java.util.List;

import org.starmx.core.AnchorObject;
import org.starmx.core.ExecutionContext;
import org.starmx.core.Process;
import org.starmx.core.ProcessConfig;

import util.MBean1MBean;

public class SampleProcess implements Process {

	@AnchorObject
	private List<MBean1MBean> mb1List;
	
	public void destroy() {
	}

	public void execute(ExecutionContext context) {
		System.out.println("mb1List.size="+mb1List.size());
		for (MBean1MBean m : mb1List){
			m.getValue();
		}
	}

	public void init(ProcessConfig config) {
//		System.out.println("Process " + config.getProcessInfo().getId() + " initialized.");
		config.getLogger().info("Process " + config.getProcessInfo().getId() + " initialized.");
	}

}
