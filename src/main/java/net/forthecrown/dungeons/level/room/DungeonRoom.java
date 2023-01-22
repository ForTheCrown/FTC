package net.forthecrown.dungeons.level.room;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.PieceStyle;
import net.forthecrown.dungeons.level.PieceVisitor;
import net.forthecrown.dungeons.level.PieceVisitor.Result;
import net.forthecrown.dungeons.level.decoration.DungeonSpawner;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.io.TagUtil;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DungeonRoom
    extends DungeonPiece
    implements RoomComponentAccess, Iterable<RoomComponent>
{
  private static final Logger LOGGER = Loggers.getLogger();

  public static final String
      TAG_SPAWNERS = "spawners",
      TAG_COMPONENTS = "components";

  @Getter
  private final List<DungeonSpawner> spawners = new ObjectArrayList<>();

  @Getter
  private final List<Player> players = new LinkedList<>();

  private RoomComponent[] components = new RoomComponent[0];

  /* ---------------------------- CONSTRUCTORS ---------------------------- */

  public DungeonRoom(RoomType type) {
    super(type);
  }

  public DungeonRoom(RoomType piece, CompoundTag tag) {
    super(piece, tag);

    if (tag.contains(TAG_SPAWNERS)) {
      spawners.addAll(
          TagUtil.readCollection(tag.get(TAG_SPAWNERS), DungeonSpawner::load)
      );
    }

    if (!tag.contains(TAG_COMPONENTS)) {
      return;
    }

    for (var e: tag.getCompound(TAG_COMPONENTS).tags.entrySet()) {
      RoomComponents.REGISTRY.get(e.getKey())
          .ifPresentOrElse(type -> {
            RoomComponent component = type.newInstance();
            component.load((CompoundTag) e.getValue());
            addComponent(component);
          }, () -> {
            LOGGER.warn(
                "Unknown component type {} found in room {}",
                e.getKey(), getId()
            );
          });
    }
  }

  /* ----------------------------- COMPONENTS ----------------------------- */

  @Override
  public boolean addComponent(RoomComponent component) {
    if (hasComponent(component.getClass())) {
      return false;
    }

    RoomComponentType<?> type = RoomComponents.of(component.getClass());
    components = ObjectArrays.ensureCapacity(components, type.getIndex() + 1);
    components[type.getIndex()] = component;

    component.room = this;
    component.onAttach(this);

    return true;
  }

  @Override
  public <T extends RoomComponent> T removeComponent(RoomComponentType<T> type) {
    if (!hasComponent(type)) {
      return null;
    }

    RoomComponent c = components[type.getIndex()];
    components[type.getIndex()] = null;

    if (c != null) {
      c.onDetach(this);
      c.room = null;
    }

    return (T) c;
  }

  @Override
  public <T> List<T> getInheritedComponents(Class<T> type) {
    LinkedList<T> list = new LinkedList<>();

    for (RoomComponent c: this)  {
      if (!type.isInstance(c)) {
        continue;
      }

      list.add((T) c);
    }

    return list;
  }

  @Override
  public <T extends RoomComponent> T getComponent(RoomComponentType<T> type) {
    return type.getIndex() >= components.length
        ? null
        : (T) components[type.getIndex()];
  }

  public void clearComponents() {
    if (components.length == 0) {
      return;
    }

    RoomComponent[] arr = components.clone();

    for (var a: arr) {
      removeComponent(RoomComponents.of(a.getClass()));
    }
  }

  @NotNull
  @Override
  public Iterator<RoomComponent> iterator() {
    return ArrayIterator.unmodifiable(components);
  }

  /* ------------------------------ METHODS ------------------------------- */

  @Override
  public String getPaletteName() {
    return getType()
        .getVariant(PieceStyle.DEFAULT);
  }

  @Override
  public RoomType getType() {
    return (RoomType) super.getType();
  }

  @Override
  protected Result onVisit(PieceVisitor walker) {
    return walker.onRoom(this);
  }

  public void onTick(World world, DungeonLevel level) {
    spawners.forEach(spawner -> spawner.onTick(world));
    forEach(component -> component.onActiveTick(level, world));
  }

  public void onIdleTick(World world, DungeonLevel level) {
    forEach(component -> component.onIdleTick(level, world));
  }

  public void onEnter(Player user, DungeonLevel level) {
    forEach(component -> component.onEnter(user, level));
  }

  public void onExit(Player user, DungeonLevel level) {
    forEach(component -> component.onExit(user, level));
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  @Override
  protected void saveAdditional(CompoundTag tag) {
    if (!spawners.isEmpty()) {
      tag.put(TAG_SPAWNERS,
          TagUtil.writeCollection(spawners, DungeonSpawner::save)
      );
    }

    var it = iterator();
    CompoundTag componentTag = new CompoundTag();

    while (it.hasNext()) {
      var n = it.next();
      CompoundTag cTag = new CompoundTag();
      n.save(cTag);

      RoomComponentType type = RoomComponents.of(n.getClass());
      componentTag.put(type.getKey(), cTag);
    }

    if (!componentTag.isEmpty()) {
      tag.put(TAG_COMPONENTS, componentTag);
    }
  }
}