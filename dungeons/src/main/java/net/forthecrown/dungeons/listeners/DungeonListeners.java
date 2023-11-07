package net.forthecrown.dungeons.listeners;

import com.google.common.base.Strings;
import java.util.Random;
import net.forthecrown.dungeons.DungeonAreas;
import net.forthecrown.dungeons.boss.SkalatanBoss;
import net.forthecrown.dungeons.enchantments.DungeonEnchantments;
import net.forthecrown.enchantment.FtcEnchants;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.CaveSpider;
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
import org.bukkit.util.Vector;

public class DungeonListeners implements Listener {

  private final Random random = new Random();

  private static final Component LOST_ITEMS
      = Component.text("You lost a random amount of your Dungeon Items...", NamedTextColor.YELLOW);

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    // Josh, the dude that drops wither goo
    // I don't wanna know where he gets the goo from
    if (Strings.isNullOrEmpty(event.getEntity().getCustomName())) {
      return;
    }

    if (event.getEntity() instanceof WitherSkeleton skeleton
        && event.getEntity().getCustomName().contains("Josh")
        && DungeonAreas.DUNGEON_AREA.contains(event.getEntity())
        && random.nextInt(4) == 0
    ) {
      ItemStack item = SkalatanBoss.witherGoo();

      item.setAmount(1);
      skeleton.getWorld().dropItemNaturally(skeleton.getLocation(), item);
    }
  }

  public static ItemStack createFork() {
    ItemStack item = ItemStacks.builder(Material.TRIDENT, 1)
        .setNameRaw(Component.text("Fork").decorate(TextDecoration.BOLD))
        .build();

    FtcEnchants.addEnchant(item, DungeonEnchantments.DOLPHIN_SWIMMER, 1);

    return item;
  }

  // Removes random amount of items on death
  @EventHandler(ignoreCancelled = true)
  public void onPlayerDeath(PlayerDeathEvent event) {
    boolean found = false;

    for (ItemStack item : event.getEntity().getInventory().getContents()) {
      if (ItemStacks.isEmpty(item)) {
        continue;
      }

      if (item.getItemMeta().getLore() != null
          && item.getItemMeta().getLore().contains("Dungeon Item")
      ) {
        found = true;

        if (item.getAmount() >= 10) {
          item.subtract(random.nextInt(0, 11));
        } else {
          item.setAmount(random.nextInt(0, item.getAmount() + 1));
        }
      }
    }

    if (found) {
      event.getEntity().sendMessage(LOST_ITEMS);
    }
  }

  // Spawns baby spiders when mommy spider is killed
  @EventHandler(ignoreCancelled = true)
  public void onMotherKill(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null
        || !(event.getEntity() instanceof Spider)
        || event.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() != 75d
    ) {
      return;
    }

    Location spawnLoc = event.getEntity().getLocation();

    Tasks.runLater(() -> {
      for (int i = 0; i <= 2; i++) {
        CaveSpider caveSpider = spawnLoc.getWorld().spawn(
            spawnLoc.add(new Vector(0.2 * i * Math.pow(-1, i), i * 0.1, 0.2 * i * Math.pow(-1, i))),
            CaveSpider.class
        );

        caveSpider.setLootTable(LootTables.EMPTY.getLootTable());
        caveSpider.setHealth(1);
      }
    }, 15L);
  }
}