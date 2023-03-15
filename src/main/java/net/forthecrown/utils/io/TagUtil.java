package net.forthecrown.utils.io;

import static net.forthecrown.nbt.BinaryTags.compoundTag;
import static net.forthecrown.nbt.BinaryTags.doubleTag;
import static net.forthecrown.nbt.BinaryTags.floatTag;
import static net.forthecrown.nbt.BinaryTags.listTag;
import static net.forthecrown.nbt.BinaryTags.stringTag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.IntArrayTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.nbt.StringTag;
import net.forthecrown.nbt.TagTypes;
import net.forthecrown.nbt.TypeIds;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
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

/**
 * A utility class to do some tedious tasks that involve NBT serialization.
 */
public final class TagUtil {

  private static final CraftPersistentDataTypeRegistry REGISTRY = new CraftPersistentDataTypeRegistry();

  private TagUtil() {
  }

  public static IntArrayTag writeUUID(UUID uuid) {
    return BinaryTags.saveUuid(uuid);
  }

  public static UUID readUUID(BinaryTag tag) {
    return BinaryTags.loadUuid((IntArrayTag) tag);
  }

  public static StringTag writeKey(Key key) {
    return stringTag(key.asString());
  }

  public static NamespacedKey readKey(BinaryTag tag) {
    Validate.isTrue(tag.getId() == TypeIds.STRING, "BinaryTag is not string");
    return Keys.parse(tag.toString());
  }

  public static BinaryTag writeItem(ItemStack item) {
    return ItemStacks.save(item);
  }

  public static ItemStack readItem(BinaryTag t) {
    return ItemStacks.load((CompoundTag) t);
  }

  public static CompoundTag ofContainer(PersistentDataContainer container) {
    return PaperNbt.fromDataContainer(container);
  }

  public static PersistentDataContainer ofCompound(CompoundTag tag) {
    return PaperNbt.toDataContainer(tag, TagUtil::newContainer);
  }

  public static CraftPersistentDataContainer newContainer() {
    return new CraftPersistentDataContainer(REGISTRY);
  }

  public static <T> ListTag writeList(Collection<T> list, Function<T, BinaryTag> serializer) {
    ListTag tag = BinaryTags.listTag();

    for (T t : list) {
      tag.add(serializer.apply(t));
    }

    return tag;
  }

  public static <T> List<T> readList(BinaryTag tag, Function<BinaryTag, T> deserializer) {
    List<T> l = new ObjectArrayList<>();

    for (BinaryTag t : tag.asList()) {
      l.add(deserializer.apply(t));
    }

    return l;
  }

  public static <E extends Enum<E>> BinaryTag writeEnum(E anum) {
    return stringTag(anum.name().toLowerCase());
  }

  public static <E extends Enum<E>> E readEnum(Class<E> clazz, BinaryTag t) {
    return Enum.valueOf(clazz, t.toString().toUpperCase());
  }

  public static BinaryTag writeText(Component component) {
    return stringTag((GsonComponentSerializer.gson().serialize(component)));
  }

  public static Component readText(BinaryTag tag) {
    return GsonComponentSerializer.gson().deserialize(tag.toString());
  }

  public static BinaryTag writeLocation(Location location) {
    CompoundTag tag = compoundTag();
    ListTag pos = listTag();
    pos.add(doubleTag(location.getX()));
    pos.add(doubleTag(location.getY()));
    pos.add(doubleTag(location.getZ()));

    ListTag rot = listTag();
    rot.add(floatTag(location.getYaw()));
    rot.add(floatTag(location.getPitch()));

    tag.put("pos", pos);
    tag.put("rot", rot);

    if (location.getWorld() != null) {
      tag.putString("world", location.getWorld().getName());
    }

    return tag;
  }

  public static Location readLocation(BinaryTag tagg) {
    CompoundTag tag = tagg.asCompound();
    ListTag pos = tag.getList("pos", TagTypes.doubleType());
    ListTag rot = tag.getList("rot", TagTypes.floatType());

    double x    = pos.get(0).asNumber().doubleValue();
    double y    = pos.get(1).asNumber().doubleValue();
    double z    = pos.get(2).asNumber().doubleValue();
    float yaw   = rot.get(0).asNumber().floatValue();
    float pitch = rot.get(1).asNumber().floatValue();

    String worldName = tag.getString("world");
    World world = worldName.isBlank() ? null : Bukkit.getWorld(worldName);

    return new Location(world, x, y, z, yaw, pitch);
  }

  public static <V> V[] readArray(BinaryTag tag,
                                  Function<BinaryTag, V> deserializer,
                                  IntFunction<V[]> factory
  ) {
    ListTag list = (ListTag) tag;
    V[] arr = factory.apply(list.size());

    for (int i = 0; i < list.size(); i++) {
      arr[i] = deserializer.apply(list.get(i));
    }

    return arr;
  }

  public static <V> ListTag writeArray(V[] arr, Function<V, BinaryTag> serializer) {
    ListTag tag = listTag();
    for (V v : arr) {
      tag.add(serializer.apply(v));
    }
    return tag;
  }

  public static BlockData readBlockData(BinaryTag t) {
    return PaperNbt.loadBlockData(t.asCompound());
  }

  public static CompoundTag writeBlockData(BlockData data) {
    return PaperNbt.saveBlockData(data);
  }
}