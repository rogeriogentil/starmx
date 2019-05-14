package org.starmx.core.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.starmx.Scope;

/**
 * Similar to HttpSession scope in Java EE applications
 */
public class ProcessScope implements Scope {

	private Map<String, Object> scopeMap = new HashMap<String, Object>();
	
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
