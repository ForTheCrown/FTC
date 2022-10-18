package net.forthecrown.dungeons.boss;

import net.forthecrown.core.Worlds;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.DungeonAreas;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.components.BossComponent;
import net.forthecrown.dungeons.boss.components.MinionSpawnerComponent;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class ZhambieBoss extends SimpleBoss {
    public ZhambieBoss() {
        super("Zhambie", new Location(Worlds.voidWorld(), -191.5, 80, 157.5), DungeonAreas.ZHAMBIE_ROOM,
                DungeonUtils.makeDungeonItem(Material.DRIED_KELP, 45,  (Component) null),
                DungeonUtils.makeDungeonItem(Material.GOLD_NUGGET, 30, (Component) null),
                DungeonUtils.makeDungeonItem(Material.ROTTEN_FLESH, 15, "Knight's Flesh"),
                mummyIngot()
        );
    }

    @Override
    protected void createComponents(Set<BossComponent> c) {
        super.createComponents(c);

        // Minion spawner
        MinionSpawnerComponent comp = MinionSpawnerComponent.create(
                (pos, world, context) -> {
                    return world.spawn(
                            new Location(world, pos.x, pos.y, pos.z),
                            Husk.class,
                            husk -> {
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
                            }
                    );
                },
                60, 10,
                // Spawn positions
                new double[][] {
                        {-185.5, 78, 166.5}, {-197.5, 78, 165.2}, {-196.5, 78, 149.5},
                        {-184.5, 78, 149.5}, {-183.5, 78, 154.4}, {-202.5, 78, 158.5},
                        {-191.5, 79, 160.5}, {-190.5, 79, 154.5}, {-202.5, 78, 157.5}
                }
        );

        c.add(comp);
    }

    public static ItemStack mummyIngot() {
        return DungeonUtils.makeDungeonItem(Material.GOLD_INGOT, 1, (Component) null);
    }

    @Override
    protected Mob onSpawn(BossContext context) {
        Husk zhambie = getWorld().spawn(getSpawn(), Husk.class, husk -> {
            husk.setAdult();
            husk.customName(Component.text("Zhambie").color(NamedTextColor.YELLOW));
            husk.setCustomNameVisible(true);
            husk.setRemoveWhenFarAway(false);
            husk.setPersistent(true);

            final double health = context.health(300);
            AttributeInstance maxHealth = husk.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            maxHealth.setBaseValue(health);

            // Modifiers ;-;
            Util.clearModifiers(maxHealth);

            husk.setHealth(health);
            husk.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25.0);
            husk.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(.9F);
            husk.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(context.damage(20));
            husk.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.31F);;

            husk.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, false, false));
        });

        return zhambie;
    }

    @Override
    protected void giveRewards(Player player) {
        Util.giveOrDropItem(player.getInventory(), entity.getLocation(), BossItems.ZHAMBIE.item());
    }

    @Override
    protected void onHit(BossContext context, EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent byEntityDamage)) {
            return;
        }

        if (!(byEntityDamage.getDamager() instanceof Arrow)) {
            return;
        }

        // Arrows cannot damage this badass B)
        event.setCancelled(true);

        getWorld().playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.3f, 1.2f);
        getWorld().spawnParticle(Particle.SQUID_INK, entity.getLocation().add(0, entity.getHeight()*0.66, 0), 5, 0.1D, 0.1D, 0.1D, 0.05D);
    }
}