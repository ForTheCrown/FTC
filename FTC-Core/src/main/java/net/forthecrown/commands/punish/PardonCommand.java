package net.forthecrown.commands.punish;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import org.bukkit.permissions.Permission;

public class PardonCommand extends FtcCommand {
    private final PunishType type;

    PardonCommand(String name, PunishType type, Permission permission, String... aliases) {
        super(name);
        this.type = type;

        setAliases(aliases);
        setPermission(permission);

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("user", UserArgument.user())
                .executes(c -> {
                    CrownUser user = UserArgument.getUser(c, "user");
                    PunishEntry entry = Punishments.entry(user);

                    if (!entry.isPunished(type)) {
                       throw FtcExceptionProvider.create(user.getNickOrName() + " is not " + type.presentableName());
                    }

                    entry.revokePunishment(type);
                    Punishments.announcePardon(c.getSource(), user, type);

                    return 0;
                })
        );
    }
}