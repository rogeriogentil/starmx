package org.starmx.core.impl;

public class StatisticalData {
	private long executionCount = 0;
	private long failureCount = 0;
	private long totalExecutionTime = 0;

	private long maxExecTime = 0;
	private long minExecTime = -1;

	public long getExecutionCount() {
		return executionCount;
	}

	public long getFailureCount() {
		return failureCount;
	}

	public long getTotalExecutionTime() {
		return totalExecutionTime;
	}

	public long getMaxExecTime() {
		return maxExecTime;
	}

	public long getMinExecTime() {
		return minExecTime;
	}

	public void incFailureCount() {
		failureCount++;
	}

	public void addExecutionTime(long t) {
		executionCount++;
		totalExecutionTime += t;

		if (t > maxExecTime)
			maxExecTime = t;
		if (t < minExecTime || minExecTime == -1)
			minExecTime = t;
	}

	public long getAverageExecutionTime() {
		if (executionCount == 0)
			return 0;

		return totalExecutionTime / executionCount;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("executionCount=");
		sb.append(executionCount);
		sb.append(", failureCount=");
		sb.append(failureCount);
		sb.append(", avgExecTime=");
		sb.append(getAverageExecutionTime());
		sb.append(", maxExecTime=");
		sb.append(maxExecTime);
		sb.append(", minExecTime=");
		sb.append(minExecTime);
		return sb.toString();
	}
}
