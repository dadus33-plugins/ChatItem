package me.dadus33.chatitem.utils;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;

public class Colors {

	public static final String YELLOW = ColorManager.COLOR_CHAR + "e";
	public static final String AQUA = ColorManager.COLOR_CHAR + "b";
	public static final String GOLD = ColorManager.COLOR_CHAR + "6";
	public static final String GRAY = ColorManager.COLOR_CHAR + "7";
	public static final String RED = ColorManager.COLOR_CHAR + "c";
	public static final String GREEN = ColorManager.COLOR_CHAR + "a";
	public static final String RESET = ColorManager.COLOR_CHAR + "r";


	public static String color(String s) {
		return s == null || s.isEmpty() ? s : translateAlternateColorCodes('&', s);
	}
	
    public static String translateAlternateColorCodes(char altColorChar, @NotNull String textToTranslate) {
        Preconditions.checkArgument(textToTranslate != null, "Cannot translate null text");

        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && ColorManager.COLORS.contains(b[i + 1] + "")) {
                b[i] = ColorManager.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}
