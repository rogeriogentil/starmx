package org.starmx.core;

import java.util.EventObject;

import org.starmx.config.ExecutionChainInfo;

public interface ExecutionChain {

	public void init();
	public void destroy();
	public void enable();
	public void disable();
	public ExecutionChainInfo getChainInfo();
	public void execute(EventObject event);
	
}
