package net.forthecrown.events;

import net.forthecrown.core.Crown;
import net.forthecrown.dungeons.DungeonConstants;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.SkalatanBoss;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.squire.enchantment.RoyalEnchant;
import net.forthecrown.squire.enchantment.RoyalEnchants;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Spider;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * THIS DESPERATLY NEEDS TO BE REWRITTEN LMAO
 * Hot garbage, is what this is
 */
public class DungeonListeners implements Listener {
    private final CrownRandom random = new CrownRandom();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Punching bag
        if(event.getEntity().getPersistentDataContainer().has(DungeonUtils.PUNCHING_BAG_KEY, PersistentDataType.BYTE)) {
            Husk dummy = (Husk) event.getEntity();
            PunchingBagTracker tracker = hit.computeIfAbsent(dummy.getUniqueId(), uuid -> new PunchingBagTracker(dummy));

            tracker.hit(event.getFinalDamage());

            return;
        }

        // Josh, the dude that drops wither goo
        // I don't wanna know where he gets the goo from
        if(FtcUtils.isNullOrBlank(event.getEntity().getCustomName())) return;
        if(event.getEntity() instanceof WitherSkeleton && event.getEntity().getCustomName().contains("Josh") && DungeonConstants.DUNGEON_AREA.contains(event.getEntity())){
            if(random.nextInt(4) > 0) return;

            WitherSkeleton skeleton = (WitherSkeleton) event.getEntity();
            ItemStack item = SkalatanBoss.witherGoo();
            item.setAmount(1);
            skeleton.getWorld().dropItemNaturally(skeleton.getLocation(), item);
        }
    }

    private final Map<UUID, PunchingBagTracker> hit = new HashMap<>();
    public static class PunchingBagTracker {
        BukkitTask task;
        final Husk dummy;

        public PunchingBagTracker(Husk husk) {
            this.dummy = husk;
        }

        void hit(double damage) {
            Component name = Component.text("Damage: " + String.format("%.2f",damage)).color(NamedTextColor.RED);
            dummy.customName(name);

            taskLogic();
        }

        void taskLogic() {
            if(task != null && !task.isCancelled()) {
                task.cancel();
            }

            task = Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
                dummy.setHealth(dummy.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                dummy.customName(Component.text("Hit Me!").color(NamedTextColor.GOLD));
            }, 100);
        }
    }

    private static final ItemStack FORK = Util.make(() -> {
        ItemStack item = new ItemStackBuilder(Material.TRIDENT, 1)
                .setName(Component.text("Fork").decorate(TextDecoration.BOLD))
                .build();
        RoyalEnchant.addCrownEnchant(item, RoyalEnchants.dolphinSwimmer(), 1);

        return item;
    });

    public static ItemStack fork() {
        return FORK.clone();
    }

    // Removes random amount of items on death
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        boolean found = false;

        for (ItemStack item : event.getEntity().getInventory().getContents()) {
            if(ItemStacks.isEmpty(item)) continue;

            if (item.getItemMeta().getLore() != null && item.getItemMeta().getLore().contains("Dungeon Item")) {
                found = true;
                if (item.getAmount() >= 10) item.setAmount(item.getAmount() - random.nextInt(0, 11));
                else item.setAmount(random.nextInt(0, item.getAmount()+1));
            }
        }

        if (found) {
            event.getEntity().sendMessage(
                    Component.translatable("dungeons.lostItems", NamedTextColor.YELLOW)
            );
        }
    }

    // Spawns baby spiders when mommy spider is killed
    @EventHandler(ignoreCancelled = true)
    public void onMotherKill(EntityDeathEvent event){
        //If not mommy :weary:
        if (event.getEntity().getKiller() == null
                || !(event.getEntity() instanceof Spider)
                || event.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() != 75d
        ) {
            return;
        }

        Location spawnLoc = event.getEntity().getLocation();

        Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.inst(), () -> {
            for (int i = 0; i <= 2; i++) {
                CaveSpider caveSpider = spawnLoc.getWorld().spawn(spawnLoc.add(new Vector(0.2 * i * Math.pow(-1, i), i * 0.1, 0.2 * i * Math.pow(-1, i))), CaveSpider.class);
                caveSpider.setLootTable(LootTables.EMPTY.getLootTable());
                caveSpider.setHealth(1);
            }
        }, 15L);
    }
}