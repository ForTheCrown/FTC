package net.forthecrown.commands.admin;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.GameModeArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcGameMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;

public class CommandGameMode extends FtcCommand {
    public CommandGameMode(){
        super("gm", Crown.inst());

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
                            FtcGameMode wrapped = FtcGameMode.wrap(gameMode);

                            if(!user.hasPermission(Permissions.GAMEMODES)){
                                if(gameMode == GameMode.CREATIVE || gameMode == GameMode.ADVENTURE) throw FtcExceptionProvider.create("You do not have permission to use this");
                            }

                            user.setGameMode(wrapped);
                            user.updateFlying();
                            user.sendMessage(adminMsg(user, wrapped));
                            return 0;
                        })

                        .then(argument("user", UserArgument.onlineUser())
                                .requires(s -> s.hasPermission(Permissions.GAMEMODES))

                                .executes(c -> {
                                    CrownUser user = UserArgument.getUser(c, "user");
                                    GameMode gameMode = c.getArgument("gamemode", GameMode.class);
                                    FtcGameMode wrapped = FtcGameMode.wrap(gameMode);

                                    user.setGameMode(wrapped);
                                    user.updateFlying();
                                    c.getSource().sendAdmin(adminMsg(user, wrapped));
                                    return 0;
                                })
                        )
                );
    }

    private Component adminMsg(CrownUser user, FtcGameMode gameMode){
        TranslatableComponent name = gameMode.title().color(NamedTextColor.GOLD);
        return Component.text("Set ")
                .color(NamedTextColor.GRAY)
                .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                .append(Component.text("'s gamemode to "))
                .append(name);
    }
}
