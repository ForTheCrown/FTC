package net.forthecrown.commands.admin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.Util;
import org.bukkit.entity.Player;

public class CommandGetOffset extends FtcCommand {

    public CommandGetOffset() {
        super("GetOffset");

        setPermission(Permissions.ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /GetOffset
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = c.getSource().asPlayer();
                    BukkitPlayer wePlayer = BukkitAdapter.adapt(player);

                    Region region = Util.getSelectionSafe(wePlayer);

                    BlockVector3 dif = region.getMaximumPoint().subtract(region.getMinimumPoint());
                    BlockVector3 dimensions = region.getDimensions();

                    player.sendMessage("dimensions: " + dimensions);
                    player.sendMessage("dif: " + dif);
                    player.sendMessage("distance: " + dif.length());
                    return 0;
                });
    }
}