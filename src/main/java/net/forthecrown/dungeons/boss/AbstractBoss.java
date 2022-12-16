package net.forthecrown.dungeons.boss;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.forthecrown.core.FTC;
import net.forthecrown.dungeons.boss.components.BossComponent;
import net.forthecrown.utils.math.WorldBounds3i;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractBoss implements DungeonBoss {
    protected static final Logger LOGGER = FTC.getLogger();

    // Final variables that give details about boss
    private final String name;
    private final SpawnTest requirement;
    private final Set<BossComponent> components;
    private final WorldBounds3i room;
    private final Location spawn;

    // Variables used when boss is alive
    protected boolean alive;
    protected BossBar bossBar;
    protected BukkitTask tickTask;
    protected BossContext currentContext;

    public AbstractBoss(String name, Location spawn, WorldBounds3i room, SpawnTest requirement) {
        this.name = name;
        this.room = room;
        this.spawn = spawn.clone();
        this.requirement = requirement;

        ObjectSet<BossComponent> components = new ObjectOpenHashSet<>();
        createComponents(components);

        // If we don't have any components created, there's no point
        // in taking up memory with an empty set
        this.components = components.isEmpty() ? ObjectSets.emptySet() : ObjectSets.unmodifiable(components);
    }

    // Methods for subclasses to implement, since they
    // don't *have* to actually implement them, they're
    // not abstract
    protected void createComponents(Set<BossComponent> c) {}
    protected void tick() {}

    // -------------------------------------------------
    //         Methods for subclasses to use
    // -------------------------------------------------

    protected void createBossBar(BossContext context) {
        bossBar = Bukkit.createBossBar(getName(), BarColor.RED, BarStyle.SOLID, BarFlag.CREATE_FOG, BarFlag.DARKEN_SKY, BarFlag.PLAY_BOSS_MUSIC);
        bossBar.setProgress(1D);

        for (Player p: context.players()) {
            bossBar.addPlayer(p);
        }
    }

    protected void updateBossbarViewers() {
        bossBar.removeAll();
        getRoom().getPlayers().forEach(bossBar::addPlayer);
    }

    protected void destroyBossBar() {
        if(bossBar == null) return;
        bossBar.setVisible(false);
        bossBar.removeAll();

        bossBar = null;
    }

    protected void startTickTask() {
        stopTickTask(); // Ensure there isn't already a tick task running

        tickTask = Bukkit.getScheduler().runTaskTimer(FTC.getPlugin(), () -> {
            tick();
            runComponents(component -> component.onTick(this, currentContext()));
        }, 1L, 1L);
    }

    protected void stopTickTask() {
        if(tickTask == null || tickTask.isCancelled()) return;
        tickTask.cancel();
        tickTask = null;
    }

    protected void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, FTC.getPlugin());

        runComponents(component -> {
            Bukkit.getPluginManager().registerEvents(component, FTC.getPlugin());
        });
    }

    protected void unregisterEvents() {
        HandlerList.unregisterAll(this);
        runComponents(HandlerList::unregisterAll);
    }

    @Override
    public SpawnTest getSpawnRequirement() {
        return requirement;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public Set<BossComponent> getComponents() {
        return components;
    }

    @Override
    public <T extends BossComponent> T getComponent(Class<T> clazz) {
        if (components.isEmpty()) {
            return null;
        }

        for (BossComponent c: getComponents()) {
            if(clazz.isAssignableFrom(c.getClass())) return (T) c;
        }

        return null;
    }

    protected void runComponents(Consumer<BossComponent> consumer) {
        if (components.isEmpty()) {
            return;
        }

        components.forEach(consumer);
    }

    // Getters :D

    @Override
    public BossContext currentContext() {
        return currentContext;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public WorldBounds3i getRoom() {
        return room;
    }

    @Override
    public Location getSpawn() {
        return spawn.clone();
    }

    @Override
    public @NotNull World getWorld() {
        return spawn.getWorld();
    }

    protected void logSpawn(BossContext context) {
        String players = Joiner.on(", ")
                .join(
                        context.players().stream()
                                .map(Player::getName)
                                .toList()
                );

        LOGGER.info("{} spawned, context: players: [{}], difficulty: {}", getName(), players, context.modifier());
    }
}