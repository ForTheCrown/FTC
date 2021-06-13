package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.commands.arguments.KitType;
import net.forthecrown.core.commands.arguments.UserType;
import net.forthecrown.core.useables.kits.Kit;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class CommandKit extends FtcCommand {

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
                                        Component.translatable("kits.got", kit.displayName().color(NamedTextColor.GOLD))
                                                .color(NamedTextColor.GRAY)
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