package scope;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.starmx.StarMXFramework;
import org.starmx.StarMXProperties;

import util.MBean1;
import util.Util;

public class TestMemoryScope {

	private MBeanServer server = null;
	private ObjectName name11 = null;
	private ObjectName name12 = null;
	private ObjectName name2 = null;

	@Test
	public void useExecutionScope() {
		try {
			
			System.setProperty(StarMXProperties.CONFIG_PATH, "scope");
			
//			registerMBeans();
			StarMXFramework starmx = StarMXFramework.createInstance();
//			registerMBeans();

			Thread.sleep(10000);
//			notificationTest();
			
			starmx.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	public static void notificationTest() throws Exception {
		Thread.sleep(10000);
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		MBeanServer server = Util.getPlatformMBeanServer();
		
		ObjectName name11 = new ObjectName("starmx:name=mb1,instance=1");
		ObjectName name12 = new ObjectName("starmx:name=mb1,instance=2");
		ObjectName name13 = new ObjectName("starmx:name=mb1,instance=3");
		server.unregisterMBean(name11);

		Thread.sleep(5000);
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		server.unregisterMBean(name12);

		MBean1 mb13 = new MBean1();
		server.registerMBean(mb13, name13);

		Thread.sleep(10000);
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		server.createMBean("util.MBean1", name11);

		Thread.sleep(5000);
		mb13.process();
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		
	}
	
	public static void test2() throws Exception{
		Thread.sleep(10000);
		
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		MBeanServer server = Util.getPlatformMBeanServer();
		
		ObjectName name2 = new ObjectName("starmx:name=mb2");
		server.unregisterMBean(name2);

		Thread.sleep(10000);
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		server.createMBean("util.MBean2", name2);

		Thread.sleep(10000);
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		
	}

	@Before
	public void registerMBeans() throws Exception {
		
		server = Util.getPlatformMBeanServer();
		
		name11 = new ObjectName("starmx:name=mb1,instance=1");
		name12 = new ObjectName("starmx:name=mb1,instance=2");
		name2 = new ObjectName("starmx:name=mb2");
		
		server.createMBean("util.MBean1", name11);
		server.createMBean("util.MBean1", name12);
		server.createMBean("util.MBean2", name2);	
	}
	
	@After
	public void unregMBeans() throws Exception {
		
		server.unregisterMBean(name11);
		server.unregisterMBean(name12);
		server.unregisterMBean(name2);
	}

}
