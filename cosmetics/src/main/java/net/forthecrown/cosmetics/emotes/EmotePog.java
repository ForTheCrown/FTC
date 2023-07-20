package net.forthecrown.cosmetics.emotes;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.text.ChannelledMessage;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.ChannelledMessage.MessageHandler;
import net.forthecrown.text.Text;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.user.User;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EmotePog extends FtcCommand {

  public EmotePog() {
    super("pog");

    setAliases("pgo", "poggers", "pogchamp", "pogo");
    setPermission(EmotePermissions.EMOTE_POG);

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          var user = getUserSender(c);

          pog(user, null);
          return 0;
        })

        .then(argument("message", Arguments.MESSAGE)
            .executes(c -> {
              var user = getUserSender(c);
              var msg = Arguments.getMessage(c, "message");

              pog(user, msg);
              return 0;
            })
        );
  }

  void pog(User user, ViewerAwareMessage message) throws CommandSyntaxException {
    Cooldown.testAndThrow(user, getName(), 3 * 20);

    ViewerAwareMessage nonNullMessage = message == null
        ? ViewerAwareMessage.wrap(Component.empty())
        : message;

    ChannelledMessage.create(nonNullMessage)
        .setHandler(MessageHandler.EMPTY_IF_NOT_VIEWING)
        .setSource(user)
        .setBroadcast()
        .setAnnouncement(false)

        .setRenderer((viewer, baseMessage) -> {
          Component displayName = user.displayName(viewer);

          var builder = text()
              .color(NamedTextColor.GRAY)
              .append(displayName.color(NamedTextColor.YELLOW))
              .append(text(" pogged"));

          if (Text.isEmpty(baseMessage)) {
            builder.append(text("!"));
          } else {
            builder.append(text(": "))
                .append(baseMessage.colorIfAbsent(NamedTextColor.WHITE));
          }

          return builder.build();
        });
  }
}