package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Keys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_18_R2.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public final class TagUtil {
    private static final CraftPersistentDataTypeRegistry REGISTRY = new CraftPersistentDataTypeRegistry();

    private TagUtil() {}

    public static IntArrayTag writeUUID(UUID uuid) {
        return NbtUtils.createUUID(uuid);
    }

    public static UUID readUUID(Tag tag) {
        return NbtUtils.loadUUID(tag);
    }

    public static StringTag writeKey(Key key) {
        return StringTag.valueOf(key.asString());
    }

    public static StringTag writeKey(Keyed keyed) {
        return writeKey(keyed.key());
    }

    public static Key readKey(Tag tag) {
        Validate.isTrue(tag.getId() == Tag.TAG_STRING, "Tag is not string");
        return Keys.parse(tag.getAsString());
    }

    public static Tag writeItem(ItemStack item) {
        return CraftItemStack.asNMSCopy(item).save(new CompoundTag());
    }

    public static ItemStack readItem(Tag t) {
        net.minecraft.world.item.ItemStack nms = net.minecraft.world.item.ItemStack.of((CompoundTag) t);
        return CraftItemStack.asCraftMirror(nms);
    }

    public static CompoundTag ofContainer(PersistentDataContainer container) {
        CraftPersistentDataContainer dataContainer = (CraftPersistentDataContainer) container;
        return dataContainer.toTagCompound();
    }

    public static PersistentDataContainer ofCompound(CompoundTag tag) {
        CraftPersistentDataContainer container = newContainer();
        container.putAll(tag);

        return container;
    }

    public static CraftPersistentDataContainer newContainer() {
        return new CraftPersistentDataContainer(REGISTRY);
    }

    public static <T> ListTag writeList(Collection<T> list, Function<T, Tag> serializer) {
        ListTag tag = new ListTag();

        for (T t: list) {
            tag.add(serializer.apply(t));
        }

        return tag;
    }

    public static <T> List<T> readList(ListTag tag, Function<Tag, T> deserializer) {
        List<T> l = new ObjectArrayList<>();

        for (Tag t: tag) {
            l.add(deserializer.apply(t));
        }

        return l;
    }

    public static <E extends Enum<E>> Tag writeEnum(E anum) {
        return StringTag.valueOf(anum.name().toLowerCase());
    }

    public static <E extends Enum<E>> E readEnum(Tag t, Class<E> clazz) {
        return Enum.valueOf(clazz, t.getAsString().toUpperCase());
    }

    public static Tag writeLocation(Location location) {
        CompoundTag tag = new CompoundTag();
        ListTag pos = new ListTag();
        pos.add(DoubleTag.valueOf(location.getX()));
        pos.add(DoubleTag.valueOf(location.getY()));
        pos.add(DoubleTag.valueOf(location.getZ()));

        ListTag rot = new ListTag();
        if(location.getYaw() != 0F) rot.add(FloatTag.valueOf(location.getYaw()));
        if(location.getPitch() != 0F) rot.add(FloatTag.valueOf(location.getPitch()));

        tag.put("pos", pos);
        if(!rot.isEmpty()) tag.put("rot", rot);
        if(location.getWorld() != null) tag.putString("world", location.getWorld().getName());

        return tag;
    }

    public static Location readLocation(Tag tagg) {
        CompoundTag tag = (CompoundTag) tagg;
        ListTag pos = tag.getList("pos", Tag.TAG_DOUBLE);
        ListTag rot = tag.getList("rot", Tag.TAG_FLOAT);

        double x = pos.getDouble(0);
        double y = pos.getDouble(1);
        double z = pos.getDouble(2);
        float yaw = rot.getFloat(0);
        float pitch = rot.getFloat(1);

        String worldName = tag.getString("world");
        World world = worldName.isBlank() ? null : Bukkit.getWorld(worldName);

        return new Location(world, x, y, z, yaw, pitch);
    }
}