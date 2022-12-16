package net.forthecrown.dungeons.boss;

import net.forthecrown.core.Worlds;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.DungeonAreas;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SkalatanBoss extends SimpleBoss {
    private static final ItemStack WITHER_GOO = DungeonUtils.makeDungeonItem(Material.BLACK_DYE, 30, "Wither Goo");
    public static final int STATE_SWITCH_INTERVAL = 100;

    private State state;
    private int tick = STATE_SWITCH_INTERVAL;

    public SkalatanBoss() {
        super("Skalatan",
                new Location(Worlds.voidWorld(), -103.5, 67, 184.5),
                DungeonAreas.SKALATAN_ROOM,
                WITHER_GOO,
                DungeonUtils.makeDungeonItem(Material.BONE, 30, "Floaty Bones"),
                DungeonUtils.makeDungeonItem(Material.BONE, 30, "Horse Bones"),
                DungeonUtils.makeDungeonItem(Material.BONE, 15, "Stray Bones")
        );
    }

    public static ItemStack witherGoo(){
        return WITHER_GOO.clone();
    }

    @Override
    protected Mob onSpawn(BossContext context) {
        state = State.MELEE;

        return getWorld().spawn(getSpawn(), WitherSkeleton.class, skeleton -> {
            skeleton.customName(name().color(NamedTextColor.YELLOW));
            skeleton.setCustomNameVisible(true);
            skeleton.setRemoveWhenFarAway(false);

            double health = context.health(300);
            skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            skeleton.setHealth(health);

            // Modifiers :(
            Util.clearModifiers(skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH));

            skeleton.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25.0);
            skeleton.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
            skeleton.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(context.damage(10));
            skeleton.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.305F);;
            skeleton.getEquipment().setItemInMainHandDropChance(0);

            skeleton.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, false, false));
        });
    }

    @Override
    protected void giveRewards(Player player) {
        Util.giveOrDropItem(player.getInventory(), entity.getLocation(), BossItems.SKALATAN.item());
    }

    @Override
    protected void tick() {
        tick--;

        if(tick < 0) {
            // Switch attack state and update weapon
            state = state.other();
            entity.getEquipment().setItemInMainHand(new ItemStack(state.material, 1));

            tick = STATE_SWITCH_INTERVAL;
        }
    }

    @Override
    protected void onHit(BossContext context, EntityDamageEvent event1) {
        if(!(event1 instanceof EntityDamageByEntityEvent event)) return;

        //If attack type matches attack state, don't go further
        if((event.getDamager() instanceof Arrow && state == State.RANGED) || (event.getDamager() instanceof Player && state == State.MELEE)){
            return;
        }

        // attack state does not match damage type,
        // stop event and play effect
        event.setCancelled(true);

        DungeonUtils.cannotHarmEffect(getWorld(), entity);
    }

    public enum State {
        RANGED (Material.BOW),
        MELEE (Material.STONE_SWORD);

        private final Material material;
        State(Material material) {
            this.material = material;
        }

        State other() {
            return switch (this) {
                case MELEE -> RANGED;
                case RANGED -> MELEE;
            };
        }
    }
}