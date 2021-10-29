package me.dadus33.chatitem.packets;

import java.util.HashMap;

public class PacketMetadata {

	private final HashMap<String, Object> metas = new HashMap<>();
	
	public void setMeta(String key, Object obj) {
		this.metas.put(key, obj);
	}
	
	public boolean hasMeta(String key) {
		return metas.containsKey(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getMeta(String key, T def) {
		return (T) metas.getOrDefault(key, def);
	}
	
	public HashMap<String, Object> getMetas() {
		return metas;
	}
}
