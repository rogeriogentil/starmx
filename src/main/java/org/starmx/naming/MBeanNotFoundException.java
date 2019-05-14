package org.starmx.naming;

import javax.management.ObjectName;

import org.starmx.config.MBeanInfo;

public class MBeanNotFoundException extends LookupException {
	private MBeanInfo mbeanInfo;
	
	public MBeanNotFoundException(String msg){
		super(msg);
	}
	
	public MBeanNotFoundException(Throwable t){
		super(t);
	}
	
	public MBeanNotFoundException(MBeanInfo mbean){
		super("MBean "+mbean.getObjectName().getCanonicalName()+" not found");
		this.mbeanInfo = mbean;
	}

	public MBeanNotFoundException(MBeanInfo mbean, Throwable t){
		super(t);
		this.mbeanInfo = mbean;
	}
	
	public ObjectName getObjectName(){
		return mbeanInfo.getObjectName();
	}
	
	public MBeanInfo getMBeanInfo(){
		return mbeanInfo;
	}
}
