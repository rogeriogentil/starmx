package framework;

import static org.junit.Assert.fail;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.starmx.StarMXFramework;

import util.Util;

public class FrameworkStartStop {

	private MBeanServer server = null;
	private ObjectName name11 = null;
	private ObjectName name12 = null;
	private ObjectName name2 = null;

	@Test
	public void startStop() {
		long milis = 10*1000;
		try {
//			registerMBeans();
			StarMXFramework starmx = StarMXFramework.createInstance();

			Thread.sleep(milis);

			starmx.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
			
			fail(e.getMessage());
		}
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
