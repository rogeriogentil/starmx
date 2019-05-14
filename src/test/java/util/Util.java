package util;

import java.lang.management.ManagementFactory;
import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Util {

	public static MBeanServer getPlatformMBeanServer() {
		return ManagementFactory.getPlatformMBeanServer();
	}

	public static MBeanServer getLocalMBeanServer() {
		return MBeanServerFactory.createMBeanServer();
	}

	public static MBeanServer getMBeanServerByJNDI() {
		Hashtable<String, String> env = new Hashtable<String, String>();
		try {
			InitialContext ctx = new InitialContext(env);
			return (MBeanServer) ctx.lookup("MBEANSERVER_JNDI");
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

}
