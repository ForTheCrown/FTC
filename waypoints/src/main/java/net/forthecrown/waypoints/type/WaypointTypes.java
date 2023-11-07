package net.forthecrown.waypoints.type;

import com.mojang.datafixers.util.Pair;
import java.util.HashSet;
import java.util.Set;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.registry.RegistryListener;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

public class WaypointTypes {

  public static final Registry<WaypointType> REGISTRY = Registries.newFreezable();

  public static final WaypointType ADMIN = new AdminWaypoint();
  public static final WaypointType PLAYER = new PlayerWaypointType();
  public static final WaypointType REGION_POLE = new RegionPoleType();

  private static int highestColumn;
  private static final Set<WaypointType> buildableTypes = new HashSet<>();

  static {
    REGISTRY.setListener(new RegistryListener<>() {
      @Override
      public void onRegister(Holder<WaypointType> value) {
        WaypointType type = value.getValue();

        if (!type.isBuildable()) {
          return;
        }

        highestColumn = Math.max(highestColumn, type.getColumn().length);
        buildableTypes.add(type);
      }

      @Override
      public void onUnregister(Holder<WaypointType> value) {
        highestColumn = 0;

        var type = value.getValue();
        if (type.isBuildable()) {
          buildableTypes.remove(type);
        }

        for (var t: REGISTRY) {
          if (!t.isBuildable()) {
            continue;
          }

          highestColumn = Math.max(highestColumn, t.getColumn().length);
        }
      }
    });
  }

  public static void registerAll() {
    register("admin", ADMIN);
    register("player", PLAYER);
    register("region_pole", REGION_POLE);
  }

  public static boolean isDestroyed(Material[] column, Vector3i pos, World world) {
    int destroyedCount = 0;

    for (int i = 0; i < column.length; i++) {
      var bPos = i == 0 ? pos : pos.add(0, i, 0);
      var block = Vectors.getBlock(bPos, world);

      if (block.getType() != column[i]) {
        ++destroyedCount;
      }
    }

    return destroyedCount >= column.length;
  }

  /**
   * Tests if the given block is the top of a waypoint.
   *
   * @param block The block to test
   * @return True, if the block's type is a waypoint's top block
   */
  public static WaypointType fromTopBlock(Block block) {
    var t = block.getType();

    for (var wt: buildableTypes) {
      Material[] col = wt.getColumn();
      Material top = col[col.length - 1];

      if (t == top) {
        return wt;
      }
    }

    return null;
  }

  /**
   * Finds a potential waypoint's top block
   * <p>
   * This gets the given player's targeted block, at a max distance of 5, and
   * checks if that block, or any block above it, qualifies as a waypoint's top
   * block, the first valid block being the one that's chosen.
   *
   * @param player The player who's looking at a waypoint's block
   * @return A waypoint's potential top block, null, if none found.
   */
  public static @Nullable Block findTopBlock(Player player) {
    var block = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);

    if (block == null) {
      return null;
    }

    return findTopBlock(block);
  }

  public static @Nullable Block findTopBlock(Block block) {
    for (int i = 0; i < highestColumn + 2; i++) {
      Block b = block.getRelative(0, i, 0);

      if (fromTopBlock(b) != null) {
        return b;
      }
    }

    return null;
  }

  public static @Nullable Pair<Block, WaypointType> findTopAndType(Block block) {
    for (int i = 0; i < highestColumn + 2; i++) {
      Block b = block.getRelative(0, i, 0);
      var wt = fromTopBlock(b);

      if (wt != null) {
        return Pair.of(b, wt);
      }
    }

    return null;
  }

  public static <T extends WaypointType> T register(String key, T type) {
    return (T) REGISTRY.register(key, type).getValue();
  }
}