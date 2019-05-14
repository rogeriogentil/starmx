package org.starmx;

public class StarMXException extends Exception {
	
	public StarMXException(String msg){
		super(msg);
	}
	
	public StarMXException(Throwable t){
		super(t);
	}

	public StarMXException(String msg, Throwable t){
		super(msg,t);
	}
}
