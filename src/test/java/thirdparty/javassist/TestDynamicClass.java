package thirdparty.javassist;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.junit.Test;

public class TestDynamicClass {

	@Test
	public void test1(){
		Class<?> dynamicClass  = DynamicClassGenerator.createClass();

		try {
			Object obj = dynamicClass.newInstance();
			Method mHello = dynamicClass.getMethod("hello", String.class);
			Method mGetter = dynamicClass.getMethod("getX", (Class[])null);
			Method mSetter = dynamicClass.getMethod("setX", int.class);
			
			mHello.invoke(obj, "reza");
			System.out.println("x = "+mGetter.invoke(obj, (Object[])null));
			
			mSetter.invoke(obj, 5);
			System.out.println("x = "+mGetter.invoke(obj, (Object[])null));
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} 
	}

	@Test
	public void test2(){
		Class<?> dynamicClass  = DynamicClassGenerator.createClass2();

		try {
			Object obj = dynamicClass.newInstance();
			Method mHello = dynamicClass.getMethod("hello", String.class);
			Method mGetter = dynamicClass.getMethod("getName", (Class[])null);
			
			mHello.invoke(obj, "reza");
			mHello.invoke(obj, (Object)null);
			System.out.println("base class name = "+mGetter.invoke(obj, (Object[])null));
			
			BaseClass bc = (BaseClass)obj;
			System.out.println("bc.name = "+bc.getName());
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		
	}
	
}
