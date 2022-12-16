package net.forthecrown.commands.admin;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import org.bukkit.GameMode;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import static net.forthecrown.commands.admin.CommandGameMode.sendGameModeMessages;

public class CommandSpecificGameMode extends FtcCommand {
    private final GameMode gameMode;

    private CommandSpecificGameMode(@NotNull String name,
                                    Permission permission,
                                    GameMode mode,
                                    String... aliases
    ) {
        super(name);

        this.aliases = aliases;
        this.gameMode = mode;

        setPermission(permission);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> setGameMode(getUserSender(c), c.getSource()))

                .then(argument("user", Arguments.ONLINE_USER)
                        .requires(s -> s.hasPermission(Permissions.CMD_GAMEMODE_OTHERS))

                        .executes(c -> setGameMode(
                                Arguments.getUser(c, "user"),
                                c.getSource()
                        ))
                );
    }

    private int setGameMode(User user, CommandSource source) {
        user.setGameMode(gameMode);
        sendGameModeMessages(source, user, gameMode);

        return 0;
    }

    public static void createCommands() {
        new CommandSpecificGameMode("survival", Permissions.CMD_GAMEMODE, GameMode.SURVIVAL, "gms");
        new CommandSpecificGameMode("creative", Permissions.CMD_GAMEMODE_CREATIVE, GameMode.CREATIVE, "gmc");
        new CommandSpecificGameMode("spectator", Permissions.CMD_GAMEMODE_SPECTATOR, GameMode.SPECTATOR, "gmsp");
        new CommandSpecificGameMode("adventure", Permissions.CMD_GAMEMODE_ADVENTURE, GameMode.ADVENTURE, "gma");
    }
}