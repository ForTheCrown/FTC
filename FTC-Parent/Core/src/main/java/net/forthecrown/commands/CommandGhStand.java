package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.pirates.grappling.GhBiome;
import net.forthecrown.pirates.grappling.GhLevelData;
import net.forthecrown.pirates.grappling.GhType;
import net.forthecrown.utils.math.BlockPos;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CommandGhStand extends FtcCommand {

    public CommandGhStand() {
        super("ghStand");

        setPermission(Permissions.CORE_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /ghStand <name> <dest> <type> <biome> <selector mat> [nextHooks] [nextDistance]
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("name", StringArgumentType.word())
                        .then(argument("dest", PositionArgument.blockPos())
                                .then(argument("type", EnumArgument.of(GhType.class))
                                        .then(argument("biome", EnumArgument.of(GhBiome.class))
                                                .then(argument("mat", EnumArgument.of(Material.class))
                                                        .executes(c -> create(c, null, null))

                                                        .then(argument("nextHooks", IntegerArgumentType.integer(1, 100))
                                                                .executes(c -> create(c, "nextHooks", null))

                                                                .then(argument("nextDist", IntegerArgumentType.integer(1, 500))
                                                                        .executes(c -> create(c, "nextHooks", "nextDist"))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                );
    }

    private int create(CommandContext<CommandSource> c, String nextHooksStr, String nextDistStr){
        String name = c.getArgument("name", String.class);
        Location dest = PositionArgument.getLocation(c, "dest");
        GhType type = c.getArgument("type", GhType.class);
        GhBiome biome = c.getArgument("biome", GhBiome.class);
        Material selectorMat = c.getArgument("mat", Material.class);

        int nextHooks = -1;
        if(nextHooksStr != null) nextHooks = c.getArgument(nextHooksStr, Integer.class);

        int nextDistance = -1;
        if(nextDistStr != null) nextDistance = c.getArgument(nextDistStr, Integer.class);

        return spawn(name, c.getSource(), dest, type, biome, selectorMat, nextHooks, nextDistance);
    }

    private int spawn(String name, CommandSource source, Location dest, GhType type, GhBiome biome, Material material, int nextHooks, int nextDist){
        GhLevelData data = new GhLevelData(name, biome, material, BlockPos.of(dest), nextHooks, nextDist, type);
        Pirates.getParkour().getData().set(name, data);

        Location spawnLoc = source.getLocation();
        spawnLoc.getWorld().spawn(spawnLoc, ArmorStand.class, stand -> {
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setCanMove(false);

            stand.setPersistent(true);
            stand.setInvulnerable(true);
            stand.setInvisible(true);

            stand.getPersistentDataContainer().set(Pirates.GH_STAND_KEY, PersistentDataType.STRING, name);
            stand.getEquipment().setHelmet(new ItemStack(Material.GLOWSTONE));
        });

        source.sendAdmin("Spawning stand");
        return 0;
    }
}