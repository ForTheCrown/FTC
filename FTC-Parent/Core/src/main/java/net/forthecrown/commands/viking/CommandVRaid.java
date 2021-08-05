package net.forthecrown.commands.viking;

import com.mojang.brigadier.context.CommandContext;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.arguments.RaidArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.valhalla.Valhalla;
import net.forthecrown.valhalla.data.VikingRaid;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class CommandVRaid extends FtcCommand {

    public CommandVRaid() {
        super("vraid");

        setPermission(Permissions.VIKING_ADMIN);
        setAliases("raid", "vikingraid");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     *
     * Permissions used:
     * ftc.admin.vikings
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("create")
                        .then(argument("key", Valhalla.KEY_PARSER)
                                .then(argument("start_loc", PositionArgument.position())
                                        .executes(c -> {
                                            Player player = c.getSource().asPlayer();

                                            Location startingLocation = PositionArgument.getLocation(c, "start_loc");
                                            NamespacedKey key = KeyArgument.getKey(c, "key");

                                            BukkitPlayer bPlayer = BukkitAdapter.adapt(player);
                                            Region region;

                                            try {
                                                region = bPlayer.getSelection();
                                            } catch (IncompleteRegionException e) {
                                                throw FtcExceptionProvider.create("You must make a region selection for raid area");
                                            }

                                            Valhalla.getInstance().addRaid(key, startingLocation, region);

                                            c.getSource().sendAdmin("Created raid with key " + key.asString());
                                            return 0;
                                        })
                                )
                        )
                )
                .then(literal("edit")
                        .then(argument("raid", RaidArgument.raid())
                                .then(LootDataArgument.arg())
                                .then(MobDataArgument.arg())
                        )
                );
    }

    static VikingRaid get(CommandContext<CommandSource> c) {
        return c.getArgument("raid", VikingRaid.class);
    }
}