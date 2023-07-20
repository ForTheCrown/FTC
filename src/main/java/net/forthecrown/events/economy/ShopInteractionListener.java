package net.forthecrown.events.economy;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import java.util.Set;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShops;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ShopInteractionListener implements Listener {

  private static final Set<Material> DYES = Set.of(
      Material.WHITE_DYE,
      Material.ORANGE_DYE,
      Material.MAGENTA_DYE,
      Material.LIGHT_BLUE_DYE,
      Material.YELLOW_DYE,
      Material.LIME_DYE,
      Material.PINK_DYE,
      Material.GRAY_DYE,
      Material.LIGHT_GRAY_DYE,
      Material.CYAN_DYE,
      Material.PURPLE_DYE,
      Material.BLUE_DYE,
      Material.BROWN_DYE,
      Material.GREEN_DYE,
      Material.RED_DYE,
      Material.BLACK_DYE,
      Material.GLOW_INK_SAC
  );

  @EventHandler
  public void onSignShopUser(PlayerInteractEvent event) {
    // Not clicking block, not worth our time
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    // Interacted block is not a shop, stop
    if (!SignShops.isShop(event.getClickedBlock())) {
      return;
    }

    var player = event.getPlayer();

    //Can't use in spectator lol
    if (player.getGameMode() == GameMode.SPECTATOR) {
      return;
    }

    // Test player being on a little cooldown
    if (Cooldown.containsOrAdd(player, 6)) {
      return;
    }

    ShopManager manager = Economy.get().getShops();
    SignShop shop = manager.getShop(event.getClickedBlock());

    // If holding a dye, and you own the shop, don't perform any logic
    if (SignShops.mayEdit(shop, player.getUniqueId())) {
      ItemStack held = player.getInventory().getItemInMainHand();

      if (!ItemStacks.isEmpty(held) && DYES.contains(held.getType())) {
        return;
      }
    }

    //Handle interaction between player and shop
    manager.handleInteraction(Users.get(player), shop);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerOpenSign(PlayerOpenSignEvent event) {
    if (SignShops.isShop(event.getSign())) {
      event.setCancelled(true);
    }
  }
}