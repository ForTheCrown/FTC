package net.forthecrown.commands.punish;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.user.User;

import javax.annotation.Nullable;

import static net.forthecrown.commands.punish.PunishmentCommand.*;
import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

public class CommandJail extends FtcCommand {

    public CommandJail() {
        super("Jail");

        setPermission(Permissions.PUNISH_JAIL);
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
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.USER)
                        .then(argument("jail", RegistryArguments.JAIL_CELL)
                                .executes(c -> punish(c, null, INDEFINITE_EXPIRY))

                                .then(argument("args", ARGS)
                                        .executes(c -> {
                                            var args = c.getArgument("args", ParsedArgs.class);
                                            return punish(c, args.get(REASON), args.get(TIME));
                                        })
                                )
                        )
                );
    }

    private int punish(CommandContext<CommandSource> c, @Nullable String reason, long length) throws CommandSyntaxException {
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