package net.forthecrown.dungeons.rewrite_4;

import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.DungeonConstants;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.rewrite_4.component.BossEntity;
import net.forthecrown.dungeons.rewrite_4.component.BossRewards;
import net.forthecrown.dungeons.rewrite_4.component.MinionSpawner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Husk;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collection;

public class ZhambieType extends StaticBossType {
    ZhambieType() {
        super("Zhambie", DungeonConstants.ID_ZHAMBIE);
    }

    public static ItemStack mummyIngot() {
        return DungeonUtils.makeDungeonItem(Material.GOLD_INGOT, 1, (Component) null);
    }

    @Override
    protected Collection<ItemStack> requiredItems() {
        return Arrays.asList(
                DungeonUtils.makeDungeonItem(Material.DRIED_KELP, 45,  (Component) null),
                DungeonUtils.makeDungeonItem(Material.GOLD_NUGGET, 30, (Component) null),
                DungeonUtils.makeDungeonItem(Material.ROTTEN_FLESH, 15, "Knight's Flesh"),
                mummyIngot()
        );
    }

    @Override
    protected void _defineBoss(DungeonBoss boss) {
        boss.setRoom(DungeonConstants.ZHAMBIE_ROOM);
        boss.setSpawnLocation(DungeonConstants.ZHAMBIE_SPAWN);

        boss.addComponent(new ZhambieEntity());
        boss.addComponent(new BossRewards.BossItemsReward(BossItems.ZHAMBIE));

        MinionSpawner spawner = new MinionSpawner();
        spawner.setSpawner((context, w, l, random) -> w.spawn(l, Husk.class, husk -> {
            // Give minion gold armor that cannot drop
            EntityEquipment equipment = husk.getEquipment();

            equipment.setHelmet(new ItemStack(Material.GOLDEN_HELMET));
            equipment.setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
            equipment.setBoots(new ItemStack(Material.GOLDEN_BOOTS));
            equipment.setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));

            equipment.setLeggingsDropChance(0);
            equipment.setHelmetDropChance(0);
            equipment.setChestplateDropChance(0);
            equipment.setBootsDropChance(0);
            equipment.setItemInMainHandDropChance(0);
            equipment.setItemInOffHandDropChance(0);

            // No babies >:(
            husk.setAdult();

            husk.getWorld().playSound(husk.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 0.7f, 1.0f);
            husk.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.8f);
            husk.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15.0);
            husk.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.32F);;
            husk.setRemoveWhenFarAway(false);
            husk.setCanPickupItems(false);
            husk.setPersistent(true);
            husk.setLootTable(LootTables.EMPTY.getLootTable());
        }));

        MinionSpawner.SpawnPosList list = new MinionSpawner.SpawnPosList();
        list.addRaw(
                new double[][] {
                        { -185.5, 78, 166.5 }, { -197.5, 78, 165.2 },
                        { -196.5, 78, 149.5 }, { -184.5, 78, 149.5 },
                        { -183.5, 78, 154.4 }, { -202.5, 78, 158.5 },
                        { -191.5, 79, 160.5 }, { -190.5, 79, 154.5 },
                        { -202.5, 78, 157.5 }
                }
        );

        spawner.setSpawnInterval(60);
        spawner.setMaxSpawned(10);

        spawner.setPosProvider(list);

        boss.addComponent(spawner);
    }

    static class ZhambieEntity extends BossEntity {
        @Override
        protected Entity summon(BossContext context, World w, Location l) {
            return w.spawn(l, Husk.class, husk -> {
                husk.setAdult();
                husk.customName(Component.text("Zhambie").color(NamedTextColor.YELLOW));
                husk.setCustomNameVisible(true);
                husk.setRemoveWhenFarAway(false);
                husk.setPersistent(true);

                final double health = context.health(300);
                AttributeInstance maxHealth = husk.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                maxHealth.setBaseValue(health);

                // Modifiers ;-;
                DungeonUtils.clearModifiers(maxHealth);

                husk.setHealth(health);
                husk.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25.0);
                husk.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(.9F);
                husk.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(context.damage(20));
                husk.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.31F);;

                husk.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, false, false));
            });
        }
    }
}