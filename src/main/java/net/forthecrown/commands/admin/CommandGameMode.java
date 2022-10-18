package net.forthecrown.commands.admin;

import net.forthecrown.text.Messages;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.GameModeArgument;
import net.forthecrown.user.User;
import org.bukkit.GameMode;
import org.bukkit.permissions.Permission;

public class CommandGameMode extends FtcCommand {
    public CommandGameMode(){
        super("gm");

        setPermission(Permissions.CMD_GAMEMODE);
        setAliases("gamemode");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("gamemode", GameModeArgument.gameMode())
                        .executes(c -> {
                            User user = getUserSender(c);
                            GameMode gameMode = c.getArgument("gamemode", GameMode.class);

                            if (!user.hasPermission(gamemodePermission(gameMode))) {
                                throw Exceptions.NO_PERMISSION;
                            }

                            user.setGameMode(gameMode);
                            sendGameModeMessages(c.getSource(), user, gameMode);
                            return 0;
                        })

                        .then(argument("user", Arguments.ONLINE_USER)
                                .requires(s -> s.hasPermission(Permissions.CMD_GAMEMODE_OTHERS))

                                .executes(c -> {
                                    var source = c.getSource();
                                    User user = Arguments.getUser(c, "user");
                                    GameMode gameMode = c.getArgument("gamemode", GameMode.class);

                                    user.setGameMode(gameMode);
                                    sendGameModeMessages(source, user, gameMode);
                                    return 0;
                                })
                        )
                );
    }

    static void sendGameModeMessages(CommandSource source, User target, GameMode mode) {
        // If self, only tell sender they changed their game mode
        if (target.getName().equals(source.textName())) {
            target.sendMessage(Messages.gameModeChangedSelf(mode));
        } else {
            // If the target user cannot see the admin broadcast that
            // their game mode was changed
            if (!target.hasPermission(gamemodePermission(mode))) {
                target.sendMessage(Messages.gameModeChangedTarget(source, mode));
            }

            source.sendAdmin(Messages.gameModeChangedOther(target, mode));
        }
    }

    static Permission gamemodePermission(GameMode mode) {
        return switch (mode) {
            case CREATIVE -> Permissions.CMD_GAMEMODE_CREATIVE;
            case ADVENTURE -> Permissions.CMD_GAMEMODE_ADVENTURE;
            case SPECTATOR -> Permissions.CMD_GAMEMODE_SPECTATOR;
            case SURVIVAL -> Permissions.CMD_GAMEMODE;
        };
    }
}