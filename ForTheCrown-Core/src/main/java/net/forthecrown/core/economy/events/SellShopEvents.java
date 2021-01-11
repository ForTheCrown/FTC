package net.forthecrown.core.economy.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.economy.SellShop;
import net.forthecrown.core.economy.files.Balances;
import net.forthecrown.core.enums.SellAmount;
import net.forthecrown.core.files.FtcUser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class SellShopEvents implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent event){
        if(!event.getView().getTitle().contains("FTC Shop") && !event.getView().getTitle().contains(" Shop Menu")) return;
        if(event.getSlotType() != InventoryType.SlotType.CONTAINER) return;
        if(event.getCurrentItem() == null) return;
        if(event.isShiftClick()){
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        Inventory inv = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();
        FtcUser user = FtcCore.getUserData(player.getUniqueId());
        SellShop sellShop = new SellShop(user);

        ItemStack item = event.getCurrentItem();

        if(item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

        //change sell amount
        if(item.getType() == Material.BLACK_STAINED_GLASS_PANE){
            switch (item.getItemMeta().getDisplayName()){
                case "Sell 1":
                    user.setSellAmount(SellAmount.PER_1);
                    break;
                case "Sell per 16":
                    user.setSellAmount(SellAmount.PER_16);
                    break;
                case "Sell per 64":
                    user.setSellAmount(SellAmount.PER_64);
                    break;
                case "Sell all":
                    user.setSellAmount(SellAmount.ALL);
                    break;
            }

            switch (event.getView().getTitle().replaceAll(" Shop Menu", "")){
                case "Mob Drops":
                    player.openInventory(sellShop.dropsMenu());
                    return;
                case "Farming Items":
                    player.openInventory(sellShop.farmingMenu());
                    return;
                case "Mining Items":
                    player.openInventory(sellShop.miningMenu());
                    return;
                default:
                    player.openInventory(sellShop.decidingMenu());
                    return;
            }
        }

        switch (item.getType()){
            case GOLD_BLOCK:
            case PAPER:
                player.openInventory(sellShop.decidingMenu());
                break;
            case EMERALD_BLOCK:
                player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.YELLOW + "You can visit the webshop here:");
                player.sendMessage(ChatColor.AQUA + "https://for-the-crown.tebex.io/");
                player.closeInventory();
                break;
            case OAK_SAPLING:
                player.openInventory(sellShop.farmingMenu());
                break;
            case ROTTEN_FLESH:
                player.openInventory(sellShop.dropsMenu());
                break;
            case IRON_PICKAXE:
                player.openInventory(sellShop.miningMenu());
                break;
            default:
                if(!sellItems(item.getType(), player, user)) player.sendMessage("Couldn't sell items!");
        }
    }

    private boolean sellItems(Material toSell, Player player, FtcUser user){
        int sellAmount = 1;
        if(user.getSellAmount() != SellAmount.ALL) sellAmount = Integer.parseInt(user.getSellAmount().toString().replaceAll("PER_", ""));
        int finalSell = sellAmount;

        Balances bals = Economy.getBalances();
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] playerItems = player.getInventory().getContents();
        for(ItemStack stack : playerItems){
            if(stack == null) continue;

            if(stack.getType() == toSell){
                if(user.getSellAmount() == SellAmount.ALL){ //remove the itemstack and add it to the finalSell variable
                    sellAmount = 0;
                    finalSell += stack.getAmount();
                    playerInventory.remove(stack);
                    continue;
                }

                if(stack.getAmount() >= sellAmount){ //if the stack is larger than the remaining sellAmount
                    stack.setAmount(stack.getAmount() - sellAmount);
                    sellAmount = 0;
                    break;
                }

                if(stack.getAmount() <= sellAmount){ //if the stack is smaller than the remaining sellAmount
                    sellAmount -= stack.getAmount(); //lessens the sellAmount so the next item requires only the amount of items still needed wat
                    playerInventory.remove(stack);
                }
            }
        }
        if(sellAmount > 0) return false; //if there's not enough items and you aren't selling all

        UUID uuid = player.getUniqueId();
        final int toPay = finalSell* user.getItemPrice(toSell);
        int comparison0 = user.getItemPrice(toSell);

        String s = toSell.toString().toLowerCase().replaceAll("_", " ");

        bals.setBalance(uuid, bals.getBalance(uuid) + toPay); //does the actual paying and adds the itemsSold to the user
        user.setAmountEarned(toSell, user.getAmountEarned(toSell)+toPay);
        player.sendMessage("You sold " + finalSell + " " + s + " for " + toPay);

        int comparison1 = user.getItemPrice(toSell);

        if(comparison1 < comparison0){
            player.sendMessage("Your price for " + s + " has dropped to " + comparison1);
        }

        return true;
    }

}
