package net.forthecrown.commands.punish;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;

public class PardonCommand extends FtcCommand {
    private final PunishType type;

    PardonCommand(String name, PunishType type, String... aliases) {
        super(name);
        this.type = type;

        setAliases(aliases);
        setPermission(type.getPermission());

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("user", UserArgument.user())
                .executes(c -> {
                    CrownUser user = UserArgument.getUser(c, "user");
                    PunishEntry entry = Punishments.entry(user);

                    if (!entry.isPunished(type)) {
                       throw FtcExceptionProvider.create(user.getNickOrName() + " is not " + type.nameEndingED());
                    }

                    if (!c.getSource().hasPermission(type.getPermission())) {
                       throw FtcExceptionProvider.create("You do not have enough permissions to pardon a " + type.presentableName());
                    }

                    entry.revokePunishment(type);
                    Punishments.announcePardon(c.getSource(), user, type);

                    return 0;
                })
        );
    }
}