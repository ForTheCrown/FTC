package net.forthecrown.royals.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.royals.RoyalUtils;
import net.forthecrown.royals.Royals;
import net.forthecrown.royals.dungeons.bosses.BossItems;
import net.forthecrown.royals.dungeons.bosses.Bosses;
import net.forthecrown.royals.dungeons.bosses.mobs.DungeonBoss;
import net.forthecrown.squire.enchantment.RoyalEnchant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CommandRoyal extends FtcCommand {

    public CommandRoyal(){
        super("royals", Royals.inst);

        setPermission("ftc.royals.admin");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("spawndummy")
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            RoyalUtils.spawnDummy(player.getLocation());
                            return 0;
                        })
                        .then(argument("location", PositionArgument.position())
                                .executes(c -> {
                                    RoyalUtils.spawnDummy(PositionArgument.getLocation(c, "location"));
                                    return 0;
                                })
                        )
                )
                .then(literal("updateLegacy")
                        .then(literal("apple")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    ItemStack item = player.getInventory().getItemInMainHand();

                                    if(item == null || item.getType() != Material.GOLDEN_APPLE || !item.hasItemMeta() || item.getItemMeta().displayName() == null)
                                        throw FtcExceptionProvider.create("You must be holding a boss apple!");

                                    ItemMeta meta = item.getItemMeta();
                                    Component display = item.getItemMeta().displayName();

                                    display = display.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

                                    meta.displayName(display);
                                    meta.getPersistentDataContainer().set(Bosses.key, PersistentDataType.BYTE, (byte) 1);
                                    item.setItemMeta(meta);
                                    broadcastAdmin(c.getSource(), "apple updated");
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
                                    lore.set(0, RoyalUtils.DUNGEON_LORE);

                                    if(meta.displayName() != null){
                                        Component name = meta.displayName();
                                        name = name.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                                        meta.displayName(name);
                                    }
                                    meta.lore(lore);
                                    item.setItemMeta(meta);

                                    broadcastAdmin(c.getSource(), "item updated");
                                    return 0;
                                })
                        )
                )

                .then(literal("enchant")
                        .then(argument("legacy", BoolArgumentType.bool())
                                .then(argument("enchantment", RoyalEnchantType.ENCHANT)
                                        .executes(c -> enchantItemInHand(c,
                                                c.getArgument("enchantment", RoyalEnchant.class),
                                                c.getArgument("legacy", Boolean.class)
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

                                            broadcastAdmin(c.getSource(), "Giving " + ChatFormatter.normalEnum(boss) + " apple");
                                            return 0;
                                        })
                                )
                        )

                        .then(argument(bossArg, BossArgument.boss())
                                .then(literal("spawn")
                                        .executes(c -> {
                                            DungeonBoss<?> boss = BossArgument.getBoss(c, bossArg);

                                            boss.summon();
                                            broadcastAdmin(c.getSource(), "Spawning boss");
                                            return 0;
                                        })
                                )
                                .then(literal("kill")
                                        .executes(c -> {
                                            DungeonBoss<?> boss = BossArgument.getBoss(c, bossArg);
                                            if(!boss.isAlive()) throw FtcExceptionProvider.create("Boss isn't alive");

                                            boss.kill(false);
                                            broadcastAdmin(c.getSource(), "Killing boss");
                                            return 0;
                                        })
                                )
                                .then(literal("giveitems")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            DungeonBoss<?> boss = BossArgument.getBoss(c, bossArg);

                                            for (ItemStack i: boss.getSpawningItems()){
                                                try {
                                                    player.getInventory().addItem(i.clone());
                                                } catch (Exception e) { throw FtcExceptionProvider.create("Inventory full!"); }
                                            }

                                            broadcastAdmin(c.getSource(), "Giving items");
                                            return 0;
                                        })
                                )
                                .then(literal("context")
                                        .executes(c -> {
                                            DungeonBoss<?> boss = BossArgument.getBoss(c, bossArg);
                                            if(!boss.isAlive()) throw FtcExceptionProvider.create("Boss has not been spawned");

                                            broadcastAdmin(c.getSource(), "boss: " + boss.getBossEntity().getCustomName());
                                            broadcastAdmin(c.getSource(), "health: " + boss.getBossEntity().getHealth());
                                            broadcastAdmin(c.getSource(), "final_mod: " + boss.getContext().getModifier());
                                            broadcastAdmin(c.getSource(), "max_health: " + boss.getBossEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                                            broadcastAdmin(c.getSource(), "attack_damage: " + boss.getBossEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue());
                                            broadcastAdmin(c.getSource(), "armor_amount: " + boss.getContext().getArmorAmount());
                                            broadcastAdmin(c.getSource(), "enchant_amount: " + boss.getContext().getEnchantAmount());

                                            return 0;
                                        })
                                )
                                .then(literal("attemptSpawn")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            DungeonBoss<?> boss = BossArgument.getBoss(c, bossArg);

                                            boss.attemptSpawn(player);
                                            broadcastAdmin(c.getSource(), "Attempting boss spawn");
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

        if(!legacy) RoyalUtils.addCrownEnchant(toEnchant, enchant, 1);
        else toEnchant.addUnsafeEnchantment(enchant, 1);
        return 0;
    }
}
