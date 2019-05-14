package org.starmx.config;

public class ObjectMapping extends ConfigurationItem {

	public enum Type {
		MBean, Bean, SpringBean, WebService
	};

	private String name;
	private Type refType;
	private String ref;

	public ObjectMapping(String name, Type refType, String ref) {
		this.name = name;
		this.refType = refType;
		this.ref = ref;
	}

	public String getName() {
		return name;
	}

	public Type getRefType() {
		return refType;
	}

	public String getRef() {
		return ref;
	}

	public Object getRefObjectInfo() {
		switch (refType) {
		case MBean:
			return configuration.getMBeanInfo(ref);
		case Bean:
			return configuration.getBeanInfo(ref);
		default:
			throw new RuntimeException("NOT SUPPORTED !");
		}
	}
}
