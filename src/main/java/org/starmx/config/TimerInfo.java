package org.starmx.config;

import java.util.Date;

public class TimerInfo {
	
	public enum IntervalUnit {
		Second(1000), 
		Minute(60 * Second.milisec), 
		Hour(60 * Minute.milisec), 
		Day(24 * Hour.milisec), 
		Week(7 * Day.milisec), 
		Month(30 * Day.milisec);

		private long milisec;

		IntervalUnit(long milisec) {
			this.milisec = milisec;
		}

		long getMilisec() {
			return milisec;
		}
	};

	private long interval;
	private Date firstExecTime;
	private int firstExecDelay = 0;

	public TimerInfo(long interval, IntervalUnit unit) {
		this.interval = interval * unit.getMilisec();
	}

	public TimerInfo(long interval, IntervalUnit unit, Date firstExecTime) {
		this(interval, unit);
		this.firstExecTime = firstExecTime;
	}

	public TimerInfo(long interval, IntervalUnit unit, int firstExecDelay) {
		this(interval, unit);
		this.firstExecDelay = firstExecDelay;
	}
	
	public long getInterval() {
		return interval;
	}

	public Date getFirstExecTime() {
		return firstExecTime;
	}

	public int getFirstExecDelay() {
		return firstExecDelay;
	}

}
