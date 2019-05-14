package notification;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.starmx.StarMXFramework;
import org.starmx.StarMXProperties;

import util.MBean1;
import util.Util;

public class TestNotification {

	private StarMXFramework starmx = null;
	private MBeanServer server = null;
	private ObjectName name11 = null;
	private ObjectName name12 = null;
	private ObjectName name13 = null;
	private ObjectName name2 = null;
	
	@Before
	public void setup() throws Exception  {
		server = Util.getPlatformMBeanServer();

		name11 = new ObjectName("starmx:name=mb1,instance=1");
		name12 = new ObjectName("starmx:name=mb1,instance=2");
		name13 = new ObjectName("starmx:name=mb1,instance=3");
		name2 = new ObjectName("starmx:name=mb2");
		
		server.createMBean("util.MBean1", name11);
		server.createMBean("util.MBean1", name12);
		server.createMBean("util.MBean2", name2);
		
		System.setProperty(StarMXProperties.CONFIG_PATH, "notification");
		
		starmx = StarMXFramework.createInstance();
	}
	
	@After
	public void tearDown() throws Exception{
		
		try {
			server.unregisterMBean(name11);
		} catch (InstanceNotFoundException e) {
			//ignore
		}
		try {
			server.unregisterMBean(name12);
		} catch (InstanceNotFoundException e) {
			//ignore
		}
		try {
			server.unregisterMBean(name13);
		} catch (InstanceNotFoundException e) {
			//ignore
		}
		try {
			server.unregisterMBean(name2);
		} catch (InstanceNotFoundException e) {
			//ignore
		}

		if (starmx != null)
			starmx.shutdown();
	}
	
	@Test
	public void test1() throws Exception {
		Thread.sleep(10000);
		System.out.println("$$$$$$$$$$$$$ unregister MBean "+ name11.getCanonicalName());
		server.unregisterMBean(name11);

		Thread.sleep(5000);
		System.out.println("$$$$$$$$$$$$$ unregister MBean "+ name12.getCanonicalName());
		server.unregisterMBean(name12);

		MBean1 mb13 = new MBean1();
		System.out.println("$$$$$$$$$$$$$ register MBean "+ name13.getCanonicalName());
		server.registerMBean(mb13, name13);

		Thread.sleep(10000);
		System.out.println("$$$$$$$$$$$$$ recreate MBean "+ name11.getCanonicalName());
		server.createMBean("util.MBean1", name11);

		Thread.sleep(5000);
		mb13.process();
		System.out.println("$$$$$$$$$$$$$ DONE.");

	}

	@Test
	public void test2() throws Exception {
		Thread.sleep(10000);

		System.out.println("$$$$$$$$$$$ unregister MBean " + name2.getCanonicalName());
		server.unregisterMBean(name2);

		Thread.sleep(10000);
		System.out.println("$$$$$$$$$$$ recreate MBean "+ name2.getCanonicalName());
		server.createMBean("util.MBean2", name2);

		Thread.sleep(10000);
		System.out.println("$$$$$$$$$$$$$ DONE.");

	}
}
