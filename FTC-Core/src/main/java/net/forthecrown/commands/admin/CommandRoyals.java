package net.forthecrown.commands.admin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.arguments.RoyalEnchantArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.DungeonBoss;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.squire.enchantment.RoyalEnchant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CommandRoyals extends FtcCommand {

    public CommandRoyals() {
        super("royals");

        setPermission(Permissions.HELPER);
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
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("spawndummy")
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            DungeonUtils.spawnDummy(player.getLocation());
                            return 0;
                        })
                        .then(argument("location", PositionArgument.position())
                                .executes(c -> {
                                    DungeonUtils.spawnDummy(PositionArgument.getLocation(c, "location"));
                                    return 0;
                                })
                        )
                )
                .then(literal("updateLegacy")
                        .then(literal("apple")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    ItemStack item = player.getInventory().getItemInMainHand();

                                    if(item == null || item.getType() != Material.GOLDEN_APPLE || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
                                        throw FtcExceptionProvider.create("You must be holding a boss apple!");
                                    }

                                    ItemMeta meta = item.getItemMeta();
                                    Component display = item.getItemMeta().displayName();

                                    display = display.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

                                    meta.displayName(display);
                                    meta.getPersistentDataContainer().set(Bosses.KEY, PersistentDataType.BYTE, (byte) 1);
                                    item.setItemMeta(meta);

                                    c.getSource().sendAdmin( "apple updated");
                                    return 0;
                                })
                        )
                        .then(literal("item")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    ItemStack item = player.getInventory().getItemInMainHand();
                                    if(item == null || !item.hasItemMeta()) throw FtcExceptionProvider.create("You need to be holding an item bruv");
                                    ItemMeta meta = item.getItemMeta();

                                    List<Component> lore = meta.lore() == null ? new ArrayList<>() : meta.lore();
                                    lore.set(0, DungeonUtils.DUNGEON_LORE);

                                    if(meta.displayName() != null){
                                        Component name = meta.displayName();
                                        name = name.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                                        meta.displayName(name);
                                    }
                                    meta.lore(lore);
                                    item.setItemMeta(meta);

                                    c.getSource().sendAdmin( "item updated");
                                    return 0;
                                })
                        )
                )

                .then(literal("enchant")
                        .then(literal("legacy")
                                .then(argument("enchantment", RoyalEnchantArgument.ENCHANT)
                                        .executes(c -> enchantItemInHand(c,
                                                c.getArgument("enchantment", RoyalEnchant.class),
                                                true
                                        ))
                                )
                        )
                        
                        .then(literal("new")
                                .then(argument("enchantment", RoyalEnchantArgument.ENCHANT)
                                        .executes(c -> enchantItemInHand(c,
                                                c.getArgument("enchantment", RoyalEnchant.class),
                                                false
                                        ))
                                )
                        )
                )

                .then(literal("debug")
                        .then(literal("apples")
                                .then(argument("boss", EnumArgument.of(BossItems.class))
                                        .executes(c -> {
                                            BossItems boss = c.getArgument("boss", BossItems.class);

                                            Player player = getPlayerSender(c);
                                            player.getInventory().addItem(boss.item());

                                            c.getSource().sendAdmin( "Giving " + FtcFormatter.normalEnum(boss) + " apple");
                                            return 0;
                                        })
                                )
                        )

                        .then(argument(bossArg, RegistryArguments.dungeonBoss())
                                .then(literal("spawn")
                                        .executes(c -> {
                                            DungeonBoss boss = c.getArgument(bossArg, DungeonBoss.class);

                                            boss.spawn();
                                            c.getSource().sendAdmin( "Spawning boss");
                                            return 0;
                                        })
                                )
                                .then(literal("kill")
                                        .executes(c -> {
                                            DungeonBoss boss = c.getArgument(bossArg, DungeonBoss.class);
                                            if(!boss.isAlive()) throw FtcExceptionProvider.create("Boss isn't alive");

                                            boss.kill(false);
                                            c.getSource().sendAdmin( "Killing boss");
                                            return 0;
                                        })
                                )
                                .then(literal("giveitems")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            DungeonBoss boss = c.getArgument(bossArg, DungeonBoss.class);

                                            for (ItemStack i: boss.getSpawningItems()){
                                                try {
                                                    player.getInventory().addItem(i.clone());
                                                } catch (Exception e) { throw FtcExceptionProvider.create("Inventory full!"); }
                                            }

                                            c.getSource().sendAdmin( "Giving items");
                                            return 0;
                                        })
                                )
                                .then(literal("attemptSpawn")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            DungeonBoss boss = c.getArgument(bossArg, DungeonBoss.class);

                                            boss.attemptSpawn(player);
                                            c.getSource().sendAdmin( "Attempting boss spawn");
                                            return 0;
                                        })
                                )
                        )
                );
    }

    private static final String bossArg = "boss";

    private int enchantItemInHand(CommandContext<CommandSource> c, RoyalEnchant enchant, boolean legacy) throws CommandSyntaxException {
        Player player = getPlayerSender(c);
        ItemStack toEnchant = player.getInventory().getItemInMainHand();
        if(toEnchant == null) throw FtcExceptionProvider.create("Hold an item");

        if(!legacy) RoyalEnchant.addCrownEnchant(toEnchant, enchant, 1);
        else toEnchant.addUnsafeEnchantment(enchant, 1);
        return 0;
    }
}