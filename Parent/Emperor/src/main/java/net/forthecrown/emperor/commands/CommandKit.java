package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.arguments.KitType;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.useables.kits.Kit;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class CommandKit extends CrownCommandBuilder {

    public CommandKit(){
        super("kit", CrownCore.inst());

        setPermission(Permissions.KIT);
        setDescription("Gives you a kit");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("kit", KitType.kit())
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            Kit kit = KitType.getKit(c, "kit");

                            if(kit.attemptItemGiving(player)){
                                player.sendMessage(
                                        Component.text("Received kit ")
                                                .color(NamedTextColor.GRAY)
                                                .append(kit.displayName().color(NamedTextColor.GOLD))
                                );
                            }
                            return 0;
                        })

                        .then(argument("user", UserType.onlineUser())
                                .requires(s -> s.hasPermission(Permissions.KIT_ADMIN))

                                .executes(c -> {
                                    CrownUser user = UserType.getUser(c, "user");
                                    Kit kit = KitType.getKit(c, "kit");

                                    if(kit.attemptItemGiving(user.getPlayer())){
                                        c.getSource().sendAdmin(
                                                Component.text("Giving kit ")
                                                        .append(kit.displayName())
                                                        .append(Component.text(" to "))
                                                        .append(user.displayName())
                                        );
                                    }
                                    return 0;
                                })
                        )
                );
    }
}