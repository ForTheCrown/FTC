package net.forthecrown.core.commands;

import net.forthecrown.FtcServer;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandSettings extends FtcCommand {

  public CommandSettings() {
    super("settings");

    setAliases("options", "preferences");
    setDescription("Opens the settings book");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Shows some basic info about a user.
   *
   * Valid usages of command:
   * - /settings
   * - /options
   *
   * Author: Wout
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      FtcServer server = FtcServer.server();
      User user = getUserSender(c);
      server.getGlobalSettingsBook().open(user, user);
      return 0;
    });
  }
}