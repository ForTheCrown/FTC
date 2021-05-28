package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.useables.warps.Warp;
import net.forthecrown.emperor.registry.WarpRegistry;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandWarpList extends CrownCommandBuilder {
    public CommandWarpList(){
        super("warplist", CrownCore.inst());

        setPermission(Permissions.WARP);
        setAliases("warps");
        setDescription("Shows you all the warps available to you");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = c.getSource().asPlayer();

            TextComponent.Builder builder = Component.text()
                    .append(Component.text("Warps:").color(NamedTextColor.GRAY))
                    .color(NamedTextColor.GOLD);

            WarpRegistry registry = CrownCore.getWarpRegistry();
            Collection<Warp> warps = player.hasPermission(Permissions.WARP_ADMIN) ?
                    registry.getEntries() : registry.getUseableWarpsFor(player);

            for (Warp w: warps){
                builder
                        .append(Component.text(" ["))
                        .append(w.displayName())
                        .append(Component.text("]"));
            }

            player.sendMessage(builder.build());
            return 0;
        });
    }
}
