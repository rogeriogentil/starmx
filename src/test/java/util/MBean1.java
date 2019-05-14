package util;

import java.lang.reflect.Method;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ReflectionException;

public class MBean1 extends NotificationBroadcasterSupport implements
	//	DynamicMBean
  MBean1MBean
{
	private int seq = 0;
	private Object str = "testStr";
	private int val = 15;

	public Object getStr() {
		return str;
	}

	public void setStr(Object str) {
		this.str = str;
	}

	public Object getAttribute(String arg0) throws AttributeNotFoundException,
			MBeanException, ReflectionException {
		if (arg0.equals("Seq"))
			return seq;
		System.out.println("getAttribute:" + arg0);
		return null;
	}

	public AttributeList getAttributes(String[] arg0) {
		return null;
	}

	public MBeanInfo getMBeanInfo() {
		MBeanAttributeInfo[] attr = new MBeanAttributeInfo[1];
		attr[0] = new MBeanAttributeInfo("Seq", "int", "", true, false, false);

		MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[1];
		constructors[0] = new MBeanConstructorInfo("Constructs a "
				+ "SimpleDynamic object", this.getClass().getConstructors()[0]);

		MBeanOperationInfo[] op = new MBeanOperationInfo[3];
		try {
			Method m1 = getClass().getMethod("process");
			op[0] = new MBeanOperationInfo("", m1);

			Method m2 = getClass().getMethod("test");
			op[1] = new MBeanOperationInfo("", m2);
			
			Method m3 = getClass().getMethod("test2",int.class, String.class);
			op[2] = new MBeanOperationInfo("", m3);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		MBeanNotificationInfo[] notif = new MBeanNotificationInfo[1];
		notif[0] = new MBeanNotificationInfo(
				new String[] { "starmx.test.event1" },
				"javax.management.Notification", "notif desc");
		
		return new MBeanInfo(MBean1.class.getName(), "MBean1 desc", attr,
				constructors, op, notif);
	}

	public Object invoke(String arg0, Object[] arg1, String[] arg2)
			throws MBeanException, ReflectionException {
		if (arg0.equals("process"))
			process();
		else if (arg0.equals("test"))
			try {
				test();
			} catch (Exception e) {
				throw new MBeanException(e);
			}
		else if (arg0.equals("print"))
			print(arg1[0]);
		else
			System.out.println("invoke:" + arg0);

		return null;
	}

	public void setAttribute(Attribute arg0) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
	}

	public AttributeList setAttributes(AttributeList arg0) {
		return null;
	}

	public void process() {
		System.out.println("sending event...");

		Notification n = new Notification("starmx.test.event1", this, seq++);
		sendNotification(n);

		System.out.println("sent.");
//		throw new RuntimeException("test");
	}

	public void test() {
		System.out.println("MBean test method invoked. seq="+seq++);
//		if (seq == 2)
//			throw new RuntimeException("for testing");
	}

	public void print(Object s) {
		System.out.println("printing:  "+s);
	}

	public int getSeq() {
//		if (seq==0)
//			throw new RuntimeException("test");
		return seq;
	}

	public Byte getValue(){
		val +=2;
		System.out.println("==>>> MBean1.getValue: " + val);
		return (byte)val;
	}

	public long getLongVal(){
		return getValue().longValue();
	}

}
