package me.dadus33.chatitem;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class C {

	public static TextComponent text(String s) {
		return LegacyComponentSerializer.legacySection().deserialize(s);
	}
}
