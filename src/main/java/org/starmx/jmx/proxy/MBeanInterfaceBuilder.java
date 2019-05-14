package org.starmx.jmx.proxy;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.apache.log4j.Logger;

class MBeanInterfaceBuilder {

	private static final ClassPool cp = ClassPool.getDefault();
	private static int counter = 0;

	private static Logger logger = Logger
			.getLogger(MBeanInterfaceBuilder.class);

	public static Class<?> createMBeanInterface(MBeanInfo mbeanInfo) {

		try {
			String interfaceName = "MBeanInterface_";
			synchronized (MBeanInterfaceBuilder.class) {
				interfaceName += counter;
				counter++;
			}

			logger.debug("Creating interface " + interfaceName + " for MBean: "
					+ mbeanInfo.getClassName());
			CtClass mbeanInterface = cp.makeInterface(interfaceName);

			for (MBeanAttributeInfo attributeInfo : mbeanInfo.getAttributes()) {
				defineAttribute(mbeanInterface, attributeInfo);
			}

			for (MBeanOperationInfo operationInfo : mbeanInfo.getOperations()) {
				defineOperation(mbeanInterface, operationInfo);
			}

			// mbeanInterface.writeFile("C:\\");
			return mbeanInterface.toClass(MBeanInterfaceBuilder.class
					.getClassLoader(), null);
		} catch (Exception e) {
			logger.error("Exception in creating interface for MBean: "
					+ mbeanInfo.getClassName(), e);
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;

			throw new RuntimeException(e);
		}
	}

	private static void defineAttribute(CtClass mbeanInterface,
			MBeanAttributeInfo attributeInfo) throws Exception {

		if (attributeInfo.isReadable()) {
			defineGetter(mbeanInterface, attributeInfo);
		}

		if (attributeInfo.isWritable()) {
			defineSetter(mbeanInterface, attributeInfo);
		}
	}

	private static void defineGetter(CtClass mbeanInterface,
			MBeanAttributeInfo attributeInfo) throws Exception {
		// TODO add support for returning MBean instead of ObjectName
		StringBuilder sb = new StringBuilder();
		sb.append("public ");
		sb.append(ClassBuilderUtil.decodeType(attributeInfo.getType()));
		sb.append(" ");
		if (attributeInfo.isIs())
			sb.append("is");
		else
			sb.append("get");
		sb.append(attributeInfo.getName());
		sb.append("();\n");

		addMethod(mbeanInterface, sb.toString());
	}

	private static void defineSetter(CtClass mbeanInterface,
			MBeanAttributeInfo attributeInfo) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("public void set");
		sb.append(attributeInfo.getName());
		sb.append("(");
		sb.append(ClassBuilderUtil.decodeType(attributeInfo.getType()));
		sb.append(" p1);\n");

		addMethod(mbeanInterface, sb.toString());
	}

	private static void defineOperation(CtClass mbeanInterface,
			MBeanOperationInfo operationInfo) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("public ");
		sb.append(ClassBuilderUtil.decodeType(operationInfo.getReturnType()));
		sb.append(" ");
		sb.append(operationInfo.getName());
		sb.append("(");

		int i = 0;
		for (MBeanParameterInfo paramInfo : operationInfo.getSignature()) {
			sb.append(ClassBuilderUtil.decodeType(paramInfo.getType()));
			sb.append(" p");
			sb.append(i);

			if (++i < operationInfo.getSignature().length) {
				sb.append(", ");
			}
		}
		sb.append(");\n");

		addMethod(mbeanInterface, sb.toString());
	}

	private static void addMethod(CtClass mbeanInterface, String methodSource)
			throws Exception {
		logger.debug("Method: "+methodSource);

		CtMethod cm = CtNewMethod.make(methodSource, mbeanInterface);
		mbeanInterface.addMethod(cm);
	}

}
