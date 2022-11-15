package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.ReflectionUtils;

public class IChatBaseComponentManager implements IComponentManager {

	private Method serializerGetJson;
	private boolean canEditVariable = true;

	public IChatBaseComponentManager() {
		try {
			for (Method m : PacketUtils.CHAT_SERIALIZER.getDeclaredMethods()) {
				if (m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(PacketUtils.COMPONENT_CLASS)
						&& m.getReturnType().equals(String.class)) {
					serializerGetJson = m;
					break;
				}
			}
			if (serializerGetJson == null)
				ChatItem.getInstance().getLogger().warning(
						"Failed to find JSON serializer in class: " + PacketUtils.CHAT_SERIALIZER.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasConditions() {
		return serializerGetJson != null;
	}

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		try {
			Object obj = packet.getContent().getChatComponents().readSafely(0);
			return obj == null ? null : (String) serializerGetJson.invoke(null, obj);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		try {
			PacketContent content = packet.getContent();
			Object chatComponent = PacketUtils.CHAT_SERIALIZER.getMethod("a", String.class).invoke(null, json);
			if (canEditVariable)
				content.getChatComponents().write(0, chatComponent);
			else {
				try {
					Class<?> packetClass = packet.getPacket().getClass();
					if (packetClass.getSimpleName().equalsIgnoreCase("ClientboundSystemChatPacket")) {
						for (Constructor<?> cons : packetClass.getConstructors()) {
							if (cons.getParameterCount() == 6) {
								Object newPacket = cons.newInstance(chatComponent, Optional.empty(),
										content.getIntegers().readSafely(0, 0),
										content.getSpecificModifier(
												PacketUtils.getNmsClass("ChatSender", "network.chat.")).read(0),
										content.getSpecificModifier(Instant.class).read(0),
										ReflectionUtils.getObject(packet.getPacket(), "f"));
								packet.setPacket(newPacket);
							}
						}
						throw new UnsupportedOperationException("Failed to find valid constructor for packet "
								+ packetClass.getSimpleName() + ". Please report this.");
					} else {
						throw new UnsupportedOperationException("The packet " + packetClass.getSimpleName()
								+ " isn't supported by the AdventureGetter. Please report this.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IllegalAccessException e) {
			if (canEditVariable) {
				canEditVariable = false;
				writeJson(packet, json); // try again with good config
			} else
				e.printStackTrace(); // something else
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
