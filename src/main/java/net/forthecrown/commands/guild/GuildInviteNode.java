package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Consumer;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;

public class GuildInviteNode extends GuildCommandNode {

  GuildInviteNode() {
    super("guildinvite", "invite");
  }

  @Override
  protected void writeHelpInfo(TextWriter writer, CommandSource source) {
    writer.field("invite <user>", "Invites a player to your guild");
    writer.field("invite <user> cancel", "Cancels a sent invite");
    writer.field("invite accept <guild>", "Accepts a guild join invite");
    writer.field("invite deny <guild>", "Denies a guild join invite");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)
            .executes(c -> {
              User user = getUserSender(c);
              User target = Arguments.getUser(c, "user");
              Guild guild = GuildProvider.SENDERS_GUILD.get(c);

              testPermission(
                  user, guild,
                  GuildPermission.CAN_INVITE,
                  Exceptions.NO_PERMISSION
              );

              if (user.equals(target)) {
                throw Exceptions.CANNOT_INVITE_SELF;
              }

              if (target.getGuild() != null) {
                throw Exceptions.format(
                    "{0, user} is already in a guild",
                    target
                );
              }

              if (guild.isFull()) {
                throw Exceptions.guildFull(guild);
              }

              if (guild.getInviteFor(target) != null) {
                throw Exceptions.requestAlreadySent(target);
              }

              GuildInvite invite = new GuildInvite(
                  guild.getId(), target.getUniqueId()
              );

              guild.addInvite(invite);

              guild.sendMessage(
                  Messages.requestSent(
                      target,
                      Messages.crossButton(
                          "/g invite %s cancel",
                          target.getName()
                      ).hoverEvent(Component.text("Click to cancel"))
                  )
              );
              user.playSound(Sound.UI_TOAST_OUT, 2, 1.5f);

              target.sendMessage(Messages.guildInviteTarget(guild));
              target.playSound(Sound.UI_TOAST_IN, 2, 1.3f);

              return 0;
            })

            .then(literal("cancel")
                .executes(c -> {
                  User user = getUserSender(c);
                  User target = Arguments.getUser(c, "user");
                  Guild guild = GuildProvider.SENDERS_GUILD.get(c);

                  testPermission(
                      user, guild,
                      GuildPermission.CAN_INVITE,
                      Exceptions.NO_PERMISSION
                  );

                  GuildInvite invite = guild.getInviteFor(target);

                  if (invite == null) {
                    throw Exceptions.format(
                        "{0, user} has not been invited",
                        target
                    );
                  }

                  invite.onCancel();
                  return 0;
                })
            )
        )

        .then(literal("deny")
            .then(guildArgument()
                .executes(c -> answerInvite(c, GuildInvite::onDeny))
            )
        )

        .then(literal("accept")
            .then(guildArgument()
                .executes(c -> answerInvite(c, GuildInvite::onAccept))
            )
        );
  }

  static int acceptInvite(CommandContext<CommandSource> c)
      throws CommandSyntaxException
  {
    return answerInvite(c, GuildInvite::onAccept);
  }

  static int answerInvite(CommandContext<CommandSource> c,
                          Consumer<GuildInvite> inviteConsumer
  ) throws CommandSyntaxException {
    User user = getUserSender(c);
    Guild guild = providerForArgument().get(c);

    ensureJoinable(user, guild);

    GuildInvite invite = guild.getInviteFor(user);

    if (invite == null) {
      throw Exceptions.NOT_INVITED;
    }

    inviteConsumer.accept(invite);
    return 0;
  }

  public static void ensureJoinable(User user, Guild guild)
      throws CommandSyntaxException
  {
    if (user.getGuild() != null) {
      throw Exceptions.ALREADY_IN_GUILD;
    }

    if (guild.isFull()) {
      throw Exceptions.guildFull(guild);
    }
  }
}