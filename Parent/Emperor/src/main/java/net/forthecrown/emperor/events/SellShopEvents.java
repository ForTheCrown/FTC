package net.forthecrown.emperor.events;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.economy.Balances;
import net.forthecrown.emperor.economy.SellShop;
import net.forthecrown.emperor.events.custom.SellShopUseEvent;
import net.forthecrown.emperor.nbt.NbtGetter;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.user.data.SoldMaterialData;
import net.forthecrown.emperor.user.enums.SellAmount;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.emperor.utils.ChatUtils;
import net.forthecrown.emperor.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class SellShopEvents implements Listener {

    private final Player player;
    private final SellShop sellShop;

    public SellShopEvents(Player p, SellShop shop){
        player = p;
        sellShop = shop;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent event){
        if(!event.getWhoClicked().equals(player)) return;
        if(event.isShiftClick()) event.setCancelled(true);
        if(event.getClickedInventory() instanceof PlayerInventory) return;

        event.setCancelled(true);
        if(event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();

        if(Cooldown.contains(player, "Core_SellShop")) return;
        Cooldown.add(player, "Core_SellShop", 6);

        CrownUser user = UserManager.getUser(player.getUniqueId());
        SellShop sellShop = this.sellShop;
        ItemStack item = event.getCurrentItem();

        if(item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
        //change sell amount
        if(item.getType() == Material.BLACK_STAINED_GLASS_PANE){
            String displayName = ChatUtils.getString(item.getItemMeta().displayName());

            int sellAmountInt;
            try {
                sellAmountInt = Integer.parseInt(displayName.replaceAll("[\\D]", "").trim());
            } catch (Exception e){
                sellAmountInt = -1;
            }
            SellAmount newAmount = SellAmount.fromInt((byte) sellAmountInt);
            user.setSellAmount(newAmount);

            //reloads the inventory, cuz changing the lores of all the items is a pain too great to even imagine
            reloadInventory();
            return;
        }

        switch (item.getType()){
            case IRON_BLOCK:
                if(sellShop.getCurrentMenu() == SellShop.Menu.MINING_BLOCKS) break;
                player.openInventory(sellShop.miningBlocksMenu());
                return;
            case GOLD_BLOCK:
                if(sellShop.getCurrentMenu() == SellShop.Menu.MINING_BLOCKS) break;
            case PAPER:
                if(sellShop.getCurrentMenu() == SellShop.Menu.MINING_BLOCKS){
                    player.openInventory(sellShop.miningMenu());
                    return;
                }

                player.openInventory(sellShop.decidingMenu());
                return;
            case EMERALD_BLOCK:
                if(sellShop.getCurrentMenu() == SellShop.Menu.MINING_BLOCKS) break;

                player.closeInventory();
                user.sendMessage("&7Webstore address:", ChatColor.AQUA + "for-the-crown.tebex.io");
                return;
            case OAK_SAPLING:
                player.openInventory(sellShop.cropsMenu());
                return;
            case IRON_PICKAXE:
                player.openInventory(sellShop.miningMenu());
                return;
            case ROTTEN_FLESH:
                if(item.getItemMeta().getDisplayName().contains("Drops")) {
                    player.openInventory(sellShop.dropsMenu());
                    return;
                }
            default:
        }
        Bukkit.getPluginManager().callEvent(new SellShopUseEvent(user, CrownCore.getBalances(), item.getType(), item, sellShop));
    }

    private void reloadInventory(){
        if(sellShop.getCurrentMenu() == null) return;

        //reloads the inventory, cuz changing the lores of all the items is a pain too great to even imagine
        switch (sellShop.getCurrentMenu()){
            case DROPS:
                player.openInventory(sellShop.dropsMenu());
                return;
            case CROPS:
                player.openInventory(sellShop.cropsMenu());
                return;
            case MINING:
                player.openInventory(sellShop.miningMenu());
                return;
            case MINING_BLOCKS:
                player.openInventory(sellShop.miningBlocksMenu());
                return;

            default:
                player.openInventory(sellShop.decidingMenu());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSellShopUse(SellShopUseEvent event){
        CrownUser seller = event.getSeller();
        Material toSell = event.getItem();

        int sellAmount = seller.getSellAmount().getValue();
        int finalSell = sellAmount;

        ItemStack toSellItem = new ItemStack(toSell, finalSell);

        Balances bals = event.getBalances();

        Player player = event.getSellerPlayer();
        PlayerInventory playerInventory = player.getInventory();

        if(!playerInventory.containsAtLeast(toSellItem, sellAmount)){
            seller.sendMessage("&7You don't have enough items to sell");
            event.setCancelled(true);
            return;
        }

        if(seller.getSellAmount() == SellAmount.ALL){
            finalSell = 0;
            for (ItemStack i: playerInventory){
                if(i == null) continue;
                if(i.getType() != toSell) continue;

                finalSell += i.getAmount();
                playerInventory.removeItemAnySlot(i);
            }
        } else playerInventory.removeItemAnySlot(toSellItem);

        UUID uuid = player.getUniqueId();
        int toPay;
        Component s = ChatFormatter.itemName(toSellItem);

        SoldMaterialData data;
        short price;

        if(event.getShop().getCurrentMenu() == SellShop.Menu.MINING_BLOCKS){
            String orig = NbtGetter.ofItemTags(event.getClickedItem()).getString("ingot");
            toSell = Material.valueOf(orig);

            data = seller.getMatData(toSell);
            price = data.getPrice();

            toPay = price * 9 * finalSell;
        } else{
            data = seller.getMatData(toSell);
            price = data.getPrice();

            toPay = price * finalSell;
        }

        int comparison0 = data.getPrice();

        bals.add(uuid, toPay, true); //does the actual paying and adds the itemsSold to the seller
        data.addEarned(toPay);
        data.recalculate();

        /*String message = ChatFormatter.translateHexCodes("sold &e" + finalSell + " " + s + " &7for &6" + Balances.getFormatted(toPay) + ".");

        Announcer.log(Level.INFO, seller.getName() + message);
        seller.sendMessage("&7You " + message);*/

        Component message = Component.text("sold ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(finalSell).color(NamedTextColor.YELLOW))
                .append(Component.text(" "))
                .append(s.color(NamedTextColor.YELLOW))
                .append(Component.text(" for "))
                .append(Balances.formatted(toPay).color(NamedTextColor.GOLD));

        seller.sendMessage(
                Component.text("You ")
                        .color(NamedTextColor.GRAY)
                        .append(message)
        );

        CrownCore.logger().info(seller.getName() + ChatUtils.PLAIN_SERIALIZER.serialize(message));

        int comparison1 = data.getPrice();

        //if the price dropped
        if(comparison1 < comparison0){
            //seller.sendMessage("&7Your price for " + s + " has dropped to " + Balances.getFormatted(comparison1));

            seller.sendMessage(
                    Component.text("Your price for ")
                            .color(NamedTextColor.GRAY)
                            .append(s)
                            .append(Component.text(" has dropped to "))
                            .append(Balances.formatted(comparison1))
            );
            reloadInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!event.getPlayer().equals(player)) return;
        if(event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

        HandlerList.unregisterAll(this);
    }
}