package net.forthecrown.commands.viking;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.grenadier.types.LootTableArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.valhalla.RaidGenerationContext;
import net.forthecrown.valhalla.Valhalla;
import net.forthecrown.valhalla.VikingUtil;
import net.forthecrown.valhalla.data.ChestGroup;
import net.forthecrown.valhalla.data.LootData;
import net.forthecrown.valhalla.data.VikingRaid;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;

import java.util.Map;

import static net.forthecrown.commands.viking.CommandVRaid.get;
import static net.forthecrown.useables.InteractionUtils.argument;
import static net.forthecrown.useables.InteractionUtils.literal;

public class LootDataArgument {
    static LiteralArgumentBuilder<CommandSource> arg() {
        return literal("loot")
                .then(literal("test")
                        .executes(c -> {
                            VikingRaid raid = get(c);
                            checkHasLoot(raid);

                            RaidGenerationContext context = VikingUtil.createTestContext(raid);
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

                        .then(literal("edit")
                                .then(argument("group_key", Valhalla.KEY_PARSER)
                                        .then(literal("add_chest")
                                                .then(argument("position", PositionArgument.blockPos())
                                                        .executes(c -> {
                                                            ChestGroup group = getGroup(c);
                                                            Vector3i pos = Vector3i.of(PositionArgument.getLocation(c, "position"));

                                                            group.getPossibleLocations().add(pos);

                                                            c.getSource().sendAdmin("Added position to chest group " + group.key().asString());
                                                            return 0;
                                                        })
                                                )
                                        )

                                        .then(literal("remove_chest")
                                                .then(argument("position", PositionArgument.blockPos())
                                                        .executes(c -> {
                                                            ChestGroup group = getGroup(c);
                                                            Vector3i pos = Vector3i.of(PositionArgument.getLocation(c, "position"));

                                                            group.getPossibleLocations().remove(pos);

                                                            c.getSource().sendAdmin("Removed position from chest group " + group.key().asString());
                                                            return 0;
                                                        })
                                                )
                                        )

                                        .then(literal("lootTable")
                                                .executes(c -> {
                                                    ChestGroup group = getGroup(c);

                                                    c.getSource().sendAdmin(group.getLootTableKey().asString());
                                                    return 0;
                                                })

                                                .then(argument("lootTable", LootTableArgument.lootTable())
                                                        .executes(c -> {
                                                            ChestGroup group = getGroup(c);
                                                            LootTable table = LootTableArgument.getLootTable(c, "lootTable");

                                                            group.setLootTableKey(table.getKey());

                                                            c.getSource().sendAdmin("Set lootTable of " + group.key().asString() + " to " + table.getKey().asString());
                                                            return 0;
                                                        })
                                                )
                                        )

                                        .then(literal("maxChests")
                                                .executes(c -> {
                                                    ChestGroup group = getGroup(c);

                                                    c.getSource().sendAdmin("maxChests: " + group.getMaxChests());
                                                    return 0;
                                                })

                                                .then(argument("amount", IntegerArgumentType.integer(1, 127))
                                                        .executes(c -> {
                                                            ChestGroup group = getGroup(c);
                                                            int amount = IntegerArgumentType.getInteger(c, "amount");

                                                            group.setMaxChests((byte) amount);

                                                            c.getSource().sendAdmin("Set maxChests to " + amount + " for " + group.key().asString());
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

                            for (Map.Entry<Vector3i, Key> e: data.getDefiniteSpawns().entrySet()) {
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

                                                    Vector3i pos = Vector3i.of(PositionArgument.getLocation(c, "chest_pos"));
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
                                            Vector3i pos = Vector3i.of(PositionArgument.getLocation(c, "remove_pos"));

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

    static void checkHasLoot(VikingRaid raid) throws CommandSyntaxException {
        if(!raid.hasLootData()) throw FtcExceptionProvider.create("Raid does not have lootData");
    }

    static ChestGroup getGroup(CommandContext<CommandSource> c) throws CommandSyntaxException {
        VikingRaid raid = get(c);
        checkHasLoot(raid);

        LootData data = raid.getLootData();
        if(!data.hasChestGroups()) throw FtcExceptionProvider.create("Given raid does not have any chest groups");

        NamespacedKey key = c.getArgument("group_key", NamespacedKey.class);
        if(!data.hasGroup(key)) throw FtcExceptionProvider.create("LootData doesn't have group with ID " + key.asString());

        return data.getChestGroup(key);
    }
}
