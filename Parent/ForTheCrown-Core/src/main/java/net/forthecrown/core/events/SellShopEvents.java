package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.enums.SellAmount;
import net.forthecrown.core.events.customevents.SellShopUseEvent;
import net.forthecrown.core.inventories.SellShop;
import net.forthecrown.core.nbt.NbtGetter;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.core.utils.CrownUtils;
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
import java.util.logging.Level;

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
            String displayName = ComponentUtils.getString(item.getItemMeta().displayName());

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
        Bukkit.getPluginManager().callEvent(new SellShopUseEvent(user, FtcCore.getBalances(), item.getType(), item, sellShop));
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

        Announcer.debug(event.getShop().getCurrentMenu());
        String s = CrownUtils.normalEnum(toSell);

        if(event.getShop().getCurrentMenu() == SellShop.Menu.MINING_BLOCKS){
            String orig = NbtGetter.ofItemTags(event.getClickedItem()).getString("ingot");
            Announcer.debug(orig);
            toSell = Material.valueOf(orig);
            toPay = seller.getItemPrice(toSell) * 9 * finalSell;
        } else toPay = seller.getItemPrice(toSell) * finalSell;

        int comparison0 = seller.getItemPrice(toSell);

        bals.add(uuid, toPay, true); //does the actual paying and adds the itemsSold to the seller
        seller.setAmountEarned(toSell, seller.getAmountEarned(toSell)+toPay); //How the fuck does this keep resetting everytime

        String message = CrownUtils.translateHexCodes(" sold &e" + finalSell + " " + s + " &7for &6" + Balances.getFormatted(toPay) + ".");

        Announcer.log(Level.INFO, seller.getName() + message);
        seller.sendMessage("&7You " + message);

        int comparison1 = seller.getItemPrice(toSell);

        //if the price dropped
        if(comparison1 < comparison0){
            seller.sendMessage("&7Your price for " + s + " has dropped to " + Balances.getFormatted(comparison1));
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