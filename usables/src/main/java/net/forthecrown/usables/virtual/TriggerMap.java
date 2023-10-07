package net.forthecrown.usables.virtual;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.SimpleMapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.io.TagOps;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class TriggerMap<A> {

  static final Codec<ObjectList<String>> OBJECT_LIST_CODEC
      = Codec.STRING.listOf().xmap(ObjectArrayList::new, Function.identity());

  private final Codec<Map<A, ObjectList<String>>> mapCodec;

  private final Map<A, ObjectList<String>> action2References = new Object2ObjectOpenHashMap<>();

  public TriggerMap(Codec<A> codec) {
    this.mapCodec = new SimpleMapCodec<>(codec, OBJECT_LIST_CODEC, null).codec();
  }

  public void clear() {
    action2References.clear();
  }

  public List<String> getAll(Set<A> keys) {
    if (keys.size() == 1) {
      return get(keys.iterator().next());
    }

    ObjectList<String> result = new ObjectArrayList<>();

    for (A key : keys) {
      var list = get(key);

      if (list.isEmpty()) {
        continue;
      }

      result.addAll(list);
    }

    if (result.isEmpty()) {
      return ObjectLists.emptyList();
    }

    return ObjectLists.unmodifiable(result);
  }

  public List<String> get(A key) {
    var list = action2References.get(key);

    if (list == null || list.isEmpty()) {
      return ObjectLists.emptyList();
    }

    return ObjectLists.unmodifiable(list);
  }

  public boolean remove(A key, String ref) {
    var list = action2References.get(key);

    if (list == null) {
      return false;
    }

    boolean removed = list.remove(ref);

    if (list.isEmpty()) {
      action2References.remove(key);
    }

    return removed;
  }

  public boolean add(A key, String ref) {
    ObjectList<String> list = action2References.computeIfAbsent(
        key,
        a -> new ObjectArrayList<>()
    );

    if (list.contains(ref)) {
      return false;
    }

    return list.add(ref);
  }

  public boolean isEmpty() {
    if (action2References.isEmpty()) {
      return true;
    }

    for (List<String> value : action2References.values()) {
      if (value.isEmpty()) {
        continue;
      }

      return false;
    }

    return true;
  }

  public DataResult<TriggerMap<A>> loadFromContainer(
      PersistentDataContainer container,
      NamespacedKey key
  ) {
    var pdc = container.get(key, PersistentDataType.TAG_CONTAINER);
    if (pdc == null) {
      return Results.success(this);
    }
    return loadFromContainer(pdc);
  }

  public DataResult<TriggerMap<A>> loadFromContainer(PersistentDataContainer container) {
    return load(new Dynamic<>(TagOps.OPS, PaperNbt.fromDataContainer(container)));
  }

  public <S> DataResult<TriggerMap<A>> load(Dynamic<S> dynamic) {
    return mapCodec.parse(dynamic).map(aListMap -> {
      aListMap.forEach((a, usableRefs) -> {
        action2References.put(a, new ObjectArrayList<>(usableRefs));
      });
      return this;
    });
  }

  public <S> DataResult<S> save(DynamicOps<S> ops) {
    return mapCodec.encodeStart(ops, action2References);
  }

  public DataResult<Unit> saveToContainer(PersistentDataContainer container, NamespacedKey key) {
    if (isEmpty()) {
      container.remove(key);
      return Results.success(Unit.INSTANCE);
    }

    return save(TagOps.OPS).map(binaryTag -> {
      var compound = binaryTag.asCompound();

      PersistentDataContainer pdc
          = PaperNbt.toDataContainer(compound, container.getAdapterContext());

      container.set(key, PersistentDataType.TAG_CONTAINER, pdc);
      return Unit.INSTANCE;
    });
  }
}
