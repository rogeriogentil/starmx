package org.starmx.core.impl;

import java.util.concurrent.Executor;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.starmx.core.ExecutionChain;

public class MBeanEventListener implements NotificationListener {

	private String eventType;
	private String eventClassName;
	private ExecutionChain executionChain;
	private Executor executor;

	public MBeanEventListener(String eventType, String eventClassName,
			boolean synchExec, final ExecutionChain executionChain) {
		this.eventType = eventType;
		this.eventClassName = eventClassName;
		this.executionChain = executionChain;

		if (synchExec) {
			executor = new Executor() {
				public void execute(Runnable r) {
					r.run();
				}
			};
		} else {
			// asynchronous execution of the policy
			executor = new Executor() {
				private int seq = 0;
				private String threadName = executionChain.getChainInfo()
						.getName() + "_NotificationHandler_";

				public void execute(Runnable r) {
					new Thread(r, threadName + seq++).start();
				}
			};
		}
	}

	public void handleNotification(Notification notification, Object handback) {
		if ((eventType != null && !eventType.equals(notification.getType()))
				|| (eventClassName != null && !eventClassName
						.equals(notification.getClass().getName()))) {
			return;
		}

		executor.execute(new ProcessRunnable(notification));
	}

	class ProcessRunnable implements Runnable {
		private Notification notification;

		private ProcessRunnable(Notification notification) {
			this.notification = notification;
		}

		public void run() {
			executionChain.execute(notification);
		}
	}
}
