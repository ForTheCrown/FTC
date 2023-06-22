package net.forthecrown.cosmetics.emotes;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.text.ChannelledMessage;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;

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

  void pog(User user, Component message) throws CommandSyntaxException {
    Cooldown.testAndThrow(user, getName(), 3 * 20);
    var formatted = Text.format("&e{0, user} &7Pogged", user);

    if (message != null) {
      formatted = formatted.append(Component.text(": ")).append(message);
    } else {
      formatted = formatted.append(Component.text("!"));
    }

    ChannelledMessage.announce(user.getPlayer(), formatted);
  }
}