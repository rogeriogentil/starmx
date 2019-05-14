package org.starmx.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The AnchorObject annotation marks a field in the Process implementation class
 * as an anchor object. This annotation results in the automatic injection of the
 * anchor object instance to the annotated field.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AnchorObject {

	/**
	 * This attribute is used to map an anchor object, defined in <i>starmx.xml</i>,
	 * to the annotated field. When is defined, it must reference to
	 * the <i>id</i> attribute of an anchor object (MBean or Bean) in the configuration file.
	 * Otherwise, the object which its <i>name</i> attribute in the <i>object</i> 
	 * tag is equal to the name of the annotated field is injected.  
	 * This attribute has also priority over the mapping information defined 
	 * in the configuration file.  
	 */
	String refId() default "";
	
	enum InjectionPolicy {
		ALWAYS,
		FIRST_TIME_ONLY
	}
	/**
	 * Determines when to inject the corresponding anchor object.  
	 * In FIRST_TIME_ONLY mode, the anchor object is injected at the first 
	 * execution time. If it was not successful, it would be retried on the subsequent
	 * executions until injection is successful. In ALWAYS mode, the
	 * anchor object is looked up and injected every time. This mode is appropriate
	 * if a fresh copy of the anchor object is needed always.
	 */
	InjectionPolicy injectionPolicy() default InjectionPolicy.FIRST_TIME_ONLY;
	
	/**
	 * Specifies the behavior of the StarMX framework on handling LookupException
	 * at injection time. Use "true", if it is needed to ignore the exception and 
	 * set the field to null, and "false", if the exception must be thrown and stop 
	 * the process execution.  
	 */
	boolean nullOnLookupException() default true;
}
