package net.forthecrown.commands.punish;

import static net.forthecrown.commands.punish.PunishmentCommand.ARGS;
import static net.forthecrown.commands.punish.PunishmentCommand.REASON;
import static net.forthecrown.commands.punish.PunishmentCommand.TIME;
import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.user.User;

public class CommandJail extends FtcCommand {

  public CommandJail() {
    super("Jail");

    setPermission(Permissions.PUNISH_JAIL);
    setDescription("Jails a user");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /Jail
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user> <jail> [length=<length: time>] [reason=<reason>]")
        .addInfo("Jails the <user> in the <jail>")
        .addInfo("If the [length] is not set, the user will")
        .addInfo("be jailed forever.");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.USER)
            .then(argument("jail", RegistryArguments.JAIL_CELL)
                .executes(c -> punish(c, null, INDEFINITE_EXPIRY))

                .then(argument("args", ARGS)
                    .executes(c -> {
                      var args = c.getArgument("args", ParsedOptions.class);
                      long length = args.has(TIME)
                          ? args.getValue(TIME).toMillis()
                          : INDEFINITE_EXPIRY;

                      return punish(c, args.getValue(REASON), length);
                    })
                )
            )
        );
  }

  private int punish(CommandContext<CommandSource> c, @Nullable String reason, long length)
      throws CommandSyntaxException {
    CommandSource source = c.getSource();
    User user = Arguments.getUser(c, "user");

    if (!Punishments.canPunish(source, user)) {
      throw Exceptions.cannotPunish(user);
    }

    Holder<JailCell> cell = c.getArgument("jail", Holder.class);
    PunishEntry entry = Punishments.entry(user);

    if (entry.isPunished(PunishType.JAIL)) {
      throw Exceptions.alreadyPunished(user, PunishType.JAIL);
    }

    Punishments.handlePunish(user, source, reason, length, PunishType.JAIL, cell.getKey());
    return 0;
  }
}