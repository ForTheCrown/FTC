package net.forthecrown.events;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.npc.Npcs;
import net.forthecrown.dungeons.enchantments.FtcEnchants;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.ChatParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataType;

public class CoreListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    if (event.getBlock().getType() != Material.HOPPER
        || GeneralConfig.hoppersInOneChunk == -1
    ) {
      return;
    }

    int hopperAmount = event.getBlock()
        .getChunk()
        .getTileEntities(block -> block.getType() == Material.HOPPER, true)
        .size();

    if (hopperAmount <= GeneralConfig.hoppersInOneChunk) {
      return;
    }

    event.setCancelled(true);
    event.getPlayer().sendMessage(Messages.tooManyHoppers());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onSignChange(SignChangeEvent event) {
    Player player = event.getPlayer();

    // Disable case checking on signs
    ChatParser parser = ChatParser.of(player)
        .addFlags(ChatParser.FLAG_IGNORE_CASE);

    for (int i = 0; i < 4; i++) {
      event.line(i, parser.render(event.getLine(i)));
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (ExtendedItems.isSpecial(event.getItemDrop().getItemStack())) {
      event.getPlayer().sendActionBar(
          Messages.CANNOT_DROP_SPECIAL
      );

      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (!event.getRightClicked()
        .getPersistentDataContainer()
        .has(Npcs.KEY, PersistentDataType.STRING)
    ) {
      return;
    }

    event.setCancelled(true);
    event.setCancelled(
        Npcs.interact(
            event.getRightClicked().getPersistentDataContainer()
                .get(Npcs.KEY, PersistentDataType.STRING),
            event.getRightClicked(),
            event.getPlayer(),
            event.isCancelled()
        )
    );
  }

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