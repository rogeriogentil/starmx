package org.starmx.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.starmx.config.ExecutionChainInfo;
import org.starmx.config.ProcessInfo;
import org.starmx.core.impl.ExecutionChainImpl;
import org.starmx.core.impl.NotificationActivator;
import org.starmx.core.impl.ProcessConfigImpl;
import org.starmx.core.impl.ProcessScope;
import org.starmx.core.impl.ProcessWrapper;
import org.starmx.core.impl.TimerActivator;
import org.starmx.policy.PolicyProcess;

public class ExecutionChainDeployer {
	private static Logger logger = Logger.getLogger(ExecutionChainDeployer.class);

	private Map<String, Process> processMap = new HashMap<String, Process>();
	private Map<String, ExecutionChain> execChainMap = new HashMap<String, ExecutionChain>();

	public ExecutionChain deployExecutionChain(ExecutionChainInfo chainInfo) {
		if (execChainMap.containsKey(chainInfo.getInternalId()))
			return execChainMap.get(chainInfo.getInternalId());

		ExecutionChainImpl execChain = new ExecutionChainImpl(chainInfo);
		try {
			execChain.setActivator(createActivator(execChain));
			execChain.setListener(createListener(chainInfo));

			for (ProcessInfo pInfo : chainInfo.getProcessInfoList()) {
				Process proc = deployProcess(pInfo);
				execChain.addProcess(proc);
			}
			execChain.init();
		} catch (RuntimeException e) {
			// exception in deployment results in not deploying the execution chain 
			logger.error("Failed to deploy ExecutionChain: "+chainInfo.getInternalId(), e);
			return null;
		}

		execChainMap.put(chainInfo.getInternalId(), execChain);
		
		logger.info("Deployed ExecutionChain: "+chainInfo.getInternalId());
		return execChain;
	}

	public void undeployExecutionChain(ExecutionChainInfo chainInfo) {
		ExecutionChain execChain = execChainMap.get(chainInfo.getInternalId());
		if (execChain != null) {
			execChain.destroy();

			for (ProcessInfo pInfo : chainInfo.getProcessInfoList()) {
				if (pInfo.isLocal())
					undeployProcess(pInfo);
			}

			execChainMap.remove(chainInfo.getInternalId());

			logger.info("Undeployed ExecutionChain: "+chainInfo.getInternalId());
		}
	}

	public Process deployProcess(ProcessInfo pInfo) {
		if (processMap.containsKey(pInfo.getId()))
			return processMap.get(pInfo.getId());

		Process proc = null;
		ProcessScope processScope = new ProcessScope();
		if (pInfo.getJavaClass() != null) {
			try {
				proc = pInfo.getJavaClass().newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else {
			proc = new PolicyProcess();
		}
		
		Process procWrapper = new ProcessWrapper(proc);
		procWrapper.init(new ProcessConfigImpl(pInfo, processScope));

		processMap.put(pInfo.getId(), procWrapper);
		
		logger.debug("Deployed process: "+pInfo.getId());
		return procWrapper;
	}

	public void undeployProcess(ProcessInfo pInfo) {
		Process proc = processMap.get(pInfo.getId());
		if (proc != null) {
			try {
				proc.destroy();
				logger.debug("Undeployed process: "+pInfo.getId());
			} catch (RuntimeException e) {
				// exception in process undeployment is ignored
				logger.error("Failed to undeploy process: "+ pInfo.getId(), e);
			}
			processMap.remove(pInfo.getId());
		}
	}

	private Activator createActivator(ExecutionChain execChain) {
		if (execChain.getChainInfo().getTimerInfo() != null)
			return new TimerActivator(execChain);
		if (execChain.getChainInfo().getNotificationInfo() != null)
			return new NotificationActivator(execChain);

		return null;
	}

	private ExecutionChainListener createListener(ExecutionChainInfo chainInfo) {
		try {
			if (chainInfo.getListener() != null)
				return chainInfo.getListener().newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	public void enableAll(){
		for (ExecutionChain chain : execChainMap.values()){
			chain.enable();
		}
	}
	
	public void disableAll(){
		for (ExecutionChain chain : execChainMap.values()){
			chain.disable();
		}
	}
}
