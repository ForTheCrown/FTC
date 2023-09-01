package net.forthecrown.core.commands.admin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.UUID;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandFile;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;

@CommandFile("commands/alts.gcn")
public class CommandAlts {

  void showInfo(
      CommandSource source,
      @Argument("user") User user
  ) throws CommandSyntaxException {
    var service = Users.getService();

    Collection<UUID> altsList = service.getAltAccounts(user.getUniqueId());
    UUID main = service.getMainAccount(user.getUniqueId());

    var writer = TextWriters.newWriter();
    writer.formattedLine("{0, user}'s alts and main data:", user);

    if (!altsList.isEmpty()) {
      writer.field("Alt accounts",
          TextJoiner.onComma()
              .add(
                  altsList.stream()
                      .map(Users::get)
                      .map(User::displayName)
              )
      );
    } else {
      writer.line("No alt accounts");
    }

    if (main != null) {
      writer.field("Main account", Users.get(main).displayName());
    } else {
      writer.formattedLine("{0, user} is not an alt", user);
    }

    source.sendMessage(writer.asComponent());
  }

  void add(
      CommandSource source,
      @Argument("alt") User alt,
      @Argument("main") User main
  ) throws CommandSyntaxException {
    var service = Users.getService();
    var targetMain = service.getMainAccount(alt.getUniqueId());

    if (targetMain != null) {
      throw Exceptions.format("{0, user} is already an alt for {1, user}", alt, targetMain);
    }

    service.setAltAccount(alt.getUniqueId(), main.getUniqueId());

    source.sendSuccess(
        Text.format("{0, user} is now an alt for {1, user}", alt, main)
    );
  }

  void remove(
      CommandSource source,
      @Argument("alt") User alt,
      @Argument("main") User main
  ) throws CommandSyntaxException {
    var service = Users.getService();
    var targetMain = service.getMainAccount(alt.getUniqueId());

    if (targetMain == null) {
      throw Exceptions.format("{0, user} is not an alt account", alt);
    }

    if (!targetMain.equals(main.getUniqueId())) {
      throw Exceptions.format("{0, user} is not an alt for {1, user}", alt, main);
    }

    service.removeAltAccount(alt.getUniqueId());

    source.sendSuccess(
        Text.format("{0, user} is no longer an alt for {1, user}", alt, main)
    );
  }

  void clear(
      CommandSource source,
      @Argument("main") User user
  ) throws CommandSyntaxException {
    var service = Users.getService();
    var altList = service.getAltAccounts(user.getUniqueId());

    if (altList.isEmpty()) {
      throw Exceptions.format("{0, user} has no alt accounts", user);
    }

    altList.forEach(service::removeAltAccount);

    source.sendSuccess(
        Text.format("Cleared {0, number} alts from {1, user}", altList.size(), user)
    );
  }
}
