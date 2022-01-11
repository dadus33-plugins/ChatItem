package me.dadus33.chatitem.chatmanager.v1.json;

public class NBTUtils {

	@SuppressWarnings("unchecked")
	public static <T> T get(Object obj, String methodName){
		try {
			return (T) obj.getClass().getDeclaredMethod(methodName).invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
