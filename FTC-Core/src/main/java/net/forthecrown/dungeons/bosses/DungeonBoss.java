package net.forthecrown.dungeons.bosses;

import com.google.common.collect.ImmutableList;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.BossFightContext;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class DungeonBoss<T extends Mob> implements Listener, Keyed {

    protected static final CrownRandom RANDOM = new CrownRandom();

    protected final String name;
    protected final Location spawnLocation;
    protected final FtcBoundingBox bossRoom;
    protected final ImmutableList<ItemStack> requiredToSpawn;
    protected final Key key;

    protected T bossEntity;
    protected BossBar bossBar;
    protected int loopID = 0;

    private boolean alive;
    private final short updaterDelay;
    protected BossFightContext context;

    protected DungeonBoss(String name, Location spawnLocation, short updaterDelay, FtcBoundingBox bossRoom, Collection<ItemStack> requiredItems){
        this.name = name;
        this.spawnLocation = spawnLocation;
        this.bossRoom = bossRoom;
        this.requiredToSpawn = ImmutableList.copyOf(requiredItems);
        this.updaterDelay = updaterDelay;

        key = Keys.ftccore(name.toLowerCase().replaceAll(" ", "_"));
    }

    protected abstract T onSummon(BossFightContext context);
    protected abstract void onUpdate();
    protected abstract void onDeath(BossFightContext context);

    public void onHit(EntityDamageEvent event) {}

    public void summon(){
        //Only 1 boss can exist and a time
        if(isAlive()) return;
        //Register events and start updater loop
        Bukkit.getPluginManager().registerEvents(this, Crown.inst());
        loopID = startUpdater();

        //Summon boss with context and create bossbar
        context = new BossFightContext(this);
        bossEntity = onSummon(context);
        bossEntity.getPersistentDataContainer().set(Bosses.BOSS_TAG, PersistentDataType.BYTE, (byte) 1);
        bossEntity.setLootTable(LootTables.EMPTY.getLootTable());

        createBossbar(context);
        alive = true;
    }

    public void kill(){
        kill(false);
    }

    public void kill(boolean server){
        //Just to prevent exceptions
        if(!isAlive()) return;
        //Unregister events and stop updater loop
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTask(loopID);

        //Destroy bossbar
        bossBar.removeAll();
        bossBar.setVisible(false);
        bossBar = null;

        bossEntity.getWorld().createExplosion(bossEntity.getLocation().add(0, 1, 0), 2.0f, false, false, bossEntity);
        bossEntity.getWorld().playSound(bossEntity.getLocation(), Sound.ENTITY_ENDERMAN_DEATH, 1.0f, 1.0f);

        if(!server) onDeath(context);
        bossEntity.remove();
        bossEntity = null;
        alive = false;
        context = null;
    }

    public void createBossbar(BossFightContext context){
        bossBar = Bukkit.createBossBar(bossEntity.getCustomName(), BarColor.RED, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC, BarFlag.DARKEN_SKY, BarFlag.CREATE_FOG); //<- Should play some epic orchestral music lol
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        for (Player p: context.getPlayers()){
            bossBar.addPlayer(p);
        }
    }

    private int startUpdater(){
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(Crown.inst(), () ->{
            Player target = DungeonUtils.getOptimalTarget(bossEntity, getBossRoom());
            if(target != null && (bossEntity.getTarget() == null || !bossEntity.getTarget().equals(target))) bossEntity.setTarget(target);

            onUpdate();
        }, updaterDelay, updaterDelay);
    }

    private void updateBossbar(){
        bossBar.setProgress(bossEntity.getHealth() / (bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
    }

    public String getName() { return name; }

    public BossFightContext getContext() {
        return context;
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

    @Override
    public @NotNull Key key() {
        return key;
    }

    protected void giveRewards(@Nullable String achievement, @NotNull ItemStack reward, @NotNull BossFightContext context){
        for (Player p: context.getPlayers()){
            if(!getBossRoom().contains(p)) continue;
            if(Crown.getUserManager().isAltForAny(p.getUniqueId(), context.getPlayers())) continue;


            if(!FtcUtils.isNullOrBlank(achievement)) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant " + p.getName() + " only " + achievement);
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

        if(isAlive()){
            player.sendMessage(Component.translatable("dungeons.alreadySpawned").color(NamedTextColor.GRAY));
            return;
        }

        Collection<ItemStack> items = getSpawningItems();
        PlayerInventory inv = player.getInventory();

        for (ItemStack it: items){
            ItemStack i = it.clone();

            if(!inv.containsAtLeast(i, i.getAmount())){
                player.sendMessage(Component.translatable("dungeons.notEnoughItems").color(NamedTextColor.GRAY));
                player.sendMessage(DungeonUtils.itemRequiredMessage(this));
                return;
            }
        }

        inv.removeItemAnySlot(items.toArray(ItemStack[]::new));
        summon();
    }
}
