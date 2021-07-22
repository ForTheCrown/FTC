package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.pirates.grappling.GhBiome;
import net.forthecrown.pirates.grappling.GhLevelData;
import net.forthecrown.pirates.grappling.GhType;
import net.forthecrown.utils.math.BlockPos;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class CommandGhStand extends FtcCommand {

    public CommandGhStand() {
        super("ghstand");

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
     * ftc.commands.ghstand
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("exit")
                        .then(argument("name", StringArgumentType.word())
                                .suggests((c, b) -> CompletionProvider.suggestMatching(b, Pirates.getParkour().keySet()))

                                .executes(c -> {
                                    Player player = c.getSource().asPlayer();

                                    GhLevelData data = Pirates.getParkour().byName(c.getArgument("name", String.class));
                                    if(data == null) throw FtcExceptionProvider.create("Unknown Level");

                                    String name = data.getName();

                                    player.getWorld().spawn(player.getLocation(), ArmorStand.class, stand -> {
                                        stand.setInvisible(true);
                                        stand.setInvulnerable(true);

                                        stand.getPersistentDataContainer().set(Pirates.GH_STAND_KEY, PersistentDataType.STRING, name);
                                        stand.getEquipment().setHelmet(new ItemStack(Material.GLOWSTONE, 1));

                                        stand.setCanMove(false);
                                        stand.setGravity(false);
                                        stand.setRemoveWhenFarAway(false);
                                        stand.setBasePlate(false);
                                    });

                                    c.getSource().sendAdmin("Placed exit stand");
                                    return 0;
                                })
                        )
                )

                .then(argument("name", StringArgumentType.word())
                        .then(argument("start", PositionArgument.blockPos())
                                .then(argument("biome", EnumArgument.of(GhBiome.class))
                                        .executes(c -> doStuff(c, null, null, null, null))

                                        .then(argument("type", EnumArgument.of(GhType.class))
                                                .executes(c -> doStuff(c, "type", null, null, null))

                                                .then(argument("next", StringArgumentType.word())
                                                        .suggests((c, b) -> CompletionProvider.suggestMatching(b, Pirates.getParkour().keySet()))
                                                        
                                                        .executes(c -> doStuff(c, "type", null, null, "next"))

                                                        .then(argument("hooks", IntegerArgumentType.integer(1))
                                                                .executes(c -> doStuff(c, "type", "hooks", null, "next"))

                                                                .then(argument("distance", IntegerArgumentType.integer(1))
                                                                        .executes(c -> doStuff(c, "type", "hooks", "distance", "next"))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                );
    }

    private int doStuff(CommandContext<CommandSource> c, @Nullable String typeStr, @Nullable String hks, @Nullable String dist, @Nullable String nxt) throws CommandSyntaxException {
        String name = c.getArgument("name", String.class);
        BlockPos start = BlockPos.of(PositionArgument.getLocation(c, "start"));
        GhBiome biome = c.getArgument("biome", GhBiome.class);
        GhType type = typeStr == null ? GhType.NORMAL : c.getArgument(typeStr, GhType.class);

        int hooks = hks == null ? -1 : c.getArgument(hks, Integer.class);
        int distance = dist == null ? -1 : c.getArgument(dist, Integer.class);

        String next = nxt == null ? null : c.getArgument(nxt, String.class);

        GhLevelData data = new GhLevelData(name, start, next, hooks, distance, biome, type);
        Pirates.getParkour().add(data);

        c.getSource().sendAdmin("Created stand with name " + name);
        return 0;
    }
}