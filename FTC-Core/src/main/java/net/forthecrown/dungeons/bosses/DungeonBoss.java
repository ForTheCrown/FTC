package net.forthecrown.dungeons.bosses;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.BossFightContext;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserDataContainer;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class DungeonBoss<T extends Mob> implements Listener, Keyed, JsonSerializable {

    protected static final CrownRandom RANDOM = new CrownRandom();

    protected final String name;
    protected final Location spawnLocation;
    protected final FtcBoundingBox bossRoom;
    protected final ImmutableList<ItemStack> requiredToSpawn;
    protected final Key key;
    protected final BossItems items;
    private final short tickInterval;
    protected T bossEntity;
    protected BossBar bossBar;
    protected BukkitTask tickTask;
    protected BossFightContext context;
    private boolean alive;

    protected DungeonBoss(String name, Location spawnLocation, short tickInterval, FtcBoundingBox bossRoom, Collection<ItemStack> requiredItems) {
        this.name = name;
        this.spawnLocation = spawnLocation;
        this.bossRoom = bossRoom;
        this.items = BossItems.valueOf(name.toUpperCase().replaceAll(" ", "_"));
        this.requiredToSpawn = ImmutableList.copyOf(requiredItems);
        this.tickInterval = tickInterval;

        key = Keys.forthecrown(name.toLowerCase().replaceAll(" ", "_"));
    }

    protected abstract T onSummon(BossFightContext context);
    protected abstract void onUpdate();
    protected abstract void onDeath(BossFightContext context);

    public void onHit(EntityDamageEvent event) {}

    public void summon() {
        //Only 1 boss can exist and a time
        if (isAlive()) return;
        //Register events and start updater loop
        Bukkit.getPluginManager().registerEvents(this, Crown.inst());
        tickTask = startUpdater();

        //Summon boss with context and create bossbar
        context = new BossFightContext(this);
        bossEntity = onSummon(context);
        bossEntity.getPersistentDataContainer().set(Bosses.BOSS_TAG, PersistentDataType.STRING, key.asString());
        bossEntity.setLootTable(LootTables.EMPTY.getLootTable());

        createBossbar(context);
        alive = true;
    }

    public void kill() {
        kill(false);
    }

    public void kill(boolean server) {
        //Just to prevent exceptions
        if (!isAlive()) return;
        //Unregister events and stop updater loop
        HandlerList.unregisterAll(this);

        // Stop tick updater
        if (tickTask != null && !tickTask.isCancelled()) {
            tickTask.cancel();
            tickTask = null;
        }

        //Destroy bossbar
        bossBar.removeAll();
        bossBar.setVisible(false);
        bossBar = null;

        bossEntity.getWorld().createExplosion(bossEntity.getLocation().add(0, 1, 0), 2.0f, false, false, bossEntity);
        bossEntity.getWorld().playSound(bossEntity.getLocation(), Sound.ENTITY_ENDERMAN_DEATH, 1.0f, 1.0f);

        if (!server) {
            onDeath(context);
            finalizeKill(context);
        }

        bossEntity.remove();
        bossEntity = null;
        alive = false;
        context = null;
    }

    public void createBossbar(BossFightContext context) {
        bossBar = Bukkit.createBossBar(bossEntity.getCustomName(), BarColor.RED, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC, BarFlag.DARKEN_SKY, BarFlag.CREATE_FOG); //<- Should play some epic orchestral music lol
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        for (Player p : context.getPlayers()) {
            bossBar.addPlayer(p);
        }
    }

    private BukkitTask startUpdater() {
        return Bukkit.getScheduler().runTaskTimer(Crown.inst(), () -> {
            Player target = DungeonUtils.getOptimalTarget(bossEntity, getBossRoom());

            // Change target only if found target is not already target
            if (target != null && (bossEntity.getTarget() == null || !bossEntity.getTarget().equals(target))) {
                bossEntity.setTarget(target);
            }

            // Do not allow boss entity to leave room
            if (!getBossRoom().contains(bossEntity)) {
                bossEntity.teleport(getSpawnLocation());
            }

            onUpdate();
        }, tickInterval, tickInterval);
    }

    private void updateBossbar() {
        bossBar.setProgress(bossEntity.getHealth() / (bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
    }

    public String getName() {
        return name;
    }

    public boolean isAlive() {
        return alive;
    }

    public FtcBoundingBox getBossRoom() {
        return bossRoom;
    }

    public T getBossEntity() {
        return bossEntity;
    }

    public ImmutableList<ItemStack> getSpawningItems() {
        return requiredToSpawn;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public BossItems getItems() {
        return items;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public Location getSpawnLocation() {
        return spawnLocation.clone();
    }

    private void finalizeKill(@NotNull BossFightContext context) {
        for (Player p : context.getPlayers()) {
            if (!getBossRoom().contains(p)) continue;
            if (Crown.getUserManager().isAltForAny(p.getUniqueId(), context.getPlayers())) continue;

            CrownUser user = UserManager.getUser(p);
            UserDataContainer container = user.getDataContainer();
            Bosses.getAccessor().setStatus(container, this, true);

            awardAdvancement(p);

            if (p.getInventory().firstEmpty() == -1) {
                bossEntity.getWorld().dropItemNaturally(bossEntity.getLocation(), items.item());
            } else {
                p.getInventory().addItem(items.item());
            }
        }
    }

    private void awardAdvancement(Player player) {
        Advancement advancement = Bukkit.getAdvancement(advancementKey());
        if (advancement == null) {
            Crown.logger().warn(key() + " has no advancement");
            return;
        }

        FtcUtils.grantAdvancement(advancement, player);
    }

    public NamespacedKey advancementKey() {
        return Keys.forthecrown("dungeons/" + key().value());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!event.getEntity().equals(bossEntity)) return;
        onHit(event);
        updateBossbar();
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!event.getEntity().equals(bossEntity)) return;
        kill();
    }

    public void attemptSpawn(Player player) {
        Validate.notNull(player, "Player is null");

        if (isAlive()) {
            player.sendMessage(Component.translatable("dungeons.alreadySpawned").color(NamedTextColor.GRAY));
            return;
        }

        Collection<ItemStack> items = getSpawningItems();
        PlayerInventory inv = player.getInventory();

        for (ItemStack it : items) {
            ItemStack i = it.clone();

            if (!inv.containsAtLeast(i, i.getAmount())) {
                player.sendMessage(Component.translatable("dungeons.notEnoughItems").color(NamedTextColor.GRAY));
                player.sendMessage(DungeonUtils.itemRequiredMessage(this));
                return;
            }
        }

        inv.removeItemAnySlot(items.toArray(ItemStack[]::new));
        summon();
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeKey(key);
    }
}
