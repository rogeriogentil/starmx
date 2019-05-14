package org.starmx.policy.adapter.imperius;

import java.util.List;
import java.util.Map;

import org.apache.imperius.javaspl.Java_SPLPolicyRuleProvider;
import org.apache.imperius.spl.parser.exceptions.PolicyDoesNotExistException;
import org.apache.imperius.spl.parser.exceptions.SPLException;
import org.apache.log4j.Logger;
import org.starmx.core.ExecutionContext;
import org.starmx.core.ProcessConfig;
import org.starmx.naming.LookupException;
import org.starmx.policy.PolicyAdapter;
import org.starmx.policy.PolicyException;

public class ImperiusPolicyAdapter implements PolicyAdapter {

	private boolean deployed = false;
	private static Java_SPLPolicyRuleProvider jspl;

	private static Logger logger = Logger
			.getLogger(ImperiusPolicyAdapter.class);

	static {
		try {
			jspl = Java_SPLPolicyRuleProvider.getInstance();
			for (String policyName : (List<String>) jspl.enumeratePolicyNames()) {
				jspl.deletePolicy(policyName);
			}
		} catch (SPLException e) {
			jspl = null;
			throw new RuntimeException(e);
		}
	}
	private ProcessConfig config;

	public void loadPolicy(ProcessConfig config) throws PolicyException {
		this.config = config;

		String splString = config.getPolicyFileContent();

		logger.debug(getPolicyName() + " SPL code: " + splString);

		try {
			jspl.createPolicy(getPolicyName(), splString);
		} catch (SPLException e) {
			e.printStackTrace();
			throw new PolicyException(e);
		}

		deployed = true;
	}

	public void unloadPolicy() throws PolicyException {
		if (!deployed)
			return;

		try {
			jspl.deletePolicy(getPolicyName());
		} catch (PolicyDoesNotExistException e) {
			// ignore it
		} catch (SPLException e) {
			throw new PolicyException(e);
		}
		deployed = false;
	}

	public void reloadPolicy() throws PolicyException {
		unloadPolicy();
		loadPolicy(config);
	}

	public void executePolicy(ExecutionContext context) throws PolicyException {

		Map<String, Object> objectMap = null;
		try {
			objectMap = config.getAllAnchorObjects();
			objectMap.put("context", context);
			objectMap.put("log", config.getLogger());
		} catch (LookupException e) {
			throw new RuntimeException(e);
		}

		long t1 = System.nanoTime();
		try {
			Object result = jspl.executePolicy(getPolicyName(), objectMap);
			if (result instanceof Integer) {
				if ((Integer) result == -1) {
					throw new PolicyException(
							"Policy evaluation failed in Imperius with an unknown reason!");
				}
			}
		} catch (SPLException e) {
			throw new PolicyException(e);
		} finally {
			if (logger.isDebugEnabled())
				logger.debug("ImperiusPolicy: " + getPolicyName()
						+ ", execTime=" + (System.nanoTime() - t1)
						+ " nanosec");
		}
	}

	private String getPolicyName() {
		return config.getProcessInfo().getId();
	}
}
