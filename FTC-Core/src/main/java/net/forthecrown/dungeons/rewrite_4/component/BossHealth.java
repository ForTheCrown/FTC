package net.forthecrown.dungeons.rewrite_4.component;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.rewrite_4.BossComponent;
import net.forthecrown.dungeons.rewrite_4.CompTracker;
import net.forthecrown.dungeons.rewrite_4.DungeonBoss;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.annotation.Nullable;

public class BossHealth extends BossComponent implements BossStatusListener {
    private CompTracker<BossHealthListener> healthListeners;
    private double baseHealth, maxHealth, health;
    private BossBar bar;

    public BossHealth(double baseHealth) {
        this.baseHealth = baseHealth;
    }

    public BossHealth() {
        this(300.0D);
    }

    @Override
    protected void onBossSet(DungeonBoss boss) {
        healthListeners = new CompTracker<>(BossHealthListener.class);
        boss.addTracker(healthListeners);
    }

    @Override
    public void onBossDeath(BossContext context, boolean forced) {
        maxHealth = 0.0D;
        health = 0.0D;

        bar.setVisible(false);
        bar.removeAll();
        bar = null;
    }

    @Override
    public void onBossSummon(BossContext context) {
        maxHealth = context.health(baseHealth);
        health = maxHealth;

        bar = Bukkit.createBossBar(getBoss().getName(), BarColor.RED, BarStyle.SOLID, BarFlag.DARKEN_SKY, BarFlag.CREATE_FOG, BarFlag.CREATE_FOG);
        updateProgress();
        updateViewers();
    }

    public void setBaseHealth(double baseHealth) {
        this.baseHealth = baseHealth;

        // I don't know who or why we would ever update a boss' base health
        // during battle, but if we do, then this should adjust it, at least for
        // the boss bar
        if(isAlive()) {
            double ratio = health / maxHealth;
            this.maxHealth = getCurrentContext().health(baseHealth);
            this.health = maxHealth * ratio;

            healthListeners.forEach(listener -> listener.onHealthSet(this));

            updateProgress();
        }
    }

    public Damage damage(double dmg, @Nullable Entity damager, EntityDamageEvent.DamageCause cause) {
        return damage(dmg, damager, cause, true);
    }

    public Damage damage(double dmg, @Nullable Entity damager, EntityDamageEvent.DamageCause cause, boolean apply) {
        Damage damage = new Damage(dmg, damager, cause);

        // There's an issue with this rn, if we have a component that uses the damage
        // and then after a component modifies the damage, the data the component
        // before it has is invalid, this is limited, but I am so tired of
        // trying to implement this, so I just can't be arsed
        healthListeners.forEach(listener -> listener.onDamage(this, damage));

        if(damage.damage > 0.0D && !damage.cancelled && apply) {
            health = Mth.clamp(health - damage.damage, 0, maxHealth);
            updateProgress();
        }

        return damage;
    }

    public void setHealth(double health) {
        this.health = Mth.clamp(health, 0, maxHealth);

        if (!isAlive()) return;

        healthListeners.forEach(listener -> listener.onHealthSet(this));
        updateProgress();
    }

    public double getBaseHealth() {
        return baseHealth;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getHealth() {
        return health;
    }

    public void updateProgress() {
        if(!isAlive()) return;
        bar.setProgress(health / maxHealth);
    }

    public void updateViewers() {
        if(!isAlive()) return;

        bar.removeAll();
        getBoss().getRoom().getPlayers().forEach(bar::addPlayer);
    }

    public BossBar getBar() {
        return bar;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putDouble("baseHealth", baseHealth);
    }

    @Override
    public void load(CompoundTag tag) {
        baseHealth = tag.getDouble("baseHealth");
    }

    public static class Damage {
        public final double initialDamage;
        public final @Nullable Entity damager;
        public final EntityDamageEvent.DamageCause cause;

        public double damage;
        public boolean cancelled;

        public Damage(double initialDamage, @Nullable Entity damager, EntityDamageEvent.DamageCause cause) {
            this.initialDamage = initialDamage;
            this.damager = damager;
            this.cause = cause;

            this.cancelled = false;
            this.damage = initialDamage;
        }
    }
}