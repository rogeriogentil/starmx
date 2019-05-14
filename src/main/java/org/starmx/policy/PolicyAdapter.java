package org.starmx.policy;

import org.starmx.core.ExecutionContext;
import org.starmx.core.ProcessConfig;

/**
 * The interface for policy adapter classes. The life cycle of a policy 
 * adapter is similar to that of a process. One instance of policy adapter
 * is created for each configured policy-based process in starmx.xml .
 * 
 */
public interface PolicyAdapter {

	/**
	 * This is used to introduce the policy text to the underlying policy engine 
	 * to performs syntax check and other required initializations for its execution,  
	 * and it is invoked once by the framework at startup. 
	 * The adapter can access the policy file content from the ProcessConfig 
	 * interface.
	 * 
	 * @param config the ProcessConfig object that contains configuration information about this process
	 * @throws PolicyException if an exception occurs in loading the policy. Throwing 
	 * this exception at load time will fail deployment of the execution chain that 
	 * the policy is a member of.
	 */
	public void loadPolicy(ProcessConfig config) throws PolicyException;

	/**
	 * This method is invoked to execute the policy. The corresponding execute 
	 * method of the engine should be invoked here giving it the anchor objects, 
	 * which are accessible from the ProcessConfig interface.
	 * @param context the ExecutionContext object associated with the current execution chain
	 * @throws PolicyException if an exception occurs in execution of the policy. 
	 * 
	 */
	public void executePolicy(ExecutionContext context) throws PolicyException;

	/**
	 * This is called to reload the policy. The policies are kept in separate files and
	 * they can be modified at runtime manually. This method will be invoked when the 
	 * user wants to apply the updated policy.
	 *   
	 * @throws PolicyException if an exception occurs in reloading the policy.
	 */
	public void reloadPolicy() throws PolicyException;

	/**
	 * This is called at the framework shutdown phase to perform cleanup tasks.
	 * @throws PolicyException if an exception occurs in unloading the policy.
	 */
	public void unloadPolicy() throws PolicyException;
}
