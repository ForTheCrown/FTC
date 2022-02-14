package net.forthecrown.commands.admin;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
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

                    Region region;

                    try {
                        region = wePlayer.getSession().getSelection();
                    } catch (IncompleteRegionException e) {
                        throw FtcExceptionProvider.create("No WorldEdit selection found");
                    }

                    BlockVector3 dif = region.getMaximumPoint().subtract(region.getMinimumPoint());
                    BlockVector3 dimensions = region.getDimensions();

                    player.sendMessage("dimensions: " + dimensions);
                    player.sendMessage("dif: " + dif);
                    player.sendMessage("distance: " + dif.length());

                    return 0;
                });
    }
}