package net.forthecrown.royals.dungeons.bosses.mobs;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.royals.RoyalUtils;
import net.forthecrown.royals.Royals;
import net.forthecrown.royals.dungeons.bosses.BossFightContext;
import net.forthecrown.royals.dungeons.bosses.Bosses;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public abstract class DungeonBoss<T extends Mob> implements Listener {

    protected static final ThreadLocalRandom random = ThreadLocalRandom.current();

    protected final Royals plugin;
    protected final Location spawnLocation;
    protected final CrownBoundingBox bossRoom;
    protected final Collection<ItemStack> requiredToSpawn;
    protected T bossEntity;
    protected BossBar bossBar;
    protected int loopID = 0;

    private boolean alive;
    private final short updaterDelay;
    protected BossFightContext context;

    protected DungeonBoss(Royals plugin, String name, Location spawnLocation, short updaterDelay, CrownBoundingBox bossRoom, Collection<ItemStack> requiredItems){
        this.plugin = plugin;
        this.spawnLocation = spawnLocation;
        this.bossRoom = bossRoom;
        this.requiredToSpawn = requiredItems;
        this.updaterDelay = updaterDelay;
        Bosses.BY_NAME.put(name, this);
    }

    protected abstract T onSummon(BossFightContext context);
    protected abstract void onUpdate();
    protected abstract void onDeath(BossFightContext context);

    public void onHit(EntityDamageEvent event) {}

    public void summon(){
        //Only 1 boss can exist and a time
        if(alive) return;
        //Register events and start updater loop
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loopID = startUpdater();

        //Summon boss with context and create bossbar
        context = new BossFightContext(this);
        bossEntity = onSummon(context);
        bossEntity.setLootTable(LootTables.EMPTY.getLootTable());
        createBossbar(context);
        bossBar.setProgress(1.0);
        alive = true;
    }

    public void kill(){
        kill(false);
    }

    public void kill(boolean server){
        //Just to prevent exceptions
        if(!alive) return;
        //Unregister events and stop updater loop
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTask(loopID);

        //Destroy bossbar
        bossBar.removeAll();
        bossBar.setVisible(false);
        bossBar = null;

        if(!server) onDeath(context);
        bossEntity.remove();
        bossEntity = null;
        alive = false;
        context = null;
    }

    public void createBossbar(BossFightContext context){
        bossBar = Bukkit.createBossBar(bossEntity.getCustomName(), BarColor.RED, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC); //<- Should play some epic orchestral music lol
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        for (Player p: context.players()){
            bossBar.addPlayer(p);
        }
    }

    private int startUpdater(){
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () ->{
            Player target = RoyalUtils.getOptimalTarget(bossEntity, bossRoom());
            if(target != null && (bossEntity.getTarget() == null || !bossEntity.getTarget().equals(target))) bossEntity.setTarget(target);

            onUpdate();
        }, updaterDelay, updaterDelay);
    }

    private void updateBossbar(){
        bossBar.setProgress(bossEntity.getHealth() / (bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
    }

    public BossFightContext getContext() {
        return context;
    }

    public boolean isAlive() {
        return alive;
    }

    public CrownBoundingBox bossRoom() {
        return bossRoom;
    }

    public T getBossEntity() {
        return bossEntity;
    }

    public Collection<ItemStack> getSpawningItems() {
        return requiredToSpawn;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    protected void giveRewards(@Nullable String achievement, @NotNull ItemStack reward, @NotNull BossFightContext context){
        for (Player p: context.players()){
            if(!bossRoom().contains(p)) continue;

            if(!CrownUtils.isNullOrBlank(achievement)) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant " + p.getName() + " only " + achievement);
            if(p.getInventory().firstEmpty() == -1) bossEntity.getWorld().dropItemNaturally(bossEntity.getLocation(), reward.clone());
            else p.getInventory().addItem(reward.clone());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if(!event.getEntity().equals(bossEntity)) return;
        onHit(event);
        updateBossbar();
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if(!event.getEntity().equals(bossEntity)) return;
        kill();
    }

    public void attemptSpawn(Player player){
        Validate.notNull(player, "Player is null");

        Collection<ItemStack> items = getSpawningItems();
        PlayerInventory inv = player.getInventory();

        for (ItemStack it: items){
            ItemStack i = it.clone();

            if(!inv.containsAtLeast(i, i.getAmount())){
                player.sendMessage(Component.text("Not enough items to spawn the boss").color(NamedTextColor.GRAY));
                player.sendMessage(RoyalUtils.itemRequiredMessage(this));
                return;
            }
        }

        ItemStack[] toRemove = new ItemStack[items.size()];
        inv.removeItemAnySlot(items.toArray(toRemove));
        summon();
    }
}
