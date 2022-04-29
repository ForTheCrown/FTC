package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Keys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.Validate;
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
}