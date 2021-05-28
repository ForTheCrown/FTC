package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.enums.CrownGameMode;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class CommandSpecificGameMode extends CrownCommandBuilder {
    private final CrownGameMode gameMode;
    public CommandSpecificGameMode(@NotNull String name, CrownGameMode mode, String... aliases) {
        super(name, CrownCore.inst());

        this.aliases = aliases;
        this.gameMode = mode;

        setPermission(Permissions.GAMEMODES);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> doStuff(getUserSender(c), c.getSource(), false))

                .then(argument("user", UserType.onlineUser())
                        .executes(c -> doStuff(
                                UserType.getUser(c, "user"),
                                c.getSource(),
                                true
                        ))
                );
    }

    private int doStuff(CrownUser user, CommandSource source, boolean broadcast){
        user.setGameMode(gameMode);
        user.updateFlying();

        TranslatableComponent title = gameMode.title().color(NamedTextColor.GOLD);

        if(broadcast){
            source.sendAdmin(
                    Component.text("Set ")
                            .color(NamedTextColor.GRAY)
                            .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                            .append(Component.text("'s gamemode to "))
                            .append(title)
            );
        } else {
            user.sendMessage(
                    Component.text("Set ")
                            .color(NamedTextColor.GRAY)
                            .append(Component.text("own").color(NamedTextColor.YELLOW))
                            .append(Component.text(" gamemode to "))
                            .append(title)
            );
        }
        return 0;
    }

    public static void init(){
        new CommandSpecificGameMode("survival", CrownGameMode.SURVIVAL, "gms");
        new CommandSpecificGameMode("creative", CrownGameMode.CREATIVE, "gmc");
        new CommandSpecificGameMode("spectator", CrownGameMode.SPECTATOR, "gmsp");
        new CommandSpecificGameMode("adventure", CrownGameMode.ADVENTURE, "gma");
    }
}
