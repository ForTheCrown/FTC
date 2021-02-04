package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.BlackMarket;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.enums.SellAmount;
import net.forthecrown.core.exceptions.CannotAffordTransaction;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.exceptions.NoRankException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class BlackMarketEvents implements Listener {

    @EventHandler
    public void onBlackMarketUse(PlayerInteractEntityEvent event) {
        if(event.getRightClicked().getType() != EntityType.VILLAGER) return;
        if(event.getHand() != EquipmentSlot.HAND) return;
        Villager villie = (Villager) event.getRightClicked();
        if(!villie.isInvulnerable()) return;
        if(villie.getCustomName() == null) return;
        if(!villie.getCustomName().contains("George") && !villie.getCustomName().contains("Otto") && !villie.getCustomName().contains("Herbert") &&
                !villie.getCustomName().contains("Edward") && !villie.getCustomName().contains("Ramun")) return;

        if(FtcCore.isOnCooldown(event.getPlayer())) return;
        FtcCore.addToCooldown(event.getPlayer(), 20, false);

        event.setCancelled(true);

        Player player = event.getPlayer();
        BlackMarket bm = FtcCore.getBlackMarket();
        CrownUser user = FtcCore.getUser(player.getUniqueId());

        if(user.getBranch() != Branch.PIRATES) throw new CrownException(player, villie.getCustomName() + " only trusts real pirates");

        if(villie.getCustomName().contains("George")) player.openInventory(bm.getMiningInventory(user));
        else if(villie.getCustomName().contains("Otto")) player.openInventory(bm.getDropInventory(user));
        else if(villie.getCustomName().contains("Herbert")) player.openInventory(bm.getFarmingInventory(user));
        else if(villie.getCustomName().contains("Edward")){
            if(!bm.isAllowedToBuyEnchant(player)) throw new CrownException(player, "-&eYou've already purchased from me today, scram!&r-");
            if(!bm.enchantAvailable()) throw new CrownException(player, "-&eUnfortunately, my good sir, I don't have anything to sell you at this moment&r-");
            player.openInventory(bm.getEnchantInventory());
        }
        else if(villie.getCustomName().contains("Ramun")) player.openInventory(bm.getParrotInventory());
    }

    @EventHandler
    public void onBmInvUse(InventoryClickEvent event) {
        if(!event.getView().getTitle().contains("Black Market: ") && !event.getView().getTitle().contains("Parrot Shop")) return;
        if(event.isShiftClick()){ event.setCancelled(true); return; }
        if(event.getClickedInventory() instanceof PlayerInventory) return;
        if(!(event.getClickedInventory() instanceof PlayerInventory)) event.setCancelled(true);
        if(event.getCurrentItem() == null) return;

        event.setCancelled(true);
        Material toSell = event.getCurrentItem().getType();

        switch (toSell){
            case GRAY_STAINED_GLASS_PANE:
            case PURPLE_STAINED_GLASS_PANE:
            case END_ROD:
            case IRON_PICKAXE:
            case ROTTEN_FLESH:
            case OAK_SAPLING:
                return;
            default:
        }

        if(event.getCurrentItem().hasItemMeta() && !event.getCurrentItem().getItemMeta().getLore().get(0).contains("Value: ")) return;

        Player player = (Player) event.getWhoClicked();
        BlackMarket bm = FtcCore.getBlackMarket();
        CrownUser user = FtcCore.getUser(event.getWhoClicked().getUniqueId());
        Balances bals = FtcCore.getBalances();

        if(event.getView().getTitle().contains("Parrot Shop")){
            String color = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).replaceAll(" Parrot", "");
            List<String> pets = user.getPets();

            if(pets.contains(color + "_parrot")){
                user.sendMessage("&7You already own this color!");
                return;
            }

            if(event.getCurrentItem().getItemMeta().getLore().get(0).contains("only for Admirals.")){
                if(!user.hasRank(Rank.ADMIRAL)) throw new NoRankException(player, "You need to be an Admiral to get this parrot!");

            } else if(event.getCurrentItem().getItemMeta().getLore().get(0).contains("only for Captains.")){
                if(!user.hasRank(Rank.CAPTAIN)) throw new NoRankException(player, "You need to be a Captain to get this parrot!");
            } else {
                int cost = Integer.parseInt(ChatColor.stripColor(event.getCurrentItem().getLore().get(0)).replaceAll("[\\D]", ""));

                if(bals.getBalance(user.getBase()) < cost) throw new CannotAffordTransaction(player);
                bals.setBalance(user.getBase(), bals.getBalance(user.getBase())  - cost);
            }

            if(pets.contains(color + "_parrot")){
                user.sendMessage("&7You already own this color!");
                return;
            }

            pets.add(color + "_parrot");
            user.setPets(pets);
            user.sendMessage("&7You bought a " + event.getCurrentItem().getItemMeta().getDisplayName() + "&7. Use /color " + color);
            return;
        }

        if(bm.getItemPrice(toSell) == null && toSell != Material.ENCHANTED_BOOK) return;

        if(toSell == Material.ENCHANTED_BOOK) buyEnchant(user, bals, bm);
        else if(!sellItem(user, bals, bm, toSell)) user.sendMessage("&7Couldn't sell items!");

        switch (event.getView().getTitle().toLowerCase().replaceAll("black market: ", "")){
            case "drops":
                player.openInventory(bm.getDropInventory(user));
                break;
            case "mining":
                player.openInventory(bm.getMiningInventory(user));
                break;
            case "crops":
                player.openInventory(bm.getFarmingInventory(user));
                break;
            case "enchants":
                player.openInventory(bm.getEnchantInventory());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + event.getView().getTitle().toLowerCase().replaceAll("black market: ", ""));
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    private boolean sellItem(CrownUser user, Balances bals, BlackMarket bm, Material toSell){
        int sellAmount = user.getSellAmount().getInt();
        int finalSell = sellAmount;
        if(finalSell == -1) finalSell++;

        Player player = user.getPlayer();
        PlayerInventory playerInventory = player.getInventory();

        for(ItemStack stack : playerInventory){
            if(stack == null) continue;
            if(stack.getType() != toSell) continue;

            if(user.getSellAmount() == SellAmount.ALL){ //remove the itemstack and add it to the finalSell variable
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

        String s = toSell.toString().toLowerCase().replaceAll("_", " ");
        int toPay = bm.getItemPrice(toSell) * finalSell;

        bals.addBalance(user.getBase(), toPay, false);
        bm.setAmountEarned(toSell, bm.getAmountEarned(toSell) + toPay);
        user.sendMessage("&7You sold &e" + finalSell + " " + s + " &7for &6" + toPay + " Rhines");

        System.out.println(user.getName() + " sold " + finalSell + " " + s + " for " + toPay);
        return true;
    }

    private void buyEnchant(CrownUser user, Balances bals, BlackMarket bm){
        if(bals.getBalance(user.getBase()) < bm.getEnchantPrice(bm.getDailyEnchantment())) throw new CannotAffordTransaction(user.getPlayer());
        if(user.getPlayer().getInventory().firstEmpty() == -1){
            user.sendMessage("There's no room in your inventory!");
            return;
        }

        int cost = bm.getEnchantPrice(bm.getDailyEnchantment());
        int newBal = bals.getBalance(user.getBase()) - cost;

        bals.setBalance(user.getBase(), newBal);
        user.sendMessage("&7You bought an Enchanted book from &eEdward for &6" + cost + " Rhines");
        user.getPlayer().getInventory().setItem(user.getPlayer().getInventory().firstEmpty(), bm.getDailyEnchantBook());
        bm.setAllowedToBuyEnchant(user.getPlayer(), false);
    }

    //TODO Anvil enchant event for unsafe enchants
}
