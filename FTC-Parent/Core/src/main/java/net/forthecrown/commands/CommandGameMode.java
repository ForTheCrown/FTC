package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.enums.CrownGameMode;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.GameModeArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;

public class CommandGameMode extends FtcCommand {
    public CommandGameMode(){
        super("gm", CrownCore.inst());

        setPermission(Permissions.HELPER);
        setAliases("gamemode");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("gamemode", GameModeArgument.gameMode())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            GameMode gameMode = c.getArgument("gamemode", GameMode.class);

                            if(!user.hasPermission(Permissions.GAMEMODES)){
                                if(gameMode == GameMode.CREATIVE || gameMode == GameMode.ADVENTURE) throw FtcExceptionProvider.create("You do not have permission to use this");
                            }

                            user.setGameMode(CrownGameMode.wrap(gameMode));
                            user.updateFlying();
                            user.sendMessage(adminMsg(user, gameMode));
                            return 0;
                        })

                        .then(argument("user", UserType.onlineUser())
                                .requires(s -> s.hasPermission(Permissions.GAMEMODES))

                                .executes(c -> {
                                    CrownUser user = UserType.getUser(c, "user");
                                    GameMode gameMode = c.getArgument("gamemode", GameMode.class);

                                    user.getPlayer().setGameMode(gameMode);
                                    user.updateFlying();
                                    c.getSource().sendAdmin(adminMsg(user, gameMode));
                                    return 0;
                                })
                        )
                );
    }

    private Component adminMsg(CrownUser user, GameMode gameMode){
        TranslatableComponent name = CrownGameMode.wrap(gameMode).title().color(NamedTextColor.GOLD);
        return Component.text("Set ")
                .color(NamedTextColor.GRAY)
                .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                .append(Component.text("'s gamemode to "))
                .append(name);
    }
}
