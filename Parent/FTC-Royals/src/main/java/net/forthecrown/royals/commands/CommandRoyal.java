package net.forthecrown.royals.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.royals.RoyalUtils;
import net.forthecrown.royals.Royals;
import net.forthecrown.royals.dungeons.bosses.BossItems;
import net.forthecrown.royals.dungeons.bosses.Bosses;
import net.forthecrown.royals.dungeons.bosses.mobs.DungeonBoss;
import net.forthecrown.royals.enchantments.CrownEnchant;
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

public class CommandRoyal extends CrownCommandBuilder {

    public CommandRoyal(){
        super("royals", Royals.inst);

        setPermission("ftc.royals.admin");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("spawndummy")
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
                .then(argument("updateLegacy")
                        .then(argument("apple")
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
                        .then(argument("item")
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

                .then(argument("enchant")
                        .then(argument("legacy", BoolArgumentType.bool())
                                .then(argument("enchantment", RoyalEnchantType.ENCHANT)
                                        .executes(c -> enchantItemInHand(c,
                                                c.getArgument("enchantment", CrownEnchant.class),
                                                c.getArgument("legacy", Boolean.class)
                                        ))
                                )

                                /*.then(argument("crit").executes(c -> enchantItemInHand(c, RoyalEnchants.poisonCrit(), c.getArgument("legacy", Boolean.class))))
                                .then(argument("block").executes(c -> enchantItemInHand(c, RoyalEnchants.healingBlock(), c.getArgument("legacy", Boolean.class))))
                                .then(argument("swim").executes(c -> enchantItemInHand(c, RoyalEnchants.dolphinSwimmer(), c.getArgument("legacy", Boolean.class))))
                                .then(argument("aim").executes(c -> enchantItemInHand(c, RoyalEnchants.strongAim(), c.getArgument("legacy", Boolean.class))))*/
                        )
                )

                .then(argument("debug")
                        .then(argument("apples")
                                .then(argument("boss", StringArgumentType.word())
                                        .suggests(suggestMatching(ListUtils.arrayToCollection(BossItems.values(), item -> item.toString().replaceAll(" ", "_"))))

                                        .executes(c -> {
                                            BossItems boss;
                                            try {
                                                boss = BossItems.valueOf(c.getArgument("boss", String.class).toUpperCase());
                                            } catch (Exception e){ throw FtcExceptionProvider.create("Invalid boss"); }

                                            Player player = getPlayerSender(c);
                                            player.getInventory().addItem(boss.item());

                                            broadcastAdmin(c.getSource(), "Giving " + CrownUtils.normalEnum(boss) + " apple");
                                            return 0;
                                        })
                                )
                        )

                        .then(argument(bossArg, StringArgumentType.word())
                                .suggests(suggestMatching(ListUtils.convertToList(Bosses.BY_NAME.keySet(), str -> str.replaceAll(" ", "_"))))

                                .then(argument("spawn")
                                        .executes(c -> {
                                            DungeonBoss<?> boss = getBoss(c);

                                            boss.summon();
                                            broadcastAdmin(c.getSource(), "Spawning boss");
                                            return 0;
                                        })
                                )
                                .then(argument("kill")
                                        .executes(c -> {
                                            DungeonBoss<?> boss = getBoss(c);
                                            if(!boss.isAlive()) throw FtcExceptionProvider.create("Boss isn't alive");

                                            boss.kill(false);
                                            broadcastAdmin(c.getSource(), "Killing boss");
                                            return 0;
                                        })
                                )
                                .then(argument("giveitems")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            DungeonBoss<?> boss = getBoss(c);

                                            for (ItemStack i: boss.getSpawningItems()){
                                                try {
                                                    player.getInventory().addItem(i.clone());
                                                } catch (Exception e) { throw FtcExceptionProvider.create("Inventory full!"); }
                                            }

                                            broadcastAdmin(c.getSource(), "Giving items");
                                            return 0;
                                        })
                                )
                                .then(argument("battleContext")
                                        .executes(c -> {
                                            DungeonBoss<?> boss = getBoss(c);
                                            if(!boss.isAlive()) throw FtcExceptionProvider.create("Boss has not been spawned");

                                            broadcastAdmin(c.getSource(), "boss: " + boss.getBossEntity().getCustomName());
                                            broadcastAdmin(c.getSource(), "health: " + boss.getBossEntity().getHealth());
                                            broadcastAdmin(c.getSource(), "final_mod: " + boss.getContext().getModifier());
                                            broadcastAdmin(c.getSource(), "max_health: " + boss.getBossEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                                            broadcastAdmin(c.getSource(), "attack_damage: " + boss.getBossEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue());
                                            broadcastAdmin(c.getSource(), "armor_amount: " + boss.getContext().getArmorAmount());
                                            broadcastAdmin(c.getSource(), "enchant_amount: " + boss.getContext().getEnchantAmount());

                                            return 0;
                                        })
                                )
                                .then(argument("attemptSpawn")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            DungeonBoss<?> boss = getBoss(c);

                                            boss.attemptSpawn(player);
                                            broadcastAdmin(c.getSource(), "Attempting boss spawn");
                                            return 0;
                                        })
                                )
                        )
                );
    }

    private static final String bossArg = "boss";

    public DungeonBoss<?> getBoss(CommandContext<CommandSource> c) throws CommandSyntaxException {
        DungeonBoss<?> boss = Bosses.BY_NAME.get(c.getArgument(bossArg, String.class).replaceAll("_", " "));
        if(boss == null) throw FtcExceptionProvider.create("Invalid boss");
        return boss;
    }

    private int enchantItemInHand(CommandContext<CommandSource> c, CrownEnchant enchant, boolean legacy) throws CommandSyntaxException {
        Player player = getPlayerSender(c);
        ItemStack to_enchant = player.getInventory().getItemInMainHand();
        if(to_enchant == null) throw FtcExceptionProvider.create("Hold an item you dumbass");

        if(!legacy) CrownEnchant.addCrownEnchant(to_enchant, enchant, 1);
        else to_enchant.addUnsafeEnchantment(enchant, 1);
        return 0;
    }
}
