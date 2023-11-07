package net.forthecrown.core.commands.admin;

import net.forthecrown.Permissions;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.entity.Player;
import org.spongepowered.math.vector.Vector3i;

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
          WorldBounds3i region = WorldBounds3i.ofPlayerSelection(player);

          if (region == null) {
            throw Exceptions.NO_REGION_SELECTION;
          }

          Vector3i dif = region.size();
          Vector3i dimensions = region.dimensions();

          player.sendMessage("dimensions: " + dimensions);
          player.sendMessage("dif: " + dif);
          player.sendMessage("distance: " + dif.length());

          return 0;
        });
  }
}