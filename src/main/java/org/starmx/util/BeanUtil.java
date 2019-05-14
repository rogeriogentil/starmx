package org.starmx.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BeanUtil {

	public static Object createInstance(Class<?> clazz,
			String factoryMethodName, String name) throws Exception {

		if (factoryMethodName == null) {
			return clazz.newInstance();
		}

		Method factoryMethod = null;
		boolean hasNameParam = false;
		try {
			factoryMethod = clazz.getMethod(factoryMethodName);
		} catch (NoSuchMethodException e) {
			factoryMethod = clazz.getMethod(factoryMethodName, String.class);
			hasNameParam = true;
		}
		Object factoryInstance = null;
		if (!Modifier.isStatic(factoryMethod.getModifiers())) {
			factoryInstance = clazz.newInstance();
		}

		if (hasNameParam)
			return factoryMethod.invoke(factoryInstance, name);
		else
			return factoryMethod.invoke(factoryInstance);
	}
}
