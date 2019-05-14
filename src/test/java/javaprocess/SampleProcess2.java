package javaprocess;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.starmx.core.AnchorObject;
import org.starmx.core.AnchorObject.InjectionPolicy;
import org.starmx.core.ExecutionContext;
import org.starmx.core.Process;
import org.starmx.core.ProcessConfig;

import util.MBean1MBean;
import util.SimpleBean;

public class SampleProcess2 implements Process {

	@AnchorObject(nullOnLookupException=false)
	private MBean1MBean mb1;
	
	@AnchorObject (injectionPolicy=InjectionPolicy.FIRST_TIME_ONLY)
	private SimpleBean sb;
	
	@AnchorObject
	private ThreadMXBean threadMXBean;
	
	public void destroy() {
	}

	public void execute(ExecutionContext context) {
		ThreadInfo ti = threadMXBean.getThreadInfo(Thread.currentThread().getId());
		mb1.print("==========> simple bean id = "+ sb.incId());
		mb1.print(" tn = "+ti.getThreadId());
	}

	public void init(ProcessConfig config) {
//		try {
//			mb1 = (MBean1MBean)config.getAnchorObject("mb1");
//		} catch (LookupException e) {
//			throw new RuntimeException(e);
//		}
		System.out.println("Process " + config.getProcessInfo().getId() + " initialized.");
	}

}
