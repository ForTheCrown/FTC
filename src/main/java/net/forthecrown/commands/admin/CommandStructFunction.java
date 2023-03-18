package net.forthecrown.commands.admin;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.structure.FunctionInfo;

public class CommandStructFunction extends FtcCommand {

  public static final String COMMAND_NAME = "StructFunction";

  public CommandStructFunction() {
    super(COMMAND_NAME);
    setDescription("Ignore this command");
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /StructFunction
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("args", FunctionInfo.PARSER));
  }
}