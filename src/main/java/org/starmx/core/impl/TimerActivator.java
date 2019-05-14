package org.starmx.core.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.starmx.config.ExecutionChainInfo;
import org.starmx.config.TimerInfo;
import org.starmx.core.Activator;
import org.starmx.core.ExecutionChain;
import org.starmx.core.TimerEvent;

public class TimerActivator implements Activator {

	private static Logger logger = Logger.getLogger(TimerActivator.class);
	private static final long DEFAULT_STARTUP_DELAY = 1000;

	private ExecutionChain executionChain;
	private ExecutionChainInfo chainInfo;
	private long cpuTime;
	private Timer timer;

	public TimerActivator(ExecutionChain executionChain) {
		this.executionChain = executionChain;
		this.chainInfo = executionChain.getChainInfo();
	}

	public void start() {
		timer = new Timer(chainInfo.getInternalId() + "_Timer");
		// timer task is not a reusable object and it must be created every time
		TimerTask chainExecTask = new TimerTask() {
			private long sequenceNumber = 1;
			private ThreadMXBean threadMXBean = ManagementFactory
					.getThreadMXBean();

			public void run() {
				TimerEvent timerEvent = new TimerEvent(chainInfo.getTimerInfo()
						.getInterval(), sequenceNumber++,
						scheduledExecutionTime());
				executionChain.execute(timerEvent);
				cpuTime = threadMXBean.getCurrentThreadUserTime() / 1000000;
			}
		};

		TimerInfo timerInfo = chainInfo.getTimerInfo();
		Date firstExecTime = null;
		if (timerInfo.getFirstExecTime() != null) {
			Calendar current = Calendar.getInstance();
			Calendar timerCl = Calendar.getInstance();
			timerCl.setTime(timerInfo.getFirstExecTime());
			current
					.set(Calendar.HOUR_OF_DAY, timerCl
							.get(Calendar.HOUR_OF_DAY));
			current.set(Calendar.MINUTE, timerCl.get(Calendar.MINUTE));
			current.set(Calendar.SECOND, 0);
			current.set(Calendar.MILLISECOND, 0);

			firstExecTime = current.getTime();
			timer.schedule(chainExecTask, firstExecTime, timerInfo
					.getInterval());
		} else {
			long delay = DEFAULT_STARTUP_DELAY;
			if (timerInfo.getFirstExecDelay() > 0) {
				delay = timerInfo.getFirstExecDelay() * 1000;
			}
			firstExecTime = new Date(System.currentTimeMillis() + delay);
			timer.schedule(chainExecTask, delay, timerInfo.getInterval());
		}

		logger.debug("Scheduled timer for execution chain: "
				+ chainInfo.getInternalId() + ", interval="
				+ timerInfo.getInterval() + " millis, firstExecTime="
				+ firstExecTime);
	}

	public void stop() {
		timer.cancel();
		timer = null;

		logger.info("ThreadID=" + chainInfo.getInternalId()
				+ "_Timer, totalCpuTime=" + cpuTime);

		logger.debug("Cancelled timer of chain: " + chainInfo.getInternalId());
	}
}
