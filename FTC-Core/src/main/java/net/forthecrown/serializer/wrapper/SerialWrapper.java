package net.forthecrown.serializer.wrapper;

import com.google.gson.JsonObject;
import net.forthecrown.core.Keys;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.UUID;

public interface SerialWrapper<SOURCE> {
    static JsonWrapper emptyJson() {
        return JsonWrapper.empty();
    }

    static JsonWrapper ofJson(JsonObject o) {
        return JsonWrapper.of(o);
    }

    static TagWrapper ofTag(CompoundTag tag) {
        return new TagWrapper(tag);
    }

    static TagWrapper emptyTag() {
        return new TagWrapper(new CompoundTag());
    }

    <E extends Enum<E>> void addEnum(String name, E anum);
    default  <E extends Enum<E>> E getEnum(String name, Class<E> clazz) {
        return getEnum(name, clazz, null);
    }
    <E extends Enum<E>> E getEnum(String name, Class<E> clazz, E def);

    void addLocation(String name, Location location);
    Location getLocation(String name);

    void addUUID(String name, UUID id);
    UUID getUUID(String name);

    default void addKey(String name, Keyed keyed) {
        addKey(name, keyed.key());
    }
    default void addKey(String name, Key key) {
        add(name, key.asString());
    }
    default NamespacedKey getKey(String name) {
        return Keys.parse(getString(name, null));
    }

    void addItem(String name, ItemStack item);
    default ItemStack getItem(String name) {
        return getItem(name, null);
    }
    ItemStack getItem(String name, ItemStack def);

    default String getString(String name) {
        return getString(name, null);
    }
    String getString(String name, String def);

    default boolean getBool(String name) {
        return getBool(name, false);
    }
    boolean getBool(String name, boolean def);

    default long getLong(String name) {
        return getLong(name, 0L);
    }
    long getLong(String name, long def);

    default double getDouble(String name) {
        return getDouble(name, 0.0D);
    }
    double getDouble(String name, double def);

    default float getFloat(String name) {
        return getFloat(name, 0.0F);
    }
    float getFloat(String name, float def);

    default int getInt(String name) {
        return getInt(name, 0);
    }
    int getInt(String name, int def);

    default short getShort(String name) {
        return getShort(name, (byte) 0);
    }
    short getShort(String name, short def);

    default byte getByte(String name) {
        return getByte(name, (byte) 0);
    }
    byte getByte(String name, byte def);

    void addDate(String name, Date date);
    default Date getDate(String name) { return getDate(name, null); }
    Date getDate(String name, Date def);

    Component getComponent(String name);
    void addComponent(String name, Component component);

    void remove(String property);

    void add(String property, String value);
    void add(String property, byte value);
    void add(String property, short value);
    void add(String property, int value);
    void add(String property, float value);
    void add(String property, double value);
    void add(String property, long value);
    void add(String property, boolean value);
    void add(String property, char value);

    int size();
    boolean isEmpty();
    boolean has(String memberName);

    SerialWrapper getWrapped(String name);
    SerialWrapper createWrapped(String name);

    SOURCE getSource();
}