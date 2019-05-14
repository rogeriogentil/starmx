package org.starmx;

/**
 * This class contains StarMX property names, their default values, and
 * their valid values
 * 
 */
public class StarMXProperties {
	/**
	 * Property name: <code>starmx.config.path</code> <br>
	 * StarMX configuration files path. This directory should contain starmx.xml
	 * file as well as other policy files referenced in starmx.xml <br>
	 * There is no default value for this property. If not set the system looks
	 * for configuration files in classpath
	 */
	public static final String CONFIG_PATH = "starmx.config.path";

	/**
	 * Property name: <code>starmx.log.dir</code> <br>
	 * The output directory of starmx.log file. <br>
	 * Default: current directory
	 */
	public static final String LOG_DIR = "starmx.log.dir";

	/**
	 * Property name: <code>starmx.log.level</code> <br>
	 * The log level of starmx, which is based on log4j framework. <br>
	 * Values: Log4j log level strings like DEBUG, INFO, WARN, ERROR <br>
	 * Default: INFO
	 */
	public static final String LOG_LEVEL = "starmx.log.level";

	/**
	 * Property name: <code>starmx.policy.onError</code> <br>
	 * The error handling strategy for policies execution. This property
	 * specifies what to do if an exception occurred during policy execution. <br>
	 * Values: DISABLE, RETRY <br>
	 * Default: DISABLE
	 */
	public static final String POLICY_ERROR_HANDLING_STRATEGY = "starmx.policy.onError";

	/**
	 * Property name: <code>starmx.policy.onError.retryCount</code> <br>
	 * The number of retries in case of exception during policy execution. This
	 * property is used only if the error handling strategy is set to RETRY. <br>
	 * Values: 0 retries forever and never disables policy, n > 0 retries for n
	 * times, if the exception still happens then disables the policy
	 */
	public static final String POLICY_ERROR_RETRY_COUNT = "starmx.policy.onError.retryCount";

	/**
	 * Property name: <code>starmx.policy.adapter.<i>xyz</i></code> <br>
	 * The adapter class for a policy engine framework. This property is
	 * prefixed by a policy type to specify an adapter class for that particular
	 * policy type. Policy type is the type attribute of a policy tag. <br>
	 * Example: starmx.policy.adapter.xyz is used to name the adapter class for
	 * xyz policy type.
	 */
	public static final String POLICY_ADAPTER = "starmx.policy.adapter";

}
