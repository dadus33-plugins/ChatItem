package me.dadus33.chatitem.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import me.dadus33.chatitem.ChatItem;

public class ReflectionUtils {

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            return clazz.getMethod(methodName, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Method getMethod(Class<?> clazz, Class<?> returned, Class<?>... params) {
        try {
        	for(Method m : clazz.getDeclaredMethods()) {
        		if(returned.equals(m.getReturnType()) && areParamsEquals(m.getParameterTypes(), params))
        			return m;
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static boolean areParamsEquals(Class<?>[] p1, Class<?>[] p2) {
		if(p1.length != p2.length)
			return false;
		for(int i = 0; i < p1.length; i++) {
			if(!p1[i].equals(p2[i]))
				return false;
		}
		return true;
    }

    public static Field getField(Class<?> clazz, String... fieldNames){
    	for(String field : fieldNames) {
	        try{
	            Field f = clazz.getDeclaredField(field);
	            f.setAccessible(true);
	            return f;
	        } catch (NoSuchFieldException e) {
	        	// go to next
	        }
    	}
    	ChatItem.getInstance().getLogger().severe("Failed to find field: " + Arrays.asList(fieldNames) + " in class: " + clazz.getCanonicalName());
        return null;
    }
	
	public static Object getPrivateField(Object object, String field)
			throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field objectField = object.getClass().getDeclaredField(field);
		objectField.setAccessible(true);
		return objectField.get(object);
	}
	
	public static void setField(Object src, String fieldName, Object value) {
		try {
			Field field = src.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(src, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the specified field name in the object source
	 * 
	 * @param source where we will find the field
	 * @param field the name of the field
	 * @return the requested object of the field
	 */
	public static Object getObject(Object source, String... field) {
		try {
			for(String fieldName : field) {
				try {
					Field f = source.getClass().getDeclaredField(fieldName);
					f.setAccessible(true);
					return f.get(source);
				} catch (NoSuchFieldException e) {
					// ignore because going to next item
				}
			}
			ChatItem.getInstance().getLogger().severe("Failed to find fields: " + String.join(", ", field) + " in " + source.getClass().getSimpleName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get the specified field name in the object source
	 * 
	 * @param source where we will find the field
	 * @param field the name of the field
	 * @return the requested field
	 */
	public static Field getField(Object source, String field) {
		try {
			Field f = source.getClass().getDeclaredField(field);
			f.setAccessible(true);
			return f;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Call method. Return the method return.
	 * 
	 * @param source the object where we want to run the method
	 * @param method the name of the method to call
	 * @return the return of the method called
	 */
	public static Object callMethod(Object source, String method) {
		try {
			return source.getClass().getDeclaredMethod(method).invoke(source);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the first object which have the searching for class type
	 * 
	 * @param from the object where we will try to find the field
	 * @param clazz the class that have to define the field
	 * @param searchingFor the class of the required field
	 * @return the field (or null if not found)
	 * @throws Exception if something gone wrong
	 */
	public static <T> T getFirstWith(Object from, Class<?> clazz, Class<T> searchingFor) throws Exception {
		for (Field f : clazz.getDeclaredFields()) {
			if (f.getType().equals(searchingFor) && !Modifier.isStatic(f.getModifiers())) {
				f.setAccessible(true);
				return (T) f.get(from);
			}
		}
		return null;
	}

	/**
	 * Get the first field which have the searching for class type
	 * 
	 * @param from the object where we will try to find the field
	 * @param clazz the class that have to define the field
	 * @param searchingFor the class of the required field
	 * @return the field (or null if not found)
	 * @throws Exception if something gone wrong
	 */
	public static Field getFirstFieldWith(Class<?> clazz, Class<?> searchingFor) throws Exception {
		for (Field f : clazz.getDeclaredFields()) {
			if (f.getType().equals(searchingFor) && !Modifier.isStatic(f.getModifiers())) {
				f.setAccessible(true);
				return f;
			}
		}
		return null;
	}
}
