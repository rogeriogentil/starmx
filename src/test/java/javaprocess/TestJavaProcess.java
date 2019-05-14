package javaprocess;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.starmx.StarMXFramework;
import org.starmx.StarMXProperties;

import util.Util;

public class TestJavaProcess {

	private MBeanServer server = null;
	private ObjectName name11 = null;
	private ObjectName name12 = null;
	private ObjectName name13 = null;
	private ObjectName name2 = null;

	@Test
	public void useJavaProcess() {
		try {

			System.setProperty(StarMXProperties.CONFIG_PATH, "javaprocess");

//			registerMBeans();
			StarMXFramework starmx = StarMXFramework.createInstance();

			Thread.sleep(20000);

			starmx.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Before
	public void registerMBeans() throws Exception {

		server = Util.getPlatformMBeanServer();

		name11 = new ObjectName("starmx:name=mb1,instance=1");
		name12 = new ObjectName("starmx:name=mb1,instance=2");
		name13 = new ObjectName("starmx:name=mb1,instance=3");
		name2 = new ObjectName("starmx:name=mb2");

		server.createMBean("util.MBean1", name11);
		server.createMBean("util.MBean1", name12);
		server.createMBean("util.MBean1", name13);
		server.createMBean("util.MBean2", name2);

		// MBean1 mb11 = new MBean1();
		// MBean1 mb12 = new MBean1();
		// MBean2 mb2 = new MBean2();
		// server.registerMBean(mb11, name11);
		// server.registerMBean(mb12, name12);
		// server.registerMBean(mb2, name2);

		// System.out.println("SEQ = "+server.getAttribute(name11, "Seq"));
		// server.invoke(name11, "test", null, null);
	}
		
	@After
	public void unregMBeans() throws Exception {
		
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
	}

}
