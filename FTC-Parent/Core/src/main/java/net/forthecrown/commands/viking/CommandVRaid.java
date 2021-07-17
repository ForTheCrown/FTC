package net.forthecrown.commands.viking;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.commands.arguments.RaidType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.grenadier.types.LootTableArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.utils.math.BlockPos;
import net.forthecrown.valhalla.RaidGenerationContext;
import net.forthecrown.valhalla.RaidUtil;
import net.forthecrown.valhalla.Valhalla;
import net.forthecrown.valhalla.data.ChestGroup;
import net.forthecrown.valhalla.data.LootData;
import net.forthecrown.valhalla.data.VikingRaid;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootTable;

import java.util.Map;

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
                                            Region region = bPlayer.getSelection();

                                            if(region == null) throw FtcExceptionProvider.create("You must make a region selection for raid area");

                                            Valhalla.getInstance().addRaid(key, startingLocation, region);

                                            c.getSource().sendAdmin("Created raid with key " + key.asString());
                                            return 0;
                                        })
                                )
                        )
                )
                .then(literal("edit")
                        .then(argument("raid", RaidType.raid())
                                .then(lootDataArgument())
                        )
                );
    }

    private VikingRaid get(CommandContext<CommandSource> c) {
        return c.getArgument("raid", VikingRaid.class);
    }

    private void checkHasLoot(VikingRaid raid) throws CommandSyntaxException {
        if(!raid.hasLootData()) throw FtcExceptionProvider.create("Raid does not have lootData");
    }

    private LiteralArgumentBuilder<CommandSource> lootDataArgument() {
        return literal("loot")
                .then(literal("test")
                        .executes(c -> {
                            VikingRaid raid = get(c);
                            checkHasLoot(raid);

                            RaidGenerationContext context = RaidUtil.createTestContext(raid);
                            raid.getLootData().generate(context);

                            c.getSource().sendAdmin("Generating loot");
                            return 0;
                        })
                )

                .then(literal("groups")
                        .executes(c -> {
                            VikingRaid raid = get(c);

                            checkHasLoot(raid);
                            LootData data = raid.getLootData();

                            if(!data.hasChestGroups()) throw FtcExceptionProvider.create("LootData has no chest groups");

                            TextComponent.Builder builder = Component.text()
                                    .append(Component.text("ChestGroups of " + raid.key().asString()));

                            for (ChestGroup g: data.getChestGroups().values()) {
                                builder
                                        .append(Component.newline())
                                        .append(Component.text(g.key().asString() + " = max: " + g.getMax()))
                                        .append(Component.text(", lootTable: " + g.getLootTableKey().asString()))
                                        .append(Component.text(", possibleLocations: " + g.getPossibleLocations().toString()));
                            }

                            c.getSource().sendMessage(builder.build());
                            return 0;
                        })

                        .then(literal("add")
                                .then(argument("group_key", Valhalla.KEY_PARSER)
                                        .then(argument("lootTable", LootTableArgument.lootTable())
                                                .then(argument("maxChests", IntegerArgumentType.integer(0, 127))
                                                        .executes(c -> {
                                                            VikingRaid raid = get(c);
                                                            LootData lootData = raid.getLootData();

                                                            NamespacedKey groupKey = KeyArgument.getKey(c, "group_key");
                                                            LootTable lootTable = LootTableArgument.getLootTable(c, "lootTable");
                                                            int maxChests = c.getArgument("maxChests", Integer.class);

                                                            if(lootData.hasGroup(groupKey)) throw FtcExceptionProvider.create("LootData for viking already has a group with the given ID");

                                                            ChestGroup group = new ChestGroup(groupKey, lootTable.getKey(), (byte) maxChests, new ObjectArrayList<>());
                                                            lootData.addGroup(group);

                                                            c.getSource().sendAdmin("Added chestGroup with ID " + groupKey.asString());
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                        )

                        .then(literal("remove")
                                .then(argument("remove_key", Valhalla.KEY_PARSER)
                                        .executes(c -> {
                                            VikingRaid raid = get(c);
                                            NamespacedKey key = KeyArgument.getKey(c, "remove_key");

                                            checkHasLoot(raid);
                                            if(!raid.getLootData().hasGroup(key)) throw FtcExceptionProvider.create("LootData does not have a group by this key");

                                            raid.getLootData().removeGroup(key);

                                            c.getSource().sendAdmin("Removed chestGroup with ID " + key.asString());
                                            return 0;
                                        })
                                )
                        )
                )

                .then(literal("definite")
                        .executes(c -> {
                            VikingRaid raid = get(c);
                            checkHasLoot(raid);

                            LootData data = raid.getLootData();
                            if(!data.hasDefiniteSpawns()) throw FtcExceptionProvider.create("Raid has no set chest spawns");

                            TextComponent.Builder builder = Component.text()
                                    .append(Component.text("Chest spawns of " + raid.key().asString() + ":"));

                            for (Map.Entry<BlockPos, Key> e: data.getDefiniteSpawns().entrySet()) {
                                builder
                                        .append(Component.newline())
                                        .append(Component.text(e.getKey().toString() + ": " + e.getValue().asString()));
                            }

                            c.getSource().sendMessage(builder.build());
                            return 0;
                        })

                        .then(literal("add")
                                .then(argument("chest_pos", PositionArgument.blockPos())
                                        .then(argument("lootTable", LootTableArgument.lootTable())
                                                .executes(c -> {
                                                    VikingRaid raid = get(c);
                                                    LootData data = raid.getLootData();

                                                    BlockPos pos = BlockPos.of(PositionArgument.getLocation(c, "chest_pos"));
                                                    LootTable lootTable = LootTableArgument.getLootTable(c, "lootTable");

                                                    if(data.hasChestAt(pos)) throw FtcExceptionProvider.create("LootData already has a chest at given position");

                                                    data.setChest(pos, lootTable.getKey());

                                                    c.getSource().sendAdmin("Placed chest spawn at " + pos.toString() + " with lootTable " + lootTable.getKey().asString());
                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(literal("remove")
                                .then(argument("remove_pos", PositionArgument.blockPos())
                                        .executes(c -> {
                                            VikingRaid raid = get(c);
                                            BlockPos pos = BlockPos.of(PositionArgument.getLocation(c, "remove_pos"));

                                            checkHasLoot(raid);
                                            if(!raid.getLootData().hasChestAt(pos)) throw FtcExceptionProvider.create("Chest spawn doesn't exist at given location");

                                            raid.getLootData().removeChest(pos);

                                            c.getSource().sendAdmin("Removed chest spawn at " + pos.toString());
                                            return 0;
                                        })
                                )
                        )
                );
    }
}