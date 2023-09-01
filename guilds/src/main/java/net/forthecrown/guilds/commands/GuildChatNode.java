package net.forthecrown.guilds.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildPermissions;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.user.User;

class GuildChatNode extends GuildCommandNode {

  GuildChatNode() {
    super("guildchat", "chat", "c");
    setAliases("gc");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<message>")
        .addInfo("Sends a message into the guild chat");

    factory.usage("<guild> <message>")
        .setPermission(GuildPermissions.GUILD_ADMIN)
        .addInfo("Sends a message into a guild's chat");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .then(argument("message", Arguments.MESSAGE)
            .executes(c -> chat(c, GuildProvider.SENDERS_GUILD))
        )

        .then(guildArgument()
            .requires(source -> source.hasPermission(GuildPermissions.GUILD_ADMIN))

            .then(argument("message", Arguments.MESSAGE)
                .executes(c -> chat(c, providerForArgument()))
            )
        );
  }

  private int chat(CommandContext<CommandSource> c,
                   GuildProvider provider
  ) throws CommandSyntaxException {
    User user = getUserSender(c);
    Guild guild = provider.get(c);
    PlayerMessage message = Arguments.getPlayerMessage(c, "message");

    guild.chat(user, message);
    return 0;
  }
}