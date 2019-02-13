package me.dadus33.chatitem.filters;

import me.dadus33.chatitem.listeners.ChatEventListener;
import me.dadus33.chatitem.utils.Storage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static org.apache.logging.log4j.core.Filter.Result.NEUTRAL;


public class Log4jFilter implements Filter {

    private boolean stopped;
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
    public Result filter(Logger logger, Level level, Marker marker, String s, Object objects) {
        return checkMessage(s);
    }
    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object objects,Object obj) {
        return checkMessage(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2) {
        return checkMessage(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3) {
        return checkMessage(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4) {
        return checkMessage(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5) {
        return checkMessage(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
        return checkMessage(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7) {
        return checkMessage(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8) {
        return checkMessage(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9) {
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

    @Override
    public State getState() {
        return State.INITIALIZED;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {
        stopped = false;
    }

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public boolean isStarted() {
        return !this.stopped;
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    private Result checkMessage(String msg){
        if(msg==null){
            return Result.NEUTRAL;
        }
        for(String placeholder : c.PLACEHOLDERS){
            if(msg.contains(placeholder)){
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(msg.contains(ChatEventListener.SEPARATOR +p.getName())){
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
