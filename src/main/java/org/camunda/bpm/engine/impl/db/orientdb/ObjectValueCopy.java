package org.camunda.bpm.engine.impl.db.orientdb;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.logging.Logger;

/**
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class ObjectValueCopy  {
	private final static Logger LOG = Logger.getLogger(ObjectValueCopy.class.getName());

	public static void copyProperties(Object o,java.util.Map<String,Object> properties) {
		Class clazz = o.getClass();
		Method[] methods = clazz.getMethods();
		for (Method m : methods) {
			Class returnType = m.getReturnType();
			String getter = m.getName();
			String baseName = getBaseName(getter);
			String prefix = getGetterPrefix(getter);
			if (!Modifier.isStatic(m.getModifiers()) && prefix != null && isPrimitiveOrPrimitiveWrapperOrString(returnType)) {
				try{
					Method method = clazz.getMethod(getter);
					Object value = method.invoke(o);
					if (value != null ) {
						properties.put( baseName, value);
					}
				}catch(Exception e){
					LOG.info("Exception("+baseName+"):"+e);
				}
			}
		}
	}
	private static boolean isPrimitiveOrPrimitiveWrapperOrString(Class<?> type) {
		return (type.isPrimitive() && type != void.class) || type == Object.class || type == Double.class || type == Float.class || type == Long.class || type == Integer.class || type == Short.class || type == Character.class || type == Byte.class || type == Boolean.class || type == String.class || type == java.util.Date.class || type == byte[].class;
	}

	private static String[] getterPrefixes = new String[] { "is", "has", "get" };
	private static String getGetterPrefix(String mName) {
		for (String pre : getterPrefixes) {
			if (mName.startsWith(pre)) {
				return pre;
			}
		}
		return null;
	}
	private static String getBaseName(String methodName) {
		if (methodName.startsWith("get")) {
			return firstToLower(methodName.substring(3));
		}
		if (methodName.startsWith("has")) {
			return firstToLower(methodName.substring(3));
		}
		if (methodName.startsWith("is")) {
			return firstToLower(methodName.substring(2));
		}
		return firstToLower(methodName);
	}
	private static String firstToLower(String s) {
		char c[] = s.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}

}

