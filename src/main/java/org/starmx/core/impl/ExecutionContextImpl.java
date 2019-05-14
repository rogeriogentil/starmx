package org.starmx.core.impl;

import java.util.EventObject;

import javax.management.Notification;

import org.starmx.Scope;
import org.starmx.StarMXContext;
import org.starmx.core.ExecutionContext;
import org.starmx.core.TimerEvent;

public class ExecutionContextImpl implements ExecutionContext {

	private EventObject activationEvent;
	private ProcessScope processScope;
	private ExecutionScope executionScope;
	private boolean executeNextProcess = true;

	ExecutionContextImpl(EventObject activationEvent) {
		this.activationEvent = activationEvent;
	}

	public TimerEvent getTimerEvent() {
		if (activationEvent instanceof TimerEvent)
			return (TimerEvent)activationEvent;
		return null;
	}

	public Notification getNotification() {
		if (activationEvent instanceof Notification)
			return (Notification)activationEvent;
		return null;
	}

	public boolean calledByTimerEvent() {
		return getTimerEvent() != null;
	}

	public boolean calledByNotification() {
		return getNotification() != null;
	}

	public Scope getExecutionScope() {
		return executionScope;
	}

	public Scope getProcessScope() {
		return processScope;
	}

	public Scope getStarMXScope() {
		return StarMXContext.getDefault().getStarmxScope();
	}

	public void setProcessScope(ProcessScope processScope) {
		this.processScope = processScope;
	}

	public void setExecutionScope(ExecutionScope executionScope) {
		this.executionScope = executionScope;
	}

	public boolean getExecuteNext() {
		return executeNextProcess;
	}
	
	public void noExecuteNext() {
		executeNextProcess = false;
	}

	public void raiseEvent() {
		throw new RuntimeException("not impl");
	}
}
