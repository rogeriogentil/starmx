package org.starmx.naming;

public interface ObjectLocator {
	
	public Object lookup(String name) throws LookupException;
}
