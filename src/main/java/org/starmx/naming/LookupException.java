package org.starmx.naming;

/**
 * 
 * The exception occurred at lookup time of the anchor objects. The reason might
 * be an object instantiation exception or mbean proxy generation exception.  
 */
public class LookupException extends Exception {

	public LookupException() {
		super();
	}

	public LookupException(String message, Throwable cause) {
		super(message, cause);
	}

	public LookupException(String message) {
		super(message);
	}

	public LookupException(Throwable cause) {
		super(cause);
	}

}
