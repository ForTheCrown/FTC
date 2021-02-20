package net.forthecrown.core.events;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.SellAmount;
import net.forthecrown.core.inventories.SellShop;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class SellShopEvents implements Listener {

    @EventHandler
    public void onServerShopNpcUse(PlayerInteractEntityEvent event){
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(event.getRightClicked().getType() != EntityType.WANDERING_TRADER) return;
        LivingEntity trader = (LivingEntity) event.getRightClicked();

        if(trader.hasAI() && trader.getCustomName() == null || !trader.getCustomName().contains("Server Shop")) return;

        event.getPlayer().openInventory(new SellShop(event.getPlayer()).mainMenu());
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent event){
        if(!event.getView().getTitle().contains("FTC Shop") && !event.getView().getTitle().contains(" Shop Menu")) return;
        if(event.isShiftClick()){ event.setCancelled(true); return; }
        if(event.getClickedInventory() instanceof PlayerInventory) return;
        if(!(event.getClickedInventory() instanceof PlayerInventory)) event.setCancelled(true);
        if(event.getCurrentItem() == null) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        if(Cooldown.contains(player, "Core_SellShop")) return;
        Cooldown.add(player, "Core_SellShop", 6);

        CrownUser user = FtcCore.getUser(player.getUniqueId());
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

            //reloads the inventory, cuz changing the lores of all the items is a pain too great to even imagine
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
                player.closeInventory();
                user.sendMessage("&7Webstore address:", ChatColor.AQUA + "for-the-crown.tebex.io");
                break;
            case OAK_SAPLING:
                player.openInventory(sellShop.farmingMenu());
                break;
            case IRON_PICKAXE:
                player.openInventory(sellShop.miningMenu());
                break;
            case ROTTEN_FLESH:
                if(item.getItemMeta().getDisplayName().contains("Drops")) {
                    player.openInventory(sellShop.dropsMenu());
                    break;
                }
            default:
                if(!sellItems(item.getType(), user)) user.sendMessage("&7Couldn't sell items!");
        }
    }

    private boolean sellItems(Material toSell, CrownUser seller){
        int sellAmount = seller.getSellAmount().getInt();
        int finalSell = sellAmount;
        if(sellAmount == -1) finalSell++;

        Player player = seller.getPlayer();

        Balances bals = FtcCore.getBalances();
        PlayerInventory playerInventory = player.getInventory();
        for(ItemStack stack : playerInventory){
            if(stack == null) continue;
            if(stack.getType() != toSell) continue;

            if(seller.getSellAmount() == SellAmount.ALL){ //remove the itemstack and add it to the finalSell variable
                finalSell += stack.getAmount();
                sellAmount = 0;
                playerInventory.removeItem(stack);
                continue;
            }

            if(stack.getAmount() >= sellAmount){ //if the stack is larger than the remaining sellAmount
                stack.setAmount(stack.getAmount() - sellAmount);
                if(stack.getAmount() < 0) playerInventory.removeItem(stack);
                sellAmount = 0;
                break;
            }

            if(stack.getAmount() < sellAmount){ //if the stack is smaller than the remaining sellAmount
                sellAmount -= stack.getAmount(); //lessens the sellAmount so the next item requires only the amount of items still needed
                playerInventory.removeItem(stack);
            }
        }
        if(sellAmount != 0) return false; //if there's not enough items and you aren't selling all

        UUID uuid = player.getUniqueId();
        int toPay = finalSell * seller.getItemPrice(toSell);
        int comparison0 = seller.getItemPrice(toSell);

        String s = CrownUtils.capitalizeWords(toSell.toString().toLowerCase().replaceAll("_", " "));
        s = s.substring(0, s.length()-1) + s.substring(s.length()-1).replaceAll("s", "");
        if(seller.getSellAmount().getInt() > 1) s += "s";

        bals.addBalance(uuid, toPay, true); //does the actual paying and adds the itemsSold to the seller
        seller.setAmountEarned(toSell, seller.getAmountEarned(toSell)+toPay); //How the fuck does this keep resetting everytime
        seller.sendMessage("&7You sold &e" + finalSell + " " + s + " &7for &6" + toPay + " Rhines");

        System.out.println(seller.getName() + " sold " + finalSell + " " + s + " for " + toPay);

        int comparison1 = seller.getItemPrice(toSell);

        //if the price dropped
        if(comparison1 < comparison0) seller.sendMessage("&7Your price for " + s + " has dropped to " + comparison1);

        return true;
    }

}
