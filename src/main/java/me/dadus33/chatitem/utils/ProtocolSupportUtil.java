package me.dadus33.chatitem.utils;


import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProtocolSupportUtil {
    private static Method GET_PROTOCOL_VERSION;
    private static Method GET_ID;

    public static void initialize(){
        try {
            GET_PROTOCOL_VERSION = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player.class);
            GET_ID = Class.forName("protocolsupport.api.ProtocolVersion").getMethod("getId");
        } catch (ClassNotFoundException | NoSuchMethodException e){
            e.printStackTrace(); //We only call initialize if we know for sure the plugin is present, so this should never happen
        }
    }


    public static int getProtocolVersion(Player p){
        try {
            Object protocolVersion = GET_PROTOCOL_VERSION.invoke(null, p);
            return (int) GET_ID.invoke(protocolVersion);
        } catch (IllegalAccessException | InvocationTargetException e){
            e.printStackTrace(); //If ProtocolSupport is installed and we called initialize() before, this should never happen
            return -1;
        }
    }
}
