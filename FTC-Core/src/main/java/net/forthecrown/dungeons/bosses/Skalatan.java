package net.forthecrown.dungeons.bosses;

import net.forthecrown.core.Worlds;
import net.forthecrown.dungeons.BossFightContext;
import net.forthecrown.dungeons.DungeonAreas;
import net.forthecrown.dungeons.DungeonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

    private static final ItemStack WITHER_GOO = DungeonUtils.makeDungeonItem(Material.BLACK_DYE, 30, "Wither Goo");

    public Skalatan() {
        super("Skalatan", new Location(Worlds.voidWorld(), -103.5, 67, 184.5), (short) 100, DungeonAreas.SKALATAN_ROOM,
                Arrays.asList(
                        WITHER_GOO,
                        DungeonUtils.makeDungeonItem(Material.BONE, 30, "Floaty Bones"),
                        DungeonUtils.makeDungeonItem(Material.BONE, 30, "Horse Bones"),
                        DungeonUtils.makeDungeonItem(Material.BONE, 15, "Stray Bones")
                )
        );
    }

    @Override
    protected WitherSkeleton onSummon(BossFightContext context) {
        return spawnLocation.getWorld().spawn(spawnLocation, WitherSkeleton.class, skeleton -> {
            skeleton.customName(Component.text("Skalatan").color(NamedTextColor.YELLOW));
            skeleton.setCustomNameVisible(true);
            skeleton.setRemoveWhenFarAway(false);

            double health = context.getBossHealth(300);
            skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            skeleton.setHealth(health);

            skeleton.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25.0);
            skeleton.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
            skeleton.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(context.getBossDamage(10));
            skeleton.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.305F);;
            skeleton.getEquipment().setItemInMainHandDropChance(0);

            attackState = AttackState.MELEE;
            skeleton.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, false, false));
        });
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
    protected void onDeath(BossFightContext context) {}

    public static ItemStack witherGoo(){
        return WITHER_GOO.clone();
    }

    public enum AttackState{
        RANGED,
        MELEE
    }
}
