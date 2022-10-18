package net.forthecrown.dungeons.boss.evoker.phases;

import net.forthecrown.core.registry.Keys;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.BossMessage;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.persistence.PersistentDataType;

public class ShulkerPhase implements AttackPhase {
    public static final NamespacedKey SHULKER_STATUS_KEY = Keys.forthecrown("shulker_status");
    public static final byte
            STATUS_FIGHTING = 0,
            STATUS_TRAITOR  = 1;

    public static final int[][] SPAWNS = {
            { -286, 34, 36 },
            { -286, 34, 52 },
            { -270, 34, 52 },
            { -270, 34, 36 }
    };

    public static final BossMessage
            START_MESSAGE    = BossMessage.random("phase_shulker_start", 2),
            BETRAYAL_MESSAGE = BossMessage.simple("phase_shulker_betrayal");

    private ShulkerController controller;

    private int tick;
    private int allDoneTick;

    @Override
    public void onStart(EvokerBoss boss, BossContext context) {
        allDoneTick = -1;
        tick = 0;
        controller = new ShulkerController(this, boss, context);
        boss.getPhaseBar().setVisible(true);
        boss.getPhaseBar().setTitle("Guardians, don't stop moving!");

        boss.broadcast(false, START_MESSAGE);

        for (int[] cord: SPAWNS) {
            Location l = new Location(boss.getWorld(), cord[0] + 0.5D, cord[1], cord[2] + 0.5D);

            Shulker s = boss.getWorld().spawn(l, Shulker.class, shulker -> {
                AttackPhases.modifyHealth(shulker, shulker.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), context);
                shulker.setAware(false);
                shulker.customName(Component.text("Shulker Guardian", NamedTextColor.RED));
                shulker.setColor(DyeColor.RED);

                shulker.getPersistentDataContainer().set(SHULKER_STATUS_KEY, PersistentDataType.BYTE, STATUS_FIGHTING);
            });

            ShulkerController.ShulkerData data = new ShulkerController.ShulkerData(s);
            controller.shulkers.add(data);
        }
    }

    void onCompleteBetrayal(EvokerBoss boss) {
        allDoneTick = tick;
        boss.broadcast(false, BETRAYAL_MESSAGE);
    }

    @Override
    public void onEnd(EvokerBoss boss, BossContext context) {
        controller.removeAll();
        controller = null;
    }

    @Override
    public void onTick(EvokerBoss boss, BossContext context) {
        tick++;

        if (allDoneTick != -1 && tick >= (allDoneTick + 50)) {
            // Advance instantly, no transition
            boss.nextPhase(false);
            return;
        }

        double progress = controller.progress();
        boss.getPhaseBar().setProgress(progress);

        controller.tick();
    }

    boolean isPhaseShulker(EntityEvent event) {
        if(!event.getEntity().getPersistentDataContainer().has(SHULKER_STATUS_KEY, PersistentDataType.BYTE)) {
            return false;
        }

        return event.getEntity() instanceof Shulker;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!isPhaseShulker(event)) return;
        Shulker shulker = (Shulker) event.getEntity();

        // Explosions
        // 11 - block_explosion, 12 - entity_explosion
        // ints cuz smaller
        if (event.getCause().ordinal() == 11 || event.getCause().ordinal() == 12) {
            event.setCancelled(true);
        }

        if (shulker.getPersistentDataContainer().get(SHULKER_STATUS_KEY, PersistentDataType.BYTE) == STATUS_TRAITOR) {
            DungeonUtils.cannotHarmEffect(controller.world, shulker);
            event.setCancelled(true);
        }

        double newHealth = shulker.getHealth() - event.getFinalDamage();
        if (newHealth <= 0) {
            event.setCancelled(true);
            shulker.getPersistentDataContainer().set(SHULKER_STATUS_KEY, PersistentDataType.BYTE, STATUS_TRAITOR);
            controller.betray(controller.get(shulker));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        event.setCancelled(isPhaseShulker(event));
    }
}