package net.forthecrown.utils.io;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R2.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_19_R2.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * A utility class to do some tedious tasks
 * that involve NBT serialization.
 */
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

    public static NamespacedKey readKey(Tag tag) {
        Validate.isTrue(tag.getId() == Tag.TAG_STRING, "Tag is not string");
        return Keys.parse(tag.getAsString());
    }

    public static Tag writeItem(ItemStack item) {
        return ItemStacks.save(item);
    }

    public static ItemStack readItem(Tag t) {
        return ItemStacks.load((CompoundTag) t);
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

    public static <E extends Enum<E>> E readEnum(Class<E> clazz, Tag t) {
        return Enum.valueOf(clazz, t.getAsString().toUpperCase());
    }

    public static Tag writeText(Component component) {
        return StringTag.valueOf(GsonComponentSerializer.gson().serialize(component));
    }

    public static Component readText(Tag tag) {
        return GsonComponentSerializer.gson().deserialize(tag.getAsString());
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

    public static <V> ListTag writeCollection(Collection<V> c, Function<V, Tag> serializer) {
        ListTag listTag = new ListTag();

        for (var v: c) {
            listTag.add(serializer.apply(v));
        }

        return listTag;
    }

    public static <V> List<V> readCollection(Tag tag, Function<Tag, V> deserializer) {
        List<V> list = new ArrayList<>();

        if (tag instanceof ListTag listTag) {
            for (var t : listTag) {
                list.add(deserializer.apply(t));
            }
        }

        return list;
    }

    public static BlockData readBlockData(Tag t) {
        var lookup = VanillaAccess.getLevel(Worlds.overworld())
                .holderLookup(BuiltInRegistries.BLOCK.key());

        return NbtUtils.readBlockState(lookup, (CompoundTag) t)
                .createCraftBlockData();
    }

    public static CompoundTag writeBlockData(BlockData data) {
        return NbtUtils.writeBlockState(VanillaAccess.getState(data));
    }
}