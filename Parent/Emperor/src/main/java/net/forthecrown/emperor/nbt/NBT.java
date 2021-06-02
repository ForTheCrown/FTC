package net.forthecrown.emperor.nbt;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;

import java.util.Set;
import java.util.UUID;

public class NBT {

    public static NBT of(NBTTagCompound tag){
        return new NBT(tag);
    }

    public static NBT empty(){
        return new NBT(new NBTTagCompound());
    }

    public static NBT fromJson(JsonElement element){
        try {
            return of(MojangsonParser.parse(element.getAsString()));
        } catch (CommandSyntaxException e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    final NBTTagCompound tag;
    private NBT(NBTTagCompound tag){
        this.tag = tag;
    }

    public Set<String> getKeys(){
        return tag.getKeys();
    }

    public int size(){
        return tag.e();
    }

    public void put(String name, NBTBase nbt){
        tag.set(name, nbt);
    }

    public void put(String name, NBT tags){
        this.tag.set(name, tags.tag);
    }

    public void put(String name, byte b){
        tag.setByte(name, b);
    }

    public void put(String name, short s){
        tag.setShort(name, s);
    }

    public void put(String name, int s){
        tag.setInt(name, s);
    }

    public void put(String name, long l){
        tag.setLong(name, l);
    }

    public void put(String name, float f){
        tag.setFloat(name, f);
    }

    public void put(String name, double d){
        tag.setDouble(name, d);
    }

    public void put(String name, String s){
        tag.setString(name, s);
    }

    public void put(String name, boolean b){
        tag.setBoolean(name, b);
    }

    public void put(String name, UUID id){
        tag.setUUID(name, id);
    }

    public UUID getUUID(String name){
        return tag.a(name);
    }

    public boolean hasUUID(String name){
        return tag.b(name);
    }

    public void setArray(String name, byte[] bytes){
        tag.setByteArray(name, bytes);
    }

    public void setArray(String name, int[] ints){
        tag.setIntArray(name, ints);
    }

    public void setArray(String name, long[] longPenish){
        tag.a(name, longPenish);
    }

    public NBTBase get(String name){
        return tag.get(name);
    }

    public NBTTagList getAsList(String name){
        return (NBTTagList) tag.get(name);
    }

    public byte getByte(String s){
        return tag.getByte(s);
    }

    public short getShort(String s){
        return tag.getShort(s);
    }

    public int getInt(String s){
        return tag.getInt(s);
    }

    public long getLong(String s){
        return tag.getLong(s);
    }

    public float getFloat(String s){
        return tag.getFloat(s);
    }

    public double getDouble(String s){
        return tag.getDouble(s);
    }

    public String getString(String s){
        return tag.getString(s);
    }

    public boolean getBoolean(String s){
        return tag.getBoolean(s);
    }

    public byte[] getByteArray(String s){
        return tag.getByteArray(s);
    }

    public int[] getIntArray(String s){
        return tag.getIntArray(s);
    }

    public long[] getLongArray(String s){
        return tag.getLongArray(s);
    }

    public NBT getCompound(String s){
        return NBT.of(tag.getCompound(s));
    }

    public void remove(String s){
        tag.remove(s);
    }

    public boolean isEmpty(){
        return tag.isEmpty();
    }

    public boolean has(String s){
        return tag.hasKey(s);
    }

    public Component getPrettyDisplay(String s, int i){
        return PaperAdventure.asAdventure(tag.a(s, i));
    }

    public Component getPrettyDisplay(){
        return PaperAdventure.asAdventure(tag.getNbtPrettyComponent());
    }

    public NBT merge(NBT tags){
        return of(this.tag.a(tags.tag));
    }

    public NBT clone(){
        return of(tag.clone());
    }

    public NBTTagCompound getNMS() {
        return tag;
    }

    public String serialize() {
        return tag.toString();
    }
}
