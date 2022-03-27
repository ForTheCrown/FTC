package net.forthecrown.dungeons.rewrite_4;

import net.forthecrown.dungeons.boss.BossContext;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public abstract class BossComponent {
    private DungeonBoss boss;

    final void setBoss(DungeonBoss boss) {
        this.boss = boss;
    }

    public final DungeonBoss getBoss() {
        return boss;
    }

    protected boolean isAlive() {
        return boss != null && boss.isAlive();
    }

    protected <T> CompTracker<T> getFamily(Class<T> clazz) {
        return getBoss().getTracker(clazz);
    }

    protected <T> T getComponent(Class<T> clazz) {
        return getBoss().getComponent(clazz);
    }

    protected <T> List<T> getComponents(Class<T> clazz) {
        return getBoss().getComponents(clazz);
    }

    protected BossContext getCurrentContext() {
        return getBoss().getCurrentContext();
    }

    protected Location getSpawnLocation() {
        return getBoss().getSpawnLocation();
    }

    protected World getWorld() {
        return getBoss().getWorld();
    }

    protected void onBossSet(DungeonBoss boss) {}

    public void save(CompoundTag tag) {}
    public void load(CompoundTag tag) {}
}