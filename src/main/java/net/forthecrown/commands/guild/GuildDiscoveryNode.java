package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.menu.GuildMenus;

class GuildDiscoveryNode extends GuildCommandNode {

  protected GuildDiscoveryNode() {
    super("guilddiscover", "discover");
    setAliases("discoverguilds", "guilddiscovery", "gdiscover");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Opens a menu to find guilds to join");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command.executes(c -> {
      var user = getUserSender(c);

      GuildMenus.open(
          GuildMenus.DISCOVERY_MENU,
          user,
          null
      );
      return 0;
    });
  }
}