package net.forthecrown.guilds.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.guilds.GuildPermissions;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.menu.GuildMenus;

public class GuildCommands {

  static final GuildCommandNode[] NODES = {
      new GuildHelpNode(),
      new GuildInfoNode(),
      new GuildListNode(),

      new GuildSetNode(),
      new GuildPermNode(),

      new GuildInviteNode(),
      new GuildKickNode(),
      new GuildJoinNode(),
      new GuildLeaveNode(),

      new GuildChunkNode(),

      new GuildChatNode(),
      new GuildInventoryNode(),

      new GuildCreateNode(),
      new GuildDeleteNode(),

      new GuildDiscoveryNode(),
      new GuildMultiplierNode(),

      new GuildChangeRankNode("guildpromote", "promote", true),
      new GuildChangeRankNode("guilddemote", "demote", false),
  };

  public static void createCommands() {
    new CommandGuild();
  }

  static class CommandGuild extends FtcCommand {

    public CommandGuild() {
      super("guild");

      setPermission(GuildPermissions.GUILD);
      setDescription("Guild command");
      setAliases("g");

      register();
    }

    @Override
    public void populateUsages(UsageFactory factory) {
      for (var n: NODES) {
        for (var name: n.getArgumentName()) {
          var prefixed = factory.withPrefix(name)
              .withPermission(n.getPermission());

          n.populateUsages(prefixed);
          break;
        }
      }
    }

    @Override
    public void createCommand(GrenadierCommand command) {
      for (var n : NODES) {
        // The help command shouldn't be created
        if (!n.getName().contains("help")) {
          n.register();
        }

        for (var name : n.getArgumentName()) {
          var literal = literal(name)
              .executes(c -> {
                StringReader reader = new StringReader(c.getInput());
                reader.setCursor(reader.getTotalLength());

                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                    .dispatcherUnknownArgument()
                    .createWithContext(reader);
              })

              .requires(n);

          n.create(literal);
          command.then(literal);
        }
      }

      command
          .executes(context -> {
            var user = getUserSender(context);

            if (Guilds.getGuild(user) == null) {
              GuildMenus.open(
                  GuildMenus.DISCOVERY_MENU,
                  user,
                  null
              );
              return 0;
            }

            GuildMenus.open(
                GuildMenus.MAIN_MENU,
                user,
                Guilds.getGuild(user)
            );
            return 0;
          });
    }
  }
}