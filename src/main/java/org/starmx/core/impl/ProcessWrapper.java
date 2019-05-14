package org.starmx.core.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.starmx.StarMXContext;
import org.starmx.config.Configuration;
import org.starmx.config.ConfigurationException;
import org.starmx.core.AnchorObject;
import org.starmx.core.ExecutionContext;
import org.starmx.core.Process;
import org.starmx.core.ProcessConfig;
import org.starmx.core.AnchorObject.InjectionPolicy;
import org.starmx.naming.LookupException;

public class ProcessWrapper implements Process {

	private static Logger logger = Logger.getLogger(ProcessWrapper.class);

	public enum ProcessState {
		NOT_EXIST, NOT_DEPLOYED, READY
	};

	private Process target;
	private ProcessConfig processConfig;
	private ProcessState state = ProcessState.NOT_DEPLOYED;
	private StatisticalData statData = new StatisticalData();
	private List<AnnotatedField> annotatedFields = new ArrayList<AnnotatedField>();

	public ProcessWrapper(Process target) {
		this.target = target;
	}

	public void init(ProcessConfig config) {
		processConfig = config;
		findAnchorObjectAnnotatedFields();

		target.init(processConfig);

		state = ProcessState.READY;
	}

	public void destroy() {
		target.destroy();
		state = ProcessState.NOT_EXIST;

		logger.info("Destroyed Process: "
				+ processConfig.getProcessInfo().getId() + ", "
				+ statData.toString());
	}

	public void execute(ExecutionContext context) {
		// if (state != ProcessState.READY)
		// return;

		long t1 = System.nanoTime();
		try {
			injectAnchorObjects();
			target.execute(context);
		} catch (RuntimeException e) {
			statData.incFailureCount();
			throw e;
		} finally {
			long execTime = System.nanoTime() - t1;
			statData.addExecutionTime(execTime);

			if (logger.isDebugEnabled()) {
				logger.debug("Executed process: "
						+ processConfig.getProcessInfo().getId()
						+ ", execCount="
						+ statData.getExecutionCount()
						+ ", avgExecTime="
						+ statData.getAverageExecutionTime()
						+ ", currentExecTime=" + execTime + " nanosec");
			}
		}
	}

	Process getTarget() {
		return target;
	}

	ProcessConfig getProcessConfig() {
		return processConfig;
	}

	public ProcessState getState() {
		return state;
	}

	private void findAnchorObjectAnnotatedFields() {
		Field[] f = target.getClass().getDeclaredFields();
		for (int i = 0; i < f.length; i++) {
			AnchorObject annotation = f[i].getAnnotation(AnchorObject.class);
			if (annotation != null) {
				f[i].setAccessible(true);

				AnnotatedField af = new AnnotatedField();
				af.field = f[i];
				af.injectAlways = (annotation.injectionPolicy() == InjectionPolicy.ALWAYS);
				af.setNullOnLookupException = annotation
						.nullOnLookupException();

				if (annotation.refId().length() > 0) {
					defineNewAnchorObject(f[i].getName(), annotation.refId());
				} else {
					if (processConfig.getProcessInfo().getObjectMapping(
						f[i].getName()) == null) {
						throw new RuntimeException(
								"Anchor object not found for field: "
										+ f[i].getName());
					}
				}

				annotatedFields.add(af);
			}
		}
	}

	private void injectAnchorObjects() {
		try {
			for (AnnotatedField af : annotatedFields) {
				if (af.injectionRequired) {
					try {
						af.field.set(target, processConfig
								.getAnchorObject(af.field.getName()));

						if (!af.injectAlways)
							af.injectionRequired = false;

					} catch (LookupException e) {
						if (af.setNullOnLookupException)
							af.field.set(target, null);
						else
							throw new RuntimeException(e);
					}
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void defineNewAnchorObject(String mappingName, String refId) {
		Configuration config = StarMXContext.getDefault().getConfiguration();
		try {
			config.addObjectMapping(processConfig.getProcessInfo().getId(),
				mappingName, refId);
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private class AnnotatedField {
		Field field;
		boolean injectAlways;
		boolean setNullOnLookupException;

		boolean injectionRequired = true;
	}
}
