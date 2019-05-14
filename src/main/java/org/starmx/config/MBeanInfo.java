package org.starmx.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

public class MBeanInfo extends ConfigurationItem implements PropertySet, Serializable {

	private String id;
	private ObjectName objectName;
	private Class<?> mbeanInterface;
	private String mbeanServerId;
	
	private Map<String, String> properties = new HashMap<String, String>();

	public MBeanInfo(String id, ObjectName objectName, String mbeanServerId, Class<?> mbeanInterface){
		this.id = id;
		this.objectName = objectName;
		this.mbeanServerId = mbeanServerId;
		this.mbeanInterface = mbeanInterface;
	}
	
	public String getId(){
		return id;
	}
	
	public String getProperty(String name){
		String propVal = properties.get(name);
		if (propVal == null)
			propVal = configuration.getProperty(name);
		return propVal;
	}
	
	public void setProperty(String name, String value){
		properties.put(name, value);
	}

	public ObjectName getObjectName() {
		return objectName;
	}

	public String getMbeanServerId() {
		return mbeanServerId;
	}

	public boolean isPattern(){
		return objectName.isPattern(); 
	}

	public Class<?> getMbeanInterface() {
		return mbeanInterface;
	}

	public void setMbeanInterface(Class<?> mbeanInterface) {
		this.mbeanInterface = mbeanInterface;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("id=");
		sb.append(id);
		sb.append(", mbeanserver=");
		sb.append(mbeanServerId);
		sb.append(", objectName=");
		sb.append(objectName.getCanonicalName());
		sb.append(", interface=");
		if (mbeanInterface==null){
			sb.append("null");
		}
		else{
			sb.append(mbeanInterface.getName());
		}
		
		return sb.toString();
	}

}
