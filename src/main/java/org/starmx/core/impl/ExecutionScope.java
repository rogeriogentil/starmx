package org.starmx.core.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.starmx.Scope;

/**
 * Similar to HttpServletRequest scope in Java EE applications
 */
public class ExecutionScope implements Scope {

	private Map<String, Object> scopeMap = new HashMap<String, Object>();
	private int executionDepth = 0;
	
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

	public int getExecutionDepth() {
		return executionDepth;
	}
	
	public int incExecutionDepth(){
		return ++executionDepth;
	}

	public int decExecutionDepth(){
		return --executionDepth;
	}
	
	public void clear(){
		scopeMap.clear();
	}
}
