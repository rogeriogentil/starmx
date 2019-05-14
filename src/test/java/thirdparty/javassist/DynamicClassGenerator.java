package thirdparty.javassist;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class DynamicClassGenerator {
	
	public static Class<?> createClass(){
		try {
			ClassPool cp = ClassPool.getDefault();
			CtClass cc = cp.makeClass("MyDynamicClass");
			CtMethod cm = CtNewMethod.make("public void hello(String name) {" +
					" System.out.println(\"Hello \"+name);}", cc);
			cc.addMethod(cm);
			
			CtField cf = CtField.make("private int x = 1;",cc);
			cc.addField(cf);
			
			CtMethod cmGetter = CtNewMethod.make("public int getX() {return x;}", cc);
			CtMethod cmSetter = CtNewMethod.make("public void setX(int x) { this.x = x;}", cc);

			cc.addMethod(cmGetter);
			cc.addMethod(cmSetter);
			
			System.out.println(cc.getClassFile().getSourceFile());
			
			return cc.toClass();
			
		} catch (CannotCompileException e) {
			
			return null;
		}
	}

	public static Class<?> createClass2(){
		try {
			ClassPool cp = ClassPool.getDefault();
			CtClass cc = cp.makeClass("MyDynamicClass2");
			CtClass csuper = cp.get("thirdparty.javassist.BaseClass");
			cc.setSuperclass(csuper);

			CtMethod cm = CtNewMethod.make("public void hello(String name) {" +
					" if (name==null) name=super.name; System.out.println(\"Hello \"+name);}", cc);
			cc.addMethod(cm);
			
			
			return cc.toClass();
			
		} catch (CannotCompileException e) {
			e.printStackTrace();
			return null;
		}catch (NotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
