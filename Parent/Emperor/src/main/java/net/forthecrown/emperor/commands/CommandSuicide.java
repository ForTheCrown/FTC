package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class CommandSuicide extends CrownCommandBuilder {
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
                            Component.text("Committing suicide :(")
                                    .color(NamedTextColor.GRAY)
                    );
                    return 0;
                });
    }
}
