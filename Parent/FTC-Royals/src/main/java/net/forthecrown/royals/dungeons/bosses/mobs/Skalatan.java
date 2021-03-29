package net.forthecrown.royals.dungeons.bosses.mobs;

import net.forthecrown.royals.RoyalUtils;
import net.forthecrown.royals.Royals;
import net.forthecrown.royals.dungeons.DungeonAreas;
import net.forthecrown.royals.dungeons.bosses.BossFightContext;
import net.forthecrown.royals.dungeons.bosses.BossItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class Skalatan extends DungeonBoss<WitherSkeleton> {

    private AttackState attackState;

    private static final ItemStack WITHER_GOO = RoyalUtils.makeDungeonItem(Material.BLACK_DYE, 30, "Wither Goo");

    public Skalatan(Royals plugin) {
        super(plugin, "Skalatan", new Location(DungeonAreas.WORLD, -103.5, 67, 184.5), (short) 100, DungeonAreas.SKALATAN_ROOM,
                Arrays.asList(
                        WITHER_GOO,
                        RoyalUtils.makeDungeonItem(Material.BONE, 30, "Floaty Bones"),
                        RoyalUtils.makeDungeonItem(Material.BONE, 30, "Horse Bones"),
                        RoyalUtils.makeDungeonItem(Material.BONE, 15, "Stray Bones")
                )
        );
    }

    @Override
    protected WitherSkeleton onSummon(BossFightContext context) {
        WitherSkeleton skeleton = spawnLocation.getWorld().spawn(spawnLocation, WitherSkeleton.class, skalatan -> {
            skalatan.customName(Component.text("Skalatan").color(NamedTextColor.YELLOW));
            skalatan.setCustomNameVisible(true);
            skalatan.setRemoveWhenFarAway(false);
            skalatan.setPersistent(true);

            double health = context.bossHealthMod(300);
            skalatan.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            skalatan.setHealth(health);

            skalatan.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25.0);
            skalatan.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
            skalatan.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10.0);
            skalatan.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.305F);;

            attackState = AttackState.MELEE;
            skalatan.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, false, false));
        });
        return skeleton;
    }

    @Override
    protected void onUpdate() {
        ItemStack toSet;
        if(attackState == AttackState.RANGED){
            toSet = new ItemStack(Material.STONE_SWORD, 1);
            attackState = AttackState.MELEE;
        } else {
            toSet = new ItemStack(Material.BOW, 1);
            attackState = AttackState.RANGED;
        }
        bossEntity.getEquipment().setItemInMainHand(toSet);
    }

    @Override
    public void onHit(EntityDamageEvent event1) {
        if(!(event1 instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) event1;

        //If attack type matches attack state
        if((event.getDamager() instanceof Arrow && attackState == AttackState.RANGED) || (event.getDamager() instanceof Player && attackState == AttackState.MELEE)){
            return;
        }

        event.setCancelled(true);
        bossEntity.getWorld().playSound(bossEntity.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.3f, 1.2f);
        bossEntity.getWorld().spawnParticle(Particle.SQUID_INK, bossEntity.getLocation().add(0, bossEntity.getHeight()*0.66, 0), 5, 0.1D, 0.1D, 0.1D, 0.05D);
    }

    @Override
    protected void onDeath(BossFightContext context) {
        giveRewards("adventure:skalatan", BossItems.SKALATAN.item(), context);
    }

    public static ItemStack witherGoo(){
        return WITHER_GOO.clone();
    }

    public enum AttackState{
        RANGED,
        MELEE
    }
}
