package net.forthecrown.scripts.commands;

import static net.forthecrown.scripts.commands.ScriptingCommand.executeScript;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;

public class CommandJs extends FtcCommand {

  public CommandJs() {
    super("js");
    setDescription("Runs JavaScript code");
    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<code>", "Runs JavaScript code");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.then(argument("code", StringArgumentType.greedyString())
        .executes(c -> {
          var code = c.getArgument("code", String.class);
          Source scriptSource = Sources.direct(code, "<command script>");
          executeScript(c.getSource(), scriptSource, false, null);

          return 0;
        })
    );
  }
}
