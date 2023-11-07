package net.forthecrown.guilds.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.menu.GuildMenus;

class GuildInfoNode extends GuildCommandNode {

  public GuildInfoNode() {
    super("guildinfo", "info", "i");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Displays info about your guild");
    factory.usage("<guild>", "Displays info about a specific guild");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .executes(c -> showInfo(c, GuildProvider.SENDERS_GUILD))

        .then(guildArgument()
            .executes(c -> showInfo(c, providerForArgument()))
        );
  }

  private int showInfo(CommandContext<CommandSource> c, GuildProvider provider)
      throws CommandSyntaxException
  {
    var guild = provider.get(c);
    var user = getUserSender(c);

    GuildMenus.open(
        GuildMenus.MAIN_MENU.getStats(),
        user, guild
    );
    return 0;
  }

}