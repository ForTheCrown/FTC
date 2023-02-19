package net.forthecrown.events.player;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.dungeons.enchantments.FtcEnchants;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class PlayerDeathListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDeath(PlayerDeathEvent event) {
    if (event.getKeepInventory()) {
      return;
    }

    User user = Users.get(event.getEntity());
    Location loc = user.getLocation();

    user.setReturnLocation(loc);

    if (!loc.getWorld().equals(Worlds.voidWorld())) {
      user.sendMessage(Messages.diedAt(loc));
    }

    Loggers.getLogger().info("! {} died at x={} y={} z={} world='{}'",
        user.getName(),
        loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
        loc.getWorld().getName()
    );

    PlayerInventory inventory = event.getEntity().getInventory();
    Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();

    var it = ItemStacks.nonEmptyIterator(inventory);
    while (it.hasNext()) {
      int index = it.nextIndex();
      var item = it.next();

      if (shouldRemainAfterDeath(item)) {
        items.put(index, item);
      }
    }

    if (items.isEmpty()) {
      return;
    }

    event.getDrops().removeAll(items.values());

    Tasks.runLater(() -> {
      PlayerInventory inv = user.getInventory();

      for (var e : items.int2ObjectEntrySet()) {
        inv.setItem(e.getIntKey(), e.getValue());
      }
    }, 1);
  }

  private boolean shouldRemainAfterDeath(ItemStack item) {
    // IDK why, but item.containsEnchantment() doesn't work
    // for this, I guess because custom enchantment or something
    var enchants = item.getEnchantments();

    if (item.getItemMeta() instanceof EnchantmentStorageMeta storageMeta) {
      var storedEnchants = storageMeta.getStoredEnchants();

      if (storedEnchants.containsKey(FtcEnchants.SOUL_BOND)) {
        return true;
      }
    }

    return ExtendedItems.shouldRemainInInventory(item)
        || enchants.containsKey(FtcEnchants.SOUL_BOND);
  }
}