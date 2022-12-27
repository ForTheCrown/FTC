package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;

class GuildChatNode extends GuildCommandNode {

  GuildChatNode() {
    super("guildchat", "chat", "c");
    setAliases("gc");
  }

  @Override
  protected void writeHelpInfo(TextWriter writer, CommandSource source) {
    writer.field("chat <message>", "Sends a chat into the guild chat");

    if (source.hasPermission(Permissions.GUILD_ADMIN)) {
      writer.field("chat <guild> <message>", "Sends a message into a guild's chat");
    }
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .then(argument("message", Arguments.MESSAGE)
            .executes(c -> chat(c, GuildProvider.SENDERS_GUILD))
        )

        .then(guildArgument()
            .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

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
    Component message = Arguments.getMessage(c, "message");

    guild.chat(user, message);
    return 0;
  }
}