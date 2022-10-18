package net.forthecrown.dungeons.boss;

import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.boss.components.BossComponent;
import net.forthecrown.dungeons.boss.components.EmptyRoomComponent;
import net.forthecrown.dungeons.boss.components.InsideRoomComponent;
import net.forthecrown.dungeons.boss.components.TargetUpdateComponent;
import net.forthecrown.utils.math.WorldBounds3i;
import net.minecraft.util.Mth;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A simple dungeon boss used by the first 4 dungeon bosses
 */
public abstract class SimpleBoss extends KeyedBossImpl implements SingleEntityBoss {
    protected Mob entity;

    public SimpleBoss(String name, Location location, WorldBounds3i room, ItemStack... items) {
        super(name, location, room, SpawnTest.items(items));
    }

    @Override
    protected void createComponents(Set<BossComponent> c) {
        c.add(TargetUpdateComponent.create());
        c.add(InsideRoomComponent.create());
        c.add(EmptyRoomComponent.create(this));
    }

    // Methods subclasses must or should implement
    // The only required one is the one which creates
    // the boss entity
    protected abstract Mob onSpawn(BossContext context);
    protected void onDeath(BossContext context) {}
    protected void onHit(BossContext context, EntityDamageEvent event) {}

    @Override
    public void spawn() {
        // Don't spawn more than 1 boss
        if(isAlive()) return;

        // Get everything that's needed started
        registerEvents();
        startTickTask();

        // Create context and entity
        currentContext = BossContext.create(getRoom());
        createBossBar(currentContext);

        entity = onSpawn(currentContext);

        logSpawn(currentContext);

        // Give boss the boss tag and an empty loottable
        entity.getPersistentDataContainer()
                .set(Bosses.BOSS_TAG, PersistentDataType.STRING, getKey());

        entity.setLootTable(LootTables.EMPTY.getLootTable());

        runComponents(component -> component.onSpawn(this, currentContext));

        alive = true;
    }

    @Override
    public void kill(boolean force) {
        if(!isAlive()) return;

        // Destroy the basic things first
        unregisterEvents();
        stopTickTask();
        destroyBossBar();

        // Play some death effects
        getWorld().createExplosion(entity.getLocation().add(0, 1, 0), 2.0f, false, false, entity);
        getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_DEATH, 1.0f, 1.0f);

        // If we're not being forced, run the onDeath functions
        // as well as giving away the awards for this boss
        if(!force) {
            onDeath(currentContext);
            finalizeKill(currentContext);
        }

        runComponents(component -> component.onDeath(this, currentContext, force));

        // Nullify everything
        entity.remove();
        entity = null;
        alive = false;
        currentContext = null;
    }

    @Override
    public @Nullable Mob getBossEntity() {
        return entity;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!event.getEntity().equals(entity)) return;
        onHit(currentContext, event);

        double newHealth = entity.getHealth() - event.getFinalDamage();
        newHealth = Mth.clamp(newHealth, 0, 1);
        // Update boss bar
        bossBar.setProgress(newHealth / entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        runComponents(component -> component.onHit(this, currentContext, event));
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!event.getEntity().equals(entity)) return;
        kill();
    }
}