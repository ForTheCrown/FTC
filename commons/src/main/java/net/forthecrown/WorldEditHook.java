package net.forthecrown;

import javax.annotation.Nullable;
import net.forthecrown.utils.math.AreaSelection;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.entity.Player;

/**
 * An abstraction of common parts of the WorldEdit API, so individual plugins don't have to depend
 * on the WorldEdit plugin for 1 or 2 features
 */
public interface WorldEditHook {

  static WorldEditHook hook() {
    return BukkitServices.loadOrThrow(WorldEditHook.class);
  }

  @Nullable
  AreaSelection getSelectedBlocks(Player player);

  @Nullable
  WorldBounds3i getPlayerSelection(Player player);
}