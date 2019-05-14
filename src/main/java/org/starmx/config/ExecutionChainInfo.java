package org.starmx.config;

import java.util.ArrayList;
import java.util.List;

import org.starmx.core.ExecutionChainListener;

public class ExecutionChainInfo extends ConfigurationItem {

	private String name;
	private Class<ExecutionChainListener> listener;
	private List<ProcessInfo> processList = new ArrayList<ProcessInfo>();
	private TimerInfo timerInfo;
	private NotificationInfo notificationInfo;
	
	private String internalId;
	private static int counter = 0;

	private ExecutionChainInfo(String name,
			Class<ExecutionChainListener> listener){
		this.name = name;
		this.listener = listener;
		internalId = "ExecChain_" + ++counter;
	}
	
	public ExecutionChainInfo(String name,
			Class<ExecutionChainListener> listener, TimerInfo timerInfo) {
		this(name, listener);
		this.timerInfo = timerInfo;
	}

	public ExecutionChainInfo(String name,
			Class<ExecutionChainListener> listener,
			NotificationInfo notificationInfo) {
		this(name, listener);
		this.notificationInfo = notificationInfo;
	}

	public String getName() {
		return name;
	}

	public TimerInfo getTimerInfo() {
		return timerInfo;
	}

	public NotificationInfo getNotificationInfo() {
		return notificationInfo;
	}

	public List<ProcessInfo> getProcessInfoList() {
		return processList;
	}

	void addProcessInfo(ProcessInfo p) {
		processList.add(p);
	}

	public Class<ExecutionChainListener> getListener() {
		return listener;
	}

	public String getInternalId() {
		return internalId;
	}
}
