package org.starmx.core;

import java.util.EventObject;

public class TimerEvent extends EventObject{

	private long period;
	private long sequenceNumber;
	private long nextExecutionTime;
	private long timeStamp;
	
	public TimerEvent(long period, long sequenceNumber, long nextExecutionTime){
		super("TimerTask");
		this.period = period;
		this.sequenceNumber = sequenceNumber;
		this.nextExecutionTime = nextExecutionTime;
		this.timeStamp = System.currentTimeMillis();
	}

	public long getPeriod() {
		return period;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public long getNextExecutionTime() {
		return nextExecutionTime;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
}
