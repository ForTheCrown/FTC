package net.forthecrown.commands;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.useables.kits.Kit;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandKitList extends FtcCommand {
    public CommandKitList(){
        super("kitlist", ForTheCrown.inst());

        setPermission(Permissions.KIT);
        setAliases("kits");
        setDescription("Shows you all the kits available to you");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = getPlayerSender(c);

                    TextComponent.Builder builder = Component.text()
                            .append(Component.translatable("kits.kits").color(NamedTextColor.GRAY))
                            .append(Component.text(":").color(NamedTextColor.GRAY))
                            .color(NamedTextColor.GOLD);

                    Collection<Kit> kits = c.getSource().hasPermission(Permissions.KIT_ADMIN) ?
                            ForTheCrown.getKitRegistry().values() : ForTheCrown.getKitRegistry().getUsableFor(player);

                    for (Kit k: kits){
                        Component name = Component.text(" [")
                                .append(k.displayName())
                                .append(Component.text("]"));

                        builder.append(name);
                    }

                    player.sendMessage(builder.build());
                    return 0;
                });
    }
}
