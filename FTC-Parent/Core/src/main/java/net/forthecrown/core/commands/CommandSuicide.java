package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class CommandSuicide extends FtcCommand {
    public CommandSuicide(){
        super("suicide", CrownCore.inst());

        setPermission(Permissions.DONATOR_2);
        setDescription("Commits suicide D:");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = c.getSource().asPlayer();

                    player.setHealth(0);
                    player.sendMessage(
                            Component.translatable("commands.suicide")
                                    .color(NamedTextColor.GRAY)
                    );
                    return 0;
                });
    }
}
