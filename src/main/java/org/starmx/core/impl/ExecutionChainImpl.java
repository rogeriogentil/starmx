package org.starmx.core.impl;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.apache.log4j.Logger;
import org.starmx.config.ExecutionChainInfo;
import org.starmx.core.Activator;
import org.starmx.core.ExecutionChain;
import org.starmx.core.ExecutionChainEvent;
import org.starmx.core.ExecutionChainListener;
import org.starmx.core.Process;

public class ExecutionChainImpl implements ExecutionChain {

	private static Logger logger = Logger.getLogger(ExecutionChainImpl.class);

	public enum ExecutionChainState {
		NOT_EXIST, NOT_DEPLOYED, READY, DISABLED
	};

	private ExecutionChainInfo chainInfo;
	private Activator activator;
	private List<Process> processList = new ArrayList<Process>();
	private ExecutionChainListener listener;
	private ExecutionChainEvent lifeCycleEvent;

	private StatisticalData statData = new StatisticalData();

	private ExecutionChainState state = ExecutionChainState.NOT_DEPLOYED;

	private static final ThreadLocal<ExecutionScope> execScopeTL = new ThreadLocal<ExecutionScope>() {
		@Override
		protected ExecutionScope initialValue() {
			return new ExecutionScope();
		}
	};

	public ExecutionChainImpl(ExecutionChainInfo chainInfo) {
		this.chainInfo = chainInfo;
		lifeCycleEvent = new ExecutionChainEvent(chainInfo);
	}

	public void init() {
		try {
			if (listener != null)
				listener.postCreate(lifeCycleEvent);
		} catch (RuntimeException e) {
			logger.warn("Failed to invoke postCreate", e);
		}
		// it means that the execution-chain has been created but it is not
		// ready yet
		state = ExecutionChainState.DISABLED;

		enable();
	}

	public void destroy() {
		disable();
		try {
			if (listener != null)
				listener.preDestroy(lifeCycleEvent);
		} catch (RuntimeException e) {
			logger.warn("Failed to invoke preDestroy", e);
		}

		state = ExecutionChainState.NOT_EXIST;
		logger.info("Destroyed chain: " + chainInfo.getInternalId() + ", "
				+ statData.toString());
	}

	public void enable() {
		if (state == ExecutionChainState.READY)
			return;

		try {
			if (listener != null)
				listener.preEnable(lifeCycleEvent);
		} catch (RuntimeException e) {
			logger.warn("Failed to invoke preEnable", e);
		}

		activator.start();
		state = ExecutionChainState.READY;

		try {
			if (listener != null)
				listener.postEnable(lifeCycleEvent);
		} catch (RuntimeException e) {
			logger.warn("Failed to invoke postEnable", e);
		}

		logger.info("Enabled chain: " + chainInfo.getInternalId());
	}

	public void disable() {
		if (state != ExecutionChainState.READY)
			return;

		try {
			if (listener != null)
				listener.preDisable(lifeCycleEvent);
		} catch (RuntimeException e) {
			logger.warn("Failed to invoke preDisable", e);
		}

		activator.stop();
		state = ExecutionChainState.DISABLED;

		try {
			if (listener != null)
				listener.postDisable(lifeCycleEvent);
		} catch (RuntimeException e) {
			logger.warn("Failed to invoke postDisable", e);
		}

		logger.info("Disabled chain: " + chainInfo.getInternalId());
	}

	public void execute(EventObject event) {

		long t1 = System.nanoTime();
		if (state != ExecutionChainState.READY) {
			logger.error("EecutionChain " + chainInfo.getInternalId()
					+ " is in <" + state + "> state and can not be executed.");
			return;
		}

		if (logger.isDebugEnabled())
			logger.debug("Executing chain: " + chainInfo.getInternalId()
					+ "...");

		ExecutionContextImpl context = new ExecutionContextImpl(event);

		ExecutionScope executionScope = execScopeTL.get();
		executionScope.incExecutionDepth();
		context.setExecutionScope(executionScope);
		boolean success = true;

		try {
			if (listener != null)
				listener.onExecute(context);

			for (Process p : processList) {
				ProcessWrapper procWrapper = (ProcessWrapper) p;
				context.setProcessScope((ProcessScope) procWrapper
						.getProcessConfig().getProcessScope());

				p.execute(context);
				if (!context.getExecuteNext())
					break;
			}
		} catch (RuntimeException e) {
			statData.incFailureCount();
			success = false;

			// exception in execution stops the execution of chain
			logger.error("Failure occurred during the execution of chain: "
					+ chainInfo.getInternalId()
					+ " due to the following exception ", e);

		} finally {
			if (executionScope.decExecutionDepth() <= 0)
				executionScope.clear();

			long execTime = System.nanoTime() - t1;
			statData.addExecutionTime(execTime);

			if (logger.isDebugEnabled())
				logger.debug((success ? "Executed successfully, "
						: "Execution failed, ")
						+ "chain="
						+ chainInfo.getInternalId()
						+ ", execCount="
						+ statData.getExecutionCount()
						+ ", avgExecTime="
						+ statData.getAverageExecutionTime()
						+ ", currentExecTime=" + execTime + " nanosec");
		}
	}

	public ExecutionChainInfo getChainInfo() {
		return chainInfo;
	}

	public Activator getActivator() {
		return activator;
	}

	public void setActivator(Activator activator) {
		this.activator = activator;
	}

	public ExecutionChainListener getListener() {
		return listener;
	}

	public void setListener(ExecutionChainListener listener) {
		this.listener = listener;
	}

	public List<Process> getProcessList() {
		return processList;
	}

	public void addProcess(Process p) {
		processList.add(p);
	}

	public ExecutionChainState getState() {
		return state;
	}

	public void setState(ExecutionChainState state) {
		this.state = state;
	}
}
