package org.starmx.policy;

/**
 * 
 * An exception occurred in the policy adapter class maybe in communication with 
 * the underlying policy engine.
 */
public class PolicyException extends Exception {

	public PolicyException() {
		super();
	}

	public PolicyException(String message, Throwable cause) {
		super(message, cause);
	}

	public PolicyException(String message) {
		super(message);
	}

	public PolicyException(Throwable cause) {
		super(cause);
	}

}
