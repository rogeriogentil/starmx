package org.starmx.jmx.proxy;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

class StarMXProxyClassBuilder {

	private static final ClassPool cp = ClassPool.getDefault();
	private static int counter = 0;

	private static Logger logger = Logger
			.getLogger(StarMXProxyClassBuilder.class);

	public static <T> Class<T> createProxyClass(Class<T> mbeanInterface) {
		try {
			String className = "StarMXProxy_";
			synchronized (StarMXProxyClassBuilder.class) {
				className += counter;
				counter++;
			}
			
			logger.debug("Creating proxy " + className + " for MBean interface: "
					+ mbeanInterface.getName());

			CtClass proxyClass = cp.makeClass(className);

			CtClass interfaceClass = cp.get(mbeanInterface.getName());
			proxyClass.addInterface(interfaceClass);

			defineField(proxyClass, mbeanInterface.getName(), "target");
			defineConstructor(proxyClass, mbeanInterface.getName());
			for (Method method : mbeanInterface.getMethods()) {
				defineMethod(proxyClass, method);
			}

			// proxyClass.writeFile("C:\\");
			return proxyClass.toClass(StarMXProxyClassBuilder.class
					.getClassLoader(), null);
		} catch (Exception e) {
			logger.error("Exception in creating proxy for MBean interface: "
					+ mbeanInterface.getName(), e);
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;

			throw new RuntimeException(e);
		}
	}

	private static void defineField(CtClass proxyClass, String fieldType,
			String fieldName) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("private ");
		sb.append(fieldType);
		sb.append(" ");
		sb.append(fieldName);
		sb.append(";\n");

		CtField cf = CtField.make(sb.toString(), proxyClass);
		proxyClass.addField(cf);
	}

	private static void defineConstructor(CtClass proxyClass, String intfName)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("public ");
		sb.append(proxyClass.getName());
		sb.append("(");
		sb.append(intfName);
		sb.append(" target){ this.target=target; }\n");

		logger.debug("Constructor: "+sb.toString());
		CtConstructor cc = CtNewConstructor.make(sb.toString(), proxyClass);
		proxyClass.addConstructor(cc);
	}

	private static void defineMethod(CtClass proxyClass, Method method)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("public ");

		sb.append(ClassBuilderUtil.decodeType(method.getReturnType().getName()));

		sb.append(" ");
		sb.append(method.getName());
		sb.append("(");

		int i = 0;
		for (Class<?> paramInfo : method.getParameterTypes()) {
			sb.append(ClassBuilderUtil.decodeType(paramInfo.getName()));
			sb.append(" p");
			sb.append(i);

			if (++i < method.getParameterTypes().length) {
				sb.append(", ");
			}
		}
		sb.append("){ ");
		if (!method.getReturnType().getName().equals("void")) {
			sb.append("return ($r)");
		}
		sb.append("target.");
		sb.append(method.getName());
		sb.append("($$); }\n");

		logger.debug("Method: "+sb.toString());

		CtMethod cm = CtNewMethod.make(sb.toString(), proxyClass);
		proxyClass.addMethod(cm);
	}

}
