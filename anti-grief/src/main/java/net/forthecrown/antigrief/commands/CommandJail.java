package net.forthecrown.antigrief.commands;

import static net.forthecrown.antigrief.commands.PunishmentCommand.ARGS;
import static net.forthecrown.antigrief.commands.PunishmentCommand.REASON;
import static net.forthecrown.antigrief.commands.PunishmentCommand.TIME;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import javax.annotation.Nullable;
import net.forthecrown.antigrief.GExceptions;
import net.forthecrown.antigrief.GriefPermissions;
import net.forthecrown.antigrief.JailCell;
import net.forthecrown.antigrief.PunishEntry;
import net.forthecrown.antigrief.PunishType;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.registry.Holder;
import net.forthecrown.user.User;

public class CommandJail extends FtcCommand {

  public CommandJail() {
    super("Jail");

    setPermission(GriefPermissions.PUNISH_JAIL);
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
            .then(argument("jail", AntiGriefCommands.JAIL_CELL_ARG)
                .executes(c -> punish(c, null, null))

                .then(argument("args", ARGS)
                    .executes(c -> {
                      var args = c.getArgument("args", ParsedOptions.class);
                      var length = args.getValue(TIME);

                      return punish(c, args.getValue(REASON), length);
                    })
                )
            )
        );
  }

  private int punish(CommandContext<CommandSource> c, @Nullable String reason, Duration length)
      throws CommandSyntaxException
  {
    CommandSource source = c.getSource();
    User user = Arguments.getUser(c, "user");

    if (!Punishments.canPunish(source, user)) {
      throw GExceptions.cannotPunish(user);
    }

    Holder<JailCell> cell = c.getArgument("jail", Holder.class);
    PunishEntry entry = Punishments.entry(user);

    if (entry.isPunished(PunishType.JAIL)) {
      throw GExceptions.alreadyPunished(user, PunishType.JAIL);
    }

    Punishments.handlePunish(user, source, reason, length, PunishType.JAIL, cell.getKey());
    return 0;
  }
}