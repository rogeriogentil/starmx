package org.starmx.core;

/**
 * This is a life cycle listener for the execution chain events.  
 */
public interface ExecutionChainListener {

	public void postCreate(ExecutionChainEvent event);
	public void preDestroy(ExecutionChainEvent event);
	public void preEnable(ExecutionChainEvent event);
	public void postEnable(ExecutionChainEvent event);
	public void preDisable(ExecutionChainEvent event);
	public void postDisable(ExecutionChainEvent event);
	public void onExecute(ExecutionContext context);
	
}
