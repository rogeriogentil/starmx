package org.starmx.core;

import javax.management.Notification;

import org.starmx.Scope;

/**
 * This class provides miscellaneous services to the process at its execution
 * time, including access the memory scopes and the activation event object. The
 * memory scopes returned by this object are the ones returned from ProcessConfig.
 * <p>
 * This object can be passed to a policy as an anchor object by the related
 * policy adapter, allowing access the provided services within the policy code.
 * </p>
 */
public interface ExecutionContext {

	/**
	 * @return the TimerEvent object if the execution chain is activated by a
	 *         timer, otherwise null
	 */
	public TimerEvent getTimerEvent();

	/**
	 * 
	 * @return the JMX Notification object which activated the execution chain.
	 */
	public Notification getNotification();

	/**
	 * 
	 * @return True, if the execution chain is activated by timer; False,
	 *         otherwise
	 */
	public boolean calledByTimerEvent();

	/**
	 * 
	 * @return True, if the execution chain is activated by notification; False,
	 *         otherwise
	 */
	public boolean calledByNotification();

	/**
	 * 
	 * @return the ExecutionScope of the current execution chain
	 */
	public Scope getExecutionScope();

	/**
	 * 
	 * @return the ProcessScope memory of the current process, which is private to the process.
	 */
	public Scope getProcessScope();

	/**
	 * 
	 * @return the StarMXScope memory, which is shared among all processes.
	 */
	public Scope getStarMXScope();

	/**
	 * Returns if the next process in the chain will be called or not.
	 * @return True, if the next process will be executed; False, otherwise
	 * 
	 * @see #noExecuteNext() 
	 */
	public boolean getExecuteNext();

	/**
	 * Invocation of this method results in not executing the subsequent 
	 * processes in the current chain. If the process that calls this method is the 
	 * last process in the chain, it has no effect. The invocation of this method 
	 * will effect the current execution cycle only.
	 * 
	 * @see #getExecuteNext()
	 */
	public void noExecuteNext();

}
