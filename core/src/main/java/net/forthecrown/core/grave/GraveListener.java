package net.forthecrown.core.grave;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.forthecrown.ItemGraveService;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class GraveListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDeath(PlayerDeathEvent event) {
    ItemGraveService service = ItemGraveService.grave();

    Player player = event.getPlayer();
    PlayerInventory inventory = player.getInventory();
    Int2ObjectMap<ItemStack> remaining = new Int2ObjectOpenHashMap<>();

    var it = ItemStacks.nonEmptyIterator(inventory);
    while (it.hasNext()) {
      int index = it.nextIndex();
      ItemStack n = it.next();

      if (service.shouldRemain(n, player)) {
        remaining.put(index, n);
      }
    }

    if (remaining.isEmpty()) {
      return;
    }

    event.getDrops().removeAll(remaining.values());

    Tasks.runLater(() -> {
      PlayerInventory inv = player.getInventory();

      for (Entry<ItemStack> entry: remaining.int2ObjectEntrySet()) {
        inv.setItem(entry.getIntKey(), entry.getValue());
      }
    }, 1);
  }
}