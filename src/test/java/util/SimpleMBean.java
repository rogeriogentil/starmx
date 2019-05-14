package util;

import javax.management.ObjectName;

public interface SimpleMBean {
	
	public void hello();
	public void hello(int x, String s);
	public byte hello(Integer n);
	
	public int getIntVal();
	public Long getLongVal();
	public String getStrVal();
	public boolean isValid();
	public ObjectName getOtherBean();
	
	public void setIntVal(int a);
	public void setLongVal(Long l);
	public void setStrVal(String s);
	public void setValid(boolean b);
	
}
