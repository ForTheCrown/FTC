package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.cosmetics.CosmeticMenus;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandCosmetics extends FtcCommand {

  public CommandCosmetics() {
    super("cosmetics");

    setDescription("Opens the cosmetics menu");
    setPermission(Permissions.DEFAULT);
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Opens the Cosmetics menu
   *
   *
   * Valid usages of command:
   * - /cosmetics
   *
   * Author: Wout
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      User u = getUserSender(c);
      CosmeticMenus.MAIN.open(u);
      return 0;
    });
  }
}