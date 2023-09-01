package net.forthecrown.guilds.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Consumer;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildExceptions;
import net.forthecrown.guilds.GuildMessages;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;

public class GuildInviteNode extends GuildCommandNode {

  GuildInviteNode() {
    super("guildinvite", "invite");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user>", "Invites a player to your guild");
    factory.usage("<user> cancel", "Cancels a sent invite");
    factory.usage("accept <guild>", "Accepts a guild join invite");
    factory.usage("deny <guild>", "Denies a guild join invite");
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
                throw Exceptions.create("Cannot invite yourself");
              }

              if (Guilds.getGuild(target) != null) {
                throw Exceptions.format("{0, user} is already in a guild", target);
              }

              if (guild.isFull()) {
                throw GuildExceptions.guildFull(guild);
              }

              if (guild.getInviteFor(target) != null) {
                throw Exceptions.requestAlreadySent(target);
              }

              GuildInvite invite = new GuildInvite(
                  guild.getId(), target.getUniqueId()
              );

              guild.addInvite(invite);

              guild.announce(
                  Messages.requestSent(
                      target,
                      Messages.crossButton(
                          "/g invite %s cancel",
                          target.getName()
                      ).hoverEvent(Component.text("Click to cancel"))
                  )
              );
              user.playSound(Sound.UI_TOAST_OUT, 2, 1.5f);

              target.sendMessage(GuildMessages.guildInviteTarget(guild));
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
      throw Exceptions.create("You have not been invited");
    }

    inviteConsumer.accept(invite);
    return 0;
  }

  public static void ensureJoinable(User user, Guild guild)
      throws CommandSyntaxException
  {
    if (Guilds.getGuild(user) != null) {
      throw GuildExceptions.ALREADY_IN_GUILD;
    }

    if (guild.isFull()) {
      throw GuildExceptions.guildFull(guild);
    }
  }
}