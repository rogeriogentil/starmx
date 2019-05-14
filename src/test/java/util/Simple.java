package util;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class Simple implements SimpleMBean {
	private int intVal = 20;
	private Long longVal = new Long(100000);
	private String strVal = "str_val";
	private boolean valid = true;
	
	public void hello(){
		System.out.println("public void hello() called");
	}
	
	public void hello(int x, String s){
		System.out.println("public void hello(int x, String s): x="+x+", s="+s);
	}
	
	public byte hello(Integer n){
		
		System.out.append("public int hello(int n): n="+n);
		
		if (n == null)
			return 0;
		return n.byteValue();
	}

	public int getIntVal(){
		return intVal;
	}
	public Long getLongVal(){
		return longVal;
	}
	public String getStrVal(){
		return strVal;
	}
	
	public boolean isValid(){
		return valid;
	}

	public void setIntVal(int a){
		intVal = a;
	}
	
	public void setLongVal(Long l){
		longVal = l;
	}
	
	public void setStrVal(String s){
		strVal = s;
	}
	
	public void setValid(boolean b){
		valid = b;
	}

	public ObjectName getOtherBean(){
		try {
			return new ObjectName("domain:k1=v1,k2=v2");
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
		}
	}

}
