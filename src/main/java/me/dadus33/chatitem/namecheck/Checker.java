package me.dadus33.chatitem.namecheck;


import me.dadus33.chatitem.namecheck.nbt.JsonToNBT;
import me.dadus33.chatitem.namecheck.nbt.NBTBase;
import me.dadus33.chatitem.namecheck.nbt.NBTException;

public class Checker {

    public static boolean checkItem(String jsonRepresentation){
        try{
            NBTBase base = JsonToNBT.getTagFromJson(jsonRepresentation);
            return base != null;
        }catch(NBTException e){
            return false;
        }
    }

}
