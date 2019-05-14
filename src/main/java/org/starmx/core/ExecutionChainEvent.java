package org.starmx.core;

import java.util.EventObject;

import org.starmx.Scope;
import org.starmx.StarMXContext;
import org.starmx.config.ExecutionChainInfo;

public class ExecutionChainEvent extends EventObject {

	private ExecutionChainInfo chainInfo;
	
	public ExecutionChainEvent(ExecutionChainInfo chainInfo) {
		super(chainInfo);
		this.chainInfo = chainInfo;
	}

	/**
	 * @return the execution chain configuration information
	 */
	public ExecutionChainInfo getChainInfo() {
		return chainInfo;
	}
	
	/**
	 * @return the StarMXScope memory, which is shared among all processes.
	 */
	public Scope getStarMXScope() {
		return StarMXContext.getDefault().getStarmxScope();
	}
}
