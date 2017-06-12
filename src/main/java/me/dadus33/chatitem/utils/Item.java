package me.dadus33.chatitem.utils;


import com.github.steveice10.opennbt.tag.builtin.CompoundTag;

public class Item {
    private String id;
    private byte amount;
    private short data;
    private CompoundTag tag;

    public CompoundTag getTag(){
        return tag;
    }

    public void setTag(CompoundTag newTag){
        this.tag = newTag;
    }

    public void setId(String newId){
        this.id = newId;
    }

    public void setData(short newData){
        data = newData;
    }

    public String getId(){
        return id;
    }

    public short getData(){
        return data;
    }

    public void setAmount(byte newAmount){
        this.amount = newAmount;
    }

    public byte getAmount(){
        return amount;
    }
}
