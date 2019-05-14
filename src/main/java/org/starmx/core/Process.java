package org.starmx.core;

/**
 * 
 * The super interface of all process classes and should be implemented by
 * Java-based processes. The process class is instantiated and initialized once
 * at startup. It will be executed for several times, and finally, it will be
 * destroyed at shut down stage.
 */
public interface Process {

	/**
	 * At the framework startup, this method is called to perform initialization
	 * tasks.
	 * 
	 * @param config
	 *            the ProcessConfig object that contains configuration
	 *            information about this process
	 */
	public void init(ProcessConfig config);

	/**
	 * The main method of the process object and is invoked at runtime. In this
	 * method, the process generally uses the anchor objects to check the
	 * system's state and to adjust the the system behaviour by calling the
	 * anchor objects methods.
	 * 
	 * @param context
	 *            the ExecutionContext object associated with the current
	 *            execution chain
	 */
	public void execute(ExecutionContext context);

	/**
	 * During the framework shutdown phase, this method is called to do the
	 * cleanup activities.
	 */
	public void destroy();
}
