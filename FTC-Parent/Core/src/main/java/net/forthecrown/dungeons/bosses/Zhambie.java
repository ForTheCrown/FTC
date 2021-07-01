package net.forthecrown.dungeons.bosses;

import net.forthecrown.core.CrownCore;
import net.forthecrown.dungeons.DungeonAreas;
import net.forthecrown.dungeons.BossFightContext;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.utils.RoyalUtils;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Husk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Zhambie extends DungeonBoss<Husk> {

    private final Set<Husk> helpers = new HashSet<>();

    public Zhambie() {
        super("Zhambie", new Location(Worlds.VOID, -191.5, 80, 157.5),
                (short) 60, DungeonAreas.ZHAMBIE_ROOM,
                Arrays.asList( //Required items
                        RoyalUtils.makeDungeonItem(Material.DRIED_KELP, 45,  (Component) null),
                        RoyalUtils.makeDungeonItem(Material.GOLD_NUGGET, 30, (Component) null),
                        RoyalUtils.makeDungeonItem(Material.ROTTEN_FLESH, 15, "Knight's Flesh"),
                        mummyIngot()
                )
        );
    }

    @Override
    protected Husk onSummon(BossFightContext context) {
        Husk zhambie = spawnLocation.getWorld().spawn(spawnLocation, Husk.class, husk -> {
            husk.setAdult();
            husk.customName(Component.text("Zhambie").color(NamedTextColor.YELLOW));
            husk.setCustomNameVisible(true);
            husk.setRemoveWhenFarAway(false);
            husk.setPersistent(true);

            final double health = context.getBossHealth(300);
            husk.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            husk.setHealth(health);
            husk.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25.0);
            husk.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(.9F);
            husk.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(context.getBossDamage(20));
            husk.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.31F);;

            husk.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, false, false));
        });

        return zhambie;
    }

    @Override
    protected void onUpdate() {
        if(helpers.size() <= 3 && random.nextBoolean()) spawnHelper(bossEntity.getLocation().clone());
    }

    @Override
    public void onHit(EntityDamageEvent event) {
        if(!(event instanceof EntityDamageByEntityEvent)) return;

        EntityDamageByEntityEvent event1 = (EntityDamageByEntityEvent) event;
        if(!(event1.getDamager() instanceof Arrow)) return;
        event.setCancelled(true);
        bossEntity.getWorld().playSound(bossEntity.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.3f, 1.2f);
        bossEntity.getWorld().spawnParticle(Particle.SQUID_INK, bossEntity.getLocation().add(0, bossEntity.getHeight()*0.66, 0), 5, 0.1D, 0.1D, 0.1D, 0.05D);
    }

    @Override
    protected void onDeath(BossFightContext context) {
        giveRewards("adventure:zhambie", BossItems.ZHAMBIE.item(), context);
        for (Husk s: helpers){
            s.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 99999, 254, false, false));
        }
        helpers.clear();
    }

    private void spawnHelper(Location location){
        location.getWorld().spawnParticle(Particle.FLAME, location.add(0, 2, 0), 5, 0.1D, 0.4D, 0.1D, 0.01D);
        for (long i = 1; i < 6; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CrownCore.inst(), () -> location.getWorld().spawnParticle(Particle.FLAME, location, 5, 0.1D, 0.4D, 0.1D, 0.01D), i*10);
        }

        Husk helper = location.getWorld().spawn(location, Husk.class, zhelper  -> {
            EntityEquipment equipment = zhelper.getEquipment();
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

            zhelper.setAdult();
            zhelper.getWorld().playSound(zhelper.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 0.7f, 1.0f);
            zhelper.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.8f);
            zhelper.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15.0);
            zhelper.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.32F);;
            zhelper.setRemoveWhenFarAway(false);
            zhelper.setCanPickupItems(false);
            zhelper.setPersistent(true);
            zhelper.setLootTable(LootTables.EMPTY.getLootTable());
        });

        helpers.add(helper);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(!helpers.contains(event.getEntity())) return;
        helpers.remove(event.getEntity());
    }

    public static ItemStack mummyIngot(){
        return RoyalUtils.makeDungeonItem(Material.GOLD_INGOT, 1, (Component) null).clone();
    }
}