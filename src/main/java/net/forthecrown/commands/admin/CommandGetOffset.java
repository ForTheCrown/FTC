package net.forthecrown.commands.admin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.utils.Util;
import org.bukkit.entity.Player;

public class CommandGetOffset extends FtcCommand {

  public CommandGetOffset() {
    super("GetOffset");

    setPermission(Permissions.ADMIN);
    setDescription("Gets the offset between 2 selected points");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /GetOffset
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          Player player = c.getSource().asPlayer();
          BukkitPlayer wePlayer = BukkitAdapter.adapt(player);

          Region region = Util.getSelectionSafe(wePlayer);

          BlockVector3 dif = region.getMaximumPoint().subtract(region.getMinimumPoint());
          BlockVector3 dimensions = region.getDimensions();

          player.sendMessage("dimensions: " + dimensions);
          player.sendMessage("dif: " + dif);
          player.sendMessage("distance: " + dif.length());
          return 0;
        });
  }
}