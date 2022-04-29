package net.forthecrown.events;

import com.sk89q.worldguard.protection.flags.StateFlag;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcFlags;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.shops.*;
import net.forthecrown.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopCreateListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onSignShopCreate(SignChangeEvent event1) {
        Events.handle(event1.getPlayer(), event1, event -> {
            if(ChatUtils.getString(event.line(0)).isBlank()) return;

            Player player = event.getPlayer();

            String line0 = ChatUtils.plainText(event.line(0));
            String line1 = ChatUtils.plainText(event.line(1));
            String line2 = ChatUtils.plainText(event.line(2));
            String line3 = ChatUtils.plainText(event.line(3));

            ShopType shopType;
            switch (line0.toLowerCase()) {
                default: return; //switch statement to set the shop's type, basically that's it

                case "-[buy]-":
                case "=[buy]=":
                case "(buy)":
                case "[buy]":
                case "<buy>":
                    if(player.hasPermission(Permissions.SHOP_ADMIN) && player.getGameMode() == GameMode.CREATIVE) shopType = ShopType.ADMIN_BUY;
                    else shopType = ShopType.BUY;
                    break;

                case "-[sell]-":
                case "=[sell]=":
                case "(sell)":
                case "[sell]":
                case "<sell>":
                    if(player.hasPermission(Permissions.SHOP_ADMIN) && player.getGameMode() == GameMode.CREATIVE) shopType = ShopType.ADMIN_SELL;
                    else shopType = ShopType.SELL;
                    break;
            }

            //No price given
            if(line3.isBlank()) throw FtcExceptionProvider.translatable("shops.created.failed.noPrice");

            Sign sign = (Sign) event.getBlock().getState();
            String lastLine = line3.toLowerCase().replaceAll("[\\D]", "").trim();

            //Parse price
            int price;
            try {
                price = Integer.parseInt(lastLine);
            } catch (Exception e) {
                throw FtcExceptionProvider.translatable("shops.created.failed.noPrice");
            }

            //Make sure they don't exceed the max shop price
            if(price > FtcVars.maxSignShopPrice.get()) {
                throw FtcExceptionProvider.maxShopPriceExceeded();
            }

            //They must give at least one line of info about the shop
            if(line2.isBlank() && line1.isBlank()) {
                throw FtcExceptionProvider.translatable("shops.created.failed.noDesc");
            }

            //WorldGuard flag check
            StateFlag.State state = FtcFlags.query(player.getLocation(), FtcFlags.SHOP_CREATION);

            if(state == StateFlag.State.DENY && !player.hasPermission("ftc.admin")) {
                player.sendMessage(
                        Component.text()
                                .append(Component.text("Hey! ")
                                        .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
                                )
                                .append(Component.translatable("shops.created.failed.wgFlag").color(NamedTextColor.GRAY))
                );
                return;
            }

            ShopManager shopManager = Crown.getShopManager();

            //creates the signshop
            SignShop shop = shopManager.createSignShop(sign.getLocation(), shopType, price, player.getUniqueId());

            //Opens the example item selection screen
            player.openInventory(shopManager.getExampleInventory());
            Crown.inst().getServer().getPluginManager().registerEvents(new ExampleItemSelectionListener(player, shop), Crown.inst());

            if(shopType == ShopType.BUY) event.line(0, shopType.outOfStockLabel());
            else event.line(0, shopType.inStockLabel());

            event.line(3, shopManager.getPriceLine(price));
        });
    }

    public class ExampleItemSelectionListener implements Listener{
        private final Player player;
        private final SignShop shop;

        public ExampleItemSelectionListener(Player p, SignShop s){
            player = p;
            shop = s;
        }

        @EventHandler
        public void invClickEvent(InventoryClickEvent event){
            if(event.getCurrentItem() == null) return;
            if(!event.getWhoClicked().equals(player)) return;
            if(event.getCurrentItem().getType() == Material.BARRIER) event.setCancelled(true);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) { //sets the example item and adds the item(s) to the shop's inventory
            if(!event.getPlayer().equals(player)) return;
            if(event.getInventory().getType() != InventoryType.HOPPER) return;

            Player player = (Player) event.getPlayer();
            Sign sign = (Sign) shop.getBlock().getState();

            HandlerList.unregisterAll(this);

            Inventory inv = event.getInventory();
            ShopInventory shopInv = shop.getInventory();

            ItemStack item = inv.getContents()[ShopConstants.EXAMPLE_ITEM_SLOT];
            if(ItemStacks.isEmpty(item)){ //If example item was not found: destroy shop and tell them why they failed
                shop.destroy(false);

                player.sendMessage(
                        Component.text()
                                .append(Component.translatable("shops.created.failed1", NamedTextColor.DARK_RED))
                                .append(Component.space())
                                .append(Component.translatable("shops.created.failed2", NamedTextColor.RED))
                                .build()
                );
                return;
            }

            //Add the item to the inventory
            shopInv.setExampleItem(item);
            shopInv.addItem(item.clone());

            //Send the info message
            player.sendMessage(
                    Component.text()
                            .append(Component.translatable("shops.created.info1", NamedTextColor.GREEN))
                            .append(Component.space())

                            .append(Component.translatable("shops.created.info2",
                                    Component.translatable("shops." + (shop.getType().isBuyType() ? "buy" : "sell")),
                                    FtcFormatter.itemAndAmount(shopInv.getExampleItem()),
                                    FtcFormatter.rhines(shop.getPrice())
                            ))

                            .append(Component.newline())
                            .append(Component.translatable("shops.created.restockInfo", NamedTextColor.GRAY))
                            .build()
            );

            shop.update();
        }
    }
}