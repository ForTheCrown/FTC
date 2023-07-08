package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.Permissions;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.kyori.adventure.text.Component;

public class CommandHelp extends FtcCommand {

  private static final int DEF_PAGE_SIZE = 10;

  public CommandHelp() {
    super("Help");

    setPermission(Permissions.HELP);
    setDescription("Displays help information");
    setAliases("?");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Displays all help info");

    factory.usage("<topic> [<page: number(1..)>] [<page size: number(5..20)>]")
        .addInfo("Queries information for a specific topic.")
        .addInfo("[page] optionally displays the specific page of information");

    factory.usage("all [<page: number(1..)>] [<page size: number(5..20)>]")
        .addInfo("Displays a specific page of all help info.");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> help(c, false, false, false))

        .then(argument("string", new HelpArgument())
            .executes(c -> help(c, true, false, false))

            .then(argument("page", IntegerArgumentType.integer(1))
                .executes(c -> help(c, true, true, false))

                .then(argument("pageSize", IntegerArgumentType.integer(5, 20))
                    .executes(c -> help(c, true, true, true))
                )
            )
        );
  }

  private int help(CommandContext<CommandSource> c,
      boolean inputGiven,
      boolean pageGiven,
      boolean sizeGiven
  ) throws CommandSyntaxException {
    int page = 0;
    int pageSize = DEF_PAGE_SIZE;
    String input = "";

    if (inputGiven) {
      input = c.getArgument("string", String.class);
    }

    if (pageGiven) {
      page = c.getArgument("page", Integer.class) - 1;
    }

    if (sizeGiven) {
      pageSize = c.getArgument("pageSize", Integer.class);
    }

    Component component = FtcHelpList.helpList().query(c.getSource(), input, page, pageSize);

    if (component == null) {
      throw Exceptions.NOTHING_TO_LIST;
    }

    c.getSource().sendMessage(component);
    return 0;
  }
}
