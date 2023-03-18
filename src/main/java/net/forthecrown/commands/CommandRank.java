package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.inventory.RankMenu;

public class CommandRank extends FtcCommand {

  public CommandRank() {
    super("rank");

    setAliases("ranks");
    setDescription("Allows you to set your rank");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Gives the executor the RankGUI and allows them to switch ranks
   *
   *
   * Valid usages of command:
   * - /rank
   * - /ranks
   *
   * Author: Wout
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Opens the rank menu");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      RankMenu.getInstance().open(getUserSender(c));

      return 0;
    });
  }
}