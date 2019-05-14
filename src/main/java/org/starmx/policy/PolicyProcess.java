package org.starmx.policy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.starmx.StarMXContext;
import org.starmx.StarMXProperties;
import org.starmx.config.Configuration;
import org.starmx.config.ProcessInfo;
import org.starmx.core.ExecutionContext;
import org.starmx.core.Process;
import org.starmx.core.ProcessConfig;

public class PolicyProcess implements Process {

	private static Logger logger = Logger.getLogger(PolicyProcess.class);
	private static Map<String, String> adaptersMap;

	static {
		adaptersMap = new HashMap<String, String>();
		adaptersMap.put("spl", "org.starmx.policy.adapter.imperius.ImperiusPolicyAdapter");
		adaptersMap.put("arl", "org.starmx.policy.adapter.able.AblePolicyAdapter");
	}

	private PolicyAdapter policyAdapter = null;
	private ProcessConfig config;

	public void init(ProcessConfig config) {
		try {
			this.config = config;
			policyAdapter = createPolicyAdapter(config);
			policyAdapter.loadPolicy(config);
		} catch (PolicyException e) {
			throw new RuntimeException(e);
		}
	}

	public void execute(ExecutionContext context) {
		try {
			policyAdapter.executePolicy(context);
		} catch (PolicyException e) {
			throw new RuntimeException(e);
		}
	}

	public void destroy() {
		try {
			Iterator<String> it = config.getProcessScope().getAttributeNames();
			while (it.hasNext()) {
				String s = it.next();
				if (logger.isDebugEnabled()) {
					logger.debug("ProcessScope " + config.getProcessInfo().getId()
							+ " contains: " + s + " = "
							+ config.getProcessScope().getAttribute(s));
				}
			}
			policyAdapter.unloadPolicy();
		} catch (PolicyException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates and initializes proper adapter based on the policy type
	 * 
	 * @param policyConfig
	 * @return The created PolicyAdapter instance
	 * @throws PolicyException
	 */
	private static PolicyAdapter createPolicyAdapter(ProcessConfig policyConfig) {

		PolicyAdapter adapter = null;
		ProcessInfo policyInfo = policyConfig.getProcessInfo();
		String adapterClassName = getAdapterClassName(policyInfo
				.getPolicyType());

		if (adapterClassName != null) {
			try {
				Class<?> adapterClass = Class.forName(adapterClassName);
				adapter = (PolicyAdapter) adapterClass.newInstance();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (ClassCastException e) {
				throw new RuntimeException("Policy adapter <"
						+ adapterClassName + "> must be instance of "
						+ PolicyAdapter.class.getName(), e);
			}
		} else {
			throw new RuntimeException(
					"No policy adapter found for policy type="
							+ policyInfo.getPolicyType());
		}

		return adapter;
	}

	private static String getAdapterClassName(String policyType) {
		Configuration config = StarMXContext.getDefault().getConfiguration();
		String adapterClassName = config
				.getProperty(StarMXProperties.POLICY_ADAPTER + "." + policyType);
		if (adapterClassName == null) {
			adapterClassName = adaptersMap.get(policyType);
		}
		return adapterClassName;
	}

}
