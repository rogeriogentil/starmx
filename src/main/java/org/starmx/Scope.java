package org.starmx;

import java.util.Iterator;

/**
 * 
 * The super interface of all memory scope objects.
 */
public interface Scope {
	
	/**
	 * Returns an object by its name.
	 * @param name the name of the object to be returned
	 * @return the object to which the specified name is mapped, 
	 * or null if there is no mapping for the given name 
	 */
	public Object getAttribute(String name);
	
	/**
	 * 
	 * @return an iterator over the names of available objects in the scope
	 */
	public Iterator<String> getAttributeNames();
	
	/**
	 * Removes the objects associated with the given name.
	 * @param name the name of the object to be removed.
	 */
	public void removeAttribute(String name);
	
	/**
	 * Puts an object with a name in the scope
	 * @param name the name of the object
	 * @param obj the object to be associated with the name
	 */
	public void setAttribute(String name, Object obj);

}
