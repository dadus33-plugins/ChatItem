package me.dadus33.chatitem.listeners;

import java.lang.reflect.Method;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.packets.ChatItemPacket;
import me.dadus33.chatitem.packets.PacketContent;
import me.dadus33.chatitem.packets.PacketHandler;
import me.dadus33.chatitem.packets.PacketMetadata;
import me.dadus33.chatitem.packets.PacketType;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.ProtocolVersion;
import me.dadus33.chatitem.utils.Storage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatPacketValidatorV2 extends PacketHandler {

    private Storage c;
    private Method serializerGetJson;

    public ChatPacketValidatorV2(Storage s) {
        c = s;
        try {
        	for(Method m : PacketUtils.CHAT_SERIALIZER.getDeclaredMethods()) {
        		if(m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(PacketUtils.COMPONENT_CLASS) && m.getReturnType().equals(String.class)) {
        			serializerGetJson = m;
        			break;
        		}
        	}
        	if(serializerGetJson == null)
        		ChatItem.getInstance().getLogger().warning("Failed to find JSON serializer in class: " + PacketUtils.CHAT_SERIALIZER.getCanonicalName());
        } catch (Exception e) {
        	e.printStackTrace();
		}
    }

    @Override
    public void onSend(ChatItemPacket e) {
    	if(!e.hasPlayer() || !e.getPacketType().equals(PacketType.Server.CHAT))
    		return;
    	ProtocolVersion version = ProtocolVersion.getServerVersion();
        if(version.isNewerOrEquals(ProtocolVersion.V1_8)) { //only if action bar messages are supported in this version of minecraft
            if(version.isNewerOrEquals(ProtocolVersion.V1_12)){
            	try {
	                if(((Enum<?>)e.getContent().getSpecificModifier(PacketUtils.getNmsClass("ChatMessageType", "network.chat.")).read(0)).name().equals("GAME_INFO")){
	                    return; //It means it's an actionbar message, and we ain't intercepting those
	                }
            	} catch (Exception exc) {
            		exc.printStackTrace();
				}
            } else if (e.getContent().getBytes().readSafely(0) == (byte) 2) {
                return;  //It means it's an actionbar message, and we ain't intercepting those
            }
        }
        boolean usesBaseComponents = false;
        PacketContent packet = e.getContent();
        String json;
        if(packet.getChatComponents().readSafely(0) == null){  //null check for some cases of messages sent using spigot's Chat Component API or other means
            if(!ChatItem.supportsChatComponentApi())  //only if the API is supported in this server distribution
            	return;//We don't know how to deal with anything else. Most probably some mod message we shouldn't mess with anyways
            
            BaseComponent[] components = packet.getSpecificModifier(BaseComponent[].class).readSafely(0);
            if(components == null){
                return;
            }
            json = ComponentSerializer.toString(components);
            usesBaseComponents = true;
        } else {
        	try {
        		json = (String) serializerGetJson.invoke(null, packet.getChatComponents().readSafely(0));// (String) ReflectionUtils.callMethod(packet.getChatComponents().readSafely(0), "getJson");
            } catch (Exception exc) {
        		exc.printStackTrace();
        		json = "{}";
			}
        }

        boolean found = false;
        for (String rep : c.PLACEHOLDERS) {
            if (json.contains(rep)) {
                found = true;
                break;
            }
        }
        if (!found) {
            return; //then it's just a normal message without placeholders, so we leave it alone
        }
        if(json.lastIndexOf("\\u0007") == -1){ //if the message doesn't contain the BELL separator, then it's certainly NOT a message we want to parse
            return;
        }
        PacketMetadata meta = e.getMetaData();
        meta.setMeta("parse", true); //We mark this packet to be parsed by the packet listener
        meta.setMeta("base-component", usesBaseComponents); //We also tell it whether this packet uses the base component API
        meta.setMeta("json", json); //And we finally provide it with the json we already got from the packet
    }

    public void setStorage(Storage st){
        c = st;
    }
}
