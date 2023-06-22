package net.forthecrown.antigrief.commands;

import net.forthecrown.antigrief.GExceptions;
import net.forthecrown.antigrief.PunishEntry;
import net.forthecrown.antigrief.PunishType;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
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
            throw GExceptions.notPunished(user, type);
          }

          if (!c.getSource().hasPermission(type.getPermission())) {
            throw GExceptions.cannotPardon(type);
          }

          entry.revokePunishment(type, c.getSource().textName());
          Punishments.announcePardon(c.getSource(), user, type);

          return 0;
        })
    );
  }
}