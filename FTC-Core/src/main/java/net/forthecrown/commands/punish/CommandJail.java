package net.forthecrown.commands.punish;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.*;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;

import javax.annotation.Nullable;

import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

public class CommandJail extends FtcCommand {

    public CommandJail() {
        super("Jail");

        setPermission(Permissions.HELPER);
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
                .then(argument("user", UserArgument.user())
                        .then(argument("jail", RegistryArguments.jailCell())
                                .suggests((context, builder) ->
                                        FtcSuggestionProvider.suggestKeysNoNamespace(builder, Registries.JAILS.keySet())
                                )

                                .executes(c -> punish(c, null, INDEFINITE_EXPIRY))

                                .then(argument("time", TimeArgument.time())
                                        .executes(c -> punish(c, null, TimeArgument.getMillis(c, "time")))

                                        .then(argument("reason", StringArgumentType.greedyString())
                                                .executes(c -> punish(
                                                        c,
                                                        c.getArgument("reason", String.class),
                                                        TimeArgument.getMillis(c, "time")
                                                ))
                                        )
                                )
                        )
                );
    }

    private int punish(CommandContext<CommandSource> c, @Nullable String reason, long length) throws CommandSyntaxException {
        CommandSource source = c.getSource();
        CrownUser user = UserArgument.getUser(c, "user");

        if (!Punishments.canPunish(source, user)) {
            throw FtcExceptionProvider.create("Cannot punish " + user.getName());
        }

        JailCell cell = c.getArgument("jail", JailCell.class);

        PunishEntry entry = Punishments.entry(user);

        if(entry.isPunished(PunishType.JAIL)) {
            throw FtcExceptionProvider.create(user.getNickOrName() + " has already been jailed");
        }

        Punishments.handlePunish(user, source, reason, length, PunishType.JAIL, cell.key().asString());
        return 0;
    }
}