package net.forthecrown.events.economy;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldguard.protection.flags.StateFlag;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FtcFlags;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.economy.shops.ShopType;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShops;
import net.forthecrown.events.Events;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopCreateListener implements Listener {

  public static final String REGEX_START = "(|-|=)(|\\[|<|\\()";

  // The order of the braces matters here apparently,
  // the longer ones must be first
  public static final String REGEX_END = "(]|\\)|>|)(=|-|)";

  public static final Pattern SELL_PATTERN
      = Pattern.compile(REGEX_START + "sell" + REGEX_END, CASE_INSENSITIVE);

  public static final Pattern BUY_PATTERN
      = Pattern.compile(REGEX_START + "buy" + REGEX_END, CASE_INSENSITIVE);

  @EventHandler(ignoreCancelled = true)
  public void onSignShopCreate(SignChangeEvent event1) {
    Events.runSafe(event1.getPlayer(), event1, this::onShopCreateThrowing);
  }

  private void onShopCreateThrowing(SignChangeEvent event)
      throws CommandSyntaxException
  {
    if (Text.toString(event.line(0)).isBlank()) {
      return;
    }

    Player player = event.getPlayer();

    String line0 = Text.plain(event.line(0)).trim();
    String line1 = Text.plain(event.line(1));
    String line2 = Text.plain(event.line(2));
    String line3 = Text.plain(event.line(3));

    ShopType shopType;

    if (SELL_PATTERN.matcher(line0).matches()) {
      shopType = ShopType.SELL;
    } else if (BUY_PATTERN.matcher(line0).matches()) {
      shopType = ShopType.BUY;
    } else {
      // Top line matches neither of the type patterns, therefor
      // the player is probably not even trying to make a shop sign
      return;
    }

    if (player.getGameMode() == GameMode.CREATIVE
        && player.hasPermission(Permissions.SHOP_ADMIN)
    ) {
      shopType = shopType.toAdmin();
    }

    // No price given
    if (line3.isBlank()) {
      throw Exceptions.SHOP_NO_PRICE;
    }

    Sign sign = (Sign) event.getBlock().getState();
    String lastLine = line3.toLowerCase()
        .replaceAll("\\D", "")
        .trim();

    // Parse price
    int price;
    try {
      price = Integer.parseInt(lastLine);
    } catch (Exception e) {
      throw Exceptions.SHOP_NO_PRICE;
    }

    // Make sure they don't exceed the max shop price
    if (price > GeneralConfig.maxSignShopPrice) {
      throw Exceptions.shopMaxPrice();
    }

    // They must give at least one line of info about the shop
    if (line2.isBlank() && line1.isBlank()) {
      throw Exceptions.SHOP_NO_DESC;
    }

    //WorldGuard flag check
    StateFlag.State flagState = FtcFlags.query(
        event.getBlock().getLocation(),
        FtcFlags.SHOP_CREATION
    );

    if (flagState == StateFlag.State.DENY
        && !player.hasPermission(Permissions.ADMIN)
    ) {
      player.sendMessage(Messages.WG_CANNOT_MAKE_SHOP);
      return;
    }

    ShopManager shopManager = Economy.get().getShops();

    //creates the sign shop
    SignShop shop = shopManager.createSignShop(
        sign.getLocation(),
        shopType,
        price,
        player.getUniqueId()
    );

    //Opens the example item selection screen
    player.openInventory(SignShops.createExampleInventory());
    Events.register(new ExampleItemSelectionListener(player, shop));

    if (shopType == ShopType.BUY) {
      event.line(0, shopType.getUnStockedLabel());
    } else {
      event.line(0, shopType.getStockedLabel());
    }

    event.line(3, SignShops.priceLine(price, NamedTextColor.WHITE));
  }

  @RequiredArgsConstructor
  public static class ExampleItemSelectionListener implements Listener {

    private final Player player;
    private final SignShop shop;

    @EventHandler
    public void invClickEvent(InventoryClickEvent event) {
      if (event.getCurrentItem() == null
          || !event.getWhoClicked().equals(player)
      ) {
        return;
      }

      if (event.getCurrentItem().getType() == Material.BARRIER) {
        event.setCancelled(true);
      }
    }

    //sets the example item and adds the item(s) to the shop's inventory
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
      if (!event.getPlayer().equals(player)
          || event.getInventory().getType() != InventoryType.HOPPER
      ) {
        return;
      }

      Player player = (Player) event.getPlayer();

      Events.unregister(this);

      Inventory inv = event.getInventory();
      var shopInv = shop.getInventory();

      ItemStack item = inv.getContents()[SignShops.EXAMPLE_ITEM_SLOT];

      //If example item was not found: destroy shop and tell them why they failed
      if (ItemStacks.isEmpty(item)) {
        shop.destroy(false);
        player.sendMessage(Messages.SHOP_CREATE_FAILED);

        return;
      }

      //Add the item to the inventory
      shop.setExampleItem(item);
      shopInv.addItem(item.clone());

      //Send the info message
      player.sendMessage(Messages.createdShop(shop));

      player.playSound(
          player.getLocation(),
          Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
          SoundCategory.MASTER,
          1, 1
      );

      shop.update();
      shop.delayUnload();
    }
  }
}