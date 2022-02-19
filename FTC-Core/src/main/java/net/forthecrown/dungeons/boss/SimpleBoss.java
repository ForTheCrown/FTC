package net.forthecrown.dungeons.boss;

import net.forthecrown.core.Crown;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.boss.components.BossComponent;
import net.forthecrown.dungeons.boss.components.TargetUpdateComponent;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserDataContainer;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
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
    protected final Location spawnLocation;

    protected Mob entity;

    public SimpleBoss(String name, Location location, FtcBoundingBox room, ItemStack... items) {
        super(name, room, items);
        this.spawnLocation = location.clone();
    }

    @Override
    protected void createComponents(Set<BossComponent> c) {
        c.add(TargetUpdateComponent.getInstance());
    }

    // Methods subclasses must or should implement
    // The only required one is the one which creates
    // the boss entity
    protected abstract Mob onSpawn(BossContext context);
    protected void onDeath(BossContext context) {}
    protected void onHit(BossContext context, EntityDamageEvent event) {}
    protected void giveRewards(Player player) {}

    @Override
    public void spawn() {
        // Don't spawn more than 1 boss
        if(isAlive()) return;

        // Get everything that's needed started
        registerEvents();
        startTickTask();
        createBossBar(currentContext);

        // Create context and entity
        currentContext = BossContext.create(getRoom());
        entity = onSpawn(currentContext);

        // Give boss the boss tag and an empty loottable
        entity.getPersistentDataContainer().set(Bosses.BOSS_TAG, PersistentDataType.STRING, key().asString());
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
    protected void tick() {
        // Ensure the boss isn't outside the room
        if(!getRoom().contains(entity)) {
            entity.teleport(getSpawnLocation());
        }
    }

    @Override
    public @Nullable Mob getBossEntity() {
        return entity;
    }

    public Location getSpawnLocation() {
        return spawnLocation.clone();
    }

    private void finalizeKill(BossContext context) {
        for (Player p: context.players()) {
            // Players outside of the room during the kill,
            // or alt accounts, cannot earn rewards
            if(!getRoom().contains(p)) continue;
            if(Crown.getUserManager().isAltForAny(p.getUniqueId(), context.players())) continue;

            CrownUser user = UserManager.getUser(p);

            // I forgot why this exists, but it does lol
            UserDataContainer container = user.getDataContainer();
            Bosses.ACCESSOR.setStatus(container, this, true);

            // Give the advancement and
            // any other awards
            awardAdvancement(p);
            giveRewards(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!event.getEntity().equals(entity)) return;
        onHit(currentContext, event);

        // Update boss bar
        bossBar.setProgress(entity.getHealth() / entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        runComponents(component -> component.onHit(this, currentContext, event));
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!event.getEntity().equals(entity)) return;
        kill();
    }
}
