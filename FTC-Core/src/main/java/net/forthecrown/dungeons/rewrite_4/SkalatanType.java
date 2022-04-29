package net.forthecrown.dungeons.rewrite_4;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.dungeons.DungeonConstants;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.rewrite_4.component.BossEntity;
import net.forthecrown.dungeons.rewrite_4.component.BossHealth;
import net.forthecrown.dungeons.rewrite_4.component.TickComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collection;

public class SkalatanType extends StaticBossType {
    private static final ItemStack WITHER_GOO = DungeonUtils.makeDungeonItem(Material.BLACK_DYE, 30, "Wither Goo");
    public static final int STATE_SWITCH_INTERVAL = 100;

    SkalatanType() {
        super("Skalatan", DungeonConstants.ID_SKALATAN);
    }

    @Override
    protected Collection<ItemStack> requiredItems() {
        return Arrays.asList(
                WITHER_GOO,
                DungeonUtils.makeDungeonItem(Material.BONE, 30, "Floaty Bones"),
                DungeonUtils.makeDungeonItem(Material.BONE, 30, "Horse Bones"),
                DungeonUtils.makeDungeonItem(Material.BONE, 15, "Stray Bones")
        );
    }

    @Override
    protected void _defineBoss(DungeonBoss boss) {
        boss.setRoom(DungeonConstants.SKALATAN_ROOM);
        boss.setSpawnLocation(DungeonConstants.SKALATAN_SPAWN);

        boss.addComponent(new SkalatanEntity());
    }

    public static ItemStack witherGoo() {
        return WITHER_GOO.clone();
    }

    @RequiredArgsConstructor
    public enum Phase {
        MELEE (Material.STONE_SWORD),
        RANGED (Material.BOW);

        @Getter
        private final Material weapon;

        public Phase opposite() {
            return this == MELEE ? RANGED : MELEE;
        }
    }

    public class SkalatanEntity extends BossEntity implements TickComponent {
        private Phase phase;

        @Override
        protected WitherSkeleton summon(BossContext context, World w, Location l) {
            phase = Phase.MELEE;

            return w.spawn(l, WitherSkeleton.class, skeleton -> {
                skeleton.customName(name().color(NamedTextColor.YELLOW));
                skeleton.setCustomNameVisible(true);
                skeleton.setRemoveWhenFarAway(false);

                double health = context.health(300);
                skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
                skeleton.setHealth(health);

                // Modifiers :(
                DungeonUtils.clearModifiers(skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH));

                skeleton.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25.0);
                skeleton.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
                skeleton.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(context.damage(10));
                skeleton.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.305F);;
                skeleton.getEquipment().setItemInMainHandDropChance(0);

                skeleton.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, false, false));
            });
        }

        @Override
        public void onDamage(BossHealth health, BossHealth.Damage damage) {
            if((damage.getDamager() instanceof Projectile && phase == Phase.RANGED)
                    || phase == Phase.MELEE
            ) {
                return;
            }

            damage.setCancelled(true);
            DungeonUtils.cannotHarmEffect(getWorld(), getEntity());
        }

        @Override
        public WitherSkeleton getEntity() {
            return (WitherSkeleton) super.getEntity();
        }

        @Override
        public void tick(long bossTick) {
            if(bossTick == 0 || bossTick % STATE_SWITCH_INTERVAL != 0) return;

            phase = phase.opposite();

            getEntity()
                    .getEquipment()
                    .setItemInMainHand(new ItemStack(phase.getWeapon()));
        }
    }
}