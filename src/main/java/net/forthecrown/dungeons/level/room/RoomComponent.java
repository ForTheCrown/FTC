package net.forthecrown.dungeons.level.room;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import lombok.Getter;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.World;
import org.bukkit.entity.Player;

@Getter
public abstract class RoomComponent implements RoomComponentAccess {
  DungeonRoom room;

  /* ------------------------ COMPONENT DELEGATES ------------------------- */

  @Override
  public <T extends RoomComponent> T getComponent(RoomComponentType<T> type) {
    return room == null ? null : room.getComponent(type);
  }

  @Override
  public <T extends RoomComponent> T removeComponent(RoomComponentType<T> type) {
    return room == null ? null : room.removeComponent(type);
  }

  @Override
  public final <T> List<T> getInheritedComponents(Class<T> type) {
    return room == null
        ? ObjectLists.emptyList()
        : room.getInheritedComponents(type);
  }

  @Override
  public final boolean addComponent(RoomComponent component) {
    return room != null && room.addComponent(component);
  }

  /* ----------------------------- CALLBACKS ------------------------------ */

  protected void save(CompoundTag tag) {}
  protected void load(CompoundTag tag) {}

  protected void onActiveTick(DungeonLevel level, World world) {}
  protected void onIdleTick(DungeonLevel level, World world) {}

  protected void onAttach(DungeonRoom room) {}
  protected void onDetach(DungeonRoom room) {}

  protected void onEnter(Player player, DungeonLevel level) {}
  protected void onExit(Player player, DungeonLevel level) {}
}