package net.forthecrown.commands.punish;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class PardonCommand extends FtcCommand {

  private final PunishType type;

  PardonCommand(String name, PunishType type, String... aliases) {
    super(name);
    this.type = type;

    setAliases(aliases);
    setPermission(type.getPermission());
    setDescription("Pardons a user, if they've been " + type.nameEndingED());

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user>", "Pardons a <user>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.then(argument("user", Arguments.USER)
        .executes(c -> {
          User user = Arguments.getUser(c, "user");
          PunishEntry entry = Punishments.entry(user);

          if (!entry.isPunished(type)) {
            throw Exceptions.notPunished(user, type);
          }

          if (!c.getSource().hasPermission(type.getPermission())) {
            throw Exceptions.cannotPardon(type);
          }

          entry.revokePunishment(type, c.getSource().textName());
          Punishments.announcePardon(c.getSource(), user, type);

          return 0;
        })
    );
  }
}