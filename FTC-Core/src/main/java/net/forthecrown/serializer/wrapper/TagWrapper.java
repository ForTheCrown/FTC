package net.forthecrown.serializer.wrapper;

import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.utils.TagUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.UUID;

public class TagWrapper implements SerialWrapper<CompoundTag> {
    private final CompoundTag source;

    TagWrapper(CompoundTag source) {
        this.source = source;
    }

    @Override
    public <E extends Enum<E>> void addEnum(String name, E anum) {
        source.put(name, TagUtil.writeEnum(anum));
    }

    @Override
    public <E extends Enum<E>> E getEnum(String name, Class<E> clazz, E def) {
        return source.contains(name) ? TagUtil.readEnum(source.get(name), clazz) : null;
    }

    @Override
    public void addLocation(String name, Location location) {
        source.put(name, TagUtil.writeLocation(location));
    }

    @Override
    public Location getLocation(String name) {
        return source.contains(name) ? TagUtil.readLocation(source.get(name)) : null;
    }

    @Override
    public void addUUID(String name, UUID id) {
        source.putUUID(name, id);
    }

    @Override
    public UUID getUUID(String name) {
        return source.getUUID(name);
    }

    @Override
    public void addItem(String name, ItemStack item) {
        source.put(name, CraftItemStack.asNMSCopy(item).save(new CompoundTag()));
    }

    @Override
    public ItemStack getItem(String name, ItemStack def) {
        if (!source.contains(name, Tag.TAG_COMPOUND)) return def;

        return CraftItemStack.asCraftMirror(
                net.minecraft.world.item.ItemStack.of(source.getCompound(name))
        );
    }

    @Override
    public String getString(String name, String def) {
        return source.contains(name, Tag.TAG_STRING) ? source.getString(name) : def;
    }

    @Override
    public boolean getBool(String name, boolean def) {
        return source.contains(name, Tag.TAG_BYTE) ? source.getBoolean(name) : def;
    }

    @Override
    public long getLong(String name, long def) {
        return source.contains(name, Tag.TAG_LONG) ? source.getLong(name) : def;
    }

    @Override
    public double getDouble(String name, double def) {
        return source.contains(name, Tag.TAG_DOUBLE) ? source.getDouble(name) : def;
    }

    @Override
    public float getFloat(String name, float def) {
        return source.contains(name, Tag.TAG_FLOAT) ? source.getShort(name) : def;
    }

    @Override
    public int getInt(String name, int def) {
        return source.contains(name, Tag.TAG_INT) ? source.getInt(name) : def;
    }

    @Override
    public short getShort(String name, short def) {
        return source.contains(name, Tag.TAG_SHORT) ? source.getShort(name) : def;
    }

    @Override
    public byte getByte(String name, byte def) {
        return source.contains(name, Tag.TAG_BYTE) ? source.getByte(name) : def;
    }

    @Override
    public void addDate(String name, Date date) {
        add(name, date.getTime());
    }

    @Override
    public Date getDate(String name, Date def) {
        return new Date(getLong(name, def.getTime()));
    }

    @Override
    public Component getComponent(String name) {
        String s = getString(name);
        if(s == null) return null;

        return ChatUtils.GSON.deserialize(s);
    }

    @Override
    public void addComponent(String name, Component component) {
        add(name, ChatUtils.GSON.serialize(component));
    }

    @Override
    public void remove(String property) {
        source.remove(property);
    }

    @Override
    public void add(String property, String value) {
        source.putString(property, value);
    }

    @Override
    public void add(String property, byte value) {
        source.putByte(property, value);
    }

    @Override
    public void add(String property, short value) {
        source.putShort(property, value);
    }

    @Override
    public void add(String property, int value) {
        source.putInt(property, value);
    }

    @Override
    public void add(String property, float value) {
        source.putFloat(property, value);
    }

    @Override
    public void add(String property, double value) {
        source.putDouble(property, value);
    }

    @Override
    public void add(String property, long value) {
        source.putLong(property, value);
    }

    @Override
    public void add(String property, boolean value) {
        source.putBoolean(property, value);
    }

    @Override
    public void add(String property, char value) {
        source.putInt(property, value);
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    public boolean isEmpty() {
        return source.isEmpty();
    }

    @Override
    public boolean has(String memberName) {
        return source.contains(memberName);
    }

    @Override
    public TagWrapper getWrapped(String name) {
        return has(name) ? new TagWrapper(source.getCompound(name)) : null;
    }

    @Override
    public TagWrapper createWrapped(String name) {
        TagWrapper wrapper = new TagWrapper(new CompoundTag());
        source.put(name, wrapper.source);

        return wrapper;
    }

    @Override
    public CompoundTag getSource() {
        return source;
    }
}