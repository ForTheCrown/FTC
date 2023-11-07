package net.forthecrown.guilds.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.Bukkit;

class GuildHelpNode extends GuildCommandNode {

  public GuildHelpNode() {
    super("guildhelp", "help", "?");
    setAliases("ghelp");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("").addInfo("Shows help information");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command.executes(c -> {
      Bukkit.dispatchCommand(c.getSource().asBukkit(), "help guild 1 15");
      return 0;
    });
  }
}