package me.dadus33.chatitem.utils;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflect {


    private static String versionString;


    public static String getVersion() {
        if (versionString == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
        }

        return versionString;
    }

    public static Class<?> getNMSClass(String nmsClassName) {

        String clazzName = "net.minecraft.server." + getVersion() + nmsClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }

        return clazz;
    }


    public static Class<?> getOBCClass(String obcClassName) {

        String clazzName = "org.bukkit.craftbukkit." + getVersion() + obcClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }

        return clazz;
    }


    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            Method method = clazz.getMethod(methodName, params);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            return field;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}