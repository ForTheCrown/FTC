package net.forthecrown.core;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.WorldEditHook;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.entity.Player;

public class WorldEditHookImpl implements WorldEditHook {

  @Override
  public WorldBounds3i getPlayerSelection(Player player) {
    com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);

    try {
      Region selection = wePlayer.getSession().getSelection();
      CuboidRegion cube = selection.getBoundingBox();

      return new WorldBounds3i(
          BukkitAdapter.adapt(selection.getWorld()),
          cube.getMinimumX(),
          cube.getMinimumY(),
          cube.getMinimumZ(),
          cube.getMaximumX(),
          cube.getMaximumY(),
          cube.getMaximumZ()
      );

    } catch (IncompleteRegionException exc) {
      return null;
    }
  }
}
