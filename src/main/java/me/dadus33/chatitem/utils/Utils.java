package me.dadus33.chatitem.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.dadus33.chatitem.ChatItem;

public class Utils {

	@SuppressWarnings("unchecked")
	public static List<Player> getOnlinePlayers() {
		Object players = Bukkit.getOnlinePlayers();
		if(players instanceof Player[])
			return Arrays.asList((Player[]) players);
		return new ArrayList<>((Collection<Player>) players);
	}
	
	public static String getFromURL(String urlName) {
		ChatItem pl = ChatItem.getInstance();
		try {
			URL url = new URL(urlName);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setUseCaches(true);
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setRequestProperty("User-Agent", "ChatItem " + pl.getDescription().getVersion());
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String content = "";
			String input;
			while ((input = br.readLine()) != null)
				content = content + input;
			br.close();
			return content;
        } catch (SocketTimeoutException e) {
        	pl.getLogger().info("Failed to access to " + urlName + " (Reason: timed out).");
        } catch (UnknownHostException | MalformedURLException e) {
        	pl.getLogger().info("Could not use the internet connection to check for update or send stats");
        } catch (ConnectException e) {
        	pl.getLogger().warning("Cannot connect to " + urlName + " (Reason: " + e.getMessage() + ").");
        } catch (IOException e) {
        	e.printStackTrace();
		}
		return null;
	}
}
