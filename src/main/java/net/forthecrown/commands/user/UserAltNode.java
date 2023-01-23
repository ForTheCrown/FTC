package net.forthecrown.commands.user;

import com.mojang.brigadier.builder.ArgumentBuilder;
import java.util.List;
import java.util.UUID;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.forthecrown.utils.text.writer.TextWriters;

public class UserAltNode extends UserCommandNode {

  public UserAltNode() {
    super("user_alts", "alts");
  }

  @Override
  void createUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Lists a <user>'s alt accounts and main account,")
        .addInfo("if they have them");

    factory.usage("add <alt>")
        .addInfo("Adds a <target> as an alt for a <user>");

    factory.usage("remove <alt>")
        .addInfo("Removes a <target> as an alt for a <user>");

    factory.usage("clear")
        .addInfo("Clears all alt accounts that belong to a <user>");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(
      T command,
      UserProvider provider
  ) {
    command
        .executes(c -> {
          var user = provider.get(c);
          var alts = UserManager.get().getAlts();

          List<UUID> altsList = alts.getAlts(user.getUniqueId());
          UUID main = alts.getMain(user.getUniqueId());

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

          c.getSource().sendMessage(writer.asComponent());
          return 0;
        })

        .then(literal("add")
            .then(argument("target", Arguments.USER)
                .executes(c -> {
                  var user = provider.get(c);
                  var alts = UserManager.get().getAlts();
                  var target = Arguments.getUser(c, "target");

                  var targetMain = alts.getMain(target.getUniqueId());

                  if (targetMain != null) {
                    throw Exceptions.format(
                        "{0, user} is already an alt for {1, user}",
                        target, targetMain
                    );
                  }

                  alts.addEntry(target.getUniqueId(), user.getUniqueId());

                  c.getSource().sendAdmin(
                      Text.format("{0, user} is now an alt for {1, user}",
                          target, user
                      )
                  );
                  return 0;
                })
            )
        )

        .then(literal("remove")
            .then(argument("target", Arguments.USER)
                .executes(c -> {
                  var user = provider.get(c);
                  var alts = UserManager.get().getAlts();
                  var target = Arguments.getUser(c, "target");

                  var targetMain = alts.getMain(target.getUniqueId());

                  if (targetMain == null) {
                    throw Exceptions.format("{0, user} is not an alt account",
                        target
                    );
                  }

                  if (!targetMain.equals(user.getUniqueId())) {
                    throw Exceptions.format(
                        "{0, user} is not an alt for {1, user}",
                        target, user
                    );
                  }

                  alts.removeEntry(target.getUniqueId());

                  c.getSource().sendAdmin(
                      Text.format("{0, user} is no longer an alt for {1, user}",
                          target, user
                      )
                  );
                  return 0;
                })
            )
        )

        .then(literal("clear")
            .executes(c -> {
              var user = provider.get(c);
              var alts = UserManager.get().getAlts();

              var altList = alts.getAlts(user.getUniqueId());

              if (altList.isEmpty()) {
                throw Exceptions.format("{0, user} has no alt accounts", user);
              }

              altList.forEach(alts::removeEntry);

              c.getSource().sendAdmin(
                  Text.format("Cleared {0, number} alts from {1, user}",
                      altList.size(),
                      user
                  )
              );
              return 0;
            })
        );
  }
}