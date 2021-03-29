package net.forthecrown.royals.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.types.Vector3DType;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.royals.RoyalUtils;
import net.forthecrown.royals.Royals;
import net.forthecrown.royals.dungeons.bosses.BossItems;
import net.forthecrown.royals.dungeons.bosses.Bosses;
import net.forthecrown.royals.dungeons.bosses.mobs.DungeonBoss;
import net.forthecrown.royals.enchantments.CrownEnchant;
import net.forthecrown.royals.enchantments.RoyalEnchants;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandRoyal extends CrownCommandBuilder {

    public CommandRoyal(){
        super("royals", Royals.inst);

        setPermission("ftc.admin");
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("spawndummy")
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            RoyalUtils.spawnDummy(player.getLocation());
                            return 0;
                        })
                        .then(argument("location", Vector3DType.vec3())
                                .executes(c -> {
                                    RoyalUtils.spawnDummy(Vector3DType.getLocation(c, "location"));
                                    return 0;
                                })
                        )
                )

                .then(argument("debug")
                        .then(argument("enchants")
                                .then(argument("aim").executes(c -> giveEnchantBook(c, RoyalEnchants.strongAim(), Material.BOW)))
                                .then(argument("dolphin").executes(c -> giveEnchantBook(c, RoyalEnchants.dolphinSwimmer(), Material.TRIDENT)))
                                .then(argument("crit").executes(c -> giveEnchantBook(c, RoyalEnchants.poisonCrit(), Material.DIAMOND_SWORD)))
                                .then(argument("block").executes(c -> giveEnchantBook(c, RoyalEnchants.healingBlock(), Material.SHIELD)))
                        )

                        .then(argument("apples")
                                .then(argument("boss", StringArgumentType.word())
                                        .suggests((c, b) -> suggestMatching(b, ListUtils.arrayToCollection(BossItems.values(), item -> item.toString().replaceAll(" ", "_"))))

                                        .executes(c -> {
                                            BossItems boss;
                                            try {
                                                boss = BossItems.valueOf(c.getArgument("boss", String.class).toUpperCase());
                                            } catch (Exception e){ throw new CrownCommandException("Invalid boss"); }

                                            Player player = getPlayerSender(c);
                                            player.getInventory().addItem(boss.item());
                                            broadcastAdmin(c.getSource(), "Giving " + CrownUtils.normalEnum(boss) + " apple");
                                            return 0;

                                        })
                                )
                        )
                        .then(argument("diplayEnchants")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    broadcastAdmin(c.getSource(), player.getInventory().getItemInMainHand().getEnchantments().toString());
                                    return 0;
                                })
                        )

                        .then(argument(bossArg, StringArgumentType.word())
                                .suggests((c, b) -> suggestMatching(b, ListUtils.convertToList(Bosses.BY_NAME.keySet(), str -> str.replaceAll(" ", "_"))))

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
                                            if(!boss.isAlive()) throw new CrownCommandException("Boss isn't alive");

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
                                                } catch (Exception e) { throw new CrownCommandException("Inventory full!"); }
                                            }

                                            broadcastAdmin(c.getSource(), "Giving items");
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

    public DungeonBoss<?> getBoss(CommandContext<CommandListenerWrapper> c) throws CrownCommandException {
        DungeonBoss<?> boss = Bosses.BY_NAME.get(c.getArgument(bossArg, String.class).replaceAll("_", " "));
        if(boss == null) throw new CrownCommandException("Invalid boss");
        return boss;
    }

    private int giveEnchantBook(CommandContext<CommandListenerWrapper> c, CrownEnchant enchant, Material mat) throws CommandSyntaxException {
        Player player = getPlayerSender(c);
        ItemStack toGive = new ItemStack(mat, 1);
        CrownEnchant.addCrownEnchant(toGive, enchant, 1);
        player.getInventory().addItem(toGive);
        return 0;
    }
}
