package net.forthecrown.cosmetics.commands;

import net.forthecrown.Permissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.cosmetics.menu.CosmeticMenus;
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

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      User u = getUserSender(c);
      CosmeticMenus.open(u);
      return 0;
    });
  }
}