package me.dadus33.chatitem.filters;

import static org.apache.logging.log4j.core.Filter.Result.NEUTRAL;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.dadus33.chatitem.chatmanager.v2.ChatListener;
import me.dadus33.chatitem.utils.Storage;


public class Log4jFilter implements Filter {

    private Storage c;

    public Log4jFilter(Storage st){
        c = st;
        ((Logger) LogManager.getRootLogger()).addFilter(this);
    }


    @Override
    public Result getOnMismatch() {
        return NEUTRAL;
    }

    @Override
    public Result getOnMatch() {
        return NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object... objects) {
        return checkMessage(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object o, Throwable throwable) {
        return checkMessage(((Message)o).getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return checkMessage(message.getFormattedMessage());
    }

    @Override
    public Result filter(LogEvent logEvent) {
        return checkMessage(logEvent.getMessage().getFormattedMessage());
    }

    private Result checkMessage(String msg){
        if(msg==null){
            return Result.NEUTRAL;
        }
        for(String placeholder : c.PLACEHOLDERS){
            if(msg.contains(placeholder)){
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(msg.contains(ChatListener.SEPARATOR +p.getName())){
                        return Result.DENY;
                    }
                }

            }
        }
        return Result.NEUTRAL;
    }

    public void setStorage(Storage nst){
        this.c = nst;
    }
}
