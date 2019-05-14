package org.starmx.config;

public class BeanInfo extends ConfigurationItem {

	private String id;
	private Class<?> clazz;
	private String factoryMethod;

	public BeanInfo(String id, Class<?> clazz) {
		this.id = id;
		this.clazz = clazz;
	}

	public BeanInfo(String id, Class<?> clazz, String factoryMethod) {
		this(id, clazz);
		this.factoryMethod = factoryMethod;
	}

	public String getId() {
		return id;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public String getFactoryMethod() {
		return factoryMethod;
	}

}
