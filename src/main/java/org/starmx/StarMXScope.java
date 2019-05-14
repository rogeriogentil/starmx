package org.starmx;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Similar to ServletContext scope in Java EE applications
 */
public class StarMXScope implements Scope {

	private Map<String, Object> scopeMap = new Hashtable<String, Object>();
	
	public Object getAttribute(String name) {
		return scopeMap.get(name);
	}

	public Iterator<String> getAttributeNames() {
		return scopeMap.keySet().iterator();
	}

	public void removeAttribute(String name) {
		scopeMap.remove(name);
	}

	public void setAttribute(String name, Object obj) {
		scopeMap.put(name, obj);
	}
}
