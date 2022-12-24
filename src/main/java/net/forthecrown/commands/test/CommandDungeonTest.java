package net.forthecrown.commands.test;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.dungeons.level.generator.TreeGenerator;
import net.forthecrown.dungeons.level.generator.TreeGeneratorConfig;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandDungeonTest extends FtcCommand {

  public CommandDungeonTest() {
    super("DungeonTest");
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /DungeonTest
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  protected void createCommand(BrigadierCommand command) {
    command
        .executes(c -> {
            TreeGenerator.generateAsync(TreeGeneratorConfig.defaultConfig())
                .whenComplete((level, throwable) -> {
                    c.getSource().sendMessage("Generated");
                });

            return 0;
        });
  }
}