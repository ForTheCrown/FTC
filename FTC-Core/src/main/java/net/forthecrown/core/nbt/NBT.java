package net.forthecrown.core.nbt;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

import java.util.Set;
import java.util.UUID;

public class NBT {

    public static NBT of(CompoundTag tag){
        return new NBT(tag);
    }

    public static NBT empty(){
        return new NBT(new CompoundTag());
    }

    public static NBT fromJson(JsonElement element){
        try {
            return of(TagParser.parseTag(element.getAsString()));
        } catch (CommandSyntaxException e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    final CompoundTag tag;
    private NBT(CompoundTag tag){
        this.tag = tag;
    }

    public Set<String> getKeys(){
        return tag.getAllKeys();
    }

    public int size(){
        return tag.size();
    }

    public void put(String name, Tag nbt){
        tag.put(name, nbt);
    }

    public void put(String name, NBT tags){
        this.tag.put(name, tags.tag);
    }

    public void put(String name, byte b){
        tag.putByte(name, b);
    }

    public void put(String name, short s){
        tag.putShort(name, s);
    }

    public void put(String name, int s){
        tag.putInt(name, s);
    }

    public void put(String name, long l){
        tag.putLong(name, l);
    }

    public void put(String name, float f){
        tag.putFloat(name, f);
    }

    public void put(String name, double d){
        tag.putDouble(name, d);
    }

    public void put(String name, String s){
        tag.putString(name, s);
    }

    public void put(String name, boolean b){
        tag.putBoolean(name, b);
    }

    public void put(String name, UUID id){
        tag.putUUID(name, id);
    }

    public UUID getUUID(String name){
        return tag.getUUID(name);
    }

    public boolean hasUUID(String name){
        return tag.hasUUID(name);
    }

    public void setArray(String name, byte[] bytes){
        tag.putByteArray(name, bytes);
    }

    public void setArray(String name, int[] ints){
        tag.putIntArray(name, ints);
    }

    public void setArray(String name, long[] longPenish){
        tag.putLongArray(name, longPenish);
    }

    public Tag get(String name){
        return tag.get(name);
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
        return tag.contains(s);
    }

    public NBT merge(NBT tags){
        return of(tag.merge(tags.tag));
    }

    public NBT clone(){
        return of(tag.copy());
    }

    public CompoundTag getNMS() {
        return tag;
    }

    public String serialize() {
        return tag.toString();
    }

    public Component display(){
        return PaperAdventure.asAdventure(NbtUtils.toPrettyComponent(tag));
    }
}
