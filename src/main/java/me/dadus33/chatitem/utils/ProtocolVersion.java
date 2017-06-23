package me.dadus33.chatitem.utils;

import me.dadus33.chatitem.ChatItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.Via;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ProtocolVersion {
    PRE_1_8(0, 5, 0),  //1.7.X
    BETWEEN_1_8_AND_1_9(6, 47, 1),  //1.8.X
    BETWEEN_1_9_AND_1_10(49, 110, 2),  //1.9.X - Starts with 49 as 48 was an april fools update
    BETWEEN_1_10_AND_1_11(201, 210, 3),  //1.10.X
    POST_1_11(311, 316, 4),  //1.11.X
    INVALID(-1, -1, 5);

    private final int MIN_VER;
    private final int MAX_VER;
    public final int INDEX; //Represents how new the version is (0 - extremely old)

    private static ProtocolVersion serverVersion;

    private static ConcurrentHashMap<String, Integer> PLAYER_VERSION_MAP = new ConcurrentHashMap<>();

    ProtocolVersion(int min, int max, int index){
        this.MIN_VER = min;
        this.MAX_VER = max;
        this.INDEX = index;
    }

    public static ProtocolVersion getVersion(int protocolVersion){
        for (ProtocolVersion ver : ProtocolVersion.values()) {
            if(protocolVersion >= ver.MIN_VER && protocolVersion <= ver.MAX_VER){
                return ver;
            }
        }
        return INVALID;
    }

    public static ProtocolVersion getServerVersion(){
        if(serverVersion == null){
            String version = ChatItem.getVersion(Bukkit.getServer());
            switch (version){
                case "v1_7_R1": serverVersion = PRE_1_8; break;
                case "v1_7_R2": serverVersion = PRE_1_8; break;
                case "v1_7_R3": serverVersion = PRE_1_8; break;
                case "v1_8_R1": serverVersion = BETWEEN_1_8_AND_1_9; break;
                case "v1_8_R2": serverVersion = BETWEEN_1_8_AND_1_9; break;
                case "v1_8_R3": serverVersion = BETWEEN_1_8_AND_1_9; break;
                case "v1_9_R1": serverVersion = BETWEEN_1_9_AND_1_10; break;
                case "v1_9_R2": serverVersion = BETWEEN_1_9_AND_1_10; break;
                case "v1_10_R1": serverVersion = BETWEEN_1_10_AND_1_11; break;
                case "v1_10_R2": serverVersion = BETWEEN_1_10_AND_1_11; break;
                case "v1_11_R1": serverVersion = POST_1_11; break;
                case "v1_11_R2": serverVersion = POST_1_11; break;
            }
        }
        return serverVersion;
    }

    public static void remapIds(ProtocolVersion server, ProtocolVersion player, Item item){
        if(areIdsCompatible(server, player)){
            return;
        }
        if((server == BETWEEN_1_9_AND_1_10 && player == BETWEEN_1_8_AND_1_9) || (server == BETWEEN_1_8_AND_1_9 && player == BETWEEN_1_9_AND_1_10)){
            if(server == BETWEEN_1_9_AND_1_10){
                ItemRewriter_1_9_TO_1_8.reversedToClient(item);
            }else{
                ItemRewriter_1_9_TO_1_8.toClient(item);
            }
        }else{
            if(server == BETWEEN_1_10_AND_1_11){
                ItemRewriter_1_11_TO_1_10.toClient(item);
            }else{
                ItemRewriter_1_11_TO_1_10.reverseToClient(item);
            }
        }
    }

    public static boolean areIdsCompatible(ProtocolVersion first, ProtocolVersion second){
        if((first == BETWEEN_1_9_AND_1_10 && second == BETWEEN_1_8_AND_1_9) || (first == BETWEEN_1_8_AND_1_9 && second == BETWEEN_1_9_AND_1_10)){
            return false;
        }
        if((first == BETWEEN_1_10_AND_1_11 && second == POST_1_11) || (first == POST_1_11 && second == BETWEEN_1_10_AND_1_11)){
            return false;
        }
        return true;
    }

    public static int getClientVersion(final Player p){

        if(p==null){
            throw new NullPointerException("Player cannot be null!");
        }

        if(ChatItem.usesViaVersion()){
            return Via.getAPI().getPlayerVersion(p.getUniqueId());
        }else if(ChatItem.usesProtocolSupport()){
             return ProtocolSupportUtil.getProtocolVersion(p);
        }


        Bukkit.getScheduler().scheduleSyncDelayedTask(ChatItem.getInstance(), new Runnable() {
            @Override
            public void run() {
                for(Map.Entry<String, Integer> entry : PLAYER_VERSION_MAP.entrySet()){
                    System.out.println(entry.getKey()+"      ==      "+entry.getValue());
                }
                for(int i = 1; i<=5; ++i){
                    System.out.println(" ");
                }
                System.out.println(stringifyAdress(p.getAddress()));
            }
        });

        return PLAYER_VERSION_MAP.get(stringifyAdress(p.getAddress()));
    }

    public static String stringifyAdress(InetSocketAddress address){
        String port = String.valueOf(address.getPort());
        String ip = address.getAddress().getHostAddress();
        return ip+":"+port;
    }

    public static Map<String, Integer> getPlayerVersionMap(){
        return PLAYER_VERSION_MAP;
    }


}
