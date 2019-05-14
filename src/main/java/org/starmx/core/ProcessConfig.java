package org.starmx.core;

import java.util.Map;

import org.apache.log4j.Logger;
import org.starmx.Scope;
import org.starmx.config.ProcessInfo;
import org.starmx.naming.LookupException;

/**
 * This interface provides access to the process configuration information,
 * such as its anchor objects and memory scopes. It is passed to the process and 
 * policy adapter classes at initialization time.
 * 
 */
public interface ProcessConfig {

	/**
	 * @return process configuration information
	 */
	public ProcessInfo getProcessInfo();

	/**
	 * Looks up an anchor object by its name
	 * @param mappingName the name of the anchor object defined in starmx.xml within 
	 * the <i>object</i> tag.
	 * @return the anchor object with the given name
	 * @throws LookupException if there is no anchor object with the specified name, or
	 * an exception occurs at object instantiation or mbean proxy generation 
	 */
	public Object getAnchorObject(String mappingName) throws LookupException;

	/**
	 * Returns all requires set of anchor objects for the current process 
	 * @return map of the anchor objects and their names
	 * @throws LookupException if an exception occurs at object instantiation or mbean proxy generation
	 */
	public Map<String, Object> getAllAnchorObjects() throws LookupException;
	
	/**
	 * If the process is policy-based it returns the content of the policy file. If
	 * it contains <i><b>name</b>@StarMX</i> expressions, it replaces them with the 
	 * corresponding anchor objects class names. <br> This method is usually used by the 
	 * policy adapters to introduce the policy text to the policy engines.  
	 * 
	 * @return the content of policy file
	 */
	public String getPolicyFileContent();
	
	/**
	 * 
	 * @return ProcessScope memory of the current process, which is private to the process. 
	 */
	public Scope getProcessScope();

	/**
	 * 
	 * @return StarMXScope memory, which is shared among all processes.
	 */
	public Scope getStarMXScope();
	
	/**
	 * 
	 * @return associated logger for the current process
	 */
	public Logger getLogger();
	
}