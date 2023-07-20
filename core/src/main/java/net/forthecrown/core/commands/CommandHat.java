package net.forthecrown.core.commands;

import net.forthecrown.command.Commands;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandHat extends FtcCommand {

  public CommandHat() {
    super("Hat");

    setPermission(CorePermissions.HAT);
    setDescription("Places the item in your hand on your head");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /Hat
   *
   * Permissions used:
   *
   * Main Author:
   */

  private static final Sound SOUND = Sound.sound(
      org.bukkit.Sound.ITEM_ARMOR_EQUIP_NETHERITE,
      Sound.Source.PLAYER,
      1f, 1f
  );

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          Player player = c.getSource().asPlayer();
          ItemStack held = Commands.getHeldItem(player);

          var inventory = player.getInventory();
          ItemStack helmet = inventory.getHelmet();

          inventory.setHelmet(held);
          inventory.setItemInMainHand(helmet);

          player.playSound(SOUND);
          return 0;
        });
  }
}