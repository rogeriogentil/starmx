package util;

import java.lang.reflect.Method;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

public class MBean2 implements DynamicMBean{

	private int counter = 0;
	public Object getAttribute(String arg0) throws AttributeNotFoundException,
			MBeanException, ReflectionException {
		return null;
	}

	public AttributeList getAttributes(String[] arg0) {
		return null;
	}

	public MBeanInfo getMBeanInfo() {
		MBeanOperationInfo[] op = new MBeanOperationInfo[1];
		try {
			Method m = getClass().getMethod("hello", String.class);
			op[0] = new MBeanOperationInfo("",m);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return new MBeanInfo(MBean2.class.getName(),"MBean2 desc",null,null,op,null);
	}

	public Object invoke(String arg0, Object[] arg1, String[] arg2)
			throws MBeanException, ReflectionException {
		if (arg0.equals("hello"))
			hello((String)arg1[0]);
		
		return null;
	}

	public void setAttribute(Attribute arg0) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
	}

	public AttributeList setAttributes(AttributeList arg0) {
		return null;
	}

	public void hello(String s) {
		System.out.println("["+Thread.currentThread().getName()+"] hello "+s+"  "+counter++);
//		if (counter == 2 || counter == 3)
//			throw new RuntimeException("Test message");
	}
}
