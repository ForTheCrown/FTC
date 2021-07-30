package net.forthecrown.commands;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.useables.warps.Warp;
import net.forthecrown.registry.WarpRegistry;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandWarpList extends FtcCommand {
    public CommandWarpList(){
        super("warplist", ForTheCrown.inst());

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
                    .append(Component.translatable("warps.warps").color(NamedTextColor.GRAY))
                    .append(Component.text(":").color(NamedTextColor.GRAY))
                    .color(NamedTextColor.GOLD);

            WarpRegistry registry = ForTheCrown.getWarpRegistry();
            Collection<Warp> warps = player.hasPermission(Permissions.WARP_ADMIN) ?
                    registry.values() : registry.getUsableFor(player);

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
